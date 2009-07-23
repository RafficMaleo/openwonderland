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
package org.jdesktop.wonderland.client.cell.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;
import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 * Manages the set of propery panels configuring cell components. Cell components
 * implement the CellComponentPropertiesSPI interface and register their class
 * with the Java service loader mechanism. This class lists all of these
 * component properties.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@InternalAPI
public class CellComponentPropertiesManager implements PrimaryServerListener {

    /* A set of all cell property objects */
    private Set<CellComponentPropertiesSPI> componentPropertiesSet;
    
    /* A map of component server-state class and their cell properties objects */
    private Map<Class, CellComponentPropertiesSPI> componentPropertiesClassMap;

    /* A set of cell property objects associated with the current session */
    private final Set<CellComponentPropertiesSPI> sessionProperties =
            new HashSet<CellComponentPropertiesSPI>();

    /** Default constructor */
    public CellComponentPropertiesManager() {
        componentPropertiesClassMap = new HashMap();
        componentPropertiesSet = new HashSet();
    
        LoginManager.addPrimaryServerListener(this);
    }
    
    /**
     * Singleton to hold instance of CellRegistry. This holder class is loaded
     * on the first execution of CellRegistry.getMediaManager().
     */
    private static class ComponentPropertiesHolder {
        private final static CellComponentPropertiesManager cellProperties = new CellComponentPropertiesManager();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final CellComponentPropertiesManager getCellComponentPropertiesManager() {
        return ComponentPropertiesHolder.cellProperties;
    }
    
    /**
     * Registers a CellComponentPropertiesSPI class. This interface is used to generate
     * a GUI to allow editing of a cell's properties on the client-side.
     * 
     * @param properties The CellComponentPropertiesSPI class to register
     */
    public synchronized void registerCellComponentProperties(CellComponentPropertiesSPI properties) {
        // First check to see if the server-side component state class already
        // exists and print a warning message if so (but we'll
        // still add it later)
        Class clazz = properties.getServerCellComponentClass();
        if (componentPropertiesClassMap.containsKey(clazz) == true) {
            Logger logger = Logger.getLogger(CellComponentPropertiesManager.class.getName());
            logger.warning("A CellComponentPropertiesSPI already exist for class " +
                    clazz.getName());
        }

        // Add to the set of all CellComponentPropertiesSPI objects and the map relating
        // the client-side cell class name to the object
        componentPropertiesSet.add(properties);
        componentPropertiesClassMap.put(clazz, properties);
    }

    /**
     * Unregisters a CellComponentPropertiesSPI class.
     *
     * @param properties The CellComponentPropertiesSPI class to unregister
     */
    public synchronized void unregisterCellComponentProperties(CellComponentPropertiesSPI properties) {
        Class clazz = properties.getServerCellComponentClass();

        // remove the SPI object.  Check to make sure the class maps to the same
        // properties object before removing the mapping
        componentPropertiesSet.remove(properties);

        CellComponentPropertiesSPI cur = componentPropertiesClassMap.get(clazz);
        if (cur == properties) {
            componentPropertiesClassMap.remove(clazz);
        }
    }
    
    /**
     * Returns a set of all cell properies objects. If no properties are registered,
     * returns an empty set.
     * 
     * @return A set of registered cell property objects
     */
    public Set<CellComponentPropertiesSPI> getAllCellComponentProperties() {
        return new HashSet(componentPropertiesSet);
    }
    
    /**
     * Returns a cell properties object given the name of the server-side cell
     * component state Class that the properties supports. If no properties are
     * present for the given cell, returns null.
     * 
     * @param clazz The Class of the server-side component state
     * @return A CellComponentPropertiesSPI object registered for the cell class
     */
    public CellComponentPropertiesSPI getCellComponentPropertiesByClass(Class clazz) {
        return componentPropertiesClassMap.get(clazz);
    }

    /**
     * Notification that the primary server has changed
     * @param server the current primary server
     */
    public void primaryServer(ServerSessionManager server) {
        // get rid of the current properties
        unregisterProperties();

        // add back the properties from the current server
        if (server != null) {
            registerProperties(server);
        }
    }

    /**
     * Register all Properties associated with the given session manager.
     * @param sessionManager the manager to register
     */
    protected synchronized void registerProperties(ServerSessionManager manager) {
        /* Attempt to load the class names using the service providers */
        ScannedClassLoader cl = manager.getClassloader();

        Iterator<CellComponentPropertiesSPI> it = cl.getAll(
                CellComponentProperties.class, CellComponentPropertiesSPI.class);
        while (it.hasNext() == true) {
            CellComponentPropertiesSPI spi = it.next();
            registerCellComponentProperties(spi);
            sessionProperties.add(spi);
        }
    }

    /**
     * Unregister all Properties associated with the current session
     */
     protected synchronized void unregisterProperties() {
         for (CellComponentPropertiesSPI spi : sessionProperties) {
            unregisterCellComponentProperties(spi);
         }
         sessionProperties.clear();
     }
}