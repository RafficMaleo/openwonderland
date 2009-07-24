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
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.testcells.server.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.ServerPlugin;

/**
 *
 * @author paulby
 */
public class TestWorld implements ServerPlugin {

    public TestWorld() {
    }

    public void initialize() {
        try {
            //WonderlandContext.getCellManager().insertCellInWorld(new SimpleShapeCellMO(new Vector3f(7, 0, 5), 1));

            WonderlandContext.getCellManager().insertCellInWorld(new MouseSpinCellMO(new Vector3f(-8, 0, 2), 1));

//            WonderlandContext.getCellManager().insertCellInWorld(new SingingTeapotCellMO(new Vector3f(-8, 0, 7), 1, new MaterialJME(ColorRGBA.green, null, null, null, 0.5f)));
            
            WonderlandContext.getCellManager().insertCellInWorld(new DragTestMO(new Vector3f(8, 0, 2), 1));

//            WonderlandContext.getCellManager().insertCellInWorld(new SimpleShapeCellMO(new Vector3f(0, 2, -5), 1, SimpleShapeConfig.Shape.SPHERE));
//
//            WonderlandContext.getCellManager().insertCellInWorld(new SimpleShapeCellMO(new Vector3f(0, 2, 5), 1, SimpleShapeConfig.Shape.CONE));
//
//            WonderlandContext.getCellManager().insertCellInWorld(new SimpleShapeCellMO(new Vector3f(5, 2, 0), 1, SimpleShapeConfig.Shape.CYLINDER));
            
//            WonderlandContext.getCellManager().insertCellInWorld(new MirrorCellMO(new Vector3f(0, 0, 0), 20));
        } catch (Exception ex) {
            Logger.getLogger(CellManagerMO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}