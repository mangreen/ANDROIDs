package com.iiiesti.walkmap.indoor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel_CUSTOM;

public class IndoorDrawView extends View{
	/*This for debug*/
	private static final String TAG = "Draw" ;
	
	private Context context;
	
	private int dialogOwner = PinPoint.MODE_CLEAR;
	
	/*set bitmap topleft and downright point*/
	//private static BitMapPoint topleft;
	//private static BitMapPoint bottomright;
	private BitMapPoint pressPoint;
	private String pressTittle;
	public boolean pressFlag = false;
	private RectF clickInfoRect;
	
	private int floor;
	private String buildID;
	private String floorname;
	
	public PinPoint pin;
	public PinPoint pinStart;
	public PinPoint pinEnd;
	
	private Resources res = getResources();
	private PoiPoint poi;
	/*
	private String poi[];
	private int drawable[]={R.drawable.idr_baby,
							R.drawable.idr_elevator,
							R.drawable.idr_escalator,
							R.drawable.idr_exit,
							R.drawable.idr_info,
							R.drawable.idr_person, 
							R.drawable.idr_stair,
							R.drawable.idr_toilet,
							};
	*/
	public IndoorDrawView(Context context) {
		super(context);
		this.context = context;
		pin = new PinPoint(context, PinPoint.MODE_PIN);
		pinStart = new PinPoint(context, PinPoint.MODE_START);
		pinEnd = new PinPoint(context, PinPoint.MODE_END);
		poi = new PoiPoint(res);
	}
	/*
	public void setAnchorPoint(BitMapPoint topleft, BitMapPoint downright){
		this.topleft = topleft;
		this.bottomright = downright;
	}
	
	public void setAnchorPoint(BitMapPoint topleft, BitMapPoint downright, int floor, String buildID, String floorname){
		this.topleft = topleft;
		this.bottomright = downright;
		this.floor = floor;
		this.buildID = buildID;
		this.floorname = floorname;
	}*/
	public void setAnchorPoint(int floor, String buildID, String floorname){
		this.floor = floor;
		this.buildID = buildID;
		this.floorname = floorname;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		/*
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		//paint.setShadowLayer(5, 3, 0, 0xFFFF00FF);// set shadow(柔邊, X 軸位移, Y 軸位移, 陰影顏色)
		
		float px = longitudeToScreenX(121554277);
		float py = latitudeToScreenY(25058938);
		
		float bx = longitudeToScreenX(121554969);
		float by = latitudeToScreenY(25058447);

		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_person), px, py, paint);
		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_baby), bx, by, paint);
		
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLUE);
		*/
		//drawPoi(paint, canvas);
		
		
		if(dialogOwner == PinPoint.MODE_POI){
			poi.clickFlag = true;
		}else{
			poi.clickFlag = false;
		}
		poi.draw(canvas, floor);
		if(true == pressFlag){
			pin.setCoordinate(pressPoint.getLongitude(), pressPoint.getLatitude(), floor, buildID, pressTittle);
			pin.draw(canvas, true);
			pin.pinFlag = true;
			pressFlag = false;
			
		}else if(true == pin.pinFlag){
			if(floor == pin.getFloor() && buildID.equals(pin.getBuildID())){
				if(dialogOwner == PinPoint.MODE_PIN){
					pin.draw(canvas, true);
				}else{
					pin.draw(canvas, false);
				}
			}
		}
		
		if(false == pin.animateDone){	
			this.invalidate();
		}
		
		if(true == pinStart.pinFlag){
			if(floor == pinStart.getFloor() && buildID.equals(pinStart.getBuildID())){
				if(dialogOwner == PinPoint.MODE_START){
					pinStart.draw(canvas, true);
				}else{
					pinStart.draw(canvas, false);
				}
			}
		}
		
		if(true == pinEnd.pinFlag){
			if(floor == pinEnd.getFloor() && buildID.equals(pinEnd.getBuildID())){
				if(dialogOwner == PinPoint.MODE_END){
					pinEnd.draw(canvas, true);
				}else{
					pinEnd.draw(canvas, false);
				}
			}
		}
		//Log.d(TAG, "top(" + topleft.x + ", " + topleft.y +"), down(" + downright.x + ", " + downright.y);
		//Log.d(TAG, "person(" + px + ", " + py +"), baby(" + bx + ", " + by);
	}

