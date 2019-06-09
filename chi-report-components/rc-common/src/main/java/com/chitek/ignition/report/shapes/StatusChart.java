package com.chitek.ignition.report.shapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.python.core.Py;
import org.python.core.PyObject;

import com.inductiveautomation.ignition.common.TypeUtilities;
import com.inductiveautomation.ignition.common.script.ScriptFunction;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.reporting.common.api.QueryResults.Row;
import com.inductiveautomation.reporting.common.api.ReportRenderContext;
import com.inductiveautomation.reporting.common.api.shape.ConfigFactory;
import com.inductiveautomation.reporting.common.api.shape.DataKeyProperty;
import com.inductiveautomation.reporting.common.api.shape.InitialSize;
import com.inductiveautomation.reporting.common.api.shape.KeyBase;
import com.inductiveautomation.reporting.common.api.shape.OrderBelow;
import com.inductiveautomation.reporting.common.api.shape.Preferred;
import com.inductiveautomation.reporting.common.api.shape.ShapeMeta;
import com.inductiveautomation.reporting.common.api.shape.ShapeProperty;
import com.inductiveautomation.rm.archiver.RXArchiver;
import com.inductiveautomation.rm.archiver.RXElement;
import com.inductiveautomation.rm.base.RMGroup;
import com.inductiveautomation.rm.base.RMKey;
import com.inductiveautomation.rm.base.RMListUtils;
import com.inductiveautomation.rm.shape.RMShape;
import com.inductiveautomation.rm.shape.RMTableRowRPG;
import com.inductiveautomation.rm.shape.ReportOwner;
import com.inductiveautomation.rm.shape.j2dshapes.AbstractJ2DShape;

/**
 * This class extends AbstractJ2DShape (itself extending {@link RMShape}) to provide an easy way to add
 * simple shapes to the Report Design Palette.
 */
@ShapeMeta(category = "reporting.Category.Charts", iconPath = "status-chart.png")
@KeyBase("chi-reporting.Shape.StatusChart")  // the base resource path to our bundle containing property terms for this component
@ConfigFactory("com.chitek.ignition.report.designer.config.StatusChartConfigFactory")
@InitialSize(width=300, height=200)
public class StatusChart extends AbstractJ2DShape {
    public static final String ARCHIVE_NAME = "chi-rc-status-chart";	   // unique id of serialization archive
    
    protected Color plotBackground = new Color(232, 234, 232, 128);;
    protected Color plotBackgroundAlternate = plotBackground;
	
    protected String datasetKey="";
    protected String idKey="id";
    protected String seriesKey="id";
    protected String startKey="start";
    protected String colorKey="color";
	
    protected String eventPropsDatasetKey="";

    protected String dateFormat;
    protected boolean showLegend=true;
    protected boolean showRangeAxis=true;
    protected boolean showDomainAxis=true;
    protected double seriesSpacing=0.95;	// 0.1 to 1.0 - width of the bars
    protected Font axisLabelFont=new Font("Dialog", 0, 12);
    protected Font axisTickLabelFont=new Font("Dialog", 0, 10);
	private Date startDate;
	private Date endDate;

    protected String script = null;
    protected boolean scriptEnabled = false;
    
    protected boolean sampleData = true;
    protected List<?> eventData;
    protected List<?> eventPropsData;

