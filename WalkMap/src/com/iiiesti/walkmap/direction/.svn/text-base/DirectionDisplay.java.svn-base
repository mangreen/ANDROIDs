package com.iiiesti.walkmap.direction;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customType.Direction_Constant;
import com.iiiesti.walkmap.customType.Direction_RoutePoint;

public abstract class DirectionDisplay extends View {
	private static final int PAINT_TYPES = Direction_Constant.TRAVEL_MODE_MT + 1;
	private static final int RADIUS_STEP_AIM = 10;
	private static final int RADIUS_STEP_END_POINT = 5;
	
	/* Resources */
	private List<Direction_RoutePoint> routeLine;
	private Timer timer;
	private String timeHint_about;
	private String timeHint_arrive;
	private String timeHint_arriving;
	private String timeHint;
	
	// Paints
	private Paint[] paintLines;
	private Paint paintLineBase;
	private Paint paintStepAimBase;
	private Paint paintStepAim;
	private Paint paintStepPoint;
	private Paint paintText;
	private Paint paintBack;
	
	/* Variables */
	private Point fPoint;
	private int fMode;
	private Point tPoint;
	private Direction_RoutePoint pointInfo;
	private GeoPoint tStepPoint;
	private boolean drawLines;
	private boolean drawAim;
	private boolean drawEndPoint;
	
	private boolean trick_showArrivingTime;
	private int trick_arrivalInterval;
	
