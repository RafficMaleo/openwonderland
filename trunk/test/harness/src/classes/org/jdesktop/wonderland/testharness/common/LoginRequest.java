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
package org.jdesktop.wonderland.testharness.common;

import java.util.Properties;

/**
 *
 * @author paulby
 */
public class LoginRequest extends TestRequest {
    private String processorName;
    private Properties props;
    
    public LoginRequest(String processorName,
                        Properties props,
                        String username)
    {
        super(username);

        this.processorName = processorName;
        this.props = props;
    }

    public String getProcessorName() {
        return processorName;
    }

    public Properties getProps() {
        return props;
    }
}