	public void setPressPoint(float x, float y){
		pressPoint = new BitMapPoint(screenXToLongitude(x), screenYToLatitude(y), x, y);
		pressTittle = null;
		pressFlag = true;
		pin.animateDone = false;
		dialogOwner = PinPoint.MODE_PIN;
		this.invalidate();
	}
	
	public void setPressPoint(int longtitude, int latitude, String tittle){
		pressPoint = new BitMapPoint(longtitude, latitude, longitudeToScreenX(longtitude), latitudeToScreenY(latitude));
		pressTittle = tittle;
		pressFlag = true;
		pin.animateDone = false;
		dialogOwner = PinPoint.MODE_PIN;
		this.invalidate();
	}
	
	public void checkClickPoint(float x, float y){
		
		if(dialogOwner  == PinPoint.MODE_PIN){
			clickInfoRect = pin.getBackRect();
			
			if(x >= clickInfoRect.left && y >= clickInfoRect.top){
				if(x <= clickInfoRect.right && y <= clickInfoRect.bottom){
					try{
						Bundle bundle = new Bundle();
						bundle.putBoolean(PointsDetailPanel.BUNDLE_IS_CUSTOM, true);
						bundle.putString(PointsDetailPanel.BUNDLE_TITLE, context.getString(R.string.custom_point));
						bundle.putString(PointsDetailPanel.BUNDLE_INFO, floorname+" "+pin.getName());
						bundle.putDouble(PointsDetailPanel.BUNDLE_LAT, (double)(pin.getIntLatitude() / 1E6));
						bundle.putDouble(PointsDetailPanel.BUNDLE_LON, (double)(pin.getIntLontitude() / 1E6));
						bundle.putInt(PointsDetailPanel.BUNDLE_FLOOR, floor);
						bundle.putString(PointsDetailPanel.BUNDLE_BUILD_ID, buildID);
	
						((Activity)context).startActivityForResult(new Intent(context, PointsDetailPanel_CUSTOM.class).putExtras(bundle), IndoorMap.REQUEST_POINT_DETAIL);	
						//((Activity)context).startActivity(new Intent(context, PointsDetailPanel_CUSTOM.class).putExtras(bundle));
					}catch (Exception e){
						Log.e(TAG, e.getMessage());
					}
				}
			}
		}
		if(poi.checkClickEvent(x, y)){
			dialogOwner = PinPoint.MODE_POI;
			this.invalidate();
		}else if(true == pin.checkClickEvent(x, y) && true == pin.pinFlag){
			dialogOwner = PinPoint.MODE_PIN;
			this.invalidate();
		}else if(true == pinStart.checkClickEvent(x, y) && true == pinStart.pinFlag){
			dialogOwner = PinPoint.MODE_START;
			this.invalidate();
		}else if(true == pinEnd.checkClickEvent(x, y) && true == pinEnd.pinFlag){
			dialogOwner = PinPoint.MODE_END;
			this.invalidate();
		}else{
			dialogOwner = PinPoint.MODE_CLEAR;
			this.invalidate();
		}
	}
	
	public void setStartPoint(){
		pinStart.setCoordinate(pin.getIntLontitude(), pin.getIntLatitude(), pin.getFloor(), pin.getBuildID(), pin.getName());
		pin.pinFlag = false;
		pinStart.pinFlag = true;
		dialogOwner = PinPoint.MODE_START;
		this.invalidate();
	}
	
	public void setEndPoint(){
		pinEnd.setCoordinate(pin.getIntLontitude(), pin.getIntLatitude(), pin.getFloor(), pin.getBuildID(), pin.getName());
		pin.pinFlag = false;
		pinEnd.pinFlag = true;
		dialogOwner = PinPoint.MODE_END;
		this.invalidate();
	}
	
	public void hide(){
		pin.pinFlag = false;
		pinStart.pinFlag = false;
		pinEnd.pinFlag = false;
		dialogOwner = PinPoint.MODE_CLEAR;
	}
	
	public void setStartPoint(GeoPoint location, String buildID, int floorID, String popupText)	// Tony ADDED @ 20110113
	{
		if(buildID != null && floorID != 0)
		{
			pinStart.setCoordinate(location.getLongitudeE6(), 
					location.getLatitudeE6(), 
					floorID, buildID, popupText);
			pinStart.pinFlag = true;
			this.invalidate();
		}
	}
	
