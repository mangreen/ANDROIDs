package com.iiiesti.walkmap.customType;

public class MT_SimpleStop {
	
	/* Members */
	public int stopId;
	public int latE6;
	public int lonE6;
	public int type;
	public int countyId;
	public int townId;
	public String name;
	
	public MT_SimpleStop(int transitStopId, String transitStopName, int latitudeE6, int longitudeE6, int transitType, int countyRegion, int townRegion) {
		stopId = transitStopId;
		name = transitStopName;
		latE6 = latitudeE6;
		lonE6 = longitudeE6;
		type = transitType;
		countyId = countyRegion;
		townId = townRegion;
	}
}
