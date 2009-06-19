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
package org.jdesktop.wonderland.client.jme.login;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import java.io.File;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jdesktop.wonderland.client.softphone.AudioQuality;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import com.sun.stun.NetworkAddressManager;
import com.sun.stun.NetworkAddressManager.NetworkAddress;

/**
 *
 * @author jkaplan
 * @author nsimpson
 */
public class LoginOptionsFrame extends javax.swing.JDialog {

    public LoginOptionsFrame() {
	super((JDialog) null, true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        initComponents();
        
        // set the OK button as the default
        getRootPane().setDefaultButton(okButton);

        // setup the list of IP addresses
        ipAddressComboBox.setModel(
                new DefaultComboBoxModel(NetworkAddressManager.getNetworkAddresses()));

        ipAddressComboBox.setSelectedItem(NetworkAddressManager.getDefaultNetworkAddress());

        // get the default client configuration
        //WonderlandClientConfig wcc = WonderlandClientConfig.getDefault();

        // setup the list of audio qualities
        audioQualityComboBox.setModel(
                new DefaultComboBoxModel(AudioQuality.values()));
        //audioQualityComboBox.setSelectedItem(wcc.getAudioQuality());

	AudioQuality audioQuality = AudioQuality.VPN;

	Preferences prefs = Preferences.userNodeForPackage(LoginOptionsFrame.class);

	String s = prefs.get(
	    "org.jdesktop.wonderland.modules.audiomanager.client.AUDIO_QUALITY", null);

	if (s != null) {
	    AudioQuality[] AudioQualityValues = AudioQuality.values();

	    for (int i = 0; i < AudioQualityValues.length; i++) {
		if (AudioQualityValues[i].toString().equals(s)) {
		    audioQuality = AudioQualityValues[i];
		    break;
		}
	    }
	}

        audioQualityComboBox.setSelectedItem(audioQuality);

        //wcc.setPhoneNumber("");

        // read in proxy information
        //switch (wcc.getProxyType()) {
        //    case NONE:
                noProxyRB.setSelected(true);
        //        break;
        //    case SYSTEM:
        //        systemProxyRB.setSelected(true);
        //        break;
        //    case USER:
        //        wlProxyRB.setSelected(true);
        //        break;
        //}
        //httpProxyTF.setText(wcc.getHttpProxyHost());
        //httpProxyPortTF.setText(String.valueOf(wcc.getHttpProxyPort()));
        //httpsProxyTF.setText(wcc.getHttpsProxyHost());
        //httpsProxyPortTF.setText(String.valueOf(wcc.getHttpsProxyPort()));
        //noProxyTF.setText(wcc.getNoProxyHosts());

        // get the right initial value for the http proxy
        updateHttpProxy();

        // set the cache directory
        //cacheLocation.setText(WonderlandConfigUtil.getWonderlandDir() +
        //        File.separator + "cache");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox2 = new javax.swing.JComboBox();
        proxyBG = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        networkPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        noProxyRB = new javax.swing.JRadioButton();
        systemProxyRB = new javax.swing.JRadioButton();
        wlProxyRB = new javax.swing.JRadioButton();
        httpProxyTFLabel = new javax.swing.JLabel();
        httpProxyTF = new javax.swing.JTextField();
        httpProxyPortTFLabel = new javax.swing.JLabel();
        httpProxyPortTF = new javax.swing.JTextField();
        httpsProxyTFLabel = new javax.swing.JLabel();
        httpsProxyTF = new javax.swing.JTextField();
        httpsProxyPortTFLabel = new javax.swing.JLabel();
        httpsProxyPortTF = new javax.swing.JTextField();
        noProxyTFLabel = new javax.swing.JLabel();
        noProxyTF = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ipAddressComboBox = new javax.swing.JComboBox();
        audioPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        audioQualityComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        phoneNumber = new javax.swing.JTextField();
        cachePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cacheLocation = new javax.swing.JTextField();
        clearCacheButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setTitle("Wonderland Options");
        setAlwaysOnTop(true);

        jTabbedPane1.setFont(new java.awt.Font("Dialog", 0, 13));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Web Proxies"));
        jPanel2.setFont(new java.awt.Font("Dialog", 0, 13));

        proxyBG.add(noProxyRB);
        noProxyRB.setFont(new java.awt.Font("Dialog", 0, 13));
        noProxyRB.setSelected(true);
        noProxyRB.setText("No proxy");
        noProxyRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noProxyRBActionPerformed(evt);
            }
        });

        proxyBG.add(systemProxyRB);
        systemProxyRB.setFont(new java.awt.Font("Dialog", 0, 13));
        systemProxyRB.setText("Use system proxy");
        systemProxyRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemProxyRBActionPerformed(evt);
            }
        });

        proxyBG.add(wlProxyRB);
        wlProxyRB.setFont(new java.awt.Font("Dialog", 0, 13));
        wlProxyRB.setText("Specify a proxy:");
        wlProxyRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wlProxyRBActionPerformed(evt);
            }
        });

        httpProxyTFLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        httpProxyTFLabel.setText("HTTP Proxy:");

        httpProxyTF.setFont(new java.awt.Font("Dialog", 0, 13));

        httpProxyPortTFLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        httpProxyPortTFLabel.setText("Port:");

        httpProxyPortTF.setFont(new java.awt.Font("Dialog", 0, 13));
        httpProxyPortTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                httpProxyPortTFActionPerformed(evt);
            }
        });

        httpsProxyTFLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        httpsProxyTFLabel.setText("HTTPS Proxy:");

        httpsProxyTF.setFont(new java.awt.Font("Dialog", 0, 13));

        httpsProxyPortTFLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        httpsProxyPortTFLabel.setText("Port:");

        httpsProxyPortTF.setFont(new java.awt.Font("Dialog", 0, 13));

        noProxyTFLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        noProxyTFLabel.setText("No Proxy For:");

        noProxyTF.setFont(new java.awt.Font("Dialog", 0, 13));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(noProxyRB)
                    .add(systemProxyRB)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(wlProxyRB)
                            .add(httpProxyTFLabel)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(httpsProxyTFLabel)
                                    .add(noProxyTFLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(6, 6, 6)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, httpsProxyTF)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, httpProxyTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(httpProxyPortTFLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(httpProxyPortTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(httpsProxyPortTFLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(httpsProxyPortTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))))
                            .add(noProxyTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(149, 149, 149))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(noProxyRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(systemProxyRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(wlProxyRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(httpProxyPortTFLabel)
                    .add(httpProxyPortTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(httpProxyTFLabel)
                    .add(httpProxyTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(httpsProxyTFLabel)
                    .add(httpsProxyTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(httpsProxyPortTFLabel)
                    .add(httpsProxyPortTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(noProxyTFLabel)
                    .add(noProxyTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Interface"));
        jPanel1.setFont(new java.awt.Font("Dialog", 0, 13));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 13));
        jLabel1.setText("Local IP Address:");

        ipAddressComboBox.setEditable(true);
        ipAddressComboBox.setFont(new java.awt.Font("Dialog", 0, 13));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ipAddressComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 188, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(242, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(ipAddressComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout networkPanelLayout = new org.jdesktop.layout.GroupLayout(networkPanel);
        networkPanel.setLayout(networkPanelLayout);
        networkPanelLayout.setHorizontalGroup(
            networkPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        networkPanelLayout.setVerticalGroup(
            networkPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(networkPanelLayout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Network", networkPanel);

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 13));
        jLabel4.setText("Audio Quality:");

        audioQualityComboBox.setFont(new java.awt.Font("Dialog", 0, 13));

        jLabel3.setText("Phone number to use instead of Softphone:");

        phoneNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                phoneNumberKeyTyped(evt);
            }
        });

        org.jdesktop.layout.GroupLayout audioPanelLayout = new org.jdesktop.layout.GroupLayout(audioPanel);
        audioPanel.setLayout(audioPanelLayout);
        audioPanelLayout.setHorizontalGroup(
            audioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(audioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(audioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(audioPanelLayout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(audioQualityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(1, 1, 1)
                .add(phoneNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );
        audioPanelLayout.setVerticalGroup(
            audioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(audioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(audioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(audioQualityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(46, 46, 46)
                .add(audioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(phoneNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(169, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Audio", audioPanel);

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 13));
        jLabel2.setText("Cache Directory:");

        cacheLocation.setEditable(false);
        cacheLocation.setFont(new java.awt.Font("Dialog", 0, 13));

        clearCacheButton.setFont(new java.awt.Font("Dialog", 1, 13));
        clearCacheButton.setText("Clear Cache");
        clearCacheButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearCacheButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout cachePanelLayout = new org.jdesktop.layout.GroupLayout(cachePanel);
        cachePanel.setLayout(cachePanelLayout);
        cachePanelLayout.setHorizontalGroup(
            cachePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cachePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cachePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clearCacheButton)
                    .add(cacheLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 254, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(182, Short.MAX_VALUE))
        );
        cachePanelLayout.setVerticalGroup(
            cachePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cachePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cachePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cacheLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clearCacheButton)
                .addContainerGap(199, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cache", cachePanel);

        okButton.setFont(new java.awt.Font("Dialog", 1, 13));
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Dialog", 1, 13));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(274, Short.MAX_VALUE)
                .add(okButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton)
                .add(209, 209, 209))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void wlProxyRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wlProxyRBActionPerformed
        updateHttpProxy();
}//GEN-LAST:event_wlProxyRBActionPerformed

private void systemProxyRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemProxyRBActionPerformed
        updateHttpProxy();
}//GEN-LAST:event_systemProxyRBActionPerformed

private void noProxyRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noProxyRBActionPerformed
        updateHttpProxy();
}//GEN-LAST:event_noProxyRBActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        //WonderlandClientConfig wcc = WonderlandClientConfig.getDefault();

        // store the audio quality
        //wcc.setAudioQuality((AudioQuality) audioQualityComboBox.getSelectedItem());

	Preferences prefs = Preferences.userNodeForPackage(LoginOptionsFrame.class);

	prefs.put("org.jdesktop.wonderland.modules.audiomanager.client.AUDIO_QUALITY",
            ((AudioQuality) audioQualityComboBox.getSelectedItem()).toString());

	SoftphoneControlImpl.getInstance().setAudioQuality((AudioQuality) audioQualityComboBox.getSelectedItem());
        
	// store the phone number
	//wcc.setPhoneNumber(phoneNumber.getText());

	System.setProperty("org.jdesktop.wonderland.modules.audiomanager.client.PHONE_NUMBER",
	    phoneNumber.getText());
	
        // store proxy properties
        //WonderlandClientConfig.ProxyType proxyType = WonderlandClientConfig.ProxyType.NONE;
        //if (systemProxyRB.isSelected()) {
        //    proxyType = WonderlandClientConfig.ProxyType.SYSTEM;
        //} else if (wlProxyRB.isSelected()) {
        //    proxyType = WonderlandClientConfig.ProxyType.USER;
        //    
        //    if (httpProxyTF.getText().trim().length() == 0 ||
        //        httpProxyPortTF.getText().trim().length() == 0)
        //    {
        //         JOptionPane.showMessageDialog(this, "Invalid proxy settings", 
        //                                       "Error", JOptionPane.ERROR_MESSAGE);
        //         return;
        //    }
        //        
        //    wcc.setHttpProxyHost(httpProxyTF.getText());
        //    wcc.setHttpProxyPort(Integer.parseInt(httpProxyPortTF.getText()));
        //    wcc.setHttpsProxyHost(httpsProxyTF.getText());
        //    wcc.setHttpsProxyPort(Integer.parseInt(httpsProxyPortTF.getText()));
        //    wcc.setNoProxyHosts(noProxyTF.getText());
        //
        //}
        //wcc.setProxyType(proxyType);
        
        // write out the user configuration
        //WonderlandConfigUtil.writeUserConfig(wcc);

	NetworkAddress na = null;

        // store the network preferences
	if (ipAddressComboBox.getSelectedItem() instanceof String) {
	    String ipAddress = (String) ipAddressComboBox.getSelectedItem();

	    try {
		InetAddress ia = InetAddress.getByName(ipAddress);
		na = new NetworkAddress("", ia);
	    } catch (UnknownHostException e) {
		System.out.println("Unknown host:  " + ipAddress);
	    }
	} else {
	    na = (NetworkAddress) ipAddressComboBox.getSelectedItem();
	}

	if (na != null) {
            NetworkAddressManager.setDefaultNetworkAddress(na);
	}
        
        // close the dialog
        setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void clearCacheButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCacheButtonActionPerformed
        //String cacheDir = WonderlandConfigUtil.getWonderlandDir() + 
        //                  File.separator + "cache";
        //String assetDBDir = WonderlandConfigUtil.getWonderlandDir() +
        //                  File.separator + "AssetDB";
        //
        //int result = JOptionPane.showConfirmDialog(this, 
        //        "WARNING\n" +
        //        "The contents of the following directories will be deleted: \n\n" +
        //        "    " + cacheDir + "\n    " + assetDBDir + "\n\n" + 
        //        "Would you like to continue?",
        //        "Confirm delete directories",
        //        JOptionPane.YES_NO_OPTION,
        //        JOptionPane.WARNING_MESSAGE);
        //if (result == JOptionPane.YES_OPTION) {
        //    deleteTree(new File(cacheDir));
        //    deleteTree(new File(assetDBDir));
        //}
}//GEN-LAST:event_clearCacheButtonActionPerformed

private void httpProxyPortTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_httpProxyPortTFActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_httpProxyPortTFActionPerformed

private void phoneNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumberKeyTyped
    String text = phoneNumber.getText();

    if (text == null || text.length() == 0) {
        audioQualityComboBox.setEnabled(true);
    } else {
        audioQualityComboBox.setEnabled(false);
    }
}//GEN-LAST:event_phoneNumberKeyTyped
    
    private void deleteTree(File file) {
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                deleteTree(child);
            } else {
                child.delete();
            }
        }
        
        file.delete();
    }

    private void updateHttpProxy() {
        boolean selected = wlProxyRB.isSelected();
            
        httpProxyTF.setEnabled(selected);
        httpProxyTFLabel.setEnabled(selected);
        httpProxyPortTF.setEnabled(selected);
        httpProxyPortTFLabel.setEnabled(selected);
        httpsProxyTF.setEnabled(selected);
        httpsProxyTFLabel.setEnabled(selected);
        httpsProxyPortTF.setEnabled(selected);
        httpsProxyPortTFLabel.setEnabled(selected);
        noProxyTF.setEnabled(selected);
        noProxyTFLabel.setEnabled(selected);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LoginOptionsFrame lfe = new LoginOptionsFrame();
                lfe.addWindowStateListener(new WindowStateListener() {
                    public void windowStateChanged(WindowEvent evt) {
                        if (evt.getNewState() == WindowEvent.WINDOW_CLOSED) {
                            System.exit(0);
                        }
                    }
                });
                
                lfe.setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel audioPanel;
    private javax.swing.JComboBox audioQualityComboBox;
    private javax.swing.JTextField cacheLocation;
    private javax.swing.JPanel cachePanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearCacheButton;
    private javax.swing.JTextField httpProxyPortTF;
    private javax.swing.JLabel httpProxyPortTFLabel;
    private javax.swing.JTextField httpProxyTF;
    private javax.swing.JLabel httpProxyTFLabel;
    private javax.swing.JTextField httpsProxyPortTF;
    private javax.swing.JLabel httpsProxyPortTFLabel;
    private javax.swing.JTextField httpsProxyTF;
    private javax.swing.JLabel httpsProxyTFLabel;
    private javax.swing.JComboBox ipAddressComboBox;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel networkPanel;
    private javax.swing.JRadioButton noProxyRB;
    private javax.swing.JTextField noProxyTF;
    private javax.swing.JLabel noProxyTFLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField phoneNumber;
    private javax.swing.ButtonGroup proxyBG;
    private javax.swing.JRadioButton systemProxyRB;
    private javax.swing.JRadioButton wlProxyRB;
    // End of variables declaration//GEN-END:variables
    
}
