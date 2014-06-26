package com.iiiesti.pedestrian.pmap.overlays;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.android.maps.TrackballGestureDetector;
import com.iiiesti.pedestrian.pmap.PMap;

public class ItemizedPointsOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String D_TAG = "ItemizedOverlay";
	protected Drawable mMarker;
	protected Context mContext;
	protected MapView mMapView;
	protected Projection projection;
	protected List<OverlayItem> mOverlays;
	protected TrackballGestureDetector gestureDetector;
	
	/* Variables */
	protected int mMarkerHalfWidth;
	protected int mMarkerHeight;
	protected boolean mDrawShadow;
	protected Point mDummyPoint;
	
	public ItemizedPointsOverlay(Context context, MapView mapView, Drawable marker, boolean drawShadow) {
		super(boundCenterBottom(marker));

		mContext = context;
		mMapView = mapView;
		projection = mMapView.getProjection();
		mOverlays = new ArrayList<OverlayItem>();
		mMarker = boundCenterBottom(marker);
		mDrawShadow = drawShadow;
		mDummyPoint = new Point();
		
		mMarkerHalfWidth = mMarker.getIntrinsicWidth() >> 1;
		mMarkerHeight = mMarker.getIntrinsicHeight();
		
		populate();
	}
	
	public void addPointsOverlayNonPopulate(OverlayItem pointsOverlay)
	{
	    mOverlays.add(pointsOverlay);
	}
	
	public void doPopulate()
	{
	    populate();  
	}
	
	public void addPointsOverlay(OverlayItem pointsOverlay)
	{
	    mOverlays.add(pointsOverlay);
	    populate();  
	}
	
	public void removeAllPointsOverlay()
	{	
		mOverlays.clear();
		populate();
	}
	
	@Override
	protected boolean hitTest(OverlayItem item, Drawable marker, int hitX, int hitY) {
		Point itemPoint = projection.toPixels(item.getPoint(), null);
		
		if((hitX <= itemPoint.x + mMarkerHalfWidth) &&
		   (hitX >= itemPoint.x - mMarkerHalfWidth) &&
		   (hitY <= itemPoint.y) &&
		   (hitY >= itemPoint.y - mMarkerHeight))
			return true;
		else
			return false;
	}
	
	protected boolean hitTestTolerable(OverlayItem item, Drawable marker, int hitX, int hitY, int tolerance) {
		Point itemPoint = projection.toPixels(item.getPoint(), null);
		
		if((hitX <= itemPoint.x + mMarkerHalfWidth + tolerance) &&
		   (hitX >= itemPoint.x - mMarkerHalfWidth - tolerance) &&
		   (hitY <= itemPoint.y + tolerance) &&
		   (hitY >= itemPoint.y - mMarkerHeight - tolerance))
			return true;
		else
			return false;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		projection.toPixels(p, mDummyPoint);
		Log.v(D_TAG, "onTap " + Integer.toString(mDummyPoint.x) + "," + Integer.toString(mDummyPoint.y));
	

		for(int i = 0; i < size(); i++)
			if(hitTestTolerable(mOverlays.get(i), mMarker, mDummyPoint.x, mDummyPoint.y, 20))
			{
				Log.e(D_TAG, "Hit");
				OverlayItem item = (OverlayItem)mOverlays.get(i);
				((PMap)mContext).popupOverlay.popup(item.getPoint(), mMarker.getIntrinsicHeight(), item.getTitle(), null, false);
				return true;
			}

		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// Canceling the default onTouchEvent() for avoiding unnecessary hitTest() execution
		return false;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(mDrawShadow)
			super.draw(canvas, mapView, shadow);
		else if(!shadow)
			super.draw(canvas, mapView, shadow);
	}
	
	public void triggerPopup(int idx)
	{
		OverlayItem item = (OverlayItem)mOverlays.get(idx);
		((PMap)mContext).popupOverlay.popup(item.getPoint(), mMarker.getIntrinsicHeight(), item.getTitle(), null, false);
	}
	
	public int getDefaultMarkerHeight()
	{
		return mMarkerHeight;
	}
}
