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
package org.jdesktop.wonderland.modules.appbase.client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.util.geom.BufferUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;

/**
 * The generic 2D window superclass. All 2D windows in Wonderland have this root class. Instances of this 
 * class are created by the createWindow methods of App2D subclasses.
 *
 * Windows can be arranged into a stack with other windows. Each window occupies a unique position in the 
 * stack. The lowest window is at position 0, the window immediately above that is at position 1, and so on.
 *
// TODO: move this to the class description
// User configuration of a primary window configures the cell. A cell has only one primary window.
// TODO: move this to the class description
// A secondary window is a top-level window which is potentially decorated but which 
// is positioned relative to the primary. A secondary window may be promoted to a 
// primary window goes away.

A Window2D can have zero or more views.

fundamental: currently, a window can only have one view for a particular displayer    
otherwise view.setParent gets really tricky

 * @author deronj
 */
@ExperimentalAPI
public abstract class Window2D {

    private static final Logger logger = Logger.getLogger(Window2D.class.getName());
    private static final int CHANGED_ALL         = -1;
    private static final int CHANGED_TYPE        = 0x01;
    private static final int CHANGED_PARENT      = 0x02;
    protected static final int CHANGED_VISIBLE_APP = 0x04;
    private static final int CHANGED_DECORATED   = 0x08;
    private static final int CHANGED_OFFSET      = 0x10;
    private static final int CHANGED_SIZE        = 0x20;
    private static final int CHANGED_TITLE       = 0x40;
    private static final int CHANGED_STACK     = 0x80;

    /** The type of the 2D window. */
    public enum Type {
        UNKNOWN, PRIMARY, SECONDARY, POPUP
    };
    /** 
     * The offset in pixels from top left of parent. 
     * Ignored by primary (initially primary is always centered in cell).
     */
    private Point offset = new Point(0, 0);
    /** The size of the window specified by the application. */
    private Dimension size = new Dimension(1, 1);
    /** The initial size of the pixels of the window's views (specified by WFS). */
    protected Vector2f pixelScale;
    /** The string to display as the window title */
    protected String title;
    /** The desired Z order of all views of the window. */
    private int desiredZOrder;
    /** The texture which contains the contents of the window */
    protected Texture2D texture;
    /** Listeners for key events */
    protected ArrayList<KeyListener> keyListeners = null;
    /** Listeners for mouse events */
    protected ArrayList<MouseListener> mouseListeners = null;
    /** Listeners for mouse motion events */
    protected ArrayList<MouseMotionListener> mouseMotionListeners = null;
    /** Listeners for mouse wheel events */
    protected ArrayList<MouseWheelListener> mouseWheelListeners = null;
    /** The views associated with this window. */
    private LinkedList<View2D> views = new LinkedList<View2D>();
    /** The app to which this window belongs */
    protected App2D app;
    /** The name of the window. */
    private String name;
    /** 
     * Provides, for each displayer, a list of views associated with the window that belong to the displayer.
     * Note: currently a displayer can have only one view of a window.
     */
    private HashMap<View2DDisplayer, View2D> displayerToView = new HashMap<View2DDisplayer, View2D>();
    /** The type of the window. */
    private Type type = Type.UNKNOWN;
    /** The parent of the window. (Ignored for primaries). */
    private Window2D parent;
    /** Whether the app wants the window to be visible. */
    protected boolean visibleApp;
    /** Whether the window is decorated with a frame. (Ignored for popups). */
    private boolean decorated;
    /** The set of changes to apply to views. */
    protected int changeMask;
    /** A list of event listeners to attach to this window's views. */
    private LinkedList<EventListener> eventListeners = new LinkedList<EventListener>();
    /** If true, the window is in the same local Z plane as parent. Only used for frame header windows. */
    private boolean coplanar;
    /** The surface the client on which subclasses should draw. */
    protected DrawingSurface surface;

    /** A type for entries in the entityComponents list. */
    private static class EntityComponentEntry {

        private Class clazz;
        private EntityComponent comp;

        private EntityComponentEntry(Class clazz, EntityComponent comp) {
            this.clazz = clazz;
            this.comp = comp;

        }
    }
    /** 
     * The entity components which should be attached to the views of this window.
     */
    private LinkedList<EntityComponentEntry> entityComponents = new LinkedList<EntityComponentEntry>();

    /**
     * A close listener is a listener which is called when the window is closed (e.g. via the user 
     * clicking on frame close button).
     */
    public interface CloseListener {
        public void windowClosed (Window2D window);
    }

    /** The attached close listeners. */
    private LinkedList<CloseListener> closeListeners = new LinkedList<CloseListener>();

