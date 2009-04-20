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
package org.jdesktop.wonderland.modules.phone.client.cell;

import org.jdesktop.wonderland.modules.phone.common.CallListing;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.messages.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellRenderer;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.modules.phone.common.PhoneCellClientState;
import org.jdesktop.wonderland.modules.phone.common.PhoneInfo;

import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author jkaplan
 */
public class PhoneCell extends Cell {

    private static final Logger logger =
            Logger.getLogger(PhoneCell.class.getName());
    private PhoneForm phoneForm;
    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    private CallListing mostRecentCallListing;
    //private boolean projectorState;
    //private ProjectorStateUpdater projectorStateUpdater;
    private PhoneInfo phoneInfo;
    private PhoneMessageHandler phoneMessageHandler;

    public PhoneCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        logger.fine("CREATED NEW PHONE CELL " + cellID);
    }

    public boolean setStatus(CellStatus status) {
        boolean changed = super.setStatus(status);

        if (!changed) {
            return changed;
        }

        switch (status) {
        case ACTIVE:
	    if (phoneMessageHandler == null) {
                phoneMessageHandler = new PhoneMessageHandler(this);
	    }
            break;
        case DISK:
            phoneMessageHandler.done();
            phoneMessageHandler = null;
            break;
        }

        return changed;
    }

    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param setupData
     */
    @Override
    public void setClientState(CellClientState cellClientState) {
        super.setClientState(cellClientState);

        PhoneCellClientState phoneCellClientState = (PhoneCellClientState) cellClientState;

        phoneInfo = phoneCellClientState.getPhoneInfo();
    }

    public PhoneInfo getPhoneInfo() {
        return phoneInfo;
    }

    public WonderlandSession getSession() {
        return getCellCache().getSession();
    }

    public void phoneSelected() {
        if (phoneMessageHandler == null) {
            logger.warning("No phoneMessageHandler");
            return;
        }

        phoneMessageHandler.phoneSelected();
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new PhoneCellRenderer(this);
        }

        return super.createCellRenderer(rendererType);
    }
}
