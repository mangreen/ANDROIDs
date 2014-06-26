package com.iiiesti.walkmap.direction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customType.MT_Constant;
import com.iiiesti.walkmap.direction.DirectionJSON.Routes;
import com.iiiesti.walkmap.direction.DirectionJSON.Steps;
import com.iiiesti.walkmap.direction.DirectionJSON.SubSteps;

public abstract class DirectionManager {
	// Constants
	private static final String SERVER_DOMAIN = "140.92.13.231";	// 10.0.2.2
//	private static final String SERVER_DOMAIN = "140.92.13.111";	// 10.0.2.2
	private static final String EMPTY_BUILDID = "";
	private static final int HTTP_TIMEOUT = 3000;
	private static final int HTTP_SO_TIMEOUT = 20000;
	private static final int DIRECTION_MODE_WALK = R.id.btn_direction_mode_walk;
	private static final int DIRECTION_MODE_MASS = R.id.btn_direction_mode_mt;
	private static final int POINFO_START_NOT_SET = 0;
	private static final int POINFO_START_CUR_LOCATION = 1;
	private static final int POINFO_START_CUSTOM = 2;
	private static final int POINFO_END_NOT_SET = 0;
	private static final int POINFO_END_CUR_LOCATION = 1;
	private static final int POINFO_END_CUSTOM = 2;
	
	public static final int SP_TYPE_START_POINT = 0;
	public static final int SP_TYPE_END_POINT = 1;
	
	private static final String PREFS_ISSTARTSET = "bool_isStartSet";
	private static final String PREFS_ISSTARTCUR = "bool_isStartCurrent";
	private static final String PREFS_STARTTITLE = "string_startTitle";
	private static final String PREFS_STARTINFO = "string_startInfo";
	private static final String PREFS_STARTPNT_LAT = "int_startPointLatE6";
	private static final String PREFS_STARTPNT_LON = "int_startPointLonE6";
	private static final String PREFS_STARTPNT_STR_BUILDID = "int_startPointBuildID";
	private static final String PREFS_STARTPNT_INT_FLOORID = "int_startPointFloorID";
	
	private static final String PREFS_ISENDSET = "bool_isEndSet";
	private static final String PREFS_ISENDCUR = "bool_isEndCurrent";
	private static final String PREFS_ENDTITLE = "string_endTitle";
	private static final String PREFS_ENDINFO = "string_endInfo";
	private static final String PREFS_ENDPNT_LAT = "int_endPointLatE6";
	private static final String PREFS_ENDPNT_LON = "int_endPointLonE6";
	private static final String PREFS_ENDPNT_STR_BUILDID = "int_endPointBuildID";
	private static final String PREFS_ENDPNT_INT_FLOORID = "int_endPointFloorID";
	
	private static final String PREFS_DIRECTIONMODE = "direction_mode";
	private static final String PREFS_IS_ENABLED = "isEnabled";
	
	private static final String PREFS_HAS_TA_RECORD = "hasTransitAvoidRecords";
	private static final String PREFS_TA_PREFIX = "transitAvoid_";
	
	private static final String PREFS_RESULT_IS_DISPLAY = "isDisplayingResults";
	private static final String PREFS_RESULT_SRC = "curSourceResult";
	private static final String PREFS_RESULT_IDX = "curResultIdx";
	private static final String PREFS_RESULT_STEP_IDX = "curStepIdx";
	private static final String PREFS_RESULT_SUBSTEP_IDX = "curSubStepIdx";
	private static final String PREFS_RESULT_IS_SUBSTEP = "inSubStep";
	
	private String debugTag;
	
	// Data of start location
	private boolean isStartSet;
	private boolean isStartCurrent;
	private String startTitle;
	private String startInfo;
	private GeoPoint startPoint;
	private String startBuildID;
	private int startFloorID;
	
	// Data of end location
	private boolean isEndSet;
	private boolean isEndCurrent;
	private String endTitle;
	private String endInfo;
	private GeoPoint endPoint;
	private String endBuildID;
	private int endFloorID;
	
	// Data of direction mode
	private int directionMode;
	
	// Data for results operation
	private int curResultIdx;
	private int curStepIdx;
	private int maxStepIdx;
	private int curSubStepIdx;
	private int maxSubStepIdx;
	private Routes curRoute;
	private List<Steps> curStepList;
	private Steps curStep;
	private SubSteps curSubStep;
	
	// Flags
	private boolean isEnabled;
	private boolean isDisplayingResults;
	private boolean inSubStep;
	
	// Resources
	private Context mContext;
	private View mParentView;
	private DirectionDisplay mDisplay;
	private RelativeLayout mSetupLayout;
	private RelativeLayout mStepLayout;
	private DirectionJSON jResult;
	private List<Map<String, Object>> mRoutesList;
	private List<Map<String, Object>> mStepsList;
	private String curSourceResult;
	private boolean transitAvoidStatus[]; 			// Maintains the last status of transit-avoidance
	
	// Setup buttons
	private Button btnFromInfo;
	private Button btnToInfo;
	private Button btnToFromPnt;
	private Button btnToToPnt;
	private Button btnGo;
	private RadioGroup btnGroup;
	
	// Result operation buttons
	private Button btnStepInfo;
	private Button btnLastStep;
	private Button btnNextStep;
	private Button btnStepsList;
	private CheckBox cboxStepSwitch;
	
	/** Initialization */
	
