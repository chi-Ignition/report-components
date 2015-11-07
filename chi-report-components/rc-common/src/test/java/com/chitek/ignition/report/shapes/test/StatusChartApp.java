package com.chitek.ignition.report.shapes.test;

import org.jfree.chart.ChartPanel;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.chitek.ignition.report.shapes.StatusChart;

@SuppressWarnings("serial")
public class StatusChartApp extends ApplicationFrame {

	public StatusChartApp(String title) {
		super(title);
		
		StatusChart statusChart = new StatusChart();
		statusChart.setShowLegend(true);
        // add the chart to a panel...
        final ChartPanel chartPanel = new ChartPanel(statusChart.createChart());
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
	}

	public static void main(String[] args) {
        final StatusChartApp demo = new StatusChartApp("Chart Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
	}
}
