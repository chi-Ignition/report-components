package com.chitek.ignition.report.shapes;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

@SuppressWarnings("serial")
public class StatusBarRenderer extends XYBarRenderer {

	private Paint itemPaint;
	private double seriesSpacing=0.9;

	public StatusBarRenderer() {
		super();
	}
	
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
			CrosshairState crosshairState, int pass) {

		EventSeriesDataset eventDataset = (EventSeriesDataset) dataset;
		Event event = eventDataset.getEvent(series, item);
		
		double startX = event.getStart();
		double endX = event.getEnd();
				
		RectangleEdge domainEdge = plot.getDomainAxisEdge();
		double start = domainAxis.valueToJava2D(startX, dataArea, domainEdge);
		double end = domainAxis.valueToJava2D(endX, dataArea, domainEdge);
		double width = end - start;
		RectangleEdge rangeEdge = plot.getRangeAxisEdge();
		double bottom = rangeAxis.valueToJava2D(series-seriesSpacing*0.5, dataArea, rangeEdge);
		double top = rangeAxis.valueToJava2D(series+seriesSpacing*0.5, dataArea, rangeEdge);
		double height = bottom-top;
				
		RectangleEdge barBase;
		Rectangle2D bar = null;
		PlotOrientation orientation = plot.getOrientation();
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(bottom, end, -height, -width);
			barBase = RectangleEdge.LEFT;
		} else { //PlotOrientation.VERTICAL
			bar = new Rectangle2D.Double(start, top, width, height);
			barBase = RectangleEdge.BOTTOM;
		}
		
		setItemPaint(getEventPaint(eventDataset, event));
		getBarPainter().paintBar(g2, this, series, item, bar, barBase);
	}
	
	/**
	 * Set the amount of space that a single series uses. 1.0 means that the
	 * bars use 100% of the space.
	 * 
	 * @param seriesSpacing
	 */
	public void setSeriesSpacing(double seriesSpacing) {
		this.seriesSpacing=Math.max(0.1, Math.min(seriesSpacing, 1.0));
	}

	/**
	 * The legend explains the different colors used to paint the bars. The
	 * colors and descriptions have to be defined as EventProperties in the
	 * EventSeriesDataset
	 *
	 * @return The legend item collection (never <code>null</code>).
	 */
	@Override
	public LegendItemCollection getLegendItems() {
		if (this.getPlot() == null) {
			return new LegendItemCollection();
		}
		LegendItemCollection result = new LegendItemCollection();
		
		int index = this.getPlot().getIndexOf(this);
		XYDataset dataset = this.getPlot().getDataset(index);
		
		if (dataset != null && dataset instanceof EventSeriesDataset) {
			EventSeriesDataset eventDataset = (EventSeriesDataset) dataset;
			for (Object eventId: eventDataset.getKnownEventIds()) {
				LegendItem legendItem = getLegendItem(eventDataset, eventId);
				result.add(legendItem);
			}
		}
		return result;
	}
	
	/**
	 * Returns a legend item for the given event id.
	 * 
	 * @param eventDataset
	 * 	The EventProperties are read from this EventSeriesDataset.
	 * @param eventId
	 * 	The event id.
	 * @return
	 * 	A legend item.
	 */
	public LegendItem getLegendItem(EventSeriesDataset eventDataset, Object eventId) {
		LegendItem item;
		EventProperties eventProps = eventDataset.getEventProperties(eventId);
		if (eventProps != null) {
			item = new LegendItem(eventProps.getDescription());
			item.setFillPaint(eventProps.getColor());
		} else {
			item = new LegendItem("Unknown Event " + eventId.toString());
		}

		return item;
	}
	
	protected Paint getEventPaint(EventSeriesDataset dataset, Event event) {
		EventProperties props = dataset.getEventProperties(event.getId());
		if (props != null) {
			return props.getColor();
		}
		
		return event.getColor();
	}
	
	/**
	 * Set the Paint that is used by the BarPainter.
	 * 
	 * @param paint
	 */
	protected void setItemPaint(Paint paint) {
		// The paint is stored in drawItem. This way we don't have to access the dataset here again for better performance.
		itemPaint = paint;
	}
	
	/**
	 * 	Returns the paint used to color the bars as they are drawn.
	 */
	@Override
	public Paint getItemPaint(int series, int item) {
		return itemPaint;
	}
	
}