	private void findViews()
	{
		btnFromInfo = (Button)mSetupLayout.findViewById(R.id.btn_direction_from_pnt);
		btnToInfo = (Button)mSetupLayout.findViewById(R.id.btn_direction_to_pnt);
		btnToFromPnt = (Button)mSetupLayout.findViewById(R.id.btn_direction_to_f_pnt);
		btnToToPnt = (Button)mSetupLayout.findViewById(R.id.btn_direction_to_t_pnt);
		btnGo = (Button)mSetupLayout.findViewById(R.id.btn_direction_go);
		btnGroup = (RadioGroup)mSetupLayout.findViewById(R.id.btn_group_mode);
		
		// Connect the result operation buttons
		btnStepInfo = (Button)mStepLayout.findViewById(R.id.btn_direction_step_info);
		btnLastStep = (Button)mStepLayout.findViewById(R.id.btn_direction_step_last);
		btnNextStep = (Button)mStepLayout.findViewById(R.id.btn_direction_step_next);
		btnStepsList = (Button)mStepLayout.findViewById(R.id.btn_direction_step_list);
		cboxStepSwitch = (CheckBox)mStepLayout.findViewById(R.id.cbox_direction_step_switch);
	}
	
	private void setListener()
	{
		btnFromInfo.setOnClickListener(toFromPnt);
		btnToInfo.setOnClickListener(toToPnt);
		btnToFromPnt.setOnClickListener(fromInfo);
		btnToToPnt.setOnClickListener(toInfo);
		btnGo.setOnClickListener(startRoute);
		btnGroup.setOnCheckedChangeListener(modeChange);
		
		// Set the result operation listeners
		btnStepInfo.setOnClickListener(toStepPnt);
		btnLastStep.setOnClickListener(lastStep);
		btnNextStep.setOnClickListener(nextStep);
		btnStepsList.setOnClickListener(openStepsList);
		cboxStepSwitch.setOnClickListener(stepSwitch);
	}
	
