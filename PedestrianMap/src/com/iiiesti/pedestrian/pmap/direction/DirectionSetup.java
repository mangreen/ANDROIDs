package com.iiiesti.pedestrian.pmap.direction;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.iiiesti.pedestrian.pmap.PMap;
import com.iiiesti.pedestrian.pmap.R;

public class DirectionSetup {
	// Constants
	private static final String D_TAG = "Direction";
	private static final int DIRECTION_MODE_WALK = R.id.btn_direction_mode_walk;
	private static final int DIRECTION_MODE_MASS = R.id.btn_direction_mode_mt;
	private static final int POINFO_START_NOT_SET = 0;
	private static final int POINFO_START_CUR_LOCATION = 1;
	private static final int POINFO_START_CUSTOM = 2;
	private static final int POINFO_END_NOT_SET = 0;
	private static final int POINFO_END_CUR_LOCATION = 1;
	private static final int POINFO_END_CUSTOM = 2;
	
	// Data of start location
	private boolean isStartSet;
	private boolean isStartCurrent;
	private String startTitle;
	private String startInfo;
	private GeoPoint startPoint;
	
	// Data of end location
	private boolean isEndSet;
	private boolean isEndCurrent;
	private String endTitle;
	private String endInfo;
	private GeoPoint endPoint;
	
	// Flags
	private boolean isEnabled;
	
	// Resources
	private Context mContext;
	private View mParentView;
	private RelativeLayout mLayout;
	private DirectionJSON jResult;
	
	// Buttons
	private Button btnFromInfo;
	private Button btnToInfo;
	private Button btnToFromPnt;
	private Button btnToToPnt;
	private Button btnGo;
	private RadioGroup btnGroup;
	
	private void findViews()
	{
		btnFromInfo = (Button)mLayout.findViewById(R.id.btn_direction_from_pnt);
		btnToInfo = (Button)mLayout.findViewById(R.id.btn_direction_to_pnt);
		btnToFromPnt = (Button)mLayout.findViewById(R.id.btn_direction_to_f_pnt);
		btnToToPnt = (Button)mLayout.findViewById(R.id.btn_direction_to_t_pnt);
		btnGo = (Button)mLayout.findViewById(R.id.btn_direction_go);
		btnGroup = (RadioGroup)mLayout.findViewById(R.id.btn_group_mode);
	}
	
	private void setListener()
	{
		btnFromInfo.setOnClickListener(fromInfo);
		btnToInfo.setOnClickListener(toInfo);
		btnToFromPnt.setOnClickListener(toFromPnt);
		btnToToPnt.setOnClickListener(toToPnt);
		btnGo.setOnClickListener(startRoute);
		btnGroup.setOnCheckedChangeListener(modeChange);
	}
	