    /**
     * Create an instance of Window2D with a default name. The first such window created for an app 
     * becomes the primary window. Subsequent windows are secondary windows.
     * @param app The application to which this window belongs.
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param surface The drawing surface on which the creator will draw
     */
    protected Window2D(App2D app, int width, int height, boolean decorated, Vector2f pixelScale,
                       DrawingSurface surface) {
        this(app, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of Window2D with the given name. The first such window created for an app 
     * becomes the primary window. Subsequent windows are secondary windows.
     * @param app The application to which this window belongs.
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     * @param surface The drawing surface on which the creator will draw
     */
    public Window2D(App2D app, int width, int height, boolean decorated, Vector2f pixelScale, String name,
                    DrawingSurface surface) {
        this.app = app;
        this.size = new Dimension(width, height);
        this.decorated = decorated;
        this.pixelScale = new Vector2f(pixelScale);
        this.name = name;

        this.surface = surface;
        surface.setWindow(this);

        // Must occur before adding window to the app
        updateTexture();

        app.addWindow(this);

        changeMask = CHANGED_ALL;
        updateViews();
    }

    /**
     * Create an instance of Window2D of the given type with a default name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary window.
     * window of the app (if there is one).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param surface The drawing surface on which the creator will draw
     * Throws a RuntimeException if the rules of setType are not followed.
     */
    protected Window2D(App2D app, Type type, int width, int height, boolean decorated, Vector2f pixelScale,
                       DrawingSurface surface) {
        this(app, type, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of Window2D of the given type with the given name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary window.
     * window of the app (if there is one).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     * @param surface The drawing surface on which the creator will draw
     * Throws a RuntimeException if the rules of setType are not followed.
     */
    public Window2D(App2D app, Type type, int width, int height, boolean decorated, Vector2f pixelScale,
            String name, DrawingSurface surface) {
        this(app, type, app.getPrimaryWindow(), width, height, decorated, pixelScale, name, surface);
    }

    /**
     * Create an instance of Window2D of the given type with the given parent with a default name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. 
     * @param parent The parent of the window. (Ignored for primary windows).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param surface The drawing surface on which the creator will draw
     * Throws a RuntimeException if the rules of setType are not followed.
     */
    protected Window2D(App2D app, Type type, Window2D parent, int width, int height, boolean decorated,
                       Vector2f pixelScale, DrawingSurface surface) {
        this(app, type, parent, width, height, decorated, pixelScale, null, surface);
    }

    /**
     * Create an instance of Window2D of the given type with the given parent with the given name. 
     * @param app The application to which this window belongs.
     * @param type The type of the window. If this is non-primary, the parent is set to the primary
     * window of the app (if there is one).
     * @param parent The parent of the window. (Ignored for primary windows).
     * @param width The window width (in pixels).
     * @param height The window height (in pixels).
     * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param name The name of the window.
     * @param surface The drawing surface on which the creator will draw
     * Throws a RuntimeException if the rules of setType are not followed.
     */
    public Window2D(App2D app, Type type, Window2D parent, int width, int height, boolean decorated,
            Vector2f pixelScale, String name, DrawingSurface surface) {
        this.app = app;
        this.size = new Dimension(width, height);
        this.decorated = decorated;
        this.pixelScale = new Vector2f(pixelScale);
        this.name = name;

        try {
            setType(type);
        } catch (IllegalStateException ise) {
            RuntimeException re = new RuntimeException("Cannot set type of window");
            re.initCause(ise);
            throw re;
        }

        this.parent = parent;

        this.surface = surface;
        surface.setWindow(this);

        // Must occur before adding window to the app
        updateTexture();

        app.addWindow(this);

        changeMask = CHANGED_ALL;
        updateViews();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void cleanup() {
        setParent(null);
        texture = null;
        if (surface != null) {
            surface.setUpdateEnable(false);
            surface.cleanup();
            surface = null;
        }
        if (app != null) {
            App2D theApp = app;
            app = null;
            theApp.removeWindow(this);
        }
        visibleApp = false;
    }

    /**
     * Returns the app to which this this window belongs.
     */
    public App2D getApp() {
        return app;
    }

    /** Returns the name of the window. */
    public String getName() {
        if (name == null) {
            return "Window2D for app " + app.getName();
        } else {
            return name;
        }
    }

    /**
     * Returns the drawing surface of this window. 
     */
    public DrawingSurface getSurface() {
        return surface;
    }

    /**
     * Change the type of the window. Only certain combinations are permitted.
     * Here are the rules.
     * <br><br>
     * 1. You may not change a window to the type <code>UNKNOWN</code>.
     * <br><br>
     * 2. A window of type <code>UNKNOWN</code> or <code>SECONDARY</code> may be changed to any type
     * (except, of course, <code>UNKNOWN</code>).
     * <br><br>
     * 3. You cannot change the type of a <code>PRIMARY</code> window. Once a window has been changed
     * to primary you must destroy the window before you make another window primary. You cannot change
     * a window to primary while the app already has another primary window.
     * <br><br>
     * 4. You cannot change the type of a <code>POPUP</code> window. Once a window has been changed
     * to a popup it stays that way until the window is destroyed.
     * <br><br>
     * Special Note: when you make a window primary all existing secondary windows are parented to it.
     *
     * @param type The new type of the window.
     * @throws IllegalStateException if the rules above are not followed.
     */
    public synchronized void setType (Type type) throws IllegalStateException {
        if (type == Type.UNKNOWN) {
            throw new RuntimeException("Cannot set window type to unknown.");
        }

        if (this.type == type) return;

        switch (this.type) {
        case UNKNOWN:
        case SECONDARY:
            break;
        case PRIMARY:
            throw new IllegalStateException("Cannot change the type of a primary window.");
        case POPUP:
            throw new IllegalStateException("Cannot change the type of a popup window.");
        }

        if (type == Type.PRIMARY) {
            // Is there already a primary window? 
            if (app.getPrimaryWindow() != null) {
                throw new IllegalStateException("This app already has a primary window.");
            }
        }

        logger.info("Set type of window " + this + " to " + type);
        this.type = type;
        changeMask |= CHANGED_TYPE;
        updateViews();

        if (type == Type.PRIMARY) {
            // Tell the app about the new primary. This also parent existing secondaries to this new primary.
            app.setPrimaryWindow(this);
        }
    }

    /**
     * Returns the window type.
     */
    public Type getType() {
        return type;
    }

    /** 
     * Set the parent of the window. (This is ignored for primary windows). 
     */
    public synchronized void setParent(Window2D parent) {
        if (type == Type.PRIMARY || parent == this.parent) {
            return;
        }
        logger.info("Set parent of window " + this + " to " + parent);
        this.parent = parent;
        changeMask |= CHANGED_PARENT;
        updateViews();
    }

    /**
     * Returns the window parent.
     */
    public Window2D getParent() {
        return parent;
    }

    /**
     * Set both the parent and the type of the window.
     * @param type The new type of the window. Must obey the rules listed in the documentation 
     * for method <code>setType</code>.
     * @param type parent new parent of the window. null if there is no parent.
     * @throws IllegalStateException if the rules are not followed.
     */
    public synchronized void setTypeParent (Type type, Window2D parent) throws IllegalStateException {
        // TODO: for now, do two updates
        setType(type);
        setParent(parent);
    }

    /* TODO: Eventually implement these getters
       isParentOf
       getSecondaryChildren
       getPopupChildren
       getAllChildren
    */

    /**
     * Sets the offset (in pixels) of this window relative to its parent. If the window has no parent
     * the offset is ignored. The offset is the distance from the upper left corner of the parent to the
     * upper left corner of this window. Any decoration is ignored in both cases.
     */
    public synchronized void setOffset(int x, int y) {
        if (offset.x == x && offset.y == y) {
            return;
        }
        this.offset = new Point(x, y);
        changeMask |= CHANGED_OFFSET;
        updateViews();
    }

    /**
     * Returns the X offset of the window with respect to its parent.
     */
    public int getOffsetX() {
        return offset.x;
    }

    /**
     * Returns the Y offset of the window with respect to its parent.
     */
    public int getOffsetY() {
        return offset.y;
    }

    /**
     * Specify the size of the window (excluding the decoration).
     * Note: the arguments do NOT include the borderWidth.
     *
     * TODO: Currently, the entire window contents will be lost when the window is resized, 
     * so you must repaint the entire window after the resize.
     *
     * @param width The new width of the window.
     * @param height The new height of the window.
     */
    // TODO: winconfig: delete
    public synchronized void setSize(int width, int height) {
        if (this.size.width == width && this.size.height == height) {
            return;
        }
        this.size = new Dimension(width, height);
        if (surface != null) {
            surface.setTexture(texture);
            surface.setSize(width, height);
        }
        changeMask |= CHANGED_SIZE;
        updateViews();
    }

    /**
     * The width of the window (excluding the decoration).
     */
    public int getWidth() {
        return size.width;
    }

    /** 
     * The height of the window (excluding the decoration).
     */
    public int getHeight() {
        return size.height;
    }

    /**
     * Change both the window size and window stacking order in the same call. This
     * is done for this window on the local client and all other clients.
     * <br><br>
     * This is like performing the following:
     * <br><br>
     * setSize(width, height);
     * <br>
     * restackAbove(sibling);
     * <br><br>
     * The visual representations of the window are updated accordingly.
     * 
     * @param width The new width of the window.
     * @param height The new height of the window.
     * @param sibling The window which will be directly below this window after this call.
     */
    /* TODO: winconfig: notyet
    public synchronized void configure(int width, int height, Window2D sibling) {
        configureLocal(width, height, sibling);

        // TODO: winconfig: swing: resize
        //if (app.isSwsShared(SwsClient.Attr.WINDOW_SIZE)) {
        //  SwsClient swsClient = getApp().getSwsClient();
        //  swsClient.setSize(swsWindowID, width, height);
        //}

        // TODO: winconfig: swing: stack
        //if (app.isSwsShared(SwsClient.Op.WINDOW_RESTACK)) {
        //    SwsClient swsClient = getApp().getSwsClient();
        //    swsClient.restackAbove(swsWindowID, sibling.swsWindowID);
        //}
    }
    */
    // TODO: winconfig: delete
    public synchronized void configure(int width, int height, Window2D sibWin) {
        this.size = new Dimension(width, height);
        changeMask |= CHANGED_SIZE;

        // TODO: stack

        updateViews();
    }

    /**
     * Change both the window size and window stacking order in the same call. This
     * is done for this window on the local client only.
     * <br><br>
     * This is like performing the following:
     * <br><br>
     * setSizeLocal(width, height);
     * <br>
     * restackAboveLocal(sibling);
     * <br><br>
     * The visual representations of the window are updated accordingly.
     * 
     * @param width The new width of the window.
     * @param height The new height of the window.
     * @param sibling The window which will be directly below this window after this call.
     */
    /* TODO: winconfig: notyet
    public synchronized void configureLocal (int width, int height, Window2D sibling) {
        this.size = new Dimension(width, height);
        changeMask |= CHANGED_SIZE;
 
        if (sibling != null) {
            // This will call updateViews if necessary
            app.getWindowStack().restackAbove(this, sibling);
            changeMask |= CHANGED_STACK;
            updateViews();
        }
    }
    */

    /**
     * Specify the initial pixel scale for the window's views when they are in cell mode.
     */
    public synchronized void setPixelScale(Vector2f pixelScale) {
        if (this.pixelScale.equals(pixelScale)) {
            return;
        }
        this.pixelScale = pixelScale.clone();
    }

    /** 
     * Returns the initial pixel scale of the window's views when they are in cell mode.
     */
    public Vector2f getPixelScale() {
        return pixelScale.clone();
    }

    /**
     * The app calls this to change the visibility of the window.
     * @param visible Whether the app wants the window to be visible.
     * Throws a RuntimeException if visible is true and the type is <code>UNKNOWN</code>.
     */
    public synchronized void setVisibleApp(boolean visible) {
        if (visibleApp == visible) {
            return;
        }

        setVisibleAppPart1(visible);
        setVisibleAppPart2();
    }

    protected void setVisibleAppPart1(boolean visible) {
        visibleApp = visible;
        changeMask |= CHANGED_VISIBLE_APP;
    }

    protected void setVisibleAppPart2() {
        if (visibleApp) {
            // Add newly visible windows to the stack if they aren't coplanar
            if (!coplanar) {
                app.getWindowStack().add(this);
                changeMask |= CHANGED_STACK;
                updateViews();
            }
        } else {
            // Remove newly invisible windows from the stack
            app.getWindowStack().remove(this);
            changeMask |= CHANGED_STACK;
            updateViews();
        }
    }

    /** 
     * Does the app want the window to be visible?
     */
    public boolean isVisibleApp() {
        return visibleApp;
    }

    /**
     * Specifies whether the user wants the window to be visible in the given displayer.
     */
    public synchronized void setVisibleUser(View2DDisplayer displayer, boolean visible) {
        View2D view = getView(displayer);
        if (view != null) {
            // Note: update immediately
            view.setVisibleUser(visible);
        }
    }

    /**
     * Does the user want the window to be visible in the given displayer?
     */
    public boolean isVisibleUser(View2DDisplayer displayer) {
        View2D view = getView(displayer);
        if (view != null) {
            return view.isVisibleUser();
        } else {
            return false;
        }
    }

    /**
     * Specify whether this window is decorated with a frame.
     */
    public synchronized void setDecorated(boolean decorated) {
        if (this.decorated == decorated) {
            return;
        }
        this.decorated = decorated;
        changeMask |= CHANGED_DECORATED;
        updateViews();
    }

    /**
     * Returns whether the window is decorated.
     */
    public boolean isDecorated() {
        return decorated;
    }

    /**
     * Specify the window's title.
     *
     * @param title The string to display as the window title.
     */
    public synchronized void setTitle(String title) {
        //System.err.println("@@@@@@@@@@@@ window = " + this);
        //System.err.println("@@@@@@@@@@@@ title = " + title);
        if (title == null && this.title == null) {
            return;
        }
        if (title.equals(this.title)) {
            return;
        }
        this.title = title;
        changeMask |= CHANGED_TITLE;
        updateViews();
    }

    /**
     * Returns the window title.
     */
    public String getTitle() {
        return title;
    }

     /**
      * Specify whether this window is in the same local Z plane as its parent.
      * Ignored by non-popups.
      */
    public synchronized void setCoplanar (boolean coplanar) {
        if (this.coplanar == coplanar) return;
        this.coplanar = coplanar;
        if (coplanar) {
            // Remove newly coplanar windows from the stack
            app.getWindowStack().remove(this);
            changeMask |= CHANGED_STACK;
            updateViews();
        } else {
            // If a window has just become non-coplanar and it is visible, add it to the stack
            if (isVisibleApp()) {
                app.getWindowStack().add(this);
                changeMask |= CHANGED_STACK;
                updateViews();
            }
        }
    }

    /** 
     * Returns whether this window is in the same local Z plane as its parent.
     */
    public synchronized boolean isCoplanar () {
        return coplanar;
    }

    /**
     * Moves this window to the top of the app's window stack in the local client and all other clients.
     */
    public synchronized void restackToTop () {
        restackToTopLocal();

        /* TODO: winconfig: swing: stack
        if (app.isSwsShared(Sws.Op.WINDOW_RESTACK)) {
            SwsClient swsClient = getApp().getSwsClient();
            swsClient.restackToTop(swsWindowID);
        }
        */
    }

    /**
     * Moves this window to the top of the app's window stack in the local client only.
     */
    public synchronized void restackToTopLocal () {
        app.getWindowStack().restackToTop(this);
        changeMask |= CHANGED_STACK;
        updateViews();
    }

    /**
     * Moves this window to the bottom of the app's window stack in the local client and all other clients.
     */
    public synchronized void restackToBottom () {
        restackToBottomLocal();

        /* TODO: winconfig: swing: stack
        if (app.isSwsShared(Sws.Op.WINDOW_RESTACK)) {
            SwsClient swsClient = getApp().getSwsClient();
            swsClient.restackToBottom(swsWindowID);
        }
        */
    }

    /**
     * Moves this window to the bottom of the app's window stack in the local client only.
     */
    public synchronized void restackToBottomLocal () {
        app.getWindowStack().restackToBottom(this);
        changeMask |= CHANGED_STACK;
        updateViews();
    }

    /**
     * Moves this window so that it is above the given sibling window in the app's window stack
     * in the local client and all other clients. If sibling is null, this window is moved to the 
     * top of the stack.
     * @param sibling After this call, the sibling window will be below this window in the stack.
     */
    /* TODO: winconfig: stack
    public synchronized void restackAbove (Window2D sibling) {
        restackAboveLocal(sibling);

        //TODO: winconfig: swing: stack
        //if (app.isSwsShared(Sws.Op.WINDOW_RESTACK)) {
        //    SwsClient swsClient = getApp().getSwsClient();
        //    swsClient.restackAbove(swsWindowID, sibling.swsWindowID);
        //}
    }
    */

    /**
     * Moves this window so that it is above the given sibling window in the app's window stack
     * in the local client only. If sibling is null, this window is moved to the top of the stack.
     * @param sibling After this call, the sibling window will be below this window in the stack.
     */
    public synchronized void restackAbove/*TODO:winconfig:restack:Local*/ (Window2D sibling) {
        app.getWindowStack().restackAbove(this, sibling);
        changeMask |= CHANGED_STACK;
        updateViews();
    }

    /**
     * Moves this window so that it is below the given sibling window in the app's window stack
     * in the local client and all other clients. If sibling is null, this window is moved to the 
     * bottom of the stack.
     * @param sibling After this call, the sibling window will be above this window in the stack.
     */
    public synchronized void restackBelow (Window2D sibling) {
        restackBelow(sibling);
    }

    /**
     * Moves this window so that it is below the given sibling window in the app's window stack
     * in the local client only. If sibling is null, this window is moved to the bottom of the stack.
     * @param sibling After this call, the sibling window will be above this window in the stack.
     */
    public synchronized void restackBelowLocal (Window2D sibling) {
        app.getWindowStack().restackBelow(this, sibling);
        changeMask |= CHANGED_STACK;
        updateViews();
    }

    /**
     * Called by the App when the stacking order of this window has possibly changed.
     */
    public void changedStack () {
        changeMask |= CHANGED_STACK;
        updateViews();
    }

    /**
     * Returns the window above this window in the app's window stack. If this window is
     * on the top, null is returned.
     */
    public Window2D getWindowAbove () {
        return app.getWindowStack().getAbove(this);
    }

    /**
     * Returns the window below this window in the app's window stack. If this window is
     * on the bottom, null is returned.
     */
    public Window2D getWindowBelow () {
        return app.getWindowStack().getBelow(this);
    }

    /**
     * Specify the window's desired Z (stacking) order. This is used mainly for client/slave 
     * synchronization of the window stack.
     * @param zOrder The desired Z (stacking) order. Lower values are higher in the stack.
     */
    public synchronized void setDesiredZOrder(int desiredZOrder) {
        if (desiredZOrder == this.desiredZOrder) {
            return;
        }
        this.desiredZOrder = desiredZOrder;
    }

    /**
     * Returns the window's desired Z order. 
     */
    public int getDesiredZOrder() {
        return desiredZOrder;
    }

    /**
     * Returns the window's actual Z order in its app's window stack.
     * @returns The Z order, or -1 if the window is invisible or coplanar.
     */
    public int getZOrder() {
        if (!isVisibleApp()) return -1;
        if (coplanar) {
            if (parent == null) {
                return -1;
            } else {
                return parent.getZOrder();
            }
        } else {
            return app.getWindowStack().getZOrderOfWindow(this);
        }
    }

    /**
     * Returns the window's stack position in its app's window stack.
     * @returns The stack position, or -1 if the window is invisible or coplanar.
     */
    public int getStackPosition() {
        if (!isVisibleApp()) return -1;
        if (coplanar) {
            if (parent == null) {
                return -1;
            } else {
                return parent.getZOrder();
            }
        } else {
            return app.getWindowStack().getStackPositionOfWindow(this);
        }
    }

    /**
     * Return the texture containing the window contents.
     * Note: mostly used only for share-aware apps.
     */
    public Texture2D getTexture() {
        return texture;
    }

    /**
     * Deliver the given key event to this window.
     *
     * @param event The key event.
     */
    public void deliverEvent(KeyEvent event) {
        if (keyListeners == null) {
            return;
        }

        for (KeyListener listener : keyListeners) {
            switch (event.getID()) {
                case KeyEvent.KEY_PRESSED:
                    listener.keyPressed(event);
                    break;
                case KeyEvent.KEY_RELEASED:
                    listener.keyReleased(event);
                    break;
                case KeyEvent.KEY_TYPED:
                    listener.keyTyped(event);
                    break;
            }
        }
    }

    /**
     * Deliver the given mouse event to this window.
     * Note: mostly used only for share-aware apps.
     *
     * @param event The mouse event.
     */
    public void deliverEvent(MouseEvent event) {
        if (event instanceof MouseWheelEvent) {
            deliverEvent((MouseWheelEvent) event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED ||
                event.getID() == MouseEvent.MOUSE_MOVED) {
            deliverMouseMotionEvent(event);
        }

        if (mouseListeners == null) {
            return;
        }

        for (MouseListener listener : mouseListeners) {
            switch (event.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(event);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(event);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(event);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    listener.mouseEntered(event);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    listener.mouseExited(event);
                    break;
            }
        }
    }

    /**
     * Deliver the given mouse motion event to the window.
     *
     * @param event The mouse motion event to deliver.
     */
    private void deliverMouseMotionEvent(MouseEvent event) {
        if (mouseMotionListeners == null) {
            return;
        }

        for (MouseMotionListener listener : mouseMotionListeners) {
            switch (event.getID()) {
                case MouseEvent.MOUSE_MOVED:
                    listener.mouseMoved(event);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    listener.mouseDragged(event);
                    break;
            }
        }
    }

    /**
     * Deliver the given mouse wheel event to the window.
     *
     * @param event The mouse wheel event to deliver.
     */
    protected void deliverEvent(MouseWheelEvent event) {
        if (mouseWheelListeners == null) {
            return;
        }

        for (MouseWheelListener listener : mouseWheelListeners) {
            listener.mouseWheelMoved(event);
        }
    }

    /**
     * Add a new listener for key events.
     *
     * @param listener The key listener to add.
     */
    public synchronized void addKeyListener(KeyListener listener) {
        if (keyListeners == null) {
            keyListeners = new ArrayList<KeyListener>();
        }
        keyListeners.add(listener);
    }

    /**
     * Add a new listener for mouse events.
     *
     * @param listener The mouse listener to add.
     */
    public synchronized void addMouseListener(MouseListener listener) {
        if (mouseListeners == null) {
            mouseListeners = new ArrayList<MouseListener>();
        }
        mouseListeners.add(listener);
    }

    /**
     * Add a new listener for mouse motion events.
     *
     * @param listener The mouse motion listener to add.
     */
    public synchronized void addMouseMotionListener(MouseMotionListener listener) {
        if (mouseMotionListeners == null) {
            mouseMotionListeners = new ArrayList<MouseMotionListener>();
        }
        mouseMotionListeners.add(listener);
    }

    /**
     * Add a new listener for mouse wheel events.
     *
     * @param listener The mouse wheel listener to add.
     */
    public synchronized void addMouseWheelListener(MouseWheelListener listener) {
        if (mouseWheelListeners == null) {
            mouseWheelListeners = new ArrayList<MouseWheelListener>();
        }
        mouseWheelListeners.add(listener);
    }

    /**
     * Add a listener for key events.
     *
     * @param listener The key listener to add.
     */
    public synchronized void removeKeyListener(KeyListener listener) {
        if (keyListeners == null) {
            return;
        }
        keyListeners.remove(listener);
        if (keyListeners.size() == 0) {
            keyListeners = null;
        }
    }

    /**
     * Remove a listener for mouse events.
     *
     * @param listener The mouse listener to remove.
     */
    public synchronized void removeMouseListener(MouseListener listener) {
        if (mouseListeners == null) {
            return;
        }
        mouseListeners.remove(listener);
        if (mouseListeners.size() == 0) {
            mouseListeners = null;
        }
    }

    /**
     * Remove a listener for mouse motion events.
     *
     * @param listener The mouse motion listener to remove.
     */
    public synchronized void removeMouseMotionListener(MouseMotionListener listener) {
        if (mouseMotionListeners == null) {
            return;
        }
        mouseMotionListeners.remove(listener);
        if (mouseMotionListeners.size() == 0) {
            mouseMotionListeners = null;
        }
    }

    /**
     * Remove a listener for mouse wheel events.
     *
     * @param listener The mouse wheel listener to remove.
     */
    public synchronized void removeMouseWheelListener(MouseWheelListener listener) {
        if (mouseWheelListeners == null) {
            return;
        }
        mouseWheelListeners.remove(listener);
        if (mouseWheelListeners.size() == 0) {
            mouseWheelListeners = null;
        }
    }

    /**
     * Called by the GUI to close the window.
     */
    public void closeUser() {

        // User must have control in order to close the window
        if (!app.getControlArb().hasControl()) {
            // TODO: bring up swing option window: "You cannot close this window because you do not have control"
            logger.warning("You cannot close this window because you do not have control");
            return;
        }

        // Call close listeners
        synchronized (closeListeners) {
            for (CloseListener listener : closeListeners) {
                listener.windowClosed(this);
            }
        }

        cleanup();
    }

    /**
     * Add a close listener to this window. The listener will be called when the window is closed.
     */
    public void addCloseListener (CloseListener listener) {
        synchronized (closeListeners) {
            closeListeners.add(listener);
        }
    }

    /**
     * Remove a close listener from this window. 
     */
    public void removeCloseListener (CloseListener listener) {
        synchronized (closeListeners) {
            closeListeners.remove(listener);
        }
    }

    /**
     * Return an iterator over the close listeners for this window. 
     */
    public Iterator<CloseListener> getCloseListeners () {
        synchronized (closeListeners) {
            return closeListeners.iterator();
        }
    }

    /**
     * Add an event listener to all of this window's views.
     * @param listener The listener to add.
     */
    public synchronized void addEventListener(EventListener listener) {
        if (eventListeners.contains(listener)) {
            return;
        }
        eventListeners.add(listener);
        for (View2D view : views) {
            view.addEventListener(listener);
        }
    }

    /**
     * Remove an event listener from all of this window's views.
     * @param listener The listener to remove.
     */
    public synchronized void removeEventListener(EventListener listener) {
        if (eventListeners.contains(listener)) {
            eventListeners.remove(listener);
            for (View2D view : views) {
                view.removeEventListener(listener);
            }
        }
    }

    /**
     * Does this window's views have the given listener attached to them?
     * @param listener The listener to check.
     */
    public synchronized boolean hasEventListener(EventListener listener) {
        return eventListeners.contains(listener);
    }

    /** Given a entity component class returns the corresponding entity component. */
    private EntityComponentEntry entityComponentEntryForClass(Class clazz) {
        for (EntityComponentEntry entry : entityComponents) {
            if (entry.clazz.equals(clazz)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Add an entity component to all of this window's views.
     * If the window's views already have an entity component with this class, nothing happens.
     */
    public synchronized void addEntityComponent(Class clazz, EntityComponent comp) {
        if (entityComponentEntryForClass(clazz) != null) {
            return;
        }
        entityComponents.add(new EntityComponentEntry(clazz, comp));
        for (View2D view : views) {
            view.addEntityComponent(clazz, comp);
        }
    }

    /**
     * Remove an entity component from this window's view that have them.
     */
    public synchronized void removeEntityComponent(Class clazz) {
        EntityComponentEntry entry = entityComponentEntryForClass(clazz);
        if (entry != null) {
            entityComponents.remove(entry);
            for (View2D view : views) {
                view.removeEntityComponent(clazz);
            }
        }
    }

    /**
     * Returns the entity component of the given class which this window's views have attached.
     * @param listener The listener to check.
     */
    public synchronized EntityComponent getEntityComponent(Class clazz) {
        EntityComponentEntry entry = entityComponentEntryForClass(clazz);
        if (entry == null) {
            return null;
        } else {
            return entry.comp;
        }
    }

    /**
     * Adds a view to the window and sets the view's window-dependent attributes to the current window state.
     * Thereafter, changes to the window state result in corresponding changes to these attributes.
     * (In other words, things that happen to a window happen the same to all of its views).
     */
    public synchronized void addView(View2D view) {
        if (views.contains(view)) {
            return;
        }

        // TODO: someday: Currently ViewSet2D constrains a view for a window to appear only once in 
        // a single displayer. Someday we might relax this. Until then we enforce it.
        if (getView(view.getDisplayer()) != null) {
            throw new RuntimeException("A view of this window is already in this view's displayer.");
        }

        views.add(view);
        addViewForDisplayer(view);

        changeMask = CHANGED_ALL;
        updateViews();

        // Attach event listeners and entity components to this new view
        for (EventListener listener : eventListeners) {
            view.addEventListener(listener);
        }
        for (EntityComponentEntry entry : entityComponents) {
            view.addEntityComponent(entry.clazz, entry.comp);
        }
    }

    /**
     * Removes a view from the window.
     */
    public synchronized void removeView(View2D view) {
        if (views.remove(view)) {
            removeViewForDisplayer(view);

            // Detach event listeners and entity components to this new view
            for (EventListener listener : eventListeners) {
                view.removeEventListener(listener);
            }
            for (EntityComponentEntry entry : entityComponents) {
                view.removeEntityComponent(entry.clazz);
            }
        }
    }

    /**
     * Remove all views from the window.
     */
    public synchronized void removeViewsAll() {
        LinkedList<View2D> viewsToRemove = (LinkedList<View2D>) views.clone();
        for (View2D view : viewsToRemove) {
            View2DDisplayer displayer = view.getDisplayer();
            displayer.destroyView(view);
        }
        views.clear();
    }

    /** Add a new view for the displayer of the view. */
    private void addViewForDisplayer(View2D view) {
        View2DDisplayer displayer = view.getDisplayer();
        displayerToView.put(displayer, view);
    }

    /** Remove a view for the displayer of the view. */
    private void removeViewForDisplayer(View2D view) {
        View2DDisplayer displayer = view.getDisplayer();
        displayerToView.remove(displayer);
    }

    /**
     * Returns the view of this window in the given displayer.
     */
    public synchronized View2D getView(View2DDisplayer displayer) {
        return displayerToView.get(displayer);
    }

    /**
     * Returns an iterator over the views of this window for all displayers.
     */
    public synchronized Iterator<View2D> getViews() {
        return views.iterator();
    }

    /** {@inheritDoc}
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Update all views with the current state of the window.
     */
    protected void updateViews() {
        
        // Only update this window's views if the window is visible and this isn't a visibility change.
        // This improves performance and decreases transient visible artifacts from view frames.
        if (!isVisibleApp() &&
            ((changeMask & (CHANGED_VISIBLE_APP | CHANGED_SIZE)) == 0)) {
            return;
        }

        logger.info("=================== Processing window changes for window " + getName());
        logger.info(" changeMask = " + Integer.toHexString(changeMask));

        for (View2D view : views) {
            if ((changeMask & CHANGED_TYPE) != 0) {
                View2D.Type viewType;
                switch (type) {
                    case UNKNOWN:
                        viewType = View2D.Type.UNKNOWN;
                        break;
                    case PRIMARY:
                        viewType = View2D.Type.PRIMARY;
                        break;
                    case POPUP:
                        viewType = View2D.Type.POPUP;
                        break;
                    case SECONDARY:
                        viewType = View2D.Type.SECONDARY;
                        break;
                    default:
                        throw new RuntimeException("Window " + this + " has an invalid type " + type);
                }
                view.setType(viewType, false);
            }
            if ((changeMask & CHANGED_PARENT) != 0) {
                View2D parentView = null;
                if (parent != null) {
                    parentView = parent.getView(view.getDisplayer());
                }
                view.setParent(parentView, false);
            }
            if ((changeMask & CHANGED_OFFSET) != 0) {
                view.setOffset(offset, false);
            }
            if ((changeMask & CHANGED_VISIBLE_APP) != 0) {
                view.setVisibleApp(visibleApp, false);
            }
            if ((changeMask & CHANGED_SIZE) != 0) {
                updateTexture();
                view.setSizeApp(size, false);
            }
            if ((changeMask & CHANGED_DECORATED) != 0) {
                view.setDecorated(decorated, false);
            }
            if ((changeMask & CHANGED_TITLE) != 0) {
                view.setTitle(title, false);
            }
            if ((changeMask & CHANGED_STACK) != 0) {
                view.stackChanged(false);
            }
            view.update();
        }

        logger.info("Done processing changes for window " + getName());
        changeMask = 0;
    }

    /** 
     * The window size has been updated. Recreate the texture.
     */
    protected void updateTexture() {

        // TODO: someday dynamically detect graphics card support for NPOT
        int roundedWidth = getSmallestEnclosingPowerOf2(size.width);
        int roundedHeight = getSmallestEnclosingPowerOf2(size.height);

        // Check if we already have the size we want
        if (texture != null) {
            int texWidth = texture.getImage().getWidth();
            int texHeight = texture.getImage().getHeight();
            if (texWidth == roundedWidth && texHeight == roundedHeight) {
                return;
            }
        }

        // Create the buffered image using dummy data to initialize it
        // TODO: change this after by ref textures are implemented
        ByteBuffer data = BufferUtils.createByteBuffer(roundedWidth * roundedHeight * 4);
        Image image = new Image(Image.Format.RGB8, roundedWidth, roundedHeight, data);

        // Create the texture which wraps the image
        texture = new Texture2D();
        logger.fine("Created new texture " + texture);
        texture.setImage(image);
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setApply(Texture.ApplyMode.Replace);

    /*
     * TODO: NOTYET: set anisotropic filtering
     * This improves texture filtering when the texture is close up from the side,
     * viewing down the length of the window, but it actually causes more drop outs
     * in magnified text in some cases.
     * The anticipated Java3D code was:
    texture.setAnisotropicFilterMode(Texture2D.ANISOTROPIC_SINGLE_VALUE);
    texture.setAnisotropicFilterDegree(8.0f);
     */

        if (surface != null) {
            surface.setTexture(texture);
        }
    }

    /** 
     * Rounds up the given value to the nearest power of two which is larger or equal to the value.
     * @param value The value to round.
     */
    private static int getSmallestEnclosingPowerOf2(int value) {

        if (value < 1) {
            return value;
        }

        int powerValue = 1;
        for (;;) {
            powerValue *= 2;
            if (value <= powerValue) {
                return powerValue;
            }
        }
    }

    /**
     * Initialize the contents of the surface.
     */
    protected void initializeSurface() {
        if (surface != null) {
            surface.initializeSurface();
        }
    }

    protected void repaint() {
    }
}
