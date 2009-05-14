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
package org.jdesktop.wonderland.modules.appbase.client.cell;

import com.jme.math.Vector2f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.view.View2DCellFactory;
import org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault.View2DCell;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;

/**
 * The generic 2D application superclass. Displays the windows of a single 2D application. 
 * It's only extra attribute is the
 * pixel scale for all app windows created in the cell. The pixel scale is a Vector2f. 
 * The x component specifies the size (in local cell coordinates) of the windows along 
 * the local cell X axis. The y component specifies the same along the local cell 
 * Y axis. The pixel scale is in the cell client data (which must be of type 
 * <code>App2DCellClientState</code>) sent by the server when it instantiates this cell.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class App2DCell extends Cell implements View2DDisplayer {

    /** A list of all App2DCells in this client */
    private static final ArrayList<App2DCell> appCells = new ArrayList<App2DCell>();

    /** The view factory to use to create views for this cell. */
    private View2DCellFactory view2DCellFactory;

    /** The number of world units per pixel in the cell local X and Y directions */
    // TODO: eliminate
    protected Vector2f pixelScale = new Vector2f();

    /** All app views displayed by this cell. */
    private LinkedList<View2DCell> views = new LinkedList<View2DCell>();

    /** The app displayed in this cell. */
    protected App2D app;

    /** 
     * Creates a new instance of App2DCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public App2DCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        synchronized (appCells) {
            appCells.add(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanup() {
        synchronized (appCells) {
            appCells.remove(this);
        }
        view2DCellFactory = null;
        pixelScale = null;
        views.clear();
        app = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch (rendererType) {
            case RENDERER_2D:
                // No 2D Renderer yet
                break;
            case RENDERER_JME:
                ret = getViewFactory().createCellRenderer(this);
                break;
        }

        return ret;
    }

    /**
     * Associate the app with this cell. May only be called one time.
     *
     * @param app The world cell containing the app.
     * @throws IllegalArgumentException If the app already is associated with a cell .
     * @throws IllegalStateException If the cell is already associated with an app.
     */
    public void setApp(App2D app) throws IllegalArgumentException, IllegalStateException {

        if (app == null) {
            throw new NullPointerException();
        }
        if (this.app != null) {
            throw new IllegalStateException("Cell already has an app");
        }

        this.app = app;
    }

    /**
     * Get the app associated with this cell.
     */
    public App2D getApp() {
        return app;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        pixelScale = ((App2DCellClientState) clientState).getPixelScale();
    }

    /**
     * Returns the pixel scale.
     */
    public Vector2f getPixelScale() {
        return pixelScale;
    }

    /** Returns the view cell factory */
    private View2DCellFactory getViewFactory () {
        View2DCellFactory vFactory = view2DCellFactory;
        if (vFactory == null) {
            vFactory = App2D.getView2DCellFactory();
        }

        if (vFactory == null) {
            throw new RuntimeException("App2D View2DCellFactory is not defined.");
        }

        return vFactory;
    }

    /** {@inheritDoc} */
    public synchronized View2D createView (Window2D window) {
        View2DCell view = (View2DCell) getViewFactory().createView(this, window);

        // This type of cell allows the app to fully control the visibility
        view.setVisibleUser(true);

        if (view != null) {
            views.add(view);
            window.addView(view);
        }

        return view;
    }

    /** {@inheritDoc} */
    public synchronized void destroyView (View2D view) {
        if (views.remove(view)) {
            Window2D window = view.getWindow();
            window.removeView(view);
            view.cleanup();
        }
    }

    /** {@inheritDoc} */
    public synchronized void destroyAllViews () {
        LinkedList<View2D> toRemoveList = (LinkedList<View2D>) views.clone();
        for (View2D view : toRemoveList) {
            Window2D window = view.getWindow();
            if (window != null) {
                window.removeView(view);
            }
            view.cleanup();
        }
        views.clear();
        toRemoveList.clear();
    }

    /** {@inheritDoc} */
    public synchronized Iterator<? extends View2D> getViews () {
        return views.iterator();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str = "App2DCell for cellID=" + getCellID();
        str += ",app=";
        if (app == null) {
            str += "UNKNOWN APP";
        } else {
            str += app.getName();
        }
        return str;
    }

    // TODO: getter
    public void setViewFactory (View2DCellFactory vFactory) {
        view2DCellFactory = vFactory;
    }

    /**
     * Log this cell's scene graph.
     * <br>
     * FOR DEBUG. INTERNAL ONLY.
     */
    @InternalAPI
    public void logSceneGraph(RendererType rendererType) {
        switch (rendererType) {
            case RENDERER_JME:
                ((App2DCellRenderer) getCellRenderer(rendererType)).logSceneGraph();
                break;
            default:
                throw new RuntimeException("Unsupported cell renderer type: " + rendererType);
        }
    }
}
