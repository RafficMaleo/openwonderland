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
package org.jdesktop.wonderland.modules.testcells.client.cell;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Interpolators;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.test.MouseEvent3DLogger;
import org.jdesktop.wonderland.client.jme.input.test.SpinObjectEventListener;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.ShapeRenderer;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.RotationAnimationProcessor;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.TranslationAnimationProcessor;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.util.Mouse3DTrigger;
import org.jdesktop.wonderland.modules.testcells.client.timingframework.util.Mouse3DTriggerEvent;

/**
 * Test for mouse over spin
 * 
 * @deprecated
 * @author paulby
 */
public class SingingTeapotCell extends SimpleShapeCell {
    
    public SingingTeapotCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = super.createCellRenderer(rendererType);

        Entity entity = ((CellRendererJME)ret).getEntity();

        Node node = getSceneRoot(entity);
        Vector3f currentLoc = node.getLocalTranslation();
        Vector3f dest = new Vector3f(currentLoc);
        dest.y+=0.3;

        RotationAnimationProcessor spinner = new RotationAnimationProcessor(entity, node, 0f, (float)Math.PI);
        Clip clip2 = Clip.create(1000, 4,  spinner);
        clip2.setInterpolator(Interpolators.getEasingInstance(0.4f, 0.4f));

        Mouse3DTrigger.addTrigger(entity, clip2, Mouse3DTriggerEvent.PRESS);

        TranslationAnimationProcessor trans = new TranslationAnimationProcessor(entity, node, currentLoc, dest);
        Clip clip = Clip.create(500, Clip.INDEFINITE, trans);
        clip.setAutoReverse(true);
        clip.start();
//        Mouse3DTrigger.addTrigger(entity, clip, Mouse3DTriggerEvent.ENTER);

        return ret;
    }
    

    static Node getSceneRoot (Entity entity) {
        RenderComponent renderComp = (RenderComponent) entity.getComponent(RenderComponent.class);
        if (renderComp == null) {
            return null;
        }
        Node node = renderComp.getSceneRoot();
        return node;
    }

}
