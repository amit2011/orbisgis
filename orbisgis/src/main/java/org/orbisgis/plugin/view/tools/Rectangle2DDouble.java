/* OrbisCAD. The Community cartography editor
 *
 * Copyright (C) 2005, 2006 OrbisCAD development team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  OrbisCAD development team
 *   elgallego@users.sourceforge.net
 */
package org.orbisgis.plugin.view.tools;

import java.awt.geom.Rectangle2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class Rectangle2DDouble extends Rectangle2D.Double {

    public Rectangle2DDouble() {
        super();
    }

    public Rectangle2DDouble(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public Geometry getEnvelope() {
    	GeometryFactory gf = ToolManager.toolsGeometryFactory;
    	LinearRing ret = gf.createLinearRing(new Coordinate[]{
    			new Coordinate(getMinX(), getMinY()),
    			new Coordinate(getMaxX(), getMinY()),
    			new Coordinate(getMaxX(), getMaxY()),
    			new Coordinate(getMinX(), getMaxY()),
    			new Coordinate(getMinX(), getMinY()),
    	});
        return gf.
        createPolygon(ret, new LinearRing[]{});
    }
}
