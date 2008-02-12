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
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * Manages the entire set of users logged into the system.
 *
 * In the future this should provide a spacial hierarchy of users
 * avatars so we can search for users who are close to each other.
 *
 * @author paulby
 */
@ExperimentalAPI
public class UserManager implements ManagedObject, Serializable {
    
    private HashMap<ClientSessionId, ManagedReference> uidToUserRef =
	    new HashMap<ClientSessionId,ManagedReference>();
    
    /**
     * Name used in binding this object in DataManager
     **/
    private static final String BINDING_NAME="USER_MANAGER";
    
    private int userLimit = Integer.MAX_VALUE;

    /**
     * Creates a new instance of UserManager
     */
    private UserManager() {
    }
    
    public static void initialize() {
        UserManager mgr = new UserManager();
        AppContext.getDataManager().setBinding(BINDING_NAME, mgr);
    }
    
    /**
     * Return singleton user manager
     * @return the user manager
     */
    public static UserManager getUserManager() {
        return AppContext.getDataManager().getBinding(UserManager.BINDING_NAME, UserManager.class);                
    }

    /**
     * Add a user to the set of logged in users
     */
    public void addUser(ClientSessionId userID, UserMO user) {
        DataManager dm = AppContext.getDataManager();
        uidToUserRef.put(userID, dm.createReference(user));
    }
    
    /**
     * Remove the user from the set of logged in users.
     *
     * @return reference to the UserGLO
     */
    public UserMO removeUser(ClientSessionId userID) {
        ManagedReference userRef = uidToUserRef.remove(userID);
        if (userRef == null) {
            return null;
        }
        
        return userRef.get(UserMO.class);
    }
    
    /**
     * Return the user with the given userID
     *
     * @return reference to the UserGLO
     */
    public UserMO getUser(ClientSessionId userID) {
        ManagedReference userRef = uidToUserRef.get(userID);
        if (userRef == null) {
            return null;
        }
        
        return userRef.get(UserMO.class);
    }
    
    /**
     * Return the UserMO object associated with the unique userName
     *
     * @return UserMO object for username, or null if no such user
     */
    public static UserMO getUserMO(String username) {
        String userObjName = "user_"+username;
        UserMO user=null;
        
        DataManager dataMgr = AppContext.getDataManager();
        try {
            user = dataMgr.getBinding(userObjName, UserMO.class);
        } catch(NameNotBoundException ex) {
            user = null;
        }
        
        return user;
    }
    
    /**
     * Find or create and return the user managed object 
     */
//    public ManagedReference getOrCreateUserMO(String username) {
//        
//        String userObjName = "user_"+username;
//        UserMO user=null;
//        
//        user = getUserMO(username);
//        if (user==null) {
//            user = new UserMO(username);
//            AppContext.getDataManager().setBinding(userObjName, user);
//        }
//        
//        ManagedReference userRef = AppContext.getDataManager().createReference(user);
//                
//        return userRef;
//    }
    
    private UserMO createUserMO(String username) {
        UserMO ret = new UserMO(username);
        AppContext.getDataManager().setBinding("user_"+username, ret);
        return ret;
    }

    /**
     * Returns true if the user with the specified userName is currently logged in, false otherwise.
     */
    public boolean isLoggedIn(String userName) {
        UserMO user = getUserMO(userName);
        if (user==null) {
            return false;
        }
        
        return user.isLoggedIn();
    }
    
    /**
      * Return a Collection of all users currently logged in
     *
     * @return collection of ManagedReferences to UserGLO's
     */
    public Collection<ManagedReference> getAllUsers() {
        return uidToUserRef.values();
    }
    
    /**
     * Log the user in from the specificed session. 
     * @param session 
     */
    public void login(ClientSession session) {
        UserMO user = getUserMO(session.getName());
        if (user==null) {
            user = createUserMO(session.getName());
        }
        user.login(session);
        uidToUserRef.put(session.getSessionId(), user.getReference());
    }
    
    /**
     * Log user out of specified session
     * @param session
     */
    public void logout(ClientSession session) {
        UserMO user = getUserMO(session.getName());
        assert(user!=null);
        
        user.logout(session);
        uidToUserRef.remove(session.getSessionId());
   }
    
    /**
     * Return a Collection of all avatars for currently logged in users
     *
     * @return Collection of ManagedReferences to AvatarCellGLO's
     */
//    public Collection<ManagedReference> getAllAvatars() {
//        return uidToAvatarRef.values();
//    }
    
    /**
     *  Return total number of users currently logged in
     **/
    public int getUserCount() {
        return uidToUserRef.size();
    }
    
    /**
     *  Get the maximum number of users allowed on the server
     */
    public int getUserLimit() {
        return userLimit;
    }

    /**
     *  Set the maximum number of users allowed on the server
     */
    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }
    
}
