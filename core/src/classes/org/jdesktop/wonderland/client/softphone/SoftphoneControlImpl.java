/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.client.softphone;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.cell.CallID;

import java.io.BufferedOutputStream;

import java.io.IOException;
import java.io.InputStream;


import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ThreadManager;

public class SoftphoneControlImpl {
    private static final Logger logger =
            Logger.getLogger(SoftphoneControlImpl.class.getName());
 
    private Process softphoneProcess;
    private OutputStream softphoneOutputStream;
    private ProcOutputListener stdOutListener;
    private ProcOutputListener stdErrListener;
    private Pinger pinger;
    
    private String username;
    private String registrar;
    private int registrarTimeout;
    private String localHost;
    
    private String softphoneAddress;

    private boolean connected;
    
    private boolean exitNotificationSent;

    private boolean isTooLoud;

    private static SoftphoneControlImpl softphoneControlImpl;

    private String callID;

    private SoftphoneControlImpl() {
    }

    /**
     * State of the softphone
     */
    enum State { VISIBLE, INVISIBLE, MUTED, UNMUTED, CONNECTED, DISCONNECTED, EXITED, TOO_LOUD }
    
    /**
     * Gets the one instance of SoftphoneControlImpl
     */
    public static SoftphoneControlImpl getInstance() {
        if (softphoneControlImpl == null) {
            softphoneControlImpl = new SoftphoneControlImpl();
        }
        return softphoneControlImpl;
    }

    /**
     * Start up the softphone
     */
    public String startSoftphone(String username, String registrar,
	    int registrarTimeout, String localHost) throws IOException {
    
	this.username = username.replaceAll("\\p{Punct}", "_");

	String previousRegistrar = this.registrar;

	this.registrar = registrar;
	this.registrarTimeout = registrarTimeout;
	this.localHost = localHost;

	if (localHost != null && localHost.equalsIgnoreCase("default")) {
	    localHost = null;
	}
        
        // if it's already running, send it a command to re-register
        if (isRunning()) {
	    if (previousRegistrar == null ||
		    previousRegistrar.equals(registrar) == false) {

	        register(registrar);
	    } else {
		logger.fine(
		    "startSoftphone:  new registrar same as previous "
		    + " registrar");
	    }

	    return softphoneAddress;
        }

        // is this a valid JVM in which to run?
        String javaVersion = System.getProperty("java.version");
        if (!JavaVersion.isMacOSX() &&
                (JavaVersion.compareVersions(javaVersion, "1.5.0") < 0))
        {
	    logger.warning("java.version is " + javaVersion);
            logger.warning("Softphone needs 1.5.0 or later to run");
	    throw new IOException("Softphone needs java 1.5.0 or later to run");
        }
        
	quiet = true;

	exitNotificationSent = false;

        // launch the sucker!
        String[] command = getSoftphoneCommand(username, registrar,
	    registrarTimeout, localHost, quality);

	if (command == null) {
	    logger.warning("Unable to find softphone.jar.  "
		+ "You cannot use the softphone!");

	    softphoneProcess = null;

	    throw new IOException("Unable to find softphone.jar");
	}

	String s = "";

        for (int i = 0; i < command.length; i++) {
	    s += " " + command[i];
        }

        System.out.println("Launching communicator: " + s);

        softphoneProcess = Runtime.getRuntime().exec(command);
            
	// open communication channels to the new process
        softphoneOutputStream =
            new BufferedOutputStream(softphoneProcess.getOutputStream());
        stdOutListener = new ProcOutputListener(softphoneProcess.getInputStream());
        stdErrListener = new ProcOutputListener(softphoneProcess.getErrorStream());
        stdOutListener.start();
        stdErrListener.start();
        pinger = new Pinger();
        pinger.start();

	if (isVisible) {
	    setVisible(true);
	}

        return waitForAddress();
    }
    