    protected void render(Graphics2D g, int width, int height) {
    	// don't draw if superclass visibility is disabled
        if (isVisible()) {
            JFreeChart chart = createChart();
            runScript(chart);
            chart.draw(g, new Rectangle(width, height));
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

    }

    public JFreeChart createChart() {
		EventSeriesDataset dataset = sampleData ? getDemoDataset() : getDataset();
		
    	String[] labels=new String[dataset.getSeriesCount()];
    	for (int i=0; i<dataset.getSeriesCount(); i++) {
    		labels[i]=dataset.getSeriesKey(i).toString();
    	}
        SymbolAxis domainAxis = new SymbolAxis(null,labels);
        domainAxis.setTickLabelFont(axisTickLabelFont);
        domainAxis.setGridBandPaint(getPlotBackground());
        domainAxis.setGridBandAlternatePaint(getAlternatePlotBackground());
        domainAxis.setVisible(showDomainAxis);
        
        DateAxis dateAxis = new DateAxis();
        dateAxis.setTickLabelFont(axisTickLabelFont);
		if (StringUtils.isNotBlank(dateFormat)) {
			try {
				dateAxis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
			} catch (Exception e) {
				log.warnf(String.format("Bad date format: \"%s\"", dateFormat));
			}
		}
		Range datasetRange = dataset.getDomainBounds(false);
		Range dateRange = new Range( 
				(startDate != null?startDate.getTime():datasetRange.getLowerBound()),
				(endDate != null?endDate.getTime():datasetRange.getUpperBound())
				);
		
		dateAxis.setRange(dateRange);
		dateAxis.setVisible(showRangeAxis);
        
		StatusBarRenderer renderer = new StatusBarRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setSeriesSpacing(seriesSpacing);
        XYPlot plot = new XYPlot(dataset, dateAxis, domainAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        if (showLegend) {
        	LegendTitle legend = new LegendTitle(renderer);
        	legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        	legend.setFrame(new LineBorder());
        	legend.setBackgroundPaint(Color.white);
        	legend.setPosition(RectangleEdge.BOTTOM);
        	legend.setItemFont(axisLabelFont);
        	chart.addLegend(legend);
        }
        chart.setBackgroundPaint(null);
        
        return chart;
    }
    
    /**
     * Run the user script, if it is configured.
     * 
     * @param chart
     * 	The chart is passed to the script as a parameter
     */
	private void runScript(JFreeChart chart) {
		if ((scriptEnabled) && (StringUtils.isNotBlank(script))) {
			ReportRenderContext ctx = ReportRenderContext.get();
			if (ctx == null) {
				log.warn("No render context, scripts disabled.");
			} else {
				ScriptManager manager = ctx.getScriptManager();
				try {
					String wholeScript = "def configureChart(chart):\n" + this.script;
					ScriptFunction function = manager.compileFunction(wholeScript);
					function.invoke(new PyObject[] { Py.java2py(chart) });
				} catch (Exception ex) {
					log.warn("Unable to run chart customization function.", ex);
				}
			}
		}
	}
    
    /**
     * Returns a demo dataset of events.
     * 
     * @return
     * 	The demo dataset.
     */
    protected EventSeriesDataset getDemoDataset() {
            	
        EventSeries events1 = new EventSeries("Series 1");
        EventSeries events2 = new EventSeries("Series 2");
        
        Calendar cal = Calendar.getInstance();
        int eventId = 1;
    	for (int i=0; i < 10; i++) {
    		Date start=cal.getTime();
        	cal.add(Calendar.MINUTE, 10);
        	Date end=cal.getTime();
        	events1.addEvent(start, end, eventId, null);
        	if (eventId++>3) {
        		eventId=1;
        	}
    	}
    	
    	cal = Calendar.getInstance();
    	for (int i=0; i < 5; i++) {
    		Date start=cal.getTime();
        	cal.add(Calendar.MINUTE, 20);
        	Date end=cal.getTime();
        	events2.addEvent(start, end, i*2, new Color(i*21,255-i*21,i*21));
    	}
    	
    	final EventSeriesDataset s1 = new EventSeriesDataset();
        s1.add(events1);
        s1.add(events2);
        
        for (int id = 1; id < 6; id++) {
        	EventProperties props = new EventProperties(id, String.format("Event %s", id), new Color(id*21,0,id*21));
        	s1.addEventProperties(id, props);
        }

        return s1;
    }
    
    /**
     * Create the internally used EventSeriesDataset from the input data.
     * 
     * @return The dataset.
	 */
    protected EventSeriesDataset getDataset() {
    	EventSeriesDataset events = new EventSeriesDataset();
    	Map<Comparable<?>, Event>previousEvents=new HashMap<Comparable<?>, Event>();
    
    	if (eventData.isEmpty()) {
    		return events;
    	}
    	
		// Usage of color key is optional
		boolean useColor = (!colorKey.isEmpty() && RMKey.hasKey(eventData.get(0), colorKey));
		
		// Check configured keys
		if (!seriesKey.isEmpty() &&  !RMKey.hasKey(eventData.get(0), seriesKey)) {
			log.errorf("Configured series key '%s' not found in dataset", seriesKey);
			return null;
		}
		if (!RMKey.hasKey(eventData.get(0), idKey)) {
			log.errorf("Configured id key '%s' not found in dataset", idKey);
			return null;
		}
		if (!RMKey.hasKey(eventData.get(0), startKey)) {
			log.errorf("Configured start time key '%s' not found in dataset", startKey);
			return null;
		}
    	
		for (Object row : eventData) {
			try {
				
				String series = seriesKey.isEmpty() ? "1" : RMKey.getStringValue(row, seriesKey).trim();
				String id = RMKey.getStringValue(row, idKey).trim();
				long start = (Long) TypeUtilities.coerceNullSafe(RMKey.getValue(row, startKey), Long.class);
				
				Color color = useColor ? TypeUtilities.toColor(RMKey.getValue(row, colorKey, String.class))	: Color.BLUE;
				Event event = new Event(start, start, id, color);
				
				EventSeries eventSeries = events.getSeries(series);
				if (eventSeries == null) {
					eventSeries = new EventSeries(series);
					events.add(eventSeries);
					previousEvents.put(series, event);
				} else {
					// series is already present - add the new value
					Event previousEvent = previousEvents.replace(series, event);
					previousEvent.setEnd(event.getStart());
					eventSeries.addEvent(previousEvent);
				}

			} catch (Exception e) {
				log.warn("Exception in getDataset()", e);
			}
		}
		// Set the end time for the last events
		for (Entry<Comparable<?>, Event> previousEvent : previousEvents.entrySet()) {
			events.getSeries(previousEvent.getKey()).addEvent(previousEvent.getValue());
		}
    	
		if (eventPropsData!=null && !eventPropsData.isEmpty()) {
			boolean propsDatasetOk = RMKey.hasKey(eventPropsData.get(0), "id")
					&& RMKey.hasKey(eventPropsData.get(0), "description")
					&& RMKey.hasKey(eventPropsData.get(0), "color");
			
			if (!propsDatasetOk) {
				log.warn("Event properties dataset does not contain all required columns (id, description,color)");
			} else {
				for (Object row : eventPropsData) {
					String idKey = RMKey.getStringValue(row, "id").trim();
					if (idKey != null) {
						String description = RMKey.getStringValue(row, "description").trim();
						Color color = TypeUtilities.toColor(RMKey.getStringValue(row, "color"));
						EventProperties properties = new EventProperties(idKey, description, color);
						events.addEventProperties(idKey, properties);
					}
				}
			}
		}

    	return events;
    }
    
	/*
	 * The ShapeProperty annotations work in concert with the KeyBase annotation
	 * on the class. By giving this method the annotation (and assuming we have
	 * both getter and setter), we enable this field to be added to the property
	 * table when designing/editing our component. So (keybase.Background, will
	 * be checked for a .Name, .Description and .Category to fill in values.
	 * Take a look at common/src/main/resources/component.properties to see
	 * examples.
	 */
	@ShapeProperty("DataKey")
	@Preferred
	@DataKeyProperty
	@Override
	public String getDatasetKey() {
		return datasetKey;
	}

	public void setDatasetKey(String key) {
		datasetKey = key;
	}
    
	@ShapeProperty("IdKey")
	@Preferred
	@DataKeyProperty
	public String getIdKey() {
		return idKey;
	}

	public void setIdKey(String key) {
		idKey = key;
	}

	@ShapeProperty("SeriesKey")
	@Preferred
	@OrderBelow("idKey")
	@DataKeyProperty
	public String getSeriesKey() {
		return seriesKey;
	}

	public void setSeriesKey(String key) {
		seriesKey = key;
	}

	@ShapeProperty("StartKey")
	@Preferred
	@OrderBelow("seriesKey")
	@DataKeyProperty
	public String getStartKey() {
		return startKey;
	}

	public void setStartKey(String key) {
		startKey = key;
	}
	
	@ShapeProperty("ColorKey")
	@Preferred
	@OrderBelow("startKey")
	@DataKeyProperty
	public String getColorKey() {
		return colorKey;
	}

	public void setColorKey(String key) {
		colorKey = key;
	}
	
	/**
	 * Get the key to the dataset with event properties.
	 * @return
	 */
	@ShapeProperty("EventPropsDatasetKey")
	@DataKeyProperty
	@Preferred
	public String getEventPropsDatasetKey() {
		return eventPropsDatasetKey;
	}

	public void setEventPropsDatasetKey(String key) {
		eventPropsDatasetKey = key;
	}
	
	@ShapeProperty("DateFormat")
	public String getDateFormat() {
		return dateFormat;
	}
	
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		setDirty();
	}

    @ShapeProperty("SeriesSpacing")
    public double getSeriesSpacing() {
        return seriesSpacing;
    }

    public void setSeriesSpacing(double seriesSpacing) {
        if (this.seriesSpacing != seriesSpacing) {
        	this.seriesSpacing = Math.max(0.1, Math.min(seriesSpacing, 1.0));
            setDirty();
        }
    }	
	
    @ShapeProperty("AxisLabelFont")
    public Font getAxisLabelFont()
    {
      return axisLabelFont;
    }
    
    public void setAxisLabelFont(Font axisLabelFont) {
      this.axisLabelFont = axisLabelFont;
      setDirty();
    }

    @ShapeProperty("AxisTickLabelFont")
    @OrderBelow("axisLabelFont")
    public Font getAxisTickLabelFont() {
      return axisTickLabelFont;
    }

    public void setAxisTickLabelFont(Font axisTickLabelFont) {
      this.axisTickLabelFont = axisTickLabelFont;
      setDirty();
    }
    
    @ShapeProperty("PlotBackground")
    public Color getPlotBackground() {
        return plotBackground;
    }

    public void setPlotBackground(Color background) {
        if (!this.plotBackground.equals(background)) {
            this.plotBackground = background;
            setDirty();
        }
    }

    @ShapeProperty("AlternatePlotBackground")
    @OrderBelow("plotBackground")
    public Color getAlternatePlotBackground() {
        return plotBackgroundAlternate;
    }

    public void setAlternatePlotBackground(Color background) {
        if (!this.plotBackgroundAlternate.equals(background)) {
            this.plotBackgroundAlternate = background;
            setDirty();
        }
    }

    @ShapeProperty("ShowLegend")
    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
        setDirty();
    }
    
