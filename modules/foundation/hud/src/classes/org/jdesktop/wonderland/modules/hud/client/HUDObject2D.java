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
package org.jdesktop.wonderland.modules.hud.client;

import com.jme.math.Vector3f;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.client.animation.FloatInterpolator;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDObject;

/**
 * A rectangular 2D visual object.
 *
 * @author nsimpson
 */
public class HUDObject2D implements HUDObject {

    private static final Logger logger = Logger.getLogger(HUDObject2D.class.getName());
    protected String name;
    protected Rectangle2D bounds;       // on-HUD position
    protected Vector3f worldLocation;   // in-world position
    protected DisplayMode mode = DisplayMode.HUD;
    protected boolean visible = false;
    protected boolean worldVisible = false;
    protected float preferredTransparency = 1.0f;
    protected float transparency = preferredTransparency;
    protected boolean enabled = false;
    protected boolean minimized = false;
    protected boolean decoratable = true;
    protected ImageIcon iconImage;
    protected Layout compassPoint = Layout.NONE;
    protected ConcurrentLinkedQueue<HUDEventListener> listeners;
    private ConcurrentLinkedQueue<HUDEvent> eventQueue;
    private boolean notifying = false;
    private int id;

    public HUDObject2D() {
        listeners = new ConcurrentLinkedQueue();
        eventQueue = new ConcurrentLinkedQueue();
        bounds = new Rectangle2D.Double();
        id = (int) (Math.random() * 10000);
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(int width) {
        bounds.setRect(bounds.getX(), bounds.getY(), width, bounds.getHeight());

        notifyEventListeners(HUDEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth() {
        return (int) bounds.getWidth();
    }

    /**
     * {@inheritDoc}
     */
    public void setHeight(int height) {
        bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), height);

        notifyEventListeners(HUDEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public int getHeight() {
        return (int) bounds.getHeight();
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(int width, int height) {
        bounds.setRect(bounds.getX(), bounds.getY(), width, height);

        notifyEventListeners(HUDEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(Dimension dimension) {
        bounds.setRect(bounds.getX(), bounds.getY(), dimension.getWidth(), dimension.getHeight());

        notifyEventListeners(HUDEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getSize() {
        return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    /**
     * {@inheritDoc}
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;

        notifyEventListeners(HUDEventType.RESIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setBounds(int x, int y, int width, int height) {
        setBounds(new Rectangle(x, y, width, height));
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle getBounds() {
        return bounds.getBounds();
    }

    /**
     * {@inheritDoc}
     */
    public void setX(int x) {
        bounds.setRect(x, bounds.getY(), bounds.getWidth(), bounds.getHeight());

        notifyEventListeners(HUDEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getX() {
        return (int) bounds.getX();
    }

    /**
     * {@inheritDoc}
     */
    public void setY(int y) {
        bounds.setRect(bounds.getX(), y, bounds.getWidth(), bounds.getHeight());

        notifyEventListeners(HUDEventType.MOVED);
    }

    /**
     * {@inheritDoc}
     */
    public int getY() {
        return (int) bounds.getY();
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(int x, int y) {
        setLocation(x, y, true);
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(int x, int y, boolean notify) {
        bounds.setRect(x, y, bounds.getWidth(), bounds.getHeight());

        if (notify) {
            notifyEventListeners(HUDEventType.MOVED);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * {@inheritDoc}
     */
    public Point getLocation() {
        return new Point((int) bounds.getX(), (int) bounds.getY());
    }

    /**
     * {@inheritDoc}
     */
    public void setPreferredLocation(Layout compassPoint) {
        this.compassPoint = compassPoint;
    }

    /**
     * {@inheritDoc}
     */
    public Layout getPreferredLocation() {
        return compassPoint;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorldLocation(Vector3f location) {
        this.worldLocation = location;

        notifyEventListeners(HUDEventType.MOVED_WORLD);
    }

    /**
     * {@inheritDoc}
     */
    public Vector3f getWorldLocation() {
        return worldLocation;
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;

        notifyEventListeners((visible == true) ? HUDEventType.APPEARED
                : HUDEventType.DISAPPEARED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorldVisible(boolean worldVisible) {
        if (this.worldVisible == worldVisible) {
            return;
        }
        this.worldVisible = worldVisible;

        notifyEventListeners((worldVisible == true) ? HUDEventType.APPEARED_WORLD
                : HUDEventType.DISAPPEARED_WORLD);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWorldVisible() {
        return worldVisible;
    }

    /**
     * {@inheritDoc}
     */
    public void setClosed() {
        notifyEventListeners(HUDEventType.CLOSED);
    }

    /**
     * {@inheritDoc}
     */
    public void setDisplayMode(DisplayMode mode) {
        this.mode = mode;

        notifyEventListeners(HUDEventType.CHANGED_MODE);
    }

    /**
     * {@inheritDoc}
     */
    public DisplayMode getDisplayMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    public void setPreferredTransparency(Float preferredTransparency) {
        this.preferredTransparency = preferredTransparency;
    }

    /**
     * {@inheritDoc}
     */
    public float getPreferredTransparency() {
        return preferredTransparency;
    }

    /**
     * {@inheritDoc}
     */
    public void setTransparency(Float transparency) {
        this.transparency = transparency;

        notifyEventListeners(HUDEventType.CHANGED_TRANSPARENCY);
    }

    /**
     * {@inheritDoc}
     */
    public float getTransparency() {
        return transparency;
    }

    /**
     * {@inheritDoc}
     */
    public void changeTransparency(Float from, Float to) {
        new Thread(new HUDAnimator(this, "transparency", new FloatInterpolator(), from, to)).start();
    }

    /**
     * {@inheritDoc}
     */
    public void changeTransparency(Float from, Float to, long duration) {
        new Thread(new HUDAnimator(this, "transparency", new FloatInterpolator(), from, to, duration)).start();
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;

        notifyEventListeners((enabled == true) ? HUDEventType.ENABLED
                : HUDEventType.DISABLED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setMinimized() {
        if (minimized == true) {
            return;
        }
        minimized = true;

        notifyEventListeners(HUDEventType.MINIMIZED);
    }

    /**
     * {@inheritDoc}
     */
    public void setMaximized() {
        if (minimized == false) {
            return;
        }
        minimized = false;

        notifyEventListeners(HUDEventType.MAXIMIZED);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMinimized() {
        return minimized;
    }

    /**
     * {@inheritDoc}
     */
    public void setDecoratable(boolean decoratable) {
        this.decoratable = decoratable;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getDecoratable() {
        return decoratable;
    }

    /**
     * {@inheritDoc}
     */
    public void setIcon(ImageIcon iconImage) {
        this.iconImage = iconImage;
    }

    /**
     * {@inheritDoc}
     */
    public ImageIcon getIcon() {
        return iconImage;
    }

    /**
     * {@inheritDoc}
     */
    public void addEventListener(HUDEventListener listener) {
        listeners.add(listener);
        logger.finest(this.getClass().getSimpleName() + " added event listener: " + listener.getClass().getSimpleName() + ", " + listeners.size() + " listeners");
        // TODO: notify the new listener that the component was created?
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventListener(HUDEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    public HUDEventListener[] getEventListeners() {
        return listeners.toArray(new HUDEventListener[0]);
    }

    /**
     * {@inheritDoc}
     */
    public void notifyEventListeners(final HUDEvent e) {
        if (listeners != null) {
            eventQueue.add(new HUDEvent(e));
            if (notifying) {
                // already notifying, let the current notification loop handle
                // this new event as well
                return;
            } else {
                notifying = true;
            }

            //logger.finest("=== [" + id + "] START notifying listeners");

            while (!eventQueue.isEmpty()) {
                HUDEvent ev = (HUDEvent) eventQueue.remove();
                HUDEventType type = ev.getEventType();
                Iterator<HUDEventListener> iterator = listeners.iterator();
                //int num = listeners.size();
                //int i = 1;
                while (iterator.hasNext()) {
                    HUDEventListener notifiee = iterator.next();
                    //logger.finest("   === [" + id + "] " + i + "/" + num + ": sending " + type + " to " + notifiee.getClass().getSimpleName());
                    //i++;
                    notifiee.HUDObjectChanged(ev);
                }
                iterator = null;
                ev = null;
            }

            //logger.finest("=== [" + id + "] DONE notifying listeners: " + eventQueue.size());
            notifying = false;
        }
    }

    /**
     * Convenience methods for notifying listeners
     * @param eventType the type of the notification event
     */
    public void notifyEventListeners(HUDEventType eventType) {
        notifyEventListeners(new HUDEvent(this, eventType, new Date()));
    }

    @Override
    public String toString() {
        return "HUDObject2D: " + id +
                ", bounds: " + bounds +
                ", mode: " + mode +
                ", visible: " + visible +
                ", world visible: " + worldVisible +
                ", enabled: " + enabled +
                ", transparency: " + transparency;
    }
}
