/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.jme;

import java.awt.event.ActionEvent;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.jme.login.JmeLoginUI;
import imi.loaders.repository.Repository;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.mtgame.PhysicsSystem;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.common.ThreadManager;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.MainFrame.ServerURLListener;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;

/**
 *
 */
public class JmeClientMain {
    private static final Logger logger =
            Logger.getLogger(JmeClientMain.class.getName());

    public static final String SERVER_URL_PROP = "sgs.server";

    /** The frame of the Wonderland client window. */
    private static MainFrame frame;

    // standard properties
    private static final String PROPS_URL_PROP = "run.properties.file";
    private static final String CONFIG_DIR_PROP = "wonderland.client.config.dir";
    // default values
    private static final String SERVER_URL_DEFAULT = "http://localhost:8080";

    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 30;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    
    // the current Wonderland login and session
    private JmeLoginUI login;
    private JmeClientSession curSession;

    public JmeClientMain(String[] args) {
        // process command line arguments
        processArgs(args);

        // load properties in a properties file
        URL propsURL = getPropsURL();
        loadProperties(propsURL);

        // make sure the server URL is set
        String serverURL = System.getProperty(SERVER_URL_PROP);
        if (serverURL == null) {
            serverURL = SERVER_URL_DEFAULT;
            System.setProperty(SERVER_URL_PROP, serverURL);
        }

        WorldManager worldManager = ClientContextJME.getWorldManager();
        worldManager.addUserData(Repository.class, new Repository(worldManager));
        worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        createUI(worldManager);

        // Register our loginUI for login requests
        login = new JmeLoginUI(frame);
        LoginManager.setLoginUI(login);

        // add a listener that will be notified when the user selects a new
        // server
        frame.addServerURLListener(new ServerURLListener() {
            public void serverURLChanged(final String serverURL) {
                // run in a new thread so we don't block the AWT thread
                new Thread(ThreadManager.getThreadGroup(), new Runnable() {
                    public void run() {
                        try {
                            loadServer(serverURL);
                        } catch (IOException ioe) {
                            logger.log(Level.WARNING, "Error connecting to " +
                                       serverURL, ioe);
                        }
                    }
                }).start();
            }

            public void logout() {
                new Thread(ThreadManager.getThreadGroup(), new Runnable() {
                    public void run() {
                        JmeClientMain.this.logout();
                    }
                }).start();
            }
        });

        JMenuItem physicsMI = new JCheckBoxMenuItem("Physics Enabled");
        physicsMI.setSelected(false);
        physicsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PhysicsSystem phySystem = ClientContextJME.getPhysicsSystem(LoginManager.find(curSession), "Default");
                if (phySystem instanceof JBulletPhysicsSystem) {
                    ((JBulletPhysicsSystem)phySystem).setStarted(((JCheckBoxMenuItem)e.getSource()).isSelected());
                } else {
                    logger.severe("Unsupported physics system "+phySystem);
                }
            }
        });
        frame.addToEditMenu(physicsMI);

        // connect to the default server
        try {
            loadServer(serverURL);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error connecting to default server " +
                       serverURL, ioe);
        }
        
    }

    protected void loadServer(String serverURL) throws IOException {
        logout();

        // get the login manager for the given server
        ServerSessionManager lm = LoginManager.getInstance(serverURL);

        // Register default collision and physics systems for this session
        JBulletDynamicCollisionSystem collisionSystem = (JBulletDynamicCollisionSystem)
                ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
        JBulletPhysicsSystem physicsSystem = (JBulletPhysicsSystem)
                ClientContextJME.getWorldManager().getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem);
        ClientContextJME.addCollisionSystem(lm, "Default", collisionSystem);
        ClientContextJME.addPhysicsSystem(lm, "Default", physicsSystem);

        // create a new session
        try {
            curSession = lm.createSession(login);
        } catch (LoginFailureException lfe) {
            IOException ioe = new IOException("Error connecting to " +
                                              serverURL);
            ioe.initCause(lfe);
            throw ioe;
        }

        // make sure we logged in successfully
        if (curSession == null) {
            logger.log(Level.WARNING, "Unable to connect to session");
            return;
        }

        // Listen for session disconnected and remove session physics and collision systems
        curSession.addSessionStatusListener(new SessionStatusListener() {
            public void sessionStatusChanged(WonderlandSession session, Status status) {
                if (status==Status.DISCONNECTED) {
                    ClientContextJME.removeAllPhysicsSystems(LoginManager.find(session));
                    ClientContextJME.removeAllCollisionSystems(LoginManager.find(session));
                }
            }
        });

        // set the primary login manager and session
        LoginManager.setPrimary(lm);
        lm.setPrimarySession(curSession);
        frame.setServerURL(serverURL);

    }

    protected void logout() {
        // disconnect from the current session
        if (curSession != null) {
            curSession.getCellCache().detachRootEntities();
            curSession.logout();
            curSession = null;
        }
    }

    protected URL getPropsURL() {
        String propURLStr = System.getProperty(PROPS_URL_PROP);
        try {
            URL propsURL;
            
            if (propURLStr == null) {
                String configDir = System.getProperty(CONFIG_DIR_PROP);
                if (configDir == null) {
                    File userDir = new File(System.getProperty("user.dir"));
                    configDir = userDir.toURI().toURL().toString();
                }

                // use the default
                URL configDirURL = new URL(configDir);
                propsURL = new URL(configDirURL, "run-client.properties");
            } else {
                propsURL = new URL(propURLStr);
            }
            
            return propsURL;
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to load properties", ioe);
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (Webstart.isWebstart()) {
            Webstart.webstartSetup();
        }
        
        JmeClientMain worldTest = new JmeClientMain(args);
        
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }

            if (args[i].equals("-p")) {
                System.setProperty(PROPS_URL_PROP, "file:" + args[i+1]);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {
        ViewManager.initialize(width, height); // Initialize an onscreen view
        
        frame = new MainFrame(wm, width, height);
        // center the frame
        frame.setLocationRelativeTo(null);

        // show frame
        frame.setVisible(true);

        ViewManager.getViewManager().attachViewCanvas(frame.getCanvas3DPanel());



	// Initialize the input manager.
	// Note: this also creates the view manager.
	// TODO: low bug: we would like to initialize the input manager BEFORE frame.setVisible.
	// But if we create the camera before frame.setVisible the client window never appears.
	CameraComponent cameraComp = ViewManager.getViewManager().getCameraComponent();
	InputManager inputManager = ClientContext.getInputManager();
	inputManager.initialize(frame.getCanvas(), cameraComp);

	// Default Policy: Enable global key and mouse focus everywhere 
	// Note: the app base will impose its own (different) policy later
	inputManager.addKeyMouseFocus(inputManager.getGlobalFocusEntity());

	//TODO: temporary: example global key event listener for Paul */
	InputManager3D.getInputManager().addGlobalEventListener(
	    new EventClassFocusListener () {
		private final Logger logger = Logger.getLogger("My Logger");
		public Class[] eventClassesToConsume () {
		    return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
		}
		public void commitEvent (Event event) {
		    // NOTE: to test, change the two logger.fine calls below to logger.warning
		    if (event instanceof KeyEvent3D) {
			if (((KeyEvent3D)event).isPressed()) {
			    logger.fine("Global listener: received key event, event = " + event );
			}
		    } else {
			logger.fine("Global listener: received mouse event, event = " + event);
		    }
		}
    	    });
    }

    /**
     * Load system properties and properties from the named file
     */
    /**
     * Returns the frame of the Wonderland client window.
     */
    public static MainFrame getFrame () {
        return frame;
    }

    protected void loadProperties(URL propsURL) {
        // load the given file
        if (propsURL != null) {
            try {
                System.getProperties().load(propsURL.openStream());
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error reading properties from " +
                           propsURL, ioe);
            }
        }
    }    
}
