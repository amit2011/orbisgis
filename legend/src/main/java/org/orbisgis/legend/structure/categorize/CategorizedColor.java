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
 * Copyright (C) 2015-2016 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.legend.structure.categorize;

import org.orbisgis.coremap.renderer.se.parameter.ParameterException;
import org.orbisgis.coremap.renderer.se.parameter.SeParameter;
import org.orbisgis.coremap.renderer.se.parameter.color.Categorize2Color;
import org.orbisgis.coremap.renderer.se.parameter.color.ColorLiteral;
import org.orbisgis.coremap.renderer.se.parameter.color.ColorParameter;
import org.orbisgis.coremap.renderer.se.parameter.real.RealAttribute;
import org.orbisgis.coremap.renderer.se.parameter.real.RealLiteral;
import org.orbisgis.coremap.renderer.se.parameter.real.RealParameter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexis Guéganno
 */
public class CategorizedColor extends CategorizedLegend<Color>{
    private ColorParameter parameter = new ColorLiteral();

    /**
     * Build a CategorizedColor from the given ColorParameter.
     * @param sp The input parameter.
     */
    public CategorizedColor(ColorParameter sp){
        setParameter(sp);
    }

    @Override
    public SeParameter getParameter() {
        return parameter;
    }

    /**
     * Replaces the inner ColorParameter with the given one. {@code param} must either be a literal or a
     * Categorize2Color whose lookup value is a simple RealAttribute
     * @param param The new inner ColorParameter used in this CategorizedColor.
     * @throws IllegalArgumentException if param can't be used to build a valid CategorizedColor
     */
    public void setParameter(ColorParameter param) {
        if(param instanceof ColorLiteral){
            parameter = param;
            fireTypeChanged();
        } else if(param instanceof Categorize2Color){
            RealParameter rp = ((Categorize2Color) param).getLookupValue();
            if(rp instanceof RealAttribute){
                parameter = param;
                field = ((RealAttribute) rp).getColumnName();
                fireTypeChanged();
            } else {
                throw new IllegalArgumentException("The given ColorParameter instance can't be recognized as a " +
                        "valid CategorizedColor.");
            }
        } else {
            throw new IllegalArgumentException("The given ColorParameter instance can't be recognized as a " +
                    "valid CategorizedColor.");
        }
    }

    /**
     * Gets the value obtained when the input data can't be processed for whatever reason.
     * @return The value directly as a Color.
     */
    public Color getFallbackValue(){
        if(parameter instanceof ColorLiteral){
            return ((ColorLiteral) parameter).getColor(null);
        } else {
            return ((Categorize2Color)parameter).getFallbackValue().getColor(null);
        }
    }

    /**
     * Sets the value obtained when the input data can't be processed for whatever reason.
     * @param value The new fallback value.
     */
    public void setFallbackValue(Color value) {
        if(parameter instanceof ColorLiteral){
            ((ColorLiteral) parameter).setColor(value);
        } else {
            ((Categorize2Color)parameter).setFallbackValue(new ColorLiteral(value));
        }
    }

    @Override
    public Color get(Double d){
        if(parameter instanceof ColorLiteral){
            return Double.isInfinite(d) && d < 0 ? ((ColorLiteral) parameter).getColor(null) : null;
        } else {
            try{
                ColorParameter sp = ((Categorize2Color)parameter).get(new RealLiteral(d));
                return sp == null ? null : sp.getColor(null);
            } catch (ParameterException pe){
                throw new IllegalArgumentException("Can't process the input value: "+d, pe);
            }
        }
    }

    @Override
    public void put(Double d, Color v){
        if(d == null || v == null){
            throw new NullPointerException("Null values are not allowed in this mapping.");
        }
        if(parameter instanceof ColorLiteral){
            if(Double.isInfinite(d) && d < 0){
                ((ColorLiteral)parameter).setColor(v);
            } else {
                try{
                    Color current = parameter.getColor(null);
                    Categorize2Color c2s = new Categorize2Color(new ColorLiteral(current),
                            new ColorLiteral(current),
                            new RealAttribute(getField()));
                    c2s.put(new RealLiteral(d),new ColorLiteral(v));
                    parameter = c2s;
                    fireTypeChanged();
                } catch (ParameterException pe){
                    throw new IllegalStateException("We've failed at retrieved the value of a literal. " +
                            "Something is going really wrong here.");
                }
            }
        } else {
            ((Categorize2Color)parameter).put(new RealLiteral(d), new ColorLiteral(v));
        }
    }

    @Override
    public Color remove(Double d){
        if(d==null){
            throw new NullPointerException("The input threshold must not be null");
        }
        if(parameter instanceof ColorLiteral){
            return null;
        } else {
            Categorize2Color c2s = (Categorize2Color) parameter;
            ColorParameter ret = c2s.remove(new RealLiteral(d));
            if(ret == null){
                return null;
            } else if(c2s.getNumClasses()==1 && c2s.getFallbackValue().equals(c2s.get(0))){
                parameter = new ColorLiteral(c2s.getFallbackValue().getColor(null));
            }
            if(ret instanceof ColorLiteral){
                try{
                    return ret.getColor(null);
                } catch (ParameterException pe){
                    throw new IllegalStateException("We've failed at retrieved the value of a literal. " +
                            "Something is going really wrong here.");
                }
            } else {
                throw new IllegalStateException("We're not supposed to have values that are not ColorLiteral in this categorize.");
            }
        }
    }

    @Override
    public Color getFromLower(Double d){
        if(d==null){
            throw new NullPointerException("The input threshold must not be null");
        }
        if(parameter instanceof ColorLiteral){
            return ((ColorLiteral) parameter).getColor(null);
        } else {
            Color col = get(d);
            if(col == null){
                Categorize2Color c2s = (Categorize2Color) parameter;
                Map<String,Object> inp = new HashMap<>();
                inp.put(getField(), d);
                try {
                    return c2s.getColor(inp);
                } catch (ParameterException e) {
                    throw new IllegalStateException("May this categorize need many fields ?");
                }
            } else {
                return col;
            }
        }
    }
}
