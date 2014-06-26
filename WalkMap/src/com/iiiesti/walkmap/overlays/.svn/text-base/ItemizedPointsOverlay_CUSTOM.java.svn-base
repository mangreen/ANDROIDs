package com.iiiesti.walkmap.overlays;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel_CUSTOM;

public class ItemizedPointsOverlay_CUSTOM extends ItemizedPointsOverlay {
	
	/* Constants */
	private static final String D_TAG = "CustomPin";
	private static final int GUIDE_LINE_BASE_LENGTH = 10;
	private static final int GUIDE_LINE_BASE_WIDTH = 5;
	private static final int PIN_ASSISTANCE_OFFSET = 10;
	private static final int PIN_DETECT_TOLERANCE = 5;
	private static final int SCROLL_TRIGGER_DIST_THRESHOLD = 10;
	private static final int SCROLL_TRIGGER_SQUAREDIST_THRESHOLD = SCROLL_TRIGGER_DIST_THRESHOLD * SCROLL_TRIGGER_DIST_THRESHOLD * 2;
	private static final int PIN_ANIMATE_DIST = 35;
	private static final int PIN_ANIMATE_OFFSET = 7;
	
	/* Resources */
	private Geocoder geocoder;
	private Drawable pinMarker;
	private Overlay_Location mLocation;
	private Paint mAddressIndicatePnt;
	private Paint mAddressIndicateLine;
	private Paint mAddressConnectionLine;
	private Paint mGuideLine;
	private Paint mGuideLineBase;
	private Rect rect;
	
	/* Variables */
	private int guideX;
	private int guideY;
	private int pinAssistOffset;
	private int viewHeight;
	private int viewWidth;
	private int guideBase_X1;
	private int guideBase_Y1;
	private boolean isDraggable;
	private boolean isDragging;
	private boolean isDetailReady;
	private boolean isAnimateDone;
	private float lastDownX;
	private float lastDownY;
	private int pinAnimateYPosition;
	private Point currentPin;
	private GeoPoint currentPoint;
	private List<Address> address;
	private boolean hasAddressLocation;
	private double addressLat;
	private double addressLon;
	private GeoPoint addressGeoPnt;
	private Point currentPnt;
	private Point addressPnt;
	private String titleName;
	private String addressName;
	private String buildID;
	private int floorID;
	
	public ItemizedPointsOverlay_CUSTOM(Context context, MapView mapView, Overlay_Location location) {
		super(context, mapView, mapView.getResources().getDrawable(R.drawable.ic_point_custom_pin_disable), true);
		
		mLocation = location;
		
		isDraggable = false;
		isDragging = false;
		isDetailReady = false;
		isAnimateDone = true;
		hasAddressLocation = false;
		
		mGuideLine = new Paint();
		mGuideLine.setStyle(Paint.Style.STROKE);
		mGuideLine.setARGB(50, 0, 50, 100);
		
		mGuideLineBase = new Paint();
		mGuideLineBase.setStyle(Paint.Style.STROKE);
		mGuideLineBase.setStrokeWidth(GUIDE_LINE_BASE_WIDTH);
		mGuideLineBase.setARGB(80, 0, 50, 100);
		
		mAddressConnectionLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAddressConnectionLine.setStyle(Paint.Style.STROKE);
		mAddressConnectionLine.setStrokeWidth(2);
		mAddressConnectionLine.setPathEffect(new DashPathEffect(new float[]{3, 1}, 1));
		mAddressConnectionLine.setColor(0x7F41D2E2);
		
		mAddressIndicateLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAddressIndicateLine.setStyle(Paint.Style.STROKE);
		mAddressIndicateLine.setStrokeWidth(2);
		mAddressIndicateLine.setColor(0xDD41D2E2);
		
		mAddressIndicatePnt = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAddressIndicatePnt.setStyle(Paint.Style.FILL);
		mAddressIndicatePnt.setColor(0x8F41D2E2);
		
		pinAssistOffset = mMarkerHeight + PIN_ASSISTANCE_OFFSET;
		
		pinMarker = context.getResources().getDrawable(R.drawable.ic_point_custom_pin_disable);
		
		currentPin = null;
		currentPoint = null;
		
		geocoder = new Geocoder(mContext, Locale.TRADITIONAL_CHINESE);
		address = null;
		
		buildID = null;
		floorID = 0;
		
		gestureDetector.setOnDoubleTapListener(onDoubleTapListener);
	}
	
	public boolean onDrag()
	{
		return isDraggable;
	}
	