	/*
	 * Constructor
	 */
	public DirectionSetup(Context context, View parentView) {
		mContext = context;
		
		// Get the main layout
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mParentView = parentView;
        mLayout = (RelativeLayout)inflater.inflate(R.layout.direction_setup, null);
        
        ((ViewGroup)mParentView).addView(mLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        mLayout.setVisibility(View.GONE);
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
        
        isEndSet = false;
        isEndCurrent = false;
        endTitle = null;
        endInfo = null;
        endPoint = null;
        
        setStartPointInfoStyle(POINFO_START_NOT_SET);
        setEndPointInfoStyle(POINFO_END_NOT_SET);
        
        ((PMap)mContext).clearOverlay(PMap.OVERLAY_START_PIN);
        ((PMap)mContext).clearOverlay(PMap.OVERLAY_END_PIN);
        if(((PMap)mContext).popupOverlay != null)
        	((PMap)mContext).popupOverlay.hide();
        
        btnGo.setEnabled(false);
        
        btnGroup.check(R.id.btn_direction_mode_walk);
	}

	public void show()
	{
		if(!isEnabled)
		{
			mLayout.setVisibility(View.VISIBLE);
			isEnabled = true;
		}
	}
	
	public void hide()
	{
		mLayout.setVisibility(View.GONE);
		isEnabled = false;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}
	
	private void setStartPointInfoStyle(int mode)
	{
		switch(mode)
		{
		case POINFO_START_CUR_LOCATION:
			btnFromInfo.clearAnimation();
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfoCurLocation);
			btnToFromPnt.setEnabled(true);
			break;
		case POINFO_START_CUSTOM:
			btnFromInfo.clearAnimation();
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfo);
			btnToFromPnt.setEnabled(true);
			break;
		case POINFO_START_NOT_SET:
			btnFromInfo.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnFromInfo.setTextAppearance(mContext, R.style.DirectionPntInfoDefault);
			btnFromInfo.setText(R.string.hint_on_direction_from_default);
			btnToFromPnt.setEnabled(false);
			break;
		}
	}
	
	private void setEndPointInfoStyle(int mode)
	{
		switch(mode)
		{
		case POINFO_END_CUR_LOCATION:
			btnToInfo.clearAnimation();
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfoCurLocation);
			btnToToPnt.setEnabled(true);
			break;
		case POINFO_END_CUSTOM:
			btnToInfo.clearAnimation();
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfo);
			btnToToPnt.setEnabled(true);
			break;
		case POINFO_END_NOT_SET:
			btnToInfo.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.warrning_fade));
			btnToInfo.setTextAppearance(mContext, R.style.DirectionPntInfoDefault);
			btnToInfo.setText(R.string.hint_on_direction_to_default);
			btnToToPnt.setEnabled(false);
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
	
	// Listeners
	private Button.OnClickListener fromInfo = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "fromInfo " + Integer.toString(((TextView)arg0).getText().length()));
			
			((PMap)mContext).openBookmarkDialog(PMap.BOOKMARK_MODE_SET_START);
		}
		
	};
	
	private Button.OnClickListener toInfo = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "toInfo " + Integer.toString(((TextView)arg0).getText().length()));
			
			((PMap)mContext).openBookmarkDialog(PMap.BOOKMARK_MODE_SET_END);
		}
		
	};
	
	private Button.OnClickListener toFromPnt = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "toFromPnt");
			((PMap)mContext).mapCtrl.animateTo(startPoint);
			((PMap)mContext).popupStartPin();
		}
		
	};
	
	private Button.OnClickListener toToPnt = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "toToPnt");
			((PMap)mContext).mapCtrl.animateTo(endPoint);
			((PMap)mContext).popupEndPin();
		}
		
	};
	
	private Button.OnClickListener startRoute = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "startRoute");
    		new GetDirection().execute();
		}
		
	};
	
	private RadioGroup.OnCheckedChangeListener modeChange = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup arg0, int checkedId) {
			Log.v(D_TAG, "modeChange");
			
			switch(checkedId)
			{
			case DIRECTION_MODE_WALK:
				Log.d(D_TAG, "WALK_MODE");
				break;
			case DIRECTION_MODE_MASS:
				Log.d(D_TAG, "MASS_MODE");
				break;
			}
		}
	};
	
	// Data operations
	public void setStart(boolean isCurLoc, String title, String info, GeoPoint location)
	{
        startTitle = title;
        startInfo = info;
        startPoint = location;
        
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
		((PMap)mContext).overlayUpdateStartPin(location, info, "");
		((PMap)mContext).popupStartPin();
	}
	
	public void setEnd(boolean isCurLoc, String title, String info, GeoPoint location)
	{
        endTitle = title;
        endInfo = info;
        endPoint = location;
        
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
		((PMap)mContext).overlayUpdateEndPin(location, info, "");
		((PMap)mContext).popupEndPin();
	}
	
	public boolean hasSetSomething()
	{
		return (isStartSet || isEndSet);
	}
	
	public void reverseStartEnd()
	{
		boolean isTempSet = isEndSet;
		String tempTitle = endTitle;
		String tempInfo = endInfo;
		GeoPoint tempPoint = endPoint;
		
		isEndSet = isStartSet;
		endTitle = startTitle;
		endInfo = startInfo;
		endPoint = startPoint;
		
		isStartSet = isTempSet;
		startTitle = tempTitle;
		startInfo = tempInfo;
		startPoint = tempPoint;
		
		btnFromInfo.setText(startTitle);
		btnToInfo.setText(endTitle);
		
		if(!isStartSet)
		{
			setStartPointInfoStyle(POINFO_START_NOT_SET);
			((PMap)mContext).clearOverlay(PMap.OVERLAY_START_PIN);
		}
		else
		{
			if(isStartCurrent)
				setStartPointInfoStyle(POINFO_START_CUR_LOCATION);
			else
				setStartPointInfoStyle(POINFO_START_CUSTOM);
			
			((PMap)mContext).overlayUpdateStartPin(startPoint, startInfo, "");
		}
		
		if(!isEndSet)
		{
			setEndPointInfoStyle(POINFO_END_NOT_SET);
			((PMap)mContext).clearOverlay(PMap.OVERLAY_END_PIN);
		}
		else
		{
			if(isEndCurrent)
				setEndPointInfoStyle(POINFO_END_CUR_LOCATION);
			else
				setEndPointInfoStyle(POINFO_END_CUSTOM);
			
			((PMap)mContext).overlayUpdateEndPin(endPoint, endInfo, "");
		}
	}
	
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
			
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpget = new HttpGet("http://maps.google.com/maps/api/directions/json?origin=" + 
	        							   Double.toString(startPoint.getLatitudeE6() / 1e6) +
	        							   ","+
	        							   Double.toString(startPoint.getLongitudeE6() / 1e6) + 
	        							   "&destination=" + 
	        							   Double.toString(endPoint.getLatitudeE6() / 1e6) +
	        							   ","+
	        							   Double.toString(endPoint.getLongitudeE6() / 1e6) + 
	        							   "&mode=walking&sensor=true&ie=zh-TW"); 
	        // Create a response handler
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				responseBody = httpclient.execute(httpget, responseHandler);
				routingStatus = true;
				Log.d(D_TAG, responseBody);
			} catch (ClientProtocolException e) {
				Log.e(D_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(D_TAG, e.getMessage());
			}
			
			if(routingStatus)
			{
				jResult = new DirectionJSON(mContext, responseBody);
		    	
		    	if(jResult.status.equals(DirectionJSON.SUCCESS))
		    	{
		    		Log.e(D_TAG, "OK");
		    		
		    		/*TODO: Display the routing result*/
		    		((PMap)mContext).directionLineOverlay.setLineData(jResult.routes[0].legs[0].routeLine);
		    	}
		    	else
		    	{
		    		new AlertDialog.Builder(mContext)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.hint_on_get_direction_failed_title)
					.setMessage(jResult.statusToString())
		            .setNeutralButton(R.string.btn_ok, null)
					.show();
		    		
		    		jResult = null;
		    	}
			}
			else
			{
				new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle(R.string.hint_on_get_direction_failed_title)
				.setMessage(R.string.hint_on_get_direction_failed_hint)
	            .setNeutralButton(R.string.btn_ok, null)
				.show();
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
				((PMap)mContext).directionLineOverlay.show();
			
			super.onPostExecute(result);
		}
	}
}
