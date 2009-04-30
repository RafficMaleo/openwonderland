package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.client.*;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;


import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;



import org.jdesktop.wonderland.client.comms.WonderlandSession;


import org.jdesktop.wonderland.common.cell.CellID;

import java.util.ArrayList;

import java.util.logging.Logger;

/*
 * AddUserDialog.java
 *
 * Created on April 22, 2009, 8:25 AM
 */
/**
 *
 * @author  jp
 */
public class AddUserDialog extends javax.swing.JFrame implements PresenceManagerListener,
        MemberChangeListener {

    private static final Logger logger =
            Logger.getLogger(AddUserDialog.class.getName());
    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;
    private String group;
    private InCallDialog inCallDialog;

    /** Creates new form AddUserDialog */
    public AddUserDialog() {
        initComponents();
    }

    public AddUserDialog(AudioManagerClient client, WonderlandSession session,
            CellID cellID, String group, InCallDialog inCallDialog) {

        this.client = client;
        this.session = session;
        this.group = group;
        this.inCallDialog = inCallDialog;

        initComponents();

        inCallDialog.addMemberChangeListener(this);

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

        presenceInfo = pm.getPresenceInfo(cellID);

        if (presenceInfo == null) {
            logger.warning("No Presence info for cell " + cellID);
            return;
        }

        setUserList();
        setVisible(true);
    }

    private void setUserList() {
        ArrayList<PresenceInfo> members = inCallDialog.getMembers();

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> userData = new ArrayList();

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

            if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

            if (members.contains(info)) {
                continue;
            }

            userData.add(NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                    info.isMuted));
        }

        userList.setListData(userData.toArray(new String[0]));
    }

    public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
        setUserList();
    }

    public void aliasChanged(String previousAlias, PresenceInfo info) {
        setUserList();
    }

    public void memberAdded(PresenceInfo info) {
        setUserList();
    }

    public void memberRemoved(PresenceInfo info) {
        setUserList();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        jLabel1.setText("Ask User to Join");

        userList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(userList);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(77, 77, 77)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(jLabel1)
                .add(31, 31, 31)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
        PresenceInfo[] info = pm.getAliasPresenceInfo((String) selectedValues[i]);

        session.send(client, new VoiceChatJoinMessage(group, presenceInfo, info, ChatType.PRIVATE));
    }

    userList.clearSelection();
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    userList.clearSelection();
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AddUserDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables
}
