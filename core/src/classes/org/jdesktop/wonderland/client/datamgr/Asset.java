/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.datamgr;

import java.io.File;
import java.util.ArrayList;
import org.jdesktop.wonderland.client.datamgr.AssetManager.AssetReadyListener;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.Checksum;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An asset in the system. An asset is uniquely identified by an AssetURI, which
 * describes where the asset comes from. See the AssetURI Javadoc for more
 * details of types of asset URI's.
 * <p>
 * Each asset has a type: typically, either file, image, or model and given by
 * the AssetType enumeration.
 * <p>
 * The url gives the full URL from which the asset was downloaded
 * <p>
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public abstract class Asset<T> {
    protected AssetType type=null;
    protected AssetURI assetURI=null;
    protected String url=null;
    protected File localCacheFile=null;
    protected Checksum localChecksum=null;
    
    protected ArrayList<AssetReadyListener> listeners = null;
    
    protected String failureInfo = null;
    
    /**
     * Constructor that takes the unique URI as an argument.
     * 
     * @param assetURI The unique identifying asset URI.
     */
    public Asset(AssetURI assetURI) {
        this.assetURI = assetURI;
    }

    /**
     * Returns the asset type, typically either a file, image, or model.
     * 
     * @return The type of asset
     */
    public AssetType getType() {
        return type;
    }

    /**
     * Returns the unique URI describing the asset.
     * 
     * @return The unique URI describing the asset
     */
    public AssetURI getAssetURI() {
        return this.assetURI;
    }
    
    /**
     * Returns the URL from which the asset was downloaded
     * 
     * @return The absolute URL from which the asset was downloaded
     */
    public String getURL() {
        return this.url;
    }
    
    /**
     * Sets the URL from which the asset was downloaded
     * 
     * @param url The absolute URL from which the asset was downloaded
     */
    public void setURL(String url) {
        this.url = url;
    }
    
    /**
     * Return the file containing the local cache of the asset
     * 
     * @return
     */
    public File getLocalCacheFile() {
        return localCacheFile;
    }

    void setLocalCacheFile(File localCacheFile) {
        this.localCacheFile = localCacheFile;
    }

    /**
     * Get the checksum of this file in the local cache.
     * @return
     */
    public Checksum getLocalChecksum() {
        return localChecksum;
    }

    void setLocalChecksum(Checksum checksum) {
        this.localChecksum = checksum;
    }
    
    /**
     * Called whenever the asset has been downloaded from the server
     */
    abstract void postProcess();

    /**
     * Load and return an asset from the local cache. Multiple instances
     * of the same asset can be shared this call will implement the necessary
     * sharing.
     * 
     * Returns true if load was succesful, otherwise returns false.
     */
    abstract boolean loadLocal();
    
    /**
     * Asset has been unloaded, cleanup.
     */
    abstract void unloaded();
    
    /**
     * Return the asset
     * @return
     */
    public abstract T getAsset();
    

    /**
     * Notify listeners waiting for asset to be downloaded, if failureInfo
     * is set will call assetFailure, otherwise it will call assetReady
     * @param asset
     */
    void notifyAssetReadyListeners() {
        if (listeners==null)
            return;
        
        synchronized(listeners) {
            if (failureInfo==null) {
                for(AssetReadyListener listener : listeners)
                    listener.assetReady(this);
            } else {
                for(AssetReadyListener listener : listeners)
                    listener.assetFailure(this, failureInfo);
            }
        }
    }
    
    public void addAssetReadyListener(AssetReadyListener listener) {
        if (listeners==null)
            listeners = new ArrayList();
        synchronized(listeners) {
            listeners.add(listener);
            if (localCacheFile!=null)
                listener.assetReady(this);
        }
    }
    
    public void removeAssetReadyListener(AssetReadyListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    public String getFailureInfo() {
        return failureInfo;
    }

    public void setFailureInfo(String failureInfo) {
        this.failureInfo = failureInfo;
    }  
}
