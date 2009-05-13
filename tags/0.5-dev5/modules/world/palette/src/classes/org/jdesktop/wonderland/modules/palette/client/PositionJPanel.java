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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ComponentChangeListener;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A special properties editor panel that edits the transform of the cell. It
 * interacts with the Movable component directly.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PositionJPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger(PositionJPanel.class.getName());
    private Cell cell = null;
    private CellPropertiesEditor editor = null;
    private MovableComponent movableComponent = null;

    /* Various listener on the Cell and Swing JSpinners */
    private ComponentChangeListener componentListener = null;
    private TransformChangeListener transformListener = null;
    private ChangeListener translationListener = null;
    private ChangeListener rotationListener = null;
    private ChangeListener scaleListener = null;

    /* Models for the Swing JSpinners */
    private SpinnerNumberModel xTranslationModel = null;
    private SpinnerNumberModel yTranslationModel = null;
    private SpinnerNumberModel zTranslationModel = null;
    private SpinnerNumberModel xScaleModel = null;
    private SpinnerNumberModel yScaleModel = null;
    private SpinnerNumberModel zScaleModel = null;
    private SpinnerNumberModel xRotationModel = null;
    private SpinnerNumberModel yRotationModel = null;
    private SpinnerNumberModel zRotationModel = null;

    /*
     * This boolean indicates whether the values of the spinners are being
     * set programmatically, e.g. when a transform changed event has been
     * received from the Cell. In such a case, we do not want to generate a
     * new message to the movable component
     */
    private boolean setLocal = false;
    
    /** Creates new form PositionJPanel */
    public PositionJPanel(Cell cell) {
        // Initialize the GUI components
        this.cell = cell;
        initComponents();

        // Set the maximum and minimum values for each
        Float value = new Float(0);
        Float min = new Float(Float.NEGATIVE_INFINITY);
        Float max = new Float(Float.POSITIVE_INFINITY);
        Float step = new Float(0.1);
        xTranslationModel = new SpinnerNumberModel(value, min, max, step);
        yTranslationModel = new SpinnerNumberModel(value, min, max, step);
        zTranslationModel = new SpinnerNumberModel(value, min, max, step);
        translationXTF.setModel(xTranslationModel);
        translationYTF.setModel(yTranslationModel);
        translationZTF.setModel(zTranslationModel);

        value = new Float(1);
        min = new Float(0);
        xScaleModel = new SpinnerNumberModel(value, min, max, step);
        yScaleModel = new SpinnerNumberModel(value, min, max, step);
        zScaleModel = new SpinnerNumberModel(value, min, max, step);
        scaleXTF.setModel(xScaleModel);
        scaleYTF.setModel(yScaleModel);
        scaleZTF.setModel(zScaleModel);

        value = new Float(0);
        min = new Float(-360);
        max = new Float(360);
        step = new Float(1);
        xRotationModel = new SpinnerNumberModel(value, min, max, step);
        yRotationModel = new SpinnerNumberModel(value, min, max, step);
        zRotationModel = new SpinnerNumberModel(value, min, max, step);
        rotationXTF.setModel(xRotationModel);
        rotationYTF.setModel(yRotationModel);
        rotationZTF.setModel(zRotationModel);

        // Fetch the movable component. For now, if it does not exist, then
        // turn off everything
        movableComponent = cell.getComponent(MovableComponent.class);
        if (movableComponent == null) {
            translationXTF.setEnabled(false);
            translationYTF.setEnabled(false);
            translationZTF.setEnabled(false);
            rotationXTF.setEnabled(false);
            rotationYTF.setEnabled(false);
            rotationZTF.setEnabled(false);
            scaleXTF.setEnabled(false);
            scaleYTF.setEnabled(false);
            scaleZTF.setEnabled(false);
        }

        // Listen for changes, if there is a movable component added or removed
        // update the state of the fields
        componentListener = new ComponentChangeListener() {
            public void componentChanged(Cell cell, ChangeType type, CellComponent component) {
                if (type == ChangeType.ADDED && component instanceof MovableComponent) {
                    movableComponent = (MovableComponent)component;
                    translationXTF.setEnabled(true);
                    translationYTF.setEnabled(true);
                    translationZTF.setEnabled(true);
                    rotationXTF.setEnabled(true);
                    rotationYTF.setEnabled(true);
                    rotationZTF.setEnabled(true);
                    scaleXTF.setEnabled(true);
                    scaleYTF.setEnabled(true);
                    scaleZTF.setEnabled(true);
                }
            }
        };
        cell.addComponentChangeListener(componentListener);

        // If it does not exist, attempt to add the movable component. Create
        // a suitable message using only the server-side movable component
        // class name and send over the cell channel.
        if (movableComponent == null) {
            String className = "org.jdesktop.wonderland.server.cell.MovableComponentMO";
            CellServerComponentMessage cscm = CellServerComponentMessage.newAddMessage(cell.getCellID(), className);
            ResponseMessage response = cell.sendCellMessageAndWait(cscm);
            if (response instanceof ErrorMessage) {
                logger.log(Level.WARNING, "Unable to add movable component " +
                        "for Cell " + cell.getName() + " with ID " +
                        cell.getCellID(), ((ErrorMessage) response).getErrorCause());
            }
        }

        // Listen for changes to the cell transform that may be done by other
        // parts of this client or other clients. We need to do this updating
        // of the GUI in the AWT Event Thread. We also want to make a note that
        // the values are being set programmatically so they do not spawn extra
        // messages to the movable component.
        transformListener = new TransformChangeListener() {
            public void transformChanged(Cell cell, ChangeSource source) {
//                CellTransform cellTransform = cell.getLocalTransform();
//                Quaternion rotation = cellTransform.getRotation(null);
//                float[] angles = rotation.toAngles(new float[3]);
//                System.out.println("CELL TRANSFORM CHANGED NEW ROTATION VALUES " +
//                        angles[0] + " " + angles[1] + " " + angles[2]);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            setLocalChanges(true);
                            updateGUI();
                        } finally {
                            setLocalChanges(false);
                        }
                    }
                });
            }
        };
        cell.addTransformChangeListener(transformListener);

        // Listen for changes to the translation values and update the cell as
        // a result. Only update the result if it doesn't happen because the
        // value in the spinner is changed programmatically. The value of
        // 'setLocal' is set always in the AWT Event Thread, the same thread
        // as this listener.
        translationListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (setLocal == false) {
                    updateTranslation();
                }
            }
        };
        xTranslationModel.addChangeListener(translationListener);
        yTranslationModel.addChangeListener(translationListener);
        zTranslationModel.addChangeListener(translationListener);

        // Listen for changes to the rotation values and update the cell as a
        // result. See the comments above for 'translationListener' too.
        rotationListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
