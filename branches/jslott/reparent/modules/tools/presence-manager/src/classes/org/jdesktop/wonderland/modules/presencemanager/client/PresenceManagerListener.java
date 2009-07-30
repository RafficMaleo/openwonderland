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
package org.jdesktop.wonderland.modules.presencemanager.client;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

public interface PresenceManagerListener {

    public enum ChangeType {
	USER_ADDED,
	USER_REMOVED,
	SPEAKING_CHANGED,
	MUTE_CHANGED,
	ENTER_EXIT_CONE_OF_SILENCE,
	IN_SECRET_CHAT
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type);

    public void usernameAliasChanged(PresenceInfo presenceInfo);

}