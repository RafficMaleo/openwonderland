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

package org.jdesktop.wonderland.modules;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.wfs.WFS;

/**
 * The Module class represents a single module within Wonderland. A module
 * consists of several possible subcomponents: artwork, WFSs, and plugins.
 * Artwork either includes textures, images, and 3D geometry. Modules may also
 * contain Wonderland Filesystems (WFSs) that assemble the artwork resources
 * into a subworld component. Plugins are runnable code that extend the
 * functionality of the server and/or client.
 * <p>
 * A module is stored within a jar/zip archive file. To open an existing module
 * archive file, use the ModuleFactory.open() method. Once open, users of this
 * class may query for the module's artwork, WFSs, and plugins.
 * <p>
 * Modules also have major.minor version numbers and a list of other modules
 * upon which this module depends.
 * <p>
 * This is an abstract class -- it is typically subclassed to handle whether
 * the module was loaded on disk or from an archive file.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class Module {

    /* Useful names of files within the archive */
    public static final String MODULE_INFO       = "module.xml";
    public static final String MODULE_REQUIRES   = "requires.xml";
    public static final String MODULE_REPOSITORY = "repository.xml";
    public static final String MODULE_ART        = "art/";
    
    private ModuleInfo       moduleInfo       = null; /* Basic module info   */
    private ModuleRequires   moduleRequires   = null; /* Module dependencies */
    private ModuleRepository moduleRepository = null; /* Module repository   */
    
    /* A map of unique artwork resource names to their resource objects */
    private HashMap<String, ModuleArtResource> moduleArtwork = null;
        
    /** Default constructor */
    protected Module() {}
    
    /**
     * Returns the basic information about a module: its name and version.
     * <p>
     * @return The basic module information
     */
    public ModuleInfo getModuleInfo() {
        return this.moduleInfo;
    }
    
    /**
     * Sets the basic information about a module, assumes the given argument
     * is not null.
     * <p>
     * @param moduleInfo The basic module information
     */
    public void setModuleInfo(ModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    
    /**
     * Returns the module's dependencies.
     * <p>
     * @return The module's dependencies
     */
    public ModuleRequires getModuleRequires() {
        return this.moduleRequires;
    }
    
    /**
     * Sets the module's dependencies, assumes the given argument is not null.
     * <p>
     * @param moduleRequires The module dependencies
     */
    public void setModuleRequires(ModuleRequires moduleRequires) {
        this.moduleRequires = moduleRequires;
    }
    
    /**
     * Returns the module's repository information.
     * <p>
     * @return The module's repository information
     */
    public ModuleRepository getModuleRepository() {
        return this.moduleRepository;
    }
    
    /**
     * Sets the module's repository information, assumes the argument is not
     * null.
     * <p>
     * @param moduleRepository The module's repository information
     */
    public void setModuleRepository(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }
    
    /**
     * Sets the module's collection of artwork resources, assumes the argument
     * is not null.
     * <p>
     * @param moduleArtwork The module's artwork
     */
    public void setModuleArtwork(HashMap<String, ModuleArtResource> moduleArtwork) {
        this.moduleArtwork = moduleArtwork;
    }
    
    /**
     * Returns the module's collection of artwork resources.
     * <p>
     * @return The module's collection of artwork
     */
    public HashMap<String, ModuleArtResource> getModuleArtwork() {
        return this.moduleArtwork;
    }
            
    /**
     * Returns a map of WFS objects for all WFSs contained within the module.
     * The key for each entry of the map is the name of the WFS (without the
     * -wfs extension) and the value is its WFS object. If no WFSs exist within
     * the module, this method returns an empty map.
     * <p>
     * @return A map of WFS entries within the module
     */
    public Map<String, WFS> getWFSs() {
        return null; // XXX
    }
    
    /**
     * Returns an input stream for the given resource, null upon error.
     * <p>
     * @param resource A resource contained within the archive
     * @return An input stream to the resource
     */
    public abstract InputStream getInputStream(ModuleResource resource);
    
    /**
     * Stream this module out to an archive file.
     */
    public void writeToJar(File file) {
        // XXX
    }
    
    /**
     * Returns a string representing this module.
     */
    @Override
    public String toString() {
        return this.getModuleInfo().toString() + this.getModuleRequires().toString() +
            this.getModuleRepository().toString() + this.getModuleArtwork().toString();
    }
}