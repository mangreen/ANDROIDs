package com.iiiesti.walkmap.direction;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customType.Direction_Constant;
import com.iiiesti.walkmap.customType.Direction_RoutePoint;


public class DirectionJSON {
	
	private static final String D_TAG = "DirectionJSON";
	private static final int IGNORE_GOOGLE_LEVEL_DEFAULT = 0;
	private static final String NULL = "NULL";
	private static final String PREFIX_MRT = "mrt";
	
	public static final String SUCCESS = "OK";
	
	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String ZERO_RESULTS = "ZERO_RESULTS";
	public static final String MAX_WAYPOINTS_EXCEEDED = "MAX_WAYPOINTS_EXCEEDED";
	public static final String INVALID_REQUEST = "INVALID_REQUEST";
	public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	public static final String REQUEST_DENIED = "REQUEST_DENIED";
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
	
	private Context mContext;
	
	/* Direction translated data type */
	public static List<Direction_RoutePoint> polylineDecoder(String encodedPolyline, int travalMode)
	{
        List<Direction_RoutePoint> lineList;
        int len = encodedPolyline.length();
        int index = 0;
        int lat = 0;
        int lng = 0;
        
        if(len > 0)
        	lineList = new ArrayList<Direction_RoutePoint>();
    	else
    		return null;

        // Decode polyline according to Google's polyline decoder utility.
        while (index < len) {
                int b;
                int shift = 0;
                int result = 0;
                do {
                        b = encodedPolyline.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                } while (b >= 0x20);
                int dlat = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                        b = encodedPolyline.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                } while (b >= 0x20);
                int dlng = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
                lng += dlng;
                
                // Convert the lat and lng to microdegrees.
                lineList.add(new Direction_RoutePoint(new GeoPoint(lat * 10, lng * 10), IGNORE_GOOGLE_LEVEL_DEFAULT, travalMode));	// [NOTE] The level of google polyline is ignored here
        }
		return lineList;
	}
	
	public List<Direction_RoutePoint> routelineExtractor(List<Steps> steps)
	{
		int tTravelMode;
		Steps tStep;
		SubSteps tSubStep;
		
		Log.d(D_TAG, "routelineExtractor: extracting");
		
		if(steps.size() > 0)
		{
			List<Direction_RoutePoint> totalLine = new ArrayList<Direction_RoutePoint>();
			
			for(int i = 0; i < steps.size(); i++)	// For each step
			{
				tStep = steps.get(i);														// Make a reference
				tTravelMode = Direction_Constant.travelModeTranslator(tStep.travel_mode);	// Get the travel mode
				
				// Add the route point
				totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.start_location.latE6, tStep.start_location.lngE6),
						IGNORE_GOOGLE_LEVEL_DEFAULT, tTravelMode));
				
				switch(tTravelMode)
				{
				case Direction_Constant.TRAVEL_MODE_WALKING:	// Google WALKING type, need to parse the polyline
					if(tStep.polyline != null)
						totalLine.addAll(polylineDecoder(tStep.polyline.points, tTravelMode));
					break;
				case Direction_Constant.TRAVEL_MODE_MT:			// III MT type, parse the sub_step
					if(tStep.sub_steps != null && tStep.sub_steps.size() > 0)
						for(int j = 0; j < tStep.sub_steps.size(); j++)
						{
							tSubStep = tStep.sub_steps.get(j);
							totalLine.add(new Direction_RoutePoint(new GeoPoint(tSubStep.start_location.latE6, tSubStep.start_location.lngE6),
									IGNORE_GOOGLE_LEVEL_DEFAULT, tTravelMode));
						}
					break;
				case Direction_Constant.TRAVEL_MODE_VIRTUAL:
					
					boolean patchMrtFloor;
					
					// [PATCH]
					if(tStep.building_id.startsWith(PREFIX_MRT))
						patchMrtFloor = true;
					else
						patchMrtFloor = false;
					
					if(i == 0)
						tStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_at_start_step);
					else if(i == steps.size() - 1)
						tStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_at_end_step);
					
					if(tStep.building_id != null && tStep.sub_steps != null && tStep.sub_steps.size() > 0)	// Contains valid indoor information
					{
						List<SubSteps> tmpSubSteps = new ArrayList<DirectionJSON.SubSteps>();
						SubSteps tmpSubStep = null;
						SubSteps curSubStep = null;
						int curSubStepZID;
						int lastZID;
						int curZID;
						int nextZID;
						
						if(patchMrtFloor)
						{
							if(tStep.start_location.z_index == 1)
								tStep.start_location.z_index = -1;
							if(tStep.end_location.z_index == 1)
								tStep.end_location.z_index = -1;
							for(int j = 0; j < tStep.sub_steps.size(); j++)
							{
								curSubStep = tStep.sub_steps.get(j);
								if(curSubStep.start_location.z_index == 1)
									curSubStep.start_location.z_index = -1;
								if(curSubStep.end_location.z_index == 1)
									curSubStep.end_location.z_index = -1;
							}
							curSubStep = null;
						}
						
						totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.start_location.latE6, tStep.start_location.lngE6),
								tStep.start_location.z_index, tTravelMode));
						totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.end_location.latE6, tStep.end_location.lngE6),
								tStep.end_location.z_index, tTravelMode));
						
						/* Recompile the indoor subStep data */
						
						// Create the first subStep
						tmpSubStep = new SubSteps();
						tmpSubStep.routeLine = new ArrayList<Direction_RoutePoint>();
						tmpSubStep.start_location = tStep.start_location;
						tmpSubStep.start_location.z_index = tStep.sub_steps.get(0).start_location.z_index;	// [PATCH]
						
						if(tmpSubStep.start_location.z_index > 0)
							tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_positive_at);
						else
							tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_negative_at);
						tmpSubStep.html_instructions += Math.abs(tmpSubStep.start_location.z_index) + 
							mContext.getResources().getString(R.string.hint_on_indoor_floor) + 
							mContext.getResources().getString(R.string.hint_on_indoor_guide);
						curSubStepZID = lastZID = tmpSubStep.start_location.z_index;
						
						// Create the first routeLinePoint
						tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(tmpSubStep.start_location.latE6, tmpSubStep.start_location.lngE6), tmpSubStep.start_location.z_index, tTravelMode));
						
						// Parse the raw subSteps
						for(int j = 0; j < tStep.sub_steps.size(); j++)
						{
							curSubStep = tStep.sub_steps.get(j);
							curZID = curSubStep.start_location.z_index;
							nextZID = curSubStep.end_location.z_index;
							
							if(curZID != lastZID && curZID != nextZID)
							{
								lastZID = curZID;
								continue;
							}
							else 
							{
								if(tmpSubStep == null)
								{
									tmpSubStep = new SubSteps();
									tmpSubStep.routeLine = new ArrayList<Direction_RoutePoint>();
									tmpSubStep.start_location = curSubStep.start_location;
									if(tmpSubStep.start_location.z_index > 0)
										tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_positive_at);
									else
										tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_negative_at);
									tmpSubStep.html_instructions += Math.abs(tmpSubStep.start_location.z_index) + 
										mContext.getResources().getString(R.string.hint_on_indoor_floor) + 
										mContext.getResources().getString(R.string.hint_on_indoor_guide);
									curSubStepZID = tmpSubStep.start_location.z_index;
								}
								
								tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(curSubStep.start_location.latE6, curSubStep.start_location.lngE6), curSubStep.start_location.z_index, tTravelMode));
								if(j + 1 == tStep.sub_steps.size() && curSubStep.end_location.z_index == curSubStep.start_location.z_index)
									tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(curSubStep.end_location.latE6, curSubStep.end_location.lngE6), curSubStep.end_location.z_index, tTravelMode));
								
								if(nextZID != curSubStepZID)
								{
									tmpSubStep.end_location = curSubStep.start_location;
									tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(tmpSubStep.end_location.latE6, tmpSubStep.end_location.lngE6), tmpSubStep.end_location.z_index, tTravelMode));
									tmpSubSteps.add(tmpSubStep);
									tmpSubStep = null;
								}
								lastZID = curZID;
							}
						}
						if(tmpSubStep == null)	// [PATCH]
						{
							tmpSubStep = new SubSteps();
							tmpSubStep.routeLine = new ArrayList<Direction_RoutePoint>();
							tmpSubStep.start_location = curSubStep.end_location;
							if(tmpSubStep.start_location.z_index > 0)
								tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_positive_at);
							else
								tmpSubStep.html_instructions = mContext.getResources().getString(R.string.hint_on_indoor_negative_at);
							tmpSubStep.html_instructions += Math.abs(tmpSubStep.start_location.z_index) + 
								mContext.getResources().getString(R.string.hint_on_indoor_floor) + 
								mContext.getResources().getString(R.string.hint_on_indoor_guide);
							tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(curSubStep.end_location.latE6, curSubStep.end_location.lngE6), curSubStep.end_location.z_index, tTravelMode));
						}
						
						tmpSubStep.end_location = tStep.end_location;
						tmpSubStep.end_location.z_index = curSubStep.end_location.z_index;
						tmpSubStep.routeLine.add(new Direction_RoutePoint(new GeoPoint(tmpSubStep.end_location.latE6, tmpSubStep.end_location.lngE6), tmpSubStep.end_location.z_index, tTravelMode));
						tmpSubSteps.add(tmpSubStep);
						
						// Restore the compiled data
						tStep.sub_steps.clear();
						tStep.sub_steps = tmpSubSteps;
					}
					else
					{
						totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.start_location.latE6, tStep.start_location.lngE6),
								IGNORE_GOOGLE_LEVEL_DEFAULT, tTravelMode));
						totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.end_location.latE6, tStep.end_location.lngE6),
								IGNORE_GOOGLE_LEVEL_DEFAULT, tTravelMode));
					}
					break;
				}
			}
			
			// Add the last point to complete the route line
			tStep = steps.get(steps.size() - 1);
			tTravelMode = Direction_Constant.travelModeTranslator(tStep.travel_mode);
			totalLine.add(new Direction_RoutePoint(new GeoPoint(tStep.end_location.latE6, tStep.end_location.lngE6),
					IGNORE_GOOGLE_LEVEL_DEFAULT, tTravelMode));
			
			return totalLine;
		}
		else
			return null;
	}
	
	/* Direction basic data type */
	public class Location
	{
		public int latE6;
		public int lngE6;
		public int z_index;
		public String attr;
	}
	private static final String LAT = "lat";
	private static final String LNG = "lng";
	private static final String ZIDX = "z_index";
	private static final String ATTR = "attr";
	private Location parseLocation(JSONObject jLocation)
	{
		if(jLocation != null)
		{
			Log.d(D_TAG, "parseLocation");
			Location location = new Location();

			location.latE6 = jLocation.optInt(LAT, 91000000);
			location.lngE6 = jLocation.optInt(LNG, 181000000);
			location.z_index = jLocation.optInt(ZIDX);
			location.attr = jLocation.optString(ATTR);
			
			return location;
		}
		else
		{
			Log.d(D_TAG, "parseLocation: NULL");
			return null;
		}
	}
	
	public class Polyline
	{
		public String points;
		public String levels;
	}
	private static final String POINTS = "points";
	private static final String LEVELS = "levels";
	private Polyline parsePolyline(JSONObject jPolyline)
	{
		if(jPolyline != null)
		{
			Log.d(D_TAG, "parsePolyline");
			Polyline polyline = new Polyline();
			polyline.points = jPolyline.optString(POINTS);
			polyline.levels = jPolyline.optString(LEVELS);
			return polyline;
		}
		else
		{
			Log.d(D_TAG, "parsePolyline: NULL");
			return null;
		}
	}
	
	public class ValueText
	{
		public int value;
		public String text;
	}
	private static final String VALUE = "value";
	private static final String TEXT = "text";
	private ValueText parseValueText(JSONObject jValueText)
	{
		if(jValueText != null)
		{
			Log.d(D_TAG, "parseValueText");
			ValueText valueText = new ValueText();
			valueText.value = jValueText.optInt(VALUE, -1);
			valueText.text = jValueText.optString(TEXT);
			return valueText;
		}
		else
		{
			Log.d(D_TAG, "parseValueText: NULL");
			return null;
		}
	}
	
	/* Direction advanced data type */
	public class SubSteps
	{
		public Location start_location;
		public Location end_location;
		public Polyline polyline;
		public ValueText duration;
		public ValueText distance;
		public String html_instructions;
		
		// Translated data
		public List<Direction_RoutePoint> routeLine;	// Step-level route shape (for indoor)
		
		public SubSteps() {
			start_location = null;
			end_location = null;
			polyline = null;
			duration = null;
			distance = null;
			html_instructions = null;
			
			// Translated data
			routeLine = null;	// Step-level route shape (for indoor)
		}
	}
	private static final String START_LOCATION = "start_location";
	private static final String END_LOCATION = "end_location";
	private static final String POLYLINE = "polyline";
	private static final String DURATION = "duration";
	private static final String DISTANCE = "distance";
	private static final String HTML_INS = "html_instructions";
	private SubSteps parseSubSteps(JSONObject jSubSteps)
	{
		if(jSubSteps != null)
		{
			Log.d(D_TAG, "parseSubSteps");
			SubSteps subSteps = new SubSteps();
			subSteps.start_location = parseLocation(jSubSteps.optJSONObject(START_LOCATION));
			subSteps.end_location = parseLocation(jSubSteps.optJSONObject(END_LOCATION));
			subSteps.polyline = parsePolyline(jSubSteps.optJSONObject(POLYLINE));
			subSteps.duration = parseValueText(jSubSteps.optJSONObject(DURATION));
			subSteps.distance = parseValueText(jSubSteps.optJSONObject(DISTANCE));
			subSteps.html_instructions = jSubSteps.optString(HTML_INS);
			subSteps.routeLine = null;	// This might be filled when routelineExtractor() is executed
			return subSteps;
		}
		else
		{
			Log.d(D_TAG, "parseSubSteps: NULL");
			return null;
		}
	}
	private List<SubSteps> parseSubStepsList(JSONArray jSubStepsArray)
	{
		// [NOTE] You should check the ability of JSONArray before invoking "parseSomethingArray(JSONArray)"
		Log.d(D_TAG, "parseSubStepsArray");
		List<SubSteps> subStepsArray = new ArrayList<SubSteps>();
		for(int i = 0; i < jSubStepsArray.length(); i++)
			subStepsArray.add(parseSubSteps(jSubStepsArray.optJSONObject(i)));
		return subStepsArray;
	}
	
	public class Steps extends SubSteps
	{
		// [NOTE] The other members is inherited from SubSteps
		public String travel_mode;
		public String building_id;
		public int stop_to_id;
		public int stop_from_id;
		public List<SubSteps> sub_steps;
		
		public Steps() {
			travel_mode = null;
			building_id = null;
			stop_to_id = 0;
			stop_from_id = 0;
			sub_steps = null;
		}
	}
	private static final String TRAVEL_MODE = "travel_mode";
	private static final String BIDX = "building_id";
	private static final String S_T_IDX = "stop_to_id";
	private static final String S_F_IDX = "stop_from_id";
	private static final String SUB_STEPS = "sub_steps";
	private Steps parseSteps(JSONObject jSteps)
	{
		if(jSteps != null)
		{
			Log.d(D_TAG, "parseSteps");
			Steps steps = new Steps();
			steps.travel_mode = jSteps.optString(TRAVEL_MODE);
			steps.building_id = jSteps.optString(BIDX);
			steps.stop_to_id = jSteps.optInt(S_T_IDX, 0);
			steps.stop_from_id = jSteps.optInt(S_F_IDX, 0);
			steps.start_location = parseLocation(jSteps.optJSONObject(START_LOCATION));
			steps.end_location = parseLocation(jSteps.optJSONObject(END_LOCATION));
			steps.polyline = parsePolyline(jSteps.optJSONObject(POLYLINE));
			steps.duration = parseValueText(jSteps.optJSONObject(DURATION));
			steps.distance = parseValueText(jSteps.optJSONObject(DISTANCE));
			steps.html_instructions = jSteps.optString(HTML_INS);
				
			JSONArray jSubSteps = jSteps.optJSONArray(SUB_STEPS);
			if(jSubSteps != null && jSubSteps.length() > 0)
				steps.sub_steps = parseSubStepsList(jSubSteps);
			else
				steps.sub_steps = null;
			return steps;
		}
		else
		{
			Log.d(D_TAG, "parseSteps: NULL");
			return null;
		}
	}
	private List<Steps> parseStepsList(JSONArray jStepsArray)
	{
		// [NOTE] You should check the ability of JSONArray before invoking "parseSomethingArray(JSONArray)"
		Log.d(D_TAG, "parseStepsArray");
		List<Steps> stepsArray = new ArrayList<Steps>();
		for(int i = 0; i < jStepsArray.length(); i++)
			stepsArray.add(parseSteps(jStepsArray.optJSONObject(i)));
		return stepsArray;
	}
	
	public class Legs
	{
		public List<Steps> steps;
		public ValueText duration;
		public ValueText distance;
		public Location start_location;
		public Location end_location;
		public String start_address;
		public String end_address;
		
		public Legs() {
			steps = null;
			duration = null;
			distance = null;
			start_location = null;
			end_location = null;
			start_address = null;
			end_address = null;
		}
	}
	private static final String STEPS = "steps";
	private static final String START_ADDR = "start_address";
	private static final String END_ADDR = "end_address";
	private Legs parseLegs(JSONObject jLegs)
	{
		if(jLegs != null)
		{
			JSONArray jSteps = jLegs.optJSONArray(STEPS);	// [WARRING] No "steps", no "legs"
			if(jSteps != null && jSteps.length() > 0)
			{
				Log.d(D_TAG, "parseLegs");
				Legs legs = new Legs();
				legs.steps = parseStepsList(jSteps);
				legs.duration = parseValueText(jLegs.optJSONObject(DURATION));
				legs.distance = parseValueText(jLegs.optJSONObject(DISTANCE));
				legs.start_location = parseLocation(jLegs.optJSONObject(START_LOCATION));
				legs.end_location = parseLocation(jLegs.optJSONObject(END_LOCATION));
				legs.start_address = jLegs.optString(START_ADDR);
				legs.end_address = jLegs.optString(END_ADDR);
				
				return legs;
			}
			else
			{
				Log.d(D_TAG, "parseLegs: NULL (\"steps\" is NULL)");
				return null;
			}
		}
		else
		{
			Log.d(D_TAG, "parseLegs: NULL");
			return null;
		}
	}
	private List<Legs> parseLegsList(JSONArray jLegsArray)
	{
		// [NOTE] You should check the ability of JSONArray before invoking "parseSomethingArray(JSONArray)"
		Log.d(D_TAG, "parseLegsArray");
		List<Legs> legsArray = new ArrayList<Legs>();
		for(int i = 0; i < jLegsArray.length(); i++)
			legsArray.add(parseLegs(jLegsArray.optJSONObject(i)));
		return legsArray;
	}

	public static class Routes
	{
		public String summary;
		public List<Legs> legs;
		public String copyrights;
		public Polyline overview_polyline;
		
		// Translated data
		public List<Direction_RoutePoint> routeLine;	// Step-level route shape (for outdoor)
		
		public Routes() {
			summary = null;
			legs = null;
			copyrights = null;
			overview_polyline = null;
			routeLine = null;
		}
	}
	private static final String SUMMARY = "summary";
	private static final String LEGS = "legs";
	private static final String COPYRIGHTS = "copyrights";
	private static final String OVERVIEW_POLYLINE = "overview_polyline";
	private Routes parseRoutes(JSONObject jRoutes)
	{
		if(jRoutes != null)
		{
			JSONArray jLegs = jRoutes.optJSONArray(LEGS);	// [WARRING] No "legs", no "routes"
			if(jLegs != null && jLegs.length() > 0)
			{
				Log.d(D_TAG, "parseRoutes");
				Routes routes = new Routes();
				routes.summary = jRoutes.optString(SUMMARY);
				routes.legs = parseLegsList(jLegs);
				routes.copyrights = jRoutes.optString(COPYRIGHTS);
				routes.overview_polyline = parsePolyline(jRoutes.optJSONObject(OVERVIEW_POLYLINE));
				
				// Translating outdoor routeLine data
				if(routes.legs.size() == 1)
					routes.routeLine = routelineExtractor(routes.legs.get(0).steps);
				
				return routes;
			}
			else
			{
				Log.d(D_TAG, "parseRoutes: NULL (\"legs\" is NULL)");
				return null;
			}
		}
		else
		{
			Log.d(D_TAG, "parseRoutes: NULL");
			return null;
		}
	}
	private List<Routes> parseRoutesList(JSONArray jRoutesArray)
	{
		// [NOTE] You should check the ability of JSONArray before invoking "parseSomethingArray(JSONArray)"
		Log.d(D_TAG, "parseRoutesArray");
		List<Routes> routesArray = new ArrayList<Routes>();
		for(int i = 0; i < jRoutesArray.length(); i++)
			routesArray.add(parseRoutes(jRoutesArray.optJSONObject(i)));
		return routesArray;
	}
	
	private static final String STATUS = "status";
	private static final String ROUTES = "routes";
	
	/** Direction data definition */
	/* Level - 1 */
	public String status;
	public List<Routes> routes;
	
	private void dataInitialize()
	{
		status = NULL;
		routes = null;
	}
	
	private void dataParse(String content) throws Exception
	{
		Log.v(D_TAG, "DirectionJSON: dataParse");
		
		JSONObject jObj = new JSONObject(content);					// Get the whole package
		if((status = jObj.optString(STATUS, NULL)).equals(SUCCESS))	// Get "status" first, parse the following data only when status is "OK"
		{
			JSONArray jRoutes;
			if((jRoutes = jObj.getJSONArray(ROUTES)) != null && jRoutes.length() > 0)	// Get "routes" array
				routes = parseRoutesList(jRoutes);
			else
				status = NULL;
		}
	}
	
	public DirectionJSON(Context context, String content) {
		mContext = context;
		dataInitialize();
		try
		{
			if(content != null)
				dataParse(content);
			else
				Log.e(D_TAG, "DirectionJSON: content is null");
		}
		catch(Exception e)
		{
			dataInitialize();	// [NOTE] Do dataClean() rather than dataInitialize()
			Log.e(D_TAG, e.toString());
		}
	}
	
	public String statusToString()
	{
		if(status.equals(NOT_FOUND))
			return mContext.getString(R.string.dstatus_not_found);
		else if(status.equals(ZERO_RESULTS))
			return mContext.getString(R.string.dstatus_zero_results);
		else if(status.equals(MAX_WAYPOINTS_EXCEEDED))
			return mContext.getString(R.string.dstatus_waypoints_exceeded);
		else if(status.equals(INVALID_REQUEST))
			return mContext.getString(R.string.dstatus_invalid_req);
		else if(status.equals(OVER_QUERY_LIMIT))
			return mContext.getString(R.string.dstatus_over_query);
		else if(status.equals(REQUEST_DENIED))
			return mContext.getString(R.string.dstatus_req_denied);
		else if(status.equals(UNKNOWN_ERROR))
			return mContext.getString(R.string.dstatus_unknown_err);
		else
			return mContext.getString(R.string.dstatus_null);
	}
}