	// Create the gesture detector
	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent event) {
			Log.v(D_TAG, "GestureDetector: onDown");
			if(hitTestTolerable(getItem(0), mMarker, (int)event.getX(), (int)event.getY(), PIN_DETECT_TOLERANCE))
			{
				lastDownX = event.getX();
				lastDownY = event.getY();
				guideX = (int)lastDownX;
				guideY = (int)lastDownY - pinAssistOffset;
				isDraggable = true;		// lock the onLongPress handler of the caller
			}
			return false;
		}

		@Override
		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			Log.v(D_TAG, "GestureDetector: onFling");
			return false;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onLongPress");
			((PMap)mContext).popupOverlay.hide();
			
			if(isDraggable)
			{
				isDragging = true;
				rect = pinMarker.copyBounds();	// Save the current state for future restore (in ACTION_UP handler)
				removeAllPointsOverlay();
				mMapView.postInvalidate();
			}
			else
				((PMap)mContext).overlayUpdateCustomPin(new Point((int)arg0.getX(), (int)arg0.getY()));
		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onSingleTapUp");
			return false;
		}
	};
	
	private GestureDetector.OnDoubleTapListener onDoubleTapListener = new GestureDetector.OnDoubleTapListener() {

		@Override
		public boolean onDoubleTap(MotionEvent arg0) {
			Log.v(D_TAG, "onDoubleTapListener: onDoubleTap");
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			Log.v(D_TAG, "onDoubleTapListener: onDoubleTapEvent");
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			Log.v(D_TAG, "onDoubleTapListener: onSingleTapConfirmed");
			
			if(isDetailReady && hitTestTolerable(getItem(0), mMarker, (int)event.getX(), (int)event.getY(), PIN_DETECT_TOLERANCE))
				((PMap)mContext).popupOverlay.popup(currentPoint, mMarker.getIntrinsicHeight(), addressName, getPopupRunnable(), true);
				
			return false;
		}
		
	};
	
	private GestureDetector gestureDetector = new GestureDetector(onGestureListener);
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		gestureDetector.onTouchEvent(event);
		
		switch(event.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			if(isDraggable)
			{
				if(isDragging)
				{
					guideX = (int)event.getX();
					guideY = (int)event.getY() - pinAssistOffset;
					return true;
				}
				else
				{
					if(((event.getX() - lastDownX) * (event.getX() - lastDownX) + (event.getY() - lastDownY) * (event.getY() - lastDownY)) > SCROLL_TRIGGER_SQUAREDIST_THRESHOLD)
						isDraggable = false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.v(D_TAG, "TapUp");
			if(isDraggable)
			{
				isDraggable = false;
				
				if(isDragging)
				{
					guideX = (int)event.getX();
					guideY = (int)event.getY() - pinAssistOffset;
					
					this.addPointsOverlay(new OverlayItem(projection.fromPixels(guideX, guideY), "", ""));
					
					pinMarker.setBounds(rect);	// Restore the last state store in onLongPress()
					isDragging = false;
				}

				return true;
			}
			break;		
		}
		
		// Canceling the default onTouchEvent() for avoiding unnecessary hitTest() execution	

		return false;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		return false;
	}
	
	private class reverseGeocodingTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... arg) {
			try {
				Log.d(D_TAG, "Loading address...");
				address = null;
				address = geocoder.getFromLocation((double)(currentPoint.getLatitudeE6() / 1e6), (double)(currentPoint.getLongitudeE6() / 1e6), 1);
			} catch (IOException e) {
				Log.e(D_TAG, e.getMessage());
				address = null;
			} catch (IllegalArgumentException e) {
				Log.e(D_TAG, e.getMessage());
				address = null;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(null != address && 0 < address.size())
			{
				Log.d(D_TAG, address.get(0).toString());

				try
				{
					addressLat = address.get(0).getLatitude();
					addressLon = address.get(0).getLongitude();
					addressGeoPnt = new GeoPoint((int)(addressLat * 1E6), (int)(addressLon * 1E6));
					hasAddressLocation = true;
					mMapView.invalidate();
				}
				catch(IllegalStateException e)
				{
					Log.e(D_TAG, "Address location not found: " + e.toString());
				}

				titleName = mContext.getString(R.string.custom_point);
				addressName = address.get(0).getAddressLine(0);
			}
			else
			{
				Log.d(D_TAG, "Address not found.");
				Toast.makeText(mContext, R.string.hint_on_get_address_failed_hint, Toast.LENGTH_LONG).show();
				
				titleName = mContext.getString(R.string.custom_point);
				addressName = "( " + Double.toString((double)(currentPoint.getLatitudeE6() / 1e6)) + "," + Double.toString((double)(currentPoint.getLongitudeE6() / 1e6)) + " )";
			}
			
			buildID = null;
			floorID = 0;
			isDetailReady = true;
			((PMap)mContext).popupOverlay.popup(currentPoint, mMarker.getIntrinsicHeight(), addressName, getPopupRunnable(), true);
			getItem(0).setMarker(boundCenterBottom(mMapView.getResources().getDrawable(R.drawable.ic_point_custom_pin)));
		}
	}
	
	public void addPointsOverlayAnimated(OverlayItem pointsOverlay) {
		isAnimateDone = false;
		rect = pinMarker.copyBounds();	// Save the current state for future restore (in animated draw)
		currentPin = projection.toPixels(pointsOverlay.getPoint(), null);
		pinAnimateYPosition = currentPin.y - PIN_ANIMATE_DIST;
		
		addPointsOverlay(pointsOverlay);
	}
	
	@Override
	public void addPointsOverlay(OverlayItem pointsOverlay) {
		super.addPointsOverlay(pointsOverlay);
		
		currentPoint = pointsOverlay.getPoint();
		
		// Retrieve the dimension information
		viewHeight = mMapView.getHeight();
		viewWidth = mMapView.getWidth();
		guideBase_X1 = viewWidth - GUIDE_LINE_BASE_LENGTH;
		guideBase_Y1 = viewHeight - GUIDE_LINE_BASE_LENGTH;
		
		// Try to reverse out the address
		new reverseGeocodingTask().execute();
	}
	
	public void addPointsOverlayWithData(OverlayItem pointsOverlay, String name, String info, int addrLatE6, int addrLonE6, String buildIdx, int floorIdx) {
		super.addPointsOverlay(pointsOverlay);
		
		currentPoint = pointsOverlay.getPoint();
		
		// Retrieve the dimension information
		viewHeight = mMapView.getHeight();
		viewWidth = mMapView.getWidth();
		guideBase_X1 = viewWidth - GUIDE_LINE_BASE_LENGTH;
		guideBase_Y1 = viewHeight - GUIDE_LINE_BASE_LENGTH;
		
		// Fill the panel with given data
		titleName = name;
		addressName = info;
		
		buildID = buildIdx;
		floorID = floorIdx;
		
		if(!(addrLatE6 > 90000000 || addrLonE6 > 180000000))
		{
			addressLat = addrLatE6 / 1e6;
			addressLon = addrLonE6 / 1e6;
			addressGeoPnt = new GeoPoint(addrLatE6, addrLonE6);
			hasAddressLocation = true;
			mMapView.invalidate();
		}
		isDetailReady = true;
		getItem(0).setMarker(boundCenterBottom(mMapView.getResources().getDrawable(R.drawable.ic_point_custom_pin)));
		((PMap)mContext).popupOverlay.popup(currentPoint, mMarker.getIntrinsicHeight(), addressName, getPopupRunnable(), true);
	}
	
	@Override
	public void removeAllPointsOverlay()
	{
		Log.d(D_TAG, "removeAllPointsOverlay self");
		isDetailReady = false;
		hasAddressLocation = false;
		super.removeAllPointsOverlay();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(hasAddressLocation && !shadow)
		{
			addressPnt = projection.toPixels(addressGeoPnt, null);
			currentPnt = projection.toPixels(getItem(0).getPoint(), null);
			
			canvas.drawLine(currentPnt.x, currentPnt.y, addressPnt.x, addressPnt.y, mAddressConnectionLine);
			canvas.drawCircle(addressPnt.x, addressPnt.y, 8, mAddressIndicateLine);
			canvas.drawCircle(addressPnt.x, addressPnt.y, 6, mAddressIndicatePnt);
		}
		
		if(isDragging && !shadow)
		{
			hasAddressLocation = false;
			
			canvas.drawLine(guideX, 0, guideX, mapView.getHeight(), mGuideLine);
			canvas.drawLine(0, guideY, mapView.getWidth(), guideY, mGuideLine);
			
			canvas.drawLine(guideX, 0, guideX, GUIDE_LINE_BASE_LENGTH, mGuideLineBase);
			canvas.drawLine(guideX, guideBase_Y1, guideX, mapView.getHeight(), mGuideLineBase);
			canvas.drawLine(0, guideY, GUIDE_LINE_BASE_LENGTH, guideY, mGuideLineBase);
			canvas.drawLine(guideBase_X1, guideY, mapView.getWidth(), guideY, mGuideLineBase);
			
			pinMarker.setBounds(guideX - mMarkerHalfWidth, guideY - mMarkerHeight, guideX + mMarkerHalfWidth, guideY);
			pinMarker.draw(canvas);
		}
		else
			super.draw(canvas, mapView, shadow);
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		if(isAnimateDone)
		{
			draw(canvas, mapView, shadow);
			return false;
		}
		else
		{	
			if(!shadow && (pinAnimateYPosition < currentPin.y))
			{
				pinMarker.setBounds(currentPin.x - mMarkerHalfWidth, pinAnimateYPosition - mMarkerHeight, currentPin.x + mMarkerHalfWidth, pinAnimateYPosition);
				pinMarker.draw(canvas);
				
				pinAnimateYPosition += PIN_ANIMATE_OFFSET;
				
				if(pinAnimateYPosition >= currentPin.y)
				{
					isAnimateDone = true;
					pinMarker.setBounds(rect);	// Restore the last state store in addPointsOverlayAnimated()
				}
			}
			return true;
		}
	}

	@Override
	protected boolean getSTAAFlag() {
		return true;
	}

	@Override
	protected Runnable getPopupRunnable() {
		return new Runnable() {	
			@Override
			public void run() {
				try
				{
					Log.d(D_TAG, "Switch to PointsDetailPanel now");
					
					Bundle bundle = new Bundle();
					bundle.putBoolean(PointsDetailPanel.BUNDLE_IS_CUSTOM, true);
					bundle.putString(PointsDetailPanel.BUNDLE_TITLE, titleName);
					bundle.putString(PointsDetailPanel.BUNDLE_INFO, addressName);
					bundle.putDouble(PointsDetailPanel.BUNDLE_LAT, (double)(currentPoint.getLatitudeE6() / 1e6));
					bundle.putDouble(PointsDetailPanel.BUNDLE_LON, (double)(currentPoint.getLongitudeE6() / 1e6));
					if(hasAddressLocation)
					{
						bundle.putDouble(PointsDetailPanel.BUNDLE_ADDRESS_LAT, addressLat);
						bundle.putDouble(PointsDetailPanel.BUNDLE_ADDRESS_LON, addressLon);
					}
					
					if(null != mLocation.getCurrentLocation())
					{
						bundle.putDouble(PointsDetailPanel.BUNDLE_CUR_LAT, mLocation.getCurrentLocation().getLatitude());
						bundle.putDouble(PointsDetailPanel.BUNDLE_CUR_LON, mLocation.getCurrentLocation().getLongitude());
					}
					
					if(null != buildID)
					{
						bundle.putString(PointsDetailPanel.BUNDLE_BUILD_ID, buildID);
						bundle.putInt(PointsDetailPanel.BUNDLE_FLOOR, floorID);
					}
		
					((Activity)mContext).startActivityForResult(new Intent(mContext, PointsDetailPanel_CUSTOM.class).putExtras(bundle), PMap.REQUEST_POINT_DETAIL);	
				}
				catch (Exception e)
				{
					Log.e(D_TAG, e.getMessage());
				}
			}
		};
	}
}