//                float x = (Float) xRotationModel.getValue();
//                float y = (Float) yRotationModel.getValue();
//                float z = (Float) zRotationModel.getValue();
//                System.out.println("STATE CHANGED in JSPINNER VALUES setLocal=" +
//                        setLocal + " " + x + " " + y + " " + z + " SOURCE " +
//                        e.getSource());

                if (setLocal == false) {
                    updateRotation();
                }
            }
        };
        xRotationModel.addChangeListener(rotationListener);
        yRotationModel.addChangeListener(rotationListener);
        zRotationModel.addChangeListener(rotationListener);

        // Listen for changes to the scale values and update the cell as a
        // result. See the comments above for 'translationListener' too.
        scaleListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (setLocal == false) {
                    updateScale();
                }
            }
        };
        xScaleModel.addChangeListener(scaleListener);
        yScaleModel.addChangeListener(scaleListener);
        zScaleModel.addChangeListener(scaleListener);
    }

    /**
     * Sets whether the changes being made to the JSpinners are doing so
     * programmatically, rather than via a movable event. This is used to
     * make sure that requests to the movable component are not made at the
     * wrong time.
     *
     * @param isLocal True to indicate the JSpinner values are being set
     * programmatically.
     */
    void setLocalChanges(boolean isLocal) {
        setLocal = isLocal;
    }

    /**
     * Cleans up any listeners and resources that are present on this panel.
     * Once this method is invoked, the panel can no longer be used again.
     */
    public void dispose() {
        cell.removeComponentChangeListener(componentListener);
        cell.removeTransformChangeListener(transformListener);
        xTranslationModel.removeChangeListener(translationListener);
        yTranslationModel.removeChangeListener(translationListener);
        zTranslationModel.removeChangeListener(translationListener);
        xRotationModel.removeChangeListener(rotationListener);
        yRotationModel.removeChangeListener(rotationListener);
        zRotationModel.removeChangeListener(rotationListener);
        xScaleModel.removeChangeListener(scaleListener);
        yScaleModel.removeChangeListener(scaleListener);
        zScaleModel.removeChangeListener(scaleListener);

        componentListener = null;
        transformListener = null;
        translationListener = null;
        rotationListener = null;
        scaleListener = null;
    }

    /**
     * Returns the properties JPanel object.
     *
     * @param editor The editor for all of the cell's properties
     * @param The properties JPanel
     */
    public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
        this.editor = editor;
        return this;
    }

    /**
     * Updates the GUI based upon the given CellTransform
     */
    public void updateGUI() {
        // Fetch the current transform from the movable component
        CellTransform cellTransform = cell.getLocalTransform();
        Vector3f translation = cellTransform.getTranslation(null);
        Quaternion rotation = cellTransform.getRotation(null);
        Vector3f scale = cellTransform.getScaling(null);
        float[] angles = rotation.toAngles(new float[3]);

        // Update the translation spinners
        translationXTF.setValue(translation.x);
        translationYTF.setValue(translation.y);
        translationZTF.setValue(translation.z);

        // Update the rotation spinners only if they have changed
//        System.out.println("UPDATING GUI WITH ROTATION " +
//                Math.toDegrees(angles[0]) + " " + Math.toDegrees(angles[1]) +
//                " " + Math.toDegrees(angles[2]));

        rotationXTF.setValue((float) Math.toDegrees(angles[0]));
        rotationYTF.setValue((float) Math.toDegrees(angles[1]));
        rotationZTF.setValue((float) Math.toDegrees(angles[2]));

        // Update the scale spinners only if they have changes
        scaleXTF.setValue((float) scale.x);
        scaleYTF.setValue((float) scale.y);
        scaleZTF.setValue((float) scale.z);
    }

    /**
     * Updates the translation of the cell with the given values of the GUI.
     */
    private void updateTranslation() {
        float x = (Float) xTranslationModel.getValue();
        float y = (Float) yTranslationModel.getValue();
        float z = (Float) zTranslationModel.getValue();

        Vector3f translation = new Vector3f(x, y, z);
        if (movableComponent != null) {
            CellTransform cellTransform = cell.getLocalTransform();
            cellTransform.setTranslation(translation);
            movableComponent.localMoveRequest(cellTransform);
        }
    }

    /**
     * Updates the rotation of the cell with the given values of the GUI.
     */
    private void updateRotation() {
        // Fetch the x, y, z rotation values from the GUI in degrees
        float x = (Float) xRotationModel.getValue();
        float y = (Float) yRotationModel.getValue();
        float z = (Float) zRotationModel.getValue();

//        System.out.println("NEW ROTATION SET IN GUI " + x + " " + y + " " + z);

        // Convert to radians
        x = (float)Math.toRadians(x);
        y = (float)Math.toRadians(y);
        z = (float)Math.toRadians(z);

        Quaternion newRotation = new Quaternion(new float[] { x, y, z });
        if (movableComponent != null) {
//            System.out.println("SENDING ROTATION VALUES TO SERVER");
            CellTransform cellTransform = cell.getLocalTransform();
            cellTransform.setRotation(newRotation);
            movableComponent.localMoveRequest(cellTransform);
        }
    }

    /**
     * Updates the scale of the cell with the given values of the GUI.
     */
    private void updateScale() {
        float x = (Float) xScaleModel.getValue();
        float y = (Float) yScaleModel.getValue();
        float z = (Float) zScaleModel.getValue();

        Vector3f scale = new Vector3f(x, y, z);            
        if (movableComponent != null) {
            CellTransform cellTransform = cell.getLocalTransform();
            cellTransform.setScaling(scale);
            movableComponent.localMoveRequest(cellTransform);
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

        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        translationXTF = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        translationYTF = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        translationZTF = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        rotationYTF = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        rotationXTF = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        rotationZTF = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        scaleYTF = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        scaleXTF = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        scaleZTF = new javax.swing.JSpinner();

        jLabel7.setText("Location");

        jLabel6.setText("X :");

        jLabel8.setText("Y :");

        jLabel9.setText("Z :");

        jLabel10.setText("Rotation");

        jLabel13.setText("Y :");

        jLabel11.setText("X :");

        jLabel12.setText("Z :");

        jLabel14.setText("Scale");

        jLabel15.setText("Y :");

        jLabel16.setText("X :");

        jLabel17.setText("Z :");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel14)
                            .add(layout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel15)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(scaleYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel16)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(scaleXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel17)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(scaleZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                        .add(236, 236, 236))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel6)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(translationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel8)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(translationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel9)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(translationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 32, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(layout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel13)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(rotationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel11)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(rotationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel12)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(rotationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))))
                .add(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(rotationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(rotationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel13))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(rotationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel12)))
                    .add(layout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(translationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(translationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel8))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(translationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9))
                        .add(18, 18, 18)
                        .add(jLabel14)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel16)
                            .add(scaleXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(scaleYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel15))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(scaleZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel17))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner rotationXTF;
    private javax.swing.JSpinner rotationYTF;
    private javax.swing.JSpinner rotationZTF;
    private javax.swing.JSpinner scaleXTF;
    private javax.swing.JSpinner scaleYTF;
    private javax.swing.JSpinner scaleZTF;
    private javax.swing.JSpinner translationXTF;
    private javax.swing.JSpinner translationYTF;
    private javax.swing.JSpinner translationZTF;
    // End of variables declaration//GEN-END:variables
}
