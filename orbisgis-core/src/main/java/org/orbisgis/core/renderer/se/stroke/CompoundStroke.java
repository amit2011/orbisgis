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
package org.orbisgis.core.renderer.se.stroke;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.gdms.data.SpatialDataSourceDecorator;

import org.orbisgis.core.map.MapTransform;
import net.opengis.se._2_0.core.CompoundStrokeType;
import net.opengis.se._2_0.core.ObjectFactory;
import net.opengis.se._2_0.core.StrokeAnnotationGraphicType;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.UomNode;
import org.orbisgis.core.renderer.se.common.RelativeOrientation;
import org.orbisgis.core.renderer.se.common.ShapeHelper;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.graphic.GraphicCollection;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.real.RealParameterContext;

/**
 *
 * @author maxence
 */
public final class CompoundStroke extends Stroke implements UomNode {

    private RealParameter preGap;
    private RealParameter postGap;
    private List<CompoundStrokeElement> elements;
    private List<StrokeAnnotationGraphic> annotations;
    private Uom uom;

    public CompoundStroke() {
        super();
        elements = new ArrayList<CompoundStrokeElement>();
        addElement(new StrokeElement());
        annotations = new ArrayList<StrokeAnnotationGraphic>();
    }

    public CompoundStroke(CompoundStrokeType s) throws InvalidStyle {
        super(s);

        if (s.getUom() != null) {
            setUom(Uom.fromOgcURN(s.getUom()));
        }

        if (s.getPreGap() != null) {
            setPreGap(SeParameterFactory.createRealParameter(s.getPreGap()));
        }

        if (s.getPostGap() != null) {
            setPostGap(SeParameterFactory.createRealParameter(s.getPostGap()));
        }

        elements = new ArrayList<CompoundStrokeElement>();
        annotations = new ArrayList<StrokeAnnotationGraphic>();

        if (s.getStrokeElementOrAlternativeStrokeElements() != null) {
            for (Object o : s.getStrokeElementOrAlternativeStrokeElements()) {
                CompoundStrokeElement cse = CompoundStrokeElement.createCompoundStrokeElement(o);
                addCompoundStrokeElement(cse);
            }
        }

        if (s.getStrokeAnnotationGraphic() != null) {
            for (StrokeAnnotationGraphicType sagt : s.getStrokeAnnotationGraphic()) {
                StrokeAnnotationGraphic sag = new StrokeAnnotationGraphic(sagt);
                addStrokeAnnotationGraphic(sag);
            }
        }
    }

    public CompoundStroke(JAXBElement<CompoundStrokeType> s) throws InvalidStyle {
        this(s.getValue());
    }

    public void setPreGap(RealParameter preGap) {
        this.preGap = preGap;

        if (preGap != null) {
            this.preGap.setContext(RealParameterContext.NON_NEGATIVE_CONTEXT);
        }
    }

    public void setPostGap(RealParameter postGap) {
        this.postGap = postGap;

        if (postGap != null) {
            this.postGap.setContext(RealParameterContext.NON_NEGATIVE_CONTEXT);
        }
    }

    public RealParameter getPreGap() {
        return preGap;
    }

    public RealParameter getPostGap() {
        return postGap;
    }

    public List<StrokeAnnotationGraphic> getAnnotations() {
        return annotations;
    }

    public List<CompoundStrokeElement> getElements() {
        return elements;
    }

    @Override
    public Double getNaturalLength(SpatialDataSourceDecorator sds, long fid, Shape shp, MapTransform mt) throws ParameterException, IOException {
        return Double.POSITIVE_INFINITY;
        //throw new UnsupportedOperationException("Nesting a compound stroke within a compound stroke is forbidden !!!");
    }

