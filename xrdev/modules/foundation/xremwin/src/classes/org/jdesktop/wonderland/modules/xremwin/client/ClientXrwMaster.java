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
package org.jdesktop.wonderland.modules.xremwin.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ProcessReporter;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.ControllerStatus;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.ControllerStatusMsgArgs;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.MessageArgs;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.ServerMessageType;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.SlaveCloseWindowMsgArgs;
import org.jdesktop.wonderland.modules.xremwin.client.Proto.SetWindowTitleMsgArgs;

/**
 * The master version of the Xremwin protocol client. This is the only client to 
 * have a direct connection to the Xremwin server. This client interprets protocol
 * messages from the server and applies them locally in the master, but it also
 * forwards the messages to the slaves. It also multiplexes incoming events from
 * slaves and sends them to the server.
 *
 * @author deronj
 */
@ExperimentalAPI
public class ClientXrwMaster extends ClientXrw implements WindowSystemXrw.ExitListener {

    private SlaveCloseWindowMsgArgs slaveCloseWindowMsgArgs = new SlaveCloseWindowMsgArgs();
    private SetWindowTitleMsgArgs setWindowTitleMsgArgs = new SetWindowTitleMsgArgs();
    protected WindowSystemXrw winSys;

    /**
     * Create a new instance of ClientXrwMaster.
     *
     * @param app The application for whom the client is operating.
     * @param controlArb The control arbiter for the app.
     * @param session This app's Wonderland session.
     * @param masterHost The master host name (this host).
     * @param serverSocket The server socket to which slaves should connect.
     * @param winSys The Xremwin window system for the app.
     * @param reporter Report output and exit status to this.
     * @throws InstantiationException If it could not make contact with the server.
     */
    public ClientXrwMaster(AppXrw app, ControlArbXrw controlArb, WonderlandSession session, String masterHost,
            ServerSocket serverSocket, WindowSystemXrw winSys, ProcessReporter reporter)
            throws InstantiationException {
        super(app, controlArb, reporter);
        this.winSys = winSys;
        winSys.setExitListener(this);

        // Connect to the Xremwin server

        int wsDisplayMaster = winSys.getDisplayNum();
        serverProxy = new ServerProxyMaster(session, masterHost, wsDisplayMaster, serverSocket);
        try {
            serverProxy.connect();
        } catch (IOException ex) {
            throw new InstantiationException("Could not contact Xremwin server");
        }
        serverConnected = true;

        controlArb.setServerProxy(serverProxy);

        // Start the protocol interpreter
        start();
    }

    /**
     * Release held resources.
     */
    @Override
    public void cleanup() {
        super.cleanup();
        winSys = null;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected MessageArgs readMessageArgs(ServerMessageType msgType) {
        switch (msgType) {

            case SERVER_DISCONNECT:
                return null;

            case SLAVE_HELLO:
                return null;

            case SLAVE_CLOSE_WINDOW:
                ((ServerProxyMaster) serverProxy).getData(slaveCloseWindowMsgArgs);
                return slaveCloseWindowMsgArgs;

            case SET_WINDOW_TITLE:
                serverProxy.getData(setWindowTitleMsgArgs);
                return null;

            default:
                return super.readMessageArgs(msgType);
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    protected void processMessage(ServerMessageType msgType) {
        switch (msgType) {

            case SERVER_DISCONNECT:
                serverConnected = false;
                break;

            // A slave is trying to connect. Welcome messages to slaves need to be
            // properly serialized with the server messages which we will send to them.

            case SLAVE_HELLO:
                ServerProxyMaster spm = (ServerProxyMaster) serverProxy;
                BigInteger slaveID = spm.getConnectingSlaveID();
                String userName = spm.getConnectingUserName();
                // TODO: It is definitely weird that we are calling a method of a
                // ServerSocketMaster object order to communicate with the slave. But
                // really we are communicating a slave via the SlaveForwarder that
                // the ServerSocketMaster owns. Eventually must up-level most of
                // SlaveForwarder.unicastWelcomeMessage et al. into Client.
                spm.sendWelcomeMessageToSlave(slaveID, userName);
                break;

            case SLAVE_CLOSE_WINDOW:

                // If this was performed interactively by this client, ignore it */
                if (slaveCloseWindowMsgArgs.clientId != clientId) {
                    slaveCloseWindow(slaveCloseWindowMsgArgs.wid);
                }
                break;

            case SET_WINDOW_TITLE:
                // TODO: Ignore the message for now
                break;

            default:
                super.processMessage(msgType);
        }
    }

    /**
     * Inform the slaves that a window's title has changed.
     *
     * @param wid The window whose title changed.
     * @param title The window's new title.
     */
    public synchronized void setWindowTitle(int wid, String title) {
        WindowXrw win = lookupWindow(wid);
        if (win != null) {
            win.setTitle(title);
        }

        // Now tell the server so it can tell the slave clients
        try {
            AppXrw.logger.finer("Write set window title to server");
	    if (title == null || title.length() <= 0) {
		title = " ";
	    }
            serverProxy.writeSetWindowTitle(wid, title);
        } catch (IOException ex) {
            AppXrw.logger.warning("Master cannot set window title for wid " + wid);
        }
    }

    /**
     * Handle the ControllerStatus Message.
     */
    @Override
    protected void processControllerStatus(ControllerStatusMsgArgs msgArgs) {
        super.processControllerStatus(msgArgs);
        if (msgArgs.status == ControllerStatus.GAINED) {
            controlArb.setController(((ServerProxyMaster) serverProxy).getControllingUser());
        }
    }

    /**
     * Inform the slaves that the parent of a popup window has changed.
     *
     * @param window The popup window.
     * @param parent The new parent of the popup window.
     */
    public void setPopupParent(WindowXrw window, WindowXrw parent) {
        int wid = window.getWid();
        int parentWid = parent.getWid();
        ((ServerProxyMaster) serverProxy).setPopupParent(wid, parentWid);
    }

    /**
     * A slave has closed a window.
     *
     * @param wid The wid of the window the slave has closed.
     */
    public void slaveCloseWindow(int wid) {
        // A slave can only close a window when it has control
        if (controlArb.hasControl()) {
            AppXrw.logger.finer("CLOSE_WINDOW");
            WindowXrw win = lookupWindow(wid);
            if (win != null) {
                closeWindow(win);
            }
        }
    }

    /**
     * Close the given window.
     */
    public void closeWindow(WindowXrw win) {
        winSys.deleteWindow(win.getWid());
    }

    // Called by window system exit listener 
    public void windowSystemExitted() {
        controlArb.releaseControl();
    }

    public void writeSyncSlavePixels(BigInteger slaveID, byte[] pixelBytes) {
        ((ServerProxyMaster) serverProxy).writeSlaveSyncPixels(slaveID, pixelBytes);
    }
}