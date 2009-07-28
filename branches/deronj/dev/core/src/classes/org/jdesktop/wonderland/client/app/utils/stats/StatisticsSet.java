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
package org.jdesktop.wonderland.client.app.utils.stats;

/**
 * A base class which holds group of statistics variables.
 *
 * @author deronj
 */ 

@StableAPI
public abstract class StatisticsSet {

    private String name;

    public StatisticsSet (String name) {
	this.name = name;
    }

    public String getName () {
	return name;
    }

    protected static final int max (int a, int b) {
	return (a > b) ? a : b;
    }

    protected static final long max (long a, long b) {
	return (a > b) ? a : b;
    }

    protected static final double max (double a, double b) {
	return (a > b) ? a : b;
    }

    /**
     * Collect the latest data into this statistics object
     */
    protected abstract void probe ();

    /**
     * Reset all stats to zero. All of stats are cumulative as of 
     * the last time a reset was performed.
     */
    protected abstract void reset ();

    /**
     * Accumulate the statistics into the given statistics object.
     * The given object must be of the same type as this object.
     */
    protected abstract void accumulate (StatisticsSet cumulativeStats);

    /**
     * Remember in the given statistics object the maximum values of 
     * this statistics object. The given object must be of the same type 
     * as this object.
     */
    protected abstract void max (StatisticsSet maxStats);

    /**
     * Print out current statistics values only. 
     */
    protected abstract void printStats ();

    /**
     * Print out current statistics values and some derived rates. 
     * timeSecs is the time (in seconds) it took to measure the stats.
     */
    protected abstract void printStatsAndRates (double timeSecs);

    /**
     * Statistics are printed only when the statistics set has been 
     * triggered. This allows subclasses to disable printing when
     * there is no worthwhile information (e.g. all values are zero).
     */
    protected boolean hasTriggered () {
	return true;
    }
}