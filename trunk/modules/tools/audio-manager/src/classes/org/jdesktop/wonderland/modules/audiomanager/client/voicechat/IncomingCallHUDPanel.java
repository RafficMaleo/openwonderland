/*
 * IncomingCallHUDPanel.java
 *
 * Created on July 19, 2009, 11:53 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client.voicechat;

import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;

import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel.Mode;

import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinAcceptedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author  jp
 */
public class IncomingCallHUDPanel extends javax.swing.JPanel {
    
    private static final Logger logger =
            Logger.getLogger(IncomingCallHUDPanel.class.getName());

    private ChatType chatType = ChatType.PRIVATE;
    private AudioManagerClient client;
    private WonderlandSession session;
    private CellID cellID;
    private String group;
    private PresenceInfo caller;
    private PresenceInfo myPresenceInfo;

    private HUDComponent incomingCallHUDComponent;

    private boolean answered;

    /** Creates new form IncomingCallHUDPanel */
    public IncomingCallHUDPanel(AudioManagerClient client, WonderlandSession session,
            CellID cellID, VoiceChatJoinRequestMessage message) {

        initComponents();

        this.client = client;
        this.cellID = cellID;
        this.session = session;

        initComponents();

        group = message.getGroup();

        caller = message.getCaller();

        callerText.setText(caller.usernameAlias);

        PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);

        myPresenceInfo = pm.getPresenceInfo(cellID);

	privacyDescription.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
    }
    
    public void setHUDComponent(HUDComponent incomingCallHUDComponent) {
	this.incomingCallHUDComponent = incomingCallHUDComponent;
    }

    public void busy() {
	if (answered) {
	    return;
	}

        session.send(client, new VoiceChatBusyMessage(group, caller, myPresenceInfo, chatType));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        callerLabel = new javax.swing.JLabel();
        callerText = new javax.swing.JLabel();
        privateRadioButton = new javax.swing.JRadioButton();
        secretRadioButton = new javax.swing.JRadioButton();
        speakerPhoneRadioButton = new javax.swing.JRadioButton();
        privacyDescription = new javax.swing.JLabel();
        BusyButton = new javax.swing.JButton();
        AnswerButton = new javax.swing.JButton();

        callerLabel.setFont(callerLabel.getFont().deriveFont(callerLabel.getFont().getStyle() | java.awt.Font.BOLD));
        callerLabel.setText("Incoming call from:");

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setFont(privateRadioButton.getFont());
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setFont(secretRadioButton.getFont());
        secretRadioButton.setText("Secret");
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(speakerPhoneRadioButton);
        speakerPhoneRadioButton.setFont(speakerPhoneRadioButton.getFont());
        speakerPhoneRadioButton.setText("SpeakerPhone");
        speakerPhoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerPhoneRadioButtonActionPerformed(evt);
            }
        });

        BusyButton.setText("Busy");
        BusyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BusyButtonActionPerformed(evt);
            }
        });

        AnswerButton.setText("Answer");
        AnswerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AnswerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(BusyButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(AnswerButton))
                    .add(layout.createSequentialGroup()
                        .add(privateRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(secretRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(speakerPhoneRadioButton))
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(privacyDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(callerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(callerText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(callerText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(callerLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(privateRadioButton)
                    .add(secretRadioButton)
                    .add(speakerPhoneRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(privacyDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(AnswerButton)
                    .add(BusyButton))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {callerText, privacyDescription}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
	chatType = ChatType.SECRET;
	privacyDescription.setText(VoiceChatMessage.SECRET_DESCRIPTION);
}//GEN-LAST:event_secretRadioButtonActionPerformed

    private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
	chatType = ChatType.PRIVATE;
	privacyDescription.setText(VoiceChatMessage.PUBLIC_DESCRIPTION);
}//GEN-LAST:event_privateRadioButtonActionPerformed

    private void speakerPhoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerPhoneRadioButtonActionPerformed
	chatType = ChatType.PUBLIC;
}//GEN-LAST:event_speakerPhoneRadioButtonActionPerformed

    private void BusyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BusyButtonActionPerformed
	answered = true;
        session.send(client, new VoiceChatBusyMessage(group, caller, myPresenceInfo, chatType));

        incomingCallHUDComponent.setVisible(false);
    }//GEN-LAST:event_BusyButtonActionPerformed

    private HUDComponent addComponent;

    private void AnswerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AnswerButtonActionPerformed
        logger.info("Sent join accepted message");

        AddHUDPanel addHUDPanel = new AddHUDPanel(client, session, myPresenceInfo, caller, group, Mode.IN_PROGRESS);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        addComponent = mainHUD.createComponent(addHUDPanel);
        addComponent.setIcon(new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListChatVoice32x32.png")));
	addComponent.setName("Voice Chat");

        addHUDPanel.setHUDComponent(addComponent);

        addHUDPanel.setPreferredLocation(Layout.NORTHWEST);

        mainHUD.addComponent(addComponent);

        addComponent.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent e) {
                if (e.getEventType().equals(HUDEventType.CLOSED)) {
		    addComponent = null;
                }
            }
        });

        addComponent.setVisible(true);

        session.send(client, new VoiceChatJoinAcceptedMessage(group, myPresenceInfo, chatType));

	answered = true;

        incomingCallHUDComponent.setVisible(false);

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
            client.getWlAvatarCharacter().playAnimation(answerCell);
        } else {
            client.getWlAvatarCharacter().stop();
        }
    }//GEN-LAST:event_AnswerButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AnswerButton;
    private javax.swing.JButton BusyButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel callerLabel;
    private javax.swing.JLabel callerText;
    private javax.swing.JLabel privacyDescription;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JRadioButton speakerPhoneRadioButton;
    // End of variables declaration//GEN-END:variables
    
}
