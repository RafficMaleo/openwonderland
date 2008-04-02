/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.client;

import org.jdesktop.wonderland.server.*;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Client plugins can implement this interface to add new functionality to
 * the Wonderland client.  The initialize method of the service will be called
 * during the intialization of the client Main.  It is guaranteed to be called
 * after theclient context has been initialized, so plugins can use
 * the context to get access to server resources.
 * 
 * @author kaplanj
 */
@ExperimentalAPI
public interface ClientPlugin {
    /** intialize this plugin */
    public void initialize();
}