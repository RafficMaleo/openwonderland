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
package org.jdesktop.wonderland.modules.affordances.client;


import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.security.MoveAction;
import org.jdesktop.wonderland.modules.affordances.client.event.AffordanceRemoveEvent;
import org.jdesktop.wonderland.modules.security.client.SecurityComponent;

/**
 * Client-size plugin for the cell manipulator affordances.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ContextMenuFactory
public class AffordancesClientPlugin implements ContextMenuFactorySPI {

    /* The single instance of the Affordance HUD Panel */
    private static JFrame affordanceHUDFrame = null;
    private static AffordanceHUDPanel affordanceHUDPanel = null;

    /**
     * Creates the affordance HUD frame
     */
    private void createHUDFrame() {
        affordanceHUDFrame = new JFrame();
        affordanceHUDFrame.getContentPane().setLayout(new GridLayout(1, 1));
        affordanceHUDFrame.getContentPane().add(affordanceHUDPanel = new AffordanceHUDPanel(affordanceHUDFrame));
        affordanceHUDFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        affordanceHUDFrame.addWindowListener(new FrameCloseListener());
        affordanceHUDFrame.setTitle("Edit Cell");
        affordanceHUDFrame.pack();
    }

    /**
     * @inheritDoc()
     */
    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        final SimpleContextMenuItem editItem =
                new SimpleContextMenuItem("Edit...", new EditContextListener());
        
        // if there is security on this cell, do some calculation to
        // figure out if the user has access to this cell
        Cell cell = event.getPrimaryCell();
        final SecurityComponent sc = cell.getComponent(SecurityComponent.class);
        if (sc != null) {
            // see if the permissions are available immediately
            if (sc.hasPermissions()) {
                editItem.setEnabled(canMove(sc));
            } else {
                // if not, we need to start a thread to load them
                editItem.setLabel("Edit (checking) ...");
                editItem.setEnabled(false);
                new Thread(new Runnable() {
                    public void run() {
                        editItem.setLabel("Edit...");
                        editItem.setEnabled(canMove(sc));
                        editItem.fireMenuItemRepaintListeners();
                    }
                }, "Security check wait thread").start();
            }
        }
        
        // return the edit item
        return new ContextMenuItem[] {
            editItem
        };
    }

    private boolean canMove(SecurityComponent sc) {
        try {
            MoveAction ma = new MoveAction();
            return sc.getPermissions().contains(ma);
        } catch (InterruptedException ie) {
            // shouldn't happen, since we check above
            return true;
        }
    }

    /**
     * Handles when the affordance frame is closed
     */
    class FrameCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            // Tell all of the affordances to remove themselves by posting
            // an event to the input system as such.
            InputManager.inputManager().postEvent(new AffordanceRemoveEvent());
        }
    }

    /**
     * Handles when the "Edit" context menu item has been selected
     */
    class EditContextListener implements ContextMenuActionListener {
        
        public void actionPerformed(ContextMenuItemEvent event) {
            
            // Display the affordance HUD Panel
            if (affordanceHUDFrame == null) {
                createHUDFrame();
            }
            affordanceHUDPanel.setTranslationVisible(true);
            affordanceHUDFrame.setVisible(true);
            affordanceHUDPanel.updateGUI();
        }
    }
}
