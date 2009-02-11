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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;

/**
 * A component that extends MovableComponent to add additional information
 * for animating avatars.
 *
 * @author paulby
 */
@ComponentLookupClass(MovableComponent.class)
public class MovableAvatarComponent extends MovableComponent {
    private int trigger;
    private boolean pressed;

    public final static int NO_TRIGGER = -1;

    public MovableAvatarComponent(Cell cell) {
        super(cell);
    }


    @Override
    public void localMoveRequest(CellTransform transform) {
        localMoveRequest(transform, NO_TRIGGER, false, null);
    }

    @Override
    public void localMoveRequest(CellTransform transform,
                                 final CellMoveModifiedListener listener) {
        localMoveRequest(transform, NO_TRIGGER, false, listener);
    }


    public void localMoveRequest(CellTransform transform,
                                 int trigger,
                                 boolean pressed,
                                 CellMoveModifiedListener listener) {

        synchronized(this) {
            this.trigger = trigger;
            this.pressed = pressed;
            if (trigger==NO_TRIGGER) {
                // Just a transform update, so it can be throttled
                super.localMoveRequest(transform, null);
            } else {
                // State change, so no throttling
                channelComp.send(createMoveRequestMessage(transform));

                // As we don't call super, make sure we update the cell transform
                applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.LOCAL);
            }
        }
    }

    @Override
    protected CellMessage createMoveRequestMessage(CellTransform transform) {
        return MovableAvatarMessage.newMoveRequestMessage(cell.getCellID(),
                                                    transform.getTranslation(null),
                                                    transform.getRotation(null),
                                                    trigger,
                                                    pressed);
    }

    @Override
    protected ResponseListener createMoveResponseListener(final CellMoveModifiedListener listener) {
        throw new RuntimeException("Not supported");
    }

    @Override
    protected Class getMessageClass() {
        return MovableAvatarMessage.class;
    }

    @Override
    protected void serverMoveRequest(MovableMessage msg) {
        super.serverMoveRequest(msg);

        MovableAvatarMessage mam = (MovableAvatarMessage) msg;
        if (mam.getTrigger()!=NO_TRIGGER) {
            ((AvatarCell)cell).triggerAction(mam.getTrigger(), mam.isPressed());
        }
    }


}