	/*
	 * Constructor
	 */
	public DirectionManager(Context context, View parentView, DirectionDisplay display, int displayPosition) {
		debugTag = getDebugTag();
		mContext = context;
		mDisplay = display;
		mRoutesList = new ArrayList<Map<String, Object>>();
		mStepsList = new ArrayList<Map<String, Object>>();
		transitAvoidStatus = new boolean[context.getResources().getStringArray(R.array.transit_avoid_menu).length];
		
		// Get the main layout
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mParentView = parentView;
        mSetupLayout = (RelativeLayout)inflater.inflate(R.layout.direction_setup, null);
        mStepLayout = (RelativeLayout)inflater.inflate(R.layout.direction_step, null);
        
        ((ViewGroup)mParentView).addView(mDisplay, displayPosition, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        ((ViewGroup)mParentView).addView(mSetupLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        ((ViewGroup)mParentView).addView(mStepLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        mSetupLayout.setVisibility(View.GONE);
        mStepLayout.setVisibility(View.GONE);
        isEnabled = false;
        
        // Setup connections
        findViews();
        setListener();
        
        // Data initialization
        dataInitialize();
	}
	
	/*
	 * Data initialization
	 */
	public void dataInitialize()
	{
		isStartSet = false;
		isStartCurrent = false;
        startTitle = null;
        startInfo = null;
        startPoint = null;
        startBuildID = null;
    	startFloorID = 0;

        isEndSet = false;
        isEndCurrent = false;
        endTitle = null;
        endInfo = null;
        endPoint = null;
        endBuildID = null;
    	endFloorID = 0;

		setStartPointInfoStyle(POINFO_START_NOT_SET);
		setEndPointInfoStyle(POINFO_END_NOT_SET);
        
// [RESOLVED REQUIREMENT] Remove the icons of start and end point from the map view
//        ((PMap)mContext).clearOverlay(PMap.OVERLAY_START_PIN);
//        ((PMap)mContext).clearOverlay(PMap.OVERLAY_END_PIN);
//        if(((PMap)mContext).popupOverlay != null)
//        	((PMap)mContext).popupOverlay.hide();
		clearStartPointDisplay();
		clearEndPointDisplay();
		hidePopup();
        
        btnGo.setEnabled(false);
        btnGroup.check(DIRECTION_MODE_WALK);
        
        switchToSetup();
        
        // Initialize the results parameters
    	curResultIdx = 0;
    	curStepIdx = 0;
    	maxStepIdx = 0;
    	curSubStepIdx = 0;
    	maxSubStepIdx = 0;
    	inSubStep = false;
    	Arrays.fill(transitAvoidStatus, true);
	}
	
	/*
	 * Restore the data from sharedPreference
	 */
	public void dataRestoration()
	{
		Log.v(debugTag, "dataRestoration");
		// Restore setup preferences (if existed)
		SharedPreferences settings = mContext.getSharedPreferences(PMap.PREFNAME_DIRECTION, 0);
		int tmpLatE6;
		int tmpLonE6;
		
		if((isStartSet = settings.getBoolean(PREFS_ISSTARTSET, false)) && 
				(tmpLatE6 = settings.getInt(PREFS_STARTPNT_LAT, 91000000)) != 91000000 &&
				(tmpLonE6 = settings.getInt(PREFS_STARTPNT_LON, 181000000)) != 181000000)
		{
			isStartCurrent = settings.getBoolean(PREFS_ISSTARTCUR, false);
	        startTitle = settings.getString(PREFS_STARTTITLE, mContext.getString(R.string.custom_point));
	        startInfo = settings.getString(PREFS_STARTINFO, "(" + tmpLatE6 + "," + tmpLonE6 + ")");
	        startPoint = new GeoPoint(tmpLatE6, tmpLonE6);
	        startBuildID = settings.getString(PREFS_STARTPNT_STR_BUILDID, null);
	        startFloorID = settings.getInt(PREFS_STARTPNT_INT_FLOORID, 0);
	        
			if(isStartCurrent)
				setStartPointInfoStyle(POINFO_START_CUR_LOCATION);
			else
				setStartPointInfoStyle(POINFO_START_CUSTOM);
			
			setStart(isStartCurrent, startTitle, startInfo, startPoint, startBuildID, startFloorID);
			
			Log.d(debugTag, "dataRestoration - setStart: " + startTitle + "," + startInfo);
		}
		
		if((isEndSet = settings.getBoolean(PREFS_ISENDSET, false)) && 
				(tmpLatE6 = settings.getInt(PREFS_ENDPNT_LAT, 91000000)) != 91000000 &&
				(tmpLonE6 = settings.getInt(PREFS_ENDPNT_LON, 181000000)) != 181000000)
		{
			isEndCurrent = settings.getBoolean(PREFS_ISENDCUR, false);
			endTitle = settings.getString(PREFS_ENDTITLE, mContext.getString(R.string.custom_point));
			endInfo = settings.getString(PREFS_ENDINFO, "(" + tmpLatE6 + "," + tmpLonE6 + ")");
			endPoint = new GeoPoint(tmpLatE6, tmpLonE6);
			endBuildID = settings.getString(PREFS_ENDPNT_STR_BUILDID, null);
	        endFloorID = settings.getInt(PREFS_ENDPNT_INT_FLOORID, 0);
			
			if(isEndCurrent)
				setEndPointInfoStyle(POINFO_END_CUR_LOCATION);
			else
				setEndPointInfoStyle(POINFO_END_CUSTOM);
			
			setEnd(isEndCurrent, endTitle, endInfo, endPoint, endBuildID, endFloorID);
			
			Log.d(debugTag, "dataRestoration - setEnd: " + endTitle + "," + endInfo);
		}
		
		isEnabled = settings.getBoolean(PREFS_IS_ENABLED, false);
		directionMode = settings.getInt(PREFS_DIRECTIONMODE, DIRECTION_MODE_WALK);
		btnGroup.check(directionMode);
		
// [RESOLVED REQUIREMENT] Remove any popup from the map view
//		if(((PMap)mContext).popupOverlay != null)
//        	((PMap)mContext).popupOverlay.hide();
		hidePopup();
		
		// Restore the transit-avoid records
		if(settings.getBoolean(PREFS_HAS_TA_RECORD, false))
			for(int i = 0; i < transitAvoidStatus.length; i++)
				transitAvoidStatus[i] = settings.getBoolean(PREFS_TA_PREFIX + i, true);
			
		// Restore the result operation parameters and the routing results
		isDisplayingResults = settings.getBoolean(PREFS_RESULT_IS_DISPLAY, false);
		if(isDisplayingResults)
		{
			curSourceResult = settings.getString(PREFS_RESULT_SRC, null);
			if(curSourceResult != null && (jResult = new DirectionJSON(mContext, curSourceResult)) != null)
			{
				if(jResult.status.equals(DirectionJSON.SUCCESS))
				{
					// Prepare the summary data list
					mRoutesList.clear();
					for(int i = 0; i < jResult.routes.size(); i++)
					{
						Map<String, Object> route = new HashMap<String, Object>();
						route.put("ROUTE_NAME", jResult.routes.get(i).summary);
						route.put("ROUTE_SUMMARY", jResult.routes.get(i).legs.get(0).duration.text + ", " + jResult.routes.get(i).legs.get(0).distance.text);
						mRoutesList.add(route);
					}

					curResultIdx = settings.getInt(PREFS_RESULT_IDX, 0);
					initResultRoute(curResultIdx);
					
					curStepIdx = settings.getInt(PREFS_RESULT_STEP_IDX, 0);
					initResultStep(curStepIdx);
					
					inSubStep = settings.getBoolean(PREFS_RESULT_IS_SUBSTEP, false);	// [NOTE]: This should be true if it is indoor currently
					if(inSubStep)
					{
						curSubStepIdx = settings.getInt(PREFS_RESULT_SUBSTEP_IDX, 0);
						initResultOutdoorSubStep(curSubStepIdx);
					}
					
					if(inSubStep && curStep.building_id != null && !curStep.building_id.equals(EMPTY_BUILDID) && curSubStep.routeLine != null)
						switchToResult(true);
					else
						switchToResult(false);
				}
				else
					isDisplayingResults = false;
			}
			else
				isDisplayingResults = false;
		}
		
		if(isEnabled)
			show();
		else
			hide();
	}
	
	/*
	 * Push the current data into sharedPreference
	 */
	public void dataFinalize()
	{
		Log.v(debugTag, "dataFinalize");
		// Store the preferences
		SharedPreferences settings = mContext.getSharedPreferences(PMap.PREFNAME_DIRECTION, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean(PREFS_ISSTARTSET, isStartSet);
		editor.putBoolean(PREFS_ISSTARTCUR, isStartCurrent);
		editor.putString(PREFS_STARTTITLE, startTitle);
		editor.putString(PREFS_STARTINFO, startInfo);
		if(startPoint != null)
		{
			editor.putInt(PREFS_STARTPNT_LAT, startPoint.getLatitudeE6());
			editor.putInt(PREFS_STARTPNT_LON, startPoint.getLongitudeE6());
		}
		editor.putString(PREFS_STARTPNT_STR_BUILDID, startBuildID);
		editor.putInt(PREFS_STARTPNT_INT_FLOORID, startFloorID);
		
		editor.putBoolean(PREFS_ISENDSET, isEndSet);
		editor.putBoolean(PREFS_ISENDCUR, isEndCurrent);
		editor.putString(PREFS_ENDTITLE, endTitle);
		editor.putString(PREFS_ENDINFO, endInfo);
		if(endPoint != null)
		{
			editor.putInt(PREFS_ENDPNT_LAT, endPoint.getLatitudeE6());
			editor.putInt(PREFS_ENDPNT_LON, endPoint.getLongitudeE6());
		}
		editor.putString(PREFS_ENDPNT_STR_BUILDID, endBuildID);
		editor.putInt(PREFS_ENDPNT_INT_FLOORID, endFloorID);
		
		editor.putInt(PREFS_DIRECTIONMODE, directionMode);
		editor.putBoolean(PREFS_IS_ENABLED, isEnabled);
		
		// Store the transit-avoid records
		editor.putBoolean(PREFS_HAS_TA_RECORD, true);
		for(int i = 0; i < transitAvoidStatus.length; i++)
			editor.putBoolean(PREFS_TA_PREFIX + i, transitAvoidStatus[i]);
		
		// Store the result operation parameters and the routing results
		editor.putBoolean(PREFS_RESULT_IS_DISPLAY, isDisplayingResults);
		if(isDisplayingResults)
		{
			editor.putString(PREFS_RESULT_SRC, curSourceResult);
			editor.putInt(PREFS_RESULT_IDX, curResultIdx);
			editor.putInt(PREFS_RESULT_STEP_IDX, curStepIdx);
			editor.putInt(PREFS_RESULT_SUBSTEP_IDX, curSubStepIdx);
			editor.putBoolean(PREFS_RESULT_IS_SUBSTEP, inSubStep);
		}
		
		// Commit the edits
		editor.commit();
	}
	
	/*
	 * Return the last transitAvoid status
	 */
	public boolean[] getTransitAvoidArray()
	{
		return transitAvoidStatus;
	}
	
	/*
	 * Set the current transitAvoid status
	 */
	public void setTransitAvoidArray(boolean[] transitAvoid)
	{
		transitAvoidStatus = transitAvoid;
	}
	
	/*
	 * Initialize the local parameters for displaying the target route
	 */
	private void initResultRoute(int routeIdx)
	{
		curRoute = jResult.routes.get(routeIdx);
		curStepList = curRoute.legs.get(0).steps;
		curStep = curStepList.get(0);
		
		// Data for results operation
		curResultIdx = routeIdx;
		curStepIdx = 0;
		maxStepIdx = curStepList.size();
		curSubStepIdx = 0;
		
		if(curStep.sub_steps != null && curStep.sub_steps.size() > 0)
		{
			maxSubStepIdx = curStep.sub_steps.size();
			curSubStep = curStep.sub_steps.get(0);
		}
		else
		{
			maxSubStepIdx = 0;
			curSubStep = null;
		}
		
		inSubStep = false;
		
		// Prepare the step simple list
		mStepsList.clear();
		for(int i = 0; i < curStepList.size(); i++)
		{
			Map<String, Object> step = new HashMap<String, Object>();
			step.put("STEP_NAME", curStepList.get(i).html_instructions);
			step.put("STEP_SUMMARY", curStepList.get(i).distance.text + ", " + curStepList.get(i).duration.text);
			mStepsList.add(step);
		}
	}
	
	/*
	 * Update the local parameters for displaying the target step
	 */
	private void initResultStep(int stepIdx)
	{
		curStepIdx = stepIdx;
		
		curStep = curStepList.get(curStepIdx);
		curSubStepIdx = 0;
		
		if(curStep.sub_steps != null && curStep.sub_steps.size() > 0)
		{
			maxSubStepIdx = curStep.sub_steps.size();
			curSubStep = curStep.sub_steps.get(0);
		}
		else
		{
			maxSubStepIdx = 0;
			curSubStep = null;
		}
		
		inSubStep = false;
	}
	
	private void initResultOutdoorSubStep(int subStepIdx)
	{
		curSubStepIdx = subStepIdx;
		curSubStep = curStep.sub_steps.get(subStepIdx);
	}
	
	/** Panel's visual control */
	
	private void updateResultPanel(boolean moveToStep)
	{
		if(curStepIdx == 0 && curSubStepIdx == 0)
			btnLastStep.setEnabled(false);
		else
			btnLastStep.setEnabled(true);
		
		if(maxSubStepIdx == 0)
			cboxStepSwitch.setEnabled(false);
		else
			cboxStepSwitch.setEnabled(true);
		
		if(inSubStep)
		{
			if(curStepIdx == maxStepIdx - 1 && curSubStepIdx == maxSubStepIdx - 1)
				btnNextStep.setEnabled(false);
			else
				btnNextStep.setEnabled(true);
			
			cboxStepSwitch.setChecked(true);
			btnStepInfo.setText(curSubStep.html_instructions);
		
			if(curStep.building_id != null && !curStep.building_id.equals(EMPTY_BUILDID))
			{
				mDisplay.setLineData(curSubStep.routeLine);
				mDisplay.showEndPoint();
				animateToStep(moveToStep, curSubStep.start_location.latE6, curSubStep.start_location.lngE6, curStep.building_id, curSubStep.start_location.z_index, false, 0);
			}
			else
			{
				mDisplay.setLineData(curRoute.routeLine);
				animateToStep(moveToStep, curSubStep.end_location.latE6, curSubStep.end_location.lngE6, curStep.building_id, curSubStep.end_location.z_index, false, 0);
			}
		}
		else	// [NOTE] It must be outdoor if not in subStep currently
		{
			mDisplay.setLineData(curRoute.routeLine);
			
			if(curStepIdx == maxStepIdx - 1)
				btnNextStep.setEnabled(false);
			else
				btnNextStep.setEnabled(true);
			
			cboxStepSwitch.setChecked(false);
			btnStepInfo.setText(curStep.html_instructions);
			
			/* TRICK: Simulating the real-arriving-time for transit steps */
			if(curStep.stop_from_id == 0)
				animateToStep(moveToStep, curStep.start_location.latE6, curStep.start_location.lngE6, null, 0, false, 0);
			else
			{
				int timeOffset = curStep.stop_from_id;
				timeOffset = (timeOffset%100)/10 + timeOffset%10;
				if((timeOffset > 25) || (timeOffset < 5)) timeOffset = 13;
				
				animateToStep(moveToStep, curStep.start_location.latE6, curStep.start_location.lngE6, null, 0, true, timeOffset);
			}
		}
	}
	
	private void animateToStep(boolean moveToIt, int latE6, int lonE6, String buildID, int floor, boolean TRICK_ShowTheArrivingTime, int TRICK_TheArrivingTime)
	{
		mDisplay.mapAim(latE6, lonE6, TRICK_ShowTheArrivingTime, TRICK_TheArrivingTime);
		mDisplay.invalidate();
		
// [RESOLVED REQUIREMENT] Move the map view the the given (latitudeE6, longitudeE6)
//		((PMap)mContext).mapCtrl.animateTo(new GeoPoint(latE6, lonE6));
		if(moveToIt)
			moveMapTo(latE6, lonE6, buildID, floor);
	}
	
	private void switchToResult(boolean moveToStep)
	{
		if(isEnabled)
		{
			mSetupLayout.setVisibility(View.GONE);
			mStepLayout.setVisibility(View.VISIBLE);
		}
		isDisplayingResults = true;
		mDisplay.setLineData(curRoute.routeLine);
		mDisplay.showLine();
		updateResultPanel(moveToStep);
	}
	
	private void switchToSetup()
	{
		if(isEnabled)
		{
			mSetupLayout.setVisibility(View.VISIBLE);
			mStepLayout.setVisibility(View.GONE);
		}
		mDisplay.hideLine();
		isDisplayingResults = false;
	}
	
	private void setStartPointInfoStyle(int mode)
	{
		switch(mode)
		{
		case POINFO_START_CUR_LOCATION:
			btnToFromPnt.clearAnimation(); 
			btnFromInfo.clearAnimation();
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfoCurLocation);
			btnFromInfo.setEnabled(true);
			break;
		case POINFO_START_CUSTOM:
			btnToFromPnt.clearAnimation();
			btnFromInfo.clearAnimation();
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfo);
			btnFromInfo.setEnabled(true);
			break;
		case POINFO_START_NOT_SET:
			btnToFromPnt.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnFromInfo.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfoDefault);
			btnFromInfo.setText(R.string.hint_on_direction_from_default);
			btnFromInfo.setEnabled(false);
			break;
		}
	}
	
	private void setEndPointInfoStyle(int mode)
	{
		switch(mode)
		{
		case POINFO_END_CUR_LOCATION:
			btnToToPnt.clearAnimation();
			btnToInfo.clearAnimation();
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfoCurLocation);
			btnToInfo.setEnabled(true);
			break;
		case POINFO_END_CUSTOM:
			btnToToPnt.clearAnimation();
			btnToInfo.clearAnimation();
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfo);
			btnToInfo.setEnabled(true);
			break;
		case POINFO_END_NOT_SET:
			btnToToPnt.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnToInfo.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfoDefault);
			btnToInfo.setText(R.string.hint_on_direction_to_default);
			btnToInfo.setEnabled(false);
			break;
		}
	}
	
	private void checkReady()
	{
		if(isStartSet && isEndSet)
			btnGo.setEnabled(true);
		else 
			btnGo.setEnabled(false);
			
	}
	
	/** Overall's visual & data control for the parent view */

	public void quitSubStep()
	{
		inSubStep = false;
	}
	
	public void show()
	{
		Log.v(debugTag, "Try to enter direction mode, curIsEnabled:" + Boolean.toString(isEnabled) + ", curIsDisplayingResults:" + Boolean.toString(isDisplayingResults));
		isEnabled = true;
		if(!isDisplayingResults)
			switchToSetup();
		else
		{
			switchToResult(false);
			mDisplay.showLine();
		}
	}
	
	public void hide()
	{
		mSetupLayout.setVisibility(View.GONE);
		mStepLayout.setVisibility(View.GONE);
		isEnabled = false;
		if(isDisplayingResults && inSubStep && curStep.building_id != null)
			mDisplay.hideLine();
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}
	
	public boolean isDisplayingResult()
	{
		return isDisplayingResults;
	}
	
	public void setStart(boolean isCurLoc, String title, String info, GeoPoint location, String buildID, int floorID)
	{
        startTitle = title;
        startInfo = info;
        startPoint = location;
        startBuildID = buildID;
        startFloorID = floorID;
        
        isStartSet = true;
        btnFromInfo.setText(startTitle);
        if(isCurLoc)
        {
        	setStartPointInfoStyle(POINFO_START_CUR_LOCATION);
        	isStartCurrent = true;
        }
        else
        {
        	setStartPointInfoStyle(POINFO_START_CUSTOM);
        	isStartCurrent = false;
        }
		checkReady();
		switchToSetup();
// [RESOLVED REQUIREMENT] Set the icon and popup of start point on the map view
//		((PMap)mContext).overlayUpdateStartPin(location, info, "");
//		((PMap)mContext).popupStartPin();
		setStartPointDisplay(location, buildID, floorID, info);
		popupStartPoint();
	}
	
	public void setEnd(boolean isCurLoc, String title, String info, GeoPoint location, String buildID, int floorID)
	{
        endTitle = title;
        endInfo = info;
        endPoint = location;
        endBuildID = buildID;
        endFloorID = floorID;
        
        isEndSet = true;
        btnToInfo.setText(endTitle);
        if(isCurLoc)
        {
        	setEndPointInfoStyle(POINFO_END_CUR_LOCATION);
        	isEndCurrent = true;
        }
        else
        {
        	setEndPointInfoStyle(POINFO_END_CUSTOM);
        	isEndCurrent = false;
        }
		checkReady();
		switchToSetup();
// [RESOLVED REQUIREMENT] Set the icon and popup of end point on the map view
//		((PMap)mContext).overlayUpdateEndPin(location, info, "");
//		((PMap)mContext).popupEndPin();
		setEndPointDisplay(location, buildID, floorID, info);
		popupEndPoint();
	}
	
	public boolean hasSetSomething()
	{
		return (isStartSet || isEndSet);
	}
	
	public boolean isMass()
	{
		if(directionMode == DIRECTION_MODE_MASS)
			return true;
		else
			return false;
	}
	
	public int getCurIndoorSubFloor()
	{
		if(curSubStep != null)
			return curSubStep.start_location.z_index;
		else
			return 0;
	}
	
	private void switchDirectionMode()
	{
		if(directionMode == DIRECTION_MODE_WALK)
			directionMode = DIRECTION_MODE_MASS;
		else
			directionMode = DIRECTION_MODE_WALK;
		btnGroup.check(directionMode);
		switchToSetup();
	}
	
	public void reverseStartEnd()
	{
		boolean isTempSet = isEndSet;
		String tempTitle = endTitle;
		String tempInfo = endInfo;
		GeoPoint tempPoint = endPoint;
		String tempBuildID = endBuildID;
		int tempFloorID = endFloorID;
		
		isEndSet = isStartSet;
		endTitle = startTitle;
		endInfo = startInfo;
		endPoint = startPoint;
		endBuildID = startBuildID;
		endFloorID = startFloorID;
		
		isStartSet = isTempSet;
		startTitle = tempTitle;
		startInfo = tempInfo;
		startPoint = tempPoint;
		startBuildID = tempBuildID;
		startFloorID = tempFloorID;
		
		btnFromInfo.setText(startTitle);
		btnToInfo.setText(endTitle);
		
		if(!isStartSet)
		{
			setStartPointInfoStyle(POINFO_START_NOT_SET);
// [RESOLVED REQUIREMENT] Remove the icon of start point from the map view
//			((PMap)mContext).clearOverlay(PMap.OVERLAY_START_PIN);
			clearStartPointDisplay();
		}
		else
		{
			if(isStartCurrent)
				setStartPointInfoStyle(POINFO_START_CUR_LOCATION);
			else
				setStartPointInfoStyle(POINFO_START_CUSTOM);
// [RESOLVED REQUIREMENT] Set the icon of start point on the map view
//			((PMap)mContext).overlayUpdateStartPin(startPoint, startInfo, "");
			setStartPointDisplay(startPoint, startBuildID, startFloorID, startInfo);
		}
		
		if(!isEndSet)
		{
			setEndPointInfoStyle(POINFO_END_NOT_SET);
// [RESOLVED REQUIREMENT] Remove the icon of end point from the map view
//			((PMap)mContext).clearOverlay(PMap.OVERLAY_END_PIN);
			clearEndPointDisplay();
		}
		else
		{
			if(isEndCurrent)
				setEndPointInfoStyle(POINFO_END_CUR_LOCATION);
			else
				setEndPointInfoStyle(POINFO_END_CUSTOM);
// [RESOLVED REQUIREMENT] Set the icon of start point on the map view
//			((PMap)mContext).overlayUpdateEndPin(endPoint, endInfo, "");
			setEndPointDisplay(endPoint, endBuildID, endFloorID, endInfo);
		}
		switchToSetup();
	}
	
	public void openResultListDlg(boolean canChangeMode)
	{
		if(mRoutesList.size() > 0)
		{
			if(canChangeMode)
				new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_dialog_dialer)
		        .setTitle(R.string.hint_on_got_direction_title)
		        .setAdapter(new SimpleAdapter(mContext, 
		        		mRoutesList, 
		        		R.layout.simple_info, 
		        		new String[] {"ROUTE_NAME", "ROUTE_SUMMARY"}, 
		        		new int[] {R.id.simple_info_title, R.id.simple_info_info}), 
		        		new DialogInterface.OnClickListener(){
	
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								initResultRoute(arg1);
								switchToResult(true);
							}
			            }
		        )
		        .setNeutralButton(R.string.btn_chg_mode, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	switchDirectionMode();
		            }
		        })
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		        .show();
			else
				new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_dialog_dialer)
		        .setTitle(R.string.hint_on_got_direction_title)
		        .setAdapter(new SimpleAdapter(mContext, 
		        		mRoutesList, 
		        		R.layout.simple_info, 
		        		new String[] {"ROUTE_NAME", "ROUTE_SUMMARY"}, 
		        		new int[] {R.id.simple_info_title, R.id.simple_info_info}), 
		        		new DialogInterface.OnClickListener(){
	
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								initResultRoute(arg1);
								switchToResult(true);
							}
			            }
		        )
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		        .show();	
		}
		else	// This should not be happened
		{
			Log.e(debugTag, "openResultListDlg: mRoutesList is empty!");
		}
	}
	
	public void openStepListDlg()
	{
		if(mStepsList.size() > 0)
		{
			new AlertDialog.Builder(mContext)
			.setIcon(R.drawable.ic_dialog_dialer)
	        .setTitle(R.string.hint_on_direction_step_list_title)
	        .setAdapter(new SimpleAdapter(mContext, 
	        		mStepsList, 
	        		R.layout.simple_info, 
	        		new String[] {"STEP_NAME", "STEP_SUMMARY"}, 
	        		new int[] {R.id.simple_info_title, R.id.simple_info_info}), 
	        		new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							initResultStep(arg1);
							updateResultPanel(true);
						}
		            }
	        )
	        .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })
	        .show();
		}
		else	// This should not be happened
		{
			Log.e(debugTag, "openStepListDlg: mStepList is empty!");
		}
	}
	
	/** Panel's listener */
	
	private Button.OnClickListener fromInfo = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "fromInfo " + Integer.toString(((TextView)arg0).getText().length()));
			
