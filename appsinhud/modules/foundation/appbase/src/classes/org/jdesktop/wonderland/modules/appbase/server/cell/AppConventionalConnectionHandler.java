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
package org.jdesktop.wonderland.modules.appbase.server.cell;

import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.modules.appbase.common.AppConventionalConnectionType;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellSetConnectionInfoMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
  * Server handler for the app base conventional connection.
 *
 * @author deronj
 */

@InternalAPI
public class AppConventionalConnectionHandler implements ClientConnectionHandler, Serializable {

    private static final Logger logger = Logger.getLogger(AppConventionalConnectionHandler.class.getName());
    
    protected static final ConnectionType CLIENT_TYPE = AppConventionalConnectionType.CLIENT_TYPE;
    
    public ConnectionType getConnectionType () {
        return CLIENT_TYPE;
    }

    public void registered (WonderlandClientSender sender) {
        // Ignore
    }
    
    public void clientConnected (WonderlandClientSender sender, WonderlandClientID clientID, 
                                 Properties properties) {
        // Nothing to do
    }

    public void clientDisconnected (WonderlandClientSender sender, WonderlandClientID clientID) {
        // Nothing to do
    }
    
    public void messageReceived (WonderlandClientSender sender, WonderlandClientID clientID, Message message)
    {
        if (!(message instanceof AppConventionalCellSetConnectionInfoMessage)) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(),
						  "Unexpected message type: " + message.getClass()));
            return;
        }

        AppConventionalCellSetConnectionInfoMessage msg =
            (AppConventionalCellSetConnectionInfoMessage) message;           
        CellID cellID = msg.getCellID();
        CellMO cell = CellManagerMO.getCell(cellID);
        if (cell == null) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(), "Cannot find cell " + cellID));
            return;
        }
        if (!(cell instanceof AppConventionalCellMO)) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(), "Cell " + cellID + 
                                                       " is not an AppConventionalCellMO."));
            return;
        }

        // The connection info must be non-null
        if (msg.getConnectionInfo() == null) {
            sender.send(clientID, new ErrorMessage(message.getMessageID(),
                                                   "Cannot set null connection info for cell " + cellID));
            return;
        }

        // Update server cell state
        AppConventionalCellMO appConvCell = (AppConventionalCellMO) cell;
        appConvCell.setConnectionInfo(msg.getConnectionInfo());

        // Reply success
        sender.send(clientID, new OKMessage(message.getMessageID()));
        System.err.println("*********** Sent OKMessage");

        // Now send this message to all clients to notify them of the change
        sender.send(message);
    }
  
    /**
     * Get the channel used for sending to all clients of this type
     * @return the channel to send to all clients
     */
    public static WonderlandClientSender getSender() {
        return WonderlandContext.getCommsManager().getSender(CLIENT_TYPE);
    }
}
