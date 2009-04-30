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

package org.jdesktop.wonderland.modules.portal.client;

import com.jme.math.Quaternion;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;
import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellComponentProperties
public class PortalComponentProperties extends javax.swing.JPanel implements CellComponentPropertiesSPI {

    private CellPropertiesEditor editor = null;
    
    private String origServerURL = null;
    private String origX = null;
    private String origY = null;
    private String origZ = null;
    private String origAngle = null;
    
    /** Creates new form PortalComponentProperties */
    public PortalComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the text fields
        TextFieldListener listener = new TextFieldListener();
        urlTF.getDocument().addDocumentListener(listener);
        locX.getDocument().addDocumentListener(listener);
        locY.getDocument().addDocumentListener(listener);
        locZ.getDocument().addDocumentListener(listener);
        angleTF.getDocument().addDocumentListener(listener);
    }

    /**
     * @inheritDoc()
     */
    public Class getServerCellComponentClass() {
        return PortalComponentServerState.class;
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return "Portal";
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
        this.editor = editor;
        return this;
    }

    /**
     * @inheritDoc()
     */
    public <T extends CellServerState> void updateGUI(T cellServerState) {
        PortalComponentServerState state = (PortalComponentServerState)
                 cellServerState.getComponentServerState(PortalComponentServerState.class);
        if (state != null) {
            origServerURL = state.getServerURL();
            if (origServerURL != null) {
                urlTF.setText(origServerURL);
            }
            
            Origin origin = state.getLocation();
            if (origin != null) {
                origX = String.valueOf(origin.x);
                origY = String.valueOf(origin.y);
                origZ = String.valueOf(origin.z);
            } else {
                origX = "";
                origY = "";
                origZ = "";
            }
            locX.setText(origX);
            locY.setText(origY);
            locZ.setText(origZ);
            
            Rotation r = state.getLook();
            if (r != null) {
                origAngle = String.valueOf(r.angle);
            } else {
                origAngle = "";
            }
            angleTF.setText(origAngle);
            
            return;
        }
    }

    /**
     * @inheritDoc()
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState) {
        // Figure out whether there already exists a server state for the
        // component.
        PortalComponentServerState state = (PortalComponentServerState) 
            cellServerState.getComponentServerState(PortalComponentServerState.class);
        if (state == null) {
            state = new PortalComponentServerState();
        }
        
        String serverURL = urlTF.getText().trim();
        if (serverURL.length() == 0) {
            serverURL = null;
        }
        state.setServerURL(serverURL);
        
        Origin location = new Origin();
        String xstr = locX.getText().trim();
        String ystr = locY.getText().trim();
        String zstr = locZ.getText().trim();
        if (xstr.length() == 0 || ystr.length() == 0 || zstr.length() == 0) {
            location = null;
        } else {
            location.x = Float.parseFloat(xstr);
            location.y = Float.parseFloat(ystr);
            location.z = Float.parseFloat(zstr);
        }
        state.setLocation(location);
        
        Rotation look = new Rotation();
        String anglestr = angleTF.getText().trim();
        if (anglestr.length() == 0) {
            look = null;
        } else {
            Quaternion q = new Quaternion(0.0f, 1.0f, 0.0f,
                                          Float.parseFloat(anglestr));
            look = new Rotation(q);
        }
        state.setLook(look);
        
        cellServerState.addComponentServerState(state);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class TextFieldListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }

        private void checkDirty() {
            if (editor == null) {
                return;
            }
            
            boolean clean = urlTF.getText().equals(origServerURL);
            clean &= locX.getText().trim().equals(origX);
            clean &= locY.getText().trim().equals(origY);
            clean &= locZ.getText().trim().equals(origZ);
            clean &= angleTF.getText().trim().equals(origAngle);
            
            editor.setPanelDirty(PortalComponentProperties.class, !clean);
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

        jLabel1 = new javax.swing.JLabel();
        urlTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        locX = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        locY = new javax.swing.JTextField();
        locZ = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        angleTF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();

        jLabel1.setText("Server URL:");

        jLabel2.setText("Location:");

        jLabel3.setText("x:");

        jLabel4.setText("y:");

        jLabel5.setText("z:");

        jLabel10.setText("angle:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel10)
                            .add(jLabel5)
                            .add(jLabel4)
                            .add(jLabel3)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(urlTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, angleTF)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, locY, 0, 0, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, locX, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, locZ, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)))
                        .addContainerGap())
                    .add(jLabel2)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(urlTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locZ, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(angleTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField angleTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField locX;
    private javax.swing.JTextField locY;
    private javax.swing.JTextField locZ;
    private javax.swing.JTextField urlTF;
    // End of variables declaration//GEN-END:variables
}
