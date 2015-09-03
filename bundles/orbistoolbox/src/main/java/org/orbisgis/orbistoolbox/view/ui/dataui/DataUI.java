/**
 * OrbisToolBox is an OrbisGIS plugin dedicated to create and manage processing.
 * <p/>
 * OrbisToolBox is distributed under GPL 3 license. It is produced by CNRS <http://www.cnrs.fr/> as part of the
 * MApUCE project, funded by the French Agence Nationale de la Recherche (ANR) under contract ANR-13-VBDU-0004.
 * <p/>
 * OrbisToolBox is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p/>
 * OrbisToolBox is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with OrbisToolBox. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.orbisgis.org/> or contact directly: info_at_orbisgis.org
 */

package org.orbisgis.orbistoolbox.view.ui.dataui;

import org.orbisgis.orbistoolbox.model.DescriptionType;
import org.orbisgis.orbistoolbox.model.Input;
import org.orbisgis.orbistoolbox.model.Output;

import javax.swing.*;
import java.net.URI;
import java.util.Map;

/**
 * Interface for the definition of the ui to give the input value for a data type (LiteralData, RawData ...).
 *
 * @author Sylvain PALOMINOS
 **/

public interface DataUI {

    /**
     * Return the ui for the definition of the input data.
     * @param input Input to render.
     * @param dataMap Map that will contain the data.
     * @return JComponent containing the ui.
     */
    public JComponent createUI(Input input, Map<URI, Object> dataMap);

    /**
     * Return the ui for the definition of the output data.
     * @param output Output to render.
     * @param dataMap Map that will contain the data.
     * @return JComponent containing the ui.
     */
    public JComponent createUI(Output output, Map<URI, Object> dataMap);

    /**
     * Returns the map of default input value if it exists.
     * @param descriptionType Input or Output to analyse.
     * @return The default input map.
     */
    public Map<URI, Object> getDefaultValue(DescriptionType descriptionType);
}