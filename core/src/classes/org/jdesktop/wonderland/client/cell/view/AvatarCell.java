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
package org.jdesktop.wonderland.client.cell.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarActionTrigger;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * A cell representing the users avatar
 * 
 * @author paulby
 */
public class AvatarCell extends ViewCell {

    public AvatarCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * TODO this is a temporary interface for handling avatar actions, need
     * to uplevel
     * @param trigger
     * @param pressed
     */
    public void triggerAction(int trigger, boolean pressed) {
        if (ClientContext.getRendererType()==RendererType.RENDERER_JME) {
            CellRenderer rend = getCellRenderer(RendererType.RENDERER_JME);
            if (rend instanceof AvatarActionTrigger) {
                ((AvatarActionTrigger)rend).trigger(trigger, pressed);
            }
        }
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                if (ViewManager.getViewManager().useAvatars) {
                    try {
                        ret = ClientContextJME.getAvatarRenderManager().createRenderer("org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME", this);
                    } catch (RendererUnavailable ex) {
                        Logger.getLogger(AvatarCell.class.getName()).log(Level.SEVERE, null, ex);
                        ret = new AvatarJME(this);
                    }
                } else {
                    ret = new AvatarJME(this);
                }
                break;                
        }
        
        return ret;
    }

}
