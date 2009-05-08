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
package org.jdesktop.wonderland.modules.celleditor.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.PropertiesManager;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentResponseMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateRequestMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateResponseMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateUpdateMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A frame to allow the editing of properties for the cell.
 * * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellPropertiesJFrame extends javax.swing.JFrame implements CellPropertiesEditor {

    private static Logger logger = Logger.getLogger(CellPropertiesJFrame.class.getName());
    private List<PropertiesFactorySPI> factoryList = null;
    private Cell selectedCell = null;
    private CellServerState selectedCellServerState = null;
    private PropertiesFactorySPI cellProperties = null;
    private DefaultListModel listModel = null;
    private DefaultMutableTreeNode treeRoot = null;
    private Map<Cell, DefaultMutableTreeNode> cellNodes = null;
    private CellStatusChangeListener cellListener = null;
    private TreeSelectionListener treeListener = null;
    private DefaultMutableTreeNode dragOverTreeNode = null;
    private Set<Class> dirtyPanelSet = new HashSet();
    private StateUpdates stateUpdates = null;

    /** Constructor */
    public CellPropertiesJFrame() {
        factoryList = new LinkedList();
        stateUpdates = new StateUpdates();

        // Initialize the GUI components
        initComponents();

        // Add a list model for the list of capabilities. Also, listen for
        // selections on the list to display the appropriate panel
        listModel = new DefaultListModel();
        capabilityList.setModel(listModel);
        capabilityList.addListSelectionListener(new CapabilityListSelectionListener());

        // Create and add a basic panel for all cells as a special case.
        BasicJPanel basicPanel = new BasicJPanel();
        basicPanel.setCellPropertiesEditor(this);
        listModel.add(0, "Basic");
        factoryList.add(0, basicPanel);

        // Set up all of the stuff we need to the tree to display Cells
        treeRoot = new DefaultMutableTreeNode("World Root");
        cellNodes = new HashMap();
        ((DefaultTreeModel) cellHierarchyTree.getModel()).setRoot(treeRoot);
        cellHierarchyTree.setCellRenderer(new CellTreeRenderer());

        // Create a listener that will listen to the status of Cells. This
        // listener gets added when the dialog is made visible
        cellListener = new CellStatusChangeListener() {
            public void cellStatusChanged(Cell cell, CellStatus status) {
                DefaultMutableTreeNode node = cellNodes.get(cell);
                if (status == CellStatus.DISK) {
                    // If there is a Node that corresponds to the Cell, then
                    // remove it from the tree
                    if (node != null) {
                        ((DefaultTreeModel) cellHierarchyTree.getModel()).removeNodeFromParent(node);
                    }
                }
                else if (status == CellStatus.BOUNDS) {
                    // If the node does not exist, then create it
                    if (node == null) {
                        createJTreeNode(cell);
                    }
                }
            }
        };

        // Listen to selections on the tree and change the selected Cell.
        treeListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
                        cellHierarchyTree.getLastSelectedPathComponent();
                Object userObject = selectedNode.getUserObject();
                if (userObject instanceof Cell) {
                    setSelectedCell((Cell)userObject);
                }
                else {
                    setSelectedCell(null);
                }
            }
        };

        // Install drag and drop on the tree. This will handle when a tree node
        // is dropped on top of another node.
        cellHierarchyTree.setDragEnabled(true);
        DropTarget dt = new DropTarget();
        try {
            dt.addDropTargetListener(new CellDropTargetListener());
        } catch (TooManyListenersException ex) {
            Logger.getLogger(CellPropertiesJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        cellHierarchyTree.setDropTarget(dt);

        // Listen for when the window is closing. If so, see if there are any
        // changes to the properties and ask if the user wants to apply
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dirtyPanelSet.isEmpty() == false) {
                    int result = JOptionPane.showConfirmDialog(
                            CellPropertiesJFrame.this,
                            "Do you wish to apply the properties before switching?",
                            "Apply values?", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        applyValues();
                    }
                }

                // Regardless, tell the panels to close themselves
                for (PropertiesFactorySPI factory : factoryList) {
                    factory.close();
                }
            }
        });
    }

    /**
     * Overrides setVisible() to refect the GUI if being made visible.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible == true) {
            // Add the listener for the JTree for Cell status changes
            CellManager.getCellManager().addCellStatusChangeListener(cellListener);
            updateTreeGUI();
            updateGUI();
        }
        else {
            // Remove the listener for the JTree for Cell status changes
            CellManager.getCellManager().removeCellStatusChangeListener(cellListener);
        }
    }

    /**
     * Sets the currently selected Cell. Update the GUI of the Cell Properties
     * frame to reflect the newly-selected Cell's state.
     */
    public void setSelectedCell(Cell cell) throws IllegalStateException {

        // Check to see if there have been changes to the values in the Cell
        // Properties. If so, then prompt the user whether these should be
        // applies first. If so, then apply, otherwise not.
        if (dirtyPanelSet.isEmpty() == false) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you wish to apply the properties before switching?",
                    "Apply values?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                applyValues();
            }
        }


        // Remove any existing panels from the GUI. We first need to tell them
        // to close themselves. We do this before we set to the new selected
        // Cell.
        clearPanelSet();

        // Now, set the currnent Cell. If it is null, we simply return
        selectedCell = cell;
        if (selectedCell == null) {
            return;
        }

        // Next, fetch the server-state of the Cell. This will tell us which
        // panels we will need to add to the frame.
        selectedCellServerState = fetchCellServerState();
        if (selectedCellServerState == null) {
            logger.warning("Unable to fetch cell server state for " + cell.getName());
            throw new IllegalStateException("Unable to fetch cell server state");
        }

        // Update the panel set based upon the elements in the server state
        updatePanelSet();
        if (isVisible() == true) {
            updateGUI();
        }
    }

    /**
     * @inheritDoc()
     */
    public void addToUpdateList(CellServerState cellServerState) {
        stateUpdates.cellServerState = cellServerState;
    }

    /**
     * @inheritDoc()
     */
    public void addToUpdateList(CellComponentServerState cellComponentServerState) {
        stateUpdates.cellComponentServerStateSet.add(cellComponentServerState);
    }

    /**
     * @inheritDoc()
     */
    public Cell getCell() {
        return selectedCell;
    }

    /**
     * @inheritDoc()
     */
    public CellServerState getCellServerState() {
        return selectedCellServerState;
    }
    
    /**
     * @inheritDoc()
     */
    public void setPanelDirty(Class clazz, boolean isDirty) {
        // Either add or remove the Class depending upon whether it is dirty
        if (isDirty == true) {
            dirtyPanelSet.add(clazz);
        }
        else {
            dirtyPanelSet.remove(clazz);
        }

        // Enable/disable the Ok/Apply buttons depending upon whether the set
        // of dirty panels is empty or not
        applyButton.setEnabled(dirtyPanelSet.isEmpty() == false);
        restoreButton.setEnabled(dirtyPanelSet.isEmpty() == false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        topLevelSplitPane = new javax.swing.JSplitPane();
        cellHierarchyPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        treePanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        cellHierarchyTree = new javax.swing.JTree();
        bottomLevelSplitPane = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        propertyPanel = new javax.swing.JPanel();
        propertyButtonPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        restoreButton = new javax.swing.JButton();
        capabilityPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        capabilityListPanel = new javax.swing.JPanel();
        capabilityListScrollPane = new javax.swing.JScrollPane();
        capabilityList = new javax.swing.JList();
        capabilityButtonPanel = new javax.swing.JPanel();
        addCapabilityButton = new javax.swing.JButton();
        removeCapabilityButton = new javax.swing.JButton();

        setTitle("Cell Editor");
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mainPanel.setLayout(new java.awt.GridLayout(1, 1));

        topLevelSplitPane.setOneTouchExpandable(true);

        cellHierarchyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cellHierarchyPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setLayout(new java.awt.GridLayout(1, 1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Cell Hierarchy");
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel1.add(jLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        cellHierarchyPanel.add(jPanel1, gridBagConstraints);

        treePanel.setMinimumSize(new java.awt.Dimension(250, 23));
        treePanel.setLayout(new java.awt.GridLayout(1, 0));

        treeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        treeScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        cellHierarchyTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        cellHierarchyTree.setDragEnabled(true);
        cellHierarchyTree.setEditable(true);
        cellHierarchyTree.setShowsRootHandles(true);
        treeScrollPane.setViewportView(cellHierarchyTree);

        treePanel.add(treeScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        cellHierarchyPanel.add(treePanel, gridBagConstraints);

        topLevelSplitPane.setLeftComponent(cellHierarchyPanel);

        bottomLevelSplitPane.setBorder(null);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setLayout(new java.awt.GridLayout(1, 1));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Cell Properties");
        jLabel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel3.add(jLabel4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        jPanel4.add(jPanel3, gridBagConstraints);

        propertyPanel.setBackground(new java.awt.Color(255, 255, 255));
        propertyPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        jPanel4.add(propertyPanel, gridBagConstraints);

        propertyButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        propertyButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        propertyButtonPanel.add(applyButton);

        restoreButton.setText("Restore");
        restoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreButtonActionPerformed(evt);
            }
        });
        propertyButtonPanel.add(restoreButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel4.add(propertyButtonPanel, gridBagConstraints);

        bottomLevelSplitPane.setRightComponent(jPanel4);

        capabilityPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        capabilityPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setLayout(new java.awt.GridLayout(1, 1));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Cell Capabilities");
        jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel2.add(jLabel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        capabilityPanel.add(jPanel2, gridBagConstraints);

        capabilityListPanel.setLayout(new java.awt.GridLayout());

        capabilityListScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        capabilityListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        capabilityList.setBackground(new java.awt.Color(204, 204, 255));
        capabilityList.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        capabilityList.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        capabilityList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        capabilityListScrollPane.setViewportView(capabilityList);

        capabilityListPanel.add(capabilityListScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        capabilityPanel.add(capabilityListPanel, gridBagConstraints);

        capabilityButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        capabilityButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        addCapabilityButton.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        addCapabilityButton.setText("+");
        addCapabilityButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addCapabilityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCapabilityButtonActionPerformed(evt);
            }
        });
        capabilityButtonPanel.add(addCapabilityButton);

        removeCapabilityButton.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        removeCapabilityButton.setEnabled(false);
        removeCapabilityButton.setLabel("-");
        removeCapabilityButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        removeCapabilityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCapabilityButtonActionPerformed(evt);
            }
        });
        capabilityButtonPanel.add(removeCapabilityButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        capabilityPanel.add(capabilityButtonPanel, gridBagConstraints);

        bottomLevelSplitPane.setLeftComponent(capabilityPanel);

        topLevelSplitPane.setRightComponent(bottomLevelSplitPane);

        mainPanel.add(topLevelSplitPane);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addCapabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCapabilityButtonActionPerformed
        // Create a new AddComponentDialog and display. Wait for the dialog
        // to close
        AddComponentDialog dialog = new AddComponentDialog(this, true, selectedCell);
        dialog.setVisible(true);

        // If the OK button was pressed on the dialog and we can fetch a valid
        // cell component factory, then try to add it on the server.
        CellComponentFactorySPI spi = dialog.getCellComponentFactorySPI();
        if (dialog.getReturnStatus() == AddComponentDialog.RET_OK && spi != null) {
            addComponent(spi);
        }
    }//GEN-LAST:event_addCapabilityButtonActionPerformed

    private void removeCapabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCapabilityButtonActionPerformed
        // Find out which component is selected and remove it
        int index = capabilityList.getSelectedIndex();
        PropertiesFactorySPI spi = factoryList.get(index);
        removeComponent(spi);
    }//GEN-LAST:event_removeCapabilityButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        // Simply apply all of the values in the GUI
        applyValues();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void restoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreButtonActionPerformed
        // Simply restore all of the values in the GUI
        restoreValues();
    }//GEN-LAST:event_restoreButtonActionPerformed


    /**
     * Inner class to deal with selection on the capability list.
     */
    class CapabilityListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            // Check to see if the value is still changing and if so, ignore
            if (e.getValueIsAdjusting() == true) {
                return;
            }
            
            // Handles when an item has been selected in the list of capabilities
            // Display the proper panel in such an instance. We also enable
            // or disable the '-' sign to remove components depending upon
            // what is selected
            int index = capabilityList.getSelectedIndex();
            if (index == -1) {
                propertyPanel.removeAll();
                removeCapabilityButton.setEnabled(false);
                propertyPanel.revalidate();
                propertyPanel.repaint();
                return;
            }

            // For all items, look up the panel in the ordered list of panels
            propertyPanel.removeAll();
            PropertiesFactorySPI factory = factoryList.get(index);
            propertyPanel.add(factory.getPropertiesJPanel());

            // We want to enable/disable the "remove" button only for property
            // sheets other than the first two. (The first three if the Cell
            // property sheet is non-null.
            if (cellProperties == null) {
                removeCapabilityButton.setEnabled(index >= 1);
            }
            else {
                removeCapabilityButton.setEnabled(index >= 2);
            }

            // Invalidate the layout and repaint
            propertyPanel.revalidate();
            propertyPanel.repaint();
        }
    }

    /**
     * Applies the values stored in the GUI to the cell. Loops through each
     * of the panels and tells them to apply().
     */
    private void applyValues() {
        // Loop through all of the properties in the list and tell them to
        // apply
        for (PropertiesFactorySPI factory : factoryList) {
            factory.apply();
        }

        // As a first step, remove all of the cell component server states from
        // the cell server state. These cell component server states to update
        // are kept in a separate list.
        CellServerState updateState = stateUpdates.cellServerState;
        if (updateState != null) {
            updateState.removeAllComponentServerStates();
        }

        // Form a new CellUpdateServerState message with the appropriate
        // information and send it
        CellServerStateUpdateMessage msg = new CellServerStateUpdateMessage(
                selectedCell.getCellID(),
                updateState,
                stateUpdates.cellComponentServerStateSet);
        selectedCell.sendCellMessage(msg);

        // XXX Probably should get a success/failed here!

        // Clear any existing updates store by this class
        stateUpdates.clear();

        // Re-fetch the server state of the Cell here. This is so we have a
        // fetch copy of the state.
        selectedCellServerState = fetchCellServerState();
        if (selectedCellServerState == null) {
            logger.warning("Unable to fetch cell server state for " +
                    selectedCell.getName());
            return;
        }

        // Tell each of the panels to update their GUIs. This will have the
        // effect of resetting their original values so they know whether they
        // are dirty or not.
        updateGUI();
    }

    /**
     * Restores all of the values in the GUI to the original values. Loops
     * through each of the panels and tells them to refresh()
     */
    private void restoreValues() {
        // First clear out any existing updates stored by this class
        stateUpdates.clear();

        // Next iteratate through all factories and tell them to refresh
        for (PropertiesFactorySPI factory : factoryList) {
            factory.refresh();
        }
    }

    /**
     * Iterates through all of the properties panels and tell them they are
     * about to be closed. They should (perhaps) reset some values. Then this
     * method removes the panels from the Cell Properties dialog.
     */
    private void clearPanelSet() {
        // First, loop through all of the factories and tell them they are about
        // to close. It is up to them to decide what to do.
        for (PropertiesFactorySPI factory : factoryList) {
            factory.close();
        }

        // Next, loop through all the factories (again), and remove them from
        // the list model. We do not remove the first, since is it the fixed
        // "Basic" panel. Also, remove the item from the list of factories now
        // too.
        int size = factoryList.size();
        for (int i = 1; i < size; i++) {
            listModel.remove(i);
            factoryList.remove(i);
        }
    }

    /**
     * Asks the server for the server state of the cell; returns null upon
     * error
     */
    private CellServerState fetchCellServerState() {
        // Fetch the setup object from the Cell object. We send a message on
        // the cell channel, so we must fetch that first.
        ResponseMessage response = selectedCell.sendCellMessageAndWait(
                new CellServerStateRequestMessage(selectedCell.getCellID()));
        if (response == null) {
            return null;
        }

        // We need to remove the position component first as a special case
        // since we do not want to update it after the cell is created.
        CellServerStateResponseMessage cssrm = (CellServerStateResponseMessage)response;
        CellServerState state = cssrm.getCellServerState();
        if (state != null) {
            state.removeComponentServerState(PositionComponentServerState.class);
        }
        return state;
    }

    /**
     * Update the node in the tree with the current Cell hierarchy.
     */
    private void updateTreeGUI() {
        // Stop listening to selections on the tree while we update the GUI
        cellHierarchyTree.removeTreeSelectionListener(treeListener);

        // Refresh the Cell hierarchy tree. Expand the tree path around the
        // selected cell and select it in the tree.
        refreshCells(LoginManager.getPrimary().getPrimarySession());
        DefaultMutableTreeNode node = cellNodes.get(selectedCell);
        if (node == null) {
            logger.warning("Unable to find tree node for selected Cell " + selectedCell);
            return;
        }
        TreePath path = new TreePath(node.getPath());
        cellHierarchyTree.expandPath(path);
        cellHierarchyTree.setSelectionPath(path);

        // Start listening to the tree once again
        cellHierarchyTree.addTreeSelectionListener(treeListener);
    }

    /**
     * Updates the GUI with values currently set in the cell 
     */
    private void updateGUI() {
        // Loop through all of the panels and tell them to refresh their values
        for (PropertiesFactorySPI factory : factoryList) {
            factory.refresh();
        }
    }
    
    /**
     * For the current Cell object, fetch which CellComponents are currently
     * associated with the cell and creates/deletes any panels on the GUI
     * edit frame as necessary. 
     */
    private void updatePanelSet() {
        // Look through the registry of cell property objects and check to see
        // if a panel exists for the cell. Add it if so
        Class clazz = selectedCellServerState.getClass();
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        cellProperties = manager.getPropertiesByClass(clazz);

        // If the cell properties panel exists, add an entry for it
        if (cellProperties != null && cellProperties.getPropertiesJPanel() != null) {
            listModel.addElement(cellProperties.getDisplayName());
            factoryList.add(cellProperties);
            cellProperties.setCellPropertiesEditor(this);
        }

        // Loop through all of the cell components in the server state and for
        // each see if there is a properties sheet registered for it. If so,
        // then add it.
        for (Map.Entry<Class, CellComponentServerState> e :
            selectedCellServerState.getComponentServerStates().entrySet()) {

            CellComponentServerState state = e.getValue();
            PropertiesFactorySPI spi = manager.getPropertiesByClass(state.getClass());
            if (spi != null) {
                JPanel panel = spi.getPropertiesJPanel();
                if (panel != null) {
                    String displayName = spi.getDisplayName();
                    spi.setCellPropertiesEditor(this);
                    listModel.addElement(displayName);
                    factoryList.add(spi);
                }
            }
        }
    }

    /**
     * Adds an individual component panel to the set of panels, given the
     * cell component factory and the component server state.
     */
    private void addComponentToPanelSet(CellComponentFactorySPI spi, CellComponentServerState state) {
        // FircellServerStatest, since this is a new panel since the server state was fetched,
        // add the component server state to the cell server state.
        Class clazz = state.getClass();
        selectedCellServerState.addComponentServerState(state);

        // Next, add the component display name to the list and to the list
        // of properties panels. We look up the properties in the manager of
        // all component properties given the class name of the component
        // server state.
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        PropertiesFactorySPI factory = manager.getPropertiesByClass(clazz);
        if (factory != null) {
            JPanel panel = factory.getPropertiesJPanel();
            if (panel != null) {
                String displayName = factory.getDisplayName();
                listModel.addElement(displayName);
                factoryList.add(factory);
                factory.setCellPropertiesEditor(this);
                factory.refresh();
            }
        }
    }

    /**
     * Given a component factory, adds the component to the server and upates
     * the GUI to indicate its presence
     */
    private void addComponent(CellComponentFactorySPI spi) {
        // Fetch the default server state for the factory, and cell id. Make
        // sure we make it dynamically added
        CellComponentServerState state = spi.getDefaultCellComponentServerState();
        //state.setStatic(false);
        CellID cellID = selectedCell.getCellID();

        // Send a ADD component message on the cell channel. Wait for a
        // response. If OK, then update the GUI with the new component.
        // Otherwise, display an error dialog box.
        CellServerComponentMessage message = CellServerComponentMessage.newAddMessage(cellID, state);
        ResponseMessage response = selectedCell.sendCellMessageAndWait(message);
        if (response == null) {
            // log and error and post a dialog box
            logger.warning("Received a null reply from cell with id " +
                    selectedCell.getCellID() + " with name " + selectedCell.getName() +
                    " adding component.");
            return;
        }

        if (response instanceof CellServerComponentResponseMessage) {
            // If successful, add the component to the GUI
            CellServerComponentResponseMessage cscrm = (CellServerComponentResponseMessage) response;
            addComponentToPanelSet(spi, cscrm.getCellComponentServerState());
        }
        else if (response instanceof ErrorMessage) {
            // Log an error. Eventually we should display a dialog
            logger.log(Level.WARNING, "Unable to add component to the server",
                    ((ErrorMessage) response).getErrorCause());
        }
    }

    /**
     * Given the component properties SPI, removes the component from the server
     * and updates the GUI to indicate its absense
     */
    private void removeComponent(PropertiesFactorySPI factory) {
        // Using the given factory, find out the server-side class name that
        // corresponds to the component.
        Class clazz = PropertiesManager.getServerStateClass(factory);
        if (clazz == null) {
            logger.warning("Unable to remove component for " + factory);
            return;
        }

        // Send a message to remove the component giving the class name. Wait
        // for a response.
        CellID cellID = selectedCell.getCellID();
        String className = clazz.getName();

        // Send a message to the server with the cell id and class name and
        // wait for a response
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newRemoveMessage(cellID, className);
        ResponseMessage response = selectedCell.sendCellMessageAndWait(cscm);
        if (response == null) {
            logger.warning("Received a null reply from cell with id " +
                    cellID + " with name " + selectedCell.getName() +
                    " removing component.");
            return;
        }

        // Send the message to the server. Wait for a response. If OK, then
        // update the GUI with the new component. Otherwise, display an error
        // dialog box.
        if (response instanceof OKMessage) {
            // If successful, add the component to the GUI
            listModel.removeElement(factory.getDisplayName());
            capabilityList.setSelectedIndex(-1);
            factoryList.remove(factory);
        }
        else if (response instanceof ErrorMessage) {
            // Log an error. Eventually we should display a dialog
            logger.log(Level.WARNING, "Unable to add component to the server",
                    ((ErrorMessage) response).getErrorCause());
        }
    }

    /**
     * Holds the collection of updates to the server state to be sent to the
     * server
     */
    private class StateUpdates {
        public CellServerState cellServerState = null;
        public Set<CellComponentServerState> cellComponentServerStateSet = null;

        /** Default constructor */
        public StateUpdates() {
            cellComponentServerStateSet = new HashSet();
        }

        /**
         * Clears out any existing updates of state
         */
        public void clear() {
            cellServerState = null;
            cellComponentServerStateSet.clear();
        }
    }

    /**
     * Render for Cells in the JTree. This uses the "default" tree cell renderer
     * which is a subclass of JLabel. Each tree node has a "user object" which
     * is a Cell. Draw the Cell name, and a border around it if it is currently
     * dragged-over in a drag-and-drop operation. The "root" node is just a
     * default mutable tree node with a user object of a String name.
     */
    private class CellTreeRenderer extends DefaultTreeCellRenderer {

        /**
         * @inheritDoc()
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            // Call the super class method to render the tree node properly.
            super.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);
            
            // Assume the node is a default mutable tree node. If the node is
            // currently being dragged-over in a drag-and-drop operation, then
            // set a black line border around the tree node, otherwise clear
            // the border.
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            if (treeNode == dragOverTreeNode) {
                setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            else {
                setBorder(null);
            }

            // Using the name of the Cell to set the name of the tree node.
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof Cell) {
                Cell cell = (Cell) treeNode.getUserObject();
                setText(cell.getName());
            }
            return this;
        }
    }

    /**
     * Listener for drop target events. Makes sure the node in the tree for the
     * drop is properly highlighted and performs the drop by reparenting the
     * Cell.
     */
    private class CellDropTargetListener extends DropTargetAdapter {

        /**
         * @inheritDoc()
         */
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // Fetch the location over which we are dragging and find
            // the tree node corresponding to that position.
            Point location = dtde.getLocation();
            TreePath path = cellHierarchyTree.getPathForLocation(
                    location.x, location.y);
            if (path != null) {
                dragOverTreeNode = (DefaultMutableTreeNode)
                        path.getLastPathComponent();
            }
            else {
                dragOverTreeNode = null;
            }
            cellHierarchyTree.repaint();
        }

        /**
         * @inheritDoc()
         */
        @Override
        public void dragExit(DropTargetEvent dte) {
            dragOverTreeNode = null;
            cellHierarchyTree.repaint();
        }

        /**
         * @inheritDoc()
         */
        @Override
        public void drop(DropTargetDropEvent dtde) {
            // Fetch the location over which we are dropping and find
            // the tree node corresponding to that position.
            Point location = dtde.getLocation();
            TreePath path = cellHierarchyTree.getPathForLocation(
                    location.x, location.y);
            if (path == null) {
                dtde.rejectDrop();
                return;
            }

            // Accept the drop and fetch the transferable from the drop event.
            // We are given the value of toString() from the dropped object.
            // Since this is a default mutable tree node, we defined the
            // toString() method below to return CellID@<CellID>.
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            Transferable transferable = dtde.getTransferable();
            String cellIDString = null;
            try {
                DataFlavor df = new DataFlavor("application/x-java-jvm-local-objectref; class=java.lang.String");
                cellIDString = (String)transferable.getTransferData(df);
            } catch (Exception excp) {
                logger.log(Level.WARNING, "Unable to fetch Cell ID string " +
                        "from the drop target", excp);
                return;
            }

            // Check to make sure the cellID String is properly formed. If not,
            // then log an error and return
            if (cellIDString == null || cellIDString.startsWith("CellID@") == false) {
                logger.warning("Invalid Cell ID from drag and drop " + cellIDString);
                return;
            }

            // Parse out the Cell ID from the String
            int cellIDInt = -1;
            try {
                cellIDInt = Integer.parseInt(cellIDString.substring(7));
            } catch (Exception excp) {
                logger.log(Level.WARNING, "Unable to fetch Cell ID integer " +
                        "from the drop target", excp);
                return;
            }
            System.out.println("CELL ID " + cellIDInt);
            CellID cellID = new CellID(cellIDInt);

            // Fetch the client-side Cell cache and find the Cell with the
            // dropped CellID
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            CellCache cache = ClientContext.getCellCache(session);
            if (cache == null) {
                logger.warning("Unable to find Cell cache for session " + session);
                return;
            }
            Cell draggedCell = cache.getCell(cellID);
            if (draggedCell == null) {
                logger.warning("Unable to find dragged Cell with ID " + cellID);
                return;
            }

            // Find out what Cell ID this was dropped over. This will form the
            // new parent. If the Cell is dropped over the world root, then set
            // the CellID to -1
            CellID parentCellID = new CellID(-1);
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                    path.getLastPathComponent();
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof Cell) {
                parentCellID = ((Cell)userObject).getCellID();
            }
            System.out.println("Parent CELL ID " + parentCellID.toString());

            // Turn off the selected node border and repaint the tree.
            dragOverTreeNode = null;
            cellHierarchyTree.repaint();
        }
    }

    /**
     * Get the  cells from the cache and update the nodes in tree
     */
    private void refreshCells(WonderlandSession session) {
        // Fetch the client-side Cell cache, log an error if not found and
        // return
        CellCache cache = ClientContext.getCellCache(session);
        if (cache == null) {
            logger.warning("Unable to find Cell cache for session " + session);
            return;
        }

        // Clear out any existing Cells
        cellNodes.clear();

        // Loop through all of the root cells and add into the world
        Collection<Cell> rootCells = cache.getRootCells();
        for(Cell rootCell : rootCells) {
            // Special case to ignore Avatar Cells
            if (rootCell instanceof AvatarCell) {
                continue;
            }
            createJTreeNode(rootCell);
        }
    }

    /**
     * Creates a new tree node for the given Cell, inserts it into the tree and
     * returns it.
     */
    private DefaultMutableTreeNode createJTreeNode(Cell cell) {
        // Create the tree node and put into the map of all nodes. We override
        // the toString() method to return a string containing the Cell ID.
        // This is used in the drag and drop mechanism to figure out which
        // Cell is being dragged.
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(cell) {
            @Override
            public String toString() {
                Cell cell = (Cell)getUserObject();
                return "CellID@" + cell.getCellID().toString();
            }
        };
        cellNodes.put(cell, ret);

        // Find the parent node of the new node, and insert it into the tree
        DefaultMutableTreeNode parentNode = cellNodes.get(cell.getParent());
        if (parentNode == null) {
            parentNode = treeRoot;
        }
        DefaultTreeModel treeModel = (DefaultTreeModel)cellHierarchyTree.getModel();
        treeModel.insertNodeInto(ret, parentNode, parentNode.getChildCount());

        // Recursively iterate through all of the Cell's children and add to
        // the tree.
        List<Cell> children = cell.getChildren();
        for(Cell child : children) {
            ret.add(createJTreeNode(child));
        }
        return ret;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCapabilityButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JSplitPane bottomLevelSplitPane;
    private javax.swing.JPanel capabilityButtonPanel;
    private javax.swing.JList capabilityList;
    private javax.swing.JPanel capabilityListPanel;
    private javax.swing.JScrollPane capabilityListScrollPane;
    private javax.swing.JPanel capabilityPanel;
    private javax.swing.JPanel cellHierarchyPanel;
    private javax.swing.JTree cellHierarchyTree;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel propertyButtonPanel;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JButton removeCapabilityButton;
    private javax.swing.JButton restoreButton;
    private javax.swing.JSplitPane topLevelSplitPane;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
}
