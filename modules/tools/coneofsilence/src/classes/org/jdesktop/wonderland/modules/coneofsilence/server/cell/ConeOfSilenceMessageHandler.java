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

package org.jdesktop.wonderland.modules.coneofsilence.server.cell;

import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.modules.coneofsilence.common.ConeOfSilenceCellSetup;

import com.sun.mpk20.voicelib.app.AudioGroup;
import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
import com.sun.mpk20.voicelib.app.AudioGroupSetup;
import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;

import com.sun.voip.CallParticipant;
import com.sun.voip.client.connector.CallStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;


import java.io.IOException;
import java.io.Serializable;

import java.lang.String;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.MultipleParentException;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

import org.jdesktop.wonderland.server.UserManager;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import org.jdesktop.wonderland.server.setup.BeanSetupMO;

import org.jdesktop.wonderland.modules.coneofsilence.common.ConeOfSilenceCellSetup;

import org.jdesktop.wonderland.modules.coneofsilence.common.messages.ConeOfSilenceEnterCellMessage;

import org.jdesktop.wonderland.modules.coneofsilence.server.cell.ConeOfSilenceCellMO;

import com.jme.bounding.BoundingVolume;

import com.jme.math.Vector3f;

/**
 * A server cell that provides conference coneofsilence functionality
 * @author jprovino
 */
public class ConeOfSilenceMessageHandler implements Serializable, ComponentMessageReceiver {

    private static final Logger logger =
        Logger.getLogger(ConeOfSilenceMessageHandler.class.getName());
     
    private ManagedReference<ConeOfSilenceCellMO> coneOfSilenceCellMORef;

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    private String name;

    public ConeOfSilenceMessageHandler(ConeOfSilenceCellMO coneOfSilenceCellMO, String name) {
	this.name = name;

	coneOfSilenceCellMORef = AppContext.getDataManager().createReference(
	        (ConeOfSilenceCellMO) CellManagerMO.getCell(coneOfSilenceCellMO.getCellID()));

        ChannelComponentMO channelComponent = (ChannelComponentMO) 
	    coneOfSilenceCellMO.getComponent(ChannelComponentMO.class);

        if (channelComponent == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
	}

        channelComponent.addMessageReceiver(ConeOfSilenceEnterCellMessage.class, this);

        channelComponentRef = AppContext.getDataManager().createReference(channelComponent);

        ProximityComponentMO prox = new ProximityComponentMO(coneOfSilenceCellMO);
        BoundingVolume[] bounds = new BoundingVolume[1];

	bounds[0] = coneOfSilenceCellMO.getLocalBounds();

        prox.addProximityListener(new MyProximityListener(name), bounds );
        coneOfSilenceCellMO.addComponent(prox);
    }

    static class MyProximityListener implements ProximityListenerSrv {
	String name;

	public MyProximityListener(String name) {
	    this.name = name;
	}

        public void viewEnterExit(boolean entered, CellID cellID, 
		CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

	// TODO:  Use this after Paul adds the arugment
        //public void viewEnterExit(boolean entered, CellID cellID, 
	//	CellID avatarCellID, BoundingVolume proximityVolume, 
	//	int proximityIndex) {

	    System.out.println("viewEnterExit:  " + entered + " cellID " + cellID);

	    if (entered) {
		cellEntered(cellID);
		//cellEntered(avatarCellID);
	    } else {
		cellExited(cellID);
		//cellExited(avatarCellID);
	    }
        }

        private void cellEntered(CellID softphoneCellID) {
	    /*
	     * The avatar has entered the ConeOfSilence cell.
	     * Set the public and incoming spatializers for the avatar to be 
	     * the zero volume spatializer.
	     * Set a private spatializer for the given fullVolume radius
	     * for all the other avatars in the cell.
	     * For each avatar already in the cell, set a private spatializer
	     * for this avatar.
	     */
	    String callId = softphoneCellID.toString();

	    logger.warning(callId + " entered cone " + name + " avatar cell ID "
	        + callId);

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

	    audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true, 
	        AudioGroupPlayerInfo.ChatType.EXCLUSIVE));

	    player.attenuateOtherGroups(audioGroup, 0, 0);
        }

        private void cellExited(CellID softphoneCellID) {
	    String callId = softphoneCellID.toString();

	    logger.warning(callId + " exited cone " + name + " avatar cell ID "
	        + callId);

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

	    if (audioGroup.getPlayers().size() == 0) {
	        vm.removeAudioGroup(name);
	    }

	    player.attenuateOtherGroups(audioGroup, AudioGroup.DEFAULT_SPEAKING_ATTENUATION,
	        AudioGroup.DEFAULT_LISTEN_ATTENUATION);
        }
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final CellMessage message) {

	ConeOfSilenceEnterCellMessage msg = (ConeOfSilenceEnterCellMessage) message;

	logger.warning("Got message " + msg);
    }

}
