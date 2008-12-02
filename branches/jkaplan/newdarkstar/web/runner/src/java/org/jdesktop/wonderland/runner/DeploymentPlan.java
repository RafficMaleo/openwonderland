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
package org.jdesktop.wonderland.runner;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Store information for starting runners
 * @author jkaplan
 */
@XmlRootElement(name="DeploymentPlan")
public class DeploymentPlan {
    private Collection<DeploymentEntry> entries = 
            new LinkedHashSet<DeploymentEntry>();

    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all DeploymentEntries */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(DeploymentPlan.class);
            DeploymentPlan.unmarshaller = jc.createUnmarshaller();
            DeploymentPlan.marshaller = jc.createMarshaller();
            DeploymentPlan.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    public DeploymentPlan() {
        // default no-arg constructor
    }
    
    public DeploymentPlan(DeploymentEntry[] entries) {
        this (Arrays.asList(entries));
    }
    
    public DeploymentPlan(Collection<DeploymentEntry> entries) {
        this.entries.addAll(entries);
    }
    
    public void addEntry(DeploymentEntry entry) {
        entries.add(entry);
    }
    
    public void removeEntry(DeploymentEntry entry) {
        entries.remove(entry);
    }
        
    public DeploymentEntry getEntry(String name) {
        for (DeploymentEntry entry : entries) {
            if (entry.getRunnerName().equals(name)) {
                return entry;
            }
        }
        
        // not found
        return null;
    }
    
    /* Setters and getters */
    @XmlElements({
        @XmlElement(name="entry")
    })
    public Collection<DeploymentEntry> getEntries() { 
        return this.entries; 
    }
     
    public void setEntries(Collection<DeploymentEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }
    
    public void setEntries(DeploymentEntry[] entries) {
        setEntries(Arrays.asList(entries));
    }
    
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the DeploymentPlan class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to DeploymentPlan
     * @throw JAXBException Upon error reading the XML file
     */
    public static DeploymentPlan decode(Reader r) throws JAXBException {
        return (DeploymentPlan) DeploymentPlan.unmarshaller.unmarshal(r);
    }
    
    /**
     * Writes the DeploymentPlan class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        DeploymentPlan.marshaller.marshal(this, w);
    }

    /**
     * Writes the DeploymentPlan class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        DeploymentPlan.marshaller.marshal(this, os);
    }
}
