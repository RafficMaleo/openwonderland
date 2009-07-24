/**
 * Project Looking Glass
 *
 * $RCSfile: CellStatus.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/04 23:11:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common.cell;

/**
 *
 * @author paulby
 */
public enum CellStatus {
    
    DISK,                   // Cell is on disk with no memory footprint
    BOUNDS,                 // Cell object and bounds are in memory
    INACTIVE,               // Cell geometry is in memory, but not being rendered
    ACTIVE,                 // Cell is 'close' to avatar
    VISIBLE                 // Cell is in view frustum
}