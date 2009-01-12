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
package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.StaticModelCellConfig;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 * A cell for static models.
 * @author paulby
 * @deprecated
 */
@ExperimentalAPI
public class StaticModelCellMO extends CellMO { 
    	
    /** Default constructor, used when cell is created via WFS */
    public StaticModelCellMO() {
    }

    public StaticModelCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
    }
    
    @Override protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.StaticModelCell";
    }

    @Override
    public CellClientState getCellClientState(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return new StaticModelCellConfig();
    }

    @Override
    public void setServerState(CellServerState setup) {
        super.setServerState(setup);
    }

    @Override
    public void reconfigureCell(CellServerState setup) {
        super.reconfigureCell(setup);
        setServerState(setup);
    }
}
