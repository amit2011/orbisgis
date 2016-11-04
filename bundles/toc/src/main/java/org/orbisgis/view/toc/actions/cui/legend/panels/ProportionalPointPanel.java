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
package org.orbisgis.view.toc.actions.cui.legend.panels;

import org.orbisgis.coremap.renderer.se.parameter.ParameterException;
import org.orbisgis.legend.thematic.proportional.ProportionalPoint;
import org.orbisgis.view.toc.actions.cui.SimpleGeometryType;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legend.components.MaxSizeSpinner;
import org.orbisgis.view.toc.actions.cui.legend.components.MinSizeSpinner;
import org.orbisgis.view.toc.actions.cui.legend.components.OnVertexOnInteriorButtonGroup;
import org.orbisgis.view.toc.actions.cui.legend.components.PPointFieldsComboBox;
import org.orbisgis.view.toc.actions.cui.legend.components.SymbolUOMComboBox;
import org.orbisgis.view.toc.actions.cui.legend.components.WKNComboBox;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.sql.DataSource;
import javax.swing.*;

/**
 * "Proportional Point" settings panel.
 *
 * @author Adam Gouge
 */
public class ProportionalPointPanel extends AbsPanel {

    private static final I18n I18N = I18nFactory.getI18n(ProportionalPointPanel.class);

    private DataSource dataSource;
    private String table;
    private int geometryType;

    private PPointFieldsComboBox pPointFieldsComboBox;
    private SymbolUOMComboBox symbolUOMComboBox;
    private WKNComboBox wknComboBox;
    private MaxSizeSpinner maxSizeSpinner;
    private MinSizeSpinner minSizeSpinner;

    private OnVertexOnInteriorButtonGroup onVertexOnInteriorButtonGroup;

    /**
     * Constructor
     *
     * @param legend       Legend
     * @param preview      Preview
     * @param title        Title
     * @param dataSource   DataSource
     * @param geometryType The type of geometry linked to this legend
     */
    public ProportionalPointPanel(ProportionalPoint legend,
                                  CanvasSE preview,
                                  String title,
                                  DataSource dataSource,String table,
                                  int geometryType) {
        super(legend, preview, title);
        this.dataSource = dataSource;
        this.table = table;
        this.geometryType = geometryType;
        init();
        addComponents();
    }

    @Override
    public ProportionalPoint getLegend() {
        return (ProportionalPoint) legend;
    }

    @Override
    protected void init() {
        pPointFieldsComboBox = new PPointFieldsComboBox(dataSource, table, getLegend(), preview);
        symbolUOMComboBox = new SymbolUOMComboBox(getLegend(), preview);
        wknComboBox = new WKNComboBox(getLegend(), preview);
        try {
            maxSizeSpinner = new MaxSizeSpinner(getLegend(), preview);
            minSizeSpinner = new MinSizeSpinner(getLegend(), maxSizeSpinner);
            maxSizeSpinner.setMinSizeSpinner(minSizeSpinner);
        } catch (ParameterException e) {
            e.printStackTrace();
        }
        if (geometryType != SimpleGeometryType.POINT) {
            onVertexOnInteriorButtonGroup =
                    new OnVertexOnInteriorButtonGroup(getLegend(), preview);
        }
    }

    @Override
    protected void addComponents() {
        // Field
        add(new JLabel(I18N.tr(NUMERIC_FIELD)));
        add(pPointFieldsComboBox, COMBO_BOX_CONSTRAINTS);
        // Unit of measure - symbol size
        add(new JLabel(I18N.tr(SYMBOL_SIZE_UNIT)));
        add(symbolUOMComboBox, COMBO_BOX_CONSTRAINTS);
        // Symbol
        add(new JLabel(I18N.tr(SYMBOL)));
        add(wknComboBox, COMBO_BOX_CONSTRAINTS);
        // Max size
        add(new JLabel(I18N.tr("Max. size")));
        add(maxSizeSpinner, "growx");
        // Min size
        add(new JLabel(I18N.tr("Min. size")));
        add(minSizeSpinner, "growx");
        // If geometryType != POINT, we must let the user choose if he
        // wants to draw symbols on interior point or on vertices.
        if (geometryType != SimpleGeometryType.POINT) {
            add(new JLabel(I18N.tr(PLACE_SYMBOL_ON)), "span 1 2");
            add(onVertexOnInteriorButtonGroup, "span 1 2");
        }
    }
}
