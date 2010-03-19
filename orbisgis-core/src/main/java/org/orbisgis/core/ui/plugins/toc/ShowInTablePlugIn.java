package org.orbisgis.core.ui.plugins.toc;

import org.orbisgis.core.Services;
import org.orbisgis.core.background.BackgroundManager;
import org.orbisgis.core.geocognition.Geocognition;
import org.orbisgis.core.geocognition.GeocognitionElement;
import org.orbisgis.core.geocognition.GeocognitionFilter;
import org.orbisgis.core.images.IconNames;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.ui.editorViews.toc.EditableLayer;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;
import org.orbisgis.core.ui.plugins.views.geocognition.OpenGeocognitionElementJob;

public class ShowInTablePlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		getPlugInContext().executeLayers();
		return true;
	}

	@Override
	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbContext = context.getWorkbenchContext();
		WorkbenchFrame frame = wbContext.getWorkbench().getFrame().getToc();
		context.getFeatureInstaller().addPopupMenuItem(frame, this,
				new String[] { Names.POPUP_TOC_TABLE_PATH1 },
				Names.POPUP_TOC_TABLE_GROUP, false,
				getIcon(IconNames.POPUP_TOC_TABLE_ICON), wbContext);
	}


	public void execute(final MapContext mapContext, ILayer layer) {

		GeocognitionElement[] element = Services.getService(Geocognition.class)
				.getElements(new GeocognitionFilter() {

					@Override
					public boolean accept(GeocognitionElement element) {
						return element.getObject() == mapContext;
					}
				});
		Services.getService(BackgroundManager.class).backgroundOperation(
				new OpenGeocognitionElementJob(new EditableLayer(element[0],
						layer)));
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return getPlugInContext().checkLayerAvailability();
	}

	public boolean accepts(MapContext mc, ILayer layer) {
		return layer.getDataSource() != null;
	}

	public boolean acceptsSelectionCount(int selectionCount) {
		return selectionCount == 1;
	}
}