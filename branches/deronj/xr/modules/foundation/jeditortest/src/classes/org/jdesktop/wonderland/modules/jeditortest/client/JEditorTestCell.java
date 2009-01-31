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
package org.jdesktop.wonderland.modules.jeditortest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
import org.jdesktop.wonderland.modules.jeditortest.common.JEditorTestCellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * Client cell for the jEditor test.
 *
 * @author deronj
 */

@ExperimentalAPI
public class JEditorTestCell extends App2DCell {
    
    /** The logger used by this class */
    private static final Logger logger = Logger.getLogger(JEditorTestCell.class.getName());
    
    /** The (singleton) window created by the JEditor test app */
    private JEditorTestWindow window;

    /** The cell client state message received from the server cell */
    private JEditorTestCellClientState clientState;
    
    /**
     * Create an instance of JEditorTestCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public JEditorTestCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    /** 
     * {@inheritDoc}
     */
    public AppType getAppType () {
	return new JEditorTestAppType();
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    public void setClientState (CellClientState state) {
	super.setClientState(state);
        clientState = (JEditorTestCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    public boolean setStatus(CellStatus status) {
        boolean ret = super.setStatus(status);

        switch (status) {

	    // The cell is now visible
            case ACTIVE:

		setApp(new JEditorTestApp(getAppType(), clientState.getPreferredWidth(), 
					  clientState.getPreferredHeight(),
					  clientState.getPixelScale()));

		// Associate the app with this cell (must be done before making it visible)
		app.setCell(this);

		// Get the window the app created
		window = ((JEditorTestApp)app).getWindow();

		// Make the app window visible
		((JEditorTestApp)app).setVisible(true);
		break;

	    // The cell is no longer visible
            case DISK:
		((JEditorTestApp)app).setVisible(false);
		window = null;
		break;
	}

        return ret;
    }
}
