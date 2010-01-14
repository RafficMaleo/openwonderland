<%--
   Document   : index
   Created on : Thu Aug 27 14:29:04 EDT 2009 @811 /Internet Time/
   Author     : gritchie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>Project Wonderland 0.5 Launch Page</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="keywords" content="Project Wonderland, Virtual World, Open Source" />
    <meta name="description" content="Project Wonderland Start Page" />
    <link href="css/base.css" rel="stylesheet" type="text/css" media="screen" />
    <!--[if lt IE 7]>
	<link href="css/patch.css" rel="stylesheet" type="text/css" />
	<![endif]-->
	
  </head>

  <body>
    <div id="page">
      <div id="head">
        <a href="http://projectwonderland.com"><img alt="Project Wonderland logo" src="images/idy_launch.png" /></a>

        <h1>Project Wonderland</h1>
      </div>

      <div id="content">
        <div id="banner">
          <!-- Add your own image(s) here. Max Width: 776px -->
        </div>

        <div id="launch">
          <h2>Welcome to Project Wonderland</h2>

          <div class="btn">
            <a href="app/Wonderland.jnlp"><img alt="Launch. Requires Java" src="images/btn_launch.png" /></a><br />
            <p><a href="http://www.java.com/en/download/index.jsp">Get Java...</a></p>
          </div>

          <p>The Project Wonderland Client launches using Java Web Start, which automatically downloads the latest version of the software when you click the launch button below. To get started, all you need is a current version of Java installed on your system.</p>

          <h3>For more information:</h3>

          <ul>
            <li>Project Wonderland v0.5 User's Guide [coming soon]</li>

            <li><a href="http://ProjectWonderland.com">Project Wonderland Web Site</a></li>

            <li><a href="http://wiki.java.net/bin/view/Javadesktop/ProjectWonderland">Documentation Wiki</a></li>

            <li><a href="http://forums.java.net/jive/forum.jspa?forumID=112">Help Forum</a></li>
          </ul>
        </div>

        <div id="admin">
          <h2>Server Administration</h2>

          <div class="btn">
            <a href="admin"><img alt="Server Admin. May require login." src="images/btn_admin.png" /></a>
          </div>

          <h3>For more information:</h3>

          <ul>
            <li><a href="http://wiki.java.net/bin/view/Javadesktop/ProjectWonderlandServerAdministration">Web-Based Administration Guide</a></li>

            <li><a href="http://wiki.java.net/bin/view/Javadesktop/ProjectWonderlandWebstartPoint5">Launching Wonderland Clients using Java Webstart</a></li>

            <li><a href="http://wiki.java.net/bin/view/Javadesktop/ProjectWonderlandFirewallPoint5">Configuring Project Wonderland for Firewalls, NATs, and Proxies</a></li>
          </ul>
        </div>
      </div>

      <div id="footer">
	<p id="serverInfo">
	Server:  <%= request.getLocalName() %>, Port: <%= request.getLocalPort() %><br/>
	Version: @VERSION@
	</p>
      </div>
    </div>
  </body>
</html>

