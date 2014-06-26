package com.iiiesti.pedestrian.pmap.overlays;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.iiiesti.pedestrian.pmap.customType.Direction_Constant;
import com.iiiesti.pedestrian.pmap.customType.Direction_RoutePoint;

public class Overlay_DirectionLine extends Overlay{
	/* Constants */
	private static final String D_TAG = "DirectionLine";
	private static final int PAINT_TYPES = Direction_Constant.TRAVEL_MODE_TOTAL;

	/* Resources */
	private List<Direction_RoutePoint> routeLine;
	private MapView mMapView;
	private Projection projection;
	
	// Paints
	private Paint[] paints;
	
	/* Variables */
	private Point fPoint;
	private int fMode;
	private Point tPoint;
	private Direction_RoutePoint pointInfo;
	private boolean drawRouteLine;
	
	/** Constructor */
	public Overlay_DirectionLine(Context context, MapView mapView) {
		mMapView = mapView;
		projection = mMapView.getProjection();
		routeLine = new ArrayList<Direction_RoutePoint>();
		drawRouteLine = false;
		
		// Setup the custom paints
		paints = new Paint[PAINT_TYPES];
		for(int i = 0; i < PAINT_TYPES; i++)
		{
			paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
			paints[i].setStyle(Paint.Style.STROKE);
			paints[i].setStrokeWidth(6);
			paints[i].setStrokeCap(Paint.Cap.ROUND);
		}
		
		paints[Direction_Constant.TRAVEL_MODE_WALKING].setColor(0x8890C030);
		paints[Direction_Constant.TRAVEL_MODE_VIRTUAL].setColor(0x99000000);
		paints[Direction_Constant.TRAVEL_MODE_INDOOR].setColor(0x99000000);
		paints[Direction_Constant.TRAVEL_MODE_MT].setColor(0x99000000);
		paints[Direction_Constant.TRAVEL_MODE_MTSUB].setColor(0x99000000);
	}
	
	/** Overlay overrides */
	@Override
	public void draw(android.graphics.Canvas canvas,
            MapView mapView,
            boolean shadow)
	{
		if(!shadow && drawRouteLine)
		{
			pointInfo = routeLine.get(0);
			fPoint = projection.toPixels(pointInfo.geoPoint, null);
			fMode = pointInfo.mode;
			
			for(int i = 1; i < routeLine.size(); i++)
			{
				pointInfo = routeLine.get(i);
				tPoint = projection.toPixels(pointInfo.geoPoint, null);
				canvas.drawLine(fPoint.x, fPoint.y, tPoint.x, tPoint.y, paints[fMode]);
				fMode = pointInfo.mode;
				fPoint.set(tPoint.x, tPoint.y);
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
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return false;
	}
	
	// Interface
	public void setLineData(List<Direction_RoutePoint> lineList)
	{
		routeLine.clear();
		routeLine.addAll(lineList);
		Log.d(D_TAG, "setLineData: " + Integer.toString(routeLine.size()) + "points");
	}
	
	public void show()
	{
		if(routeLine.size() > 1)
			drawRouteLine = true;
		else
			drawRouteLine = false;
		mMapView.invalidate();
	}
	
	public void hide()
	{
		drawRouteLine = false;
		mMapView.invalidate();
	}
}