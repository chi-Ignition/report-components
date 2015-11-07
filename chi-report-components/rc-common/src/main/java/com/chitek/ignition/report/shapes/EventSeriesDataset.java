package com.chitek.ignition.report.shapes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class EventSeriesDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, DomainInfo, RangeInfo {
	private static final long serialVersionUID = 2315297031857780291L;

	List<EventSeries> data;
	Map<Comparable<?>, Integer> keys;
	Map<Object, EventProperties> eventProps;
	
	public EventSeriesDataset() {
		this.data = new ArrayList<EventSeries>();
		this.keys = new HashMap<Comparable<?>, Integer>();
		this.eventProps = new LinkedHashMap<Object, EventProperties>();	// The LinkedHashMap keeps the items in the order in which they are inserted
	}
	
	/**
	 * Adds a series to the dataset and sends a
	 * {@link org.jfree.data.general.DatasetChangeEvent} to all registered
	 * listeners.
	 *
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 */
	public void add(EventSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null 'series' argument.");
		}
		keys.put(series.getKey(), data.size());
		data.add(series);
		series.addChangeListener(this);
		
		fireDatasetChanged();
	}

	/**
	 * EventProperties contains the display properties (like description, bar color, ...) for a given event.
	 * 
	 * @param eventId
	 * 	The eventId that the EventProperties belongs to
	 * @param properties
	 * 	The display properties for the eventId
	 */
	public void addEventProperties(Object eventId, EventProperties properties) {
		eventProps.put(eventId, properties);
	}
	
	@Override
	public int getSeriesCount() {
		return data.size();
	}
	
	/**
	 * Returns a series from the collection.
	 *
	 * @param series
	 *            the series index (zero-based).
	 *
	 * @return The series.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>series</code> is not in the range <code>0</code> to
	 *             <code>getSeriesCount() - 1</code>.
	 */
	public EventSeries getSeries(int series) {
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		return this.data.get(series);
	}
	
	/**
	 * Returns a series from the collection.
	 *
	 * @param key
	 *            the series key.
	 *
	 * @return The series or <code>null</code> if the key does not exist.
	 * 
	 */
	public EventSeries getSeries(Comparable<?> key) {
		try {
			int index = keys.get(key);
			return data.get(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns an event.
	 * 
	 * @param series
	 *            the series index (zero-based).
	 * @param event
	 *            the event index (zero-based).
	 * @return
	 * 	The event.
	 */
	public Event getEvent(int series, int event) {
		return ((EventSeries) getSeries(series)).get(event);
	}
	
	/**
	 * @param series  the series (zero-based index).
	 */
	@Override
	public Comparable<?> getSeriesKey(int series) {
		return getSeries(series).getKey();
	}
	
	/**
	 * Returns the number of events in the specified series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * 
	 * @return The event count.
	 */
	@Override
	public int getItemCount(int series) {
		return getSeries(series).getItemCount();
	}

	/**
	 * Returns the x-value for an item within a series.
	 *
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 *
	 * @return The x-value.
	 */
	public Number getX(int series, int item) {
		EventSeries s = this.data.get(series);
		Event event = s.get(item);
		return event.getStart();
	}

	@Override
	public Number getStartX(int series, int item) {
		return getX(series, item);
	}
	
	@Override
	public Number getEndX(int series, int item) {
		EventSeries s = this.data.get(series);
		Event event = s.get(item);
		return event.getEnd();
	}
	
	/**
	 * Returns the count of known event id's.
	 * 
	 * @return
	 * 	The count of event id's
	 */
	public int getEventIdCount() {
		return eventProps.size();
	}
	
	/**
	 * Returns the display properties for an event id. This properties are also used for the plots legend.
	 * 
	 * @param eventId
	 * 	The event id to return properties for.
	 * @return
	 * 	The EventProperties
	 */
	public EventProperties getEventProperties(Object eventId) {
		return eventProps.get(eventId);
	}
	
	public List<Object>getKnownEventIds() {
		return new ArrayList<Object>(eventProps.keySet());
	}
	
	/**
	 * Returns the y-value for an event. The returned value is the index of the series.
	 */
	@Override
	public Number getY(int series, int item) {
		return series;
	}

	@Override
	public Number getEndY(int series, int item) {
		return getY(series, item);
	}

	@Override
	public Number getStartY(int series, int item) {
		return getY(series, item);
	}

	@Override
	public Range getDomainBounds(boolean includeInterval) {
		
		if (data.isEmpty()) {
			return new Range(0, 1);
		}
		
		long start=Long.MAX_VALUE;
		long end=Long.MIN_VALUE;
		for (DomainInfo domainInfo : data) {
			start=Math.min(start, (long)domainInfo.getDomainLowerBound(true));
			end=Math.max(end, (long)domainInfo.getDomainUpperBound(true));
		}
		
		Range range = new Range(start, end);
		return range;
	}

	@Override
	public double getDomainLowerBound(boolean includeInterval) {
		return getDomainBounds(includeInterval).getLowerBound();
	}

	@Override
	public double getDomainUpperBound(boolean includeInterval) {
		return getDomainBounds(includeInterval).getUpperBound();
	}
	
	/**
	 * Return the bounds for the range axis. The range axis is used for separate
	 * event series, every series uses a range of series-0.5 to series+0.5.
	 */
	@Override
	public Range getRangeBounds(boolean includeInterval) {
		Range range = new Range(-0.5, getSeriesCount()-0.5);
		return range;
	}

	@Override
	public double getRangeLowerBound(boolean includeInterval) {
		return getRangeBounds(includeInterval).getLowerBound();
	}

	@Override
	public double getRangeUpperBound(boolean includeInterval) {
		return getRangeBounds(includeInterval).getUpperBound();
	}
	
}
