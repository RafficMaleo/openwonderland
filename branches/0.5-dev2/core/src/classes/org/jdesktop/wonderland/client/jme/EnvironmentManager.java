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
package org.jdesktop.wonderland.client.jme;

import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class EnvironmentManager {
    
    private static EnvironmentManager environmentManager=null;
    private HashMap<ServerSessionManager,HashMap<String, Environment>> environments = new HashMap();

    private EnvironmentManager() {
        
    }
    
    public static EnvironmentManager getEnvironmentManager() {
        if (environmentManager==null)
            environmentManager = new EnvironmentManager();
        return environmentManager;
    }

    /**
     * Register an environment
     * @param name
     * @param environment
     */
    public void addEnvironment(ServerSessionManager loginMgr, String name, Environment environment) {
        HashMap<String, Environment> env = environments.get(loginMgr);
        if (env==null) {
            env = new HashMap();
            environments.put(loginMgr, env);
        }
        env.put(name, environment);
    }
    
    /**
     * Remove the specified environment
     * @param loginMgr
     * @param name
     */
    public void removeEnvironment(ServerSessionManager loginMgr, String name) {
        Logger.getAnonymousLogger().warning("TODO - implement EnvironmentManager.removeEnvironment");
    }

    /**
     * Set the current Environment used by default
     * @param name
     */
    public void setCurrentEnvironment(ServerSessionManager loginMgr, String name) {
        HashMap<String, Environment> env = environments.get(loginMgr);
        if (env==null)
            throw new RuntimeException("No such Environment for session");
        Environment e = env.get(name);
        if (e==null)
            throw new RuntimeException("No such Environment for session");
        e.setGlobalLights();
        e.setSkybox();
    }
}