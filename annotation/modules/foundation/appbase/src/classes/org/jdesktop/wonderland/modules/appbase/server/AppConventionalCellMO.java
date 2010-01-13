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
package org.jdesktop.wonderland.modules.appbase.server;

import java.io.Serializable;
import java.util.UUID;
import com.jme.bounding.BoundingVolume;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellClientState;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellCreateMessage;
import org.jdesktop.wonderland.common.cell.CellTransform;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.state.BasicCellServerStateHelper;

/**
 * The server-side cell for an 2D conventional application.
 *
 * This cell can be created in two different ways:
 * <br><br>
 * 1. World-launched App
 * <br><br>
 * When WFS launches the app it uses the default constructor and
 * calls <code>setServerState</code> to transfer the information from the wlc file
 * into the cell. 
 * <br><br>
 * In this case the wlc <code>setServerState</code> must specify:
 * <ol>
 * + command: The command to execute. This must not be a non-empty string.         
 * </ol>
 * The wlc <code>setServerState</code> can optionally specify:
 * <ol>
 * + <code>appName</code>: The name of the application (Default: "NoName").
 * </ol>
 * <ol>
 * + <code>pixelScaleX</code>: The number of world units per pixel in the cell local X direction (Default: 0.01).
 * </ol>
 * <ol>
 * + <code>pixelScaleY</code> The number of world units per pixel in the cell local Y direction (Default: 0.01).
 * </ol>
 * In this case <code>userLaunched</code> is set to false.
 *<br><br>
 * 2. User-launched App
 *<br><br> 
 * When the user launches an app it sends a command to the server. The handler for
 * this command uses the non-default constructor of this class to provide the
 * necessary information to the client
 *<br><br>
 * In this case <code>userLaunched</code> is set to true.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppConventionalCellMO extends App2DCellMO { 

    /** Whether the app has been launched by the world or user */
    protected boolean userLaunched;

    /** The name of the app */
    protected String appName;

    /** The host on which to run the app master */
    protected String masterHost;

    /** Will the app be moved to the best view on the master after start up? */
    protected boolean bestView;

    /** Subclass-specific data for making a peer-to-peer connection between master and slave. */
    private Serializable connectionInfo;

    /** 
     * The unique ID of the app. For user-launched apps this is assigned
     * by the master and therefore is only unique within the master client.
     * For world-launched apps this is assigned by the server and is
     * therefore unique within the entire system.
     */
    protected UUID appId;

    /** 
     * The command the master should use to execute the app program.
     * This is only used in the case of world-launched apps.
     */
    protected String command;

    /** Client state data */
    private AppConventionalCellClientState clientState;

    /** Default constructor, used when the cell is created via WFS */
    public AppConventionalCellMO() {
	super();
    }

    /**
     * Creates a new instance of a user-launched <code>AppConventionalCellMO</code>.
     *
     * @param msg The creation message received from the client.
     */
    public AppConventionalCellMO (AppConventionalCellCreateMessage msg) {
        super(calcBounds(msg.getBestView(), msg.getBounds()), 
	      calcTransform(msg.getBestView(), msg.getTransform()), 
	      msg.getPixelScale());
	this.masterHost = msg.getMasterHost();
	this.appName = msg.getAppName();
	this.appId = msg.getAppId();
	this.bestView = msg.getBestView();
	this.connectionInfo = msg.getConnectionInfo();
	userLaunched = true;
    }

    /**
     * If bestView is true, returns a reasonable "best view" bounds. Otherwise just returns the given bounds.
     */
    private static BoundingVolume calcBounds (boolean bestView, BoundingVolume bounds) {
	if (bestView) {
	    // Override bounds with a temporary value which will get the cell loaded into 
	    // the client caches before permanent positioning.
	    bounds = new BoundingBox(new Vector3f(0f, 0f, 0f),  1f, 1f, 1f);
	}
	return bounds;
    }

    /**
     * If bestView is true, supply a reasonable "best view" transform. Otherwise just return the given transform.
     */
    private static CellTransform calcTransform (boolean bestView, CellTransform transform) {
	if (bestView) {
	    // Override origin with a temporary value which will get the cell loaded into 
	    // the client caches before permanent positioning.
	    transform = new CellTransform(null, null);
	}
	return transform;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getClientState (CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
	if (clientState == null) {
	    clientState = new AppConventionalCellClientState(masterHost, appName, pixelScale, connectionInfo);
	    if (userLaunched) {
		clientState.setUserLaunched(true);
		clientState.setAppId(appId);
		clientState.setBestView(bestView);
		clientState.setConnectionInfo(connectionInfo);
	    } else {
		clientState.setUserLaunched(false);
		clientState.setCommand(command);
	    }
	}
	return clientState;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
	super.setServerState(serverState);

	AppConventionalCellServerState state = (AppConventionalCellServerState) serverState;

	// TODO: what should this be?
	//masterHost = NetworkAddress.getDefaultHostAddress();
	masterHost = "localHost";

	appName = state.getAppName();

	command = state.getCommand();
	if (command == null || command.length() <= 0) {
	    // TODO: what is the proper way to signal this error which is non-fatal to the server?
	    throw new RuntimeException("Invalid app cell command");
	}

	pixelScale = state.getPixelScale();
    }

    /**
     * Return a new CellServerState Java bean class that represents the current
     * state of the cell.
     * 
     * @return a JavaBean representing the current state
     */
    @Override
    public CellServerState getServerState(CellServerState cellServerState) {

        /* Create a new BasicCellState and populate its members */
        if (cellServerState == null) {
            cellServerState = new AppConventionalCellServerState();
        }
	((AppConventionalCellServerState)cellServerState).setMasterHost(this.masterHost);
	((AppConventionalCellServerState)cellServerState).setAppName(this.appName);
	((AppConventionalCellServerState)cellServerState).setCommand(this.command);
	((AppConventionalCellServerState)cellServerState).setPixelScale(this.pixelScale);
        
        return super.getServerState(cellServerState);
    }
}