    /**
     * Attempts to wait for the softphone to register itself and get
     * a sip address.  Displays a waiting dialog after a time, and allows
     * the user to cancel the wait.
     * <p>This method SHOULD NOT be called from the AWT event thread.
     * @return the sip address of the softphone, or null if the softphone
     * could not be launched or was canceled by the user.
     */
    private String waitForAddress() throws IOException {
	synchronized (this) {
	    try {
                wait(60000);
	    } catch (InterruptedException e) {
	    }

	    if (softphoneAddress == null) {
                logger.warning("Softphone failed to start!");
	    }
	}

	return softphoneAddress;
    }

    /**
     * Get the location of softphone.jar.  The location is determined as
     * follows:
     *     <li>look for the file specified by the system property
     *         com.sun.mc.softphone.jar
     *     <li>Search the classpath for softphone.jar
     *
     * @return a path that points to softphone.jar, or null if the path
     * cannot be found
     */
    private String getJarPath() {
        // try the system property
        String jarPath = System.getProperty(SoftphoneControl.SOFTPHONE_PROP);

        if (jarPath != null && checkPath(jarPath)) {
            return jarPath;
        }

        // try the classpath
        String paths[] = System.getProperty("java.class.path").split(
                System.getProperty("path.separator"));

        for (int i=0; i<paths.length; i++) {
	    String path = paths[i];

            if (path.endsWith("softphone.jar")) {
		if (checkPath(path)) {
                    return path;
		}
            }
	
	    path += File.separator + "softphone.jar";

	    if (checkPath(path)) {
                return path;
	    }
        }

        // no luck 
        return null;
    }

    /**
     * Check if the given path is a valid jar file
     * @param path the path
     * @return true if this is a valid jar file, or false if not
     */
    private boolean checkPath(String jarPath) {
        return new File(jarPath).exists();
    }
    
    /**
     * Gets a String array suitable for System.exec() that will launch the
     * softphone program on behalf of a particular user
     * @param username the name of the user to display in the softphone
     */
    private String[] getSoftphoneCommand(String username,
	    String registrar, int registrarTimeout, String localHost,
            AudioQuality quality) {

        String javaHome = System.getProperty("java.home");
        
        String softphonePath = getJarPath();

	if (softphonePath == null) {
	    return null;
	}

        String fileSeparator = System.getProperty("file.separator");

        /*
         * Set path for native libraries
         */
	int ix = softphonePath.indexOf(fileSeparator + "softphone.jar");

        System.setProperty("java.library.path", softphonePath.substring(0, ix));
        
	int cmdLength = 11;
	
	if (registrarTimeout != 0) {
	    cmdLength += 2;
	}

	if (localHost != null) {
	    cmdLength += 2;
	}

        if (quality != null) {
            cmdLength += 8;
        }
        
	if (isMuted == true) {
	    cmdLength ++;
	}

        String[] command = new String[cmdLength];
        
        command[0] = javaHome + fileSeparator + "bin"
                + fileSeparator + "java";
	command[1] = "-Dsun.java2d.noddraw=true";
        command[2] = "-jar";
        command[3] = softphonePath;
        command[4] = "-mc";
        command[5] = "-u";
        command[6] = username;
	command[7] = "-r";
	command[8] = registrar;
	command[9] = "-stun";

	String[] tokens = registrar.split(":");

	ix = tokens[0].indexOf(";");
	
	if (ix >= 0) {
	    command[10] = tokens[0].substring(0, ix);
	} else {
	    command[10] = tokens[0];
	}

	if (tokens.length >= 2) {
	    command[10] += ":" + tokens[1];
	}

	int i = 11;

	if (registrarTimeout != 0) {
	    command[i++] = "-t";
	    command[i++] = String.valueOf(registrarTimeout);
	}

	if (localHost != null) {
	    command[i++] = "-l";
	    command[i++] = localHost;
	}
              
        if (quality != null) {
            command[i++] = "-sampleRate";
            command[i++] = String.valueOf(quality.sampleRate());
            command[i++] = "-channels";
            command[i++] = String.valueOf(quality.channels());
            command[i++] = "-transmitSampleRate";
            command[i++] = String.valueOf(quality.transmitSampleRate());
            command[i++] = "-transmitChannels";
            command[i++] = String.valueOf(quality.transmitChannels());
        }
        
	if (isMuted == true) {
	    command[i++] = "-mute";
	}

        return command;
    }

