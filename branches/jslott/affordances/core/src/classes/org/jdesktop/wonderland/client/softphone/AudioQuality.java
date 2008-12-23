/**
 * Project Looking Glass
 *
 * $RCSfile: SipStarter.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.28 $
 * $Date: 2007/12/17 19:45:54 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.softphone;

    /**
     * Different audio qualities
     */
public enum AudioQuality {
    MINIMUM (8000, 1, 8000, 1, "Minimum (8k mono)"),
    STEREO  (8000, 2, 8000, 1, "Stereo (8k stereo)"),
    VPN     (16000, 2, 16000, 1, "VPN (16k stereo)"),
    BEST    (44100, 2, 44100, 1, "Intranet (44.1k stereo)");
    
    private final int sampleRate;
    private final int channels;
    private final int transmitSampleRate;
    private final int transmitChannels;
    private final String description;
    
    AudioQuality(int sampleRate, int channels, int transmitSampleRate, 
                 int transmitChannels, String description) 
    {
        this.sampleRate         = sampleRate;
        this.channels           = channels;
        this.transmitSampleRate = transmitSampleRate;
        this.transmitChannels   = transmitChannels;
        this.description        = description;
    }
    
    public int sampleRate() {
        return sampleRate;
    }
    
    public int channels() {
        return channels;
    }
    
    public int transmitSampleRate() {
        return transmitSampleRate;
    }
    
    public int transmitChannels() {
        return transmitChannels;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
