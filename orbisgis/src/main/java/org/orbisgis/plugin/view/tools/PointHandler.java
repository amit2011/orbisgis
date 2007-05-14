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


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class PointHandler extends AbstractHandler implements Handler {

	private String geometryType;

	public PointHandler(Geometry g,
			String geometryType, int vertexIndex, Coordinate p) {
		super(g, vertexIndex, p);
		this.geometryType = geometryType;
	}

	public com.vividsolutions.jts.geom.Geometry moveJTSTo(double x, double y)
			throws CannotChangeGeometryException {
		com.vividsolutions.jts.geom.Geometry ret = (com.vividsolutions.jts.geom.Geometry) geometry
				.clone();
		Coordinate[] coords = ret.getCoordinates();
		coords[vertexIndex].x = x;
		coords[vertexIndex].y = y;
		ret.geometryChanged();

		return ret;
	}

	public Geometry moveTo(double x, double y)
			throws CannotChangeGeometryException {
		com.vividsolutions.jts.geom.Geometry ret = moveJTSTo(x, y);
		if (!ret.isValid()) {
			throw new CannotChangeGeometryException(
				LA_GEOMETRIA_NO_ES_VALIDA);
		}
		return ret;
	}

	public com.vividsolutions.jts.geom.Geometry removeVertex()
			throws CannotChangeGeometryException {
		if (geometryType == Primitive.MULTIPOINT_GEOMETRY_TYPE) {
			return gf.createMultiPoint(removeVertex(vertexIndex, geometry, 1));
		} else if (geometryType == Primitive.LINE_GEOMETRY_TYPE) {
			return gf.createLineString(removeVertex(vertexIndex, geometry, 2));
		}

		throw new RuntimeException();
	}

	/**
	 * @see org.estouro.theme.Handler#remove()
	 */
	public Geometry remove() throws CannotChangeGeometryException {
		if (geometryType == Primitive.POINT_GEOMETRY_TYPE) {
			throw new CannotChangeGeometryException(
					"No se puede eliminar un vrtice a un punto");
		} else if ((geometryType == Primitive.LINE_GEOMETRY_TYPE)
				|| (geometryType == Primitive.MULTIPOINT_GEOMETRY_TYPE)) {
			com.vividsolutions.jts.geom.Geometry g = removeVertex();
			if (!g.isValid()) {
				throw new CannotChangeGeometryException(
						LA_GEOMETRIA_NO_ES_VALIDA);
			}
			return g;
		}
		throw new RuntimeException();
	}

}
