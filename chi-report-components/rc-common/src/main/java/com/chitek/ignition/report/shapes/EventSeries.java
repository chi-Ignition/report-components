package com.chitek.ignition.report.shapes;

import java.awt.Color;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.Series;

public class EventSeries extends Series implements DomainInfo {
	private static final long serialVersionUID = 7860937944288672451L;
	
	private List<Event> data;
	private Event start;
	private Event end;

	/**
	 * Constructs a new series with the specified name.
	 * 
	 * @param name  the series name (<code>null</code> not permitted).
	 */
	public EventSeries(String name) {
		super(name);
		data = new ArrayList<Event>();
		start = new Event(Long.MAX_VALUE, Long.MIN_VALUE, 0, Color.BLACK);
		end = new Event(Long.MIN_VALUE, Long.MIN_VALUE, 0, Color.BLACK);
	}

	/**
	 * Adds an event to the series and sends a
	 * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param event
	 * 	The StatusInterval to add to the series
	*/
	public void addEvent(Event event) {
		this.data.add(event);
		if (event.getStart() < start.getStart()) {
			start = event;
		}
		if (event.getEnd() > end.getEnd()) {
			end = event;
		}
		fireSeriesChanged();
	}
	
	/**
	 * Adds an event to the series and sends a
	 * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param start
	 *            The start time
	 * @param end
	 *            The end time
	 * @param id
	 *            The event id
	 * @param color
	 *            The color for the bar.
	 */
	public void addEvent(long start, long end, Object id, Color color) {
		addEvent(new Event(start, end, id, color));
	}
	
	/**
	 * Adds an event to the series and sends a
	 * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param start
	 * 	The start time
	 * @param end
	 *  The end time
	 * @param id
	 *            The event id
	 * @param color
	 * 	The color for the bar.
	 */
	public void addEvent(Date start, Date end, Object id, Color color) {
		addEvent(start.getTime(), end.getTime(), id, color);
	}
		
	
	/**
	 * Returns the number of items in the series.
	 * 
	 * @return
	 * 	The item count.
	 */
	@Override
	public int getItemCount() {
		return data.size();
	}

	/**
	 * Return an interval from the series.
	 * 
	 * @param index	The interval index (zero-based).
	 * @return The interval.
	 */
	public Event get(int index) {
		return data.get(index);
	}
	
	/**
	 * Returns an unmodifiable list of the intervals in this series.
	 * 
	 * @return
	 * 	The intervals.
	 */
	public List<Event> getEvents() {
		return Collections.unmodifiableList(data);
	}
	
	@Override
	public Range getDomainBounds(boolean includeInterval) {
		return new Range(start.getStart(), end.getEnd());
	}

	@Override
	public double getDomainLowerBound(boolean includeInterval) {
		return getDomainBounds(includeInterval).getLowerBound();
	}

	@Override
	public double getDomainUpperBound(boolean includeInterval) {
		return getDomainBounds(includeInterval).getUpperBound();
	}
	
	
}
