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
package org.jdesktop.wonderland.server.cell;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSessionId;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.AvatarBoundsHelper;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.WonderlandChannelNames;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.comms.ClientType;
import org.jdesktop.wonderland.server.CellAccessControl;
import org.jdesktop.wonderland.server.UserPerformanceMonitor;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.bounds.BoundsManager;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.CommsManagerFactory;

/**
 * Container for the cell cache for an avatar.
 *
 * Calculates the set of cells that the client needs to load and sends the
 * information to the client.
 *
 * This is a nieve implementation that does not contain View Frustum culling,
 * culling is performed only on relationship to users position.
 *
 * @author paulby
 */
public class AvatarCellCacheMO implements ManagedObject, Serializable {
    
    private final static Logger logger = Logger.getLogger(AvatarCellCacheMO.class.getName());
    
//    private Channel cacheChannel;
//    private CellRef rootCellRef;
    
    private ManagedReference avatarRef;
    private String username;
    private ClientSessionId userID;
    private CellID rootCellID;
    private ClientType clientType;
    
    private BoundingSphere proximityBounds = AvatarBoundsHelper.getProximityBounds(new Vector3f());
    
    /**
     * List of currently visible cells (ManagedReference of CellGLO)
     */
    private Map<CellID, CellRef> currentCells = new HashMap<CellID, CellRef>();
    
    /**
     * Creates a new instance of AvatarCellCacheMO
     */
    public AvatarCellCacheMO(ManagedReference avatarRef) {
        this.avatarRef = avatarRef;
        DataManager dataMgr = AppContext.getDataManager();
        logger.config("Creating AvatarCellCache");
        username = avatarRef.get(AvatarMO.class).getUser().getUsername()+"_"+avatarRef.get(AvatarMO.class).getCellID().toString();
        ChannelManager chanMgr = AppContext.getChannelManager();
        
        dataMgr.setBinding(username+"_CELL_CACHE", this);
        rootCellID = WonderlandContext.getMasterCellCache().getRootCellID();
        
        AvatarMO avatar = avatarRef.get(AvatarMO.class);
        
//        cacheChannel = chanMgr.createChannel(WonderlandChannelNames.AVATAR_CACHE_PREFIX+"_"+username+"_"+avatar.getCellID(), null, Delivery.RELIABLE);
    }
    
    /**
     * Notify CellCache that user has logged out
     */
    void logout(ClientSessionId userID) {
        currentCells.clear();
//        MasterCellCacheGLO master = MasterCellCacheGLO.getMasterCellCache();
//        AppContext.getDataManager().markForUpdate(master);
//        master.removeUserCellCache(this);
    }
    
    /**
     * Notify CellCache that user has logged in
     */
    void login(ClientSessionId userID, ClientType clientType) {
        this.userID = userID;
        this.clientType = clientType;
//        MasterCellCacheGLO master = MasterCellCacheGLO.getMasterCellCache();
//        AppContext.getDataManager().markForUpdate(master);
//        master.addUserCellCache(this);
        
        CommsManager cacheChannel = CommsManagerFactory.getCommsManager();
        
        // Setup Root Cell
        CellHierarchyMessage msg;
        CellMO rootCell = MasterCellCache.getCell(rootCellID);
        msg = MasterCellCache.newCreateCellMessage(rootCell);
        cacheChannel.send(clientType, msg);
        msg = MasterCellCache.newRootCellMessage(rootCell);
        cacheChannel.send(clientType, userID.getClientSession(), msg);
        currentCells.put(rootCellID, new CellRef(rootCell));
        
        // revalidate to discover initial cells
        revalidate();
    }
    
    /**
     * Called when the avatar cell is moved, informs the client what cells need
     * to be loaded and what can be unloaded
     */
//    public void avatarCellMoved(Point3f location, Vector3f velocity) {
//        proximityBounds = AvatarBoundsHelper.getProximityBounds(location);
//        
//        DataManager dataMgr = AppContext.getDataManager();
//        MasterCellCacheGLO master = MasterCellCacheGLO.getMasterCellCache();
//        dataMgr.markForUpdate(master);
//        master.revalidate();       
//    }
    
    /**
     * Revalidate cell cache. This first finds the new list of visible cells
     * and if the cell does not exist in the current list of visible cells, then
     * creates the cell on the client. If the visible cell exists, check to see
     * if it has been modified, and send the appropriate message to the client.
     * Finally, remove all of the cells which are no longer visibe.
     */
    void revalidate() {
        // make sure the user is still logged on
        if (userID == null || userID.getClientSession() == null) {
            return;
        }
        
//        logger.warning("Revalidating CellCache for "+userName+"  "+userID.getClientSession().isConnected()+"  "+proximityBounds);

        UserPerformanceMonitor monitor = new UserPerformanceMonitor();
        BoundsManager boundsMgr = AppContext.getManager(BoundsManager.class);
        Collection<CellID> visCells = boundsMgr.getVisibleCells(rootCellID, proximityBounds, monitor);
        
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(monitor.getRevalidateStats());
        }
        
        // copy the existing cells into the list of known cells 
        List<CellRef> oldCells = new ArrayList(currentCells.values());
        
        CellHierarchyMessage msg;
        AvatarMO user = avatarRef.get(AvatarMO.class);
        CommsManager cacheChannel = CommsManagerFactory.getCommsManager();
         
