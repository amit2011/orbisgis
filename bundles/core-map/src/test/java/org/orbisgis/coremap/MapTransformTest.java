/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.coremap;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.BufferedImage;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.coremap.map.MapTransform;
import static org.junit.Assert.*;

public class MapTransformTest {

	private BufferedImage img;
	private Envelope extent;
	private MapTransform mt;

	@Before
	public void setUp() throws Exception {
		img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		mt = new MapTransform();
		extent = new Envelope(0, 100, 0, 100);
	}

        @Test
	public void testExtentAndImage() throws Exception {
		mt.setExtent(extent);
		mt.setImage(img);
		assertTrue(mt.getAdjustedExtent().equals(extent));
	}

        @Test
	public void testImageAndExtent() throws Exception {
		mt.setImage(img);
		mt.setExtent(extent);
		assertTrue(mt.getAdjustedExtent().equals(extent));
	}

}
