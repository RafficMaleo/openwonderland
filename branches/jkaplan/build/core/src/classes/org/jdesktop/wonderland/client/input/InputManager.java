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
package org.jdesktop.wonderland.client.input;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * A singleton container for all of the processor objects in the Wonderland input subsystem.
 *
 * The <code>InputManager</code> provides a global Event Mode which which can take on one of two values: 
 * <code>WORLD</code> and <code>APP</code>. 
 * When the event mode is <code>WORLD</code> it is okay for event listeners which modify the world to handle events and
 * app-related event listeners should ignore incoming events. When the event mode is <code>APP</code>, 
 * it is okay for app-related
 * event listeners to handle events and world-related event listeners must ignore incoming events.
 *
 * The <code>InputManager</code> supports event listener (<code>EventListener</code>) objects. 
 * These listeners can be added to entities
 * in the world in order to allow these entities to respond to events. These events can be generated as a result
 * of user input or can be programmatically generated by other parts of the client.
 *
 * The <code>InputManager</code> also supports a set of global event listeners. These are independent of 
 * any entities. The system always delivers all events to global event listeners which are willing 
 * to consume these events. Note: the return values of <code>propagateToParent()</code> and 
 * <code>propagateToUnder()</code> for global listeners
 * are ignored. Note: The <code>pickDetails</code> field of an event is null for events received by the 
 * global listeners.
 *
 * @author deronj
 */

// TODO: generate 3D enter/exit events for canvas enter exit

@ExperimentalAPI
public abstract class InputManager 
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
    /* TODO: the non-embedded swing case is for prototyping only. Eventually this should be true */
    private static boolean ENABLE_EMBEDDED_SWING = false;

    /** The singleton input manager */
    protected static InputManager inputManager;

    /** The singleton input picker. (Used only in non-embedded swing case). */
    protected InputPicker inputPicker;

    /** The singleton event distributor. */
    protected EventDistributor eventDistributor;

    /** The canvas from which this input manager should receive events. */
    protected Canvas canvas;

    /** The global event mode type. */
    public static enum EventMode { WORLD, APP };

    /**
     * Return the input manager singleton.
     */
    static InputManager inputManager () {
        return inputManager;
    }

    /**
     * Returns the current event mode. By default, event mode is WORLD.
     * @return The current event mode.
     */
    public EventMode getEventMode () {
	return eventDistributor.getEventMode();
    }

    /**
     * Returns true if the event mode is currently WORLD.
     */
    public boolean isWorldMode () {
	return getEventMode() == EventMode.WORLD;
    }

    /**
     * Returns true if the event mode is currently APP.
     */
    public boolean isAppMode () {
	return getEventMode() == EventMode.APP;
    }

    /**
     * Sets the event mode.
     * @param eventMode The new event mode.
     */
    public void setEventMode (EventMode eventMode) {
	eventDistributor.setEventMode(eventMode);
    }

    /** 
     * Initialize the input manager to receive input events from the given AWT canvas
     * and start the input manager running. This method does not define a camera
     * component, so picking on events will not start occuring until a camera component
     * is specified with a subsequent call to <code>setCameraComponent</code>.
     *
     * @param canvas The AWT canvas which generates AWT user events.
     */
    public void initialize (Canvas canvas) {
	initialize(canvas, null);
    }

    /** 
     * Initialize the input manager to receive input events from the given AWT canvas
     * and start the input manager running. The input manager will perform picks with the
     * given camera. This routine can only be called once. To subsequently change the 
     * camera, use <code>setCameraComponent</code>.
     *
     * @param canvas The AWT canvas which generates AWT user events.
     * @param cameraComp The mtgame camera component to use for picking operations.
     */
    public void initialize (Canvas canvas, CameraComponent cameraComp) {
	if (canvas != null) {
	    throw new IllegalStateException("initialize has already been called for this InputManager");
	}
	this.canvas = canvas;
	inputPicker.setCanvas(canvas);

	setCameraComponent(cameraComp);

	canvas.addKeyListener(this);

	if (!ENABLE_EMBEDDED_SWING) {
	    // When not using Embedded Swing the input manager receives events directly from the AWT canvas.
	    canvas.addMouseListener(this);
	    canvas.addMouseMotionListener(this);
	    canvas.addMouseWheelListener(this);
	}

	injectInitialMouseEvent();
    }

    /**
     * This sends an initial, synthetic mouse event through the input system. The event position is the center
     * of the canvas. This is done for two reasons:
     * <br><br>
     * 1. To initialize the keyboard focus.
     * <br><br>
     * 2. To send out the initial enter event.
     * <br><br>
     * TODO: this is currently weak: it is only effective if called *AFTER* the scene graph is initialized.
     * This will be rectified when we implement synthetic event injection (repick) on scene graph change.
     */
    private void injectInitialMouseEvent () {
	MouseEvent me = new MouseEvent(canvas, MouseEvent.MOUSE_MOVED, 0, 0, 
                                       canvas.getWidth()/2, canvas.getHeight()/2, 0, false, MouseEvent.NOBUTTON);
	mouseMoved(me);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseClicked(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }
    
    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseEntered(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseExited(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mousePressed(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseReleased(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseDragged(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseMoved(MouseEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }
    
    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     * <br><br>
     * Only used in the non-embedded swing case.
     */
    @InternalAPI
    public void mouseWheelMoved(MouseWheelEvent e) {
	inputPicker.pickMouseEventNonSwing(e);
    }
    
    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     */
    @InternalAPI
    public void keyPressed(KeyEvent e) {
	inputPicker.pickKeyEvent(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     */
    @InternalAPI
    public void keyReleased(KeyEvent e) {
	inputPicker.pickKeyEvent(e);
    }

    /**
     * INTERNAL ONLY
     * <br><br>
     * <@inheritDoc>
     */
    @InternalAPI
    public void keyTyped(KeyEvent e) {
	inputPicker.pickKeyEvent(e);
    }

    /**
     * Add an event listener to be tried once per event. This global listener can be added only once.
     * Subsequent attempts to add it will be ignored.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The global event listener to be added.
     */
    public void addGlobalEventListener (EventListener listener) {
	eventDistributor.addGlobalEventListener(listener);
    }

    /**
     * Remove this global event listener.
     *
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The entity to which to attach this event listener.
     */
    public void removeGlobalEventListener (EventListener listener) {
	eventDistributor.removeGlobalEventListener(listener);
    }

    /** 
     * Specify the camera component to be used for picking.
     *
     * @param cameraComp The mtgame camera component to use for picking operations.
     */
    public void setCameraComponent (CameraComponent cameraComp) {
	inputPicker.setCameraComponent(cameraComp);
    }

    /** 
     * Returns the camera component that is used for picking.
     */
    public CameraComponent getCameraComponent () {
	return inputPicker.getCameraComponent();
    }
}
