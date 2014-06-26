package com.iiiesti.pedestrian.pmap.direction;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.iiiesti.pedestrian.pmap.R;
import com.iiiesti.pedestrian.pmap.customType.Direction_Constant;
import com.iiiesti.pedestrian.pmap.customType.Direction_RoutePoint;


public class DirectionJSON {
	
	private static final String D_TAG = "DirectionJSON";
	private static final String NULL = "NULL";
	
	public static final String SUCCESS = "OK";
	
	private static final String NOT_FOUND = "NOT_FOUND";
	private static final String ZERO_RESULTS = "ZERO_RESULTS";
	private static final String MAX_WAYPOINTS_EXCEEDED = "MAX_WAYPOINTS_EXCEEDED";
	private static final String INVALID_REQUEST = "INVALID_REQUEST";
	private static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	private static final String REQUEST_DENIED = "REQUEST_DENIED";
	private static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
	
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
                lineList.add(new Direction_RoutePoint(new GeoPoint(lat * 10, lng * 10), 3, travalMode));
        }
		return lineList;
	}
	
	public static List<Direction_RoutePoint> polylineExtractor(Steps[] steps)
	{
		if(steps.length > 0)
		{
			List<Direction_RoutePoint> totalLine = new ArrayList<Direction_RoutePoint>();
			
			for(int i = 0; i < steps.length - 1; i++)
			{
				totalLine.addAll(polylineDecoder(steps[i].polyline.points, Direction_Constant.travelModeTranslator(steps[i].travel_mode)));
				totalLine.remove(totalLine.size()-1);
			}
			totalLine.addAll(polylineDecoder(steps[steps.length - 1].polyline.points, Direction_Constant.travelModeTranslator(steps[steps.length - 1].travel_mode)));
			
			return totalLine;
		}
		else
			return null;
	}
	
	/* Direction basic data type */
	public static class Location
	{
		public double lat;
		public double lng;
	}
	private static final String LAT = "lat";
	private static final String LNG = "lng";
	private static Location parseLocation(JSONObject jLocation)
	{
		Log.d(D_TAG, "parseLocation");
		Location location = new Location();
		location.lat = jLocation.optDouble(LAT, 91.0);
		location.lng = jLocation.optDouble(LNG, 181.0);
		return location;
	}
	
	public static class Polyline
	{
		public String points;
		public String levels;
	}
	private static final String POINTS = "points";
	private static final String LEVELS = "levels";
	private static Polyline parsePolyline(JSONObject jPolyline)
	{
		Log.d(D_TAG, "parsePolyline");
		Polyline polyline = new Polyline();
		polyline.points = jPolyline.optString(POINTS);
		polyline.levels = jPolyline.optString(LEVELS);
		return polyline;
	}
	
	public static class ValueText
	{
		public int value;
		public String text;
	}
	private static final String VALUE = "value";
	private static final String TEXT = "text";
	private static ValueText parseValueText(JSONObject jValueText)
	{
		Log.d(D_TAG, "parseValueText");
		ValueText valueText = new ValueText();
		valueText.value = jValueText.optInt(VALUE, -1);
		valueText.text = jValueText.optString(TEXT);
		return valueText;
	}
	
	/* Direction advanced data type */
	public static class Steps
	{
		public String travel_mode;
		public Location start_location;
		public Location end_location;
		public Polyline polyline;
		public ValueText duration;
		public ValueText distance;
		public String html_instructions;
	}
	private static final String TRAVEL_MODE = "travel_mode";
	private static final String START_LOCATION = "start_location";
	private static final String END_LOCATION = "end_location";
	private static final String POLYLINE = "polyline";
	private static final String DURATION = "duration";
	private static final String DISTANCE = "distance";
	private static final String HTML_INS = "html_instructions";
	private static Steps parseSteps(JSONObject jSteps) throws JSONException
	{
		Log.d(D_TAG, "parseSteps");
		Steps steps = new Steps();
		steps.travel_mode = jSteps.optString(TRAVEL_MODE);
		steps.start_location = parseLocation(jSteps.getJSONObject(START_LOCATION));
		steps.end_location = parseLocation(jSteps.getJSONObject(END_LOCATION));
		steps.polyline = parsePolyline(jSteps.getJSONObject(POLYLINE));
		steps.duration = parseValueText(jSteps.getJSONObject(DURATION));
		steps.distance = parseValueText(jSteps.getJSONObject(DISTANCE));
		steps.html_instructions = jSteps.optString(HTML_INS);
		return steps;
	}
	private static Steps[] parseStepsArray(JSONArray jStepsArray) throws JSONException
	{
		Log.d(D_TAG, "parseStepsArray");
		Steps[] stepsArray = new Steps[jStepsArray.length()];
		for(int i = 0; i < stepsArray.length; i++)
			stepsArray[i] = parseSteps(jStepsArray.getJSONObject(i));
		return stepsArray;
	}
	
	public static class Legs
	{
		public Steps[] steps;
		public ValueText duration;
		public ValueText distance;
		public Location start_location;
		public Location end_location;
		public String start_address;
		public String end_address;
		
		// Translated data
		public List<Direction_RoutePoint> routeLine;
	}
	private static final String STEPS = "steps";
	private static final String START_ADDR = "start_address";
	private static final String END_ADDR = "end_address";
	private static Legs parseLegs(JSONObject jLegs) throws JSONException
	{
		Log.d(D_TAG, "parseLegs");
		Legs legs = new Legs();
		JSONArray jSteps = jLegs.getJSONArray(STEPS);
		if(jSteps.length() > 0)	// [JUST IN CASE] Make sure that there are steps within this leg
		{
			legs.steps = parseStepsArray(jSteps);
			legs.duration = parseValueText(jLegs.getJSONObject(DURATION));
			legs.distance = parseValueText(jLegs.getJSONObject(DISTANCE));
			legs.start_location = parseLocation(jLegs.getJSONObject(START_LOCATION));
			legs.end_location = parseLocation(jLegs.getJSONObject(END_LOCATION));
			legs.start_address = jLegs.optString(START_ADDR);
			legs.end_address = jLegs.optString(END_ADDR);
			
			// Translating data
			legs.routeLine = polylineExtractor(legs.steps);
		}
		else
		{
			legs.steps = null;
			legs.duration = null;
			legs.distance = null;
			legs.start_location = null;
			legs.end_location = null;
			legs.start_address = null;
			legs.end_address = null;
		}
		return legs;
	}
	private static Legs[] parseLegsArray(JSONArray jLegsArray) throws JSONException
	{
		Log.d(D_TAG, "parseLegsArray");
		Legs[] legsArray = new Legs[jLegsArray.length()];
		for(int i = 0; i < legsArray.length; i++)
			legsArray[i] = parseLegs(jLegsArray.getJSONObject(i));
		return legsArray;
	}

	public static class Routes
	{
		public String summary;
		public Legs[] legs;
		public String copyrights;
		public Polyline overview_polyline;
	}
	private static final String SUMMARY = "summary";
	private static final String LEGS = "legs";
	private static final String COPYRIGHTS = "copyrights";
	private static final String OVERVIEW_POLYLINE = "overview_polyline";
	private static Routes parseRoutes(JSONObject jRoutes) throws JSONException
	{
		Log.d(D_TAG, "parseRoutes");
		Routes routes = new Routes();
		JSONArray jLegs = jRoutes.getJSONArray(LEGS);
		if(jLegs.length() > 0)	// [JUST IN CASE] Make sure that there are legs within this route
		{
			routes.summary = jRoutes.optString(SUMMARY);
			routes.legs = parseLegsArray(jLegs);
			routes.copyrights = jRoutes.optString(COPYRIGHTS);
			routes.overview_polyline = parsePolyline(jRoutes.getJSONObject(OVERVIEW_POLYLINE));
		}
		else
		{
			routes.summary = null;
			routes.legs = null;
			routes.copyrights = null;
			routes.overview_polyline = null;
		}

		return routes;
	}
	private static Routes[] parseRoutesArray(JSONArray jRoutesArray) throws JSONException
	{
		Log.d(D_TAG, "parseRoutesArray");
		Routes[] routesArray = new Routes[jRoutesArray.length()];
		for(int i = 0; i < routesArray.length; i++)
			routesArray[i] = parseRoutes(jRoutesArray.getJSONObject(i));
		return routesArray;
	}
	
	private static final String STATUS = "status";
	private static final String ROUTES = "routes";
	
	/** Direction data definition */
	/* Level - 1 */
	public String status;
	public Routes[] routes;
	
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
				routes = parseRoutesArray(jRoutes);
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
