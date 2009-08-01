/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
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
 * $Revision$
 * $Date$
 * $State: Exp $
 */
package org.jdesktop.wonderland.common.comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;

/**
 * A specialized ObjectInputStream that assumes that exactly the same
 * classes are available on either end of the stream. With this assumption this
 * stream can significantly reduce the size of serialized objects by only
 * sending the large header once for a given class. For subsequent transmission
 * of an object of a given class an id is sent.
 * 
 * @author paulby
 */public class WonderlandObjectInputStream extends ObjectInputStream {

    private HashMap<Integer, ObjectStreamClass> idToDesc = new HashMap();
    private HashMap<ObjectStreamClass, Integer> descToId = new HashMap();

    public WonderlandObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws ClassNotFoundException, IOException {
        ObjectStreamClass ret;
        int id = readInt();

        // Process any pending removes
        while (id == WonderlandObjectOutputStream.REMOVE_DESCRIPTOR) {
            System.err.println("Processing REMOVE");
            id = readInt();
            ret = idToDesc.remove(id);
            descToId.remove(ret);
            id = readInt();
        }
        
        if (id == WonderlandObjectOutputStream.NEW_DESCRIPTOR) {
            ret = super.readClassDescriptor();
            id = readInt();
            idToDesc.put(id, ret);
            descToId.put(ret, id);
        } else {
            ret = idToDesc.get(id);
        }
        if (ret == null) {
            throw new IOException("Unknown class ID " + id);
        }
        return ret;
    }
}