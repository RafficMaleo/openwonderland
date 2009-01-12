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
package org.jdesktop.wonderland.modules.phone.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import  org.jdesktop.wonderland.modules.phone.common.CallListing;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.phone.common.messages.CallEndedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallEstablishedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.CallInvitedResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.EndCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.EndCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.JoinCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.JoinCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.LockUnlockMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.LockUnlockResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneControlMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlaceCallMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlaceCallResponseMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PlayTreatmentMessage;

import org.jdesktop.wonderland.client.jme.JmeClientMain;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 *
 * @author jprovino
 */
public class PhoneMessageHandler {

    private static final Logger logger =
            Logger.getLogger(PhoneMessageHandler.class.getName());

    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    
    private CallListing mostRecentCallListing;
         
    private boolean projectorState;
    
    private ProjectorStateUpdater projectorStateUpdater;

    private String name;

    private PhoneCell phoneCell;

    private PhoneForm phoneForm;

    private ChannelComponent channelComp;

    public PhoneMessageHandler(PhoneCell phoneCell) {
	this.phoneCell = phoneCell;

	channelComp = phoneCell.getComponent(ChannelComponent.class);

	logger.fine("Channel comp is " + channelComp);

        ChannelComponent.ComponentMessageReceiver msgReceiver =
	    new ChannelComponent.ComponentMessageReceiver() {
                public void messageReceived(CellMessage message) {
                    PhoneResponseMessage msg = (PhoneResponseMessage)message;

		    processMessage((PhoneResponseMessage) message);
                }
            };

        channelComp.addMessageReceiver(CallEndedResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(CallEstablishedResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(CallInvitedResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(EndCallResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(JoinCallResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(LockUnlockResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(PhoneResponseMessage.class, msgReceiver);
        channelComp.addMessageReceiver(PlaceCallResponseMessage.class, msgReceiver);
    }

    public void phoneSelected() {
	if (phoneForm == null) {
	    boolean locked = phoneCell.getLocked();
	    boolean passwordProtected = true;

	    if (phoneCell.getPassword() == null || phoneCell.getPassword().length() == 0) {
		locked = false;
		passwordProtected = false;
	    }

	    phoneForm = new PhoneForm(phoneCell.getCellID(), channelComp,
		this, locked, phoneCell.getPhoneNumber(), passwordProtected);
	}

	phoneForm.setVisible(true);
    }

    public void processMessage(final PhoneResponseMessage message) {
	if (message instanceof CallEndedResponseMessage) {
	    final CallEndedResponseMessage msg = (CallEndedResponseMessage) message;

            if (msg.wasSuccessful() == false) {    
                logger.warning("Failed END_CALL");
		return;
	    }

            CallListing listing = msg.getCallListing();
        
            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		return;
	    }

            if (mostRecentCallListing.isPrivate()) {
		//This was a private call...
                //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(false); 
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    phoneForm.setCallEnded(msg.getReasonCallEnded());
                }
            });

	    return;
	}

	if (message instanceof LockUnlockResponseMessage) {
	    LockUnlockResponseMessage msg = (LockUnlockResponseMessage) message;

	    phoneForm.changeLocked(msg.getLocked(), msg.wasSuccessful());
	    return;
	}

	if (message instanceof PhoneResponseMessage == false) {
	    logger.warning("Invalid message:  " + message);
	    return;
	}

	PhoneResponseMessage msg = (PhoneResponseMessage) message;

        CallListing listing = msg.getCallListing();

	if (msg instanceof PlaceCallResponseMessage) {
	    logger.fine("Got place call response...");

            if (msg.wasSuccessful() == false) {
                logger.warning("Failed PLACE_CALL!");
		return;
	    }

            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		logger.warning("Didn't find listing...");
		return;
	    }

	    /*
	     * Make sure the most recent listing has the right private 
	     * client name.
	     */
	    mostRecentCallListing.setPrivateClientName(listing.getPrivateClientName());

	    /*
	     * Set the call ID used by the server.
	     */
	    logger.fine("Updating listing with " + listing.getExternalCallID());

	    mostRecentCallListing.setExternalCallID(listing.getExternalCallID());

            /*
	     * This is a confirmation msg for OUR call. 
	     * Update the form's selection.                        
	     */
            if (listing.isPrivate()) {
                //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(true);
            }
	    return;
	}

	if (msg instanceof JoinCallResponseMessage)  {
            //Hearing back from the server means this call has joined the world.
            if (msg.wasSuccessful() == false) {
                logger.warning("Failed JOIN_CALL");
		return;
	    }

	    if (mostRecentCallListing == null || 
		    listing.equals(mostRecentCallListing) == false) {

		return;
	    }

            //This is a JOIN confirmation msg for OUR call. So we should no longer be whispering...
            //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(false);                       
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    phoneForm.setCallEstablished(false);
                }
            });
	    return;
	}
            
