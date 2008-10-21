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

package org.jdesktop.wonderland.web.asset.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;
import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer.DeployedAsset;

/**
 * The ModuleAssetResource class is a Jersey RESTful service that returns some
 * art that is contained within the module system. The getModuleArt() method
 * handles the HTTP GET request.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/{modulename}/asset/get/{path}", limited=false)
public class ModuleAssetResource {
    
    /**
     * Returns a piece of artwork that is stored within the module system,
     * given the name of the module and the relative path of the art resource
     * encoded into the URI. The format of the URI is:
     * <p>
     * /module/{modulename}/art/{path}
     * <p>
     * where {modulename} is the name of the module and {path} is the relative
     * path of the art resource. All spaces in the module name must be encoded
     * to %20. Returns BAD_REQUEST to the HTTP connection if the module name is
     * invalid or if there was an error encoding the module's information.
     * 
     * @param moduleName The unique name of the module
     * @return An XML encoding of the module's basic information
     */
    @GET
    public Response getModuleAsset(@PathParam("modulename") String moduleName, @PathParam("path") String path) {
        Logger logger = Logger.getLogger(ModuleAssetResource.class.getName());
        logger.warning("[ART] In module " + moduleName + " getting " + path);
        
        /*
         * If the path has a leading slash, then remove it (this is typically
         * the case with @PathParam).
         */
        if (path.startsWith("/") == true) {
            path = path.substring(1);
        }
        
        /*
         * Get a map of all of the File objects for each art asset. We use the
         * convention that the first element of the 'path' is the asset type
         * (e.g. art, client, etc). Look for the entry that matches.
         */
        Map<DeployedAsset, File> assetMap = AssetDeployer.getFileMap();
        Iterator<DeployedAsset> it = assetMap.keySet().iterator();
        DeployedAsset asset = null;
        File root = null;
        while (it.hasNext() == true) {
            asset = it.next();
            if (asset.moduleName.equals(moduleName) == true && path.startsWith(asset.assetType) == true) {
                root = assetMap.get(asset);
                break;
            }
        }
        
        /* Ask the deployer for the directory of the art */
        if (root == null) {
            /* Log an error and return an error response */
            logger.warning("[ART] Unable to locate module " + moduleName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        logger.warning("[ART] Found asset " + path + " in " + root.getAbsolutePath());
        
        /*
         * Strip off the asset type from the path before we look it up
         */
        String prefix = asset.assetType + "/";
        path = path.substring(prefix.length());
        
        File file = new File(root, path);
        if (file.exists() == false || file.isDirectory() == true) {
            /* Write an error to the log and return */
            logger.warning("[ART] Unable to locate resource " + path +
                    " in module " + moduleName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Encode in an HTTP response and send */
        try {
            InputStream is = new FileInputStream(file);
            ResponseBuilder rb = Response.ok(is);
            return rb.build();
        } catch (Exception excp) {
            logger.log(Level.WARNING, "[ART] Unable to locate resource", excp);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();            
        }
    }
}
