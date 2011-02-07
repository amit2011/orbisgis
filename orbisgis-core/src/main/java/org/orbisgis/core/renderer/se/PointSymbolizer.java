/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 *  Team leader Erwan BOCHER, scientific researcher,
 *
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
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
 *
 * or contact directly:
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */
package org.orbisgis.core.renderer.se;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.io.IOException;
import java.util.ArrayList;
import javax.media.jai.RenderableGraphics;
import javax.xml.bind.JAXBElement;
import org.gdms.data.SpatialDataSourceDecorator;
import org.orbisgis.core.renderer.Drawer;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.PointSymbolizerType;

import org.gdms.driver.DriverException;
import org.orbisgis.core.Services;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.graphic.GraphicCollection;
import org.orbisgis.core.renderer.se.graphic.MarkGraphic;

import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.transform.Transform;

public final class PointSymbolizer extends VectorSymbolizer implements GraphicNode {

	/*
	 * Create a default pointSymbolizer: Square 10mm
	 *
	 *
	 */
	public PointSymbolizer() {
		super();
		this.name = "Point symbolizer";
		setGraphicCollection(new GraphicCollection());
		uom = Uom.MM;

		MarkGraphic mark = new MarkGraphic();
		mark.setTo3mmCircle();
		graphic.addGraphic(mark);
	}

	public PointSymbolizer(JAXBElement<PointSymbolizerType> st) throws InvalidStyle {
		super(st);
		PointSymbolizerType ast = st.getValue();

		if (ast.getGeometry() != null) {
			// TODO load GeometryFunction !
		}

		if (ast.getUnitOfMeasure() != null) {
			Uom u = Uom.fromOgcURN(ast.getUnitOfMeasure());
			System.out.println("This is the UOM: " + u);
			this.setUom(u);
		}

		if (ast.getTransform() != null) {
			this.setTransform(new Transform(ast.getTransform()));
		}

		if (ast.getGraphic() != null) {
			this.setGraphicCollection(new GraphicCollection(ast.getGraphic(), this));

		}
	}

	@Override
	public GraphicCollection getGraphicCollection() {
		return graphic;
	}

	@Override
	public void setGraphicCollection(GraphicCollection graphic) {
		this.graphic = graphic;
		graphic.setParent(this);
	}

	@Override
	public void draw(Graphics2D g2, SpatialDataSourceDecorator sds, long fid, boolean selected, MapTransform mt) throws IOException, DriverException {
		if (graphic != null && graphic.getNumGraphics() > 0) {

			try {
				//ArrayList<Point2D> pts = this.getPoints(sds, fid, mt);
				RenderableGraphics rg = graphic.getGraphic(sds, fid, selected, mt);

				if (rg != null) {
					double x = 0, y = 0;
					//for (Point2D pt : pts) {
						// This is to emulate ExtractFirstPoint geom function !!!
						//Point2D pt = this.getFirstPointShape(sds, fid);

						//RenderedImage cache = graphic.getCache(sds, fid, selected);
						//if (cache != null) {

                        Point2D pt = getPointShape(sds, fid, mt);


						x = pt.getX();
						y = pt.getY();

						// Draw the graphic right over the point !
						g2.drawRenderedImage(rg.createRendering(mt.getCurrentRenderContext()), AffineTransform.getTranslateInstance(x, y));
					//}
				}
			} catch (ParameterException ex) {
				Services.getErrorManager().error("Could not render feature ", ex);
			}

		}
	}

	@Override
	public JAXBElement<PointSymbolizerType> getJAXBElement() {
		ObjectFactory of = new ObjectFactory();
		PointSymbolizerType s = of.createPointSymbolizerType();

		this.setJAXBProperty(s);


		if (this.uom != null) {
			s.setUnitOfMeasure(this.getUom().toURN());
		}

		if (transform != null) {
			s.setTransform(transform.getJAXBType());
		}

		if (graphic != null) {
			s.setGraphic(graphic.getJAXBElement());
		}

		return of.createPointSymbolizer(s);
	}
	private GraphicCollection graphic;

	@Override
	public void draw(Drawer drawer, long fid, boolean selected) {
		drawer.drawPointSymbolizer(fid, selected);
	}
}