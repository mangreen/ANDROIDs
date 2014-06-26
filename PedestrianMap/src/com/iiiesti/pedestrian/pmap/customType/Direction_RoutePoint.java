package com.iiiesti.pedestrian.pmap.customType;

import com.google.android.maps.GeoPoint;

public class Direction_RoutePoint {
	
	/* Members */
	public GeoPoint geoPoint;
	public int level;
	public int mode;
	
	public Direction_RoutePoint(GeoPoint location, int displayLevel, int travelMode) {
		geoPoint = location;
		level = displayLevel;
		mode = travelMode;
	}
}
