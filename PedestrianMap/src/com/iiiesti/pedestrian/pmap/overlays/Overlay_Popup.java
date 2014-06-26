package com.iiiesti.pedestrian.pmap.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.iiiesti.pedestrian.pmap.PMap;

public class Overlay_Popup extends Overlay{
	/* Constants */
	private static final String D_TAG = "PopupText";
	private static final int ORECT_OFFSET_TOP = 4;
	private static final int ORECT_OFFSET_LEFT = 5;
	private static final int ORECT_OFFSET_RIGHT = 5;
	private static final int ORECT_OFFSET_BOTTOM = 6;
	private static final int BORDER_WIDTH = 2;
	private static final int BRECT_OFFSET_TOP = ORECT_OFFSET_TOP + BORDER_WIDTH;
	private static final int BRECT_OFFSET_LEFT = ORECT_OFFSET_LEFT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_RIGHT = ORECT_OFFSET_RIGHT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_BOTTOM = ORECT_OFFSET_BOTTOM + BORDER_WIDTH;
	private static final int INDICATED_LINE = 5;

	/* Resources */
	private Handler handler = new Handler();
	private Runnable mRunnable;
	private Context mContext;
	private MapView mMapView;
	private Projection projection;
	
	// Paints
	private Paint paintShadow;
	private Paint paintText;
	private Paint paintBack;
	private Paint paintOutline;
	private Paint paintOutlineEnabled;
	
	/* Variables */
	private String outString;
	private GeoPoint inGeoPoint;
	private Point outPoint;
	private int vShift;
	private int textBOffsetX;
	private int textBOffsetY;
	private int textOOffsetX;
	private int textOOffsetY;
	private Rect textRect;
	private RectF backRect;
	private RectF outlineRect;
	private boolean drawPopup;
	private boolean isPressed;
	private boolean willRunIntoAnotherActivity;
	
	/** Constructor */
	public Overlay_Popup(Context context, MapView mapView) {
		mMapView = mapView;
		mContext = context;
		projection = mMapView.getProjection();

		textRect = new Rect();
	
		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextAlign(Paint.Align.CENTER);
		paintText.setTextSize(15);
		paintText.setColor(0xFFEEEEEE);

		paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBack.setStyle(Paint.Style.FILL);
		paintBack.setStrokeCap(Paint.Cap.ROUND);
		paintBack.setColor(0xBB000000);

		paintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintShadow.setStyle(Paint.Style.FILL);
		paintShadow.setStrokeCap(Paint.Cap.ROUND);
		paintShadow.setColor(0x00000000);
		
		paintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintOutline.setStyle(Paint.Style.STROKE);
		paintOutline.setStrokeWidth(1);
		paintOutline.setStrokeCap(Paint.Cap.ROUND);
		paintOutline.setColor(0xFF777777);
		
		paintOutlineEnabled = new Paint(paintOutline);
		paintOutlineEnabled.setColor(0xFFBBBBBB);
		
		drawPopup = false;
		isPressed = false;
	}
	
	/** Overlay overrides */
	@Override
	public void draw(android.graphics.Canvas canvas,
            MapView mapView,
            boolean shadow)
	{
		if(!shadow && drawPopup)
		{
			outPoint = projection.toPixels(inGeoPoint, null);
			outPoint.set(outPoint.x, outPoint.y - vShift);
			
			backRect.offsetTo(outPoint.x - textBOffsetX, outPoint.y - textBOffsetY);
			outlineRect.offsetTo(outPoint.x - textOOffsetX, outPoint.y - textOOffsetY);
			
			canvas.drawRoundRect(new RectF(backRect.left+2, backRect.top+2, backRect.right+2, backRect.bottom+2), 6, 6, paintShadow);
			canvas.drawRoundRect(backRect, 6, 6, paintBack);
			canvas.drawLine(outPoint.x, outPoint.y + BRECT_OFFSET_BOTTOM, outPoint.x, outPoint.y + BRECT_OFFSET_BOTTOM + INDICATED_LINE, paintBack);
			if(!isPressed)
				canvas.drawRoundRect(outlineRect, 5, 5, paintOutline);
			else
				canvas.drawRoundRect(outlineRect, 5, 5, paintOutlineEnabled);
			canvas.drawText(outString, outPoint.x, outPoint.y, paintText);
		}
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		draw(canvas, mapView, shadow);
		return false;
	}
	
