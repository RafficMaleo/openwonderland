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
package org.jdesktop.wonderland.client.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.PickInfo;
import java.util.Iterator;
import org.jdesktop.wonderland.common.InternalAPI;
import java.util.logging.Logger;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.common.ThreadManager;
import org.jdesktop.wonderland.client.input.InputManager.FocusChange;
import org.jdesktop.wonderland.client.jme.input.SwingEnterExitEvent3D;

/**
 * The abstract base class for an Event Distributor singleton. The Entity Distributor is part of the input 
 * subsystem which distributes events throughout the scene graph according to the information provided by 
 * the entity event listeners. It also supports a set of global event listeners. This are independent of 
 * any events. Events are always tried to be delivered to the global event listeners. In general, the user
 * cannot make any assumptions about the order in which individual event listeners methods are called.
 *
 * @author deronj
 */

@InternalAPI
public abstract class EventDistributor implements Runnable {

    private static final Logger logger = Logger.getLogger(EventDistributor.class.getName());

    /** The focus sets for the various event classes. */
    protected HashMap<Class,HashSet<Entity>> focusSets = new HashMap<Class,HashSet<Entity>>();

    /** The base input queue entry. */
    private static class Entry {
        /** The Wonderland event. */
        Event event;
        /** The pick info for the event */
        PickInfo pickInfo;
            Entry (Event event, PickInfo pickInfo) {
            this.event = event;
            this.pickInfo = pickInfo;
        }
    }

    /** 
     * Used for SwingEnterExit3D events.
     * Note: this type of event has no pickInfo but has an entity./
     */
    private static class EntrySwingEnterExit extends Entry {
	/** The entity. */
	Entity entity;
        EntrySwingEnterExit (Event event, Entity entity) {
	    super(event, null);
	    this.entity = entity;
	}
    }

    /** The state of the propagation state during the traversal. */
    public class PropagationState {
	public boolean toParent;
	public boolean toUnder;
    }

    private Thread thread;

    private LinkedBlockingQueue<Entry> inputQueue = new LinkedBlockingQueue<Entry>();

    private EventListenerCollection globalEventListeners = new EventListenerCollection();

    protected void start () {
	thread = new Thread(ThreadManager.getThreadGroup(), this, "EventDistributor");
	thread.start();
    }

    /**
     * Add a Wonderland event to the event distribution queue, with null pick info.
     * @param event The event to enqueue.
     */
    void enqueueEvent (Event event) {
        inputQueue.add(new Entry(event, null));
    }

    /**
     * Add a Wonderland event to the event distribution queue.
     * @param event The event to enqueue.
     * @param pickInfo The pick info for the event.
     */
    void enqueueEvent (Event event, PickInfo pickInfo) {
        inputQueue.add(new Entry(event, pickInfo));
    }

    /**
     * Add a Wonderland SwingEnterExit3D event to the event distribution queue.
     * @param event The event to enqueue.
     * @param entity The event's entity.
     */
    void enqueueEvent (SwingEnterExitEvent3D event, Entity entity) {
	inputQueue.add(new EntrySwingEnterExit(event, entity));
    }

    /**
     * The run loop of the thread.
     */
    public void run () {
	while (true) {
	    try {
		Entry entry = null;
                entry = inputQueue.take();
		if (entry instanceof EntrySwingEnterExit) {
		    EntrySwingEnterExit esee = (EntrySwingEnterExit) entry;
		    processSwingEnterExitEvent(esee.event, esee.entity);
		} else {
		    processEvent(entry.event, entry.pickInfo);
		}
            } catch (Exception ex) {
		ex.printStackTrace();
		logger.warning("Exception caught in EventDistributor thread. Event is ignored.");
	    }
	}
    }
    
    /** 
     * The responsibility for determining how to process individual event types is delegated to the subclass.
     * @param event The event to try to deliver to event listeners.
     * @param pickInfo The pick info associated with the event.
     */
    protected abstract void processEvent (Event event, PickInfo pickInfo);

    /** 
     * The responsibility for determining how to process individual swing enter/exit event types 
     * is delegated to the subclass.
     * @param event The event to try to deliver to event listeners.
     * @param entity The entity associated with the event.
     */
    protected abstract void processSwingEnterExitEvent (Event event, Entity entity);

    /** 
     * Traverse the list of global listeners to see whether the event should be delivered to any of them. 
     * Post the event to willing consumers. Note: the return values of propagatesToParent() and propagatesToUnder() 
     * for global listeners are ignored.
     */
    protected void tryGlobalListeners (Event event) {
        logger.fine("tryGlobalListeners event = " + event);
        synchronized (globalEventListeners) {
                Iterator<EventListener> it = globalEventListeners.iterator();
            while (it.hasNext()) {
                    EventListener listener = it.next();
            if (listener.isEnabled()) {
                logger.fine("Calling consume for listener " + listener);
                Event distribEvent = createEventForGlobalListener(event);
                if (listener.consumesEvent(distribEvent)) {
                logger.fine("CONSUMED.");
                listener.postEvent(distribEvent);
                }
            }
            }
        }
    }
	
