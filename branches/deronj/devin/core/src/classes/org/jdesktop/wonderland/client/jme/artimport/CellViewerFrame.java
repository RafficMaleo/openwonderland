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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.RootCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author  paulby
 */
public class CellViewerFrame extends javax.swing.JFrame {

    private ArrayList<Cell> rootCells = new ArrayList();
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    private HashMap<Cell, DefaultMutableTreeNode> nodes = new HashMap();
    
    /** Creates new form CellViewerFrame */
    public CellViewerFrame(WonderlandSession session) {
        initComponents();
        CellManager.getCellManager().addCellStatusChangeListener(new CellStatusChangeListener() {

            public void cellStatusChanged(Cell cell, CellStatus status) {
                DefaultMutableTreeNode node = nodes.get(cell);
                
                switch(status) {
                    case DISK :
                        if (node!=null)
                            ((DefaultTreeModel)cellTree.getModel()).removeNodeFromParent(node);
                        break;
                    case BOUNDS :
                        DefaultMutableTreeNode parentNode = nodes.get(cell.getParent());
                        if (parentNode==null && !(cell instanceof RootCell)) {
                            System.err.println("******* Null parent "+cell.getParent());
                        } else {
                            if (node==null) {
                                node = new DefaultMutableTreeNode(cell);
                                nodes.put(cell, node);
                                if (cell instanceof RootCell)
                                    parentNode = treeRoot;
                                ((DefaultTreeModel)cellTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
                            }
                        }
                        break;
                }
            }
            
        });
        
        refreshCells(session);
        ((DefaultTreeModel)cellTree.getModel()).setRoot(treeRoot);
        
        jmeTree.setCellRenderer(new JmeTreeCellRenderer());
        jmeTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object selectedNode = jmeTree.getLastSelectedPathComponent();
                System.out.println("Selected "+selectedNode);
            }
            
        });
        
        cellTree.setCellRenderer(new WonderlandCellRenderer());
        cellTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object selectedNode = cellTree.getLastSelectedPathComponent();
                System.out.println("Selected "+selectedNode);
                Cell cell = (Cell) ((DefaultMutableTreeNode)selectedNode).getUserObject();
                System.out.println("Cell "+cell.getName());
                
                CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                if (renderer==null)
                    return;
                
                showJMEGraph(((RenderComponent)renderer.getEntity().getComponent(RenderComponent.class)).getSceneRoot());
            }
            
        });
    }
    
    /**
     * Show the JME scene graph for this node, find the 
     * @param node
     */
    private void showJMEGraph(Node node) {
        Node root = node;
        while(root.getParent()!=null) {
            System.out.println("Finding root "+root);
            root = root.getParent();
        }
            
        jmeTree.setModel(new JmeTreeModel(root));
    }
    
    /**
     * Get the  cells from the cache and update the UI
     */
    private void refreshCells(WonderlandSession session) {
        CellCache cache = ClientContext.getCellCache(session);
        
        for(Cell rootCell : cache.getRootCells()) {
            rootCells.add(rootCell);
        }
        
        populateJTree();
    }
    
    private void populateJTree() {
        for(Cell rootCell : rootCells) {            
            treeRoot.add(createJTreeNode(rootCell));
        }
    }
    
    private DefaultMutableTreeNode createJTreeNode(Cell cell) {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(cell);
        nodes.put(cell, ret);
        
        List<Cell> children = cell.getChildren();
        for(Cell child : children)
            ret.add(createJTreeNode(child));
        
        return ret;
    }
    
    private void populateCellPanelInfo(Cell cell) {
        if (cell==null) {
            cellClassNameTF.setText(null);
            cellNameTF.setText(null);
            DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
            listModel.clear();
        } else {
            cellClassNameTF.setText(cell.getClass().getName());
            cellNameTF.setText(cell.getName());
            DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
            listModel.clear();
            for(CellComponent c : cell.getComponents()) {
                listModel.addElement(c.getClass().getName());
            }
        }
    }

    class WonderlandCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                               Object value,
                                               boolean selected,
                                               boolean expanded,
                                               boolean leaf,
                                               int row,
                                               boolean hasFocus) {
            Cell cell = (Cell) ((DefaultMutableTreeNode)value).getUserObject();
            String name = cell.getName();
            if (name==null)
                name="";
            
            return new JLabel(getTrimmedClassname(cell)+":"+name);
        }       

        /**
         * Return the classname of the object, trimming off the package name
         * @param o
         * @return
         */
        private String getTrimmedClassname(Object o) {
            String str = o.getClass().getName();

            return str.substring(str.lastIndexOf('.')+1);
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

        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cellTree = new javax.swing.JTree();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        cellInfoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        cellComponentList = new javax.swing.JList();
        cellClassNameTF = new javax.swing.JTextField();
        cellNameTF = new javax.swing.JTextField();
        jmeGraphPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jmeTree = new javax.swing.JTree();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setTitle("Cell Viewer");

        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel4.setText("Cells");
        jPanel2.add(jLabel4, java.awt.BorderLayout.NORTH);

        cellTree.setMaximumSize(new java.awt.Dimension(400, 57));
        cellTree.setMinimumSize(new java.awt.Dimension(100, 0));
        cellTree.setPreferredSize(new java.awt.Dimension(200, 57));
        cellTree.setRootVisible(false);
        cellTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                cellTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(cellTree);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel2);

        jLabel1.setText("Cell Class :");

        jLabel2.setText("Cell Name :");

        jLabel3.setText("Cell Components :");

        cellComponentList.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(cellComponentList);

        cellClassNameTF.setText("jTextField1");

        cellNameTF.setText("jTextField1");

        org.jdesktop.layout.GroupLayout cellInfoPanelLayout = new org.jdesktop.layout.GroupLayout(cellInfoPanel);
        cellInfoPanel.setLayout(cellInfoPanelLayout);
        cellInfoPanelLayout.setHorizontalGroup(
            cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cellInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cellNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(cellClassNameTF)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)))
                .addContainerGap(134, Short.MAX_VALUE))
        );
        cellInfoPanelLayout.setVerticalGroup(
            cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cellInfoPanelLayout.createSequentialGroup()
                .add(23, 23, 23)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cellClassNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cellNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap(295, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cell Info", cellInfoPanel);

        jmeGraphPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setViewportView(jmeTree);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
        );

        jmeGraphPanel.add(jPanel4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("JME Graph", jmeGraphPanel);

        jSplitPane1.setRightComponent(jTabbedPane1);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cellTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_cellTreeValueChanged
    // Tree selection
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                       cellTree.getLastSelectedPathComponent();

    if (node == null) {
        //Nothing is selected.	
        return;
    }

    Cell cell = (Cell) node.getUserObject();
    populateCellPanelInfo(cell);

}//GEN-LAST:event_cellTreeValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cellClassNameTF;
    private javax.swing.JList cellComponentList;
    private javax.swing.JPanel cellInfoPanel;
    private javax.swing.JTextField cellNameTF;
    private javax.swing.JTree cellTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jmeGraphPanel;
    private javax.swing.JTree jmeTree;
    // End of variables declaration//GEN-END:variables

}