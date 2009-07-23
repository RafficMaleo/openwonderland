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
package org.jdesktop.wonderland.server.wfs.exporter;

import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.wfs.WorldRoot;

/**
 * A service for exporting cells.  This service provides a set of
 * asynchronous mechanisms for creating cell snapshots, and writing a set
 * of cells to those snapshots.  Callers will be notified if the
 * export succeeds or fails.
 *
 * @author jkaplan
 */
public interface CellExportManager {
    /**
     * Create a new snapshot for writing cells to.  This method will contact
     * the remote web service to create a new snapshot, and then call the
     * given listener with the result of that call.
     * @param listener a snapshot creation listener that will be notified of
     * the result of this call
     */
    public void createSnapshot(SnapshotCreationListener listener);

    /**
     * Write a set of cells to the given snapshot.  This method will fetch the
     * given set of cells, and write the contents of the cells and all their
     * children to the remote web service.  Finally, the listener will be
     * notified with the results of the call.
     * @param worldRoot the snapshot to write to
     * @param cellIDs a set of cell IDs to write.  Each cellID will be used
     * as a root for writing, so the entire graph under the given set of
     * cell IDs will be written.  The cellIDs set will be accessed across
     * multiple Darkstar transactions, so it is essential that the iterator
     * for the set be serializable and work correctly in the face of concurrent
     * access.  Typically, a ScalableHashSet is the best choice for the set.
     * @param listener a listener that will be notified of the results
     */
    public void exportCells(WorldRoot worldRoot, Set<CellID> cellIDs,
                            CellExportListener listener);

    /**
     * A listener that will be notified of the success or failure of
     * creating a snapshot.  Implementations of SnapshotCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface SnapshotCreationListener {
        /**
         * Notification that a snapshot has been created successfully
         * @param worldRoot the world root that was created
         */
        public void snapshotCreated(WorldRoot worldRoot);

        /**
         * Notification that snapshot creation has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void snapshotFailed(String reason, Throwable cause);

    }

    /**
     * A listener that will be notified of the result of exporting a set
     * of cells to a snapshot.  Implementations of CellExportListener must
     * be either a ManagedObject or Serializable
     */
    public interface CellExportListener {
        /**
         * Notification of the result of cell export
         * @param results a Map from CellIDs in the request to results
         * for the export of that cell.
         */
        public void exportResult(Map<CellID, CellExportResult> results);
    }

    /**
     * The result of exporting a cell
     */
    public interface CellExportResult {
        /**
         * Whether or not the export was successful
         * @return true if the export was successful, or false if not
         */
        public boolean isSuccess();

        /**
         * If the export failed, return the reason
         * @return the reason for failure, or null
         */
        public String getFailureReason();

        /**
         * If the export failed, return the root cause exception
         * @return the root cause of the failure, or null
         */
        public Throwable getFailureCause();
    }
}