/*** Code backup */

/* Reverse Geo-coding by HTTPGET */
//final Runnable reverseGeocoding = new Runnable() {	
//	@Override
//	public void run() {
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpGet httpget = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + 
//        							   Double.toString(currentPoint.getLatitudeE6() / 1e6) +
//        							   ","+
//        							   Double.toString(currentPoint.getLongitudeE6() / 1e6) + 
//        							   "&sensor=false&ie=zh-TW"); 
//        Log.d(D_NETWORK, "http://maps.google.com/maps/api/geocode/json?latlng=" + 
//				   Double.toString(currentPoint.getLatitudeE6() / 1e6) +
//				   ","+
//				   Double.toString(currentPoint.getLongitudeE6() / 1e6) + 
//				   "&sensor=false&oe=utf8");
//        // Create a response handler
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();
//        String responseBody;
//		try {
//			responseBody = httpclient.execute(httpget, responseHandler);
//			Log.d(D_NETWORK, responseBody);
//		} catch (ClientProtocolException e) {
//			Log.e(D_NETWORK, e.getMessage());
//		} catch (IOException e) {
//			Log.e(D_NETWORK, e.getMessage());
//		}
//
//        // When HttpClient instance is no longer needed, 
//        // shut down the connection manager to ensure
//        // immediate deallocation of all system resources
//        httpclient.getConnectionManager().shutdown();
//	}
//};