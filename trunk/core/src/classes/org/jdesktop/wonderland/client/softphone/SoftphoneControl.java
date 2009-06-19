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
package org.jdesktop.wonderland.client.softphone;

import org.jdesktop.wonderland.common.cell.CellID;

import java.io.IOException;

public interface SoftphoneControl {
    public static final String SOFTPHONE_PROP =
            "org.jdesktop.wonderland.client.softphone.jar";
 
    /*
     * Start the softphone and wait for it to determine its address.
     */
    public String startSoftphone(String userName, String registrarAddress,
	    int registrarTimeout, String localHost) throws IOException;
    
    public void stopSoftphone();

    public void setCallID(String callID);

    public String getCallID();

    public void register(String registrarAddress) throws IOException;

    public boolean isRunning();

    public boolean isConnected() throws IOException;

    public void setVisible(boolean isVisible) throws IOException;

    public boolean isVisible();

    public void mute(boolean isMuted) throws IOException;

    public boolean isMuted();
    
    public AudioQuality getAudioQuality();

    public void setAudioQuality(AudioQuality quality) throws IOException;

    public void sendCommandToSoftphone(String cmd) throws IOException;
    
    public void runLineTest() throws IOException;
    
    public void logAudioProblem() throws IOException;

    public void addSoftphoneListener(SoftphoneListener listener);

    public void removeSoftphoneListener(SoftphoneListener listener);

    public void addMicrophoneVuMeterListener(MicrophoneVuMeterListener listener);

    public void removeMicrophoneVuMeterListener(MicrophoneVuMeterListener listener);

    public void startVuMeter(boolean startVuMeter);

}
