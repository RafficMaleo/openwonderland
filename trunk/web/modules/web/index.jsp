<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
    <%@ page import="java.util.Collections" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.List" %>
    <%@ page import="java.util.LinkedList" %>
    <%@ page import="java.util.Iterator" %>
    <%@ page import="org.jdesktop.wonderland.modules.Module" %>
    <%@ page import="org.jdesktop.wonderland.common.modules.ModuleInfo" %>
    <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
    <h2>Manage Modules</h2>
    <form action="removeAll.jsp">
        <table class="installed">
          <caption>
              <span class="heading">Installed Modules</span>
          </caption>
            <tr class="header">
                <td width="5%" class="installed"></td>
                <td width="15%" class="installed">Module Name</td>
                <td width="15%" class="installed">Module Version</td>
                <td width="65%" class="installed">Description</td>
            </tr>
            <%
            ModuleManager manager = ModuleManager.getModuleManager();
            Map<String, Module> installed = manager.getInstalledModules();
            List<String> nameList = new LinkedList(installed.keySet());
            Collections.sort(nameList);
            for (String moduleName : nameList) {
                ModuleInfo moduleInfo = installed.get(moduleName).getInfo();
                String description = moduleInfo.getDescription();
            %>
            <tr class="installed">
                <td width="5%" class="installed"><input type="checkbox" name="remove" value="<%= moduleName%>"/></td>
                <td width="15%" class="installed"><%= moduleName%></td>
                <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
                <td width="65%" class="installed"><%= (description != null) ? description : "[None]" %></td>
            </tr>
            <% }%>
        </table>
        <div id="actionLinks">
            <input type="submit" value="Remove Selected Modules">
        </div>
    </form>
    <br>

    <table class="installed">
        <caption>
              <span class="heading">Pending Modules (will be installed during next restart)</span>
        </caption>
        <tr class="header">
            <td width="15%" class="installed">Module Name</td>
            <td width="15%" class="installed">Module Version</td>
            <td width="70%" class="installed">Description</td>
        </tr>
        <%
            Map<String, Module> pending = manager.getPendingModules();
            Iterator<Map.Entry<String, Module>> it2 = pending.entrySet().iterator();
            while (it2.hasNext() == true) {
                Map.Entry<String, Module> entry = it2.next();
                String moduleName = entry.getKey();
                ModuleInfo moduleInfo = entry.getValue().getInfo();
        %>
        <tr class="installed">
            <td width="15%" class="installed"><%= moduleName%></td>
            <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
            <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
        </tr>
        <% }%>
    </table>
    <br>

    <table class="installed">
        <caption>
              <span class="heading">Removed Modules (will be removed during next restart)</span>
        </caption>
        <tr class="header">
            <td width="15%" class="installed">Module Name</td>
            <td width="15%" class="installed">Module Version</td>
            <td width="70%" class="installed">Description</td>
        </tr>
        <%
        Map<String, ModuleInfo> uninstall = manager.getUninstallModuleInfos();
            Iterator<Map.Entry<String, ModuleInfo>> it3 = uninstall.entrySet().iterator();
            while (it3.hasNext() == true) {
                Map.Entry<String, ModuleInfo> entry = it3.next();
                ModuleInfo moduleInfo = entry.getValue();
        %>
        <tr class="installed">
            <td width="15%" class="installed"><%= moduleInfo.getName()%></td>
            <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
            <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
        </tr>
        <% }%>
    </table>
    <br>

    <table class="installed">
        <caption>
            <span class="heading">Install a New Module</span>
        </caption>
        <tr class="installed">
            <td class="installed">
                Select a new module JAR to install and click INSTALL.<br>
                <form method="post" enctype="multipart/form-data" action="ModuleUploadServlet">
                    Module JAR file: <input type="file" name="moduleJAR">
                    <input type="submit" value="INSTALL">
                </form>
            </td>
        </tr>
    </table>
</body>
</html>