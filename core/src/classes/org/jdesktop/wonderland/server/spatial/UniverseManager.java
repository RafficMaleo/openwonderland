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
package org.jdesktop.wonderland.server.spatial;

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
//import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;

/**
 * Manages Cells within the WonderlandUniverse. Computes and tracks the
 * WorldTransform and WorldBounds of cells as their localTransform and localBounds
 * are modified (or parents/children in the graph are modified). Changes to
 * the graph will trigger cell cache updates.
 *
 * Each modification is scheduled in associating with the calling Darkstar
 * transaction so the change is not immediate but is guaranteed to complete (or fail)
 * correctly with the calling transaction.
 *
 * @author paulby
 */
public interface UniverseManager {

    public void addChild(CellMO parent, CellMO child);

    public void addTransformChangeListener(CellMO cell, TransformChangeListenerSrv listener);

    public void createCell(CellMO cellMO);

    public void removeCell(CellMO cellMO);

    public void removeChild(CellMO parent, CellMO child);

    public  void addRootToUniverse(CellMO rootCellMO);

    public void removeRootFromUniverse(CellMO rootCellMO);

    public void removeTransformChangeListener(CellMO cell, TransformChangeListenerSrv listener);

    public void setLocalTransform(CellMO cell, CellTransform localCellTransform);

    public CellTransform getWorldTransform(CellMO cell, CellTransform result);

    public BoundingVolume getWorldBounds(CellMO cell, BoundingVolume result);

    public void viewLogin(ViewCellMO viewCell);

    public void viewLogout(ViewCellMO viewCell);

    /**
     * Add a ViewUpdateLIstener to this cell. This listener will be called
     * whenever the view of a ViewCache that contains this cell is updated
     *
     * @param viewUpdateListener listener to add
     */
    public void addViewUpdateListener(CellMO cell, ViewUpdateListener viewUpdateListener);

    /**
     * Remove the specified ViewUpdateListener
     * @param viewUpdateListener listener to remove
     */
    public void removeViewUpdateListener(CellMO cell, ViewUpdateListener viewUpdateListener);

    // Don't expose this until we have a good test case
    /**
     * Schedule a runnable to execute when the transaction commits. This
     * is useful if you need to interact with other changes in the UniverseManager
     * that are pending on the completion of the current transaction.
     */
//    public void schedule(Runnable runnable);
}
