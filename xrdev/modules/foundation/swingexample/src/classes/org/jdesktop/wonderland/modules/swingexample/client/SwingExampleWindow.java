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
package org.jdesktop.wonderland.modules.swingexample.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.utils.graphics.GraphicsUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.swingexample.client.cell.SwingExampleCell;
import com.jme.math.Vector3f;

/**
 *
 * The window for the Swing example.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SwingExampleWindow extends WindowSwing {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(SwingExampleWindow.class.getName());

    /** The cell in which this window is displayed. */
    private SwingExampleCell cell;

    /**
     * Create a new instance of SwingExampleWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public SwingExampleWindow (SwingExampleCell cell, App2D app, int width, int height, boolean topLevel, 
                            Vector2f pixelScale)
        throws InstantiationException
    {
	super(app, width, height, topLevel, pixelScale);
        this.cell = cell;

	setTitle("Swing Example");

	TestPanel examplePanel = new TestPanel();
	// Note: this seems to only be required for the swing set, but do it here for safety
	// TODO: example without
       	JmeClientMain.getFrame().getCanvas3DPanel().add(examplePanel);

	setComponent(examplePanel);
    }
}
