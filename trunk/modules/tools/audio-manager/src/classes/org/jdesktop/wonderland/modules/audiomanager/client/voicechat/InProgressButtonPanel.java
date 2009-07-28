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

import java.awt.event.ActionListener;

/**
 *
 * @author nsimpson
 */
public class InProgressButtonPanel extends javax.swing.JPanel {

    public InProgressButtonPanel() {
        initComponents();
    }

    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void removeAddButtonListener(ActionListener listener) {
        addButton.removeActionListener(listener);
    }

    public void addHangUpButtonListener(ActionListener listener) {
        hangUpButton.addActionListener(listener);
    }

    public void removeHangUpButtonListener(ActionListener listener) {
        hangUpButton.removeActionListener(listener);
    }

    public void addHoldButtonListener(ActionListener listener) {
        holdButton.addActionListener(listener);
    }

    public void removeHoldButtonListener(ActionListener listener) {
        holdButton.removeActionListener(listener);
    }

    public void addLeaveButtonListener(ActionListener listener) {
        leaveButton.addActionListener(listener);
    }

    public void removeLeaveButtonListener(ActionListener listener) {
        leaveButton.removeActionListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        hangUpButton = new javax.swing.JButton();
        holdButton = new javax.swing.JButton();
        leaveButton = new javax.swing.JButton();

        addButton.setText("Add...");
        addButton.setName("addButton"); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        hangUpButton.setText("Hang Up");
        hangUpButton.setName("hangUpButton"); // NOI18N
        hangUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hangUpButtonActionPerformed(evt);
            }
        });

        holdButton.setText("Hold");
        holdButton.setName("holdButton"); // NOI18N
        holdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdButtonActionPerformed(evt);
            }
        });

        leaveButton.setText("Leave");
        leaveButton.setName("leaveButton"); // NOI18N
        leaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addButton)
                    .addComponent(hangUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 92, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(holdButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leaveButton)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, hangUpButton, holdButton, leaveButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hangUpButton)
                    .addComponent(holdButton)
                    .addComponent(leaveButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void holdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_holdButtonActionPerformed

    private void hangUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hangUpButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hangUpButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed

    private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_leaveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton hangUpButton;
    private javax.swing.JButton holdButton;
    private javax.swing.JButton leaveButton;
    // End of variables declaration//GEN-END:variables
}