package com.iiiesti.walkmap.indoor;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.iiiesti.walkmap.R;

public class PoiPoint {
	/*This for debug*/
	private static final String TAG = "Poi" ;
	
	private static final int ICON_WIDTH = 24;
	private static final int ICON_HEIGHT = 24;
	
	private static final int ORECT_OFFSET_LEFT = 5;
	private static final int ORECT_OFFSET_TOP = 4;
	private static final int ORECT_OFFSET_RIGHT = 5;
	private static final int ORECT_OFFSET_BOTTOM = 6;
	private static final int BORDER_WIDTH = 2;
	private static final int BRECT_OFFSET_LEFT = ORECT_OFFSET_LEFT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_TOP = ORECT_OFFSET_TOP + BORDER_WIDTH;
	private static final int BRECT_OFFSET_RIGHT = ORECT_OFFSET_RIGHT + BORDER_WIDTH;
	private static final int BRECT_OFFSET_BOTTOM = ORECT_OFFSET_BOTTOM + BORDER_WIDTH;
	private static final int TEXT_SHIFHT = ICON_HEIGHT/2 + 10;
	private static final float BACK_ROUND_RECT = 6;
	private static final float OUTLINE_ROUND_RECT = 5;
	
	public static final int COLUMN_NUMBER_LATITUDE = 0;
	public static final int COLUMN_NUMBER_LONGTITUDE = 1;
	public static final int COLUMN_NUMBER_FLOOR = 2;
	public static final int COLUMN_NUMBER_CATALOG = 3;
	public static final int COLUMN_NUMBER_ORG_ID = 4;
	public static final int COLUMN_NUMBER_BUILD_ID = 5;
	public static final int COLUMN_NUMBER_POI_NAME = 6; 
	
	private float iconLeft;
	private float iconTop;
	private float iconRight;
	private float iconBootom;
	private float textX;
	private float textY;
	private float clickX;
	private float clickY;
	
	private Paint paintBack;
	private Paint paintOutline;
	private Paint paintText;
	private Paint paintShadow;
	private Paint paintOutlineEnabled;
	private Paint paintIcon = new Paint();
	
	private Rect textRect;
	private RectF backRect;
	private RectF outlineRect;
	
	private Resources res;
	private int floor;
	private String column[];
	
	private int clickPoiId = -1;
	public boolean clickFlag = false;
	
