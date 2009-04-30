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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.input.Event;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;

import java.awt.Color;
import java.awt.Font;

/**
 * Set name tag
 *
 * @author jprovino
 */
public class AvatarNameEvent extends Event {

    private EventType eventType;
    private String username;
    private String usernameAlias;
    private Color foregroundColor;
    private Font font;

    public AvatarNameEvent(EventType eventType, String username, String usernameAlias) {
	this(eventType, username, usernameAlias, null, null);
    }

    public AvatarNameEvent(EventType eventType, String username, String usernameAlias, 
	    Color foregroundColor, Font font) {

	this.eventType = eventType;
	this.username = username;
	this.usernameAlias = usernameAlias;
	this.foregroundColor = foregroundColor;
	this.font = font;
    }

    public void setEventType(EventType eventType) {
	this.eventType = eventType;
    }

    public EventType getEventType() {
	return eventType;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsernameAlias(String usernameAlias) {
	this.usernameAlias = usernameAlias;
    }

    public String getUsernameAlias() {
	return usernameAlias;
    }

    public void setForegroundColor(Color foregroundColor) {
	this.foregroundColor = foregroundColor;
    }

    public Color getForegroundColor() {
	return foregroundColor;
    }

    public void setFont(Font font) {
	this.font = font;
    }

    public Font getFont() {
	return font;
    }

    @Override
    public Event clone(Event evt) {
        if (evt == null) {
            evt = new AvatarNameEvent(eventType, username, usernameAlias, 
		foregroundColor, font);
        } else {
            AvatarNameEvent e = (AvatarNameEvent) evt;

	    e.setEventType(eventType);
	    e.setUsername(username);
	    e.setUsernameAlias(usernameAlias);
	    e.setForegroundColor(foregroundColor);
	    e.setFont(font);
        }

        super.clone(evt);
        return evt;
    }

    public String toString() {
	return "AvatarNameEvent:  " + eventType + " " + username + " usernameAlias " 
	    + usernameAlias + " foregroundColor " + foregroundColor + " font " + font;
    }

}
