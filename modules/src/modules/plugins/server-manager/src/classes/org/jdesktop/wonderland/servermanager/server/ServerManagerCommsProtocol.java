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
package org.jdesktop.wonderland.servermanager.server;

import org.jdesktop.wonderland.server.comms.*;
import org.jdesktop.wonderland.common.comms.ProtocolVersion;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import org.jdesktop.wonderland.common.comms.ServerManagerProtocolVersion;
import org.jdesktop.wonderland.servermanager.server.ServerManagerSessionListener;

/**
 * The default communications protocol used by the Wonderland client.
 * @author jkaplan
 */
public class ServerManagerCommsProtocol implements CommunicationsProtocol {
    /**
     * Get the name of this protocol
     * @return "wonderland_client"
     */
    public String getName() {
        return ServerManagerProtocolVersion.PROTOCOL_NAME;
    }

    /**
     * Get the version of this protocol
     * @return the protocol version
     */
    public ProtocolVersion getVersion() {
        return ServerManagerProtocolVersion.VERSION;
    }

    public ClientSessionListener createSessionListener(ClientSession session, 
                                                       ProtocolVersion version) 
    {
        return new ServerManagerSessionListener(session);
    }
}
