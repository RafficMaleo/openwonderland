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
package org.jdesktop.wonderland.modules.buttonboxtest1.server.cell;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.modules.testcells.server.cell.SimpleShapeCellMO;

/**
 * Test of the Button Box
 *
 * @author deronj
 */
@ExperimentalAPI
public class ButtonBoxTest1MO extends SimpleShapeCellMO {
    
    /** Default constructor, used when cell is created via WFS */
    public ButtonBoxTest1MO () {
        this(new Vector3f(), 50);
    }

    public ButtonBoxTest1MO (Vector3f center, float size) {
        super(center, size);
    }
    
    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.buttonboxtest1.client.cell.ButtonBoxTest1";
    }
}
