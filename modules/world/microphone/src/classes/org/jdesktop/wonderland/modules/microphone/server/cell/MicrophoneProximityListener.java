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
package org.jdesktop.wonderland.modules.microphone.server.cell;

//import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerUtil;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.sgs.app.AppContext;

import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

import java.lang.String;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

import com.jme.bounding.BoundingVolume;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ManagedObject;

/**
 * A server cell that provides a microphone proximity listener
 * @author jprovino
 */
public class MicrophoneProximityListener implements ProximityListenerSrv, ManagedObject {

    private static final Logger logger =
            Logger.getLogger(MicrophoneProximityListener.class.getName());
    String name;

    ManagedReference<ProximityComponentMO> proxRef;

    BoundingVolume[] bounds;

    public MicrophoneProximityListener(String name,
	    ManagedReference<ProximityComponentMO> proxRef, 
	    BoundingVolume[] bounds) {

        this.name = name;
	this.proxRef = proxRef;
	this.bounds = bounds;
    }

    public void viewEnterExit(boolean entered, CellID cellID,
            CellID viewCellID, BoundingVolume proximityVolume,
            int proximityIndex) {

	System.out.println("viewEnterExit:  " + entered + " cellID " + cellID
	    + " viewCellID " + viewCellID);

	//String callId = AudioManagerUtil.getCallID(viewCellID);
	String callId = viewCellID.toString();

	if (entered) {
	    if (proximityIndex == 0) {
	        cellEntered(callId);
	    } else {
		activeAreaEntered(callId);
	    }
	} else {
	    if (proximityIndex == 0) {
	        cellExited(callId);
	    } else {
		activeAreaExited(callId);
	    }
	}
    }

    private void cellEntered(String callId) {
        /*
         * The avatar has entered the Microphone cell.
         * Set the public and incoming spatializers for the avatar to be
         * the zero volume spatializer.
         * Set a private spatializer for the given fullVolume radius
         * for all the other avatars in the cell.
         * For each avatar already in the cell, set a private spatializer
         * for this avatar.
         */
        logger.info(callId + " entered microphone " + name);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        Player player = vm.getPlayer(callId);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
            AudioGroupSetup ags = new AudioGroupSetup();

            ags.spatializer = new FullVolumeSpatializer();

            ags.spatializer.setAttenuator(
                    DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);

            audioGroup = vm.createAudioGroup(name, ags);
        }

        audioGroup.addPlayer(player, new AudioGroupPlayerInfo(false,
                AudioGroupPlayerInfo.ChatType.PUBLIC));

        player.attenuateOtherGroups(audioGroup, 0, 0);
    }

    private void cellExited(String callId) {
        logger.info(callId + " exited microphone " + name);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
            logger.warning("Not a member of audio group " + name);
            return;
        }

        Player player = vm.getPlayer(callId);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

        audioGroup.removePlayer(player);

        if (audioGroup.getNumberOfPlayers() == 0) {
            vm.removeAudioGroup(audioGroup);
        }

        player.attenuateOtherGroups(audioGroup, AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
                AudioGroup.DEFAULT_LISTEN_ATTENUATION);
    }

    private void activeAreaEntered(String callId) {
	logger.info(callId + " entered active area " + name);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        Player player = vm.getPlayer(callId);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
	    logger.warning("Can't find audio group " + name);
	    return;
	}

	if (audioGroup.getPlayerInfo(player) == null) {
            audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true,
                AudioGroupPlayerInfo.ChatType.PUBLIC));
	} else {
	    audioGroup.setSpeaking(player, true);
	}
    }

    private void activeAreaExited(String callId) {
	logger.info(callId + " exited active area " + name);

        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        Player player = vm.getPlayer(callId);

        if (player == null) {
            logger.warning("Can't find player for " + callId);
            return;
        }

        AudioGroup audioGroup = vm.getAudioGroup(name);

        if (audioGroup == null) {
	    logger.warning("Can't find audio group " + name);
	    return;
	}

	audioGroup.setSpeaking(player, false);
    }

}
