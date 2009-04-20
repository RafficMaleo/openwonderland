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
package org.jdesktop.wonderland.modules.palette.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;
import org.jdesktop.wonderland.common.cell.messages.CellDuplicateMessage;
import org.jdesktop.wonderland.common.cell.security.ChildrenAction;
import org.jdesktop.wonderland.common.cell.security.ModifyAction;
import org.jdesktop.wonderland.modules.palette.client.dnd.CellPaletteDataFlavorHandler;
import org.jdesktop.wonderland.modules.security.client.SecurityComponent;

/**
 * Client-size plugin for the cell palette.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
@ContextMenuFactory
public class PaletteClientPlugin implements ClientPlugin, ContextMenuFactorySPI {

    private static Logger logger = Logger.getLogger(PaletteClientPlugin.class.getName());

    /* The single instance of the cell palette dialog */
    private WeakReference<CellPalette> cellPaletteFrameRef = null;

    /* The single instance of the HUD cell palette dialog */
    private WeakReference<HUDCellPalette> hudCellPaletteFrameRef = null;

    /* The single instance of the module palette dialog */
    private WeakReference<ModulePalette> modulePaletteFrameRef = null;

    public void initialize(ServerSessionManager loginInfo) {
        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenu paletteMenu = new JMenu("Palettes");
        JMenuItem item = new JMenuItem("Cell Palette");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CellPalette cellPaletteFrame;
                if (cellPaletteFrameRef == null || cellPaletteFrameRef.get() == null) {
                    cellPaletteFrame = new CellPalette();
                    cellPaletteFrameRef = new WeakReference(cellPaletteFrame);
                }
                else {
                    cellPaletteFrame = cellPaletteFrameRef.get();
                }
                
                if (cellPaletteFrame.isVisible() == false) {
                    cellPaletteFrame.setVisible(true);
                }
            }
        });
        paletteMenu.add(item);

        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenuItem item2 = new JMenuItem("Cell Palette (HUD)");
        item2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HUDCellPalette hudCellPaletteFrame;
                if (hudCellPaletteFrameRef == null || hudCellPaletteFrameRef.get() == null) {
                    hudCellPaletteFrame = new HUDCellPalette();
                    hudCellPaletteFrameRef = new WeakReference(hudCellPaletteFrame);
                }
                else {
                    hudCellPaletteFrame = hudCellPaletteFrameRef.get();
                }

                if (hudCellPaletteFrame.isVisible() == false) {
                    hudCellPaletteFrame.setVisible(true);
                }
            }
        });
        paletteMenu.add(item2);
        JmeClientMain.getFrame().addToToolMenu(paletteMenu);

        // Register the handler for CellServerState flavors with the system-wide
        // drag and drop manager. When the preview icon is dragged from the Cell
        // Palette this handler creates an instance of the cell in the world.
        DragAndDropManager dndManager = DragAndDropManager.getDragAndDropManager();
        dndManager.registerDataFlavorHandler(new CellPaletteDataFlavorHandler());


        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenuItem moduleItem = new JMenuItem("Module Art Palette");
        moduleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ModulePalette modulePaletteFrame;
                if (modulePaletteFrameRef == null || modulePaletteFrameRef.get() == null) {
                    modulePaletteFrame = new ModulePalette();
                    modulePaletteFrameRef = new WeakReference(modulePaletteFrame);
                }
                else {
                    modulePaletteFrame = modulePaletteFrameRef.get();
                }

                if (modulePaletteFrame.isVisible() == false) {
                    modulePaletteFrame.setVisible(true);
                }
            }
        });
        paletteMenu.add(moduleItem);
        JmeClientMain.getFrame().addToToolMenu(paletteMenu);
    }

    /**
     * @inheritDoc()
     */
    public ContextMenuItem[] getContextMenuItems(final Cell cell) {
        final SimpleContextMenuItem deleteItem = 
                new SimpleContextMenuItem("Delete", null, new DeleteListener());

        final SimpleContextMenuItem duplicateItem =
                new SimpleContextMenuItem("Duplicate", null, new DuplicateListener());

        // find the security component for both this cell and it's parent,
        // if any
        final SecurityComponent sc = cell.getComponent(SecurityComponent.class);
        final SecurityComponent psc;
        if (cell.getParent() != null) {
            psc = cell.getParent().getComponent(SecurityComponent.class);
        } else {
            psc = null;
        }

        // see if we can check security locally, or if we have to make a
        // remote request
        if ((sc == null || sc.hasPermissions()) &&
            (psc == null || psc.hasPermissions()))
        {
            duplicateItem.setEnabled(canDuplicate(psc));
            deleteItem.setEnabled(canDelete(sc, psc));
        } else {
            new Thread(new Runnable() {
                public void run() {
                    duplicateItem.setEnabled(canDuplicate(psc));
                    deleteItem.setEnabled(canDelete(sc, psc));
                }
            }, "Cell palette security check").start();
        }

        return new ContextMenuItem[] {
            new SimpleContextMenuItem("Properties...", null, new PropertiesListener()),
            deleteItem,
            duplicateItem,
        };
    }

    private boolean canDuplicate(SecurityComponent psc) {
        if (psc == null) {
            return true;
        }

        try {
            ChildrenAction ca = new ChildrenAction();
            return psc.getPermissions().contains(ca);
        } catch (InterruptedException ie) {
            // shouldn't happen, since we check above
            return true;
        }
    }

    private boolean canDelete(SecurityComponent sc, SecurityComponent psc) {
        boolean out = true;
        if (sc == null && psc == null) {
            return out;
        }

        try {
            ModifyAction ma = new ModifyAction();
            ChildrenAction ca = new ChildrenAction();

            if (sc != null) {
                out = sc.getPermissions().contains(ma);
            }
            if (out && psc != null) {
                out = psc.getPermissions().contains(ca);
            }
        } catch (InterruptedException ie) {
            // shouldn't happen, since we check above
        }

        return out;
    }

    /**
     * Listener class for the "Properties..." context menu item
     */
    private class PropertiesListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            // Create a new cell edit frame passing in the Cell and make
            // it visible
            Cell cell = event.getCell();
            try {
                CellEditFrame frame = new CellEditFrame(cell);
                frame.setVisible(true);
            } catch (IllegalStateException excp) {
                Logger.getLogger(PaletteClientPlugin.class.getName()).log(Level.WARNING, null, excp);
            }
        }
    }

    /**
     * Listener for the "Delete" context menu item
     */
    private class DeleteListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            // Display a confirmation dialog to make sure we really want to
            // delete the cell.
            Cell cell = event.getCell();
            int result = JOptionPane.showConfirmDialog(
                    JmeClientMain.getFrame().getFrame(),
                    "Are you sure you wish to delete cell " + cell.getName(),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return;
            }

            // If we want to delete, send a message to the server as such
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            CellEditChannelConnection connection = (CellEditChannelConnection)
                    session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellDeleteMessage msg = new CellDeleteMessage(cell.getCellID());
            connection.send(msg);

            // Really should receive an OK/Error response from the server!
        }
    }

    /**
     * Listener for the "Duplicate" context menu item
     */
    private class DuplicateListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            // Create a new name for the cell, based upon the old name.
            Cell cell = event.getCell();
            String cellName = cell.getName() + " Copy";

            // If we want to delete, send a message to the server as such
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            CellEditChannelConnection connection = (CellEditChannelConnection)
                    session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellDuplicateMessage msg = new CellDuplicateMessage(cell.getCellID(), cellName);
            connection.send(msg);

            // Really should receive an OK/Error response from the server!
        }
    }
}
