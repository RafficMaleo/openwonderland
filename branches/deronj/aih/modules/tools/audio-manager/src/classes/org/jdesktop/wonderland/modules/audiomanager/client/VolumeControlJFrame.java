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

import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author  jp
 */
public class VolumeControlJFrame extends javax.swing.JFrame {

    private CellID cellID;
    private VolumeChangeListener listener;
    private String name;
    private String otherCallID;

    /** Creates new form VolumeControlJFrame */
    public VolumeControlJFrame() {
        initComponents();
    }

    public VolumeControlJFrame(CellID cellID, VolumeChangeListener listener, String name, String otherCallID,
	    int volume) {

	this.cellID = cellID;
	this.listener = listener;
	this.otherCallID = otherCallID;

        initComponents();

	volumeControlSlider.setValue(volume);

	setTitle(name);

	setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        volumeControlSlider = new javax.swing.JSlider();

        setTitle("Volume Control");

        volumeControlSlider.setMajorTickSpacing(1);
        volumeControlSlider.setMaximum(10);
        volumeControlSlider.setMinorTickSpacing(1);
        volumeControlSlider.setPaintLabels(true);
        volumeControlSlider.setPaintTicks(true);
        volumeControlSlider.setSnapToTicks(true);
        volumeControlSlider.setValue(5);
        volumeControlSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeControlSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(volumeControlSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(volumeControlSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void volumeControlSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeControlSliderStateChanged

    javax.swing.JSlider source = (javax.swing.JSlider) evt.getSource();

    int volume = source.getValue();

    listener.volumeChanged(cellID, otherCallID, volume);
}//GEN-LAST:event_volumeControlSliderStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VolumeControlJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider volumeControlSlider;
    // End of variables declaration//GEN-END:variables

}