        // TODO now visCells is a list of cellID's refactor so we don't have to
        // get the cells from sgs if they are already visible. The main issue
        // here is handling the cell version check
        
        /*
         * Loop through all of the visible cells and:
         * 1. If not already present, create it
         * 2. If already present, check to see if has been modified
         * These two steps only happen if we are given access to view the cell
         */
        for(CellID cellID : visCells) {
            /* Fetech the cell GLO class associated with the visible cell */
            CellMO cell    = MasterCellCache.getCell(cellID);
            
            // check this client's access to the cell
            if (!CellAccessControl.canView(user, cell)) {
                // the user doesn't have access to this cell -- just skip
                // it and go on                
                continue;
            }

            // find the cell in our current list of cells
            CellRef cellRef = currentCells.get(cellID);  
            
            if (cellRef == null) {
                // the cell is new -- add it and send a message
                cellRef = new CellRef(cell);
                currentCells.put(cellID, cellRef);
                    
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Entering cell " + cell +
                                 "   cellcache for user " + username);
                    
                msg = MasterCellCache.newCreateCellMessage(cell);
                cacheChannel.send(clientType, msg);
                AppContext.getDataManager().markForUpdate(cell);
                cell.addUserToCellChannel(userID);
            } else if (cell.getVersion() > cellRef.getVersion()) {
                /*
                 * The cell already exist, but has been modified, so we create
                 * a new cell modify message and send to the client. We need
                 * to clear the 'modify' bit in the cell
                 */
                msg = MasterCellCache.newContentUpdateCellMessage(cell);
                cacheChannel.send(clientType, msg);
                
                // update the cell reference in our list
                cellRef.setVersion(cell.getVersion());
                
                // t is still visible, so remove it from the oldCells set
                oldCells.remove(cellRef);
            } else {
                // t is still visible, so remove it from the oldCells set
                oldCells.remove(cellRef);
            }
        }
        
        try {
            // oldCells contains the set of cells to be removed from client memory
            for(CellRef ref : oldCells) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Leaving cell "+ref.getCellID()+"   cellcache for user "+username);
                
                // the cell may be inactive or removed.  Try to get the cell,
                // and catch the exception if it no longer exists.
                try {
                    CellMO cell = ref.getForUpdate();
                    
                    // get suceeded, so cell is just inactive
                    msg = MasterCellCache.newInactiveCellMessage(cell);
                    cell.removeUserFromCellChannel(userID);
                } catch (ObjectNotFoundException onfe) {
                    // get failed, cell is deleted
                    msg = MasterCellCache.newDeleteCellMessage(ref.getCellID());
                }
                
                cacheChannel.send(clientType, msg);
                
                // the cell is no longer visible on this client, so remove
                // our current reference to it.  This client will no longer
                // receive any updates about the given cell, including future
                // deletes.  This implies that the client must clear out
                // its cache of inactive cells periodically, as some of them
                // may have been deleted.
                // TODO periodically clean out client cell cache
                currentCells.remove(ref.getCellID());
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Cache Revalidation failed "+e.getMessage(), e);
        }
    }
    
    /**
     * Notify client that this cell has moved
     */
    void cellMoved(MoveableCellMO cell) {
        CellHierarchyMessage msg = MasterCellCache.newCellMoveMessage(cell);
        CommsManager cacheChannel = CommsManagerFactory.getCommsManager();
        cacheChannel.send(clientType, msg);
    }
    
    /**
     * The Users avatar has either entered or exited the cell
     */
    void avatarEnteredExitedCell(boolean enter, CellID cellID) {
        
    }
    
    /**
     * Write the non serializable data
     */
//    private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
//        SerializationHelper.writeBoundsObject(proximityBounds, out);
//    }
//    
//    /**
//     * Read the non serializable data
//     */
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
//        proximityBounds = (BoundingSphere)SerializationHelper.readBoundsObject(in);
//    }
   

    
    /**
     * Information about a cell in our cache.  This object contains our record
     * of the given cell, including a reference to the cell and the cell's
     * id.  CellRef object are compared by cellID, so two CellRefs with the
     * same id are considered equal, regardless of other information.
     */
    private static class CellRef implements Serializable {
        private static final long serialVersionUID = 1L;
        
        // the cell id of the cell we reference
        private CellID id;
        
        // a reference to the cell itself
        private ManagedReference cellRef;
        
        // the last version of the cell we sent to clients
        private long version;
        
        public CellRef(CellID id) {
            this (id, null);
        }
        
        public CellRef(CellMO cell) {
            this (cell.getCellID(), cell);
        }
        
        private CellRef(CellID id, CellMO cell) {
            this.id = id;
            
            // create a reference to the given CellGLO
            if (cell != null) {
                DataManager dm = AppContext.getDataManager();
                cellRef = dm.createReference(cell);
         
//                this.version = cell.getVersion();
            }
        }
        
        public CellID getCellID() {
            return id;
        }
        
        public CellMO get() {
            return cellRef.get(CellMO.class);
        }
        
        public CellMO getForUpdate() {
            return cellRef.getForUpdate(CellMO.class);
        }
        
        public long getVersion() {
            return version;
        }
        
        public void setVersion(long version) {
            this.version = version;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CellRef)) {
                return false;
            }
            
            return id.equals(((CellRef) o).id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