    public void stopSoftphone() {
        close(null);

	synchronized (this) {
	    notifyAll();
	}
    }
    
    public void setCallID(String callID) {
	this.callID = callID;
    }

    public String getCallID() {
	return callID;
    }

    public void register(String registrarAddress) {
	sendCommandToSoftphone("ReRegister=" + registrarAddress);
    }

    public boolean isRunning() {
	if (softphoneProcess == null) {
	    return false;
	}

	try {
	    int exitValue = softphoneProcess.exitValue();	// softphone exited

	    logger.warning("Softphone exited with status " + exitValue);
            close(null); // Software phone was closed.

	    synchronized (this) {
	        notifyAll();
	    }

	    return false;
	} catch (IllegalThreadStateException e) {
	    return true;		// still running
	}
    }

    public boolean isConnected() throws IOException {
	if (isRunning() == false) {
	    throw new IOException("Softphone is not running");
	}

	return connected;
    }

    public boolean isTooLoud() {
	boolean isTooLoud = this.isTooLoud;

	this.isTooLoud = false;

	return isTooLoud;
    }

    private void restartSoftphone() {
	close(null);
	notifyListeners(State.EXITED);
    }
    
    private boolean isVisible;

    public boolean isVisible() {
	return isRunning() && isVisible;
    }

    public void setVisible(boolean isVisible) {
	if (isRunning() == false) {
	    notifyListeners(State.INVISIBLE);
	    return;
	}

	if (isVisible) {
	    sendCommandToSoftphone("Show");
	} else {
	    sendCommandToSoftphone("Hide");
	}
    }

    private boolean isMuted;

    public void mute(boolean isMuted) {
        if (isMuted) {
            sendCommandToSoftphone("Mute");
        } else {
            sendCommandToSoftphone("Unmute");
        }
    }
    
    public boolean isMuted() {
	return isMuted;
    }

    private AudioQuality quality = AudioQuality.VPN;

    public AudioQuality getAudioQuality() {
	return quality;
    }

    public void setAudioQuality(AudioQuality quality) {
	this.quality = quality;

        sendCommandToSoftphone("sampleRate=" + quality.sampleRate());
        sendCommandToSoftphone("channels=" + quality.channels());
        sendCommandToSoftphone("transmitSampleRate=" + quality.transmitSampleRate());
        sendCommandToSoftphone("transmitChannels=" + quality.transmitChannels());
    }

    public void sendCommandToSoftphone(String cmd) {
        if (softphoneOutputStream == null) {
	    System.out.println("Unable to send command to softphone, output stream is null "
		+ cmd);
            return;
        }
        synchronized(softphoneOutputStream) {
            logger.finest("SoftphoneControl sending command to softphone:  " + cmd);

            try {
                byte bytes[] = (cmd+"\n").getBytes();
                softphoneOutputStream.write(bytes);
                softphoneOutputStream.flush();
            } catch (IOException e) {
                //e.printStackTrace();
                softphoneOutputStream = null;

		System.out.println("SoftphoneControl exception:  " + e.getMessage());

		close(
                    "There was an error trying to use the software phone.  "
                    + "Please check your system's audio settings and try again.");
            }
        }
    }
    
    public void runLineTest() {
        sendCommandToSoftphone("linetest");
    }

    public void logAudioProblem() {
        sendCommandToSoftphone("stack");
    }

    private ArrayList<SoftphoneListener> listeners = 
	new ArrayList<SoftphoneListener>();

    public void addSoftphoneListener(SoftphoneListener listener) {
        synchronized(listeners) {
	    if (listeners.contains(listener)) {
		logger.warning("Duplicate listener!!!");
		return;
	    }

            listeners.add(listener);
        }
    }

