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
package org.jdesktop.wonderland.client.jme;

import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.scene.Node;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.common.cell.CellTransform;


/**
 * This is simple camera control which mimics the typical first person shooter
 * camera control
 * 
 * @author Doug Twilleager
 */
public class SimpleAvatarControls extends AWTEventProcessorComponent {
    /**
     * The arming conditions for this processor
     */
    private ProcessorArmingCollection collection = null;
    
    /**
     * First, some common variables
     */
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    
    /**
     * The cumulative rotation in Y and X
     */
    private float rotY = 0.0f;
    private float rotX = 0.0f;
    
    /**
     * This scales each change in X and Y
     */
    private float scaleX = 0.7f;
    private float scaleY = 0.7f;
    private float walkInc = 0.5f;
    
    /**
     * States for movement
     */
    private static final int STOPPED = 0;
    private static final int WALKING_FORWARD = 1;
    private static final int WALKING_BACK = 2;
    private static final int STRAFE_LEFT = 3;
    private static final int STRAFE_RIGHT = 4;
    
    /**
     * Our current state
     */
    private int state = STOPPED;
    
    /**
     * Our current position
     */
    private Vector3f position = new Vector3f(0.0f, 10.0f, -30.0f);
        
    /**
     * The Y Axis
     */
    private Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);
    
    /**
     * Our current forward direction
     */
    private Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f rotatedFwdDirection = new Vector3f();
    
    /**
     * Our current side direction
     */
    private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f rotatedSideDirection = new Vector3f();
    
    /**
     * The quaternion for our rotations
     */
    private Quaternion quaternion = new Quaternion();
    
    /**
     * This is used to keep the direction rotated
     */
    private Matrix3f directionRotation = new Matrix3f();
    
    /**
     * The Node to modify
     */
    private Cell target = null;
    private MovableComponent movableComponent = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    private boolean updateRotations = false;
    
    private AWTInputComponent listener;
    
    /**
     * The default constructor
     */
    public SimpleAvatarControls(AWTInputComponent listener, Cell viewCell,
            WorldManager wm) {
        super(listener);
        this.listener = listener;
        target = viewCell;
        movableComponent = viewCell.getComponent(MovableComponent.class);
        worldManager = wm;
//        worldManager.addNodeListener(this);
        
        collection = new ProcessorArmingCollection(this);
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
    }
    
    @Override
    public void initialize() {
        setArmingCondition(collection);
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = listener.getEvents();
        updateRotations = false;
        
        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_MOVED) {
                    processRotations(me);
                    updateRotations = true;
                }
            } else if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
        
        if (updateRotations) {
            directionRotation.fromAngleAxis(rotY*(float)Math.PI/180.0f, yDir);
            directionRotation.mult(fwdDirection, rotatedFwdDirection);
            directionRotation.mult(sideDirection, rotatedSideDirection);
            //System.out.println("Forward: " + rotatedFwdDirection);
            quaternion.fromAngles(rotX*(float)Math.PI/180.0f, rotY*(float)Math.PI/180.0f, 0.0f);
        }
        
        updatePosition();
    }
    
    private void processRotations(MouseEvent me) {
        int deltaX = 0;
        int deltaY = 0;
        int currentX = 0;
        int currentY = 0;
        currentX = me.getX();
        currentY = me.getY();

        if (lastMouseX == -1) {
            // First time through, just initialize
            lastMouseX = currentX;
            lastMouseY = currentY;
        } else {
            deltaX = currentX - lastMouseX;
            deltaY = currentY - lastMouseY;
            deltaX = -deltaX;

            rotY += (deltaX * scaleX);
            rotX += (deltaY * scaleY);
            if (rotX > 60.0f) {
                rotX = 60.0f;
            } else if (rotX < -60.0f) {
                rotX = -60.0f;
            }
            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }
    
    
    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_W) {
                state = WALKING_FORWARD;
            }
            if (ke.getKeyCode() == KeyEvent.VK_S) {
                state = WALKING_BACK;
            }
            if (ke.getKeyCode() == KeyEvent.VK_A) {
                state = STRAFE_LEFT;
            }
            if (ke.getKeyCode() == KeyEvent.VK_D) {
                state = STRAFE_RIGHT;
            }
        }
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_W ||
                ke.getKeyCode() == KeyEvent.VK_S ||
                ke.getKeyCode() == KeyEvent.VK_A ||
                ke.getKeyCode() == KeyEvent.VK_D) {
                state = STOPPED;
            }
        }
    }
    
    private void updatePosition() {
        switch (state) {
            case WALKING_FORWARD:
                position.x += (walkInc * rotatedFwdDirection.x);
                position.y += (walkInc * rotatedFwdDirection.y);
                position.z += (walkInc * rotatedFwdDirection.z);
                break;
            case WALKING_BACK:
                position.x -= (walkInc * rotatedFwdDirection.x);
                position.y -= (walkInc * rotatedFwdDirection.y);
                position.z -= (walkInc * rotatedFwdDirection.z);
                break;
            case STRAFE_LEFT:
                position.x += (walkInc * rotatedSideDirection.x);
                position.y += (walkInc * rotatedSideDirection.y);
                position.z += (walkInc * rotatedSideDirection.z);
                break;
            case STRAFE_RIGHT:
                position.x -= (walkInc * rotatedSideDirection.x);
                position.y -= (walkInc * rotatedSideDirection.y);
                position.z -= (walkInc * rotatedSideDirection.z);
                break;  
        }
        
    }
    /**
     * The commit methods
     */
    @Override
    public void commit(ProcessorArmingCollection collection) {
        if (state!=STOPPED || updateRotations) {
            movableComponent.localMoveRequest(new CellTransform(quaternion, position));
        }

//        worldManager.addToUpdateList(target);
    }
    
    /**
     * The scene listener method
     */
    public void nodeMoved(Node node) {
//        System.out.println("Node Moved " + node);
    }
}