	public PoiPoint(Resources res) {
		this.res = res;
		
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
	
	public void draw(Canvas canvas, int floor) {
		this.floor = floor;
		//Log.d(TAG, IndoorMap.indoorPoi);
		
		if(0 != IndoorMap.indoorPoiAmount && true == IndoorMap.indoorPOIReadyFlag){		
			float x = -1;
			float y = -1;
			String poiname = "";
			int poiId = -1;
			for(int i=0; i<IndoorMap.indoorPoiAmount; i++){
				column = (IndoorMap.poi)[i].split(",");
	
				float iconX = IndoorDrawView.longitudeToScreenX(Integer.parseInt(column[1])) - ICON_WIDTH/2;
				float iconY = IndoorDrawView.latitudeToScreenY(Integer.parseInt(column[0])) - ICON_HEIGHT/2;
				int poifloor = Integer.parseInt(column[COLUMN_NUMBER_FLOOR]);
				String catalog = column[COLUMN_NUMBER_CATALOG];
				
				
				
				if(poifloor == this.floor){
					if(-ICON_WIDTH < iconX && IndoorMap.displayWidth > iconX && -ICON_HEIGHT < iconY && IndoorMap.displayHeight > iconY){
						boolean flagDrawRect = false;
						if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("nursery")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_baby), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[1] && catalog.equals("elevator")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_elevator), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[2] && catalog.equals("escalator")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_escalator), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[4] && catalog.equals("exit")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_exit), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("gate")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_gate), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("info")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_info), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[7] && catalog.equals("platform")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_platform), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[3] && catalog.equals("stair")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_stair), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("ticket")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_ticket), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}else if(true == IndoorMap.indoorPOIStatus[5] && catalog.equals("toilet")){
							canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.idr_toilet), iconX, iconY, paintIcon);
							flagDrawRect = true;
							//drawRect(canvas, poiname, i, x, y);
						}
						
						if(true == flagDrawRect && clickPoiId == i){
							x = IndoorDrawView.longitudeToScreenX(Integer.parseInt(column[COLUMN_NUMBER_LONGTITUDE]));
							y = IndoorDrawView.latitudeToScreenY(Integer.parseInt(column[COLUMN_NUMBER_LATITUDE]));
							poiname = column[COLUMN_NUMBER_POI_NAME];
							poiId = i;
						}
					}	
				}
			}
			
			drawRect(canvas, poiname, poiId, x, y);
			
			
		}
	}
	
	private void setRect(String poiname, float x, float y){
		textX = x;
		textY = y - TEXT_SHIFHT;
		paintText.getTextBounds(poiname, 0, poiname.length(), textRect);
		
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
		
		//if(mode != MODE_PIN){
			// Setup theme as display only
			paintText.setColor(0xFFBBBBBB);
			paintBack.setColor(0xBB000000);
			paintShadow.setColor(0x00000000);
			paintOutline.setColor(0xBB666666);
			paintOutlineEnabled.setColor(0xBB666666);
		/*
		}else{	
			 Setup theme as clickable
				paintText.setColor(0xFFEEEEEE);
				paintBack.setColor(0xBB000000);
				paintShadow.setColor(0x00000000);
				paintOutline.setColor(0xFF999999);
				paintOutlineEnabled.setColor(0xFFBBBBBB);
		}*/
	}
	
	public boolean checkClickEvent(float clickX, float clickY){
		this.clickX = clickX;
		this.clickY = clickY;
		
		if(0 != IndoorMap.indoorPoiAmount && true == IndoorMap.indoorPOIReadyFlag){
			Log.d(TAG,"xxxxxxxxxxxxx:"+IndoorMap.indoorPoiAmount);
			for(int i=0; i<IndoorMap.indoorPoiAmount; i++){
				column = (IndoorMap.poi)[i].split(",");
				
				float x = IndoorDrawView.longitudeToScreenX(Integer.parseInt(column[COLUMN_NUMBER_LONGTITUDE]));
				float y = IndoorDrawView.latitudeToScreenY(Integer.parseInt(column[COLUMN_NUMBER_LATITUDE]));
				
				iconLeft = x - ICON_WIDTH/2;
				iconTop = y - ICON_HEIGHT/2;
				iconRight = x + ICON_WIDTH/2;
				iconBootom = y + ICON_HEIGHT/2;
				
				int poifloor = Integer.parseInt(column[COLUMN_NUMBER_FLOOR]);
				String catalog = column[COLUMN_NUMBER_CATALOG];
//				String poiname = column[COLUMN_NUMBER_POI_NAME];	// Tony COMMENTED @ 1st merge
				
				if(poifloor == this.floor){
					if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("nursery")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[1] && catalog.equals("elevator")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[2] && catalog.equals("escalator")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[4] && catalog.equals("exit")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("gate")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("info")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[7] && catalog.equals("platform")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[3] && catalog.equals("stair")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("ticket")){
						if(hitClickEvent(i)){
							return true;
						}
					}else if(true == IndoorMap.indoorPOIStatus[5] && catalog.equals("toilet")){
						if(hitClickEvent(i)){							
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean hitClickEvent(int i){
		if(clickX >= iconLeft && clickY >= iconTop){
			if(clickX <= iconRight && clickY <= iconBootom){
				clickPoiId = i;
				clickFlag = true;
				return true;
			}
		}
		return false;
	}
	
	private void drawRect(Canvas canvas, String poiname, int poiID, float x, float y){
		
		if(poiID == clickPoiId && true == clickFlag){
			Log.d(TAG, "click!!! "+clickPoiId);
			setRect(poiname, x, y);
			canvas.drawRoundRect(backRect, BACK_ROUND_RECT , BACK_ROUND_RECT, paintBack);
			canvas.drawRoundRect(outlineRect, OUTLINE_ROUND_RECT, OUTLINE_ROUND_RECT, paintOutline);
			canvas.drawText(poiname, textX, textY, paintText);
		}
	}
}
