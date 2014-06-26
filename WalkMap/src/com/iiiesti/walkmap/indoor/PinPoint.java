package com.iiiesti.walkmap.indoor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.iiiesti.walkmap.R;

public class PinPoint{
	/*This for debug*/
//	private static final String TAG = "Pin" ;	// Tony COMMENTED @ 1st merge
	
	private static final int ICON_WIDTH = 20;
	private static final int ICON_HEIGHT = 45;
	private static final int ICON_SHADOW_WIDTH = 32;
	private static final int ICON_SHADOW_HEIGHT = 32;
	
	private static final int ORECT_OFFSET_LEFT = 5;
	private static final int ORECT_OFFSET_TOP = 4;
	private static final int ORECT_OFFSET_RIGHT = 5;
	private static final int ORECT_OFFSET_BOTTOM = 6;
	private static final int BORDER_WIDTH = 2;
	private static final int BRECT_OFFSET_LEFT = ORECT_OFFSET_LEFT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_TOP = ORECT_OFFSET_TOP + BORDER_WIDTH;
	private static final int BRECT_OFFSET_RIGHT = ORECT_OFFSET_RIGHT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_BOTTOM = ORECT_OFFSET_BOTTOM + BORDER_WIDTH;
	private static final int TEXT_SHIFHT = ICON_HEIGHT + 10;
	
	private static final float BACK_ROUND_RECT = 6;
	private static final float OUTLINE_ROUND_RECT = 5;
	
	private static final float ANIMATE_SHIFT_START = 60;
	private static final float ANIMATE_SHIFT_OFFSET = 10;
	private static float ANIMATE_SHIFT = ANIMATE_SHIFT_START;
	
	public static final int MODE_CLEAR = -1;
	public static final int MODE_PIN_DISABLE = 0;
	public static final int MODE_PIN = 1;
	public static final int MODE_PIN_SHADOW = 2;
	public static final int MODE_START = 3;
	public static final int MODE_END = 4;
	public static final int MODE_POI = 5;
	public int mode;
	
	private float shadowLeft;
	private float shadowTop;
	private float shadowRight;
	private float shadowBootom;
	
	private float iconLeft;
	private float iconTop;
	private float iconRight;
	private float iconBootom;
	private float textX;
	private float textY;
	
	private Paint paintBack;
	private Paint paintOutline;
	private Paint paintText;
	private Paint paintShadow;
	private Paint paintOutlineEnabled;
	
	private Rect textRect;
	private RectF backRect;
	private RectF outlineRect;
	
	private Context context;
	private Drawable drawable;
	private int longtitude;
	private int latitude;
	private int floor;
	private String buildID;
	private String pinName;
	
	public boolean animateDone = false;
	public boolean pinFlag = false;

	public PinPoint(Context context, int mode) {
		this.context = context;
		this.mode = mode;
		drawable = context.getResources().getDrawable(R.drawable.ic_point_custom_pin_disable);
		
		textRect = new Rect();
		
		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextAlign(Paint.Align.CENTER);
		paintText.setTextSize(15);

		paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBack.setStyle(Paint.Style.FILL);
		paintBack.setStrokeCap(Paint.Cap.ROUND);
		
		paintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintShadow.setStyle(Paint.Style.FILL);
		paintShadow.setStrokeCap(Paint.Cap.ROUND);
		
		paintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintOutline.setStyle(Paint.Style.STROKE);
		paintOutline.setStrokeWidth(2);
		paintOutline.setStrokeCap(Paint.Cap.ROUND);
		
		paintOutlineEnabled = new Paint(paintOutline);
		setRectColor();
	}
	
	public void setCoordinate(int longtitude, int latitude, int floor, String buildID, String pinName){
		this.longtitude = longtitude;
		this.latitude = latitude;
		this.floor = floor;
		this.buildID = buildID;
		
		if(null == pinName || 0 == pinName.length()){
			this.pinName = "("+latitude/1E6+", "+longtitude/1E6+")";
		}else{
			this.pinName = pinName;
		}
	}
	
	public void setDrawable(int mode){
		if(MODE_PIN_DISABLE == mode){
			drawable = context.getResources().getDrawable(R.drawable.ic_point_custom_pin_disable);
		}else if(MODE_PIN == mode){
			drawable = context.getResources().getDrawable(R.drawable.ic_point_custom_pin);
		}else if(MODE_PIN_SHADOW == mode){
			drawable = context.getResources().getDrawable(R.drawable.ic_point_custom_pin_shadow);
		}else if(MODE_START == mode){
			drawable = context.getResources().getDrawable(R.drawable.ic_point_start_pin);
		}else if(MODE_END == mode){
			drawable = context.getResources().getDrawable(R.drawable.ic_point_end_pin);
		}
	}

