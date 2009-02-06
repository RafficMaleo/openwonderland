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

package org.jdesktop.wonderland.server.eventrecorder;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;



/**
 * Reponsible for reording "events" in Wonderland. (I.e. not recording audio or video)
 * @author Bernard Horan
 */
public interface EventRecorder {



    /**
     * Record the message from the sender
     * @param sender the sender of the message
     * @param clientID the id of the client sending the message
     * @param message
     */
    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message);

    /**
     * Indicate if this event recorder is recording.
     * For purposes of optimisation
     * @return
     */
    public boolean isRecording();

    /**
     * Get the unique name for this event recorder
     * @return 
     */
    public String getName();


}
