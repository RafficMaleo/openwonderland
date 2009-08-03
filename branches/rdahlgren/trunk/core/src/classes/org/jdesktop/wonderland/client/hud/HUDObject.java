/*
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
package org.jdesktop.wonderland.client.hud;

import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;

/**
 * A generic HUD type that defines a rectangular 2D visual.
 *
 * @author nsimpson
 */
public interface HUDObject {

    /**
     * Display modes
     */
    public enum DisplayMode {

        /**
         * Display in world
         */
        WORLD,
        /**
         * Display on screen
         */
        HUD
    };

    /**
     * Assigns a name to this HUD object
     * @param name the name to assign to this HUD object
     */
    public void setName(String name);

    /**
     * Gets the name assigned to this HUD object
     * @return the name of the HUD object
     */
    public String getName();

    /**
     * Sets the width of the HUD object in pixels
     * @param width the width of the HUD object (pixels)
     */
    public void setWidth(int width);

    /**
     * Gets the width of the HUD object in pixels
     * @return the width of the HUD object in pixels
     */
    public int getWidth();

    /**
     * Sets the height of the HUD object in pixels
     * @param height the height of the HUD object (pixels)
     */
    public void setHeight(int height);

    /**
     * Gets the height of the HUD object in pixels
     * @return the height of the HUD object in pixels
     */
    public int getHeight();

    /**
     * Sets the width and height of the HUD object
     * @param width the new width in pixels
     * @param height the new height in pixels
     */
    public void setSize(int width, int height);

    /**
     * Sets the width and height of the HUD object
     * @param dimension the new width and height of the HUD object
     */
    public void setSize(Dimension dimension);

    /**
     * Gets the size of the HUD object
     * @return the component's size
     */
    public Dimension getSize();

    /**
     * Sets the bounds (x, y position, width, height) of the HUD object
     * @param bounds the bounds of the HUD object
     */
    public void setBounds(Rectangle bounds);

    /**
     * Sets the position and size of the HUD object
     * @param x the new x-coordinate of this HUD object
     * @param y the new y-coordinate of this HUD object
     * @param width the new width of this HUD object
     * @param height the new height of this HUD object
     */
    public void setBounds(int x, int y, int width, int height);

    /**
     * Gets the bounds of the HUD object
     * @return the bounds of the HUD object
     */
    public Rectangle getBounds();

    /**
     * Sets the x-coordinate of the HUD object's origin
     * @param x the x-coordinate of the HUD object's origin
     */
    public void setX(int x);

    /**
     * Gets the x-coordinate of the HUD object's origin
     * @return the x-coordinate of the HUD object's origin
     */
    public int getX();

    /**
     * Sets the y-coordinate of the HUD object's origin
     * @param y the y-coordinate of the HUD object's origin
     */
    public void setY(int y);

    /**
     * Gets the y-coordinate of the HUD object's origin
     * @return the y-coordinate of the HUD object's origin
     */
    public int getY();

    /**
     * Moves the HUD object to a new location
     * @param p the new position of the HUD object
     */
    public void setLocation(Point p);

    /**
     * Moves the HUD object to a new location
     * @param x the new x-coordinate of this HUD object
     * @param y the new y-coordinate of this HUD object
     */
    public void setLocation(int x, int y);

    /**
     * Moves the HUD object to a new location
     * @param x the new x-coordinate of this HUD object
     * @param y the new y-coordinate of this HUD object
     * @param notify whether to notify HUD listeners
     */
    public void setLocation(int x, int y, boolean notify);

    /**
     * Gets the location of this HUD object in the form of a point specifying
     * the view's origin
     * @return a Point representing the origin of the HUD object
     */
    public Point getLocation();

    /**
     * Sets the preferred location as a compass point
     * @param compassPoint the compass point location
     */
    public void setPreferredLocation(Layout compassPoint);

    /**
     * Gets the preferred compass point location
     * @return the preferred location as a compass point
     */
    public Layout getPreferredLocation();

    /**
     * Sets the location in 3D space
     * @param location the location of the object in 3D space
     */
    public void setWorldLocation(Vector3f location);

    /**
     * Gets the location of the object in 3D space
     * @return the 3D location
     */
    public Vector3f getWorldLocation();

    /**
     * Sets the visibility of the HUD object
     * @param visible if true, shows the HUD object, otherwise hides the
     * HUD object
     */
    public void setVisible(boolean visible);

    /**
     * Gets whether the HUD object is visible
     * @return true if the HUD object should be visible, false otherwise
     */
    public boolean isVisible();

    /**
     * Sets the visibility of the HUD object in-world
     * @param worldVisible if true, shows the HUD object in world, otherwise hides
     * the HUD object
     */
    public void setWorldVisible(boolean worldVisible);

    /**
     * Gets whether the HUD object is visible in-world
     * @return true if the HUD object should be visible in-world, false otherwise
     */
    public boolean isWorldVisible();

    /**
     * Closes the component
     */
    public void setClosed();

    /**
     * Sets the display mode, either in-world or on-HUD
     * @param mode the new mode
     */
    public void setDisplayMode(DisplayMode mode);

    /**
     * Gets the display mode
     * @return the display mode: in-world or on-HUD
     */
    public DisplayMode getDisplayMode();

    /**
     * Sets the transparency of the HUD object on a scale of 0.0f to 1.0f, where
     * 0.0f means that the HUD object is completely opaque, and 1.0f means the
     * HUD object is completely transparent.
     * Note that transparency is independent of whether the HUD object is visible
     * or not
     * @param transparency the transparency of the HUD object
     */
    public void setTransparency(float transparency);

    /**
     * Gets the transparency of the HUD object
     * @return the transparency of the HUD object on the scale of 0.0f to 1.0f
     */
    public float getTransparency();

    /**
     * Sets whether the HUD object is responsive to mouse and keyboard events
     * @param enabled true if the HUD object is to be enabled, false otherwise
     */
    public void setEnabled(boolean enabled);

    /**
     * Gets whether the HUD object is enabled
     * @return true if the HUD object is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Minimize or unminimize a component
     * @param minimized true to minimize, false to un-minimize
     */
    public void setMinimized(boolean minimized);

    /**
     * Gets the minimized state of a component
     * @return true if the component is minimized, false otherwise
     */
    public boolean isMinimized();

    /**
     * Sets whether the HUD object can be decorated
     * @param decoratable true if the HUD object can be decorated, false otherwise
     */
    public void setDecoratable(boolean decoratable);

    /**
     * Gets whether the HUD object can be decorated
     * @return true if the HUD object can be decorated, false otherwise
     */
    public boolean getDecoratable();

    /**
     * Sets the icon to display when this HUD object is minimized
     * @param iconImage the icon image
     */
    public void setIcon(ImageIcon iconImage);

    /**
     * Gets the icon for ths HUD object
     * @return the icon image
     */
    public ImageIcon getIcon();

    /**
     * Adds a listener for HUD object events
     * @param listener a new listener
     */
    public void addEventListener(HUDEventListener listener);

    /**
     * Removes a listener for HUD object events
     * @param listener the listener to remove
     */
    public void removeEventListener(HUDEventListener listener);

    /**
     * Gets the listeners for this HUD object
     * @return a list of event listeners
     */
    public List<HUDEventListener> getEventListeners();

    /**
     * Notifies this HUD object's event listeners of an event
     * @param event a HUD event
     */
    public void notifyEventListeners(HUDEvent event);

    /**
     * Convenience method for notifying event listeners
     * @param eventType the type of the notification event
     */
    public void notifyEventListeners(HUDEventType eventType);
}