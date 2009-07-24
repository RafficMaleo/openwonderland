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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDButton;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentManager;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.hud.HUDMessage;

/**
 * A WonderlandHUD is a 2D region of the Wonderland client window on which HUDComponents
 * can be displayed.
 * 
 * A client may have multiple HUDs. For example, a Status HUD which displays
 * status information about the user's session, and an Audio HUD for audio
 * controls.
 * 
 * A HUD contains HUDComponents which are visual objects such as a 2D control
 * panel or a representation of a 3D object. HUDComponents are laid out within
 * a HUD by a HUDLayoutManager.
 *
 *
 * @author nsimpson
 */
public class WonderlandHUD extends HUDObject2D implements HUD {

    private static final Logger logger = Logger.getLogger(WonderlandHUD.class.getName());
    protected List components;
    protected HUDComponentManager componentManager;
    private Dimension displayBounds = new Dimension();
    private Rectangle2D.Float scalableBounds = null;
    private static final int HUD_DEFAULT_X = 0;
    private static final int HUD_DEFAULT_Y = 0;
    private static final int HUD_DEFAULT_WIDTH = 800;
    private static final int HUD_DEFAULT_HEIGHT = 600;

    public WonderlandHUD() {
        super();
        components = Collections.synchronizedList(new ArrayList());
        bounds = new Rectangle(HUD_DEFAULT_X, HUD_DEFAULT_Y,
                HUD_DEFAULT_WIDTH, HUD_DEFAULT_HEIGHT);
    }

    /**
     * Creates a new Wonderland HUD instance the same size as the display.
     * @param displayBounds the size of the display
     * @param hudBounds the size and position of the HUD
     */
    public WonderlandHUD(Dimension displayBounds, Rectangle hudBounds) {
        this();
        this.bounds = hudBounds;
        this.displayBounds = displayBounds;
    }

    /**
     * Creates a new Wonderland HUD instance with a fixed size.
     * @param displayBounds the size of the display in pixels
     * @param x the x position of the HUD relative to the x origin of the view
     * @param y the y position of the HUD relative to the y origin of the view
     * @param width the width of the HUD relative to the width of the view
     * @param height the height of the HUD relative to the height of the view
     */
    public WonderlandHUD(Dimension displayBounds, int x, int y, int width, int height) {
        this(displayBounds, new Rectangle(x, y, width, height));
    }

    /**
     * Creates a new Wonderland HUD instance using percentages of the display
     * size for the bounds of the HUD.
     * @param displayBounds the size of the display in pixels
     * @param scalableBounds the size and position of the HUD expressed in
     * percentages
     */
    public WonderlandHUD(Dimension displayBounds, Rectangle2D.Float scalableBounds) {
        this();
        setDisplayBounds(displayBounds);
        setScalableBounds(scalableBounds);
    }

    /**
     * Creates a new HUD instance with scalable bounds.
     * @param displayBounds the size of the display in pixels
     * @param xPercent the x-coordinate of the HUD as a percentage of the width
     * of the display
     * @param yPercent the y-coordinate of the HUD as a percentage of the height
     * of the display
     * @param widthPercent the width of the HUD as a percentage of the width of
     * the display
     * @param heightPercent the height of the HUD as a percentage of the height
     * of the display
     * @return a new HUD instance with default location and size
     */
    public WonderlandHUD(Dimension displayBounds, float xPercent, float yPercent,
            float widthPercent, float heightPercent) {
        this(displayBounds, new Rectangle2D.Float(xPercent, yPercent, widthPercent, heightPercent));
    }

    /**
     * {@inheritDoc}
     */
    public void setDisplayBounds(Dimension displayBounds) {
        this.displayBounds = displayBounds;
        if (hasScalableBounds()) {
            // recalculate scalable bounds and resize HUD
            setScalableBounds(getScalableBounds());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getDisplayBounds() {
        return displayBounds;
    }

    /**
     * {@inheritDoc}
     */
    public void setScalableBounds(Rectangle2D.Float scalableBounds) {
        this.scalableBounds = scalableBounds;
        // NOTE: triggers a resize event notification
        setBounds(new Rectangle(
                (int) (scalableBounds.x * displayBounds.width),
                (int) (scalableBounds.y * displayBounds.height),
                (int) (scalableBounds.width * displayBounds.width),
                (int) (scalableBounds.height * displayBounds.height)));
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle2D.Float getScalableBounds() {
        return scalableBounds;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasScalableBounds() {
        return (scalableBounds != null);
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent createComponent() {
        return new HUDComponent2D();
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent createComponent(JComponent component) {
        return new HUDComponent2D(component);
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent createComponent(JComponent component, Cell cell) {
        return new HUDComponent2D(component, cell);
    }

    /**
     * {@inheritDoc}
     */
    public HUDMessage createMessage(String message) {
        return new HUDMessageComponent(message);
    }

    /**
     * {@inheritDoc}
     */
    public HUDDialog createDialog(String text) {
        return new HUDDialogComponent(text);
    }

    /**
     * {@inheritDoc}
     */
    public HUDButton createButton(String label) {
        return new HUDButtonComponent(label);
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(HUDComponent component) {
        components.add(component);

        if (componentManager != null) {
            componentManager.addComponent(component);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(HUDComponent component) {
        if (componentManager != null) {
            componentManager.removeComponent(component);
        }
        components.remove(component);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<HUDComponent> getComponents() {
        return components.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasComponents() {
        return !components.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void setComponentManager(HUDComponentManager componentManager) {
        if (components.size() > 0) {
            HUDComponent comps[] = (HUDComponent[]) components.toArray();

            if (this.componentManager != null) {
                // already have a component manager, so remove the components
                // it's managing in this HUD
                for (HUDComponent component : comps) {
                    this.componentManager.removeComponent(component);
                }
            }
            if (componentManager != null) {
                // add the components in this HUD to the new component manager
                for (HUDComponent component : comps) {
                    component.addEventListener((WonderlandHUDComponentManager) componentManager);
                }
            }
        }

        this.componentManager = componentManager;
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponentManager getComponentManager() {
        return componentManager;
    }
}