	public void setEndPoint(GeoPoint location, String buildID, int floorID, String popupText)	// Tony ADDED @ 20110113
	{
		if(buildID != null && floorID != 0)
		{
			pinEnd.setCoordinate(location.getLongitudeE6(), 
					location.getLatitudeE6(), 
					floorID, buildID, popupText);
			pinEnd.pinFlag = true;
			this.invalidate();
		}
	}
	
	public void hideStartPoint()		// Tony ADDED @ 20110113
	{
		pinStart.pinFlag = false;
		this.invalidate();
	}
	
	public void hideEndPoint()			// Tony ADDED @ 20110113
	{
		pinEnd.pinFlag = false;
		this.invalidate();
	}
	
	public void popupStartPoint()		// Tony ADDED @ 20110113
	{
		pinStart.pinFlag = true;
		dialogOwner = PinPoint.MODE_START;
		this.invalidate();
	}
	
	public void popupEndPoint()			// Tony ADDED @ 20110113
	{
		pinEnd.pinFlag = true;
		dialogOwner = PinPoint.MODE_END;
		this.invalidate();
	}
	
	public void hidePopup()				// Tony ADDED @ 20100113
	{
		dialogOwner = PinPoint.MODE_CLEAR;
	}
	
	/*
	private void drawPoi(Paint paint, Canvas canvas){
		
		Log.d(TAG, IndoorMap.indoorPoi);
	
		if(0 != IndoorMap.indoorPoiAmount){
			poi = IndoorMap.indoorPoi.split(";");
			
			for(int i=0; i<IndoorMap.indoorPoiAmount; i++){
				String column[] = poi[i].split(",");
				
				float x = longitudeToScreenX(Integer.parseInt(column[1]));
				float y = latitudeToScreenY(Integer.parseInt(column[0]));
				int floor = Integer.parseInt(column[2]);
				String catalog = column[3];	
				
				if(floor == this.floor){

					if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("nursery")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_baby), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[1] && catalog.equals("elevator")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_elevator), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[2] && catalog.equals("escalator")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_escalator), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[4] && catalog.equals("exit")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_exit), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("gate")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_gate), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("info")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_info), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[7] && catalog.equals("platform")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_platform), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[3] && catalog.equals("stair")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_stair), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[6] && catalog.equals("ticket")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_ticket), x, y, paint);
					}else if(true == IndoorMap.indoorPOIStatus[5] && catalog.equals("toilet")){
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idr_toilet), x, y, paint);
					}
				}
			}
		}
		
	}*/
	
	public static Point toPixel(GeoPoint location)	// Tony ADDED @ 20110113
	{
		return new Point((int)longitudeToScreenX(location.getLongitudeE6()), (int)latitudeToScreenY(location.getLatitudeE6()));
	}
	
	public static float latitudeToScreenY(int lat){
		float y = (IndoorMap.bottomright.y - IndoorMap.topleft.y)/(IndoorMap.bottomright.getLatitude() - IndoorMap.topleft.getLatitude())*(lat - IndoorMap.topleft.getLatitude()) + IndoorMap.topleft.y;
		return y;
	}
	
	public static float longitudeToScreenX(int lon){
		float x = (IndoorMap.bottomright.x - IndoorMap.topleft.x)/(IndoorMap.bottomright.getLongitude() - IndoorMap.topleft.getLongitude())*(lon - IndoorMap.topleft.getLongitude()) + IndoorMap.topleft.x;
		return x;
	}
	
	public static int screenXToLongitude(float x){
		int lon = (int)((IndoorMap.bottomright.getLongitude() - IndoorMap.topleft.getLongitude())*(x - IndoorMap.topleft.x)/(IndoorMap.bottomright.x - IndoorMap.topleft.x) + IndoorMap.topleft.getLongitude());
		return lon;
	}
	
	public static int screenYToLatitude(float y){
		int lat = (int)((IndoorMap.bottomright.getLatitude() - IndoorMap.topleft.getLatitude())*(y - IndoorMap.topleft.y)/(IndoorMap.bottomright.y - IndoorMap.topleft.y) + IndoorMap.topleft.getLatitude());
		return lat;
	}
}
