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
package org.jdesktop.wonderland.serverlistenertest.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author jkaplan
 */
public class TestMessageOne extends Message {
    private String text;
    
    public TestMessageOne(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    public String toString() {
        return "Message " + getMessageID() + ": TestMessageOne : " + 
               getText();
    }
}