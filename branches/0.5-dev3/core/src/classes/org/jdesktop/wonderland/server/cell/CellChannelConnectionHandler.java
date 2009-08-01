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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
import java.util.Properties;
import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handles CellMessages sent by the Wonderland client
 * @author jkaplan
 */
class CellChannelConnectionHandler implements ClientConnectionHandler, Serializable {

    public ConnectionType getConnectionType() {
        return CellChannelConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }

    public void clientConnected(WonderlandClientSender sender,
            WonderlandClientID clientID, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,
            WonderlandClientID clientID) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID,
            
            Message message) {
        if (message instanceof CellMessage) {
            messageReceived(sender, clientID, (CellMessage) message);
        } else {
            Message error = new ErrorMessage(message.getMessageID(),
                    "Unexpected message type: " + message.getClass());

            sender.send(clientID, error);
        }
    }

    /**
     * When a cell message is received, dispatch it to the appropriate cell.
     * If the cell does not exist, send back an error message.
     * @param channel the channel to send back to the cell client
     * @param sessionId the id of the session that generated the message
     * @param message the cell message
    
     */
    public void messageReceived(WonderlandClientSender sender,
            WonderlandClientID clientID,
            CellMessage message) {
        
        // find the appropriate cell
        CellMO cell = CellManagerMO.getCell(message.getCellID());

        // if there was no cell, handle the error
        if (cell == null) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(),
                    "Unknown cell id: " + message.getCellID()));
            return;
        }
        ChannelComponentMO channelComponent = cell.getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(),
                    "Cell does not have a ChannelComponent id: " + message.getCellID()));
            return;

        }

        // dispatch the message
        channelComponent.messageReceived(sender, clientID, message);
    }
}