// [RESOLVED REQUIREMENT] Open the bookmark for set start
//			((PMap)mContext).openBookmarkDialog(PMap.BOOKMARK_MODE_SET_START);
			openBookmarkForSetStart();
		}
		
	};
	
	private Button.OnClickListener toInfo = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "toInfo " + Integer.toString(((TextView)arg0).getText().length()));
			
// [RESOLVED REQUIREMENT] Open the bookmark for set end
//			((PMap)mContext).openBookmarkDialog(PMap.BOOKMARK_MODE_SET_END);
			openBookmarkForSetEnd();
		}
		
	};
	
	private Button.OnClickListener toFromPnt = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "toFromPnt");
// [RESOLVED REQUIREMENT] Move the map view to the start point and show its popup
//			((PMap)mContext).mapCtrl.animateTo(startPoint);
//			((PMap)mContext).popupStartPin();
			specialMoveToPoint(SP_TYPE_START_POINT, startPoint, startBuildID, startFloorID, startTitle, startInfo);
		}
		
	};
	
	private Button.OnClickListener toToPnt = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "toToPnt");
// [RESOLVED REQUIREMENT] Move the map view to the end point and show its popup
//			((PMap)mContext).mapCtrl.animateTo(endPoint);
//			((PMap)mContext).popupEndPin();
			specialMoveToPoint(SP_TYPE_END_POINT, endPoint, endBuildID, endFloorID, endTitle, endInfo);
		}
		
	};
	
	private Button.OnClickListener startRoute = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "startRoute");
    		new GetDirection().execute();
		}
		
	};
	
	private RadioGroup.OnCheckedChangeListener modeChange = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup arg0, int checkedId) {
			Log.v(debugTag, "modeChange");
			
			switch(checkedId)
			{
			case DIRECTION_MODE_WALK:
				Log.d(debugTag, "WALK_MODE");
				directionMode = DIRECTION_MODE_WALK;
				break;
			case DIRECTION_MODE_MASS:
				Log.d(debugTag, "MASS_MODE");
				directionMode = DIRECTION_MODE_MASS;
				break;
			}
		}
	};
	
	// Result operation listeners
	private Button.OnClickListener toStepPnt = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "toStepPnt");
			updateResultPanel(true);
		}
		
	};
	
	private Button.OnClickListener lastStep = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "lastStep");
			if(!inSubStep)
				initResultStep(curStepIdx - 1);
			else
			{
				if(curSubStepIdx == 0)
				{
					inSubStep = false;
				}
				else
					initResultOutdoorSubStep(curSubStepIdx - 1);
			}
			updateResultPanel(true);
		}
		
	};
	
	private Button.OnClickListener nextStep = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "nextStep");
			if(!inSubStep)
				initResultStep(curStepIdx + 1);
			else
			{
				if(curSubStepIdx == maxSubStepIdx - 1)
				{
					inSubStep = false;
					initResultStep(curStepIdx + 1);
				}
				else
					initResultOutdoorSubStep(curSubStepIdx + 1);
			}
			updateResultPanel(true);
		}
		
	};
	
	private Button.OnClickListener openStepsList = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "openStepsList");
			openStepListDlg();
		}
		
	};
	
	private Button.OnClickListener stepSwitch = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(debugTag, "stepSwitch");
			if(cboxStepSwitch.isChecked())
			{
				Log.d(debugTag, "on");
				inSubStep = true;
				updateResultPanel(true);
			}
			else
			{
				Log.d(debugTag, "off");
				inSubStep = false;
				updateResultPanel(true);
			}
		}
		
	};

	/** Inner data operation */
	
	private class GetDirection extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			jResult = null;
			progressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.hint_on_get_direction_title), mContext.getString(R.string.hint_on_get_direction_hint), false, true, new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					cancel(true);
				}
				
			});
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			boolean routingStatus = false;
			String responseBody = null;
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, HTTP_TIMEOUT);
			// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, HTTP_SO_TIMEOUT);
	        HttpClient httpclient = new DefaultHttpClient(httpParameters);
	        
	        // Compose the request URI
	        String requestUri = "http://" + SERVER_DOMAIN + ":8080/WalkServer/direction.jsp?origin=" + 
					   	Double.toString(startPoint.getLatitudeE6() / 1e6) + "," + Double.toString(startPoint.getLongitudeE6() / 1e6);
	        
	        if(startBuildID != null && startFloorID != 0)	// Attach the indoor information if existed (Indoor-POI required)
	        	requestUri += "," + startBuildID + "," + startFloorID;
	        
	        requestUri += "&destination=" + 
	        			Double.toString(endPoint.getLatitudeE6() / 1e6) + "," + Double.toString(endPoint.getLongitudeE6() / 1e6);
	        
	        if(endBuildID != null && endFloorID != 0)		// Attach the indoor information if existed (Indoor-POI required)
	        	requestUri += "," + endBuildID + "," + endFloorID;
	        
	        requestUri += "&mode=";							// Attach the mode
			switch(directionMode)
			{
			case DIRECTION_MODE_WALK:
				requestUri += "walk";
				break;
			case DIRECTION_MODE_MASS:
				requestUri += "mass&alternative=true";

				String transitAvoid = null;
				for(int i = 0; i < transitAvoidStatus.length; i++)
				{
					if(!transitAvoidStatus[i])
					{
						if(transitAvoid == null)
							transitAvoid = "&transit_avoid=";
						else
							transitAvoid += ",";
						
						switch(i + 1)	// [NOTE] Index 0 (WALK) will not be considered
						{
						case MT_Constant.TYPE_BUS:
							transitAvoid += "bus";
							break;
						case MT_Constant.TYPE_HSR:
							transitAvoid += "hsr";
							break;
						case MT_Constant.TYPE_MRT:
							transitAvoid += "mrt";
							break;
						case MT_Constant.TYPE_PLANE:
							transitAvoid += "plane";
							break;
						case MT_Constant.TYPE_SHUTTLE:
							transitAvoid += "shuttle";
							break;
						case MT_Constant.TYPE_RAIL:
							transitAvoid += "rail";
							break;
						}
					}
				}
				if(transitAvoid != null)
					requestUri += transitAvoid;
				break;
			default:
				requestUri += "walk";
			}
	        
			// Generate the HTTP-REQUEST
	        HttpGet httpget = new HttpGet(requestUri); 
	        
	        // Create a response handler
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				responseBody = httpclient.execute(httpget, responseHandler);
				routingStatus = true;
				Log.d(debugTag, requestUri);
				Log.d(debugTag, responseBody.trim());
			} catch (ClientProtocolException e) {
				Log.e(debugTag, e.getMessage());
			} catch (IOException e) {
				Log.e(debugTag, e.getMessage());
			} catch (Exception e) {
				Log.e(debugTag, e.getMessage());
			}
    		
    		jResult = null;
			
			if(routingStatus)
			{
				curSourceResult = responseBody;		// Backup for future restore
				jResult = new DirectionJSON(mContext, responseBody);
				
				mContext.getSharedPreferences(PMap.PREFNAME_DIRECTION, 0).edit().clear().commit();	// Clear the old data
			}
	
	        // When HttpClient instance is no longer needed, 
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        httpclient.getConnectionManager().shutdown();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			
			if(jResult != null)
			{
				if(jResult.status.equals(DirectionJSON.SUCCESS))
				{
					// Prepare the summary data list
					mRoutesList.clear();
					for(int i = 0; i < jResult.routes.size(); i++)
					{
						Map<String, Object> route = new HashMap<String, Object>();
						route.put("ROUTE_NAME", jResult.routes.get(i).summary);
						route.put("ROUTE_SUMMARY", jResult.routes.get(i).legs.get(0).duration.text + ", " + jResult.routes.get(i).legs.get(0).distance.text);
						mRoutesList.add(route);
					}
					openResultListDlg(false);
				}
				else
				{
		    		new AlertDialog.Builder(mContext)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.hint_on_get_direction_failed_title)
					.setMessage(jResult.statusToString())
		            .setNeutralButton(R.string.btn_ok, null)
					.show();
				}
			}
			else
				new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle(R.string.hint_on_get_direction_failed_title)
				.setMessage(R.string.hint_on_get_direction_failed_hint)
	            .setNeutralButton(R.string.btn_ok, null)
				.show();
				
			super.onPostExecute(result);
		}
	}
	
	/** Abstract function interface that need to be implemented */
	
	protected abstract String getDebugTag();
	
	// Point operations
	protected abstract void clearStartPointDisplay();
	protected abstract void clearEndPointDisplay();
	protected abstract void setStartPointDisplay(GeoPoint location, String buildID, int floorID, String popupText);
	protected abstract void setEndPointDisplay(GeoPoint location, String buildID, int floorID, String popupText);
	
	// Point-popup operations
	protected abstract void hidePopup();
	protected abstract void popupStartPoint();	// Display the popupText set by setStartPointDisplay()
	protected abstract void popupEndPoint();	// Display the popupText set by setEndPointDisplay()
	
	// Map operations
	protected abstract void moveMapTo(int latE6, int lonE6, String buildID, int floorID);
	
	// Bookmark operations
	protected abstract void openBookmarkForSetStart();
	protected abstract void openBookmarkForSetEnd();
	
	// Special operation: @Local: 1. move to point, 2. show popup; @remote: 1. set point, 2. move to point, 3. show popup
	protected abstract void specialMoveToPoint(int pointType, GeoPoint location, String buildID, int floorID, String name, String info);
}
