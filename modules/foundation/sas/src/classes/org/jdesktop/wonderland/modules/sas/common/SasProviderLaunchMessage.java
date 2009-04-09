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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sas.common;

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * A message which SAS server uses to ask a SAS provider to launch an app.
 * 
 * @author deronj
 */

@InternalAPI
public class SasProviderLaunchMessage extends Message {
    
    /** The execution capability. */
    private String executionCapability;

    /** The app name. */
    private String appName;

    /* The execution command. */
    private String command;

    /** The default constructor */
    public SasProviderLaunchMessage () {}

    /**
     * Create a new instance of SasProviderLaunchMessage
     * @param executionCapability The execution capability.
     * @param appName The name of the app instance.
     * @param command The command to execute.
     */
    public SasProviderLaunchMessage (String executionCapability, String appName, String command) {
        this.executionCapability = executionCapability;
        this.appName = appName;
        this.command = command;
    }

    public void setExecutionCapability (String executionCapability) {
        this.executionCapability = executionCapability;
    }

    public String getExecutionCapability () {
        return executionCapability;
    }

    public void setAppName (String appName) {
        this.appName = appName;
    }

    public String getAppName () {
        return appName;
    }

    public void setCommand (String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
