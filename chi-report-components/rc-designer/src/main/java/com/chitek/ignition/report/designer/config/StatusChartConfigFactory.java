package com.chitek.ignition.report.designer.config;

import com.chitek.ignition.report.shapes.StatusChart;
import com.inductiveautomation.reporting.designer.api.ShapeConfigFactory;
import com.inductiveautomation.reporting.designer.api.ShapeConfigPanel;
import com.inductiveautomation.reporting.designer.workspace.ReportingResourceEditor;
import com.inductiveautomation.rm.shape.RMShape;

/**
 * This factory class returns the config panel for the StatusChart.
 * The {@link StatusChart} references this factory with a {@link @ConfigFactory} annotation
 *
 */
public class StatusChartConfigFactory implements ShapeConfigFactory {

	@Override
	public ShapeConfigPanel createConfigPanel(ReportingResourceEditor editor, RMShape shape) {
		return new StatusChartConfigPanel(editor, (StatusChart)shape);
	}

}
