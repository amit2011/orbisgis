/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.view.toc.actions.cui.legend.panels;

import org.orbisgis.coremap.renderer.se.Symbolizer;
import org.orbisgis.legend.thematic.*;
import org.orbisgis.legend.thematic.categorize.CategorizedArea;
import org.orbisgis.legend.thematic.categorize.CategorizedLine;
import org.orbisgis.legend.thematic.categorize.CategorizedPoint;
import org.orbisgis.legend.thematic.constant.UniqueSymbolArea;
import org.orbisgis.legend.thematic.constant.UniqueSymbolLine;
import org.orbisgis.legend.thematic.constant.UniqueSymbolPoint;
import org.orbisgis.legend.thematic.map.MappedLegend;
import org.orbisgis.legend.thematic.recode.RecodedArea;
import org.orbisgis.legend.thematic.recode.RecodedLine;
import org.orbisgis.legend.thematic.recode.RecodedPoint;
import org.orbisgis.legend.thematic.uom.SymbolUom;

/**
 * Helper class for legend UIs, used to get a new fallback symbolizer based on
 * a given legend.
 *
 * @author Adam Gouge
 * @author Alexis Guéganno
 */
public class Util {

    /**
     * Returns a new fallback symbolizer based on the given legend.
     *
     * @param legend Legend
     * @return A new fallback symbolizer based on the given legend
     */
    public static Symbolizer getFallbackSymbolizer(MappedLegend legend) {
        SymbolizerLegend symLeg = getSymbolizerLegend(legend);
        setParameters(legend, symLeg);
        return symLeg.getSymbolizer();
    }

    /**
     * Returns a new fallback {@link SymbolizerLegend} based on the given
     * legend's fallback parameters.
     *
     * @param legend Legend
     * @return New fallback SymbolizerLegend
     */
    private static SymbolizerLegend getSymbolizerLegend(MappedLegend legend) {
        SymbolParameters fallbackParameters = legend.getFallbackParameters();
        SymbolizerLegend symLeg;
        if (legend instanceof CategorizedLine ||
                legend instanceof RecodedLine) {
            symLeg = new UniqueSymbolLine((LineParameters) fallbackParameters);
        } else if (legend instanceof CategorizedArea ||
                legend instanceof RecodedArea) {
            symLeg = new UniqueSymbolArea((AreaParameters) fallbackParameters);
        } else if (legend instanceof CategorizedPoint ||
                legend instanceof RecodedPoint) {
            symLeg = new UniqueSymbolPoint((PointParameters) fallbackParameters);
        } else {
            throw new IllegalArgumentException("Legend must be Categorized or Recoded");
        }
        return symLeg;
    }

    /**
     * Updates the SymbolUom, OnVertexOnInterior and StrokeUOM parameters
     * of the given SymbolizerLegend by using the given legend.
     *
     * @param legend Legend
     * @param symLeg SymbolizerLegend
     */
    private static void setParameters(MappedLegend legend, SymbolizerLegend symLeg) {
        // Set StrokeUom from the legend.
        symLeg.setStrokeUom(legend.getStrokeUom());
        // Set SymbolUom from the legend if necessary.
        if (legend instanceof SymbolUom
                && symLeg instanceof SymbolUom) {
            ((SymbolUom) symLeg).setSymbolUom(((SymbolUom) legend).getSymbolUom());
        }
        // Set OnVertexOnInterior from the legend if necessary.
        if (legend instanceof OnVertexOnInterior
                && symLeg instanceof OnVertexOnInterior) {
            if (((OnVertexOnInterior) legend).isOnVertex()) {
                ((OnVertexOnInterior) symLeg).setOnVertex();
            } else {
                ((OnVertexOnInterior) symLeg).setOnInterior();
            }
        }
    }
}
