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
package org.jdesktop.wonderland.modules.sas.provider;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Listener interface for cell cache action messages
 */
@ExperimentalAPI
public interface SasProviderConnectionListener {

    /**
     * Attach this listener to the given session.
     * TODO: params
     */
    public void setSession (SasProviderSession session);

    /**
     * Launch the specified app.
     * TODO: params
     */
    public String launch (String appName, String command, Vector2f pixelScale);
}
