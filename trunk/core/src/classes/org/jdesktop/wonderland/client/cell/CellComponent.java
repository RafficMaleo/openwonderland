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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * CellComponents provide dynamic extensions to the Cell infrastructure. 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellComponent {
    protected Cell cell;
    protected CellStatus status = CellStatus.DISK;
    
    public CellComponent(Cell cell) {
        this.cell = cell;
    }

    /**
     * Returns the current state of the component
     *
     * @return The current state of the component
     */
//    public CellStatus getStatus() {
//        return this.status;
//    }
    
    /**
     * Set the status of the component. This method is overridden by subclasses
     * of cell component. However, if you wish to set the status of a cell
     * component, invoke setComponentStatus() instead.
     *
     * @param status The new status of the component
     */
    public void setStatus(CellStatus status) {
        this.status = status;
    }

    /**
     * Sets the component status. This method should be called by anyone who
     * wishes to set the component status. This method makes sure that all
     * intermediate statuses are set.
     *
     * @param status The new status of the component
     */
    final void setComponentStatus(CellStatus status) {
        int currentStatus = this.status.ordinal();
        int requiredStatus = status.ordinal();

        if (currentStatus == requiredStatus)
            return;

        int dir = (requiredStatus > currentStatus ? 1 : -1);

        while (currentStatus != requiredStatus) {
            currentStatus += dir;
            setStatus(CellStatus.values()[currentStatus]);
        }
    }

    /**
     * Sets the client state of the cell components.
     *
     * @param clientState The client state of the cell component
     */
    public void setClientState(CellComponentClientState clientState) {
        // Do nothing
    }

    /**
     * Return the class used to reference this component. Usually this will
     * return the class of the component, but in some cases, will return another
     * Class. This method uses the ComponentClassLookup annotation on the cell
     * component to determine this class; if not present just returns the Class
     * given.
     *
     * @param clazz The Class of the component
     * @return The Class used in the component lookup table
     */
    public static final Class getLookupClass(Class clazz) {
        ComponentLookupClass l = (ComponentLookupClass)clazz.getAnnotation(ComponentLookupClass.class);
        if (l != null) {
            return l.value();
        }
        return clazz;
    }
}
