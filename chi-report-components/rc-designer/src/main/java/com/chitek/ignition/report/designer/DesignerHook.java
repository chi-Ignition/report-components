package com.chitek.ignition.report.designer;

import com.chitek.ignition.report.shapes.StatusChart;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.model.DesignerModuleHook;
import com.inductiveautomation.reporting.designer.api.DesignerShapeRegistry;
import com.inductiveautomation.rm.archiver.RMArchiver;

public class DesignerHook extends AbstractDesignerModuleHook implements DesignerModuleHook {
	public static final String BUNDLE_NAME = "chi-reporting";

	@Override
	public void startup(DesignerContext context, LicenseState activationState) throws Exception {
		super.startup(context, activationState);

		/* add our bundle to centralize strings and allow i18n support */
		BundleUtil.get().addBundle(BUNDLE_NAME, DesignerHook.class.getClassLoader(), "com/chitek/ignition/report/reporting");

		/* This is where our new shape registered for the Report Designer */
		RMArchiver.registerClass(StatusChart.ARCHIVE_NAME, StatusChart.class);
		DesignerShapeRegistry.get(context).register(StatusChart.class);
	}
	
	
}

