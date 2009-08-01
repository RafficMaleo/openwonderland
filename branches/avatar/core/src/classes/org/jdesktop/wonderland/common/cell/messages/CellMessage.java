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

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message sent by a client to a particular cell.
 * @author jkaplan
 */
@ExperimentalAPI
public abstract class CellMessage extends Message {
    /** the ID of the cell this message is for */
    private CellID cellID;
    
    /**
     * Create a new cell message to the given cellID on the server
     * @param cellID the id of the cell to send to
     */
    public CellMessage(CellID cellID) {
        this.cellID = cellID;
    }
    
    /**
     * Get the ID of the cell this message is being sent to
     * @return the cellID
     */
    public CellID getCellID() {
        return cellID;
    }
}