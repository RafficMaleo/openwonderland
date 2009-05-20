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
package org.jdesktop.wonderland.modules.xremwin.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.xremwin.common.registry.XAppRegistryItem;

/**
 *
 * @author jkaplan
 */
public class XAppsServlet extends HttpServlet implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(XAppsServlet.class.getName());
    private AdminRegistration ar;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        ServletContext sc = getServletContext();

        // Get the repository
        WebContentRepositoryRegistry reg = WebContentRepositoryRegistry.getInstance();
        WebContentRepository wcr = reg.getRepository(sc);
        if (wcr == null) {
            error(request, response, "No content repositories found. <br>" +
                  "Please contact your system administrator for assistance.");
            return;
        }

        // Fetch the content node for the "x-apps" directory under "system". If
        // "x-apps" isn't there, then create it.
        ContentCollection xAppsCollection = null;
        try {
            ContentCollection sysRoot = wcr.getSystemRoot();
            ContentNode xappsNode = sysRoot.getChild("x-apps");
            if (xappsNode == null) {
                xappsNode = sysRoot.createChild("x-apps", Type.COLLECTION);
            }
            xAppsCollection = (ContentCollection)xappsNode;
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to get x-apps collection", excp);
            error(request, response, "No x-apps collection found. <br>" +
                  "Please contact your system administrator for assistance.");
            return;
        }

        // See if the request comes with an "action" (e.g. Delete). If so,
        // handle it and fall through to below to re-load the page
        try {
            String action = request.getParameter("action");
            if (action != null && action.equalsIgnoreCase("delete") == true) {
                handleDelete(request, response, xAppsCollection);
            }
            else if (action != null && action.equalsIgnoreCase("add") == true) {
                handleAdd(request, response, xAppsCollection);
            }

            // Otherwise, display the items
            handleBrowse(request, response, xAppsCollection);
        } catch (java.lang.Exception cre) {
            throw new ServletException(cre);
        }
    }

    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         String message)
        throws ServletException, IOException
    {
        request.setAttribute("message", message);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/error.jsp");
        rd.forward(request, response);
    }

    /**
     * Deletes an entry from the X11 Apps.
     */
    private void handleDelete(HttpServletRequest request,
            HttpServletResponse response, ContentCollection xAppsCollection)
        throws ServletException, IOException, ContentRepositoryException
    {
        String path = request.getParameter("path");
        ContentResource resource = getXAppResource(xAppsCollection, path);
        if (resource == null) {
            error(request, response, "Path " + request.getPathInfo() +
                    " not found.");
            return;
        }
        xAppsCollection.removeChild(resource.getName());
    }

    /**
     * Adds an entry from the X11 Apps.
     */
    private void handleAdd(HttpServletRequest request,
            HttpServletResponse response, ContentCollection xAppsCollection)
        throws ServletException, IOException, ContentRepositoryException,
            JAXBException
    {
        String appName = request.getParameter("appName");
        String command = request.getParameter("command");

        String nodeName = appName + ".xml";
        ContentNode appNode = xAppsCollection.getChild(nodeName);
        if (appNode == null) {
            appNode = xAppsCollection.createChild(nodeName, Type.RESOURCE);
        }
        ContentResource resource = (ContentResource)appNode;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(os);
        XAppRegistryItem item = new XAppRegistryItem(appName, command);
        item.encode(w);
        byte b[] = os.toByteArray();
        resource.put(b);
    }

    /**
     * Handles the default "browse" action to display the X11 App entries.
     */
    private void handleBrowse(HttpServletRequest request,
            HttpServletResponse response, ContentCollection c)
            throws ServletException, IOException, ContentRepositoryException,
            JAXBException
    {
        // Loop through all of the entries in the content repo and spit out
        // the information to a collection of X11AppEntry objects. This will
        // be displayed by the jsp.
        Collection<X11AppEntry> entries = new ArrayList();
        for (ContentNode child : c.getChildren()) {
            if (child instanceof ContentResource) {
                // Find out the information about the content resource item
                ContentResource resource = (ContentResource)child;
                String path = resource.getPath();

                // Use JAXB to parse the item
                Reader r = new InputStreamReader(resource.getInputStream());
                XAppRegistryItem item = XAppRegistryItem.decode(r);

                String appName = item.getAppName();
                String command = item.getCommand();
                
                X11AppEntry entry = new X11AppEntry(appName, command, path);
                String url = "delete&path=" + path;
                entry.addAction(new X11AppAction("delete", url));
                entries.add(entry);
            }
        }

        request.setAttribute("entries", entries);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/browse.jsp");
        rd.forward(request, response);
    }

    /**
     * Translates a path from the web page into a ContentResource representing
     * the file storing the X11 App information.
     */
    private ContentResource getXAppResource(ContentCollection node, String path)
            throws ContentRepositoryException
    {
        // Make sure the root starts with the proper prefix.
        if (path == null || path.startsWith("/system/x-apps") == false) {
            return null;
        }
        path = path.substring("/system/x-apps".length());
        return (ContentResource)node.getChild(path);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void contextInitialized(ServletContextEvent sce) {
        // register with the admininstration page
        ServletContext sc = sce.getServletContext();
        ar = new AdminRegistration("X Apps",
                                   "/xremwin/wonderland-xremwin/browse");
        AdminRegistration.register(ar, sc);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // register with the admininstration page
        ServletContext sc = sce.getServletContext();
        AdminRegistration.unregister(ar, sc);
    }

    public static class X11AppEntry {

        private String appName;
        private String command;
        private String path;
        private List<X11AppAction> actions;

        public X11AppEntry(String appName, String command, String path) {
            this.appName = appName;
            this.command = command;
            this.actions = new ArrayList<X11AppAction>();
        }

        public void addAction(X11AppAction action) {
            actions.add(action);
        }

        public List<X11AppAction> getActions() {
            return actions;
        }

        public String getAppName() {
            return appName;
        }

        public String getCommand() {
            return command;
        }

        public String getPath() {
            return path;
        }
    }

    public static class X11AppAction {
        private String name;
        private String url;

        public X11AppAction(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