    public void removeSoftphoneListener(SoftphoneListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    private boolean quiet = false;

    private void lineReceived(ProcOutputListener source, String line) {
        if (source == stdErrListener) {
            System.err.println(line);
        } else if (source == stdOutListener) {
	    if (quiet == false) {	
                logger.info(line);
	    }
      
	    if (line.indexOf("Connected:") >= 0) {
	    	quiet = true;
	    }

	    if (line.indexOf("Connected") >= 0) {
		connected = true;
		mute(isMuted);
                notifyListeners(State.CONNECTED);
	    } else if (line.indexOf("Disconnected") >= 0) {
		connected = false;
                notifyListeners(State.DISCONNECTED);
	    }

            // WARNING:  The Sip Communicator generates this exact
            // message.  If the communicator is changed,
            // you'll need to change searchString.
            String searchString = "SipCommunicator Public Address is '";
            int idx = line.indexOf(searchString);
            if (idx >= 0) {
                String addr = line.substring(idx + searchString.length());
                int ixEnd = addr.indexOf("'");
                if (ixEnd>0) {
                    addr = addr.substring(0, ixEnd);
                }

		synchronized (this) {
                    softphoneAddress = addr;

		    notifyAll();
		}

                logger.info("softphone address is '" + addr + "'");
            } else if (line.indexOf("Softphone is visible") >= 0) {
                isVisible = true;
                notifyListeners(State.VISIBLE);
            } else if (line.indexOf("Softphone is hidden") >= 0) {
                isVisible = false;
                notifyListeners(State.INVISIBLE);
            } else if (line.indexOf("Softphone Muted") >= 0) {
		isMuted = true;
                notifyListeners(State.MUTED);
            } else if (line.indexOf("Softphone Unmuted") >= 0) {
		isMuted = false;
                notifyListeners(State.UNMUTED);
            } else if (line.indexOf("Restart softphone now") >= 0) {
                restartSoftphone();
            } else if (line.indexOf("TOO LOUD") >= 0) {
		isTooLoud = true;
            }
        }
    }

    /**
     * Notify listeners of a change
     * @param visible if true, the change is visibility, if false, the
     * change is to the mute state
     * @param state the mute or visibility state
     */
    private void notifyListeners(State state) {
        synchronized(this.listeners) {
	    ArrayList<SoftphoneListener> listeners = new ArrayList<SoftphoneListener>();

	    for (SoftphoneListener listener : this.listeners) {
		listeners.add(listener);
	    }

	    if (state == State.EXITED) {
		if (exitNotificationSent) {
		    return;
		}

		exitNotificationSent = true;
	    }

	    for (SoftphoneListener listener : listeners) {
                switch (state) {
                case VISIBLE:
                    listener.softphoneVisible(true);
                    break;
                case INVISIBLE:
                    listener.softphoneVisible(false);
                    break;
                case MUTED:
                    listener.softphoneMuted(true);
                    break;
                case UNMUTED:
                    listener.softphoneMuted(false);
                    break;
                case CONNECTED:
                    listener.softphoneConnected(true);
                    break;
                case DISCONNECTED:
                    listener.softphoneConnected(false);
                    break;
                case EXITED:
                    listener.softphoneExited();
                    break;
                }
            }
	}
    }

    /**
     * Tickle the softphone every 5 seconds.  If it doesn't hear from us,
     * it will quit.
     */
    class Pinger extends Thread {
        public Pinger() {
            super(ThreadManager.getThreadGroup(), "Softphone pinger");
        }
        
        public void run() {
            try {
		logger.info("About to ping softphone for the first time...");

                while (true) {
                    sendCommandToSoftphone("ping");
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ie) {}
        }
    }

    /**
     * Listens for lines from an input stream.
     */
    class ProcOutputListener implements Runnable {
        BufferedReader reader;
        Thread anim;

        public ProcOutputListener(InputStream stream) {
            this.reader = new BufferedReader(new InputStreamReader(stream));
        }
        
        public void start() {
            anim = new Thread(ThreadManager.getThreadGroup(), this);
            anim.start();
        }
        
        public void stop() {
            Thread hold = anim;
            anim = null;
            try {
		logger.finer("SipStarter closing input stream");
                reader.close();
            } catch (IOException ioe) {}
        }
        
        public void run() {
            try {
                while (anim == Thread.currentThread()) {
                    String line = reader.readLine();
                    if (line == null) {
			logger.info("readLine returned null!");
                        anim = null;
                    } else {
                        lineReceived(this, line);
                    }
                }
            } catch (IOException ioe) {
                if (anim != null) {
                    ioe.printStackTrace();
                }
            } finally {
		close("Lost connection to the software phone unexpectedly.");
            }
        }
        
    }

    private void close(final String failureMessage) {
	connected = false;
        
	if (failureMessage != null) {
	    logger.info("SipStarter close:  " + failureMessage);
	}

        boolean wasOpen = false;
        synchronized(this) {
            if (softphoneOutputStream != null) {
		logger.finer("SipStarter sending Shutdown to softphone");
                sendCommandToSoftphone("Shutdown");
            }

	    softphoneAddress = null;

            if (softphoneOutputStream != null) {
                try {
                    softphoneOutputStream.close();
                } catch (IOException ioe) {
		}
            }

	    logger.finer("SipStarter setting output stream to null");

            softphoneOutputStream = null;
            if (stdOutListener != null) {
                stdOutListener.stop();
                stdOutListener = null;
            }
            if (stdErrListener != null) {
                stdErrListener.stop();
                stdErrListener = null;
            }
            if (pinger != null) {
                pinger.interrupt();
                pinger = null;
            }
            softphoneProcess = null;
        }

	notifyListeners(State.EXITED);
    }
    
    /**
     * Utilities for determining Java version, platform, etc.
     * @author jkaplan
     */
    static class JavaVersion {
        /** 
         * Determine if this platform is Mac OS X.  
         * @return true for Mac OX, or false if not.
         */
        public static boolean isMacOSX() {
            String osName = System.getProperty("os.name");
            return osName.equalsIgnoreCase("Mac OS X");
        }
    
        /**
         * Compare the two given java versions.  The results are -1 if the
         * first version is earlier than the second version, 0 if the versions
         * are equal, or 1 if the first version is later than the second
         * version.  
         * <p>
         * The format of a Java version is major.minor.micro-qualifier.  Only
         * the version numbers (major, minor and micro) are compared.  Any
         * unspecified values are not compared, so 1.4.x is equivalent to
         * 1.4.
         * <p>
         * @param first the first version to compare, as a String (e.g. "1.4.2")
         * @param second the second version to compare, as a String
         * @return -1, 0, or 1 as appropriate
         */ 
        public static int compareVersions(String first, String second) {
            String[] firstSplits = first.split("\\D");
            String[] secondSplits = second.split("\\D");
        
            // start out thinking they are equal
            int res = 0;
        
            // compare as many digits as the shorter string
            int size = Math.min(firstSplits.length, secondSplits.length);   
            for (int i = 0; i < size; i++) {
                int f = Integer.parseInt(firstSplits[i]);
                int s = Integer.parseInt(secondSplits[i]);
            
                // see if we've found a number where they differ
                if (f < s) {
                    res = -1;
                    break;
                } else if (f > s) {
                    res = 1;
                    break;
                }
            }
        
            return res;
        }

        /** test method */
        public static void main(String[] args) {
            System.out.println("IsMacOSX: " + isMacOSX());
            System.out.println("Compare 1.5 to 1.4.2: " + 
                    compareVersions("1.5", "1.4.2"));
            System.out.println("Compare 1.5.0 to 1.5.1: " + 
                    compareVersions("1.5.0", "1.5.1"));
            System.out.println("Compare 1.5 to 1.5.3: " + 
                    compareVersions("1.5", "1.5.3"));
            System.out.println("Compare 1.4.3 to 1.5.0: " + 
                    compareVersions("1.4.3", "1.5.0"));
            System.out.println("Compare 1.6.0-beta2 to 1.5.0: " +
    		    compareVersions("1.6.0-beta2", "1.5.0"));
        }
    }

    public static void main(String args[]) {
	SoftphoneControlImpl softphoneControlImpl = SoftphoneControlImpl.getInstance();

	try {
	    String address = softphoneControlImpl.startSoftphone(
	        System.getProperty("user.name"), "swbridge.east.sun.com:5060", 0, null);

	    logger.warning("Softphone address is " + address);

	    softphoneControlImpl.sendCommandToSoftphone(
		"PlaceCall=conferenceId:xxx,callee=20315");
	} catch (IOException e) {
	    logger.warning(e.getMessage());
	}
    }

}
