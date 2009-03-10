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

package org.jdesktop.wonderland.client.jme.utils;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;

/**
 * Various utilties for the JME scene graph
 *
 * @author paulby
 */
public class ScenegraphUtils {


    /**
     * Traverse the scene graph from rootNode and return the first
     * node with the given name. Returns null if no node is found
     *
     * @param rootNode the root of the graph
     * @param name the name to search for
     * @return the named node, or null
     */
    public static Spatial findNamedNode(Node rootNode, String name) {
        FindNamedNodeListener listener = new FindNamedNodeListener(name);

        TreeScan.findNode(rootNode, listener);

        return listener.getResult();
    }

    static class FindNamedNodeListener implements ProcessNodeInterface {

        private String nodeName;
        private Spatial result=null;

        public FindNamedNodeListener(String name) {
            nodeName = name;
        }

        public boolean processNode(Spatial node) {
            if (node.getName().equals(nodeName)) {
                result = node;
                return false;
            }
            return true;
        }

        public Spatial getResult() {
            return result;
        }
    }

}