    @ShapeProperty("ShowRangeAxis")
    public boolean isShowRangeAxis() {
        return this.showRangeAxis;
    }

    public void setShowRangeAxis(final boolean showRangeAxis) {
        this.showRangeAxis = showRangeAxis;
        setDirty();
    }
   
    @ShapeProperty("ShowDomainAxis")
    public boolean isShowDomainAxis() {
        return this.showDomainAxis;
    }

    public void setShowDomainAxis(final boolean showDomainAxis) {
        this.showDomainAxis = showDomainAxis;
        setDirty();
    }
   
    @ShapeProperty("StartDate")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
        setDirty();
    }
    
    @ShapeProperty("EndDate")
    @OrderBelow("startDate")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
        setDirty();
    }
    
	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
		setDirty();
	}

    public boolean isScriptEnabled()
    {
      return scriptEnabled;
    }   
    
    public void setScriptEnabled(boolean scriptEnabled)
    {
      this.scriptEnabled = scriptEnabled;
      setDirty();
    }
        
    /* override to add setDirty() call to trigger the render() when changed */
    @Override
    public void setVisible(boolean aValue) {
        super.setVisible(aValue);
        setDirty();
    }

    /**
     * Shapes and components go through an XML serialization process.  To add things to the archiver, use
     * {@link RXElement} and add(String, name), where the String is a key name you will use to pull the value
     * from the archiver when deserializing.
     */
    @Override
    public RXElement toXML(RXArchiver anArchiver) {
        RXElement e = super.toXML(anArchiver);

        e.setName(ARCHIVE_NAME);   // set a unique name for this archive

        e.add("datakey", datasetKey);
        e.add("idKey", idKey);
        e.add("seriesKey", seriesKey);
        e.add("startKey", startKey);
        e.add("colorKey", colorKey);
        e.add("eventPropsDatasetKey", eventPropsDatasetKey);
        
        e.add("showLegend", showLegend);
        e.add("showRangeAxis", showRangeAxis);
        e.add("showDomainAxis", showDomainAxis);   
        e.add("seriesSpacing", seriesSpacing);
        
        e.add("plotBackground", plotBackground);
        e.add("plotBackgroundAlt", plotBackgroundAlternate);
        
        e.add("axisLabelFont", axisLabelFont);
        e.add("axisTickLabelFont", axisTickLabelFont);
        
        e.add("startDate", new Long(startDate!=null?startDate.getTime():0L));
        e.add("startDate", new Long(endDate!=null?endDate.getTime():0L));
        
        e.add("scriptEnabled", scriptEnabled);
		if (StringUtils.isNotBlank(script)) {
			e.addElement(new RXElement("script", script));
		}
        
        return e;
    }

    /**
     *  Pull data out of serialization by using the various RXElement.get() calls for your types.
     */
    @Override
    public Object fromXML(RXArchiver archiver, RXElement e) {
        super.fromXML(archiver, e);
 
		datasetKey = e.getAttributeValue("datakey", "");
		idKey = e.getAttributeValue("idKey", "id");
		seriesKey = e.getAttributeValue("seriesKey", "series");
		startKey = e.getAttributeValue("startKey", "start");
		colorKey = e.getAttributeValue("colorKey", "");
		eventPropsDatasetKey = e.getAttributeValue("eventPropsDatasetKey", "");

		showLegend = e.getAttributeBooleanValue("showLegend", true);
		showRangeAxis = e.getAttributeBooleanValue("showRangeAxis", true);
		showDomainAxis = e.getAttributeBooleanValue("showDomainAxis", true);
		setSeriesSpacing(e.getAttributeFloatValue("seriesSpacing", 0.8f));

		plotBackground = e.getAttributeColorValue("plotBackground", plotBackground);
		plotBackgroundAlternate = e.getAttributeColorValue("plotBackgroundAlt", plotBackgroundAlternate);

		axisLabelFont = e.getAttributeFontValue("axisLabelFont", axisLabelFont);
		axisTickLabelFont = e.getAttributeFontValue("axisTickLabelFont", axisTickLabelFont);

		startDate = new Date(e.getAttributeNumberValue("startDate", 0).longValue());
		if (startDate.getTime()==0) startDate=null; 
		endDate = new Date(e.getAttributeNumberValue("endDate", 0).longValue());
		if (endDate.getTime()==0) endDate=null; 
		
		scriptEnabled = e.getAttributeBooleanValue("scriptEnabled", false);
		RXElement script = e.getElement("script");
		if (script != null) {
			this.script = script.getValue();
		}

		return this;
	}

    /**
     * Called when the report is generated.  If our shape were to require data to create/populate itself, we would do so
     * in this method by building a dataset and instantiating a clone() of our shape with the associated dataset.
     */
    @Override
    protected RMShape rpgShape(ReportOwner owner, RMShape parent){
    	StatusChart rpg = (StatusChart) clone();
        
    	List<?> dataset = null;
    	String datasetKey = getDatasetKey();
    	RMShape parentTableRow = parent instanceof RMTableRowRPG?parent:parent.getParent(RMTableRowRPG.class);
    	if (parentTableRow != null) {
    		if ((datasetKey == null) || (datasetKey.length() == 0))
    	      {
    	        RMGroup tableRowGroup = (RMGroup)owner.peekDataStack();
    	        
    	        dataset = tableRowGroup.cloneDeep();
    	        if (tableRowGroup.isLeaf()) {
    	          dataset = RMListUtils.newList(new List[] { dataset });
    	        }
    	      }
    	      else
    	      {
    	        dataset = owner.getKeyChainListValue(datasetKey);
    	      }
    	} else { 
    		dataset = owner.getKeyChainListValue(datasetKey);
    	}
    	
        if (dataset == null) {
        	dataset=new ArrayList<Row>();
        }
        
        List<?> propsDataset = owner.getKeyChainListValue(getEventPropsDatasetKey());
        if (propsDataset == null) {
        	propsDataset=new ArrayList<Row>();
        }
        
        rpg.eventData = dataset;
        rpg.eventPropsData = propsDataset;
                   
        rpg.sampleData=false;
        return rpg;
    }
    
    @Override
    public boolean equals(Object obj) {

        if (obj == this){
            return true;
        }
        
        if (!super.equals(obj) || !(obj instanceof StatusChart)){
            return false;
        }

        StatusChart other = (StatusChart) obj;
        boolean equals =  !(!other.datasetKey.equals(datasetKey)
        		|| !other.idKey.equals(idKey)
        		|| !other.seriesKey.equals(seriesKey)
        		|| !other.startKey.equals(startKey)
        		|| !other.colorKey.equals(colorKey)
        		|| !other.eventPropsDatasetKey.equals(eventPropsDatasetKey)
        		|| !other.plotBackground.equals(plotBackground)
        		|| !other.plotBackgroundAlternate.equals(plotBackgroundAlternate)
        		|| !other.axisLabelFont.equals(axisLabelFont)
        		|| !other.axisTickLabelFont.equals(axisTickLabelFont)
                || other.showLegend != showLegend
                || other.showRangeAxis != showRangeAxis
                || other.showDomainAxis != showDomainAxis
                || other.seriesSpacing != seriesSpacing
                || other.startDate != startDate
                || other.endDate != endDate
                );

        return equals;
    }
}