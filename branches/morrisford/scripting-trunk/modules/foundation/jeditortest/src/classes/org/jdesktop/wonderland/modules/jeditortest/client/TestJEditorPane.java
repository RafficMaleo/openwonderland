/*
 * TestJEditorPane.java
 *
 * Created on December 12, 2008, 1:34 PM
 */

package org.jdesktop.wonderland.modules.jeditortest.client;

import java.net.URL;

/**
 *
 * @author  dj
 */
public class TestJEditorPane extends javax.swing.JPanel {

    /** Creates new form TestJEditorPane */
    public TestJEditorPane() {
        initComponents();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

 	String resourceName = "/org/jdesktop/wonderland/modules/jeditortest/client/resources/test.html";
 	URL url = this.getClass().getResource(resourceName);
 	try {
 	    jEditorPane1 = new javax.swing.JEditorPane(url);
 	} catch (Exception ex) {
 	    System.err.println("Cannot find resource " + url);
 	}
        jLabel1.setText("<html>This is 1ine 1<br>This is line 2<br>This is line 3<html>");

        add(jLabel1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(jEditorPane1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}