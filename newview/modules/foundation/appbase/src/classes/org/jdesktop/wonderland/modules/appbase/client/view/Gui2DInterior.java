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
package org.jdesktop.wonderland.modules.appbase.client.view;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import javax.media.opengl.GLContext;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;
import org.jdesktop.wonderland.modules.appbase.client.App2D;

/**
 * The event handler code which handles the 2D window interior. It supports sending 
 * all events to the app group when the user has control of the window
 * and also all global events.
 *
 * @author deronj
 */
@InternalAPI
public class Gui2DInterior extends Gui2D {

    private static final Logger logger = Logger.getLogger(Gui2DInterior.class.getName());

    // We need to call this method reflectively because it isn't available in Java 5
    // BTW: we don't support Java 5 on Linux, so this is okay.
    private static boolean isLinux = System.getProperty("os.name").equals("Linux");
    private static Method isAWTLockHeldByCurrentThreadMethod;

    static {
        if (isLinux) {
            try {
                Class awtToolkitClass = Class.forName("sun.awt.SunToolkit");
                isAWTLockHeldByCurrentThreadMethod =
                        awtToolkitClass.getMethod("isAWTLockHeldByCurrentThread");
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            }
        }
    }
    /** A listener for keys pressed and released */
    protected InteriorKeyListener keyListener;

    /** This Gui's app. */
    private App2D app;

    /** The focus entity of this Gui's app. */
    private Entity appFocusEntity;

    /**
     * Create a new instance of Gui2DInterior.
     *
     * @param view The view associated with the component that uses this Gui.
     */
    public Gui2DInterior(View2DEntity view) {
        super(view);
        app = view.getWindow().getApp();
        appFocusEntity = app.getFocusEntity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachMouseListener(Entity entity) {
        mouseListener = new InteriorMouseListener();
        mouseListener.addToEntity(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void detachMouseListener(Entity entity) {
        if (mouseListener != null && entity != null) {
            mouseListener.removeFromEntity(entity);
        }
    }

    /**
     * This Gui's listener for mouse events.
     * <br>
     * If the user has control of the app, this method sends the event to the corresponding view for this gui.
     * <br>
     * If user doesn't have control of the app, the event is checked to see if it is a recognized action.
     * If an action is recognized the action is performed.
     */
    protected class InteriorMouseListener extends Gui2D.MouseListener {

        @Override
        public boolean consumesEvent(Event event) {
            if (!super.consumesEvent(event)) {
                // Not meant for us
                return false;
            }
            logger.fine("Event is meant for this listener, event = " + event);

            // Always consume the control change event over the interior even when the app 
            // doesn't have control and the app entity doesn't have focus
            MouseEvent3D me3d = (MouseEvent3D) event;
            if (isChangeControlEvent((MouseEvent) me3d.getAwtEvent())) {
                return true;
            }

            if (!app.getControlArb().hasControl()) {
                return false;
            }
            logger.fine("User has control of app");

            // When the app has control only consume if app has focus.
            boolean entityHasFocus = InputManager3D.entityHasFocus(event, appFocusEntity);
            logger.fine("Entity has focus = " + entityHasFocus);
            return entityHasFocus;
        }

        @Override
        public void commitEvent(Event event) {
            logger.fine("Interior mouse commitEvent, event = " + event);
            MouseEvent3D me3d = (MouseEvent3D) event;

            // Linux-specific workaround: On Linux JOGL holds the SunToolkit AWT lock in mtgame commit methods.
            // In order to avoid deadlock with any threads which are already holding the AWT lock and which
            // want to acquire the lock on the dirty rectangle so they can draw (e.g Embedded Swing threads)
            // we need to temporarily release the AWT lock before we lock the dirty rectangle and then reacquire
            // the AWT lock afterward.
            GLContext glContext = null;
            if (isAWTLockHeldByCurrentThreadMethod != null) {
                try {
                    Boolean ret = (Boolean) isAWTLockHeldByCurrentThreadMethod.invoke(null);
                    if (ret.booleanValue()) {
                        glContext = GLContext.getCurrent();
                        glContext.release();
                    }
                } catch (Exception ex) {
                }
            }

            // When user has control all events over the interior are sent to the app.
            // First send it to the app's view for conversion to a 2D event.
            try {

                if (view.getWindow().getApp().getControlArb().hasControl()) {
                    view.deliverEvent((Window2D) view.getWindow(), me3d);
                    return;
                }

                MouseEvent me = (MouseEvent) me3d.getAwtEvent();

                // Handle miscellaneous events over interior when user doesn't have control
                Action action = determineIfMiscAction(me, me3d);
                if (action != null) {
                    performMiscAction(action, me, me3d);
                    return;
                }

                super.commitEvent(event);

            } finally {
                // Linux-specific workaround: Reacquire the lock if necessary.
                if (glContext != null) {
                    glContext.makeCurrent();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachKeyListener(Entity entity) {
        keyListener = new InteriorKeyListener();
        keyListener.addToEntity(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void detachKeyListener(Entity entity) {
        if (keyListener != null && entity != null) {
            keyListener.removeFromEntity(entity);
        }
    }

    /**
     * This Gui's listener for key events.
     * <br>
     * If the key is Shift-F12, user control is released for all apps in the client session.
     * <br>
     * Otherwise, if the user has control of the app, this method sends the event directly to the
     * corresponding window for this gui.
     * <br>
     * If user doesn't have control of the app then the event is discarded.
     */
    protected class InteriorKeyListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{KeyEvent3D.class};
        }

        @Override
        public boolean consumesEvent(Event event) {
            if (!super.consumesEvent(event)) {
                // Not meant for us
                return false;
            }


            if (!app.getControlArb().hasControl()) {
                return false;
            }

            // When the app has control only consume if app has focus.
            return InputManager3D.entityHasFocus(event, appFocusEntity);
        }

        @Override
        public void commitEvent(Event event) {
            logger.fine("Interior key commitEvent, event = " + event);
            KeyEvent3D ke3d = (KeyEvent3D) event;
            KeyEvent ke = (KeyEvent) ke3d.getAwtEvent();

            if (ke3d.isPressed() &&
                    ke.getKeyCode() == KeyEvent.VK_F12 &&
                    (ke.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                ControlArb.releaseControlAll();
                return;
            }

            // Note: currently no special GUI processing is needed for key events
            // so they are all just sent to the app group if it has control
            ControlArb controlArb = view.getWindow().getApp().getControlArb();
            if (controlArb.hasControl()) {
                controlArb.deliverEvent(view.getWindow(), ke);
            }
        }
    }
}
