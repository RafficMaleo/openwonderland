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
package org.jdesktop.wonderland.client.login;

import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A description of a Wonderland server we are trying to connect to.
 * Servers are uniquely identified by URL.
 * @author jkaplan
 */

@XmlRootElement
public class ServerDetails implements Cloneable {
    private String serverURL;
    private AuthenticationInfo authInfo;
    private DarkstarServer[] darkstarServers;

    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(ServerDetails.class);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }

    /**
     * Default constructor
     */
    public ServerDetails() {
    }

    /**
     * Create a new ServerDetails with the given information
     * @param serverURL the server URL
     * @param authInfo the authentication information
     * @param darkstarServers the servers to connect to
     */
    public ServerDetails(String serverURL, AuthenticationInfo authInfo,
                         DarkstarServer[] darkstarServers)
    {
        this.serverURL = serverURL;
        this.authInfo = authInfo;
        this.darkstarServers = darkstarServers;
    }

    /**
     * Get the URL of the server
     * @return the server URL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * Set the URL of the server
     * @param serverURL the server URL
     */
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    /**
     * Get the authentication information for this server
     * @return the authentication information
     */
    @XmlElement
    public AuthenticationInfo getAuthInfo() {
        return authInfo;
    }

    /**
     * Set the authentication information for this server
     * @param authInfo the authentication information
     */
    public void setAuthInfo(AuthenticationInfo authInfo) {
        this.authInfo = authInfo;
    }

    /**
     * Get the list of possible Darkstar servers to connect to.  The client
     * should try the servers in the order they are contained in the
     * array.
     * @return an ordered list of Darkstar servers
     */
    @XmlElement
    public DarkstarServer[] getDarkstarServers() {
        return darkstarServers;
    }

    /**
     * Set the list of Darkstar servers
     * @param darkstarServers the servers to connect to
     */
    public void setDarkstarServers(DarkstarServer[] darkstarServers) {
        this.darkstarServers = darkstarServers;
    }

    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ServerDetails class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to ServerDetails
     * @throw JAXBException Upon error reading the XML file
     */
    public static ServerDetails decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ServerDetails)unmarshaller.unmarshal(r);
    }

    /**
     * Writes the ServerDetails class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }

    /**
     * Writes the ServerDetails class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, os);
    }

    /**
     * Copy this serverDetails object
     * @return a copy
     */
    @Override
    public ServerDetails clone() {
        ServerDetails out = new ServerDetails();
        out.setServerURL(getServerURL());
        out.setAuthInfo(getAuthInfo().clone());

        // copy servers array
        DarkstarServer[] inServers = getDarkstarServers();
        DarkstarServer[] outServers = new DarkstarServer[inServers.length];
        for (int i = 0; i < inServers.length; i++) {
            outServers[i] = inServers[i].clone();
        }
        out.setDarkstarServers(outServers);

        return out;
    }

    /**
     * Two ServerDetails are the same if they have the same URL
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerDetails other = (ServerDetails) obj;
        if ((this.serverURL == null) ? (other.serverURL != null) : !this.serverURL.equals(other.serverURL)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.serverURL != null ? this.serverURL.hashCode() : 0);
        return hash;
    }
}