    /** 
     * See if any of the enabled event listeners for the given entity are willing to consume the given event.
     * Post the event to willing consumers. Also, returns the OR of propagatesToParent for all enabled listeners 
     * and the OR of propagatesToUnder for all enabled listeners in propState.
     */
    protected void tryListenersForEntity (Entity entity, Event event, PropagationState propState) {
        logger.fine("tryListenersForEntity, entity = " + entity + ", event = " + event);
        EventListenerCollection listeners = (EventListenerCollection) entity.getComponent(EventListenerCollection.class);
        if (listeners == null || listeners.size() <= 0) {
            logger.fine("Entity has no listeners");
            // propagatesToParent is true, so OR makes no change to its accumulator
            // propagatesToUnder is false, so OR makes its accumulator is false
            propState.toUnder = false;
        } else {
            Iterator<EventListener> it = listeners.iterator();
            while (it.hasNext()) {
            EventListener listener = it.next();
            if (listener.isEnabled()) {
                logger.fine("Calling consume for listener " + listener);
                Event distribEvent = createEventForEntity(event, entity);
                if (listener.consumesEvent(distribEvent)) {
                logger.fine("CONSUMED by entity " + entity);
                listener.postEvent(distribEvent);
                }
                propState.toParent |= listener.propagatesToParent(distribEvent);
                propState.toUnder |= listener.propagatesToUnder(distribEvent);
                logger.finer("Listener prop state, toParent = " + propState.toParent +
                    ", toUnder = " + propState.toUnder);
            }
            }
        }
        logger.fine("Cumulative prop state, toParent = " + propState.toParent + ", toUnder = " + propState.toUnder);
    }

    /** 
     * Traverse the entity parent chain (starting with the given entity) to see if the
     * event should be delivered to any of their listeners. Continue the search until no more parents are found.
     * Post the event to willing consumers. Also, returns the OR of propagatesToParent for all enabled listeners 
     * and the OR of propagatesToUnder for all enabled listeners in propState.
     */
    protected void tryListenersForEntityParents (Entity entity, Event event, PropagationState propState) {
        while (entity != null) {
            tryListenersForEntity(entity, event, propState);
            if (!propState.toParent) {
            // No more propagation to parents. We're done with this loop.
            break;
            }
            logger.fine("Propagate to next parent");
            entity = entity.getParent();
        }
    }
	
    /**
     * Create an event for distribution to a global event listener, based on the given base event.
     */
    static Event createEventForGlobalListener (Event baseEvent) {
        Event event = baseEvent.clone(null);
        event.setFocussed(entityHasFocus(baseEvent, InputManager.inputManager().getGlobalFocusEntity()));
            return event;
    }


    /**
     * Create an event for distribution to the given entity, based on the given base event.
     */
    static Event createEventForEntity (Event baseEvent, Entity entity) {
        Event event = baseEvent.clone(null);
        event.setEntity(entity);
        event.setFocussed(entityHasFocus(baseEvent, entity));
            return event;
    }

    /**
     * Add an event listener to be tried once per event. This global listener can be added only once.
     * Subsequent attempts to add it will be ignored.
     * <br><br>
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The global event listener to be added.
     */
    public void addGlobalEventListener (EventListener listener) {
        synchronized (globalEventListeners) {
            if (globalEventListeners.contains(listener)) {
            return;
            } else {
            globalEventListeners.add(listener);
            listener.addToEntity(InputManager.inputManager().getGlobalFocusEntity());
            }
        }
    }

    /**
     * Remove this global event listener.
     * <br><br>
     * Note: It is not a good idea to call this from inside EventListener.computeEvent function.
     * However, it is okay to call this from inside EventListener.commitEvent function if necessary.
     *
     * @param listener The global event listener to be removed.
     */
    public void removeGlobalEventListener (EventListener listener) {
        synchronized (globalEventListeners) {
            globalEventListeners.remove(listener);
            listener.removeFromEntity(InputManager.inputManager().getGlobalFocusEntity());
        }
    }

