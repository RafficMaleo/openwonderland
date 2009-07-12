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
package org.jdesktop.wonderland.modules.artimport.client.jme;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * Plugin for the art tools. This is not really a JPanel, the superclass
 * is just an artifact of Netbeans. Using NB to manage this class make I18N easier
 *
 * @author paulby
 */
public class ArtToolsPlugin extends javax.swing.JPanel 
        implements ClientPlugin
{
    private ImportSessionFrame importSessionFrame = null;
    private CellViewerFrame cellViewerFrame = null; 

    /** a BaseClientPlugin that delegates the activate and deactivate methods
     *  back to this class.
     */
    private BaseClientPlugin plugin;

    /** Creates new form ArtToolsPlugin1 */
    public ArtToolsPlugin() {
        initComponents();
    }

    public void initialize(ServerSessionManager lm) {
        this.plugin = new BaseClientPlugin() {
            @Override
            protected void activate() {
                ArtToolsPlugin.this.register();
            }

            @Override
            protected void deactivate() {
                ArtToolsPlugin.this.unregister();
            }
        };
        
        plugin.initialize(lm);
    }

    public void cleanup() {
        plugin.cleanup();
    }

    public void register() {
        // activate
        JmeClientMain.getFrame().addToToolsMenu(cellViewerMI, 7);
        JmeClientMain.getFrame().addToInsertMenu(importModelMI, 1);
        JmeClientMain.getFrame().addToFileMenu(createModuleMI, 1);
    }

    public void unregister() {
        // deactivate
        JmeClientMain.getFrame().removeFromToolsMenu(cellViewerMI);
        JmeClientMain.getFrame().removeFromInsertMenu(importModelMI);
        JmeClientMain.getFrame().removeFromFileMenu(createModuleMI);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cellViewerMI = new javax.swing.JMenuItem();
        importModelMI = new javax.swing.JMenuItem();
        createModuleMI = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/artimport/client/jme/resources/Bundle"); // NOI18N
        cellViewerMI.setText(bundle.getString("Cell_Viewer...")); // NOI18N
        cellViewerMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cellViewerMIActionPerformed(evt);
            }
        });

        importModelMI.setText(bundle.getString("Import_Model...")); // NOI18N
        importModelMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importModelMIActionPerformed(evt);
            }
        });

        createModuleMI.setText(bundle.getString("Create_Module")); // NOI18N
        createModuleMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createModuleMIActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cellViewerMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cellViewerMIActionPerformed
        if (cellViewerFrame==null) {
            cellViewerFrame = new CellViewerFrame();
        }
        cellViewerFrame.setVisible(true);
}//GEN-LAST:event_cellViewerMIActionPerformed

    private void importModelMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importModelMIActionPerformed
        if (importSessionFrame==null) {
                importSessionFrame = new ImportSessionFrame();
        }
        importSessionFrame.setVisible(true);
}//GEN-LAST:event_importModelMIActionPerformed

    private void createModuleMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createModuleMIActionPerformed

        ModuleManagerUI mm = new ModuleManagerUI();
        mm.setVisible(true);

}//GEN-LAST:event_createModuleMIActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem cellViewerMI;
    private javax.swing.JMenuItem createModuleMI;
    private javax.swing.JMenuItem importModelMI;
    // End of variables declaration//GEN-END:variables

}