    @Override
    public void draw(Graphics2D g2, SpatialDataSourceDecorator sds, long fid, Shape shape,
            boolean selected, MapTransform mt, double offset) throws ParameterException, IOException {

        double initGap = 0.0;
        double endGap = 0.0;

        List<Shape> shapes;
        // if not using offset rapport, compute perpendiculat offset first
        if (!this.isOffsetRapport() && Math.abs(offset) > 0.0) {
            shapes = ShapeHelper.perpendicularOffset(shape, offset);
            // Setting offset to 0.0 let be sure the offset will never been applied twice!
            offset = 0.0;
        } else {
            shapes = new ArrayList<Shape>();
            shapes.add(shape);
        }

        //ShapeHelper.printvertices(shape);

        for (Shape shp : shapes) {

            if (preGap != null) {
                initGap = Uom.toPixel(preGap.getValue(sds, fid), getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                if (initGap > 0.0) {
                    //System.out.println ("Remove Global PreGap");
                    List<Shape> splitLine = ShapeHelper.splitLine(shp, initGap);
                    if (splitLine.size() == 2) {
                        shp = splitLine.get(1);
                    } else {
                        shp = null;
                    }
                }
            }

            if (shp != null) {
                if (postGap != null) {
                    endGap = Uom.toPixel(postGap.getValue(sds, fid), getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                    if (endGap > 0.0) {
                        //System.out.println ("Remove Global PostGap");
                        double lineLength = ShapeHelper.getLineLength(shp);
                        shp = ShapeHelper.splitLine(shp, lineLength - endGap).get(0);
                    }
                }

                //System.out.println ("After removiung gaps:");
                //ShapeHelper.printvertices(shp);

                int nbElem = elements.size();

                double lengths[] = new double[nbElem];
                Stroke strokes[] = new Stroke[nbElem];
                Double preGaps[] = new Double[nbElem];
                Double postGaps[] = new Double[nbElem];

                double remainingLength = ShapeHelper.getLineLength(shp);
                double lineLength = remainingLength;
                int nbInfinite = 0;

                int i = 0;
                for (CompoundStrokeElement elem : elements) {
                    StrokeElement sElem = null;

                    if (elem instanceof StrokeElement) {
                        sElem = (StrokeElement) elem;
                    } else if (elem instanceof AlternativeStrokeElements) {
                        // do not retrieve the most suitable element, just take the first one...
                        AlternativeStrokeElements aElem = (AlternativeStrokeElements) elem;
                        sElem = aElem.getElements().get(0);
                    }

                    strokes[i] = sElem.getStroke();

                    if (sElem.getLength() != null) {
                        lengths[i] = Uom.toPixel(sElem.getLength().getValue(sds, fid),
                                getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                        //System.out.println("Has own length: " + lengths[i]);
                    } else {
                        lengths[i] = sElem.getStroke().getNaturalLength(sds, fid, shp, mt);
                        //System.out.println("Natural length: " + lengths[i]);
                    }

                    /*if (lengths[i] < 2){
                        lengths[i] = 2;
                    }*/

                    if (sElem.getPreGap() != null) {
                        preGaps[i] = Uom.toPixel(sElem.getPreGap().getValue(sds, fid), getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                        remainingLength -= preGaps[i];
                    } else {
                        preGaps[i] = null;
                    }

                    if (sElem.getPostGap() != null) {
                        postGaps[i] = Uom.toPixel(sElem.getPostGap().getValue(sds, fid), getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                        remainingLength -= postGaps[i];
                    } else {
                        postGaps[i] = null;
                    }

                    if (Double.isInfinite(lengths[i])) {
                        nbInfinite++;
                    } else {
                        remainingLength -= lengths[i];
                    }
                    i++;
                }

                double patternLength = lineLength - remainingLength;
                if (nbInfinite > 0) {
                    double infiniteLength = remainingLength / nbInfinite;
                    //System.out.println(" Share remaining: " + infiniteLength);
                    for (i = 0; i < lengths.length; i++) {
                        if (Double.isInfinite(lengths[i])) {
                            lengths[i] = infiniteLength;
                            //System.out.println ("Set infinite lenght to: " + infiniteLength);
                        }
                    }
                } else { // fixed length pattern
                    if (this.isLengthRapport()) {
                        // Scale pattern to lineLength intergral fraction
                        int nbPattern = (int) ((lineLength / patternLength) + 0.5);
                        if (nbPattern < 1) {
                            // Male sure at least one pattern will be drawn
                            nbPattern = 1;
                        }
                        // Compute factor
                        double f = lineLength / (nbPattern * patternLength);
                        for (i = 0; i < nbElem; i++) {
                            lengths[i] *= f;
                            if (preGaps[i] != null) {
                                preGaps[i] *= f;
                            }
                            if (postGaps[i] != null) {
                                postGaps[i] *= f;
                            }
                        }
                        patternLength *= f;
                    }
                }

                Shape scrap = shp;


                //while (ShapeHelper.getLineLength(chute) > 0) {
                i = 0; // stroke element iterator
                while (scrap != null) {

                    double scrapLength = ShapeHelper.getLineLength(scrap);
                    if (scrapLength < 1) {
                        break;
                    }

                    if (preGaps[i] != null && preGaps[i] > 0) {
                        List<Shape> splitLine = ShapeHelper.splitLine(scrap, preGaps[i]);
                        //System.out.println("  preGap: " + preGaps[i]);
                        if (splitLine.size() > 1) {
                            scrap = splitLine.get(1);
                        } else {
                            scrap = null;
                            //System.out.println ("  -> End of line !");
                            break;
                        }
                    }

                    if (lengths[i] >= 0) {
                        // get two lines. first is the one we'll style with i'est element
                        List<Shape> splitLine = ShapeHelper.splitLine(scrap, lengths[i]);
                        System.out.println("Extract: " + lengths[i]);
                        System.out.println ("Split ? " + splitLine.size());
                        Shape seg = splitLine.remove(0);

                        //System.out.println ("StrokeElement Seg: ");
                        //ShapeHelper.printvertices(seg);

                        strokes[i].draw(g2, sds, fid, seg, selected, mt, offset);

                        if (splitLine.size() > 0) {
                            scrap = splitLine.remove(0);
                        } else {
                            scrap = null;
                            //System.out.println ("  -> End of line !");
                            break;
                        }

                        //System.out.println ("StrokeElement SCRAP: ");
                        //ShapeHelper.printvertices(scrap);

                        //System.out.println("length: " + lengths[i]);
                    }

                    if (postGaps[i] != null && postGaps[i] > 0) {
                        List<Shape> splitLine = ShapeHelper.splitLine(scrap, postGaps[i]);
                        //System.out.println("postGap: " + postGaps[i]);
                        if (splitLine.size() > 1) {
                            scrap = splitLine.get(1);
                        } else {
                            scrap = null;
                            break;
                        }
                    }

                    i = (i + 1) % nbElem;
                }


                if (annotations.size() > 0) {
                    List<Shape> splitLineInSeg = ShapeHelper.splitLineInSeg(shp, patternLength);
                    for (Shape seg : splitLineInSeg) {
                        for (StrokeAnnotationGraphic annotation : annotations) {
                            GraphicCollection graphic = annotation.getGraphic();
                            Rectangle2D bounds = graphic.getBounds(sds, fid, selected, mt);

                            RelativeOrientation rOrient = annotation.getRelativeOrientation();
                            if (rOrient == null) {
                                rOrient = RelativeOrientation.NORMAL;
                            }

                            double gWidth = bounds.getWidth();
                            double gHeight = bounds.getHeight();

                            double gLength;
                            switch (rOrient) {
                                case NORMAL:
                                case NORMAL_UP:
                                    gLength = gWidth;
                                    break;
                                case LINE:
                                    gLength = gHeight;
                                    break;
                                case PORTRAYAL:
                                default:
                                    gLength = Math.sqrt(gWidth * gWidth + gHeight * gHeight);
                                    break;
                            }

                            double pos = (ShapeHelper.getLineLength(seg) - gLength) * annotation.getRelativePosition().getValue(sds, fid) + gLength / 2.0;

                            Point2D.Double pt = ShapeHelper.getPointAt(seg, pos);

                            AffineTransform at = AffineTransform.getTranslateInstance(pt.x, pt.y);
                            if (rOrient != RelativeOrientation.PORTRAYAL) {

                                Point2D.Double ptA = ShapeHelper.getPointAt(seg, pos - gLength / 2.0);
                                Point2D.Double ptB = ShapeHelper.getPointAt(seg, pos + gLength / 2.0);

                                double theta = Math.atan2(ptB.y - ptA.y, ptB.x - ptA.x);

                                switch (rOrient) {
                                    case LINE:
                                        theta += 0.5 * Math.PI;
                                        break;
                                    case NORMAL_UP:
                                        if (theta < -Math.PI / 2 || theta > Math.PI / 2) {
                                            theta += Math.PI;
                                        }
                                        break;
                                }
                                at.concatenate(AffineTransform.getRotateInstance(theta));
                            }

                            graphic.draw(g2, sds, fid, selected, mt, at);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String dependsOnFeature() {
        String result = "";
        for (StrokeAnnotationGraphic sag : annotations) {
            result += sag.dependsOnFeature();
        }

        for (CompoundStrokeElement elem : elements) {
            result += elem.dependsOnFeature();
        }

        return result.trim();
    }

    private void addCompoundStrokeElement(CompoundStrokeElement cse) {
        elements.add(cse);
        cse.setParent(this);
    }

    private void addStrokeAnnotationGraphic(StrokeAnnotationGraphic sag) {
        annotations.add(sag);
        sag.setParent(this);
    }

    @Override
    public JAXBElement<CompoundStrokeType> getJAXBElement() {
        ObjectFactory of = new ObjectFactory();
        return of.createCompoundStroke(this.getJAXBType());
    }

    public CompoundStrokeType getJAXBType() {
        CompoundStrokeType s = new CompoundStrokeType();

        this.setJAXBProperties(s);

        if (uom != null) {
            s.setUom(uom.toURN());
        }

        if (this.preGap != null) {
            s.setPreGap(preGap.getJAXBParameterValueType());
        }

        if (this.postGap != null) {
            s.setPostGap(postGap.getJAXBParameterValueType());
        }


        List<Object> sElem = s.getStrokeElementOrAlternativeStrokeElements();
        List<StrokeAnnotationGraphicType> sAnnot = s.getStrokeAnnotationGraphic();

        for (CompoundStrokeElement elem : this.elements) {
            sElem.add(elem.getJaxbType());
        }

        for (StrokeAnnotationGraphic sag : annotations) {
            sAnnot.add(sag.getJaxbType());
        }

        return s;
    }

    @Override
    public Uom getUom() {
        if (uom != null) {
            return uom;
        } else {
            return parent.getUom();
        }
    }

    @Override
    public void setUom(Uom u) {
        uom = u;
    }

    @Override
    public Uom getOwnUom() {
        return uom;
    }

    public void addAnnotation(StrokeAnnotationGraphic annotation) {
        if (annotation != null) {
            annotations.add(annotation);
            annotation.setParent(this);
        }
    }

    public boolean moveAnnotationUp(int i) {
        if (i > 0 && i < this.annotations.size()) {
            StrokeAnnotationGraphic anno = annotations.remove(i);
            annotations.add(i - 1, anno);
            return true;
        }
        return false;
    }

    public boolean moveAnnotationDown(int i) {
        if (i >= 0 && i < this.annotations.size() - 1) {
            StrokeAnnotationGraphic anno = annotations.remove(i);
            annotations.add(i + 1, anno);
            return true;
        }
        return false;
    }

    public boolean removeAnnotation(int i) {
        try {
            annotations.remove(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addElement(CompoundStrokeElement element) {
        if (element != null) {
            elements.add(element);
            element.setParent(this);
        }
    }

    public boolean moveElementUp(int i) {
        if (i > 0 && i < this.elements.size()) {
            CompoundStrokeElement elem = elements.remove(i);
            elements.add(i - 1, elem);
            return true;
        }
        return false;
    }

    public boolean moveElementDown(int i) {
        if (i >= 0 && i < this.elements.size() - 1) {
            CompoundStrokeElement elem = elements.remove(i);
            elements.add(i + 1, elem);
            return true;
        }
        return false;
    }

    public boolean removeElement(int i) {
        try {
            elements.remove(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
