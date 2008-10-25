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
package org.jdesktop.wonderland.common.cell;

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * The ConnectionType of the CellClient
 * @author jkaplan
 */
@InternalAPI
public class CellChannelConnectionType extends ConnectionType {
    /** the client type for the cell client */
    public static final ConnectionType CLIENT_TYPE =
            new CellChannelConnectionType("__CellClient");
    
    private CellChannelConnectionType(String type) {
        super (type);
    }
}
