package com.iiiesti.pedestrian.pmap.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.iiiesti.pedestrian.pmap.R;

public class Overlay_Location extends Overlay{
	/* Constants */
	private static final String D_TAG = "Location";
	private static final int IC_LOCATION_FULL = 20;
	private static final int IC_LOCATION_HALF = IC_LOCATION_FULL / 2;
	private static final int IC_ORIENTATION_FULL = 140;
	private static final int IC_ORIENTATION_HALF = IC_ORIENTATION_FULL / 2;
	private static final float BEARING_UPDATE_THRESHOLD = 6f;

	/* Resources */
	private Handler handler = new Handler();
	private Runnable mRunnable;
	private Context mContext;
	private MapView mMapView;
	private MapController mMapController;
	private Projection projection;
	private Paint mAccuracyBackground;
	private Paint mAccuracyBorder;
	private Bitmap mCurrentLocation;
	private Bitmap mNorthOrientation;
	private Bitmap mCurrentOrientation;
	private Matrix matrix;
	private Toast locationToast;
	
	/* Variables */
	private Point screenPoint;
	private float lastBearing;
	private float currentOrientationDrawX;
	private float currentOrientationDrawY;
	private int currentAccuracyRadius;
	private int currentDrawingBound;
	private boolean autoPan;
	private boolean drawLocation;
	private boolean drawOrientation;
	
	/* LocationListener configuration */
	private LocationManager mLocationManager;
	private LocationListener mListenerFine;
	private LocationListener mListenerCoarse;
	private GpsStatus.Listener mListenerGPSStatus;
	private MyLocationOverlay mCompassManager;
	private Overlay.Snappable snapple;
	
	/* Current operation states */
	private Location currentLocation;
	private GeoPoint currentPoint;
	private boolean isFineAvailable;
	private boolean isCoarseAvailable;
	
