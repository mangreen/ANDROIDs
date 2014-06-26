package com.iiiesti.walkmap.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;
import com.iiiesti.walkmap.R;

public class X_Deprecated_CustomLocationOverlay extends MyLocationOverlay {
	
	/* Constants */
	private static final String D_TAG = "PMap";
	private static final int IC_LOCATION_FULL = 20;
	private static final int IC_LOCATION_HALF = IC_LOCATION_FULL / 2;
	private static final int IC_ORIENTATION_FULL = 140;
	private static final int IC_ORIENTATION_HALF = IC_ORIENTATION_FULL / 2;
	private static final float BEARING_UPDATE_THRESHOLD = 6f;
	
	/* Resources */
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
	
	/* Variables */
	GeoPoint geoPoint;
	Point screenPoint;
	float lastBearing;
	int currentAccuracyRadius;
	int currentDrawingBound;
	int currentWidthMaxBound;
	int currentHeightMaxBound;
	int currentWidthMinBound;
	int currentHeightMinBound;
	boolean autoPan;
	
	/** Constructor */
	public X_Deprecated_CustomLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
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
	}
	
	/** Custom method */
	// Animate to current location under current zoom level
	public void animateToMyLocation()
	{
		Log.v(D_TAG, "animateToMyLocation");
		mMapController.animateTo(getMyLocation());
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
	
	/** Overrides */
	@Override
	protected void drawCompass(android.graphics.Canvas canvas, float bearing)
	{
		/* For disabling the default compass display only */	
	}
	
	@Override
	protected void drawMyLocation(android.graphics.Canvas canvas, MapView mapView, android.location.Location lastFix, GeoPoint myLocation, long when)
	{
		screenPoint = projection.toPixels(myLocation, null);
		currentAccuracyRadius = (int)projection.metersToEquatorPixels(lastFix.getAccuracy());
		currentDrawingBound = currentAccuracyRadius + IC_ORIENTATION_HALF;

		// Draw something only when the draw region is within the bounding box
		if(screenPoint.x < mMapView.getWidth() + currentDrawingBound &&
		   screenPoint.x > -currentDrawingBound &&
		   screenPoint.y < mMapView.getHeight() + currentDrawingBound &&
		   screenPoint.y > -currentDrawingBound)
		{
			// Draw accuracy range
			canvas.drawCircle(screenPoint.x, screenPoint.y, currentAccuracyRadius, mAccuracyBackground);
			canvas.drawCircle(screenPoint.x, screenPoint.y, currentAccuracyRadius, mAccuracyBorder);
			
			// Draw orientation
			matrix.reset();
			matrix.postRotate(getOrientation());
			mCurrentOrientation = Bitmap.createBitmap(mNorthOrientation, 0, 0, IC_ORIENTATION_FULL, IC_ORIENTATION_FULL, matrix, true);
			canvas.drawBitmap(mCurrentOrientation, screenPoint.x - (mCurrentOrientation.getWidth() >> 1), screenPoint.y - (mCurrentOrientation.getHeight() >> 1), null);

			// Draw location point
			canvas.drawBitmap(mCurrentLocation, screenPoint.x - IC_LOCATION_HALF, screenPoint.y - IC_LOCATION_HALF, null);
		}
	}
	
	@Override
	public void onLocationChanged(android.location.Location location)
	{	
		super.onLocationChanged(location);
		
		// To force updating drawMyLocation() when location updated
		mMapView.invalidate();
		
		// Map auto panning
		if(autoPan)
			mMapController.animateTo(getMyLocation());
	}
	
	@Override
	public void onSensorChanged(int sensor, float[] values)
	{
		super.onSensorChanged(sensor, values);
		
		// To force updating drawMyLocation() when compass updated where I draw the compass information
		if(BEARING_UPDATE_THRESHOLD < Math.abs(getOrientation() - lastBearing))
		{
			lastBearing = getOrientation();
			mMapView.invalidate();
		}
	}
}
