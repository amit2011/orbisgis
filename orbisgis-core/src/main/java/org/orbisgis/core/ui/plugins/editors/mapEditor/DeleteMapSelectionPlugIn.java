/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able
 * to manipulate and create vector and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2009 Erwan BOCHER, Pierre-yves FADET
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    Pierre-Yves.Fadet_at_ec-nantes.fr
 *    thomas.leduc _at_ cerma.archi.fr
 */

package org.orbisgis.core.ui.plugins.editors.mapEditor;

import java.util.Arrays;

import javax.swing.JButton;

import org.gdms.data.DataSource;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.driver.DriverException;
import org.orbisgis.core.Services;
import org.orbisgis.core.images.IconNames;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.ui.editor.IEditor;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.plugins.views.MapEditorPlugIn;

public class DeleteMapSelectionPlugIn extends AbstractPlugIn {

	private JButton btn;

	public DeleteMapSelectionPlugIn() {
		btn = new JButton(getIcon(IconNames.REMOVE));
	}

	public boolean execute(PlugInContext context) throws Exception {
		IEditor editor = getPlugInContext().getActiveEditor();
		MapContext map = (MapContext) editor.getElement().getObject();
		ILayer activeLayer = map.getActiveLayer();
		int[] sel = activeLayer.getSelection().clone();
		Arrays.sort(sel);
		SpatialDataSourceDecorator dataSource = activeLayer.getDataSource();
		try {
			dataSource.setDispatchingMode(DataSource.STORE);
			for (int i = sel.length - 1; i >= 0; i--) {
				dataSource.deleteRow(sel[i]);
			}
			dataSource.setDispatchingMode(DataSource.DISPATCH);
		} catch (DriverException e) {
			Services.getErrorManager().error("Cannot delete selected features",
					e);
		}
		return true;
	}

	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbcontext = context.getWorkbenchContext();
		wbcontext.getWorkbench().getFrame().getEditionMapToolBar().addPlugIn(
				this, btn, context);
	}
	
	public boolean isEnabled() {		
		boolean isEnabled = false;
		MapEditorPlugIn mapEditor = null;
		if((mapEditor=getPlugInContext().getMapEditor()) != null){
			MapContext map = (MapContext) mapEditor.getElement().getObject();
			ILayer activeLayer = map.getActiveLayer();
			if(activeLayer != null
					&& activeLayer.getSelection().length > 0) 
				isEnabled = true;		
		}
		btn.setEnabled(isEnabled);
		return isEnabled;
	}
	
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}
}
