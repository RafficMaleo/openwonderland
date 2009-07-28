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
package org.jdesktop.wonderland.client.app.xremwin;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.LinkedList;

import org.jdesktop.wonderland.client.app.base.App;
import org.jdesktop.wonderland.client.app.base.ControlArbSingle;
import org.jdesktop.wonderland.client.app.base.Window2D;

/**
 * The Xremwin ControlArb class. This currently doesn't implement
 * polite control arbitration--control is simply stolen.
 *
 * @author deronj
 */

@ExperimentalAPI
class ControlArbXrw extends ControlArbSingle {

    /** The default take control politeness mode */
    private static boolean TAKE_CONTROL_IMPOLITE = true;

    /** The server the client talks to */
    protected ServerProxy serverProxy;

    /** A take control request is pending */
    protected boolean takeControlPending;
    
    /** The politeness of the pending take control */
    protected boolean takeControlPendingImpolite;

    /** The previous event mode before a takeControl() method call */
    protected EventMode eventModePrev;

    /** 
     * It is okay to send events when this is enabled. This not 
     * necessarily the same as hasControl. For example, we allow
     * events to be sent to the server while a take control is
     * pending (this is called "event-ahead"). If control is 
     * subsequently refused the event-ahead events will be ignored.
     */
    protected boolean eventsEnabled;

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup () {
	super.cleanup();

	if (hasControl()) {
	    releaseControl(null);
	}

	eventsEnabled = false;
	takeControlPending = false;

	if (serverProxy != null) {
	    serverProxy.cleanup();
	    serverProxy = null;
	}
    }

    /**
     * Attach a server proxy to this control arb. The control arb forwards events to the server proxy.
     * @param serverProxy The server proxy to which to attach this ControlArbXrw.
     */
    public void setServerProxy (ServerProxy serverProxy) {
	this.serverProxy = serverProxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getController () { 
	return controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void takeControl () { 
	prevEventMode = InputManager.getEventMode();

	if (!hasControl()) {
	    super.takeControl();
	}

	take(TAKE_CONTROL_IMPOLITE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void releaseControl () { 
	if (!hasControl()) return;	
	release();
	super.releaseControl();
    }

    /**
     * Take control with the specified politeness. Tell the server that this user 
     * wants to take control of the app. If this succeeds we will receive a message 
     * from the server indicating success. At that time the setController method of this 
     * controlArb will be called with the user name of this client. 
     */
    private void take (boolean impolite) {
        
	// Enable our client to send events to the server ("event ahead"). 
	// If control is refused the events will just be ignored.
	eventsEnabled = true;

	takeControlPending = true;
	takeControlPendingImpolite = impolite;

	try {
            serverProxy.writeTakeControl(impolite);
        } catch (IOException ex) {
            eventsEnabled = false;
            takeControlPending = false;
        }
    }

    /**
     * Tell the server to release control.
     */
    private void release () {
        eventsEnabled = false;
	takeControlPending = false;

        try {
            serverProxy.writeReleaseControl();
        } catch (IOException ex) {
        }

	setController(null);
    }

    /** 
     * The server has refused our request for control. If our first
     * attempt was polite get confirmation from the user to continue.
     */
    void controlRefused () {
	String currentController = serverProxy.getControllingUser();

	if (!takeControlPending) {
	    // We weren't expecting this. We shouldn't have control. 
	    // Make sure we don't have it
	    controlError("refused", currentController);
	    releaseControl(null);
	    return;
	}

	if (takeControlPending && !takeControlPendingImpolite) {
	    if (takeControlConfirm(currentController)) {
		// User confirmed. Try again, this time steal control.
		take(null, true);
		return;
	    }
	}

	takeControlPending = false;
	eventsEnabled = false;
	hasControl = false;

	setController(currentController);

	// Revert to the previous event mode (before the take control attempt)
	InputManager.setEventMode(eventModePrev);
    }

    /** 
     * The server has told us that our request for control has succeeded. 
     */
    void controlGained () {
	String currentController = serverProxy.getControllingUser();

	if (!takeControlPending) {
	    // We weren't expecting this. We shouldn't have control. Give it up.
	    controlError("refused", currentController);
	    releaseControl(null);
	    return;
	}

	takeControlPending = false;
	hasControl = true;

	setController(currentController);
    }

    /**
     * The server has taken control away from us.
     */
    void controlLost () {
	takeControlPending = false;
	eventsEnabled = false;
	hasControl = false;
	setController(serverProxy.getControllingUser());
    }

    // TODO: not yet implemented
    private boolean takeControlConfirm (String controller) {
	// TODO: bring up dialog message:
	// "User <controller>) already has control. Take control anyway?
	// With buttons yes/no
	// Return true if yes, false if no
	return false;
    }

    /**
     * Report an error in control handling. This indicates an 
     * internal error in app.base.
     */
    private static void controlError (String errTypeStr, String currentController) {
	// TODO: is there a better way to report this?
	AppXrw.logger.warning("TakeControl: control was " + errTypeStr + " when we didn't ask for it.");
	AppXrw.logger.warning("TakeControl: current controller = " + currentController);
    }

    /**
     * {@inheritDoc}
     */
    protected synchronized void setController (String controller) {
	super.setController(controller);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void deliverEvent (Window2D window, KeyEvent event) {
	if (!eventsEnabled) return;

	// The server doesn't care about typed events
	if (event.getID() == KeyEvent.KEY_TYPED) {
	    return;
	}

	try {
	    AppXrw.logger.finer("Write key event to server");
	    // Note: server doesn't care about the window which generated key events
	    serverProxy.writeEvent(event);
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void deliverEvent (Window2D window, MouseEvent event) {
	if (!eventsEnabled) return;

	// The Xremwin server doesn't care about clicked events. Ignore them.
	if (event.getID() == MouseEvent.MOUSE_CLICKED) {
	    return;
	}

	try {
	    if (event instanceof MouseWheelEvent) {
	    AppXrw.logger.finer("Write mouse wheel event to server");
	    serverProxy.writeWheelEvent(((WindowXrw)window).getWid(), (MouseWheelEvent)event);
	    } else {
		AppXrw.logger.finer("Write mouse event to server");
		serverProxy.writeEvent(((WindowXrw)window).getWid(), event);	
	    }
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }
}