	/** Constructor */
	public Overlay_Location(Context context, MapView mapView) {
		mContext = context;
		mMapView = mapView;
		mMapController = mMapView.getController();
		projection = mMapView.getProjection();
		
		mAccuracyBackground = new Paint();
		mAccuracyBackground.setStyle(Paint.Style.FILL);
		mAccuracyBackground.setARGB(20, 0, 179, 255);
		
		mAccuracyBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAccuracyBorder.setStyle(Paint.Style.STROKE);
		mAccuracyBorder.setStrokeWidth(1);
		mAccuracyBorder.setARGB(90, 0, 179, 255);
		
		matrix = new Matrix();

		mCurrentLocation = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_current_location);
		mNorthOrientation = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_north_orientation);
		
		lastBearing = -BEARING_UPDATE_THRESHOLD;
		
		autoPan = false;
		
		// Location related setup
		currentLocation = null;
		currentPoint = null;
		drawLocation = false;
		
		locationToast = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
		locationToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 10);
		
		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		
		// create required location listeners
		mListenerGPSStatus = new GpsStatus.Listener() {
	  		@Override
	  		public void onGpsStatusChanged(int status) {
	  			switch(status)
	  			{
	  			case GpsStatus.GPS_EVENT_STARTED:
	  				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_STARTED");
	  				break;
	  			case GpsStatus.GPS_EVENT_STOPPED:
	  				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_STOPPED");
	  				break;
	  			case GpsStatus.GPS_EVENT_FIRST_FIX:
	  				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_FIRST_FIX");
	  				isFineAvailable = true;
	  				break;
	  			}
	  		}
		};
	
		mListenerCoarse = new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) {
				switch(status) {
				case LocationProvider.OUT_OF_SERVICE:
					Log.d(D_TAG, "Coarse onStatusChanged OUT_OF_SERVICE");
              		isCoarseAvailable = false;
              		break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
	              	Log.d(D_TAG, "Coarse onStatusChanged TEMPORARILY_UNAVAILABLE");
	              	isCoarseAvailable = false;
	              	break;
				case LocationProvider.AVAILABLE:
	              	Log.d(D_TAG, "Coarse onStatusChanged AVAILABLE");
	              	isCoarseAvailable = true;
				}
			}
			public void onProviderEnabled(String provider) {
				Log.d(D_TAG, "Coarse onProviderEnabled");
			}
			public void onProviderDisabled(String provider) {
	          	Log.d(D_TAG, "Coarse onProviderDisabled");       	
	          	isCoarseAvailable = false;
			}
			public void onLocationChanged(Location location) {
				if(isFineAvailable)
	          	{
					Log.d(D_TAG, "Coarse onLocationChange: fineLocation available, coarseListener is going to disable now");
	          		isCoarseAvailable = false;
	          		mLocationManager.removeUpdates(this);
	          	}
	          	else
	          	{
	          		isCoarseAvailable = true;
	          		currentLocation = location;
	          		currentPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
		                
	          		// To force updating drawMyLocation() when location updated
	          		drawLocation = true;
					mMapView.invalidate();
					
					// Map auto panning
					if(autoPan)
						mMapController.animateTo(currentPoint);
	          		
	                Log.d(D_TAG, "Coarse onLocationChange, Loc: " + Integer.toString(currentPoint.getLatitudeE6()) + ", " + Integer.toString(currentPoint.getLongitudeE6()));
	            	Log.d(D_TAG, "Coarse onLocationChange, Acc: " + Boolean.toString(location.hasAccuracy()) + ", " + Float.toString(location.getAccuracy()));
	          	}
			}
		};
  
		mListenerFine = new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) {
				switch(status) {
				case LocationProvider.OUT_OF_SERVICE:
					Log.d(D_TAG, "Fine onStatusChanged OUT_OF_SERVICE");
					isFineAvailable = false;
              	
					if(!isCoarseAvailable)
					{
						Log.d(D_TAG, "Fine onStatusChanged OUT_OF_SERVICE: coarse not available, try to enable the coarseListener now");
              		
						// Remove the old one (if existed) to prevent duplicate registration
						mLocationManager.removeUpdates(mListenerCoarse);
              		
						try
						{
							Criteria coarse = new Criteria();
							coarse.setAccuracy(Criteria.ACCURACY_COARSE);
							mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(coarse, true), 1000, 0, mListenerCoarse);
						}
						catch (IllegalArgumentException ae)
						{
							Log.e(D_TAG, "requestLocationUpdates-coarse: provider not found --> failed to enable the coarseListener");
							isCoarseAvailable = false;
						}
					}
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					Log.d(D_TAG, "Fine onStatusChanged TEMPORARILY_UNAVAILABLE");
					isFineAvailable = false;
					break;
				case LocationProvider.AVAILABLE:
					Log.d(D_TAG, "Fine onStatusChanged AVAILABLE");
					isFineAvailable = true;
				}
			}
			public void onProviderEnabled(String provider) {
				Log.d(D_TAG, "Fine onProviderEnabled");
			}
			public void onProviderDisabled(String provider) {
          		Log.d(D_TAG, "Fine onProviderDisabled");
          		isFineAvailable = false;
			}
			public void onLocationChanged(Location location) {
				isFineAvailable = true;
				currentLocation = location;
				currentPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
				
				// To force updating drawMyLocation() when location updated
				drawLocation = true;
				mMapView.invalidate();
				
				// Map auto panning
				if(autoPan)
					mMapController.animateTo(currentPoint);
              
				Log.d(D_TAG, "Fine onLocationChange, Loc: " + Integer.toString(currentPoint.getLatitudeE6()) + ", " + Integer.toString(currentPoint.getLongitudeE6()));
				Log.d(D_TAG, "Fine onLocationChange, Acc: " + Boolean.toString(location.hasAccuracy()) + ", " + Float.toString(location.getAccuracy()));
			}
      	};
      	
    	snapple = new Overlay.Snappable() {
    		
    		@Override
    		public boolean onSnapToItem(int opX, int opY, Point targetPoint, MapView mapView) {
    			if(((opX - targetPoint.x) * (opX - targetPoint.x) + (opY - targetPoint.y) * (opY - targetPoint.y)) <= (IC_LOCATION_HALF * IC_LOCATION_HALF))
    				return true;
    			else
    				return false;
    		}
    	};
		
		// Compass related setup
		drawOrientation = false;
		mCompassManager = new MyLocationOverlay(mContext, mMapView) {
			/** Overrides */
			@Override
			public boolean draw(android.graphics.Canvas canvas,
                    MapView mapView,
                    boolean shadow,
                    long when)
			{
				/* For disabling the default functionality only */
				return false;
			}
			
			@Override
			protected void drawMyLocation(android.graphics.Canvas canvas,
                    MapView mapView,
                    android.location.Location lastFix,
                    GeoPoint myLocation,
                    long when)
			{
				/* For disabling the default functionality only */
			}
			
			@Override
			protected void drawCompass(android.graphics.Canvas canvas,
                    float bearing)
			{
				/* For disabling the default functionality only */
			}
			
			@Override
			public void onLocationChanged(android.location.Location location)
			{	
				/* For disabling the default functionality only */
			}
			
			@Override
			public void onSensorChanged(int sensor, float[] values)
			{
				super.onSensorChanged(sensor, values);
				
				// To force updating drawMyLocation() when compass updated where I draw the compass information
				if(BEARING_UPDATE_THRESHOLD < Math.abs(getOrientation() - lastBearing))
				{
					drawOrientation = true;
					lastBearing = getOrientation();
					
					matrix.reset();
					matrix.postRotate(mCompassManager.getOrientation());
					mCurrentOrientation = Bitmap.createBitmap(mNorthOrientation, 0, 0, IC_ORIENTATION_FULL, IC_ORIENTATION_FULL, matrix, true);
					currentOrientationDrawX = mCurrentOrientation.getWidth() >> 1;
					currentOrientationDrawY = mCurrentOrientation.getHeight() >> 1;
					mMapView.invalidate();
				}
			}
		};
	}
	
	/** Overlay overrides */
	@Override
	public void draw(android.graphics.Canvas canvas,
            MapView mapView,
            boolean shadow)
	{
		if(!shadow && drawLocation)
		{
			screenPoint = projection.toPixels(currentPoint, null);
			currentDrawingBound = currentAccuracyRadius + IC_ORIENTATION_HALF;

			// Draw something only when the draw region is within the bounding box
			if(screenPoint.x < mMapView.getWidth() + currentDrawingBound &&
			   screenPoint.x > -currentDrawingBound &&
			   screenPoint.y < mMapView.getHeight() + currentDrawingBound &&
			   screenPoint.y > -currentDrawingBound)
			{
				// Draw accuracy range
				currentAccuracyRadius = (int)projection.metersToEquatorPixels(currentLocation.getAccuracy());
				canvas.drawCircle(screenPoint.x, screenPoint.y, currentAccuracyRadius, mAccuracyBackground);
				canvas.drawCircle(screenPoint.x, screenPoint.y, currentAccuracyRadius, mAccuracyBorder);
				
				// Draw orientation
				if(drawOrientation)
					canvas.drawBitmap(mCurrentOrientation, screenPoint.x - currentOrientationDrawX, screenPoint.y - currentOrientationDrawY, null);

				// Draw location point
				canvas.drawBitmap(mCurrentLocation, screenPoint.x - IC_LOCATION_HALF, screenPoint.y - IC_LOCATION_HALF, null);	
			}
		}
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		draw(canvas, mapView, shadow);
		return false;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView)
	{
		if(drawLocation)
		{
			Point tapPoint = projection.toPixels(p, null);
	
			if(snapple.onSnapToItem(tapPoint.x, tapPoint.y, screenPoint, mMapView))
				toastCurrentAccuracy();
		}

		return false;	// Since location layer is designated to always be the top layer, here we pass through the event for other layers
	}
	
	/** Custom method */
	// Toast current location accuracy
	public void toastCurrentAccuracy()
	{
		locationToast.setText(mContext.getText(R.string.hint_on_map_current_location_accuracy) + 
				 " " +
				 Float.toString(currentLocation.getAccuracy()) +
				 mContext.getText(R.string.hint_on_map_current_location_accuracy_unit));
		locationToast.show();
	}
	
	// Animate to current location under current zoom level
	public void animateToMyLocation()
	{
		Log.v(D_TAG, "animateToMyLocation");
		mMapController.animateTo(currentPoint);
	}
	
	// Turn auto-pan on
	public void turnOnAutoPan()
	{
		Log.v(D_TAG, "turnOnAutoPan");
		autoPan = true;
	}
	
	public void turnOffAutoPan()
	{
		Log.v(D_TAG, "turnOffAutoPan");
		autoPan = false;
	}
	
	/** Location management */
	// last location management
	final Runnable testLastFix = new Runnable() {	
		@Override
		public void run() {
			if(null != currentLocation)
				handler.post(mRunnable);
			else
				handler.postDelayed(this, 1000);
		}
	};
	
	public void runOnLastFix(java.lang.Runnable runnable)
	{
		mRunnable = runnable;
		
		if(null != currentLocation)
			handler.post(mRunnable);
		else
			handler.postDelayed(testLastFix, 1000);
	}
	
	public void setLastLocation()
	{	
		if(null == currentLocation)
		{
			currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			  
			if(null != currentLocation)
			{
				currentPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
				Log.d(D_TAG, "getLastKnownLocation from fineLocation: " + Integer.toString(currentPoint.getLatitudeE6()) + ", " + Integer.toString(currentPoint.getLongitudeE6()));
			}
			else
			{
				currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		      
				if(null != currentLocation)
				{
					currentPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
					Log.d(D_TAG, "getLastKnownLocation from coarseLocation: " + Integer.toString(currentPoint.getLatitudeE6()) + ", " + Integer.toString(currentPoint.getLongitudeE6()));
				}
				else
				{
						Log.d(D_TAG, "getLastKnownLocation: last know position not found");
				}
			}
		}
		
		if(null != currentLocation)
		{
			drawLocation = true;
			mMapView.invalidate();
		}
	}
	
	public Location getCurrentLocation()
	{
		return currentLocation;
	}
	
	public GeoPoint getCurrentPoint()
	{
		return currentPoint;
	}
	
	// Location listener management
	public boolean enableMyLocation() {
		Log.v(D_TAG, "Try to enableMyLocation");
		
		if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			return false;
		
		Log.d(D_TAG, "Register LocationListeners");
		setLastLocation();
		
		// Request for GPS status
		mLocationManager.addGpsStatusListener(mListenerGPSStatus);
	  
		// Preset the location status temporarily, for ensuring that there are available providers
		isCoarseAvailable = true;
		isFineAvailable = true;
	  
		try
		{
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, mListenerCoarse);
		}
		catch (IllegalArgumentException ae)
		{
			Log.e(D_TAG, "requestLocationUpdates-coarse: provider not found --> failed to enable the coarseListener");
			isCoarseAvailable = false;
		}
	  
		try
		{
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 3, mListenerFine);
		}
		catch (IllegalArgumentException ae)
		{
			Log.e(D_TAG, "requestLocationUpdates-fine: provider not found --> failed to enable the fineListener");
			isFineAvailable = false;
		}
	  
		if(!isCoarseAvailable && !isFineAvailable)
			return false;
		else
		{
			// reset the status for the following location update
			isCoarseAvailable = false;
			isFineAvailable = false;
			return true;
		}
	}
	
	public void disableMyLocation()
	{
		Log.v(D_TAG, "disableMyLocation");
		Log.d(D_TAG, "unRegisterLocationListeners");
		mLocationManager.removeGpsStatusListener(mListenerGPSStatus);
		mLocationManager.removeUpdates(mListenerCoarse);
		mLocationManager.removeUpdates(mListenerFine);
		drawLocation = false;
	}
	
	/** Sensor management */
	public boolean enableCompass()
	{
		Log.v(D_TAG, "Try to enableCompass");
		
		return mCompassManager.enableCompass();
	}
	
	public void disableCompass()
	{
		mCompassManager.disableCompass();
	}
}