    /**
     * Update the focus sets based on a change event which comes through the event queue.
     * Called at FocusChangeEvent time.
     * @param changes An array of the changes to apply to the focus sets.
     */
    protected void processFocusChangeEvent (FocusChange[] changes) {
        for (FocusChange change : changes) {
            Class[] classes = change.eventClasses;
            Entity[] entities = change.entities;

            HashSet<Entity> focusSet;

            switch (change.action) {

                // Add the entities to each event class focus set
            case ADD:
            for (Class clazz : classes) {
                focusSet = focusSets.get(clazz);
                if (focusSet == null) {
                // First time for this class
                focusSet = new HashSet<Entity>();
                focusSets.put(clazz, focusSet);
                }
                for (Entity entity : entities) {
                focusSet.add(entity);
                setEntityFocus(clazz, entity, true);
                }
            }
            break;

                // Remove the entities from each event class focus set
            case REMOVE:
            for (Class clazz : classes) {
                focusSet = focusSets.get(clazz);
                if (focusSet == null) continue;
                for (Entity entity : entities) {
                focusSet.remove(entity);
                setEntityFocus(clazz, entity, false);
                }
                if (focusSet.size() <= 0) {
                focusSets.remove(clazz);
                }
            }
            break;

                // Replace the existing entities from each event class focus set with the new entities
            case REPLACE:
            for (Class clazz : classes) {

                // First, unfocus previous entities
                focusSet = focusSets.get(clazz);
                for (Entity entity : focusSet) {
                setEntityFocus(clazz, entity, false);
                }

                // Now focus the new entiti
                if (entities == null || entities.length <= 0) {
                focusSets.remove(clazz);
                } else {
                focusSet = new HashSet<Entity>();
                for (Entity entity : entities) {
                    focusSet.add(entity);
                    setEntityFocus(clazz, entity, true);
                }
                focusSets.put(clazz, focusSet);
                }
            }
            break;
            }
        }

	// Debug
	//logger.warning("Updated focus sets");
	//logger.warning("------------------");
	//logFocusSets();
	//logger.warning("------------------");
    }

    /** For debug */
    private void logFocusSets () {
        for (Class clazz : focusSets.keySet()) {
            HashSet<Entity> focusSet = focusSets.get(clazz);
            StringBuffer sb = new StringBuffer();
            sb.append("Class = " + clazz + ": ");
            Iterator<Entity> it = focusSet.iterator();
            while (it.hasNext()) {
            Entity entity = it.next();
            sb.append(entity.toString() + ", ");
            }
            logger.warning(sb.toString());
        }
    }

    /** A marker component used to mark entities which have focus. */
    private static class EventFocusComponent extends EntityComponent {

	/** The set of event classes for which this entity has focus */
	private HashSet<Class> focussedClasses;

	/** Add the given event class to the focus set. */
	private void add (Class clazz) {
	    if (focussedClasses == null) {
            focussedClasses = new HashSet<Class>();
	    }
	    focussedClasses.add(clazz);
	}

	/** Remove the given event class from the focus set. */
	private void remove (Class clazz) {
	    if (focussedClasses == null) return;
            focussedClasses.remove(clazz);
	    if (focussedClasses.size() <= 0) {
            focussedClasses = null;
	    }
	}

	/** Does the focus set contain the given event class or one of its super classes? */
	private boolean containsClassOrSuperclass (Class clazz) {
	    if (focussedClasses == null) return false;
	    Iterator<Class> it = focussedClasses.iterator();
	    while (it.hasNext()) {
            Class cl = it.next();
            if (cl.isAssignableFrom(clazz)) {
                return true;
            }
	    }
	    return false;
	}

	/** Returns the number of event classes in the focus set. */
	private int size () {
	    if (focussedClasses == null) return 0;
	    return focussedClasses.size();
	}
    }

    /**
     * Specify whether the given entity is focussed. Called at FocusChangeEvent time. Marks
     * the entities has been focussed or not.
     */
    private static void setEntityFocus (Class clazz, Entity entity, boolean hasFocus) {
        EventFocusComponent focusComp = (EventFocusComponent) entity.getComponent(EventFocusComponent.class);
        if (hasFocus) {
            if (focusComp == null) {
            focusComp = new EventFocusComponent();
            entity.addComponent(EventFocusComponent.class, focusComp);
            }
            focusComp.add(clazz);
        } else {
            if (focusComp == null) return;
            focusComp.remove(clazz);
            if (focusComp.size() <= 0) {
            entity.removeComponent(EventFocusComponent.class);
            }
        }
    }

    /**
     * Returns true if the given entity is marked as having focus.
     * Called at Event Distribution time. Therefore this is based on the information on the entity
     * itself, not the focus sets, which are in a different time domain.
     * @param event The event to be delivered.
     * @param entity The entity to check if it is in the focus set.
     */
    private static boolean entityHasFocus (Event event, Entity entity) {
        EventFocusComponent focusComp = (EventFocusComponent) entity.getComponent(EventFocusComponent.class);
        if (focusComp == null) return false;
        return focusComp.containsClassOrSuperclass(event.getClass());
    }
}
