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
package org.jdesktop.wonderland.modules.assetmeter.client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin to activate the asset meter on the Tools menu.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class AssetMeterClientPlugin extends BaseClientPlugin {

    private AssetMeterJPanel assetMeterJPanel;
    private HUDComponent assetMeterHUDComponent;
    private JMenuItem item;

    public AssetMeterClientPlugin() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    item = new JCheckBoxMenuItem("Asset Meter");
                    item.setSelected(true);
                }
            });
        } catch (InterruptedException ie) {
            throw new IllegalStateException("Interrupt creating menu", ie);
        } catch (InvocationTargetException ise) {
            throw new IllegalStateException("Exception creating menu", ise);
        }
    }

    @Override
    protected void activate() {
        // Add the Asset Meter as a checkbox menu item to the Tools menu as a
        // Checkbox menu item. If it is selected, then show it or hide it.
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setEnabled(item.isSelected());
            }
        });

        // Set up the UI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // First create the asset meter frame
                assetMeterJPanel = new AssetMeterJPanel(AssetMeterClientPlugin.this);

                // Now create the HUD component
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                assetMeterHUDComponent = mainHUD.createComponent(assetMeterJPanel);
                assetMeterHUDComponent.setPreferredTransparency(0.0f);
                assetMeterHUDComponent.setPreferredLocation(Layout.SOUTHEAST);
                assetMeterHUDComponent.setName("Downloading...");
                mainHUD.addComponent(assetMeterHUDComponent);
            }
        });

        // Add the item to the tools menu and make the Asset Meter visible
        // by default initially.
        JmeClientMain.getFrame().addToWindowMenu(item, 1);
    }

    @Override
    protected void deactivate() {
        // remove items
        JmeClientMain.getFrame().removeFromWindowMenu(item);
        assetMeterJPanel.deactivate();
        assetMeterJPanel = null;
    }

    HUDComponent getHUDComponent() {
        return assetMeterHUDComponent;
    }

    void resize() {
        Dimension size = assetMeterJPanel.getSize();
        System.out.println("Resize to " + size);
        assetMeterHUDComponent.setSize(size);
    }

    private void setEnabled(boolean enabled) {
        assetMeterJPanel.setUpdateEnabled(enabled);
    }
}
