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
package org.jdesktop.wonderland.common.cell.messages;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Messages to/from Entity Cells
 * 
 * @author paulby
 */
public class EntityMessage extends CellMessage {

    private Vector3f translation;
    private Quaternion rotation;

    /**
     * MOVE_REQUEST - client asking the server to move entity
     * MOVED - server informing clients entity has moved
     * 
     */
    public enum ActionType { MOVED, MOVE_REQUEST };
    
    private ActionType actionType;
    
    private EntityMessage(CellID cellID, ActionType actionType) {
        super(cellID);
        this.actionType = actionType;
    }
    
    public Vector3f getTranslation() {
        return translation;
    }

    private void setTranslation(Vector3f locationVW) {
        this.translation = locationVW;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion orientation) {
        this.rotation = orientation;
    }

    public ActionType getActionType() {
        return actionType;
    }
    
    public static EntityMessage newMovedMessage(CellID cellID, CellTransform transform) {
        EntityMessage ret = new EntityMessage(cellID, ActionType.MOVED);
        ret.setTranslation(transform.getTranslation(null));
        ret.setRotation(transform.getRotation(null));
        return ret;
    }
    
     public static EntityMessage newMovedMessage(CellID cellID, Vector3f translation, Quaternion rotation) {
        EntityMessage ret = new EntityMessage(cellID, ActionType.MOVED);
        ret.setTranslation(translation);
        ret.setRotation(rotation);
        return ret;
    }
     
    public static EntityMessage newMoveRequestMessage(CellID cellID, Vector3f translation, Quaternion rotation) {
        EntityMessage ret = new EntityMessage(cellID, ActionType.MOVE_REQUEST);
        ret.setTranslation(translation);
        ret.setRotation(rotation);
        return ret;
    }
    
}
