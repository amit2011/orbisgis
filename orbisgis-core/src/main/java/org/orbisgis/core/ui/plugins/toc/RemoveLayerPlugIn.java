package org.orbisgis.core.ui.plugins.toc;

import org.orbisgis.core.Services;
import org.orbisgis.core.images.IconNames;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.PlugInContext.SelectionAvailability;
import org.orbisgis.core.ui.pluginSystem.PlugInContext.LayerAvailability;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;

public class RemoveLayerPlugIn extends AbstractPlugIn {

	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbContext = context.getWorkbenchContext();
		WorkbenchFrame frame = wbContext.getWorkbench().getFrame().getToc();
		context.getFeatureInstaller().addPopupMenuItem(frame, this,
				new String[] { Names.POPUP_TOC_LAYERS_REMOVE_PATH1 },
				Names.POPUP_TOC_LAYERS_REMOVE_GROUP, false,
				getIcon(IconNames.REMOVE), wbContext);
	}

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		MapContext mapContext = getPlugInContext().getMapContext();
		ILayer[] selectedResources = mapContext.getSelectedLayers();				
		for (ILayer resource : selectedResources) {
			try {
				resource.getParent().remove(resource);
			} catch (LayerException e) {
				Services.getErrorManager().error(
						"Cannot delete layer: " + e.getMessage(), e);
			}
		}		
		return true;
	}

	public boolean isEnabled() {
		return getPlugInContext().checkLayerAvailability(
				new SelectionAvailability[] {SelectionAvailability.SUPERIOR},
				0,
				new LayerAvailability[] {LayerAvailability.LAYER_NOT_NULL});
	}
	
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

}
