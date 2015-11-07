package com.chitek.ignition.report.designer.config;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;

import com.chitek.ignition.report.shapes.StatusChart;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel;
import com.inductiveautomation.ignition.client.util.gui.JideToolbarButton;
import com.inductiveautomation.ignition.client.util.gui.ScrollablePanel;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.designer.scripteditor.component.ExtensionCodeEditor;
import com.inductiveautomation.reporting.designer.workspace.ReportingResourceEditor;
import com.inductiveautomation.reporting.designer.workspace.inspector.factories.AbstractSlidingConfigPanel;
import com.inductiveautomation.reporting.designer.workspace.inspector.factories.DataKeyField;
import com.jidesoft.swing.JideButton;

import net.miginfocom.swing.MigLayout;

public class StatusChartConfigPanel extends AbstractSlidingConfigPanel {
	private static final long serialVersionUID = 8126411980756138277L;

	private final ReportingResourceEditor editor;
	private final StatusChart chart;
	private final DataKeyField datasetKey;
	private final DataKeyField seriesKey;
	private final DataKeyField idKey;
	private final DataKeyField startKey;
	private final DataKeyField colorKey;
	private final DataKeyField eventPropsDatasetKey;
	private final JCheckBox scriptEnabled;
	
	public StatusChartConfigPanel(ReportingResourceEditor editor, StatusChart statusChart) {
		this.editor = editor;
		this.chart = statusChart;

		datasetKey = new DataKeyField(editor, chart, chart.getDatasetKey()) {
			protected void onKeyUpdated(String key) {
				StatusChartConfigPanel.this.chart.setDatasetKey(key);
			}
		};
		datasetKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.DataKey.Description"));
		
		idKey = new DataKeyField(editor, chart, chart.getIdKey());
		idKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.IdKey.Description"));
		seriesKey = new DataKeyField(editor, chart, chart.getSeriesKey());
		seriesKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.SeriesKey.Description"));
		startKey = new DataKeyField(editor, chart, chart.getStartKey());
		startKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.StartKey.Description"));
		colorKey = new DataKeyField(editor, chart, chart.getColorKey());
		colorKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.ColorKey.Description"));
		
		eventPropsDatasetKey = new DataKeyField(editor, chart, chart.getEventPropsDatasetKey());
		eventPropsDatasetKey.setToolTipText(BundleUtil.i18n("chi-reporting.Shape.StatusChart.EventPropsDatasetKey.Description"));
		
		scriptEnabled = new JCheckBox(BundleUtil.i18n("words.enabled"), chart.isScriptEnabled());
		scriptEnabled.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				chart.setScriptEnabled(e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
			}
		});
		
	    ScrollablePanel scroll = new ScrollablePanel(new MigLayout());
	    scroll.setScrollableTracksViewportWidth(true);
	    scroll.setScrollableBlockIncrement(20);

	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.DataKey.Name"));
	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.IdKey.Name"),"wrap 0");
	    scroll.add(datasetKey, "gapleft 2px, pushx, growx");
	    scroll.add(idKey, "gapleft 2px, gapright 1px, pushx, growx, wrap u");
	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.SeriesKey.Name"), "skip, wrap 0");
	    scroll.add(seriesKey, "skip, gapleft 2px, pushx, growx, wrap u");
	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.StartKey.Name"), "skip, wrap 0");
	    scroll.add(startKey, "skip, gapleft 2px, pushx, growx, wrap u");
	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.ColorKey.Name"), "skip, wrap 0");
	    scroll.add(colorKey, "skip, gapleft 2px, pushx, growx, wrap u");
	    
	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.EventPropsDatasetKey.Name"), "wrap 0");
	    scroll.add(eventPropsDatasetKey, "gapleft 2px, pushx, growx, wrap u");

	    scroll.add(HeaderLabel.forKey("chi-reporting.Shape.StatusChart.ConfigPanel.Scripting"), "spanx 2, wrap 0");
	    scroll.add(this.scriptEnabled, "spanx 2, split");
	    scroll.add(new JideButton(editScriptAction));
	    
	    JScrollPane jsp = new JScrollPane(scroll);
	    jsp.setBorder(null);
	    jsp.getViewport().setBackground(UIManager.getColor("Panel.background"));
	    add(jsp, "Center");

	    initSlider(jsp);
	}
	
    @Override
    public void commitEdit() {
        super.commitEdit();
        chart.setDatasetKey(datasetKey.getText());
        chart.setIdKey(idKey.getText());
        chart.setSeriesKey(seriesKey.getText());
        chart.setStartKey(startKey.getText());
        chart.setColorKey(colorKey.getText());
        chart.setEventPropsDatasetKey(eventPropsDatasetKey.getText());

        chart.setScriptEnabled(this.scriptEnabled.isSelected());
    }
	
    @Override
    public void updateFromShape() {
    	datasetKey.setText(chart.getDatasetKey());
    	idKey.setText(chart.getIdKey());
    	seriesKey.setText(chart.getSeriesKey());
    	startKey.setText(chart.getStartKey());
    	colorKey.setText(chart.getColorKey());
    	eventPropsDatasetKey.setText(chart.getEventPropsDatasetKey());
    };
    
	@Override
	public String getTitleKey() {
		return "chi-reporting.Shape.StatusChart.ConfigPanel";
	}
 
	private final BaseAction editScriptAction = new BaseAction("chi-reporting.Shape.StatusChart.ConfigPanel.EditScript", JideToolbarButton.EDIT_ICON) {
		public void actionPerformed(ActionEvent e) {
			final ExtensionCodeEditor codeEditor = new ExtensionCodeEditor(StatusChartConfigPanel.this.editor.getDesignerContext());
			codeEditor.setScript(
					ExtensionCodeEditor.headerBuilder()
							.name("configureChart")
							.arg("chart", "A JFreeChart object that will be drawn on the report.")
							.desc("Provides an opportunity to perform chart configuration right before the report is rendered.")
							.defaultImpl(StringUtils.defaultString(StatusChartConfigPanel.this.chart.getScript(), "# Put your code here"))
							.build(50));

			StatusChartConfigPanel.this.slide(
					codeEditor,
					"chi-reporting.Shape.StatusChart.ConfigPanel.EditScript.Name",
					new Runnable() {
						public void run() {
							StatusChartConfigPanel.this.chart.setScript(codeEditor.getUserScript());
						}
			});
		}
	};
	
}
