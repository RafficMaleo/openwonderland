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
package org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault;

import com.jme.scene.Node;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.view.Frame2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 * TODO
 * Note: frame handles mtgame and jme update issues itself.
 * @author deronj
 */
@ExperimentalAPI
public class Frame2DCell implements Frame2D, ControlArb.ControlChangeListener {

    private static final Logger logger = Logger.getLogger(Frame2DCell.class.getName());

    /** The control arb of the app to which this frame's view belongs. */
    private ControlArb controlArb;

    /**
     * Components who wish to be notified when the user has pressed the
     * close button should implement this interface and register themselves
     * with addCloseListener.
     */
    public interface CloseListener {
        /** Called when the user clicks on the frame's close button. */
        public void close();
    }

    /** The height of the header */
    public static final float HEADER_HEIGHT = /* 0.2f */ /*6.3f*/ 1.25f/3f;
    /** The thickness (in the plane of the frame) of the other parts of the border */
    public static final float SIDE_THICKNESS = /*0.07f*/ /* 3.0f */ 0.75f/3f;
    /** The width of the resize corner - currently the same as a header height */
    public static final float RESIZE_CORNER_WIDTH = HEADER_HEIGHT;
    /** The height of the resize corner - currently the same as a header height */
    public static final float RESIZE_CORNER_HEIGHT = HEADER_HEIGHT;
    /** The frame's header (top side) */
    private FrameHeaderSwing header;
    /** The frame's left side */
    private FrameSide leftSide;
    /** The frame's right side */
    private FrameSide rightSide;
    /** The frame's bottom side */
    private FrameSide bottomSide;
    /** The resize corner */
    private FrameResizeCorner resizeCorner;
    /** 
     * The root of the frame subgraph. This contains all geometry and is 
     * connected to the view node via an attach point.
     */
    private Node frameNode;
    /**  The root entity for this frame. */
    private Entity frameEntity;

    /** The name of this frame. */
    private String name;

    /** The view to which this cell belongs. */
    private View2DCell view;

    /** List of listeners to notify when the frame is closed. */
    protected LinkedList<CloseListener> closeListeners = new LinkedList();

    /**
     * Create a new instance of FrameWorldDefault.
     *
     * @param view The view the frame encloses.
     */
    public Frame2DCell (View2DCell view) {
        this.view = view;
        name = "Frame for " + view.getName();

        frameEntity = new Entity("Entity for " + name);
        frameNode = new Node("Node for " + name);
        RenderComponent rc =
                ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(frameNode);
        frameEntity.addComponent(RenderComponent.class, rc);

        header = new FrameHeaderSwing(view, closeListeners);

        leftSide = new FrameSide(view, FrameSide.Side.LEFT, new Gui2DSide(view));
        leftSide.setParentEntity(frameEntity);

        rightSide = new FrameSide(view, FrameSide.Side.RIGHT, new Gui2DSide(view));
        rightSide.setParentEntity(frameEntity);

        bottomSide = new FrameSide(view, FrameSide.Side.BOTTOM, new Gui2DSide(view));
        bottomSide.setParentEntity(frameEntity);

        resizeCorner = new FrameResizeCorner(view, rightSide, bottomSide);
        resizeCorner.setParentEntity(frameEntity);

        controlArb = view.getWindow().getApp().getControlArb();
        if (controlArb != null) {
            controlArb.addListener(this);
        }

        attachToViewEntity(view);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void cleanup() {
        if (view != null) {
            detachFromViewEntity(view.getEntity());
            view = null;
        }
        if (closeListeners != null) {
            closeListeners.clear();
        }
        if (header != null) {
            header.cleanup();
            header = null;
        }
        if (leftSide != null) {
            leftSide.cleanup();
            leftSide = null;
        }
        if (rightSide != null) {
            rightSide.cleanup();
            rightSide = null;
        }
        if (bottomSide != null) {
            bottomSide.cleanup();
            bottomSide = null;
        }
        if (resizeCorner != null) {
            resizeCorner.cleanup();
            resizeCorner = null;
        }
        if (frameEntity != null) {
            frameEntity.removeComponent(RenderComponent.class);
            frameNode = null;
            frameEntity = null;
        }
        if (controlArb != null) {
            controlArb.removeListener(this);
            controlArb = null;
        }
    }

    private void attachToViewEntity (View2DCell view) {
        if (view == null) return;
        Entity viewEntity = view.getEntity();
        if (viewEntity == null) return;

        viewEntity.addEntity(frameEntity);

        RenderComponent rcFrame = (RenderComponent) frameEntity.getComponent(RenderComponent.class);

        if (rcFrame != null) {
            // We need to attach secondary view frames to the GEOMETRY NODE of its views
            // so that they move with the offset of the view
            if (view.getType() == View2D.Type.SECONDARY) {
                rcFrame.setAttachPoint(view.getGeometryNode());
            } else {
                rcFrame.setAttachPoint(view.getViewNode());
            }
        }
    }

    private void detachFromViewEntity (Entity viewEntity) {
        if (viewEntity == null) return;

        viewEntity.removeEntity(frameEntity);
        RenderComponent rcFrame = (RenderComponent) frameEntity.getComponent(RenderComponent.class);
        if (rcFrame != null) {
            rcFrame.setAttachPoint(null);
        }
    }

    /** {@inheritDoc} */
    public View2D getView () {
        return view;
    }

    /** 
     * The size of the view has changed. Make the corresponding
     * position and/or size updates for the frame components.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    public synchronized void update() throws InstantiationException {
        if (header != null) {
            header.update();
        }
        if (leftSide != null) {
            leftSide.update();
        }
        if (rightSide != null) {
            rightSide.update();
        }
        if (bottomSide != null) {
            bottomSide.update();
        }
        if (resizeCorner != null) {
            resizeCorner.update();
        }

        updateControl(controlArb);
    }

    /** {@inheritDoc} */
    public synchronized void setTitle(String title) {
        if (header != null) {
            header.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void updateControl(ControlArb controlArb) {
        if (view == null) return;
        
        // Sometimes some of these are null during debugging
        if (header != null) {
            header.updateControl(controlArb);
        }
        if (leftSide != null) {
            leftSide.updateControl(controlArb);
        }
        if (rightSide != null) {
            rightSide.updateControl(controlArb);
        }
        if (bottomSide != null) {
            bottomSide.updateControl(controlArb);
        }
        if (resizeCorner != null) {
            resizeCorner.updateControl(controlArb);
        }
    }

    /**
     * Add a close listener.
     *
     * @param listener The listener to add.
     */
    public synchronized void addCloseListener(CloseListener listener) {
        closeListeners.add(listener);
    }

    /**
     * Remove a close listener.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeCloseListener(CloseListener listener) {
        closeListeners.remove(listener);
    }

    /**
     * Returns an iterator over all close listeners.
     */
    public synchronized Iterator<CloseListener> getCloseListeners() {
        return closeListeners.iterator();
    }

    @Override
    public String toString () {
        return name;
    }
}

