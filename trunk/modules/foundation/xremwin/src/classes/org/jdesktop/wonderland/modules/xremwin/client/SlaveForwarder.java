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
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.HashMap;
import com.jme.math.Vector3f;
import java.math.BigInteger;
import java.net.ServerSocket;
import org.jdesktop.wonderland.modules.appbase.client.utils.clientsocket.ClientSocketListener;
import org.jdesktop.wonderland.modules.appbase.client.utils.clientsocket.MasterSocketSet;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The module in the master which broadcasts xremwin messages to slaves.
 *
 * @author deronj
 */
@ExperimentalAPI
class SlaveForwarder {

    private ServerProxyMaster serverProxy;
    private MasterSocketSet socketSet;
    private byte[] welcomeBuf = new byte[Proto.WELCOME_MESSAGE_SIZE];
    static boolean perfTestEnabled = false;
    static boolean perfTestExitOnCompletion = true;
    private final HashMap<Integer, String> clientIdToUserName = new HashMap<Integer, String>();
    private int nextNewClientId = 1;

    public SlaveForwarder(ServerProxyMaster serverProxy, BigInteger sessionID, ServerSocket serverSocket)
            throws IOException {
        this.serverProxy = serverProxy;
        socketSet = new MasterSocketSet(sessionID, serverSocket, new MyListener());
        socketSet.start();
    }

    public void cleanup() {
        disconnect();
        serverProxy = null;
    }

    void disconnect() {
        if (socketSet != null) {
            socketSet.close();
            socketSet = null;
        }
    }

    public void unicastSend(BigInteger slaveID, byte[] buf) {
        unicastSend(slaveID, buf, buf.length);
    }

