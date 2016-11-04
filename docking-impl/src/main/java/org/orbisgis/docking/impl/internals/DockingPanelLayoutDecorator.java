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

package org.orbisgis.docking.impl.internals;

import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.util.xml.XElement;
import org.orbisgis.sif.docking.DockingPanelLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * DockingPanel use a custom XML parser, this decorator convert from/to DockingFrame parser.
 * @author Nicolas Fortin
 */
public class DockingPanelLayoutDecorator implements MultipleCDockableLayout {
        private DockingPanelLayout externalLayout;

        public DockingPanelLayoutDecorator(DockingPanelLayout externalLayout) {
                this.externalLayout = externalLayout;
        }

        @Override
        public void writeStream(DataOutputStream dataOutputStream) throws IOException {
                externalLayout.writeStream(dataOutputStream);
        }

        @Override
        public void readStream(DataInputStream dataInputStream) throws IOException {
                externalLayout.readStream(dataInputStream);
        }

        @Override
        public void writeXML(XElement xElement) {
                externalLayout.writeXML(new XElementImpl(xElement));
        }

        @Override
        public void readXML(XElement xElement) {
                externalLayout.readXML(new XElementImpl(xElement));
        }

        /**
         * @return OrbisGIS API Layout
         */
        public DockingPanelLayout getExternalLayout() {
                return externalLayout;
        }
}
