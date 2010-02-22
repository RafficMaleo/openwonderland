/*
 * Project Wonderland
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * A panel for changing the displayed alias for a user.
 *
 * @author jp
 * @author nsimpson
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class ChangeNameHUDPanel extends javax.swing.JPanel {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
    private UsernameAliasChangeListener listener;
    private PresenceManager pm;
    private PresenceInfo presenceInfo;
    private PropertyChangeSupport listeners;

    public ChangeNameHUDPanel() {
        initComponents();
    }

    public ChangeNameHUDPanel(UsernameAliasChangeListener listener,
            PresenceManager pm, PresenceInfo presenceInfo) {

        this();
        this.listener = listener;
        this.pm = pm;
        this.presenceInfo = presenceInfo;
        String text = BUNDLE.getString("Change_Alias_For");
        text = MessageFormat.format(text, presenceInfo.getUserID().getUsername());
        aliasLabel.setText(text);
        usernameAliasTextField.setText(presenceInfo.getUserID().getUsername());
        setVisible(true);
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
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

        nameLabel = new javax.swing.JLabel();
        usernameAliasTextField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        aliasLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        nameLabel.setFont(nameLabel.getFont());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        nameLabel.setText(bundle.getString("ChangeNameHUDPanel.nameLabel.text")); // NOI18N

        usernameAliasTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameAliasTextFieldActionPerformed(evt);
            }
        });
        usernameAliasTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                usernameAliasTextFieldKeyReleased(evt);
            }
        });

        cancelButton.setText(bundle.getString("ChangeNameHUDPanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(bundle.getString("ChangeNameHUDPanel.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        aliasLabel.setFont(aliasLabel.getFont().deriveFont(aliasLabel.getFont().getStyle() | java.awt.Font.BOLD));
        aliasLabel.setText(bundle.getString("ChangeNameHUDPanel.aliasLabel.text")); // NOI18N

        statusLabel.setFont(statusLabel.getFont());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(aliasLabel)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(nameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(usernameAliasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(241, Short.MAX_VALUE)
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(okButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(aliasLabel)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameAliasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void usernameAliasTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameAliasTextFieldActionPerformed
        okButton.doClick();
}//GEN-LAST:event_usernameAliasTextFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        PresenceInfo[] info = pm.getAllUsers();

        String alias = usernameAliasTextField.getText();

        for (int i = 0; i < info.length; i++) {
            if (info[i].getUsernameAlias().equals(alias) ||
                    info[i].getUserID().getUsername().equals(alias)) {

                if (!presenceInfo.equals(info[i])) {
                    statusLabel.setText(BUNDLE.getString("Alias_Used"));
                    return;
                }
            }
        }

        statusLabel.setText("");

        presenceInfo.setUsernameAlias(usernameAliasTextField.getText());
        pm.requestChangeUsernameAlias(presenceInfo.getUsernameAlias());
        listener.changeUsernameAlias(presenceInfo);
        listeners.firePropertyChange("ok", new String(""), null);
}//GEN-LAST:event_okButtonActionPerformed

    private void usernameAliasTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameAliasTextFieldKeyReleased
        okButton.setEnabled(usernameAliasTextField.getText().length() != 0);
    }//GEN-LAST:event_usernameAliasTextFieldKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aliasLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField usernameAliasTextField;
    // End of variables declaration//GEN-END:variables
}
