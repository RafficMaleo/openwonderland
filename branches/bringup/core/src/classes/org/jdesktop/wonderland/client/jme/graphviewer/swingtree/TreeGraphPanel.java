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

package org.jdesktop.wonderland.client.jme.graphviewer.swingtree;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.wonderland.client.jme.graphviewer.NodeViewer;

/**
 *
 * @author  paulby
 */
public class TreeGraphPanel extends javax.swing.JPanel {
    
    private TreeModel model;
    private TreePath currentTreeSelection=null;
    private int mouseOverTreeRow = 0;  // The row in the tree the mouse was over when a button was clicked
    private NodeViewer nodeViewer = null;
    
    /** Creates new form TreeGraphPanel */
    public TreeGraphPanel(TreeModel model) {
        this.model = model;
        
        initComponents();
        setupTreeCellRenderer();
        jmeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        jmeTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                System.err.println("Selected "+e.getPath().getLastPathComponent());
                currentTreeSelection=e.getPath();
                if (nodeViewer!=null) {
                    if (currentTreeSelection.getLastPathComponent() instanceof SceneElement)
                        nodeViewer.setNode((SceneElement)currentTreeSelection.getLastPathComponent()); 
                    else
                        nodeViewer.setNode(null);
                }
                    
                    
            }
            
        });        
    }

    void setNodeViewer(NodeViewer nodeViewer) {
        this.nodeViewer = nodeViewer;
    }
    
    /**
     * Setup the JTree renderer to handle JME nodes
     */
    private void setupTreeCellRenderer() {
        DefaultTreeCellRenderer jmeTreeRenderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, 
                                                          Object value, 
                                                          boolean sel, 
                                                          boolean expanded, 
                                                          boolean leaf, 
                                                          int row, 
                                                          boolean hasFocus) {
                Component ret = super.getTreeCellRendererComponent(tree, 
                                                                   value, 
                                                                   sel, 
                                                                   expanded, 
                                                                   leaf, 
                                                                   row, 
                                                                   hasFocus);
                if (ret instanceof JLabel) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(trimClassName(value.getClass()));
                    if (value instanceof SceneElement) {
                        String nodeName = ((SceneElement)value).getName();
                        if (nodeName!=null)
                            buf.append(":"+nodeName);
                    }
                    ((JLabel)ret).setText(buf.toString());
                }
                return ret;
            }
        };
        jmeTree.setCellRenderer(jmeTreeRenderer);
    }
    
    /**
     * Return the class name without the leading package name
     * @param clazz
     * @return
     */
    private String trimClassName(Class clazz) {
        String ret = clazz.getName();
        return ret.substring(ret.lastIndexOf('.')+1);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treePopupMenu = new javax.swing.JPopupMenu();
        expandTreeMI = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jmeTree = new javax.swing.JTree();

        expandTreeMI.setText("Expand Tree");
        expandTreeMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandTreeMIActionPerformed(evt);
            }
        });
        treePopupMenu.add(expandTreeMI);

        jmeTree.setModel(model);
        jmeTree.setRootVisible(false);
        jmeTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jmeTreeMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jmeTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jmeTree);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 337, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 333, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 396, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 392, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void expandTreeMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandTreeMIActionPerformed
        TreePath path = jmeTree.getPathForRow(mouseOverTreeRow);
        
        if (path.getLastPathComponent() instanceof SceneElement)
            expandChildren(path);
        
}//GEN-LAST:event_expandTreeMIActionPerformed

    /**
     * Make sure all children (recursively) of the given path are visible
     * @param path
     */
    private void expandChildren(TreePath path) {
        SceneElement s = (SceneElement) path.getLastPathComponent();
        if (s instanceof Node) {
            Node n = (Node)s;
            ArrayList<Spatial> children = n.getChildren();
            if (children==null || children.size()==0) {
                jmeTree.makeVisible(path);
            } else {
                for(Spatial child : children) {
                    TreePath childPath = path.pathByAddingChild(child);
                    expandChildren(childPath);
                }
            }
        } else {
            jmeTree.makeVisible(path);
        }
    }
    
    private void jmeTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jmeTreeMouseClicked

    }//GEN-LAST:event_jmeTreeMouseClicked

    private void jmeTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jmeTreeMousePressed
        mouseOverTreeRow = jmeTree.getRowForLocation(evt.getX(), evt.getY());
        if (evt.isPopupTrigger()) {
            treePopupMenu.show(jmeTree, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jmeTreeMousePressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem expandTreeMI;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jmeTree;
    private javax.swing.JPopupMenu treePopupMenu;
    // End of variables declaration//GEN-END:variables
    
}