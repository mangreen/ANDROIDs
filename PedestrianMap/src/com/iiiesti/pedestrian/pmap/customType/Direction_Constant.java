package com.iiiesti.pedestrian.pmap.customType;

import com.iiiesti.pedestrian.pmap.R;

public class Direction_Constant {
	/* Simple mapping */
	public static int travelModeTranslator(String travelMode)
	{
		if(travelMode.equals(R.string.dtmode_virtual))
			return TRAVEL_MODE_VIRTUAL;
		else if(travelMode.equals(R.string.dtmode_indoor))
			return TRAVEL_MODE_INDOOR;
		else if(travelMode.equals(R.string.dtmode_masstransit))
			return TRAVEL_MODE_MT;
		else if(travelMode.equals(R.string.dtmode_masstransit_sub))
			return TRAVEL_MODE_MTSUB;
		else
			return TRAVEL_MODE_WALKING;
	}
	
	/* Constants */
	
	/** Direction type */	
	public static final int TRAVEL_MODE_WALKING = 0;
	public static final int TRAVEL_MODE_VIRTUAL = 1;
	public static final int TRAVEL_MODE_INDOOR = 2;
	public static final int TRAVEL_MODE_MT = 3;
	public static final int TRAVEL_MODE_MTSUB = 4;
	public static final int TRAVEL_MODE_TOTAL = 5;
}
