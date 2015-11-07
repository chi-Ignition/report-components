package com.chitek.ignition.report.shapes;

import java.awt.Color;

public class Event {
	private Object id;
	private long start;
	private long end;
	private Color color;
	
	public Event(long start, long end, Object id, Color color) {
		this.start = start;
		this.end = end;
		this.id = id;
		this.color = color;
	}
	
	/**
	 * Returns the start time.
	 * 
	 * @return
	 * 	The start time.
	 */
	public long getStart() {
		return start;
	}
	
	/**
	 * Returns the end time.
	 * 
	 * @return
	 * 	The start time.
	 */
	public long getEnd() {
		return end;
	}
	
	/**
	 * Set the end time for this event.
	 * 
	 * @param newEnd
	 * 	The new end time.
	 */
	public void setEnd(long newEnd) {
		this.end = newEnd;
	}
	
	/**
	 * Returns the color for this period.
	 * 
	 * @return
	 * 	The color to paint this period.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Returns the event id. The id is used to map display properties to an event.
	 * 
	 * @return
	 * 	The value of this period.
	 */
	public Object getId() {
		return id;
	}
	
	/**
	 * Returns an object that can be used as a key.
	 * 
	 * @return
	 * 	The key object.
	 */
	public Comparable<?> getKey() {
		return start;
	}
	
	public String toString() {
		return String.format("Event id %s (%s - %s) Color: %s", id, start, end, color);
	}
}