	if (msg instanceof CallInvitedResponseMessage) {
            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		return;  // we didn't start this call
	    }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    phoneForm.setCallInvited();
                }
            });
            return;
	}
            
	if (msg instanceof CallEstablishedResponseMessage) {
	    logger.fine("Got est resp");

            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		logger.warning("no listing " + mostRecentCallListing 
		    + " listing " + listing);

		return;  // we didn't start this call
	    }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
		    synchronized (phoneForm) {
                        phoneForm.setCallEstablished(
			    mostRecentCallListing.isPrivate());
		    }
                }
            });
            
            return;
        }
    }
    
    public void leftChannel(ClientChannel arg0) {
        // ignore
    }
    
    public void placeCall(CallListing listing) {
	WonderlandSession session = phoneCell.getCellCache().getSession();

	SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();

	listing.setSoftphoneCallID(sc.getCallID());

        PlaceCallMessage msg = new PlaceCallMessage(phoneCell.getCellID(), 
	    listing);

	logger.fine("Sending place call message " + phoneCell.getCellID() + " " 
	    + " softphoneCallID " + sc.getCallID() + " " + listing);

	synchronized (phoneForm) {
            mostRecentCallListing = listing;      

            channelComp.send(msg);    
	}
    }
    
    public void joinCall() {
	WonderlandSession session = phoneCell.getCellCache().getSession();

        JoinCallMessage msg = new JoinCallMessage(phoneCell.getCellID(), 
	    mostRecentCallListing);

        channelComp.send(msg);
    }
    
    public void endCall() {        
	logger.fine("call id is " + mostRecentCallListing.getExternalCallID());

        EndCallMessage msg = new EndCallMessage(phoneCell.getCellID(), 
	    mostRecentCallListing);

        channelComp.send(msg); 
    }
    
    public void dtmf(char c) {
        String treatment = "dtmf:" + c;

        PlayTreatmentMessage msg = new PlayTreatmentMessage(phoneCell.getCellID(), 
	    mostRecentCallListing, treatment, true);

	channelComp.send(msg);
    }
    
        public void processEvent() {
                // react to mouse enter/exit events
        }

}
    
    class doPhoneFormRunnable implements Runnable {
        
        private PhoneForm phoneForm;

        public doPhoneFormRunnable(PhoneForm phoneForm) {
            this.phoneForm = phoneForm;
        }
        
        public void run() {                                        
            //Pop up a phoneForm here and get the address info.            
            phoneForm.setVisible(true);
        }        
    }
      
    class ProjectorStateUpdater extends Thread {

        private boolean running = true;
        
	public ProjectorStateUpdater() {
	    start();
	}

	public void run() {
	    while (running) {
		updateProjectorState();

		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	    }
	}
        
        public void kill() {
            running = false;
        }

        private void updateProjectorState() {
        
            //boolean targetState = !callListingMap.isEmpty();
            boolean targetState = false;

            //ArrayList<Cell> childList = new ArrayList<Cell>();
            //getAllContainedCells(childList);
            //Iterator<Cell> iter = childList.iterator();
            //while(iter.hasNext()) {
            //    Cell c = iter.next();
                //if (c instanceof AvatarOrbCell) {
                //    targetState = true;
                //    break;
                //}
            //}
        
            //Are we switching states?
            //if (projectorState == targetState){
	    //    return;
	    //}

	    if (targetState){
                //Turn on        
		//cellLocal.addChild(projectorBG);
            } else {
                //Turn off
                //projectorBG.detach();
            }
            
            //projectorState = targetState;
	}
    
    }
