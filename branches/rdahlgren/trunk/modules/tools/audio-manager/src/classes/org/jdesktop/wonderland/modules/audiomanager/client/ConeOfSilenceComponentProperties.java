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

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;

import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

/**
 * Properties panel for the cone of silence component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@PropertiesFactory(ConeOfSilenceComponentServerState.class)
public class ConeOfSilenceComponentProperties extends javax.swing.JPanel
        implements PropertiesFactorySPI {

    private CellPropertiesEditor editor = null;
    private String originalName = null;
    private double originalFullVolumeRadius = 0.0;
    private int originalOutsideAudioVolume = 0;
    private SpinnerNumberModel fullVolumeRadiusModel = null;

    /** Creates new form ConeOfSilenceComponentProperties */
    public ConeOfSilenceComponentProperties() {
        initComponents();

        // Set the maximum and minimum values for the volume radius spinner
        Double value = new Double(1);
        Double min = new Double(0);
        Double max = new Double(100);
        Double step = new Double(1);
        fullVolumeRadiusModel = new SpinnerNumberModel(value, min, max, step);
        fullVolumeRadiusSpinner.setModel(fullVolumeRadiusModel);

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(new NameTextFieldListener());
        fullVolumeRadiusModel.addChangeListener(new RadiusChangeListener());
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        return "Cone Of Silence";
    }

    /**
     * @{inheritDoc}
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @{inheritDoc}
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @{inheritDoc}
     */
    public void open() {
        CellServerState cellServerState = editor.getCellServerState();
        ConeOfSilenceComponentServerState state =
                (ConeOfSilenceComponentServerState)cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);

        if (state == null) {
            return;
        }

        originalName = state.getName();
        originalOutsideAudioVolume = VolumeUtil.getClientVolume(state.getOutsideAudioVolume());
        originalFullVolumeRadius = state.getFullVolumeRadius();

        nameTextField.setText(originalName);
        outsideAudioVolumeSlider.setValue(originalOutsideAudioVolume);
        fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
    }

    /**
     * @{inheritDoc}
     */
    public void close() {
        // Do nothing
    }

    /**
     * @{inheritDoc}
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component. If it does not exist, then return, but we could always
        // create a new one really.
        CellServerState cellServerState = editor.getCellServerState();
        ConeOfSilenceComponentServerState state =
                (ConeOfSilenceComponentServerState)cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);
        if (state == null) {
            return;
        }

        state.setName(nameTextField.getText());
        state.setFullVolumeRadius((Double) fullVolumeRadiusModel.getValue());
        state.setOutsideAudioVolume(VolumeUtil.getServerVolume(outsideAudioVolumeSlider.getValue()));
        editor.addToUpdateList(state);
    }

    /**
     * @{inheritDoc}
     */
    public void restore() {
        // Reset the original values to the GUI
        nameTextField.setText(originalName);
        outsideAudioVolumeSlider.setValue(originalOutsideAudioVolume);
        fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor
     */
    class RadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (editor == null) { 
		return;
	    }

            Double radius = (Double) fullVolumeRadiusModel.getValue();

	    if (radius != originalFullVolumeRadius) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
            } else {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
            }
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class NameTextFieldListener implements DocumentListener {
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

            String name = nameTextField.getText();

	    System.out.println("COS name " + name + " orig " + originalName);

	    if (name.equals(originalName) == false) {
		editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
            } else {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
	    }
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
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        outsideAudioVolumeSlider = new javax.swing.JSlider();

        jLabel1.setText("Name:");

        jLabel2.setText("Full Volume Radius:");

        jLabel3.setText("Outside Audio Volume:");

        outsideAudioVolumeSlider.setMajorTickSpacing(1);
        outsideAudioVolumeSlider.setMaximum(10);
        outsideAudioVolumeSlider.setPaintLabels(true);
        outsideAudioVolumeSlider.setPaintTicks(true);
        outsideAudioVolumeSlider.setSnapToTicks(true);
        outsideAudioVolumeSlider.setValue(0);
        outsideAudioVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                outsideAudioVolumeSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(30, 30, 30)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .add(34, 34, 34))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outsideAudioVolumeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(nameTextField))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(outsideAudioVolumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(174, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void outsideAudioVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_outsideAudioVolumeSliderStateChanged
        if (editor == null) { 
	    return;
	}

        int outsideAudioVolume = outsideAudioVolumeSlider.getValue();

	if (originalOutsideAudioVolume != outsideAudioVolume) {
	    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
        } else {
            editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
        }
    }//GEN-LAST:event_outsideAudioVolumeSliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JSlider outsideAudioVolumeSlider;
    // End of variables declaration//GEN-END:variables
}