	// Create the gesture detector
	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent event) {
			Log.v(D_TAG, "GestureDetector: onDown");
			if(hitTest(backRect, event.getX(), event.getY()))
			{
				isPressed = true;
				mMapView.invalidate();
				return true;
			}
			else
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
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onSingleTapUp");
			if(isPressed)
			{
				Log.d(D_TAG, "Hit");
				if(mRunnable != null)
				{
					hide();
					
					if(willRunIntoAnotherActivity)
						((PMap)mContext).setTouchHandleEnable(false);
					
					handler.post(mRunnable);
				}
			}
			else
				hide();
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			return false;
		}
	};
	
	private GestureDetector gestureDetector = new GestureDetector(onGestureListener);

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if(drawPopup && ((PMap)mContext).getTouchHandleStatus())
		{
			gestureDetector.onTouchEvent(event);	
			
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				Log.v(D_TAG, "TapDown");
				break;
			case MotionEvent.ACTION_UP:
				Log.v(D_TAG, "TapUp");
				if(isPressed)
				{
					isPressed = false;
					mMapView.invalidate();
				}
				break;
			}
			
			if(isPressed)
				return true;
		}
		return false;
	}

	protected boolean hitTest(RectF targetRect, float hitX, float hitY) {
		if((hitX <= targetRect.right) &&
		   (hitX >= targetRect.left) &&
		   (hitY <= targetRect.bottom) &&
		   (hitY >= targetRect.top))
			return true;
		else
			return false;
	}
	
	// Interface
	public boolean popup(GeoPoint popupLocation, int verticalShift, String popupText, Runnable runnable, boolean isRunIntoAnotherActivity)
	{
		// Store data
		outString = popupText;
		inGeoPoint = popupLocation;
		mRunnable = runnable;
		vShift = verticalShift;
		
		if(popupLocation == null)
			return false;
		
		if(mRunnable == null)
			willRunIntoAnotherActivity = false;
		else
			willRunIntoAnotherActivity = isRunIntoAnotherActivity;
		
		// Get the measurement
		paintText.getTextBounds(outString, 0, outString.length(), textRect);
		outlineRect = new RectF(textRect.left - ORECT_OFFSET_LEFT,
							textRect.top - ORECT_OFFSET_TOP,
							textRect.right + ORECT_OFFSET_RIGHT,
							textRect.bottom + ORECT_OFFSET_BOTTOM);
		
		backRect = new RectF(textRect.left - BRECT_OFFSET_LEFT,
				textRect.top - BRECT_OFFSET_TOP,
				textRect.right + BRECT_OFFSET_RIGHT,
				textRect.bottom + BRECT_OFFSET_BOTTOM);
		
		textBOffsetX = (textRect.width() >> 1) + BRECT_OFFSET_LEFT;
		textBOffsetY = textRect.height() + BRECT_OFFSET_TOP;
		
		textOOffsetX = (textRect.width() >> 1) + ORECT_OFFSET_LEFT;
		textOOffsetY = textRect.height() + ORECT_OFFSET_TOP;
		
		vShift += (BRECT_OFFSET_BOTTOM + INDICATED_LINE);
		
		drawPopup = true;
		mMapView.invalidate();
		
		return true;
	}
	
	public void hide()
	{
		drawPopup = false;
		mMapView.invalidate();
	}
	
	public void show()
	{
		drawPopup = true;
		mMapView.invalidate();
	}
}