	public void draw(Canvas canvas, boolean dialog) {
		
		//coordinate transform
		shadowLeft = IndoorDrawView.longitudeToScreenX(longtitude);
		shadowTop = IndoorDrawView.latitudeToScreenY(latitude) - ICON_SHADOW_HEIGHT;
		shadowRight = IndoorDrawView.longitudeToScreenX(longtitude) + ICON_SHADOW_WIDTH;
		shadowBootom = IndoorDrawView.latitudeToScreenY(latitude);
		iconLeft = IndoorDrawView.longitudeToScreenX(longtitude)- ICON_WIDTH/2;
		iconTop = IndoorDrawView.latitudeToScreenY(latitude)- ICON_HEIGHT;
		iconRight = IndoorDrawView.longitudeToScreenX(longtitude) + ICON_WIDTH/2;
		iconBootom = IndoorDrawView.latitudeToScreenY(latitude);
		textX = IndoorDrawView.longitudeToScreenX(longtitude);
		textY = IndoorDrawView.latitudeToScreenY(latitude)- TEXT_SHIFHT;
		if(MODE_PIN == mode){
			if(false == animateDone && IndoorDrawView.latitudeToScreenY(latitude) > (iconBootom - ANIMATE_SHIFT)){	
				setDrawable(MODE_PIN_DISABLE);
				drawable.setBounds((int)iconLeft, (int)(iconTop-ANIMATE_SHIFT), (int)iconRight, (int)(iconBootom-ANIMATE_SHIFT));
				drawable.draw(canvas);
				ANIMATE_SHIFT = ANIMATE_SHIFT - ANIMATE_SHIFT_OFFSET;
			}else{
				drawIcon(canvas, mode, dialog);
				
				ANIMATE_SHIFT = ANIMATE_SHIFT_START;
				setDrawable(MODE_PIN_DISABLE);
				animateDone = true;
			}
		}else if(MODE_START == mode){
			drawIcon(canvas, mode, dialog);
		}else if(MODE_END == mode){
			drawIcon(canvas, mode, dialog);
		}
		
		
	}
	
	private void drawIcon(Canvas canvas, int mode, boolean dialog){
		setDrawable(MODE_PIN_SHADOW);
		drawable.setBounds((int)shadowLeft-2, (int)shadowTop, (int)shadowRight-2, (int)shadowBootom);
		drawable.draw(canvas);
		
		setDrawable(mode);
		drawable.setBounds((int)iconLeft, (int)iconTop, (int)iconRight, (int)iconBootom);
		drawable.draw(canvas);
		/*
		if(shadow)  
			drawable.setColorFilter(0x7F000000, PorterDuff.Mode.SRC_IN);  
		canvas.save();  
		canvas.translate(0, 0);  
		if(shadow){  
			canvas.skew(-0.9F, 0.0F);  
			canvas.scale(1.0F, 0.5F);  
		}  
		drawable.draw(canvas);  
		
		if (shadow)  
			drawable.clearColorFilter();  
		canvas.restore();
		*/
		if(true == dialog){
			setRect();			
			canvas.drawRoundRect(backRect, BACK_ROUND_RECT , BACK_ROUND_RECT, paintBack);
			canvas.drawRoundRect(outlineRect, OUTLINE_ROUND_RECT, OUTLINE_ROUND_RECT, paintOutline);
			canvas.drawText(pinName, textX, textY, paintText);
		}
	}
	
	private void setRect(){
		paintText.getTextBounds(this.pinName, 0, this.pinName.length(), textRect);
		
		outlineRect = new RectF(textX-textRect.width()/2 - ORECT_OFFSET_LEFT,
								textY-textRect.height() - ORECT_OFFSET_TOP,
								textX+textRect.width()/2 + ORECT_OFFSET_RIGHT,
								textY + ORECT_OFFSET_BOTTOM);
		
		backRect = new RectF(textX-textRect.width()/2-BRECT_OFFSET_LEFT,
							textY-textRect.height()-BRECT_OFFSET_TOP,
							textX+textRect.width()/2+BRECT_OFFSET_RIGHT,
							textY+BRECT_OFFSET_BOTTOM);
	}
	
	private void setRectColor(){
		if(mode != MODE_PIN){
			// Setup theme as display only
			paintText.setColor(0xFFBBBBBB);
			paintBack.setColor(0xBB000000);
			paintShadow.setColor(0x00000000);
			paintOutline.setColor(0xBB666666);
			paintOutlineEnabled.setColor(0xBB666666);
		}else{	
			// Setup theme as clickable
			paintText.setColor(0xFFEEEEEE);
			paintBack.setColor(0xBB000000);
			paintShadow.setColor(0x00000000);
			paintOutline.setColor(0xFF999999);
			paintOutlineEnabled.setColor(0xFFBBBBBB);
		}
	}
	
	public RectF getBackRect(){
		if(null != backRect){
			return backRect;
		}else {
			RectF rectF = new RectF(0,0,0,0);
			return rectF;
		}
	}
	
	public boolean checkClickEvent(float x, float y){
		if(x >= iconLeft && y >= iconTop){
			if(x <= iconRight && y <= iconBootom){
				return true;
			}
		}
		return false;
	}
	
	public int getIntLontitude(){
		return longtitude;
	}
	
	public int getIntLatitude(){
		return latitude;
	}
	
	public int getFloor(){
		return floor;
	}
	
	public String getBuildID(){
		return buildID;
	}
	
	public String getName(){
		return pinName;
	}
}
