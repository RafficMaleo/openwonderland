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
package org.jdesktop.wonderland.modules.appbase.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A user input control arbiter which allows only one user to have control at a time. This policy is
 * known as <italic>baton passing</italic>.
 *
 * @author deronj
 */
@ExperimentalAPI
abstract public class ControlArbSingle extends ControlArbAppFocus {

    /** The user name of the one and only controller */
    protected String controller;

    /** {@inheritDoc} */
    public void cleanup () {
        controller = null;
    }

    /**
     * Specifies the current controlling user. Typically called by a subclass.
     */
    public synchronized void setController(String controller) {
        String oldController = this.controller;
        this.controller = controller;
        if ((controller == null && oldController != null) || 
            !controller.equals(oldController)) {
            updateControl();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getControllers() {
        String[] ary = new String[1];
        ary[0] = controller;
        return ary;
    }

    /**
     * Returns the user that is currently in control.
     * (null if there currently isn't a controller).
     */
    public String getController() {
        return controller;
    }
}
