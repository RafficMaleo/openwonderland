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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * The initial message that a client must send to the Wonderland server
 * in order to specify a communications protocol to use.
 * @author jprovino
 */
@ExperimentalAPI
public class AvatarCellIDMessage extends Message {

    private CellID cellID;

    public AvatarCellIDMessage(CellID cellID) {
	this.cellID = cellID;
    }

    public void setCellID(CellID cellID) {
	this.cellID = cellID;
    }

    public CellID getCellID() {
	return cellID;
    }

}
