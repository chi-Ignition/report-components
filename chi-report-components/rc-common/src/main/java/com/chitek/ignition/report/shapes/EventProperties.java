package com.chitek.ignition.report.shapes;

import java.awt.Color;

/**
 * This class stores the display properties for an event id. 
 *
 */
public class EventProperties {
	private String description;
	private Color color;
	private Object eventId;
	
	public EventProperties(Object eventId, String description, Color color) {
		this.eventId = eventId;
		this.description = description;
		this.color = color;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Object getIdKey() {
		return eventId;
	}
	
	@Override
	public String toString() {
		return String.format("EventProperties %s - %s - Color: %s", eventId.toString(), description, color.toString());
	}

}
