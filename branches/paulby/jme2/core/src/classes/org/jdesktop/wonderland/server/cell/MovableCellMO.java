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
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;

/**
 * For cells that are expected to move frequently
 * 
 * TODO - Do we need a listener that allows veto of a move request, don't
 * think so instead I suggest we would subclass to add veto capability
 * 
 * @author paulby
 */
public class MovableCellMO extends CellMO {

    public MovableCellMO(BoundingVolume bounds, CellTransform transform) {
        super(bounds, transform);
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableComponentMO(this));
    }
    

    @Override protected String getClientCellClassName(ClientSession clientSession,ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.cell.MovableCell";
    }
    


}