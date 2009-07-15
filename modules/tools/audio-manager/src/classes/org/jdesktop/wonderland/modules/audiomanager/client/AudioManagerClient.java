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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import org.jdesktop.wonderland.common.NetworkAddress;

import org.jdesktop.wonderland.common.comms.ConnectionType;

import org.jdesktop.wonderland.common.cell.CallID;
import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantCallEndedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.CallEndedResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterExitMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayerInRangeMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoResponseMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatHoldMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinAcceptedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

import javax.swing.JMenuItem;

import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;

/**
 *
 * @author jprovino
 */
public class AudioManagerClient extends BaseConnection implements
        AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener {

    private static final Logger logger =
            Logger.getLogger(AudioManagerClient.class.getName());
    private WonderlandSession session;
    private boolean connected = true;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;
    private Cell cell;
    private JMenuItem userListJMenuItem;
    private ArrayList<DisconnectListener> disconnectListeners = new ArrayList();
    private HashMap<String, ArrayList<MemberChangeListener>> memberChangeListeners = new HashMap();
    private HUDComponent userListHUDComponent;
    private UserListHUDPanel userListHUDPanel;
    private boolean usersMenuSelected = false;

    /** 
     * Create a new AudioManagerClient
     * @param session the session to connect to, guaranteed to be in
     * the CONNECTED state
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     */
    public AudioManagerClient() {
        AudioMenu.getAudioMenu(this).setEnabled(false);

        userListJMenuItem = new javax.swing.JCheckBoxMenuItem();
        userListJMenuItem.setText("Users");
        userListJMenuItem.setSelected(usersMenuSelected);
        userListJMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usersMenuSelected = !usersMenuSelected;
                userListJMenuItem.setSelected(usersMenuSelected);
                showUsers(evt);
            }
        });
        userListJMenuItem.setEnabled(false);

        logger.fine("Starting AudioManagerCLient");
    }

    public void addDisconnectListener(DisconnectListener listener) {
        disconnectListeners.add(listener);
    }

    public void removeDisconnectListener(DisconnectListener listener) {
        disconnectListeners.add(listener);
    }

    private void notifyDisconnectListeners() {
        for (DisconnectListener listener : disconnectListeners) {
            listener.disconnected();
        }
    }

    public void addMemberChangeListener(String group, MemberChangeListener listener) {
        ArrayList<MemberChangeListener> listeners = memberChangeListeners.get(group);

        if (listeners == null) {
            listeners = new ArrayList();
            memberChangeListeners.put(group, listeners);
        }

        listeners.add(listener);
    }

    public void removeMemberChangeListener(String group, MemberChangeListener listener) {
        ArrayList<MemberChangeListener> listeners = memberChangeListeners.get(group);

        listeners.remove(listener);
    }

    public void notifyMemberChangeListeners(String group, PresenceInfo member, boolean added) {
        ArrayList<MemberChangeListener> listeners = memberChangeListeners.get(group);

        if (listeners == null) {
            logger.fine("NO LISTENERS!");
            return;
        }

        for (MemberChangeListener listener : listeners) {
            listener.memberChange(member, added);
        }
    }

    public void notifyMemberChangeListeners(String group, PresenceInfo[] members) {
        ArrayList<MemberChangeListener> listeners = memberChangeListeners.get(group);

        if (listeners == null) {
            logger.fine("NO LISTENERS!");
            return;
        }

        //for (int i = 0; i < members.length; i++) {
        //    System.out.println("setMembers:  " + members[i]);
        //}

        for (MemberChangeListener listener : listeners) {
            listener.setMemberList(members);
        }
    }

    public void showUsers(java.awt.event.ActionEvent evt) {
        if (presenceInfo == null) {
            return;
        }

        if (userListHUDComponent == null) {
            userListHUDPanel = new UserListHUDPanel(this, session, pm, cell);
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            userListHUDComponent = mainHUD.createComponent(userListHUDPanel);
	    userListHUDPanel.setHUDComponent(userListHUDComponent);
            userListHUDComponent.setPreferredLocation(Layout.NORTHWEST);

            mainHUD.addComponent(userListHUDComponent);
            userListHUDComponent.addComponentListener(new HUDComponentListener() {

                public void HUDComponentChanged(HUDComponentEvent e) {
                    if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
                        usersMenuSelected = false;
                        userListJMenuItem.setSelected(usersMenuSelected);
                    }
                }
            });
        }

        userListHUDPanel.setUserList();
        userListHUDComponent.setVisible(usersMenuSelected);
        System.out.println("U LOcation:  " + userListHUDComponent.getLocation());
    }

    public synchronized void execute(final Runnable r) {
    }

    @Override
    public void connect(WonderlandSession session)
            throws ConnectionFailureException {
        super.connect(session);

        this.session = session;

        pm = PresenceManagerFactory.getPresenceManager(session);

        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
        avatar.addViewCellConfiguredListener(this);
        if (avatar.getViewCell() != null) {
            // if the view is already configured, fake an event
            viewConfigured(avatar);
        }

        SoftphoneControlImpl.getInstance().addSoftphoneListener(this);

        // enable the menus
        AudioMenu.getAudioMenu(this).setEnabled(true);
        userListJMenuItem.setEnabled(true);
    }

    @Override
    public void disconnected() {
        super.disconnected();

        // TODO: add methods to remove listeners!

        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
        avatar.removeViewCellConfiguredListener(this);

        SoftphoneControlImpl.getInstance().removeSoftphoneListener(this);
        SoftphoneControlImpl.getInstance().sendCommandToSoftphone("endCalls");
        //JmeClientMain.getFrame().removeAudioMenuListener(this);
        notifyDisconnectListeners();
    }

    public void addMenus() {
        JmeClientMain.getFrame().addToToolsMenu(AudioMenu.getAudioMenuItem(this), 1);
        JmeClientMain.getFrame().addToWindowMenu(userListJMenuItem, 5);

        AudioMenu.getAudioMenu(this).addMenus();
    }

    public void removeMenus() {
        JmeClientMain.getFrame().removeFromToolsMenu(AudioMenu.getAudioMenuItem(this));
        JmeClientMain.getFrame().removeFromWindowMenu(userListJMenuItem);

        AudioMenu.getAudioMenu(this).removeMenus();
    }

    public void viewConfigured(LocalAvatar localAvatar) {
	cell = localAvatar.getViewCell();
        CellID cellID = cell.getCellID();

	/*
	 * We require the PresenceManager so by the time we get here,
	 * our presenceInfo has to be available.
	 */
        presenceInfo = pm.getPresenceInfo(cellID);

        logger.fine("[AudioManagerClient] view configured for cell " +
            cellID + " presence: " + presenceInfo + " from " + pm);

        connectSoftphone();
    }

    public void connectSoftphone() {
        logger.fine("[AudioManagerClient] Sending message to server to get voice bridge...");

        if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
            logger.warning("Sending message to server to get voice bridge... session is " + session.getStatus());

            session.send(this, new GetVoiceBridgeMessage());
        }
    }

    public void showSoftphone() {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        sc.setVisible(!sc.isVisible());
    }

    public void setAudioQuality(AudioQuality audioQuality) {
        SoftphoneControlImpl.getInstance().setAudioQuality(audioQuality);

        System.out.println("Set audio quality, now reconnect softphone");
        reconnectSoftphone();
    }

    public void testAudio() {
        SoftphoneControlImpl.getInstance().runLineTest();
    }

    public void reconnectSoftphone() {
        connectSoftphone();
    }

    public void transferCall() {
	AudioParticipantComponent component = cell.getComponent(AudioParticipantComponent.class);

	if (component == null) {
	    logger.warning("Can't transfer call:  No AudioParticipantComponent for " + cell.getCellID());
	    return;
	}

	component.transferCall(this);
    }

    public void logAudioProblem() {
        SoftphoneControlImpl.getInstance().logAudioProblem();
    }

    public void mute(boolean isMuted) {
	this.isMuted = isMuted;

        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        sc.mute(isMuted);

        if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
            session.send(this, new MuteCallMessage(sc.getCallID(), isMuted));
        } else {
            logger.warning("Unabled to send MuteCallMessage.  Session is not connected.");
        }
    }

    public void voiceChat() {
        if (presenceInfo == null) {
            return;
        }

        InCallHUDPanel inCallHUDPanel = 
	    new InCallHUDPanel(this, session, presenceInfo, presenceInfo);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        final HUDComponent inCallHUDComponent = mainHUD.createComponent(inCallHUDPanel);
	inCallHUDPanel.setHUDComponent(inCallHUDComponent);
        inCallHUDComponent.setPreferredLocation(Layout.NORTH);
        mainHUD.addComponent(inCallHUDComponent);
        inCallHUDComponent.addComponentListener(new HUDComponentListener() {
            public void HUDComponentChanged(HUDComponentEvent e) {
                if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
                }
            }
        });

	PropertyChangeListener plistener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
                if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                    inCallHUDComponent.setVisible(false);
                }
            }
        };
        inCallHUDPanel.addPropertyChangeListener(plistener);
	inCallHUDComponent.setVisible(true);
    }

    public void personalPhone() {
        if (presenceInfo == null) {
            return;
        }

        CallHUDPanel callHUDPanel = new CallHUDPanel(this, session, presenceInfo);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        final HUDComponent callHUDComponent = mainHUD.createComponent(callHUDPanel);
	callHUDPanel.setHUDComponent(callHUDComponent);
        callHUDComponent.setPreferredLocation(Layout.NORTHEAST);
        mainHUD.addComponent(callHUDComponent);
        callHUDComponent.addComponentListener(new HUDComponentListener() {
            public void HUDComponentChanged(HUDComponentEvent e) {
                if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
                }
            }
        });

	PropertyChangeListener plistener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
                if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                    callHUDComponent.setVisible(false);
                }
            }
        };
        callHUDPanel.addPropertyChangeListener(plistener);
	callHUDComponent.setVisible(true);
    }

    public void softphoneVisible(boolean isVisible) {
    }

    private boolean isMuted = true;

    public void softphoneMuted(boolean isMuted) {
	if (this.isMuted == isMuted) {
	    return;
	}

        AudioMenu.getAudioMenu(this).mute(isMuted);
	mute(isMuted);
    }

    public void softphoneConnected(boolean connected) {
    }

    public void softphoneExited() {
        System.out.println("Softphone exited, reconnect");

        logger.fine("Softphone exited, reconnect");

        connectSoftphone();
    }

    public void microphoneGainTooHigh() {
    }

    private MicVuMeterFrame micVuMeterFrame;

    public void microphoneVolume() {
	try {
            if (SoftphoneControlImpl.getInstance().isConnected() == false) {
	        return;
	    }
	} catch (IOException e) {
	    return;
	}
 
        if (micVuMeterFrame != null) {
            micVuMeterFrame.startVuMeter(false);
        }
        micVuMeterFrame = new MicVuMeterFrame(this);
    }

    public void transferCall(String phoneNumber) {
        session.send(this, new TransferCallMessage(presenceInfo, phoneNumber, false));
    }

    public void cancelCallTransfer() {
        session.send(this, new TransferCallMessage(presenceInfo, "", true));
    }

    @Override
    public void handleMessage(Message message) {
        logger.fine("got a message...");

        if (message instanceof GetVoiceBridgeResponseMessage) {
            GetVoiceBridgeResponseMessage msg = (GetVoiceBridgeResponseMessage) message;

            logger.warning("Got voice bridge " + msg.getBridgeInfo());

	    String phoneNumber = System.getProperty(
		"org.jdesktop.wonderland.modules.audiomanager.client.PHONE_NUMBER");

	    System.setProperty(
		"org.jdesktop.wonderland.modules.audiomanager.client.PHONE_NUMBER", "");

	    if (phoneNumber != null && phoneNumber.length() > 0) {
                session.send(this, new PlaceCallMessage(presenceInfo, phoneNumber, 0., 0., 0., 90., false));
		return;
	    }

            SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

            /*
             * The voice bridge info is a String of values separated by ":".
             * The numbers indicate the index in tokens[].
             *
             *     0      1      2                   3                 4
             * <bridgeId>::<privateHostName>:<privateControlPort>:<privateSipPort>
             *			 5                   6                 7
             *   	  :<publicHostName>:<publicControlPort>:<publicSipPort>
             */
            String tokens[] = msg.getBridgeInfo().split(":");

            String registrarAddress = tokens[5] + ";sip-stun:";

            registrarAddress += tokens[7];

            String localAddress = null;

            try {
                InetAddress ia = NetworkAddress.getPrivateLocalAddress(
                        "server:" + tokens[5] + ":" + tokens[7] + ":10000");

                localAddress = ia.getHostAddress();
            } catch (UnknownHostException e) {
                logger.warning(e.getMessage());

                logger.warning("The client is unable to connect to the bridge public address. " + " Trying the bridge private Address.");

                try {
                    InetAddress ia = NetworkAddress.getPrivateLocalAddress(
                            "server:" + tokens[2] + ":" + tokens[4] + ":10000");

                    localAddress = ia.getHostAddress();
                } catch (UnknownHostException ee) {
                    logger.warning(ee.getMessage());
                }
            }

            if (localAddress != null) {
                try {
                    String sipURL = sc.startSoftphone(
                            presenceInfo.userID.getUsername(), registrarAddress, 10, localAddress);

                    logger.fine("Starting softphone:  " + presenceInfo);

		    if (sipURL != null) {
                        // XXX need location and direction
                        session.send(this, new PlaceCallMessage(presenceInfo, sipURL, 0., 0., 0., 90., false));
		    } else {
			System.out.println("Failed to start softphone, retrying.");

			try {
			    Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			
			connectSoftphone();
		    }
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            } else {
                // XXX Put up a dialog box here
                logger.warning("LOCAL ADDRESS IS NULL.  AUDIO WILL NOT WORK!!!!!!!!!!!!");
                /*
                 * Try again.
                 */
                connectSoftphone();
            }
        } else if (message instanceof VoiceChatJoinRequestMessage) {
            new IncomingCallDialog(this, session, cell.getCellID(), (VoiceChatJoinRequestMessage) message);
        } else if (message instanceof VoiceChatBusyMessage) {
            VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;

            new VoiceChatBusyDialog(msg.getGroup(), msg.getCallee());
            notifyMemberChangeListeners(msg.getGroup(), msg.getCallee(), false);
        } else if (message instanceof VoiceChatInfoResponseMessage) {
            VoiceChatInfoResponseMessage msg = (VoiceChatInfoResponseMessage) message;
            notifyMemberChangeListeners(msg.getGroup(), msg.getChatters());
        } else if (message instanceof VoiceChatJoinAcceptedMessage) {
            VoiceChatJoinAcceptedMessage msg = (VoiceChatJoinAcceptedMessage) message;

            logger.fine("GOT JOIN ACCEPTED MESSAGE FOR " + msg.getCallee());

            PresenceInfo info = pm.getPresenceInfo(msg.getCallee().callID);

            logger.fine("GOT JOIN ACCEPTED FOR " + msg.getCallee() + " info " + info);

            if (info == null) {
                info = msg.getCallee();

                logger.warning("adding pm for " + info);
                pm.addPresenceInfo(info);
            }

            if (msg.getChatType() == ChatType.SECRET) {
                info.inSecretChat = true;
            } else {
                info.inSecretChat = false;
            }

            notifyMemberChangeListeners(msg.getGroup(), info, true);
        } else if (message instanceof VoiceChatHoldMessage) {
            VoiceChatHoldMessage msg = (VoiceChatHoldMessage) message;
        } else if (message instanceof VoiceChatLeaveMessage) {
            VoiceChatLeaveMessage msg = (VoiceChatLeaveMessage) message;

            logger.info("GOT LEAVE MESSAGE FOR " + msg.getCallee());

            PresenceInfo info = pm.getPresenceInfo(msg.getCallee().callID);

            notifyMemberChangeListeners(msg.getGroup(), info, false);
        } else if (message instanceof CallEndedResponseMessage) {
            CallEndedResponseMessage msg = (CallEndedResponseMessage) message;

            PresenceInfo info = msg.getPresenceInfo();

            String reason = msg.getReasonCallEnded();

            logger.warning("Call ended for " + info + " Reason:  " + reason);

            if (reason.equalsIgnoreCase("Hung up") == false) {
                new CallStatusJFrame(reason);
            }

	    if (info.clientID == null) {
                pm.removePresenceInfo(info);	// it's an outworlder
	    }

            notifyMemberChangeListeners(msg.getGroup(), info, false);
        } else if (message instanceof ConeOfSilenceEnterExitMessage) {
            ConeOfSilenceEnterExitMessage msg = (ConeOfSilenceEnterExitMessage) message;

            pm.setEnteredConeOfSilence(presenceInfo, msg.entered());

            PresenceInfo info = pm.getPresenceInfo(msg.getCallID());

            if (info == null) {
                logger.warning("No presence info for " + msg.getCallID());
                return;
            }

            AvatarNameEvent avatarNameEvent;

            if (msg.entered()) {
                avatarNameEvent = new AvatarNameEvent(EventType.ENTERED_CONE_OF_SILENCE,
                        info.userID.getUsername(), info.usernameAlias);
            } else {
                avatarNameEvent = new AvatarNameEvent(EventType.EXITED_CONE_OF_SILENCE,
                        info.userID.getUsername(), info.usernameAlias);
            }

            InputManager.inputManager().postEvent(avatarNameEvent);
        } else if (message instanceof PlayerInRangeMessage) {
            PlayerInRangeMessage msg = (PlayerInRangeMessage) message;

            logger.info("Player in range " + msg.isInRange() + " " + msg.getPlayerID() + " player in range " + msg.getPlayerInRangeID());
        } else if (message instanceof AudioParticipantSpeakingMessage) {
            AudioParticipantSpeakingMessage msg = (AudioParticipantSpeakingMessage) message;

            PresenceInfo info = pm.getPresenceInfo(msg.getCellID());

            if (info == null) {
                logger.warning("No presence info for " + msg.getCellID());
                return;
            }

            logger.fine("Speaking " + msg.isSpeaking() + " " + info);

            pm.setSpeaking(info, msg.isSpeaking());

            AvatarNameEvent avatarNameEvent;

            if (msg.isSpeaking()) {
                avatarNameEvent = new AvatarNameEvent(EventType.STARTED_SPEAKING,
                        info.userID.getUsername(), info.usernameAlias);
            } else {
                avatarNameEvent = new AvatarNameEvent(EventType.STOPPED_SPEAKING,
                        info.userID.getUsername(), info.usernameAlias);
            }

            InputManager.inputManager().postEvent(avatarNameEvent);
        } else if (message instanceof AudioParticipantCallEndedMessage) {
            AudioParticipantCallEndedMessage msg = (AudioParticipantCallEndedMessage) message;

            PresenceInfo info = pm.getPresenceInfo(msg.getCellID());
        } else {
            logger.warning("Unknown message " + message);

            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public ConnectionType getConnectionType() {
        return AudioManagerConnectionType.CONNECTION_TYPE;
    }

}
