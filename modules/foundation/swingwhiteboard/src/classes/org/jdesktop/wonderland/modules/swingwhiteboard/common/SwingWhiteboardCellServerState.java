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

package org.jdesktop.wonderland.modules.swingwhiteboard.common;

import com.jme.math.Vector2f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;

/**
 * The WFS server state class for SwingWhiteboardCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="swingwhiteboard-cell")
public class SwingWhiteboardCellServerState extends CellServerState implements Serializable, CellServerStateSPI {
    
    /** The user's preferred width of the whiteboard window. */
    @XmlElement(name="preferredWidth")
    public int preferredWidth = 1024;
    
    /** The user's preferred height of the whiteboard window. */
    @XmlElement(name="preferredHeight")
    public int preferredHeight = 768;
    
    /** The X pixel scale of the whiteboard window. */
    @XmlElement(name="pixelScaleX")
    public float pixelScaleX = 0.01f;

    /** The Y pixel scale of the whiteboard window. */
    @XmlElement(name="pixelScaleY")
    public float pixelScaleY = 0.01f;
    
    /** Default constructor */
    public SwingWhiteboardCellServerState() {}
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.swingwhiteboard.server.SwingWhiteboardCellMO";
    }

    @XmlTransient public int getPreferredWidth () {
        return preferredWidth;
    }
    
    public void setPreferredWidth (int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    @XmlTransient public int getPreferredHeight () {
        return preferredHeight;
    }
    
    public void setPreferredHeight (int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    @XmlTransient public float getPixelScaleX () {
        return pixelScaleX;
    }
    
    public void setPixelScaleX (float pixelScale) {
        this.pixelScaleX = pixelScaleX;
    }

    @XmlTransient public float getPixelScaleY () {
        return pixelScaleY;
    }
    
    public void setPixelScaleY (float pixelScale) {
        this.pixelScaleY = pixelScaleY;
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The server state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [SwingWhiteboardCellServerState]: " +
	    "preferredWidth=" + preferredWidth + "," +
	    "preferredHeight=" + preferredHeight + "," +
	    "pixelScaleX=" + pixelScaleX + "," +
	    "pixelScaleY=" + pixelScaleY;
    }
}
