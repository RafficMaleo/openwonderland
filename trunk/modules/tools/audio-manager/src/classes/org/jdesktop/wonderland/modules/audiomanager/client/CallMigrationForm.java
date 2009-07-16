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

import java.io.IOException;

import java.util.logging.Logger;

/**
 *
 * @author  jp
 */
public class CallMigrationForm extends javax.swing.JFrame implements DisconnectListener {

    private static final Logger logger = Logger.getLogger(CallMigrationForm.class.getName());
    private static CallMigrationForm callMigrationForm;

    private AudioManagerClient client;

    public CallMigrationForm(AudioManagerClient client) { 
	this.client = client;

        initComponents();

	client.addDisconnectListener(this);

        transferButton.setText("Transfer");
        transferButton.setEnabled(false);
        transferButton.setSelected(true);
        phoneNumberTextField.setText("");
        phoneNumberTextField.setEnabled(true);
        phoneTransferRadioButton.setSelected(true);
        callStatusLabel.setText("");
    }

    private CallMigrationForm() {
        initComponents();
    }

    public void disconnected() {
	setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radioButtonGroup1 = new javax.swing.ButtonGroup();
        phoneNumberTextField = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        transferButton = new javax.swing.JButton();
        callStatusLabel = new javax.swing.JLabel();
        phoneTransferRadioButton = new javax.swing.JRadioButton();
        connectSoftphoneRadioButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();

        setTitle("Transfer Call");
        setResizable(false);

        phoneNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                phoneNumberTextFieldKeyTyped(evt);
            }
        });

        closeButton.setFont(closeButton.getFont());
        closeButton.setText("Close");
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeButtonMouseClicked(evt);
            }
        });

        transferButton.setFont(transferButton.getFont());
        transferButton.setText("Transfer");
        transferButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferButtonActionPerformed(evt);
            }
        });

        callStatusLabel.setFont(callStatusLabel.getFont());
        callStatusLabel.setText("call status");

        radioButtonGroup1.add(phoneTransferRadioButton);
        phoneTransferRadioButton.setFont(phoneTransferRadioButton.getFont());
        phoneTransferRadioButton.setSelected(true);
        phoneTransferRadioButton.setText("Phone #:");
        phoneTransferRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneTransferRadioButtonActionPerformed(evt);
            }
        });

        radioButtonGroup1.add(connectSoftphoneRadioButton);
        connectSoftphoneRadioButton.setFont(connectSoftphoneRadioButton.getFont());
        connectSoftphoneRadioButton.setText("Softphone");
        connectSoftphoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectSoftphoneRadioButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setText("Transfer call to:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(connectSoftphoneRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                                .add(closeButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(transferButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(phoneTransferRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))))
                    .add(callStatusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 228, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phoneTransferRadioButton)
                    .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(connectSoftphoneRadioButton))
                    .add(layout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(closeButton)
                            .add(transferButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(9, 9, 9)
                .add(callStatusLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void phoneNumberTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldKeyTyped
        setTransferButtonState();
    }//GEN-LAST:event_phoneNumberTextFieldKeyTyped

    private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseClicked
        callStatusLabel.setText("");
        setVisible(false);
}//GEN-LAST:event_closeButtonMouseClicked

    private void setTransferButtonState() {
        if (connectSoftphoneRadioButton.isSelected()) {
            transferButton.setEnabled(true);
	    transferButton.setText("Transfer");
        } else {
            phoneNumberTextField.setEnabled(true);

            String text = phoneNumberTextField.getText();

            if (text != null && text.length() > 0) {
                transferButton.setEnabled(true);
                getRootPane().setDefaultButton(transferButton);
            } else {
                transferButton.setEnabled(false);
            }
        }
    }

    private void transferButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferButtonActionPerformed
        if (transferButton.getText().equals("Transfer")) {
            callStatusLabel.setText("Transferring call...");

            if (connectSoftphoneRadioButton.isSelected()) {
                client.reconnectSoftphone();
                callStatusLabel.setText("");
            } else {
                transferButton.setText("Cancel");
                phoneNumberTextField.setEnabled(false);

                client.transferCall(phoneNumberTextField.getText());
            }
        } else {
            transferButton.setText("Transfer");
            phoneNumberTextField.setEnabled(true);
            callStatusLabel.setText("");
            client.cancelCallTransfer();
        }

}//GEN-LAST:event_transferButtonActionPerformed

private void connectSoftphoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectSoftphoneRadioButtonActionPerformed
    phoneNumberTextField.setEnabled(false);
    transferButton.setEnabled(true);
    transferButton.setText("Transfer");
    callStatusLabel.setText("");
}//GEN-LAST:event_connectSoftphoneRadioButtonActionPerformed

private void phoneTransferRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneTransferRadioButtonActionPerformed
    phoneNumberTextField.setEnabled(true);
    setTransferButtonState();
    callStatusLabel.setText("");
}//GEN-LAST:event_phoneTransferRadioButtonActionPerformed
    
    public void setStatus(String status) {
	callStatusLabel.setText(status);
	transferButton.setText("Transfer");
	setTransferButtonState();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel callStatusLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JRadioButton connectSoftphoneRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField phoneNumberTextField;
    private javax.swing.JRadioButton phoneTransferRadioButton;
    private javax.swing.ButtonGroup radioButtonGroup1;
    private javax.swing.JButton transferButton;
    // End of variables declaration//GEN-END:variables
}