    public void unicastSend(BigInteger slaveID, byte[] buf, int len) {
        try {
            socketSet.send(slaveID, buf, len);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void broadcastSend(byte[] buf) {
        broadcastSend(buf, buf.length);
    }

    public void broadcastSend(byte[] buf, int len) {
        try {
            socketSet.send(buf, len);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private int allocateClientId(String userName) {

        // Is there already a client id for this user name? If so, the user has
        // disconnected and is reconnecting. So reuse the already assigned id.

        synchronized (clientIdToUserName) {
            for (int clientId : clientIdToUserName.keySet()) {
                if (clientIdToUserName.get(clientId).equals(userName)) {
                    return clientId;
                }
            }
        }

        return nextNewClientId++;
    }

    public String clientIdToUserName(int clientId) {
        synchronized (clientIdToUserName) {
            return clientIdToUserName.get(clientId);
        }
    }

    void unicastWelcomeMessage(BigInteger slaveID, String userName) {
        new Welcomer(slaveID, userName).run();
    }

    private class Welcomer implements Runnable {

        private BigInteger slaveID;
        private String userName;

        private Welcomer(BigInteger slaveID, String userName) {
            this.slaveID = slaveID;
            this.userName = userName;
        }

        public void run() {
            int clientId = allocateClientId(userName);

            int n = 0;
            welcomeBuf[n++] = (byte) Proto.ServerMessageType.WELCOME.ordinal();
            welcomeBuf[n++] = 0;
            welcomeBuf[n++] = 0;
            welcomeBuf[n++] = 0;
            welcomeBuf[n++] = (byte) ((clientId >> 24) & 0xff);
            welcomeBuf[n++] = (byte) ((clientId >> 16) & 0xff);
            welcomeBuf[n++] = (byte) ((clientId >> 8) & 0xff);
            welcomeBuf[n++] = (byte) (clientId & 0xff);

            unicastSend(slaveID, welcomeBuf);

            syncSlaveWindowStateAll(slaveID);

            // Remember that we assigned this client id to this user
            synchronized (clientIdToUserName) {
                clientIdToUserName.put(clientId, userName);
            }
        }

        // Enough to hold approximately ten lines
        private int syncPixelsBufMax = 1280 * 10 * 4;
        private byte[] syncBuf = new byte[54];
        private byte[] syncPixelsBuf = new byte[syncPixelsBufMax];

        // Send all known client window state to a slave
        private void syncSlaveWindowStateAll(BigInteger slaveID) {
            synchronized (AppXrw.widToWindow) {
                Iterator it;

                //  First count the number of windows
                int numWins = 0;
                it = AppXrw.widToWindow.values().iterator();
                while (it.hasNext()) {
                    WindowXrw win = (WindowXrw) it.next();
                    numWins++;
                }

                // Send the window count to the slave
                byte[] buf = new byte[4];
                encode(buf, 0, numWins);
                unicastSend(slaveID, buf);
                System.err.println("numWins = " + numWins);

                // Then send the individual window states
                it = AppXrw.widToWindow.values().iterator();
                while (it.hasNext()) {
                    WindowXrw win = (WindowXrw) it.next();
                    syncSlaveWindowState(slaveID, win);
                }
            }
        }

        private void syncSlaveWindowState(BigInteger slaveID, WindowXrw win) {
            System.err.println("Enter syncSlaveWindowState: win = " + win.getWid());

            String controllingUser = win.getControllingUser();
            int controllingUserLen = (controllingUser != null)
                    ? controllingUser.length() : 0;

            System.err.println("wid = " + win.getWid());
            System.err.println("xy = " + win.getOffsetX() + " " + win.getOffsetY());
            System.err.println("wh = " + win.getWidth() + " " + win.getHeight());
            System.err.println("bw = " + win.getBorderWidth());
            System.err.println("decorated = " + win.isDecorated());
            System.err.println("showing = " + win.isVisibleApp());
            System.err.println("controlling user = " + controllingUser);
            System.err.println("zOrder = " + win.getZOrder());

            /*TODO:
            System.err.println("rotY = " + win.getRotateY());
            System.err.println("userTranslation = " + win.getUserTranslation());
             */

            // Send basic window attributes
            encode(syncBuf, 0, win.getWid());
            encode(syncBuf, 4, win.getOffsetX());
            encode(syncBuf, 8, win.getOffsetY());
            encode(syncBuf, 12, win.getWidth());
            encode(syncBuf, 16, win.getHeight());
            encode(syncBuf, 20, win.getBorderWidth());
            encode(syncBuf, 24, controllingUserLen);
            encode(syncBuf, 28, win.getZOrder());
            /* TODO:
             encode(syncBuf, 32, win.getRotateY());
            encode(syncBuf, 36, userDispl.x);
            encode(syncBuf, 40, userDispl.y);
            encode(syncBuf, 44, userDispl.z);
             */
            /* TODO: 0.4 protocol:
            encode(syncBuf, 48, win.getTransientFor().getWid());
             */
            encode(syncBuf, 48, 0);
            syncBuf[52] = (byte) (win.isDecorated() ? 1 : 0);
            syncBuf[53] = (byte) (win.isVisibleApp() ? 1 : 0);

            unicastSend(slaveID, syncBuf);
            //System.err.println("Call unicastMessage with " + syncBuf.length + " bytes");
            //print10bytes(syncBuf);

            if (controllingUserLen > 0) {
                unicastSend(slaveID, controllingUser.getBytes());
            }

            // Send window contents. Note that if there are pending writes to
            // the window this may block until the next frame tick.
            win.syncSlavePixels(slaveID);

            // It is safe to write to a slave socket only when the master has enqueued 
            // all welcome message buffers.
            socketSet.setEnable(slaveID, true);
        }
    }

    private static void encode(byte[] buf, int startIdx, short value) {
        buf[startIdx + 0] = (byte) ((value >> 8) & 0xff);
        buf[startIdx + 1] = (byte) ((value) & 0xff);
    }

    private static void encode(byte[] buf, int startIdx, int value) {
        buf[startIdx] = (byte) ((value >> 24) & 0xff);
        buf[startIdx + 1] = (byte) ((value >> 16) & 0xff);
        buf[startIdx + 2] = (byte) ((value >> 8) & 0xff);
        buf[startIdx + 3] = (byte) ((value) & 0xff);
    }

    private static void encode(byte[] buf, int startIdx, float value) {
        ByteBuffer byteBuf = ByteBuffer.allocate(4);
        byteBuf.putFloat(value);
        byteBuf.rewind();
        byte[] bytes = new byte[4];
        byteBuf.get(bytes);
        System.arraycopy(bytes, 0, buf, startIdx, 4);
    }

    private class MyListener implements ClientSocketListener {

        public void receivedMessage(BigInteger otherClientID, byte[] message) {

            // See this is the hello message from the slave
            if (message[0] == (byte) Proto.ClientMessageType.HELLO.ordinal()) {
                int strLen = (int)(message[2] << 8) | (int)message[3];
                if (strLen <= 0) {
                    AppXrw.logger.warning("Invalid slave user name string length");
                    return;
                }
                byte[] userNameBuf = new byte[strLen];
                System.arraycopy(message, 4, userNameBuf, 0, strLen);
                String userName = new String(userNameBuf);

                AppXrw.logger.warning("Received hello message from slave " + otherClientID +
                               ", userName = " + userName);
                serverProxy.addIncomingSlaveHelloMessage(otherClientID, userName);
                return;
            }

            // Forward event to the xremwin server
            try {
                serverProxy.write(message);
            } catch (IOException ex) {
                AppXrw.logger.warning("IOException during write to xremwin server");
            }
        }

        public void otherClientHasLeft(BigInteger otherClientID) {
            AppXrw.logger.info("Slave has disconnected: " + otherClientID);
        }
    }

    // For Debug
    private static void print10bytes(byte[] bytes) {
        int n = (bytes.length > 10) ? 10 : bytes.length;
        for (int i = 0; i < n; i++) {
            System.err.print(Integer.toHexString(bytes[i] & 0xff) + " ");
        }
        System.err.println();
    }
}