	public DirectionDisplay(Context context) {
		super(context);
		
		routeLine = null;
		drawLines = false;
		drawAim = false;
		drawEndPoint = false;
		trick_showArrivingTime = false;
		timer = null;
		timeHint_about = getResources().getString(R.string.hint_on_direction_about);
		timeHint_arrive = getResources().getString(R.string.unit_minute) + getResources().getString(R.string.hint_on_direction_arrive);
		timeHint_arriving = getResources().getString(R.string.hint_on_direction_arriving);
		timeHint = null;
		
		// Setup the custom paints
		paintLines = new Paint[PAINT_TYPES];
		paintLineBase = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStepAimBase = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStepAim = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStepPoint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		for(int i = 0; i < PAINT_TYPES; i++)
		{
			paintLines[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
			paintLines[i].setStyle(Paint.Style.STROKE);
			paintLines[i].setStrokeWidth(5);
			paintLines[i].setStrokeCap(Paint.Cap.ROUND);
			paintLines[i].setStrokeJoin(Paint.Join.ROUND);
		}
		
		paintLineBase.setStyle(Paint.Style.STROKE);
		paintLineBase.setStrokeWidth(8);
		paintLineBase.setStrokeCap(Paint.Cap.ROUND);
		paintLineBase.setStrokeJoin(Paint.Join.ROUND);
		paintLineBase.setColor(0x99FFFFFF);
		
		paintStepAimBase.setStyle(Paint.Style.STROKE);
		paintStepAimBase.setStrokeWidth(6);
		paintStepAimBase.setColor(0x771FA2FF);
		
		paintStepAim.setStyle(Paint.Style.STROKE);
		paintStepAim.setStrokeWidth(2);
		paintStepAim.setColor(0x99FFFFFF);
		
		paintStepPoint.setStyle(Paint.Style.STROKE);
		paintStepPoint.setStrokeWidth(4);
		paintStepPoint.setColor(0x991FA2FF);
		
		paintLines[Direction_Constant.TRAVEL_MODE_WALKING].setColor(0xBB222222);
		paintLines[Direction_Constant.TRAVEL_MODE_VIRTUAL].setColor(0xBB222222);
		paintLines[Direction_Constant.TRAVEL_MODE_VIRTUAL].setPathEffect(new DashPathEffect(new float[]{1, 6}, 0.9f));
		paintLines[Direction_Constant.TRAVEL_MODE_MT].setColor(0xBB1FA2FF);
		
		// For the arriving time
		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextAlign(Paint.Align.CENTER);
		paintText.setTextSize(15);
		paintText.setColor(0xEE29E2FF);

		paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBack.setStyle(Paint.Style.FILL);
		paintBack.setStrokeCap(Paint.Cap.ROUND);
		paintBack.setColor(0xBB000000);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(drawLines)
		{
			pointInfo = routeLine.get(0);
			fPoint = projectToPixel(pointInfo.geoPoint);
			fMode = pointInfo.mode;
			
			for(int i = 1; i < routeLine.size(); i++)
			{
				pointInfo = routeLine.get(i);
				tPoint = projectToPixel(pointInfo.geoPoint);
				canvas.drawLine(fPoint.x, fPoint.y, tPoint.x, tPoint.y, paintLineBase);
				fMode = pointInfo.mode;
				fPoint.set(tPoint.x, tPoint.y);
			}
			
			pointInfo = routeLine.get(0);
			fPoint = projectToPixel(pointInfo.geoPoint);
			fMode = pointInfo.mode;
			
			for(int i = 1; i < routeLine.size(); i++)
			{
				pointInfo = routeLine.get(i);
				tPoint = projectToPixel(pointInfo.geoPoint);
				canvas.drawLine(fPoint.x, fPoint.y, tPoint.x, tPoint.y, paintLines[fMode]);
				fMode = pointInfo.mode;
				fPoint.set(tPoint.x, tPoint.y);
			}
			
			if(drawEndPoint)
			{
				fPoint = projectToPixel(routeLine.get(routeLine.size() - 1).geoPoint);
				canvas.drawCircle(fPoint.x, fPoint.y, RADIUS_STEP_END_POINT, paintStepPoint);
			}
			
			if(drawAim)
			{
				fPoint = projectToPixel(tStepPoint);
				canvas.drawCircle(fPoint.x, fPoint.y, RADIUS_STEP_AIM, paintStepAim);
				canvas.drawCircle(fPoint.x, fPoint.y, RADIUS_STEP_AIM, paintStepAimBase);
				
				if(trick_showArrivingTime)
				{
					canvas.drawRoundRect(new RectF(fPoint.x - 55, fPoint.y - 50, fPoint.x + 55, fPoint.y - 21), 8, 8, paintBack);
					canvas.drawRoundRect(new RectF(fPoint.x - 53, fPoint.y - 48, fPoint.x + 53, fPoint.y - 23), 6, 6, paintStepAim);
					canvas.drawText(timeHint, fPoint.x, fPoint.y - 30, paintText);
				}
			}
		}
		super.onDraw(canvas);
	}
	
	// Interface
	public void setLineData(List<Direction_RoutePoint> shapeLine)
	{
		routeLine = shapeLine;
		drawEndPoint = false;
	}
	
	public void mapAim(int targetLatE6, int targetLonE6, boolean trickShowArrivingTime, int trickArrivalInterval)
	{
		tStepPoint = new GeoPoint(targetLatE6, targetLonE6);
		drawAim = true;
		trick_showArrivingTime = trickShowArrivingTime;
		trick_arrivalInterval = trickArrivalInterval;
		
		if(trickShowArrivingTime)
		{
			int time = trick_arrivalInterval - Calendar.getInstance().get(Calendar.MINUTE) % trick_arrivalInterval;
			
			if(time > 1)
				timeHint = timeHint_about + Integer.toString(time) + timeHint_arrive;
			else
				timeHint = timeHint_arriving;
			
			timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					int tTime = trick_arrivalInterval - Calendar.getInstance().get(Calendar.MINUTE) % trick_arrivalInterval;
					
					if(tTime > 1)
						timeHint = timeHint_about + Integer.toString(tTime) + timeHint_arrive;
					else
						timeHint = timeHint_arriving;
					
					postInvalidate();
				}
			}, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000, 60000);
		}
		else if(timer != null)
		{
			timeHint = null;
			timer.cancel();
			timer.purge();
		}
	}
	
	public void showEndPoint()
	{
		drawEndPoint = true;
	}

	public void showLine()
	{
		if(routeLine != null && routeLine.size() > 1)
			drawLines = true;
		else
			drawLines = false;
		invalidate();
	}
	public void hideLine()
	{
		drawLines = false;
		invalidate();
	}
	
	/** Abstract function interface that need to be implemented */
	
	// Map operations
	protected abstract Point projectToPixel(GeoPoint point);
}
