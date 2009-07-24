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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 *
 * @author jprovino
 */
public class VoiceChatDialOutMessage extends VoiceChatMessage {   
    
    String group;
    String softphoneCallID;
    PresenceInfo presenceInfo;
    ChatType chatType;
    String name;
    String phoneNumber;

    public VoiceChatDialOutMessage(String group, PresenceInfo presenceInfo,
	    ChatType chatType, String name, String phoneNumber) {

	super(group);

	this.presenceInfo = presenceInfo;
	this.chatType = chatType;
	this.softphoneCallID = softphoneCallID;
	this.name = name;
	this.phoneNumber = phoneNumber;	
    }

    public PresenceInfo getPresenceInfo() {
	return presenceInfo;
    }

    public ChatType getChatType() {
	return chatType;
    }

    public String getSoftphoneCallID() {
	return softphoneCallID;
    }

    public String getName() {
	return name;
    }

    public String getPhoneNumber() {
	return phoneNumber;
    }

}