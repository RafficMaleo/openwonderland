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
package org.jdesktop.wonderland.modules.xremwin.client.registry;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.xremwin.common.cell.AppCellXrwServerState;

/**
 * A generic cell factory which launches a specific X11 App. Takes the name of
 * the X11 app and the command to launch the app.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class XAppCellFactory implements CellFactorySPI {

    private String appName = null;
    private String command = null;

    /**
     * Constructor, takes the display name of the app and the command to launch
     * the app, neither of which should be null. The app name should be unique
     * among all entries in the Cell Palette.
     *
     * @param appName The app name
     * @param command The command to launch the app
     */
    public XAppCellFactory(String appName, String command) {
        this.appName = appName;
        this.command = command;
    }

    /**
     * @inheritDoc()
     */
    public String[] getExtensions() {
        return new String[] {};
    }

    /**
     * @inheritDoc()
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        AppCellXrwServerState serverState = new AppCellXrwServerState();
        serverState.setAppName(appName);
        serverState.setCommand(command);
        serverState.setLaunchLocation("server");

        return (T) serverState;
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return appName;
    }

    /**
     * @inheritDoc()
     */
    public Image getPreviewImage() {
        // TODO
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XAppCellFactory other = (XAppCellFactory) obj;
        if ((this.appName == null) ? (other.appName != null) : !this.appName.equals(other.appName)) {
            return false;
        }
        if ((this.command == null) ? (other.command != null) : !this.command.equals(other.command)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.appName != null ? this.appName.hashCode() : 0);
        hash = 83 * hash + (this.command != null ? this.command.hashCode() : 0);
        return hash;
    }
}
