/*
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
package org.jdesktop.wonderland.modules.audiomanager.client.voicechat;

import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
import org.jdesktop.wonderland.modules.audiomanager.client.MemberChangeListener;
import org.jdesktop.wonderland.modules.audiomanager.client.UserInRangeListener;

import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel.Mode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetPlayersInRangeRequestMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.EndCallMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatDialOutMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author nsimpson
 */
public class AddUserPanel extends javax.swing.JPanel implements 
	 PresenceManagerListener, MemberChangeListener, UserInRangeListener {

    private static final Logger logger = Logger.getLogger(AddUserPanel.class.getName());

    private static final String BYSTANDER_SYMBOL = "\u25B8 ";

    private AddHUDPanel addHUDPanel;
    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;
    private PresenceInfo caller;
    private String group;

    private ChatType chatType = ChatType.PRIVATE;

    private DefaultListModel userListModel;

    private PrivacyPanel privacyPanel;

    private boolean personalPhone;

    public AddUserPanel(AddHUDPanel addHUDPanel, AudioManagerClient client, WonderlandSession session,
            PresenceInfo myPresenceInfo, PresenceInfo caller, String group) {

	this.addHUDPanel = addHUDPanel;
	this.client = client;
	this.session = session;
	this.myPresenceInfo = myPresenceInfo;
	this.caller = caller;
	this.group = group;

        initComponents();

	userListModel = new DefaultListModel();
        addUserList.setModel(userListModel);
	addUserList.setCellRenderer(new UserListCellRenderer());
	
        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

	client.addMemberChangeListener(group, this);

	client.addUserInRangeListener(this);

	privacyPanel = new PrivacyPanel();

	privacyPanel.addSecretRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		secretButtonActionPerformed(e);
	    }
	});

	privacyPanel.addPrivateRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		privateButtonActionPerformed(e);
	    }
	});

	privacyPanel.addPublicRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		publicButtonActionPerformed(e);
	    }
	});

        addUserDetailsPanel.add(privacyPanel, BorderLayout.CENTER);

        validate();

	/*
	 * Ask for group members
	 */
	session.send(client, new VoiceChatInfoRequestMessage(group));

	/*
	 * Ask for users in range
	 */
	session.send(client, new GetPlayersInRangeRequestMessage(
	    myPresenceInfo.callID));
    }

    public void addUserListSelectionListener(javax.swing.event.ListSelectionListener listener) {
        addUserList.addListSelectionListener(listener);
    }

    public void removeUserListSelectionListener(javax.swing.event.ListSelectionListener listener) {
        addUserList.removeListSelectionListener(listener);
    }

    public void setVisible(boolean isVisible) {
	super.setVisible(isVisible);

	if (isVisible == false) {
	    return;
	}

	updateUserList();
    }

    private void updateUserList() {
	Mode mode = addHUDPanel.getMode();

	if (mode.equals(Mode.ADD) || mode.equals(Mode.INITIATE)) {
	    addNonMembers();
	} else if (mode.equals(Mode.IN_PROGRESS)) {
	    addMembers();
	}
    }

    private void addNonMembers() {
	clearUserList();

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

	for (int i = 0; i < presenceInfoList.length; i++) {
	    PresenceInfo info = presenceInfoList[i];

	    if (info.callID == null) {
                // It's a virtual player, skip it.
		continue;
            }

	    synchronized (members) {
	        if (members.contains(info) || invitedMembers.contains(info)) {
                    removeFromUserList(info);
                } else {
                    addToUserList(info);
                }
	    }
	}
    }

    private void addMembers() {
	clearUserList();

	addToUserList(myPresenceInfo);

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

	for (int i = 0; i < presenceInfoList.length; i++) {
	    PresenceInfo info = presenceInfoList[i];

	    if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

	    synchronized (members) {
	        synchronized (invitedMembers) {
	            if (members.contains(info)) {
	    	        if (info.equals(myPresenceInfo) == false) {
                            addToUserList(info);
		        }

		        addBystanders(info);  // add bystanders
		    } else if (invitedMembers.contains(info)) {
	    	        if (info.equals(myPresenceInfo) == false) {
                            addToUserList(info);
		        }
		    }
		}
	    }
	}
    }

    private void addBystanders(PresenceInfo member) {
	logger.fine("Add bystanders for " + member);

	if (chatType.equals(ChatType.PUBLIC) == false) {
	    logger.fine("Chat type not public");
	    return;
	}

	CopyOnWriteArrayList<PresenceInfo> bystanders = usersInRangeMap.get(member.userID.getUsername());

	if (bystanders == null) {
	    logger.fine("No bystanders");
	    dumpu();
	    return;
	}
	    
	for (PresenceInfo bystander : bystanders) {
	    if (members.contains(bystander)) {
		logger.fine("bystander is a member " + bystander);
		continue;
	    }

	    addToUserList(bystander, true);
	}
    }

    public void secretButtonActionPerformed(ActionEvent e) {
	chatType = chatType.SECRET;

	if (addHUDPanel.getMode().equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    public void privateButtonActionPerformed(ActionEvent e) {
	chatType = chatType.PRIVATE;

	if (addHUDPanel.getMode().equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    public void publicButtonActionPerformed(ActionEvent e) {
	chatType = chatType.PUBLIC;

	if (addHUDPanel.getMode().equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    private void changePrivacy() {
	ArrayList<PresenceInfo> users = getSelectedValues();

	animateCallAnswer();

        if (users.contains(myPresenceInfo) == false) {
            session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo, 
		new PresenceInfo[0], chatType));
        }

        for (PresenceInfo info : users) {
            /*
             * You can only select yourself or outworlders
             */
            if (info.clientID != null) {
                continue;
            }

            session.send(client, new VoiceChatJoinMessage(group, info, 
		new PresenceInfo[0], chatType));
        }
    }

    public void showPrivacyPanel(boolean showPrivacy) {
        addUserDetailsPanel.setVisible(showPrivacy);
    }

    public void callUser(String name, String number) {
        personalPhone = true;

	animateCallAnswer();

        session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
            new PresenceInfo[0], chatType));

        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        String callID = sc.getCallID();

        PresenceInfo presenceInfo = new PresenceInfo(null, null, 
	    new WonderlandIdentity(name, name, null), callID);

        pm.addPresenceInfo(presenceInfo);

	updateUserList();
        session.send(client, new VoiceChatDialOutMessage(group, callID, chatType, presenceInfo, number));
    }

    private void animateCallAnswer() {
	if (true) {
	    return;
	}

	WlAvatarCharacter avatar = client.getWlAvatarCharacter();

	if (avatar == null) {
	    return;
	}

	String answerCell = null;

	for (String action : avatar.getAnimationNames()) {
	    if (action.indexOf("_AnswerCell") > 0) {
		answerCell = action;
		break;
	    }
	}

	if (answerCell == null) {
	    return;
	}

	if (chatType.equals(ChatType.PRIVATE)) {
	    avatar.playAnimation(answerCell);
	    logger.warning("Playing animation...");
	} else {
	    avatar.stop();
	    logger.warning("Stopping animation...");
	}
    }

    public void inviteUsers() {
	ArrayList<PresenceInfo> usersToInvite = getSelectedValues();
	usersToInvite.remove(myPresenceInfo);
	inviteUsers(usersToInvite);
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
	clearUserList();

	animateCallAnswer();

        for (PresenceInfo info : usersToInvite) {
	    synchronized (invitedMembers) {
                invitedMembers.remove(info);
                invitedMembers.add(info);
		logger.warning("Sending invite to " + info);
	    }
	}

	updateUserList();
        session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
            usersToInvite.toArray(new PresenceInfo[0]), chatType));
    }


    public ArrayList<PresenceInfo> getSelectedValues() {
	Object[] selectedValues = addUserList.getSelectedValues();

	ArrayList<PresenceInfo> usersToInvite = new ArrayList();

	if (selectedValues.length == 0) {
	    return new ArrayList<PresenceInfo>();
        }

	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

            usersToInvite.add(info);
        }

	return usersToInvite;
    }

    private ConcurrentHashMap<String, String> usernameMap = new ConcurrentHashMap();

    private void clearUserList() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		clearUserListLater();
            }
        });
    }

    private void clearUserListLater() {
	userListModel.clear();
	usernameMap.clear();
    }

    private void addElement(final PresenceInfo info, final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		addElementLater(info, usernameAlias);
            }
        });
    }

    private void addElementLater(PresenceInfo info, String usernameAlias) {
	//userListModel.removeElement(usernameAlias);
        userListModel.addElement(usernameAlias);
	usernameMap.put(info.userID.getUsername(), usernameAlias);
	//dump("addElement later size " + userListModel.size() + " " 
	//    + usernameAlias);
    }

    private void removeElement(final PresenceInfo info, final String usernameAlias) {
	//new Exception("removed " + info.userID.getUsername() + " mode " + addHUDPanel.getMode()).printStackTrace();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		removeElementLater(info, usernameAlias);
            }
        });
    }

    private void removeElementLater(PresenceInfo info, String usernameAlias) {
	userListModel.removeElement(usernameAlias);
	usernameMap.remove(info.userID.getUsername());
    }

    private void addToUserList(final PresenceInfo info) {
	addToUserList(info, false);
    }

    private void addToUserList(final PresenceInfo info, final boolean isBystander) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		addToUserListLater(info, isBystander);
            }
        });
    }

    private void addToUserListLater(PresenceInfo info, boolean isBystander) {
        removeFromUserListLater(info);

        String displayName = NameTagNode.getDisplayName(info.usernameAlias,
                info.isSpeaking, info.isMuted);

	if (isBystander) {
	    displayName = BYSTANDER_SYMBOL + displayName;

	    logger.fine("Adding bystander " + displayName + " FOR " + info);
	}

        addElementLater(info, displayName);
    }

    private void removeFromUserList(final PresenceInfo info) {
	//new Exception("removed " + info.userID.getUsername() + " mode " + addHUDPanel.getMode()).printStackTrace();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		removeFromUserListLater(info);
            }
        });
    }

    private void removeFromUserListLater(PresenceInfo info) {
        String name = NameTagNode.getDisplayName(info.usernameAlias, false, false);
        removeElementLater(info, name);

	name = BYSTANDER_SYMBOL + name;
        removeElementLater(info, name);

        name = NameTagNode.getDisplayName(info.usernameAlias, false, true);
        removeElementLater(info, name);

	name = BYSTANDER_SYMBOL + name;
        removeElementLater(info, name);

        name = NameTagNode.getDisplayName(info.usernameAlias, true, false);
        removeElementLater(info, name);

	name = BYSTANDER_SYMBOL + name;
        removeElementLater(info, name);
    }

    private void setElementAt(PresenceInfo info, String displayName, int ix) {
	setElementAt(displayName, ix);
	usernameMap.put(info.userID.getUsername(), displayName);
    }

    private void dump(String s) {
	System.out.println("======");
	System.out.println(s);
	
	for (int i = 0; i < userListModel.size(); i++) {
	    System.out.println((String) userListModel.getElementAt(i));
	}

	System.out.println("======");
    }

    private void setElementAt(String displayName, int ix) {
	if (ix < userListModel.size()) {
	    userListModel.setElementAt(displayName, ix);
	    //dump("Set at " + ix + " " + displayName);
	} else {
	    userListModel.addElement(displayName);
	    //dump("Added at " + ix + " " + displayName);
	}
    }

    private void removeElementAt(PresenceInfo info, int ix) {
	usernameMap.remove(info.userID.getUsername());
	userListModel.removeElementAt(ix);
	logger.fine("Removed element at " + ix + " " + info.userID.getUsername());
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	switch (addHUDPanel.getMode()) {
	case ADD:
	    switch (type) {
	    case USER_ADDED:
		synchronized (members) {
		    if (members.contains(presenceInfo)) {
		        removeFromUserList(presenceInfo);
		        break;
		    }
		}
		addToUserList(presenceInfo, false);
		break;

	    case USER_REMOVED:
		removeFromUserList(presenceInfo);
	        break;
	    }

	    break;

	case INITIATE:
	    switch (type) {
            case USER_ADDED:
		if (presenceInfo.equals(myPresenceInfo)) {
		    removeFromUserList(presenceInfo);
		    break;
		}

		addToUserList(presenceInfo, false);
		break;

	    case USER_REMOVED:
		removeFromUserList(presenceInfo);
		if (personalPhone) {
		    synchronized (members) {
		        if (presenceInfo.clientID == null && members.size() == 1) {
			    leave();
		        }
		    }
		}
	        break;
	    }

	    break;

	case IN_PROGRESS:
	    switch (type) {
	    case USER_ADDED:
		break;

	    case USER_REMOVED:
		removeFromInRangeMaps(presenceInfo);
		removeFromUserList(presenceInfo);
		break;

	    case UPDATED:
		updatePresenceInfo(presenceInfo);
	    }

	    break;
	}

	updateUserList();
    }

    private void updatePresenceInfo(PresenceInfo info) {
	int ix;

	if ((ix = members.indexOf(info)) >= 0) {
	    updatePresenceInfo(info, members.get(ix));
	}

	if ((ix = invitedMembers.indexOf(info)) >= 0) {
	    updatePresenceInfo(info, members.get(ix));
	}

	//dumpu();

	Collection<CopyOnWriteArrayList<PresenceInfo>> c = usersInRangeMap.values();

	Iterator<CopyOnWriteArrayList<PresenceInfo>> it = c.iterator();

	while (it.hasNext()) {
	    CopyOnWriteArrayList<PresenceInfo> usersInRange = it.next();

	    if ((ix = usersInRange.indexOf(info)) >= 0) {
	        updatePresenceInfo(info, usersInRange.get(ix));
	    }
	}
    }

    private void updatePresenceInfo(PresenceInfo source, PresenceInfo dest) {
	dest.isSpeaking = source.isSpeaking;
	dest.isMuted = source.isMuted;
	dest.inConeOfSilence = source.inConeOfSilence;
	dest.inSecretChat = source.inSecretChat;
    }

    private void removeFromInRangeMaps(PresenceInfo presenceInfo) {
	Enumeration<String> e = usersInRangeMap.keys();

	while (e.hasMoreElements()) {
	    String username = e.nextElement();

	    CopyOnWriteArrayList<PresenceInfo> usersInRange = usersInRangeMap.get(username);

	    usersInRange.remove(presenceInfo);
	}
    }
		
    private void dumpu() {
	System.out.println("+++++++++");

	Enumeration<String> e = usersInRangeMap.keys();

	while (e.hasMoreElements()) {
	    String username = e.nextElement();

	    System.out.println("In range of " + username);

	    CopyOnWriteArrayList<PresenceInfo> usersInRange = usersInRangeMap.get(username);
	    
	    for (PresenceInfo info : usersInRange) {
		System.out.println("  " + info.userID.getUsername());
	    }
	}
	
	System.out.println("+++++++++");
    }

    private CopyOnWriteArrayList<PresenceInfo> members = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<PresenceInfo> invitedMembers = new CopyOnWriteArrayList();

    public void memberChange(PresenceInfo presenceInfo, boolean added) {
	synchronized (invitedMembers) {
	    invitedMembers.remove(presenceInfo);
	}

	logger.fine("member change:  " + presenceInfo + " added " + added + " mode " + addHUDPanel.getMode());

	if (added) {
	    synchronized (members) {
	        if (members.contains(presenceInfo) == false) {
		    members.add(presenceInfo);
	        }
	    }
	} else {
	    synchronized (members) {
	        members.remove(presenceInfo);
	    }

	    synchronized (members) {
	        if (personalPhone && members.size() == 1) {
                    leave();
                }
	    }
	}

	updateUserList();
    }

    public void setMemberList(PresenceInfo[] memberList) {
	logger.fine("Set member list...");

	synchronized (invitedMembers) {
	    synchronized (members) {
	        for (int i = 0; i < memberList.length; i++) {
		    PresenceInfo info = memberList[i];

		    invitedMembers.remove(info);

		    logger.fine("Member " + info);

	            if (members.contains(info) == false) {
		        members.add(info);
			logger.fine("added " + members.size());
	            }
	        }
	    }
	}

	updateUserList();
    }

    private void leave() {
        session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
    }

    public void hangup() {
       ArrayList<PresenceInfo> membersInfo = getSelectedValues();

        for (PresenceInfo info : membersInfo) {
            if (info.clientID != null) {
                continue;
            }

            session.send(client, new EndCallMessage(info.callID, "Terminated with malice"));
        }
    }

    private boolean isMe(PresenceInfo info) {
	return myPresenceInfo.equals(info);
    }

    private ConcurrentHashMap<String, CopyOnWriteArrayList<PresenceInfo>> usersInRangeMap = 
	new ConcurrentHashMap();

    private boolean isInRange(PresenceInfo info) {
	CopyOnWriteArrayList<PresenceInfo> usersInRange = usersInRangeMap.get(myPresenceInfo.userID.getUsername());

        return isMe(info) || usersInRange.contains(info);
    }

    private boolean isInRangeOfSomebody(PresenceInfo info) {
	Collection<CopyOnWriteArrayList<PresenceInfo>> c = usersInRangeMap.values();

	Iterator<CopyOnWriteArrayList<PresenceInfo>> it = c.iterator();

	while (it.hasNext()) {
	    CopyOnWriteArrayList<PresenceInfo> usersInRange = it.next();
	    if (usersInRange.contains(info)) {
		return true;
	    }
	}

	return false;
    }

    public void userInRange(PresenceInfo info, PresenceInfo userInRange, boolean isInRange) {
	CopyOnWriteArrayList<PresenceInfo> usersInRange = usersInRangeMap.get(info.userID.getUsername());

	logger.fine("userInRange:  " + info + " userInRange " + userInRange + " inRange "
	    + isInRange);
 
	if (usersInRange == null) {
	    if (isInRange == false) {
		return;
	    }

	    usersInRange = new CopyOnWriteArrayList();
	    usersInRangeMap.put(info.userID.getUsername(), usersInRange);
	    logger.fine("ADDING NEW MAP FOR " + info);
	}

	if (isInRange) {
	    if (usersInRange.contains(userInRange)) {
		return;
	    }

	    logger.fine("Adding in RANGE:  " + userInRange + " FOR " + info);
	    usersInRange.add(userInRange);
	} else {
	    usersInRange.remove(userInRange);
	    logger.fine("Removing user out of range " + userInRange);
	}

	//dumpu();

	updateUserList();
    }

    private void remove(PresenceInfo info) {
        String username = info.userID.getUsername();

	String mapEntry = usernameMap.get(username);

	if (mapEntry == null) {
	    removeFromUserListLater(info);
	    return;
	}

	// TODO Need to remove from userInRangeMap
	int position = userListModel.indexOf(mapEntry);
        removeFromUserListLater(info);
	removeBystanders(position);
    }

    private void removeBystanders(int position) {
    }

    private class UserListCellRenderer implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        private Font font = new Font("SansSerif", Font.PLAIN, 13);

        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);

            String usernameAlias = NameTagNode.getUsername(((String) value).replace(BYSTANDER_SYMBOL, ""));

            PresenceInfo info = pm.getAliasPresenceInfo(usernameAlias);

            if (info == null) {
                logger.warning("No presence info for " + usernameAlias + " value " + value);
                return renderer;
            }

	    boolean isMember = members.contains(info);

	    // TODO if it's a member or a bystander, make it black.

	    if (isMember || addHUDPanel.getMode().equals(Mode.INITIATE) || addHUDPanel.getMode().equals(Mode.ADD)) {
                renderer.setFont(font);
                renderer.setForeground(Color.BLACK);
            } else {
                renderer.setFont(font);
                renderer.setForeground(Color.LIGHT_GRAY);
            }
            return renderer;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        addUserScrollPane = new javax.swing.JScrollPane();
        addUserList = new javax.swing.JList();
        addUserDetailsPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(0, 95));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(295, 95));

        addUserScrollPane.setMinimumSize(new java.awt.Dimension(23, 89));
        addUserScrollPane.setName("addUserScrollPane"); // NOI18N

        addUserList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        addUserList.setName("addUserList"); // NOI18N
        addUserList.setVisibleRowCount(5);
        addUserList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                addUserListValueChanged(evt);
            }
        });
        addUserScrollPane.setViewportView(addUserList);

        addUserDetailsPanel.setBackground(new java.awt.Color(0, 0, 0));
        addUserDetailsPanel.setName("addUserDetailsPanel"); // NOI18N
        addUserDetailsPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, addUserDetailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, addUserScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(addUserScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(addUserDetailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 2, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void addUserListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_addUserListValueChanged
// TODO add your handling code here:
}//GEN-LAST:event_addUserListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addUserDetailsPanel;
    private javax.swing.JList addUserList;
    private javax.swing.JScrollPane addUserScrollPane;
    private javax.swing.ButtonGroup buttonGroup1;
    // End of variables declaration//GEN-END:variables
}
