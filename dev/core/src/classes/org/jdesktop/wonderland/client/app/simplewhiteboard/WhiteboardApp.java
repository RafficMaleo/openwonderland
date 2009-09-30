
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
package org.jdesktop.wonderland.client.simplewhiteboard;

import org.jdesktop.wonderland.client.app.base.App;
import org.jdesktop.wonderland.client.app.base.AppType;
import org.jdesktop.wonderland.client.app.base.AppGraphics2D;
import org.jdesktop.wonderland.client.app.base.ControlArbMulti;
import com.jme.math.Vector2f;

/**
 *
 * A 2D whiteboard application
 *
 * @author paulby,deronj
 */

@ExperimentalAPI
public class WhiteboardApp extends AppGraphics2D  {
    
    /** The single window created by this app */
    private WhiteboardWindow window;

    /**
     * Create a new instance of WhiteboardApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param appType The type of app (should be WhiteboardAppType).
     * @param width The width (in pixels) of the whiteboard window.
     * @param height The height (in pixels) of the whiteboard window.
     * @param pixelScale The horizontal and vertical pixel sizes
     * (in world meters per pixel).
     * @param commComponent The communications component for communicating with the server.
     */
    public WhiteboardApp (AppType appType, int width, int height, Vector2f pixelScale,
			  WhiteboardComponent commComponent) {

	// configWorld can be null because the server cell is already configured
	super(appType, new ControlArbMulti(), pixelScale);
	controlArb.setApp(this);

	// This app has only one window, so it is always top-level 
        try {
            window = new WhiteboardWindow(this, width, height, true, pixelScale, commComponent);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** 
     * Clean up resources.
     */
    public void cleanup () {
	super.cleanup();
	if (window != null) {
	    window.setVisible(false);
	    window.cleanup();
	    window = null;
	}
    }

    /**
     * Returns the app's window.
     */
    public WhiteboardWindow getWindow () {
	return window;
    }

    /**
     * Change the visibility of the app.
     *
     * @param visible Whether the application is visible.
     */
    public void setVisible (boolean visible) {
	window.setVisible(visible);
    }
}