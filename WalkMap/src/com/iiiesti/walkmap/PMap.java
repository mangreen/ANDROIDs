package com.iiiesti.walkmap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView.ReticleDrawMode;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.iiiesti.walkmap.customPanel.BookmarkDialog;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel;
import com.iiiesti.walkmap.customType.MT_Constant;
import com.iiiesti.walkmap.customType.MT_SimpleStop;
import com.iiiesti.walkmap.customType.POI_SimplePoint;
import com.iiiesti.walkmap.database.BookmarkDBManager;
import com.iiiesti.walkmap.database.MassTransitDBManager;
import com.iiiesti.walkmap.direction.DirectionDisplay;
import com.iiiesti.walkmap.direction.DirectionManager;
import com.iiiesti.walkmap.indoor.IndoorMap;
import com.iiiesti.walkmap.overlays.ItemizedPointsOverlay;
import com.iiiesti.walkmap.overlays.ItemizedPointsOverlay_CUSTOM;
import com.iiiesti.walkmap.overlays.ItemizedPointsOverlay_NOOP;
import com.iiiesti.walkmap.overlays.ItemizedPointsOverlay_POI;
import com.iiiesti.walkmap.overlays.OverlayItem_MT;
import com.iiiesti.walkmap.overlays.OverlayItem_POI;
import com.iiiesti.walkmap.overlays.Overlay_Location;
import com.iiiesti.walkmap.overlays.Overlay_Popup;
import com.iiiesti.walkmap.searchable.OutdoorLocationSearch;

public class PMap extends MapActivity{
	
/** Tool function */
	
	/*
	 * Executor which run the submitted task immediately in the caller's thread
	 */
	public static class DirectExecutor implements Executor {
	     public void execute(Runnable r) {
	         r.run();
	     }
	 }
	
	/*
	 * Simply calculate the insight distance between two points
	 */
	public static final float calMeter(long latS, long lonS, long latE, long lonE)
	{
		// [NOTE] Input unit: 10cm
		return (float)(Math.sqrt((latS - latE)*(latS - latE) + (lonS - lonE)*(lonS - lonE)) / 10);
	}
	
	/*
	 * Simply get rid off the digits after two decimal place
	 */
	public static final float tDecPlace(float val)
	{
		return ((int)(val * 100))/100.0f;
	}
	
	/*
	 * Adjusts a drawable's bounds so that (0,0) is a pixel in the center of the bottom row of the drawable.
	 * [NOTE] Should be improved in the future.
	 */
	public static final android.graphics.drawable.Drawable boundCenterBottom(android.graphics.drawable.Drawable balloon)
	{
		int mMarkerHalfWidth = balloon.getIntrinsicWidth() >> 1;
		balloon.setBounds(0 - mMarkerHalfWidth, 0 - balloon.getIntrinsicHeight(), mMarkerHalfWidth, 0);
		return balloon;
	}
	
	/** Data definition */
	
	private class BookmarkDialogForPMap extends BookmarkDialog {

		public BookmarkDialogForPMap(Context context,
				SimpleCursorAdapter bookmarkAdapter, Cursor bookmarkCursor,
				Overlay_Location locationOverlay, int operationMode,
				DirectionManager directionSetup) {
			super(context, bookmarkAdapter, bookmarkDB, bookmarkCursor, locationOverlay, operationMode,
					directionSetup);
		}

		@Override
		protected void mBookmarkOperation(int operationMode,
				int latE6, int lonE6, int addrLatE6, 
				int addrLonE6, String name,
				String info, String buildID, int floorID) {
			bookmarkOperation(operationMode, latE6, lonE6, addrLatE6, addrLonE6, name, info, buildID, floorID);
		}

		@Override
		protected void animateToForOutdoor(GeoPoint targetLocation) {
			mapCtrl.animateTo(targetLocation);
		}
		
	}
	
	private class DirectionDisplayForPMap extends DirectionDisplay {

		public DirectionDisplayForPMap(Context context) {
			super(context);
		}

		@Override
		protected Point projectToPixel(GeoPoint point) {
			return projection.toPixels(point, null);
		}
		
	}
	
	private class DirectionManagerForPMap extends DirectionManager {

		public DirectionManagerForPMap(Context context, View parentView,
				DirectionDisplay display, int displayPosition) {
			super(context, parentView, display, displayPosition);
		}

		@Override
		protected void clearStartPointDisplay() {
			clearOverlay(PMap.OVERLAY_START_PIN);
		}

		@Override
		protected void clearEndPointDisplay() {
			clearOverlay(PMap.OVERLAY_END_PIN);
		}
		
		@Override
		protected void setStartPointDisplay(GeoPoint location, String buildID, int floorID, String popupText) {
			overlayUpdateStartPin(location, popupText, "");
		}

		@Override
		protected void setEndPointDisplay(GeoPoint location, String buildID, int floorID, String popupText) {
			overlayUpdateEndPin(location, popupText, "");
		}

		@Override
		protected void hidePopup() {
	        if(popupOverlay != null)
	        	popupOverlay.hide();
		}

		@Override
		protected void popupStartPoint() {
			popupStartPin();
		}

		@Override
		protected void popupEndPoint() {
			popupEndPin();
		}

		@Override
		protected void moveMapTo(int latE6, int lonE6, String buildID, int floorID) {
			if(buildID == null || floorID == 0)
				mapCtrl.animateTo(new GeoPoint(latE6, lonE6));
			else
			{
				this.dataFinalize();
				Bundle bundle = new Bundle();
				bundle.putString(IndoorMap.IDR_BUNDLE_STR_BUILD_ID, buildID);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_FLOOR_ID, floorID);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_OP_CODE, IndoorMap.OP_CODE_MOVE_TO);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LAT, latE6);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LON, lonE6);
				startActivity(new Intent(PMap.this, IndoorMap.class).putExtras(bundle));
			}
		}

		@Override
		protected void openBookmarkForSetStart() {
			openBookmarkDialog(PMap.BOOKMARK_MODE_SET_START);
		}

		@Override
		protected void openBookmarkForSetEnd() {
			openBookmarkDialog(PMap.BOOKMARK_MODE_SET_END);
		}

		@Override
		protected void specialMoveToPoint(int pointType,
				GeoPoint location, String buildID, int floorID, String name,
				String info) {
			if(buildID == null || floorID == 0)
			{
				mapCtrl.animateTo(location);
				
				switch(pointType)
				{
				case DirectionManager.SP_TYPE_START_POINT:
					popupStartPin();
					break;
				case DirectionManager.SP_TYPE_END_POINT:
					popupEndPin();
					break;
				}
			}
			else
			{
				directionSetup.dataFinalize();
				Bundle bundle = new Bundle();
				bundle.putString(IndoorMap.IDR_BUNDLE_STR_BUILD_ID, buildID);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_FLOOR_ID, floorID);
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_OP_CODE, IndoorMap.OP_CODE_BOOKMARK);
				switch(pointType)
				{
				case DirectionManager.SP_TYPE_START_POINT:
					bundle.putInt(IndoorMap.IDR_BUNDLE_INT_BK_OP_MODE, BOOKMARK_MODE_SET_START);
					break;
				case DirectionManager.SP_TYPE_END_POINT:
					bundle.putInt(IndoorMap.IDR_BUNDLE_INT_BK_OP_MODE, BOOKMARK_MODE_SET_END);
					break;
				}
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LAT, location.getLatitudeE6());
				bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LON, location.getLongitudeE6());
				bundle.putString(IndoorMap.IDR_BUNDLE_STR_PNT_NAME, name);
				bundle.putString(IndoorMap.IDR_BUNDLE_STR_PNT_INFO, info);
				startActivity(new Intent(PMap.this, IndoorMap.class).putExtras(bundle));
			}
		}

		@Override
		protected String getDebugTag() {
			return "PMapDirection";
		}

	}
	
	private class PMapView extends com.google.android.maps.MapView {
		static final int STOP_THRESHOLD = 2;
		GeoPoint lastPoint;
		GeoPoint curPoint;
		int hitCount;
	
		public PMapView(Context context, String apiKey) {
			super(context, apiKey);
			
			lastPoint = curPoint = new GeoPoint(0, 0);
			hitCount = 0;
		}
		
		@Override
		public void draw(Canvas canvas) {
			
			// Determine the proper rectangle of the screen for updating the itemizedOverlays in "Layer"
			// when panning the map
			curPoint = mapView.getProjection().fromPixels(0, 0);
			if(itemizedOverlaysUpdateRequired)
			{
				if(lastPoint.getLatitudeE6() == curPoint.getLatitudeE6() && lastPoint.getLongitudeE6() == curPoint.getLongitudeE6())
				{
					hitCount++;
					if(hitCount == STOP_THRESHOLD)
					{
						hitCount = 0;
						itemizedOverlaysUpdateRequired = false;
						itemizedOverlaysUpdate();
					}
				}
				else
					hitCount = 0;
				
				invalidate();	// Forced speed up the testing frequency
			}
			lastPoint = curPoint;
	
			super.draw(canvas);
		}
	}
	
	/* Constants */
	private static final String D_TAG = "PMap";
	public static final String PREFNAME_DIRECTION = "DirectionPreference";
	public static final String PREFNAME_PINPOINT = "PinTheCustomPoint";		// [NOTE] Trick for receiving the result from location search
	public static final String INTENT_FILTER_MAP_VIEW = "geo";
	public static final String INTENT_FILTER_PIN_POINT = "pin";
	public static final String INTENT_FILTER_SHOW_PIN = "show_pin";
	
	public static final int MIN_ZOOM_LEVEL = 1;
	public static final int DEFAULT_ZOOM_LEVEL = 17;
	public static final int DEFAULT_DETAIL_LEVEL = DEFAULT_ZOOM_LEVEL;
	private static final int LO_MAP_MODE_1 = R.drawable.btn_r_lo_map_mode_1;
	private static final int LO_MAP_MODE_2 = R.drawable.btn_r_lo_map_mode_2;
	private static final int LO_MAP_MODE_3 = R.drawable.btn_r_lo_map_mode_3;
	private static final int L_MAP_MODE_1 = R.drawable.btn_r_l_map_mode_1;
	private static final int L_MAP_MODE_2 = R.drawable.btn_r_l_map_mode_2;
	private static final int O_MAP_MODE_1 = R.drawable.btn_r_o_map_mode_1;
	private static final int O_MAP_MODE_2 = R.drawable.btn_r_o_map_mode_2;
	private static final int MAP_MODE_NA = R.drawable.btn_r_map_mode_na;
	private static final int MOM_NA = 0;
	private static final int MOM_L = 1;
	private static final int MOM_O = 2;
	private static final int MOM_LO = 3;
	public static final int OVERLAY_END_PIN = -3;
	public static final int OVERLAY_START_PIN = -2;
	public static final int OVERLAY_CUSTOM_PIN = -1;
	public static final int OVERLAY_POI = 9999;
	public static final int OVERLAY_POI_MT_STOP = 0;
	public static final int OVERLAY_POI_EAT = 1;
	public static final int OVERLAY_POI_PLAY = 2;
	public static final int OVERLAY_POI_BUY = 3;
	public static final int OVERLAY_POI_HOTEL = 4;
	public static final int OVERLAY_POI_MRT = 5;
	public static final String DBFLAG_POI_EAT = "23";
	public static final String DBFLAG_POI_PLAY = "93";
	public static final String DBFLAG_POI_BUY = "5";
	public static final String DBFLAG_POI_HOTEL = "3";
	public static final String DBFLAG_POI_MRT = "800";
	private static final int MENUGROUP_MAIN = R.id.menu_group_main;
	private static final int MENUGROUP_DIRECTION_SETUP = R.id.menu_group_direction_setup;
	protected static final int MENUITEM_SEARCH = R.id.pmap_menu_search;
	protected static final int MENUITEM_DIRECTION = R.id.pmap_menu_direction;
	protected static final int MENUITEM_LAYER = R.id.pmap_menu_layer;
	protected static final int MENUITEM_BOOKMARK = R.id.pmap_menu_bookmark;
	protected static final int MENUITEM_CLEANMAP = R.id.pmap_menu_clean_map;
	protected static final int MENUITEM_ABOUT = R.id.pmap_menu_about;
	protected static final int MENUITEM_DIRECTION_QUIT = R.id.direction_setup_exit_direction;
	protected static final int MENUITEM_DIRECTION_REVERSE = R.id.direction_setup_menu_reverse;
	protected static final int MENUITEM_DIRECTION_CLEAR = R.id.direction_setup_menu_clear;
	protected static final int MENUITEM_DIRECTION_MT_OPTION = R.id.direction_setup_menu_mass_opt;
	protected static final int MENUITEM_DIRECTION_ROUTE_LIST = R.id.direction_setup_menu_route_list;
	public static final String INTENT_INT_1 = "intent_key_integer_1";
	public static final int REQUEST_POINT_DETAIL = 1;
	public static final int RESULT_PDETAIL_HAS_CENTER = 1;
	public static final int RESULT_PDETAIL_CLEAR_CUSTOMPIN_OVERLAY = 2;
	public static final int RESULT_PDETAIL_ADD_BOOKMARK = 3;
	public static final int RESULT_PDETAIL_SET_START = 4;
	public static final int RESULT_PDETAIL_SET_END = 5;
	public static final int BOOKMARK_MODE_NORMAL = 1;
	public static final int BOOKMARK_MODE_SET_START = 2;
	public static final int BOOKMARK_MODE_SET_END = 3;
	public static final String PREFS_BOOL_PINPOINT = "customPoint";
	public static final String PREFS_INT_LATE6 = "pointLatE6";
	public static final String PREFS_INT_LONE6 = "pointLonE6";
	public static final String PREFS_STR_TITLE = "pointTitle";
	public static final String PREFS_STR_INFO = "pointInfo";
	public static final String PREFS_STR_BUILDING_ID = "buildingID";
	public static final String PREFS_INT_FLOOR_ID = "floorID";
	
	/* Current operation states */
	private int currentMapOpMode;			// Map operation mode: 0-Not available, 1-Location only, 2-Orientation only, 3-Full abilities
	private int currentMapMode;				// The map mode corresponds to current map-operation-mode
	private int currentLevel;
	private int maximalLevel;
	private boolean poiLayerStatus[]; 					// Maintains the current status of POI overlay
	private boolean transitAvoidStatus[]; 				// Maintains the current status of transit-avoidance
	private boolean itemizedOverlaysUpdateRequired;		// Flag for custom MapView to test on draw
	private boolean isActivating;						// [TRICK] Avoid duplicate tap when the onTap() is going to start another activity
	
	/* Others */
	private Handler handler = new Handler();
	
	/* View resources */
	private RelativeLayout layoutBase;		// Master viewGroup
	private PMapView mapView;				// Master MapView
	private Button btnZoomIn;				// Map-control
	private Button btnZoomOut;				// Map-control
	private Button btnMapMode;				// Map-control
	private DirectionManager directionSetup;		// Direction-setup set
	private DirectionDisplay directionDisplay;		// Direction-display for direction-setup
	
	/* MapView related objects */
	public MapController mapCtrl;
	public Projection projection;
	
	/* Overlays of mapView */
	private List<Overlay> overlayList;							// The overall overlay container of mapView
	
	private Overlay_Location locationOverlay;					// Current location
	public Overlay_Popup popupOverlay = null;					// Pop-up
	private ItemizedPointsOverlay_CUSTOM customPinOverlay;		// Custom pin, itemized overlay but use it as single overlay
	private ItemizedPointsOverlay directionStartOverlay;		// Direction: start location pin, itemized overlay but use it as single overlay
	private ItemizedPointsOverlay directionEndOverlay;			// Direction: end location pin, itemized overlay but use it as single overlay
	private ItemizedPointsOverlay poiMassTransitOverlay;		// POI overlay: stops of mass transit
	private ItemizedPointsOverlay poiEatOverlay;				// POI overlay: EAT POIs	[NOTE] There is a better way to manage these overlays
	private ItemizedPointsOverlay poiPlayOverlay;				// POI overlay: PLAY POIs
	private ItemizedPointsOverlay poiBuyOverlay;				// POI overlay: BUT POIs
	private ItemizedPointsOverlay poiHotelOverlay;				// POI overlay: HOTEL POIs
	private ItemizedPointsOverlay poiMrtOverlay;			// POI overlay: HOSPITAL POIs

	/* Create DB resources */
	// DB master
	private MassTransitDBManager massDB;
	private BookmarkDBManager bookmarkDB;
	
	// Related data adapter
	private Cursor bookmarkCursor;
	private SimpleCursorAdapter bookmarkAdapter;
		
	/** DB management */
	private class openMassDBTask extends AsyncTask<MassTransitDBManager, Void, Void>
	{
		@Override
		protected Void doInBackground(MassTransitDBManager... db) {
			try {
				Log.d(D_TAG, "openDBTask: Opening Mass-Transit data bases");
				db[0].open();
				db[0].setAvailable(true);
			} catch (SQLiteException e) {
				Log.e(D_TAG, e.getMessage());
			}
			return null;
		}
	}
	
	private class openBookmarkDBTask extends AsyncTask<BookmarkDBManager, Void, BookmarkDBManager>
	{
		@Override
		protected BookmarkDBManager doInBackground(BookmarkDBManager... db) {
			try {
				Log.d(D_TAG, "openDBTask: Opening Bookmark data bases");
				db[0].open();
				db[0].setAvailable(true);
				return db[0];
			} catch (SQLiteException e) {
				Log.e(D_TAG, e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(BookmarkDBManager db) {
			if(db != null)
			{
				bookmarkCursor = db.SPOINT_select();
				
				if(bookmarkCursor != null)
				{
					startManagingCursor(bookmarkCursor);
					
					bookmarkAdapter = new SimpleCursorAdapter(PMap.this, 
							R.layout.simple_info, 
							bookmarkCursor, 
							new String[] {BookmarkDBManager.SPOINT_FIELD_NAME, BookmarkDBManager.SPOINT_FIELD_INFO}, 
							new int[] {R.id.simple_info_title, R.id.simple_info_info});					
				}
			}
			super.onPostExecute(db);
		}
	}

	private void initDB()
	{
		Log.d(D_TAG, "initDB");
		
		bookmarkCursor = null;
		bookmarkAdapter = null;
		
        massDB = new MassTransitDBManager(PMap.this);
        bookmarkDB = new BookmarkDBManager(PMap.this);
        
        new openMassDBTask().execute(massDB);
        new openBookmarkDBTask().execute(bookmarkDB);
	}
	
	private void closeDB()
	{
		Log.d(D_TAG, "closeDB");
		if(massDB.availability())
			massDB.close();
		
		if(bookmarkDB.availability())
			bookmarkDB.close();
	}
	
	/** Overlay management */
	private void overlayInit()
	{
		poiLayerStatus = new boolean[PMap.this.getResources().getStringArray(R.array.layer_menu).length];
		Arrays.fill(poiLayerStatus, false);

		// Overlay of the current location
		locationOverlay = new Overlay_Location(this, mapView);
		
		// Overlay of other information
		popupOverlay = new Overlay_Popup(this, mapView);
		
		// itemizedOverlay of other information
		customPinOverlay = new ItemizedPointsOverlay_CUSTOM(PMap.this, mapView, locationOverlay);
		directionStartOverlay = new ItemizedPointsOverlay_NOOP(PMap.this, mapView, mapView.getResources().getDrawable(R.drawable.ic_point_start_pin), true);
		directionEndOverlay = new ItemizedPointsOverlay_NOOP(PMap.this, mapView, mapView.getResources().getDrawable(R.drawable.ic_point_end_pin), true);
		poiMassTransitOverlay = new ItemizedPointsOverlay_NOOP(PMap.this, mapView, mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_bus), false);
		poiEatOverlay = new ItemizedPointsOverlay_POI(PMap.this, mapView, locationOverlay);
		poiPlayOverlay = new ItemizedPointsOverlay_POI(PMap.this, mapView, locationOverlay);
		poiBuyOverlay = new ItemizedPointsOverlay_POI(PMap.this, mapView, locationOverlay);
		poiHotelOverlay = new ItemizedPointsOverlay_POI(PMap.this, mapView, locationOverlay);
		poiMrtOverlay = new ItemizedPointsOverlay_POI(PMap.this, mapView, locationOverlay);
		
		// Dispatch overlays [NOTE] itemizedOverlays will be dispatched when it has something been added at the first time

		overlayList.add(locationOverlay);
		overlayList.add(popupOverlay);
	}
	
	/* Update methods for single-pin itemizedOverlay */
	
	// [NOTE] Functions named with initial "overlayUpdate" is used to add
	// an item into the itemizedOverlay which contains single item only	
	public void overlayUpdateStartPin(GeoPoint geoPoint, String title, String info)
	{
		if(null != directionStartOverlay)
		{
			directionStartOverlay.removeAllPointsOverlay();
			directionStartOverlay.addPointsOverlay(new OverlayItem(geoPoint, title, info));
			
			if(overlayList.contains(directionStartOverlay))
				overlayList.remove(directionStartOverlay);
			
			overlayList.add(overlayList.indexOf(locationOverlay), directionStartOverlay);
			
			mapView.invalidate();
		}
	}
	
	public void overlayUpdateEndPin(GeoPoint geoPoint, String title, String info)
	{
		if(null != directionEndOverlay)
		{
			directionEndOverlay.removeAllPointsOverlay();
			directionEndOverlay.addPointsOverlay(new OverlayItem(geoPoint, title, info));
			
			if(overlayList.contains(directionEndOverlay))
				overlayList.remove(directionEndOverlay);
				
			overlayList.add(overlayList.indexOf(locationOverlay), directionEndOverlay);
			
			mapView.invalidate();
		}
	}
	
	public void overlayUpdateCustomPin(Point point)
	{
		if(null != customPinOverlay)
		{
			customPinOverlay.removeAllPointsOverlay();
			customPinOverlay.addPointsOverlayAnimated(new OverlayItem(projection.fromPixels(point.x, point.y), "", ""));
			
			if(overlayList.contains(customPinOverlay))
				overlayList.remove(customPinOverlay);
				
			overlayList.add(overlayList.indexOf(locationOverlay), customPinOverlay);
				
			mapView.invalidate();
		}
	}
	
	private void overlayUpdateCustomDataGeoPin(GeoPoint geoPoint, String name, String info, int addrLatE6, int addrLonE6, String buildID, int floorID)
	{
		if(null != customPinOverlay)
		{
			customPinOverlay.removeAllPointsOverlay();
			customPinOverlay.addPointsOverlayWithData(new OverlayItem(geoPoint, "", ""), name, info, addrLatE6, addrLonE6, buildID, floorID);
			
			if(!overlayList.contains(customPinOverlay))
				overlayList.add(overlayList.indexOf(locationOverlay), customPinOverlay);
				
			mapView.invalidate();
		}
	}
	
	/* Update methods for multiple-item itemizedOverlay */
	private void itemizedOverlaysUpdate()
	{
		Log.v(D_TAG, "itemizedOverlaysUpdate @ " + Integer.toString(currentLevel));
		
		if(currentLevel >= DEFAULT_DETAIL_LEVEL)
		{
			// All POI-oriented itemizedOverlays should be checked here
			// itemizedOverlaysUpdate() should be executed only when there are POI layers enabled
			for(int i = 0; i < poiLayerStatus.length; i++)
				switch(i)
				{
				case OVERLAY_POI_MT_STOP:
					if(poiLayerStatus[OVERLAY_POI_MT_STOP])
						new OverlayUpdateMassStopTask().execute(new Point(projection.fromPixels(-10, mapView.getHeight()+10).getLatitudeE6(), projection.fromPixels(-10, mapView.getHeight()+10).getLongitudeE6()), new Point(projection.fromPixels(mapView.getWidth()+10, -10).getLatitudeE6(), projection.fromPixels(mapView.getWidth()+10, -10).getLongitudeE6()));
					else
						clearOverlay(OVERLAY_POI_MT_STOP);
					break;
				case OVERLAY_POI_EAT:
					if(poiLayerStatus[OVERLAY_POI_EAT])
						new OverlayUpdateEatPoiTask().execute();
					else
						clearOverlay(OVERLAY_POI_EAT);
					break;
				case OVERLAY_POI_PLAY:
					if(poiLayerStatus[OVERLAY_POI_PLAY])
						new OverlayUpdatePlayPoiTask().execute();
					else
						clearOverlay(OVERLAY_POI_PLAY);
					break;
				case OVERLAY_POI_BUY:
					if(poiLayerStatus[OVERLAY_POI_BUY])
						new OverlayUpdateBuyPoiTask().execute();
					else
						clearOverlay(OVERLAY_POI_BUY);
					break;
				case OVERLAY_POI_HOTEL:
					if(poiLayerStatus[OVERLAY_POI_HOTEL])
						new OverlayUpdateHotelPoiTask().execute();
					else
						clearOverlay(OVERLAY_POI_HOTEL);
					break;
				case OVERLAY_POI_MRT:
					if(poiLayerStatus[OVERLAY_POI_MRT])
						new OverlayUpdateMrtPoiTask().execute();
					else
						clearOverlay(OVERLAY_POI_MRT);
					break;
				}
		}
		else
		{
			clearOverlay(OVERLAY_POI);
		}
	}
	
	private class OverlayUpdateEatPoiTask extends OverlayUpdateGeneralPoiTask
	{
		@Override
		protected ItemizedPointsOverlay getTargetOverlay() { return poiEatOverlay; }

		@Override
		protected String getPoiClassPrefix() { return DBFLAG_POI_EAT; }

		@Override
		protected int getPoiClassIdx() { return OVERLAY_POI_EAT; }
	}
	
	private class OverlayUpdatePlayPoiTask extends OverlayUpdateGeneralPoiTask
	{
		@Override
		protected ItemizedPointsOverlay getTargetOverlay() { return poiPlayOverlay; }

		@Override
		protected String getPoiClassPrefix() { return DBFLAG_POI_PLAY; }

		@Override
		protected int getPoiClassIdx() { return OVERLAY_POI_PLAY; }
	}
	
	private class OverlayUpdateBuyPoiTask extends OverlayUpdateGeneralPoiTask
	{
		@Override
		protected ItemizedPointsOverlay getTargetOverlay() { return poiBuyOverlay; }

		@Override
		protected String getPoiClassPrefix() { return DBFLAG_POI_BUY; }

		@Override
		protected int getPoiClassIdx() { return OVERLAY_POI_BUY; }
	}
	
	private class OverlayUpdateHotelPoiTask extends OverlayUpdateGeneralPoiTask
	{
		@Override
		protected ItemizedPointsOverlay getTargetOverlay() { return poiHotelOverlay; }

		@Override
		protected String getPoiClassPrefix() { return DBFLAG_POI_HOTEL; }

		@Override
		protected int getPoiClassIdx() { return OVERLAY_POI_HOTEL; }
	}
	
	private class OverlayUpdateMrtPoiTask extends OverlayUpdateGeneralPoiTask
	{
		@Override
		protected ItemizedPointsOverlay getTargetOverlay() { return poiMrtOverlay; }

		@Override
		protected String getPoiClassPrefix() { return DBFLAG_POI_MRT; }

		@Override
		protected int getPoiClassIdx() { return OVERLAY_POI_MRT; }
	}
	
	private abstract class OverlayUpdateGeneralPoiTask extends AsyncTask<Void, Void, String>
	{
		int lb_lat;
		int lb_lon;
		int rt_lat;
		int rt_lon;
		
		ItemizedPointsOverlay tOverlay;
		String poiClassPrefix;
		int poiClassIdx;
		
		protected abstract ItemizedPointsOverlay getTargetOverlay();
		protected abstract String getPoiClassPrefix();
		protected abstract int getPoiClassIdx();
		
		@Override
		protected void onPreExecute() {
			Log.v(D_TAG, "updateGeneralPoiTask");
			tOverlay = getTargetOverlay();
			poiClassPrefix = getPoiClassPrefix();
			poiClassIdx = getPoiClassIdx();
			lb_lat = projection.fromPixels(-10, mapView.getHeight()+10).getLatitudeE6();
			lb_lon = projection.fromPixels(-10, mapView.getHeight()+10).getLongitudeE6();
			rt_lat = projection.fromPixels(mapView.getWidth()+10, -10).getLatitudeE6();
			rt_lon = projection.fromPixels(mapView.getWidth()+10, -10).getLongitudeE6();
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... arg) {

			/** Search the local LBS server for POIs */
			String URI;
			String responseBody = null;
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, OutdoorLocationSearch.HTTP_TIMEOUT);
			// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, OutdoorLocationSearch.HTTP_SO_TIMEOUT);
	        HttpClient httpclient = new DefaultHttpClient(httpParameters);
	        
			try {
		        // Compose the request URI
		        // Generate the basic URI
		        URI = "http://" + OutdoorLocationSearch.SERVER_DOMAIN + ":8080/LBSServer/mainLBS.jsp?" + 
		        "lat_lb=" + lb_lat + 
		        "&lon_lb=" + lb_lon + 
		        "&lat_rt=" + rt_lat + 
		        "&lon_rt=" + rt_lon + 
		        "&ql=" + poiClassPrefix + ",";
		        
		        Log.d(D_TAG, URI);
				// Generate the HTTP-REQUEST
		        HttpGet httpget = new HttpGet(URI); 
		        
		        // Create a response handler
		        ResponseHandler<String> responseHandler = new BasicResponseHandler();
		        
		        // Search the local server for nearby POIs
				responseBody = httpclient.execute(httpget, responseHandler);
			} catch (ClientProtocolException e) {
				Log.e(D_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(D_TAG, e.getMessage());
			} catch (Exception e) {
				Log.e(D_TAG, e.getMessage());
			}
    		
	        // When HttpClient instance is no longer needed, 
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        httpclient.getConnectionManager().shutdown();
			return responseBody;
		}
		
		@Override
		protected void onPostExecute(String responseBody) {
			if(responseBody != null)
			{
				String tResultArray[];
				
				// Parse the search result (From mangreen's LBS)
				if(-1 != responseBody.indexOf("#")){	
					String str[] = responseBody.split("#");
					if(str[1].length()>1){
						String strbuf[] = str[1].split("@");
						
						if(Integer.parseInt(strbuf[0]) > 0 && strbuf[1].length()>1)
						{
							tOverlay.removeAllPointsOverlay();
							
							tResultArray = strbuf[1].split(";"); 
							for(String result : tResultArray)
							{
								String column[] = result.split(",");
								if(column.length == OutdoorLocationSearch.LBS_COLUMN_COUNT)
								{
									tOverlay.addPointsOverlayNonPopulate(new OverlayItem_POI(mapView, 
											new POI_SimplePoint(column[OutdoorLocationSearch.LBS_IDX_NAME], 
													column[OutdoorLocationSearch.LBS_IDX_ADDR], 
													Integer.parseInt(column[OutdoorLocationSearch.LBS_IDX_LAT]), 
													Integer.parseInt(column[OutdoorLocationSearch.LBS_IDX_LON]),
													column[OutdoorLocationSearch.LBS_IDX_BID],
													poiClassIdx)));
								}
							}
							Log.d(D_TAG, "General POI found: " + tResultArray.length);
							Log.d(D_TAG, "Add complete");
							tOverlay.doPopulate();
							
							if(!overlayList.contains(tOverlay))
								overlayList.add(overlayList.indexOf(locationOverlay), tOverlay);
							
							mapView.invalidate();
						}
						else
							Log.d(D_TAG, "Nothing to be added");
					}
				}	
			}
			super.onPostExecute(responseBody);
		}
	}
	
	private class OverlayUpdateMassStopTask extends AsyncTask<Point, Void, Cursor>
	{
		// [NOTE] Receive the simple stops information ArrayList
		// For each stop, add it into the MTOverlay with: 1. coordination, 2. name 3. _id 4. type
		// For each overlayItem's onClick(), use the _id to retrieve the detail information of this stop from certain CURSOR.
		// And fill the intent to start a PointsDetailPanel
		
		@Override
		protected void onPreExecute() {
			Log.v(D_TAG, "updateMassStopTask");
			
			super.onPreExecute();
		}
		
		@Override
		protected Cursor doInBackground(Point... pntArray) {
			return massDB.MT_getStopsByRect(pntArray[0], pntArray[1]);
		}
		
		@Override
		protected void onPostExecute(Cursor cursor) {
			if(cursor != null)
			{
				String typeString;
				
				Log.d(D_TAG, "Now add points:" + Integer.toString(cursor.getCount()));
				
				poiMassTransitOverlay.removeAllPointsOverlay();
				cursor.moveToFirst();
				while(!cursor.isAfterLast())
				{
					switch(cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_TYPE))
					{
					case MT_Constant.TYPE_BUS:
						typeString = "公車站: ";
						break;
					case MT_Constant.TYPE_HSR:
						typeString = "高鐵站: ";
						break;
					case MT_Constant.TYPE_MRT:
						typeString = "捷運站: ";
						break;
					case MT_Constant.TYPE_PLANE:
						typeString = "飛機場: ";
						break;
					case MT_Constant.TYPE_SHUTTLE:
						typeString = "客運站: ";
						break;
					case MT_Constant.TYPE_RAIL:
						typeString = "火車站: ";
						break;
					default:
						typeString = "轉運站: ";
					}
					
					poiMassTransitOverlay.addPointsOverlayNonPopulate(new OverlayItem_MT(mapView,
							new MT_SimpleStop(
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_ID), 
							typeString + cursor.getString(MassTransitDBManager.CCOLUMN_STOP_STR_NAME), 
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_LAT), 
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_LON), 
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_TYPE), 
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_COUNTY), 
							cursor.getInt(MassTransitDBManager.CCOLUMN_STOP_INT_TOWN))));
					cursor.moveToNext();
				}
				poiMassTransitOverlay.doPopulate();
				cursor.close();
				
				if(!overlayList.contains(poiMassTransitOverlay))
					overlayList.add(overlayList.indexOf(locationOverlay), poiMassTransitOverlay);
				
				mapView.invalidate();
				
				Log.d(D_TAG, "Add complete");
			}
			else
				Log.d(D_TAG, "Nothing to be added");
			super.onPostExecute(cursor);
		}
	}
	
	// Disable and clear the target itemized overlay via designating the target ID defined in PMap
	public void clearOverlay(int overlayID)
	{
		switch(overlayID)
		{
		case OVERLAY_START_PIN:
			if(directionStartOverlay != null)
			{
				overlayList.remove(directionStartOverlay);
				directionStartOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_END_PIN:
			if(directionEndOverlay != null)
			{
				overlayList.remove(directionEndOverlay);
				directionEndOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_CUSTOM_PIN:
			if(customPinOverlay != null)
			{
				overlayList.remove(customPinOverlay);
				customPinOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI:	// Clear all the poi-class overlays
			if(poiMassTransitOverlay != null)
			{
				overlayList.remove(poiMassTransitOverlay);
				poiMassTransitOverlay.removeAllPointsOverlay();
			}
			if(poiEatOverlay != null)
			{
				overlayList.remove(poiEatOverlay);
				poiEatOverlay.removeAllPointsOverlay();
			}
			if(poiPlayOverlay != null)
			{
				overlayList.remove(poiPlayOverlay);
				poiPlayOverlay.removeAllPointsOverlay();
			}
			if(poiBuyOverlay != null)
			{
				overlayList.remove(poiBuyOverlay);
				poiBuyOverlay.removeAllPointsOverlay();
			}
			if(poiHotelOverlay != null)
			{
				overlayList.remove(poiHotelOverlay);
				poiHotelOverlay.removeAllPointsOverlay();
			}
			if(poiMrtOverlay != null)
			{
				overlayList.remove(poiMrtOverlay);
				poiMrtOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_MT_STOP:
			if(poiMassTransitOverlay != null)
			{
				overlayList.remove(poiMassTransitOverlay);
				poiMassTransitOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_EAT:
			if(poiEatOverlay != null)
			{
				overlayList.remove(poiEatOverlay);
				poiEatOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_PLAY:
			if(poiPlayOverlay != null)
			{
				overlayList.remove(poiPlayOverlay);
				poiPlayOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_BUY:
			if(poiBuyOverlay != null)
			{
				overlayList.remove(poiBuyOverlay);
				poiBuyOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_HOTEL:
			if(poiHotelOverlay != null)
			{
				overlayList.remove(poiHotelOverlay);
				poiHotelOverlay.removeAllPointsOverlay();
			}
			break;
		case OVERLAY_POI_MRT:
			if(poiMrtOverlay != null)
			{
				overlayList.remove(poiMrtOverlay);
				poiMrtOverlay.removeAllPointsOverlay();
			}
			break;
		}
		mapView.postInvalidate();
	}
	
	/* Open the pop-up of the specific OverlayItem within ItemizedPointsOverlay */	
	public void popupStartPin()
	{
		try
		{
			if(directionStartOverlay != null && directionStartOverlay.size() > 0)
				directionStartOverlay.triggerPopup(0);
		}
		catch (Exception e)
		{
			Log.e(D_TAG, "popupStartPin :" + e.toString());
		}
	}
	
	public void popupEndPin()
	{
		try
		{
			if(directionEndOverlay != null && directionEndOverlay.size() > 0)
				directionEndOverlay.triggerPopup(0);
		}
		catch (Exception e)
		{
			Log.e(D_TAG, "popupEndPin :" + e.toString());
		}
	}
	
	/** View management */
	
	/* Direction setup control */
    public void exitDirectionSetup()
    {
    	directionSetup.hide();
    	directionSetup.dataFinalize();
    	btnMapMode.setVisibility(View.VISIBLE);
    }
	
	/* Map mode control */
	private void resetMapMode()
	{
    	Log.v(D_TAG, "reset the mapMode to the corresponding initial mode (MOM: " + Integer.toString(currentMapOpMode) + ")");
    	
    	switch(currentMapOpMode)
    	{
    	case MOM_LO:
    		currentMapMode = LO_MAP_MODE_1;
    		btnMapMode.setBackgroundResource(LO_MAP_MODE_1);
    		break;
    	case MOM_O:
    		currentMapMode = O_MAP_MODE_1;
    		btnMapMode.setBackgroundResource(O_MAP_MODE_1);
    		break;
    	case MOM_L:
    		currentMapMode = L_MAP_MODE_1;
    		btnMapMode.setBackgroundResource(L_MAP_MODE_1);
    		break;
    	case MOM_NA:
    		currentMapMode = MAP_MODE_NA;
    		btnMapMode.setBackgroundResource(MAP_MODE_NA);
    		break;
    	}
    	
    	locationOverlay.turnOffAutoPan();
	}
	
	private void resetMapModeOnTouch()
	{
		// NOTE: The map mode in MOM_O can ONLY be changed by click on the btnMapMode
    	switch(currentMapOpMode)
    	{
    	case MOM_LO:
    		Log.v(D_TAG, "reset the mapMode to mode 1 (MOM: " + Integer.toString(currentMapOpMode) + ")");
    		currentMapMode = LO_MAP_MODE_1;
    		btnMapMode.setBackgroundResource(LO_MAP_MODE_1);
    		break;
    	case MOM_L:
    		Log.v(D_TAG, "reset the mapMode to mode 1 (MOM: " + Integer.toString(currentMapOpMode) + ")");
    		currentMapMode = L_MAP_MODE_1;
    		btnMapMode.setBackgroundResource(L_MAP_MODE_1);
    		break;
    	}
    	
    	locationOverlay.turnOffAutoPan();
	}
	
	private void disableMapMode()
	{
		Log.d(D_TAG, "btnMapMode disabled (MOM: " + Integer.toString(currentMapOpMode) + ")");
		
		btnMapMode.setEnabled(false);
	}
	
	final Runnable runEnableMapMode = new Runnable() {	
		@Override
		public void run() {
			try
			{
				enableMapMode();
			}
			catch (Exception e)
			{
				Log.e(D_TAG, e.getMessage());
			}
		}
	};
	
	private void enableMapMode()
	{	
		Log.d(D_TAG, "enableMapMode: btnMapMode enabled (MOM: " + Integer.toString(currentMapOpMode) + ")");
		btnMapMode.setEnabled(true);
	}
	
	private void checkZoomInAbility()
	{
		Log.v(D_TAG, "checkZoomInAbility");
		
		currentLevel = mapView.getZoomLevel();
		maximalLevel = mapView.getMaxZoomLevel();
		
		if(MIN_ZOOM_LEVEL < currentLevel)
			btnZoomOut.setEnabled(true);
		if(maximalLevel == currentLevel)
			btnZoomIn.setEnabled(false);
		
		itemizedOverlaysUpdateRequired = true;
	}
	
	private void checkZoomOutAbility()
	{
		Log.v(D_TAG, "checkZoomOutAbility");
		
		currentLevel = mapView.getZoomLevel();
		maximalLevel = mapView.getMaxZoomLevel();
		
		if(maximalLevel > currentLevel)
			btnZoomIn.setEnabled(true);
		if(MIN_ZOOM_LEVEL == currentLevel)
			btnZoomOut.setEnabled(false);
		
		itemizedOverlaysUpdateRequired = true;
	}
	
	private void checkZoomAbility()
	{
		Log.v(D_TAG, "checkZoomAbility");
		
		currentLevel = mapView.getZoomLevel();
		maximalLevel = mapView.getMaxZoomLevel();
		
		if(MIN_ZOOM_LEVEL < currentLevel)
			btnZoomOut.setEnabled(true);
		else
			btnZoomOut.setEnabled(false);
		
		if(maximalLevel > currentLevel)
			btnZoomIn.setEnabled(true);
		else
			btnZoomIn.setEnabled(false);
		
		itemizedOverlaysUpdateRequired = true;
	}
	
	/* Create the listener call-back */	
	private Button.OnClickListener zoomIn = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "zoomIn");
			
			mapCtrl.zoomIn();
			checkZoomInAbility();

			Log.d(D_TAG, Integer.toString(currentLevel));	
		}
	};
	
	private Button.OnClickListener zoomOut = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "zoomOut");
			
			mapCtrl.zoomOut();
			checkZoomOutAbility();
			
			Log.d(D_TAG, Integer.toString(currentLevel));
		}
	};
	
	private Button.OnClickListener changeMapMode = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "changeMapMode in MOM: " + Integer.toString(currentMapOpMode));
			
			// NOTE: MOM_NA should always be in "disabled" status and thus will not have onClick() operation
			switch(currentMapOpMode)
			{
			case MOM_L:
				switch(currentMapMode)
				{
				case L_MAP_MODE_1:
					Log.d(D_TAG, "To mode 2");
					
					// Appearance
					currentMapMode = L_MAP_MODE_2;
					btnMapMode.setBackgroundResource(L_MAP_MODE_2);

					// Operation
					locationOverlay.animateToMyLocation();
					locationOverlay.turnOnAutoPan();
					locationOverlay.toastCurrentAccuracy();
					break;
				case L_MAP_MODE_2:
					Log.d(D_TAG, "To mode 1");
					
					// Appearance
					currentMapMode = L_MAP_MODE_1;
					btnMapMode.setBackgroundResource(L_MAP_MODE_1);

					// Operation
					locationOverlay.turnOffAutoPan();
					break;
				}
				break;
			case MOM_O:
				switch(currentMapMode)
				{
				case O_MAP_MODE_1:
					Log.d(D_TAG, "To mode 2");
					
					// Appearance
					currentMapMode = O_MAP_MODE_2;
					btnMapMode.setBackgroundResource(O_MAP_MODE_2);
					break;
				case O_MAP_MODE_2:
					Log.d(D_TAG, "To mode 1");
					
					// Appearance
					currentMapMode = O_MAP_MODE_1;
					btnMapMode.setBackgroundResource(O_MAP_MODE_1);
					break;
				}
				break;
			case MOM_LO:
				switch(currentMapMode)
				{
				case LO_MAP_MODE_1:
					Log.d(D_TAG, "To mode 2");
					
					// Appearance
					currentMapMode = LO_MAP_MODE_2;
					btnMapMode.setBackgroundResource(LO_MAP_MODE_2);
					
					// Operation
					locationOverlay.animateToMyLocation();
					locationOverlay.turnOnAutoPan();
					locationOverlay.toastCurrentAccuracy();
					break;
				case LO_MAP_MODE_2:
					Log.d(D_TAG, "To mode 3");
					
					// Appearance
					currentMapMode = LO_MAP_MODE_3;
					btnMapMode.setBackgroundResource(LO_MAP_MODE_3);
					break;
				case LO_MAP_MODE_3:
					Log.d(D_TAG, "To mode 1");
					
					// Appearance
					currentMapMode = LO_MAP_MODE_1;
					btnMapMode.setBackgroundResource(LO_MAP_MODE_1);
					
					// Operation
					locationOverlay.turnOffAutoPan();
					break;
				}
				break;
			}
			
			checkZoomAbility();
		}
	};
	
	/* Setup button connections */
	
	private void findViews()
	{
		btnZoomIn = (Button)findViewById(R.id.btn_zoom_in);
		btnZoomOut = (Button)findViewById(R.id.btn_zoom_out);
		btnMapMode = (Button)findViewById(R.id.btn_map_mode);
	}
	
	/* Setup the button listener */
	private void setListener()
	{
		gestureDetector.setOnDoubleTapListener(onDoubleTapListener);
		mapView.setOnTouchListener(whenTouch);
		btnZoomIn.setOnClickListener(zoomIn);
		btnZoomOut.setOnClickListener(zoomOut);
		btnMapMode.setOnClickListener(changeMapMode);
	}
	
    /** Overrides of MapActivity */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(D_TAG, "PMap: onCreate");
    	
        super.onCreate(savedInstanceState);
        
        // Initializing all DBs
        initDB();

        /* Setup the view of this activity */
        // Create custom MapView instance
        mapView = new PMapView(PMap.this, "0VSU0LcthqywR_RF3eJTdhk_15ysRjUIa5krsQQ");	// For mangreen
//        mapView = new PMapView(PMap.this, "0eCnhij78qYJXw-qufpUvgmYcKI2EORawr74jSA");	// For Tony
        mapView.setClickable(true);
        mapView.setReticleDrawMode(ReticleDrawMode.DRAW_RETICLE_UNDER);
        mapView.setSatellite(false);
        mapView.setStreetView(false);
        mapView.preLoad();
        
        // Prepare the basic layoutGroup
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutBase = (RelativeLayout)inflater.inflate(R.layout.main, null);
        
        // Prepare(Add) the other control layout-sets. [NOTE] Once you create a layout-set, it will add its instance into the parentView you designated on the fly
        directionDisplay = new DirectionDisplayForPMap(PMap.this);
        directionSetup = new DirectionManagerForPMap(PMap.this, layoutBase, directionDisplay, 0);
        transitAvoidStatus = directionSetup.getTransitAvoidArray();
        
        // Add the custom MapView at the bottom
        layoutBase.addView(mapView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        // Set the complete view to the PMap(MapActivity)
        setContentView(layoutBase);
        
        /* Custom operation initialization */
        findViews();
        setListener();
                
        /* Setup resources of MapView */
        mapCtrl = mapView.getController();
        overlayList = mapView.getOverlays();
        projection = mapView.getProjection();
        
        /* Setup overlay system of MapView */
        overlayInit();
    }
    
    @Override
    protected void onStart() {
    	Log.v(D_TAG, "PMap: onStart");
    	super.onStart();
    }
    
	@Override
	protected void onResume() {
		Log.v(D_TAG, "PMap: onResume");
		
		// Initialize for special flag
		itemizedOverlaysUpdateRequired = false;
		isActivating = true;	// [TRICK] When this activity is waked up again, it can retrieve its control
		
		/* Setup location related functionality */
		boolean hasOrientation;
		boolean hasLocation;
		
	    // Make sure that when the activity has been suspended to background, the device starts getting locations again
		hasOrientation = locationOverlay.enableCompass();
		hasLocation = locationOverlay.enableMyLocation();
		
		// Setup the current map-operation-mode and initial it if needed
		if(hasLocation && hasOrientation)
		{
			currentMapOpMode = MOM_LO;
			Log.d(D_TAG, "Now in MOM_LO (MOM: " + Integer.toString(currentMapOpMode) + ")");
			
        	// Enable the mapMode controller only when the location information is available at cold-startup
        	// NOTE: you should use the local handler to trigger the Runnable() that involved the local view elements,
        	// NOTE: or you will get the exception message: "Only the original thread that created a view hierarchy can touch its views"
			disableMapMode();
			locationOverlay.runOnLastFix(new Runnable() {	
        		@Override
        		public void run() {
        			Log.v(D_TAG, "runOnFirstFix");
        			handler.post(runEnableMapMode);
        		}
        	});
		}
		else if(hasOrientation)
		{
			currentMapOpMode = MOM_O;
			Log.d(D_TAG, "Now in MOM_O (MOM: " + Integer.toString(currentMapOpMode) + ")");
			enableMapMode();
		}
		else if(hasLocation)
		{
			currentMapOpMode = MOM_L;
			Log.d(D_TAG, "Now in MOM_L (MOM: " + Integer.toString(currentMapOpMode) + ")");
        	
        	// Enable the mapMode controller only when the location information is available at cold-startup
        	// NOTE: you should use the local handler to trigger the Runnable() that involved the local view elements,
        	// NOTE: or you will get the exception message: "Only the original thread that created a view hierarchy can touch its views"
			disableMapMode();
			locationOverlay.runOnLastFix(new Runnable() {	
        		@Override
        		public void run() {
        			Log.v(D_TAG, "runOnFirstFix");
        			handler.post(runEnableMapMode);
        		}
        	});
		}
		else
		{
			currentMapOpMode = MOM_NA;
			Log.d(D_TAG, "Now in MOM_NA (MOM: " + Integer.toString(currentMapOpMode) + ")");
			disableMapMode();
		}
		
		resetMapMode();
		checkZoomAbility();
		
		// Data restoration
        directionSetup.dataRestoration();
        transitAvoidStatus = directionSetup.getTransitAvoidArray();
        
        // Resolve the pinPoint requirement if existed
		SharedPreferences settings = PMap.this.getSharedPreferences(PMap.PREFNAME_PINPOINT, 0);

		boolean needToPin = settings.getBoolean(PREFS_BOOL_PINPOINT, false);
		int latE6 = settings.getInt(PREFS_INT_LATE6, 91000000);
		int lonE6 = settings.getInt(PREFS_INT_LONE6, 181000000);
		
		if(needToPin && latE6 < 91000000 && lonE6 < 181000000)
		{
			GeoPoint location = new GeoPoint(latE6, lonE6);
			overlayUpdateCustomDataGeoPin(location,
					settings.getString(PREFS_STR_TITLE, null), 
					settings.getString(PREFS_STR_INFO, null), 
					91000000, 181000000,
					settings.getString(PREFS_STR_BUILDING_ID, null),
					settings.getInt(PREFS_INT_FLOOR_ID, 0));
			mapCtrl.animateTo(location);
		}
		PMap.this.getSharedPreferences(PMap.PREFNAME_PINPOINT, 0).edit().clear().commit();	// Clear the old data
		
		/* Resolve the intent when PMap is triggered by another activity with specific ACTION and URI*/
		Intent intent = this.getIntent();
		Uri uri;
    	if(null != intent && (uri = intent.getData()) != null)
    	{
    		String schema = uri.getScheme();
    		
    		if(schema != null)
    		{
				try
				{
					if(schema.equals(INTENT_FILTER_MAP_VIEW))
	    			{
	    				Log.d(D_TAG, "Start with MAP_VIEW request");
	    				
	    				Pattern pattern = Pattern.compile("[?].+");
	    	    		String stringLocation = pattern.split(intent.getData().getSchemeSpecificPart(), 0)[0];
	    	    		
	    	    		if(Pattern.matches("^[0-9.]+,[0-9.]+", stringLocation))
	    	        	{	
	    	    			Log.d(D_TAG, "PMap started with valid URI: " + intent.getDataString());
	    	    			
	    	        		pattern = Pattern.compile(",");
	    	        		String[] locationSet = pattern.split(stringLocation, 0);
	    	        		
	    	        		if(null != intent.getData().getQuery() && Pattern.matches("z=[0-9]+", intent.getData().getQuery()))
	    	        		{
	    	        			pattern = Pattern.compile("z=");
	    	        			int level = Integer.parseInt(pattern.split(intent.getData().getQuery(), 0)[1]);
	    	        			if(!(level < MIN_ZOOM_LEVEL || level > DEFAULT_ZOOM_LEVEL))
	    		        			mapCtrl.setZoom(level);
	    	        		}
	    	        		
	    	        		mapCtrl.animateTo(new GeoPoint((int)(Double.parseDouble(locationSet[0]) * 1E6), (int)(Double.parseDouble(locationSet[1]) * 1E6)));
	    	        		checkZoomAbility();
	    	        	}
	    	        	else
	    	        	{
	    	        		Log.e(D_TAG, "PMap started with invalid URI: " + intent.getDataString());
	    	        		Toast.makeText(PMap.this, "Invalid URI: " + intent.getDataString(), Toast.LENGTH_SHORT).show();
	    	        	}
	    			}
	    			else if(schema.equals(INTENT_FILTER_PIN_POINT))
	    			{
	    				// [FORMAT] pin://?lat=25000000&lon=120000000&name=point_name&info=point_info
	    				Log.d(D_TAG, "Start with PIN_POINT request: " + uri.toString());
	    				
	    				latE6 = Integer.parseInt(uri.getQueryParameter("lat"));
	    				lonE6 = Integer.parseInt(uri.getQueryParameter("lon"));
	    				
	    				String floorStr = uri.getQueryParameter("floor_id");
	    				int floorID;
	    				
	    				if(floorStr != null)
	    					floorID = Integer.parseInt(floorStr);
	    				else
	    					floorID = 0;

	    				//mangreen modify 20110120
	    				bookmarkOperation(BOOKMARK_MODE_NORMAL,
    							latE6, 
								lonE6, 
								91000000, 
								181000000, 
								uri.getQueryParameter("name"), 
								uri.getQueryParameter("info"), 
								uri.getQueryParameter("build_id"), 
								floorID);
	    			}
	    			else if(schema.equals(INTENT_FILTER_SHOW_PIN))
	    			{
	    				// [FORMAT] show_pin://?type=0&lat=25000000&lon=120000000 [NOTE] The type is defined in DirectionManager: SP_TYPE_
	    				int type = Integer.parseInt(uri.getQueryParameter("type"));
	    				latE6 = Integer.parseInt(uri.getQueryParameter("lat"));
	    				lonE6 = Integer.parseInt(uri.getQueryParameter("lon"));
	    				
	    				switch(type)
	    				{
	    				case DirectionManager.SP_TYPE_START_POINT:
	    					popupStartPin();
	    					break;
	    				case DirectionManager.SP_TYPE_END_POINT:
	    					popupEndPin();
	    					break;
	    				}
	    				
	    				mapCtrl.animateTo(new GeoPoint(latE6, lonE6));
	    			}
				}
				catch(UnsupportedOperationException e)
				{
					Log.e(D_TAG, "UnsupportedOperationException" + e.toString());
				}
				catch(NullPointerException e)
				{
					Log.e(D_TAG, "NullPointerException" + e.toString());
				}
				catch(Exception e)
				{
					Log.e(D_TAG, "Exception" + e.toString());
				}
    		}
    		
    		// Clear the used data to prevent duplicating uses
    		intent.setData(null);
    		setIntent(intent);
    	}
		
	    super.onResume();
	}
    
    @Override
    protected void onPause() {
    	Log.v(D_TAG, "PMap: onPause");
    	
        // Make sure that when the activity goes to background, the device stops getting locations to save battery life.
    	locationOverlay.disableCompass();
        locationOverlay.disableMyLocation();
        
        // Store the last direction settings
        directionSetup.dataFinalize();
        
        super.onPause();
    }
    
    @Override
    protected void onStop() {
    	Log.v(D_TAG, "PMap: onStop");
    	
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.v(D_TAG, "PMap: onDestroy");
    	
    	// Close the DB connection
        closeDB();
    	
    	super.onDestroy();
    }
    
    @Override
    protected void onRestart() {
    	Log.v(D_TAG, "PMap: onRestart");
    	super.onRestart();
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	Log.v(D_TAG, "PMap: onConfigurationChanged");
    	
    	// [NOTE] When you declare your Activity to handle a configuration change, 
    	// you are responsible for resetting any elements for which you provide alternatives. 
    	// If you declare your Activity to handle the orientation change and have images that should change 
    	// between landscape and portrait, you must re-assign each resource to each element during onConfigurationChanged().
    	
		super.onConfigurationChanged(newConfig);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.v(D_TAG, "PMap: onActivityResult");
    	
    	if(intent != null && resultCode != RESULT_CANCELED)
    	{
    		Bundle bundle = intent.getExtras();
    		
    		switch(requestCode)
			{
			case REQUEST_POINT_DETAIL:
				switch(resultCode)
				{
				case RESULT_PDETAIL_HAS_CENTER:
					if(null != intent.getData())
			    	{
			    		Pattern pattern = Pattern.compile("[?].+");
			    		String stringLocation = pattern.split(intent.getData().getSchemeSpecificPart(), 0)[0];
			    		
			    		if(Pattern.matches("^[0-9.]+,[0-9.]+", stringLocation))
			        	{	
			    			Log.d(D_TAG, "PMap returned with valid URI: " + intent.getDataString());
			    			
			        		pattern = Pattern.compile(",");
			        		String[] locationSet = pattern.split(stringLocation, 0);
			        		
			        		if(null != intent.getData().getQuery() && Pattern.matches("z=[0-9]+", intent.getData().getQuery()))
			        		{
			        			pattern = Pattern.compile("z=");
			        			int level = Integer.parseInt(pattern.split(intent.getData().getQuery(), 0)[1]);
			        			if(!(level < MIN_ZOOM_LEVEL || level > DEFAULT_ZOOM_LEVEL))
				        			mapCtrl.setZoom(level);
			        		}
			        		
			        		popupOverlay.show();
			        		checkZoomAbility();
			        		mapCtrl.animateTo(new GeoPoint((int)(Double.parseDouble(locationSet[0]) * 1E6), (int)(Double.parseDouble(locationSet[1]) * 1E6)));
			        	}
			        	else
			        	{
			        		Log.e(D_TAG, "PMap returned with invalid URI: " + intent.getDataString());
			        		Toast.makeText(PMap.this, "Invalid URI: " + intent.getDataString(), Toast.LENGTH_SHORT).show();
			        	}
			    		
			    		// Clear the used data to prevent duplicating uses
			    		intent.setData(null);
			    	}
					break;
				case RESULT_PDETAIL_CLEAR_CUSTOMPIN_OVERLAY:
					clearOverlay(intent.getIntExtra(INTENT_INT_1, OVERLAY_CUSTOM_PIN));
					break;
				case RESULT_PDETAIL_ADD_BOOKMARK:	// Insert the outdoor point
					if(bundle != null)
					{
						bookmarkDB.SPOINT_insert(bundle.getString(PointsDetailPanel.BUNDLE_TITLE),
							bundle.getString(PointsDetailPanel.BUNDLE_INFO), 
							(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LAT) * 1E6), 
							(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LON) * 1E6),
							(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_ADDRESS_LAT, 91.0) * 1E6), 
							(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_ADDRESS_LON, 181.0) * 1E6),
							bundle.getInt(PointsDetailPanel.BUNDLE_FLOOR),
							bundle.getString(PointsDetailPanel.BUNDLE_BUILD_ID));
						bundle.clear();
						intent.putExtras(bundle);
					}
					break;
				case RESULT_PDETAIL_SET_START:
					if(bundle != null)
					{
						directionSetup.setStart(false, 
								bundle.getString(PointsDetailPanel.BUNDLE_TITLE),
								bundle.getString(PointsDetailPanel.BUNDLE_INFO), 
								new GeoPoint((int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LAT) * 1E6), 
										(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LON) * 1E6)),
								bundle.getString(PointsDetailPanel.BUNDLE_BUILD_ID),
								bundle.getInt(PointsDetailPanel.BUNDLE_FLOOR, 0));
						directionSetup.show();
						directionSetup.dataFinalize();
						bundle.clear();
						intent.putExtras(bundle);
					}
					break;
				case RESULT_PDETAIL_SET_END:
					if(bundle != null)
					{
						directionSetup.setEnd(false, 
								bundle.getString(PointsDetailPanel.BUNDLE_TITLE),
								bundle.getString(PointsDetailPanel.BUNDLE_INFO), 
								new GeoPoint((int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LAT) * 1E6), 
										(int)(bundle.getDouble(PointsDetailPanel.BUNDLE_LON) * 1E6)),
								bundle.getString(PointsDetailPanel.BUNDLE_BUILD_ID),
								bundle.getInt(PointsDetailPanel.BUNDLE_FLOOR, 0));
						directionSetup.show();
						directionSetup.dataFinalize();
						bundle.clear();
						intent.putExtras(bundle);
					}
					break;
				}
				break;
			}
    		setIntent(intent);	// Update the intent
    	}
			
		super.onActivityResult(requestCode, resultCode, intent);
	}
    
    @Override
	public boolean onSearchRequested() {
    	openSearchDialog();
		return false;			// Disable the default launch
	}
    
    @Override
	public void onBackPressed() {
		if(directionSetup.isEnabled())
		{
			exitDirectionSetup();
		}
		else
			super.onBackPressed();
	}
    
    @Override
	public void startActivity(Intent intent) {
		System.gc();
		super.startActivity(intent);
	}
	
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		System.gc();
		super.startActivityForResult(intent, requestCode);
	}
	
    // Specified override for MapActivity
    @Override
    protected boolean isRouteDisplayed() {
        return true;
    }
    
    /* OptionsMenu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)	// Executed once only at the menu first created
	{
		Log.d(D_TAG, "PMap: onCreateOptionsMenu");
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pmap_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 	// Executed each time the menu is going to be opened
	{
		Log.d(D_TAG, "PMap: onPrepareOptionsMenu");
		
		if(directionSetup.isEnabled())
		{
			menu.setGroupVisible(MENUGROUP_MAIN, false);
			menu.setGroupVisible(MENUGROUP_DIRECTION_SETUP, true);
			
			if(directionSetup.hasSetSomething())
				menu.findItem(MENUITEM_DIRECTION_REVERSE).setEnabled(true);
			else
				menu.findItem(MENUITEM_DIRECTION_REVERSE).setEnabled(false);
			
			if(directionSetup.isDisplayingResult())
			{
				menu.findItem(MENUITEM_DIRECTION_MT_OPTION).setVisible(false);
				menu.findItem(MENUITEM_DIRECTION_ROUTE_LIST).setVisible(true);
			}
			else
			{
				menu.findItem(MENUITEM_DIRECTION_ROUTE_LIST).setVisible(false);
				menu.findItem(MENUITEM_DIRECTION_MT_OPTION).setVisible(true);
				if(directionSetup.isMass())
					menu.findItem(MENUITEM_DIRECTION_MT_OPTION).setEnabled(true);
				else
					menu.findItem(MENUITEM_DIRECTION_MT_OPTION).setEnabled(false);
			}
		}
		else
		{
			menu.setGroupVisible(MENUGROUP_MAIN, true);
			menu.setGroupVisible(MENUGROUP_DIRECTION_SETUP, false);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId())
		{
		case MENUITEM_SEARCH:
			openSearchDialog();
			break;
		case MENUITEM_DIRECTION:
			directionSetup.show();
			resetMapMode();
			break;
		case MENUITEM_LAYER:
			new AlertDialog.Builder(PMap.this)
			.setIcon(R.drawable.ic_dialog_dialer)
            .setTitle(R.string.pmap_menu_layer)
            .setMultiChoiceItems(R.array.layer_menu, poiLayerStatus,
            new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int whichButton,
                        boolean isChecked) {
                }
            })
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	itemizedOverlaysUpdate();
                }
            })
            .setNegativeButton(R.string.btn_clear, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	Arrays.fill(poiLayerStatus, false);
                	itemizedOverlaysUpdate();
                }
            })
            .show();
			break;
		case MENUITEM_BOOKMARK:
			openBookmarkDialog(BOOKMARK_MODE_NORMAL);
			break;
		case MENUITEM_CLEANMAP:
			// Clear popup
			popupOverlay.hide();
	
			// Clear layer
			Arrays.fill(poiLayerStatus, false);
        	itemizedOverlaysUpdate();
		
        	// Clear custom point
        	clearOverlay(OVERLAY_CUSTOM_PIN);
        	
			// Clear direction
			directionSetup.dataInitialize();
			getSharedPreferences(PMap.PREFNAME_DIRECTION, 0).edit().clear().commit();	// Clear the old data
			break;
		case MENUITEM_ABOUT:
			new AlertDialog.Builder(PMap.this)
				.setTitle(R.string.pmap_about_title)
				.setIcon(R.drawable.ic_dialog_info)
				.setMessage(R.string.pmap_about_message)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new AlertDialog.Builder(PMap.this)
						.setIcon(R.drawable.ic_dialog_dialer)
						.setTitle(R.string.pmap_develop_title)
						.setItems(R.array.developer_menu, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {

		                    	switch(which)
		                    	{
		                    	case 0:
		                    		break;
		                    	case 1:
		                    		new AlertDialog.Builder(PMap.this)
		                			.setTitle(R.string.hint_on_mt_import_title)
		                			.setMessage(R.string.hint_on_mt_import_confirm)
		                			.setIcon(R.drawable.ic_dialog_alert)
		                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											MassTransitDBManager mt = new MassTransitDBManager(PMap.this);
				                    		new ImportMassTransitData().execute(mt);
										}
		                            	
		                            })
		                            .setNegativeButton(R.string.btn_cancel, null)
		                			.show();
		                    		
		                    		break;
		                    	case 2:
		                    		new AlertDialog.Builder(PMap.this)
		                			.setTitle(R.string.hint_on_mt_import_title)
		                			.setMessage(massDB.TC_countInfo())
		                            .setPositiveButton(R.string.btn_ok, null)
		                            .show();
		                            
		                    		break;
		                    	case 3:
		                    		if(overlayList.contains(locationOverlay))
		                    			overlayList.remove(locationOverlay);
		                    		else
		                    			overlayList.add(locationOverlay);
		                    		mapView.postInvalidate();
		                    		break;
		                    	case 4:
		                    		Log.e(D_TAG, "TEST > " + Integer.toString(mapView.getProjection().fromPixels(-10, mapView.getHeight()+10).getLatitudeE6()) + "," + Integer.toString(mapView.getProjection().fromPixels(-10, mapView.getHeight()+10).getLongitudeE6()));
		                    		break;
		                    	case 5:
		                    		directionSetup.dataInitialize();
		                    		break;
		                    	case 6:
		                    		new AlertDialog.Builder(PMap.this)
		                			.setTitle(R.string.hint_on_bookmark_db_title)
		                			.setMessage(bookmarkDB.SPOINT_countInfo())
		                            .setPositiveButton(R.string.btn_ok, null)
		                            .show();
		                            
		                    		break;
		                    	case 7:
		                    		bookmarkDB.SPOINT_insert("SAMPLE", "Oh Oh", 0, 0, 91, 181, 0, null);
		                    		break;	
		                    	case 8:
		                    		new AlertDialog.Builder(PMap.this)
		                			.setTitle(R.string.pmap_bookmark_clear_title)
		                			.setMessage(R.string.pmap_bookmark_clear_hint)
		                			.setIcon(R.drawable.ic_dialog_alert)
		                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener(){

		    							@Override
		    							public void onClick(DialogInterface arg0, int arg1) {
		    								bookmarkDB.dataClear();
		    								Toast.makeText(PMap.this, getString(R.string.pmap_bookmark_clear_done), Toast.LENGTH_SHORT).show();
		    							}
		                            	
		                            })
		                            .setNegativeButton(R.string.btn_cancel, null)
		                			.show();
		                    		break;
		                    	case 9:
		                    		bookmarkDB.SPOINT_delete(bookmarkDB.SPOINT_count());
		                    		break;
		                    	case 10:
		                    		bookmarkDB.SPOINT_update(1, getString(R.string.hint_on_direction_cur_location), 
		                    				getString(R.string.hint_on_direction_cur_location_info), 
		                    				0, 
		                    				0, 
		                    				0, 
		                    				0,
		                    				0,
		                    				null);
		                    		break;
		                    	case 11:
		                    		Bundle bundle = new Bundle();
		                			bundle.putString(IndoorMap.IDR_BUNDLE_STR_BUILD_ID, "iii");
		                			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_FLOOR_ID, 0);
		                    		startActivity(new Intent(PMap.this, IndoorMap.class).putExtras(bundle));
		                    		
		                    		break;
		                    	case 12:
		                    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("pin://?lat=25040774&lon=121566248&name=point_name&info=point_info")));
		                    		break;
		                    	}
		                    }
		                })
		                .setNegativeButton(R.string.btn_cancel, null)
						.show();
					}
				})
				.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				})
				.show();
			break;
			
		// For direction setup menu
		case MENUITEM_DIRECTION_QUIT:
			exitDirectionSetup();
			break;
		case MENUITEM_DIRECTION_REVERSE:
			directionSetup.reverseStartEnd();
			break;
		case MENUITEM_DIRECTION_CLEAR:
			directionSetup.dataInitialize();
			getSharedPreferences(PMap.PREFNAME_DIRECTION, 0).edit().clear().commit();	// Clear the old data
			break;
		case MENUITEM_DIRECTION_MT_OPTION:
			new AlertDialog.Builder(PMap.this)
			.setIcon(R.drawable.ic_dialog_dialer)
            .setTitle(R.string.pmap_menu_direction_opt)
            .setMultiChoiceItems(R.array.transit_avoid_menu, transitAvoidStatus,
            new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int whichButton,
                        boolean isChecked) {
                }
            })
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	directionSetup.setTransitAvoidArray(transitAvoidStatus);
                }
            })
            .setNegativeButton(R.string.btn_reset, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	Arrays.fill(transitAvoidStatus, true);
                	directionSetup.setTransitAvoidArray(transitAvoidStatus);
                }
            })
            .show();
			break;
		case MENUITEM_DIRECTION_ROUTE_LIST:
			directionSetup.openResultListDlg(true);
			break;
		}
		
		return true;
	}

/*** Interface */
	
	public void setTouchHandleEnable(boolean handleTouch)
	{
		isActivating = handleTouch;
	}
	
	public boolean getTouchHandleStatus()
	{
		return isActivating;
	}
	
	private void openSearchDialog()
	{
		// Search location: from Google geocoder and III-POI server
		// Patch the location information
    	Bundle bundle = new Bundle();
    	GeoPoint geoPoint;
    	geoPoint = mapView.getMapCenter();
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_CENTER_LATE6, geoPoint.getLatitudeE6());
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_CENTER_LONE6, geoPoint.getLongitudeE6());
    	
    	geoPoint = projection.fromPixels(0, mapView.getHeight());
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_LBOTTOM_LATE6, geoPoint.getLatitudeE6());
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_LBOTTOM_LONE6, geoPoint.getLongitudeE6());
    	
    	geoPoint = projection.fromPixels(mapView.getWidth(), 0);
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_RTOP_LATE6, geoPoint.getLatitudeE6());
    	bundle.putInt(OutdoorLocationSearch.BUNDLE_RTOP_LONE6, geoPoint.getLongitudeE6());
    	
		((SearchManager)getSystemService(SEARCH_SERVICE)).startSearch(null, true, PMap.this.getComponentName(), bundle, false);

		/*** Code backup - 20110117 */
		/**** Open search dialog which has options: search location / transit stops / transit lines - start */
//		new AlertDialog.Builder(PMap.this)
//		.setTitle(R.string.pmap_menu_search)
//		.setItems(R.array.search_menu, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//            	switch(which)
//            	{
//            	case 0:
//            		// Patch the location information
//                	Bundle bundle = new Bundle();
//                	GeoPoint geoPoint;
//                	geoPoint = mapView.getMapCenter();
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_CENTER_LATE6, geoPoint.getLatitudeE6());
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_CENTER_LONE6, geoPoint.getLongitudeE6());
//                	
//                	geoPoint = projection.fromPixels(0, mapView.getHeight());
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_LBOTTOM_LATE6, geoPoint.getLatitudeE6());
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_LBOTTOM_LONE6, geoPoint.getLongitudeE6());
//                	
//                	geoPoint = projection.fromPixels(mapView.getWidth(), 0);
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_RTOP_LATE6, geoPoint.getLatitudeE6());
//                	bundle.putInt(OutdoorLocationSearch.BUNDLE_RTOP_LONE6, geoPoint.getLongitudeE6());
//                	
//            		((SearchManager)getSystemService(SEARCH_SERVICE)).startSearch(null, true, PMap.this.getComponentName(), bundle, false);
//            		break;
//            	}
//            }
//        })
//        .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//            }
//        })
//		.show();
		/**** Open search dialog which has options: search location / transit stops / transit lines - end */
	}
	
	//mangreen modify 20110120
	public void bookmarkOperation(int operationMode, int latE6, int lonE6, int addrLatE6, int addrLonE6, String name, String info, String buildID, int floorID)
	{
		GeoPoint location = new GeoPoint(latE6, lonE6);

		if(floorID == 0 || buildID == null)	// Stay in outdoor
		{
			switch(operationMode)
			{
			case BOOKMARK_MODE_NORMAL:
				overlayUpdateCustomDataGeoPin(location, 
						name,
						info,
						addrLatE6,
						addrLonE6,
						buildID,
						floorID);
				break;
			case BOOKMARK_MODE_SET_START:
				directionSetup.setStart(false, name, info, location,
						buildID,
						floorID);
				break;
			case BOOKMARK_MODE_SET_END:
				directionSetup.setEnd(false, name, info, location,
						buildID,
						floorID);
				break;
			}

			mapCtrl.animateTo(location);
		}
		else	// Switch to indoor for further progress
		{
			directionSetup.dataFinalize();
			Bundle bundle = new Bundle();
			bundle.putString(IndoorMap.IDR_BUNDLE_STR_BUILD_ID, buildID);
			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_FLOOR_ID, floorID);
			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_OP_CODE, IndoorMap.OP_CODE_BOOKMARK);
			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_BK_OP_MODE, operationMode);
			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LAT, latE6);
			bundle.putInt(IndoorMap.IDR_BUNDLE_INT_PNT_LON, lonE6);
			bundle.putString(IndoorMap.IDR_BUNDLE_STR_PNT_NAME, name);
			bundle.putString(IndoorMap.IDR_BUNDLE_STR_PNT_INFO, info);
			startActivity(new Intent(PMap.this, IndoorMap.class).putExtras(bundle));
		}
	}
	
	public void openBookmarkDialog(final int operationMode)
	{
		if(bookmarkCursor != null && bookmarkAdapter != null)
		{
			bookmarkCursor.requery();
			
			BookmarkDialog bookmarkDialog = new BookmarkDialogForPMap(PMap.this, bookmarkAdapter,
					bookmarkCursor,
					locationOverlay,
					operationMode,
					directionSetup);
			bookmarkDialog.show();
		}
		else	// [NOTE] According to current design, here should never be reached
		{
			new AlertDialog.Builder(PMap.this)
			.setIcon(R.drawable.ic_dialog_bookmark)
			.setTitle(R.string.pmap_menu_bookmark)
			.setMessage(R.string.pmap_bookmark_none)
			.setPositiveButton(R.string.btn_ok, null)
			.show();
		}	
	}
	
/*** Miscellaneous */
	
	private class ImportMassTransitData extends AsyncTask<MassTransitDBManager, Void, String>
	{
		private MassTransitDBManager mdb;
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(PMap.this, PMap.this.getString(R.string.hint_on_mt_import_title), PMap.this.getString(R.string.hint_on_mt_import_progressing), false, true, new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					cancel(true);
				}
				
			});
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(MassTransitDBManager... dbs) {
			String result = null;
			
			if(dbs.length != 1)
				result = PMap.this.getString(R.string.hint_on_mt_import_failed_parameter);
			else
			{
        		try
        		{
        			mdb = dbs[0];
        			mdb.open();
        			if(!mdb.dataImport()) 
        				result = PMap.this.getString(R.string.hint_on_mt_import_failed_import);
        		}
        		catch (SQLiteException e)
        		{
        			Log.e(D_TAG, "DB error: " + e.getMessage());
        			result = PMap.this.getString(R.string.hint_on_mt_import_failed_db) + " " + e.getMessage();
        		}
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			mdb.close();
			progressDialog.dismiss();
			
			if(result == null)
			{
				new AlertDialog.Builder(PMap.this)
				.setTitle(R.string.hint_on_mt_import_title)
				.setMessage(R.string.hint_on_mt_import_complete)
	            .setNeutralButton(R.string.btn_ok, null)
				.show();
			}
			else
			{
				new AlertDialog.Builder(PMap.this)
				.setTitle(R.string.hint_on_mt_import_title)
				.setMessage(result)
	            .setNeutralButton(R.string.btn_ok, null)
				.show();
			}
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			
			mdb.close();
			
			new AlertDialog.Builder(PMap.this)
			.setTitle(R.string.hint_on_mt_import_title)
			.setMessage(R.string.hint_on_mt_import_cancel)
            .setNeutralButton(R.string.btn_ok, null)
			.show();
			super.onCancelled();
		}
	}
	
/*** Touch management */
    
    /* Current operation states */
    private boolean currentScroll = false;
    private int lastMapMode;
	
	/* Create the listener call-back */	
	
	// Create the gesture detector
	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent arg0) {
			Log.v(D_TAG, "GestureDetector: onDown");
			
			// Hold the map temporarily if it is in the mapMode 2/3 to prevent further auto-panning
			lastMapMode = currentMapMode;
			
			switch(currentMapOpMode)
	    	{
	    	case MOM_LO:
	    		currentMapMode = LO_MAP_MODE_1;
	    		break;
	    	case MOM_L:
	    		currentMapMode = L_MAP_MODE_1;
	    		break;
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
			
			// Reset the map mode
			resetMapModeOnTouch();
			
			// Pin the custom point
			if(!overlayList.contains(customPinOverlay))
				overlayUpdateCustomPin(new Point((int)arg0.getX(), (int)arg0.getY()));
		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			Log.v(D_TAG, "GestureDetector: onScroll");
			currentScroll = true;
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
			
			resetMapModeOnTouch();
			mapCtrl.zoomInFixing((int)arg0.getX(), (int)arg0.getY());
			checkZoomInAbility();
			
			Log.d(D_TAG, "double clicked zoom-in");
			Log.d(D_TAG, Integer.toString(currentLevel));
			
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			Log.v(D_TAG, "onDoubleTapListener: onDoubleTapEvent");
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent arg0) {
			Log.v(D_TAG, "onDoubleTapListener: onSingleTapConfirmed");
			
			// Release the map to the previous mapMode
			currentMapMode = lastMapMode;
			return false;
		}
		
	};
	
	private GestureDetector gestureDetector = new GestureDetector(onGestureListener);
	
	// Connect to the MotionEvent input
	private View.OnTouchListener whenTouch = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			
			if(isActivating)	// [TRICK_FLAG]
			{
				gestureDetector.onTouchEvent(arg1);
				
				if(MotionEvent.ACTION_UP == arg1.getAction())
				{
					// After-map-panning listener
					if(currentScroll)
					{
						Log.v(D_TAG, "View.OnTouchListener: ACTION_UP after SCROLL");
						
						// Reset the map mode
						resetMapModeOnTouch();
						
						// Check the zooming ability after map panning
						checkZoomAbility();
						
						Log.d(D_TAG, Integer.toString(currentLevel));
						
						// Reset the last scroll status
						currentScroll = false;
					}
					else
						Log.v(D_TAG, "View.OnTouchListener: ACTION_UP");
				}				
			}

			return false;
		}
	};
    
/*** Backup section */
    
/**** Location management - Start */
	
//    /* LocationListener configuration */
//    private LocationManager locationManager;
//    private LocationListener listenerFine;
//    private LocationListener listenerCoarse;
//    private GpsStatus.Listener listenerGPSStatus;
//	
//	/* Current operation states */
//    private Location lastLocation = null;
//    private Location currentLocation = null;
//    private GeoPoint currentPnt = null;
//    private boolean isFineAvailable = true;
//    private boolean isCoarseAvailable = true;
//    
//    private void registerLocationListeners() {
//    	Log.d(D_TAG, "registerLocationListeners");
//        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
// 
//        // Get at least something from the device, could be very inaccurate though
//    	currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        
//    	if(null != currentLocation)
//        {
//        	currentPnt = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
//        	Log.d(D_TAG, "getLastKnownLocation from fineLocation: " + Integer.toString(currentPnt.getLatitudeE6()) + ", " + Integer.toString(currentPnt.getLongitudeE6()));
//        }
//        else
//        {
//            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        	
//        	if(null != currentLocation)
//            {
//            	currentPnt = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
//            	Log.d(D_TAG, "getLastKnownLocation from coarseLocation: " + Integer.toString(currentPnt.getLatitudeE6()) + ", " + Integer.toString(currentPnt.getLongitudeE6()));
//            }
//            else
//            {
//            	currentLocation = lastLocation;
//            	
//            	if(null != currentLocation)
//                {
//                	currentPnt = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
//                	Log.d(D_TAG, "getLastKnownLocation from current activity: " + Integer.toString(currentPnt.getLatitudeE6()) + ", " + Integer.toString(currentPnt.getLongitudeE6()));
//                }
//                else
//                {
//	            	Log.d(D_TAG, "getLastKnownLocation: last know position not found");
//	            	disableMapMode();
//                }
//            }
//        }
//
//        if (listenerGPSStatus == null || listenerFine == null || listenerCoarse == null)
//            createLocationListeners();
//        
//        // Request for GPS status
//        locationManager.addGpsStatusListener(listenerGPSStatus);
//        
//        // Preset the location status temporarily, for ensuring that there are available providers
//        isCoarseAvailable = true;
//        isFineAvailable = true;
//        
//        try
//        {
//        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 100, listenerCoarse);
//        }
//        catch (IllegalArgumentException ae)
//        {
//        	Log.e(D_TAG, "requestLocationUpdates-coarse: provider not found --> failed to enable the coarseListener");
//        	isCoarseAvailable = false;
//        }
//        
//        try
//        {
//        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, listenerFine);
//        }
//        catch (IllegalArgumentException ae)
//        {
//        	Log.e(D_TAG, "requestLocationUpdates-fine: provider not found --> failed to enable the fineListener");
//        	isFineAvailable = false;
//        }
//        
//        if(!isCoarseAvailable && !isFineAvailable && null == currentPnt)
//        	disableMapMode();
//        else
//        {
//        	enableMapMode();
//        	
//        	// Reset the location status for the following update listeners
//        	isCoarseAvailable = false;
//            isFineAvailable = false;
//        }
//    }
//    
//    private void unRegisterLocationListeners()
//    {
//    	Log.d(D_TAG, "unRegisterLocationListeners");
//    	locationManager.removeGpsStatusListener(listenerGPSStatus);
//        locationManager.removeUpdates(listenerCoarse);
//        locationManager.removeUpdates(listenerFine);
//        
//        if(null != currentLocation)
//        	lastLocation = currentLocation;
//        
//        currentLocation = null;
//        currentPnt = null;
//    }
//    
//    /* Creates listeners */
//    private void createLocationListeners() {
//    	Log.d(D_TAG, "createLocationListeners");
//    	
//    	if(null == listenerGPSStatus)
//	        listenerGPSStatus = new GpsStatus.Listener() {
//	
//	    		@Override
//	    		public void onGpsStatusChanged(int status) {
//	    			switch(status)
//	    			{
//	    			case GpsStatus.GPS_EVENT_STARTED:
//	    				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_STARTED");
//	    				break;
//	    			case GpsStatus.GPS_EVENT_STOPPED:
//	    				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_STOPPED");
//	    				break;
//	    			case GpsStatus.GPS_EVENT_FIRST_FIX:
//	    				Log.d(D_TAG, "listenerGPSStatus: GPS_EVENT_FIRST_FIX");
//	    				isFineAvailable = true;
//	    				break;
//	    			}
//	    		}
//	        	
//	        };
//    	
//        if(null == listenerCoarse)
//	        listenerCoarse = new LocationListener() {
//	            public void onStatusChanged(String provider, int status, Bundle extras) {
//	                switch(status) {
//	                case LocationProvider.OUT_OF_SERVICE:
//	                	Log.d(D_TAG, "Coarse onStatusChanged OUT_OF_SERVICE");
//	                	isCoarseAvailable = false;
//	                    break;
//	                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//	                	Log.d(D_TAG, "Coarse onStatusChanged TEMPORARILY_UNAVAILABLE");
//	                	isCoarseAvailable = false;
//	                    break;
//	                case LocationProvider.AVAILABLE:
//	                	Log.d(D_TAG, "Coarse onStatusChanged AVAILABLE");
//	                	isCoarseAvailable = true;
//	                }
//	                
//	                if(!isCoarseAvailable && !isFineAvailable && null == currentPnt)
//	                	disableMapMode();
//	                else
//	                	enableMapMode();
//	            }
//	            public void onProviderEnabled(String provider) {
//	            	Log.d(D_TAG, "Coarse onProviderEnabled");
//	            }
//	            public void onProviderDisabled(String provider) {
//	            	Log.d(D_TAG, "Coarse onProviderDisabled");
//	            	
//	            	isCoarseAvailable = false;
//	            	
//	            	if(!isCoarseAvailable && !isFineAvailable && null == currentPnt)
//	                	disableMapMode();
//	                else
//	                	enableMapMode();
//	            }
//	            public void onLocationChanged(Location location) {
//	            	if(isFineAvailable)
//	            	{
//	            		Log.d(D_TAG, "Coarse onLocationChange: fineLocation available, coarseListener is going to disable now");
//	            		isCoarseAvailable = false;
//	            		locationManager.removeUpdates(this);
//	            	}
//	            	else
//	            	{
//	            		isCoarseAvailable = true;
//	            		mapOnLocationChange(location);
//		                
//		                Log.d(D_TAG, "Coarse onLocationChange, Loc: " + Integer.toString(currentPnt.getLatitudeE6()) + ", " + Integer.toString(currentPnt.getLongitudeE6()));
//		            	Log.d(D_TAG, "Coarse onLocationChange, Acc: " + Boolean.toString(location.hasAccuracy()) + ", " + Float.toString(location.getAccuracy()));
//	            	}
//	            }
//	        };
//        
//        if(null == listenerFine)
//	        listenerFine = new LocationListener() {
//	            public void onStatusChanged(String provider, int status, Bundle extras) {
//	                switch(status) {
//	                case LocationProvider.OUT_OF_SERVICE:
//	                	Log.d(D_TAG, "Fine onStatusChanged OUT_OF_SERVICE");
//	                	isFineAvailable = false;
//	                	
//	                	if(!isCoarseAvailable)
//	                	{
//	                		Log.d(D_TAG, "Fine onStatusChanged OUT_OF_SERVICE: coarse not available, try to enable the coarseListener now");
//	                		
//	                		// Remove the old one (if existed) to prevent duplicate registration
//	                		locationManager.removeUpdates(listenerCoarse);
//	                		
//	                		try
//	                        {
//	                			Criteria coarse = new Criteria();
//	                	        coarse.setAccuracy(Criteria.ACCURACY_COARSE);
//	                        	locationManager.requestLocationUpdates(locationManager.getBestProvider(coarse, true), 500, 100, listenerCoarse);
//	                        }
//	                        catch (IllegalArgumentException ae)
//	                        {
//	                        	Log.e(D_TAG, "requestLocationUpdates-coarse: provider not found --> failed to enable the coarseListener");
//	                        	isCoarseAvailable = false;
//	                        }
//	                	}
//	                    break;
//	                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//	                	Log.d(D_TAG, "Fine onStatusChanged TEMPORARILY_UNAVAILABLE");
//	                	isFineAvailable = false;
//	                    break;
//	                case LocationProvider.AVAILABLE:
//	                	Log.d(D_TAG, "Fine onStatusChanged AVAILABLE");
//	                	isFineAvailable = true;
//	                }
//	                
//	                if(!isCoarseAvailable && !isFineAvailable && null == currentPnt)
//	                	disableMapMode();
//	                else
//	                	enableMapMode();
//	            }
//	            public void onProviderEnabled(String provider) {
//	            	Log.d(D_TAG, "Fine onProviderEnabled");
//	            }
//	            public void onProviderDisabled(String provider) {
//	            	Log.d(D_TAG, "Fine onProviderDisabled");
//	            	
//	            	isFineAvailable = false;
//	            	
//	            	if(!isCoarseAvailable && !isFineAvailable && null == currentPnt)
//	                	disableMapMode();
//	                else
//	                	enableMapMode();
//	            }
//	            public void onLocationChanged(Location location) {
//	            	isFineAvailable = true;
//	            	mapOnLocationChange(location);
//	                
//	                Log.d(D_TAG, "Fine onLocationChange, Loc: " + Integer.toString(currentPnt.getLatitudeE6()) + ", " + Integer.toString(currentPnt.getLongitudeE6()));
//	            	Log.d(D_TAG, "Fine onLocationChange, Acc: " + Boolean.toString(location.hasAccuracy()) + ", " + Float.toString(location.getAccuracy()));
//	            }
//	        };
//    }
//    
//    private void mapOnLocationChange(Location location)
//    {
//    	currentLocation = location;
//        currentPnt = new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6));
//        
//        // Panning map automatically according to current position in map-mode 2/3
//        if(MAP_MODE_1 != currentMapMode)
//        	mapCtrl.animateTo(currentPnt);
//    }
    
/**** Location management - End */
    
/**** Some code for map mode initialization - Start */
	
//	// Check the current status of the location information
//    if(null == myLocationOverlay.getLastFix())
//    {
//    	Log.d(D_TAG, "Check last-fix: null, the btnMapMode will be disabled now");
//    	disableMapMode();
//    	
//    	// Enable the mapMode controller only when the location information is available at cold-startup
//    	// NOTE: you should use the local handler to trigger the Runnable() that involved the local view elements,
//    	// NOTE: or you will get the exception message: "Only the original thread that created a view hierarchy can touch its views"
//        myLocationOverlay.runOnFirstFix(new Runnable() {	
//    		@Override
//    		public void run() {
//    			handler.post(runEnableMapMode);
//    		}
//    	});
//    }
//    else
//    	Log.d(D_TAG, "Check last-fix: found, the last btnMapMode status (enable/disable) will not be changed");
    
/**** Some code for map mode initialization - End */
	
/**** Data restore for map position & zoom level - Start */
	
//	private static final String IBUNDLE_CENTER_LAT = "instanceBundleCurrentCenterLat";
//	private static final String IBUNDLE_CENTER_LON = "instanceBundleCurrentCenterLon";
//	private static final String IBUNDLE_CUR_ZOOM = "instanceBundleCurrentZoom";
    
//    @Override
//	protected void onSaveInstanceState(Bundle outState) {
//    	Log.v(D_TAG, "PMap: onSaveInstanceState");
//    	
//    	// Basic save: map status for outdoor map
//    	GeoPoint lastCenterPoint = mapView.getMapCenter();
//    	outState.putInt(IBUNDLE_CENTER_LAT, lastCenterPoint.getLatitudeE6());
//    	outState.putInt(IBUNDLE_CENTER_LON, lastCenterPoint.getLongitudeE6());	
//    	outState.putInt(IBUNDLE_CUR_ZOOM, currentLevel);
//    	
//		super.onSaveInstanceState(outState);
//	}
//    
//    @Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//    	Log.v(D_TAG, "PMap: onRestoreInstanceState");
//    	
//    	// Basic restore: map status for outdoor map
//    	int lastCenterLat6E, lastCenterLon6E, lastZoom;
//    	
//    	lastCenterLat6E = savedInstanceState.getInt(IBUNDLE_CENTER_LAT, 91);
//    	lastCenterLon6E = savedInstanceState.getInt(IBUNDLE_CENTER_LON, 181);
//    	if(lastCenterLat6E != 91 && lastCenterLon6E != 181)
//    		mapCtrl.animateTo(new GeoPoint(lastCenterLat6E, lastCenterLon6E));
//    	
//    	lastZoom = savedInstanceState.getInt(IBUNDLE_CUR_ZOOM, -1);
//    	if(lastZoom != -1)
//    		mapCtrl.setZoom(lastZoom);
//    		
//		super.onRestoreInstanceState(savedInstanceState);
//	}
	
/**** Data restore for map position & zoom level - End */
	
/**** Sample code FOR OTHER ACTIVITY: call PMap up and move the map center to the given point - Start */
	
//	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + Double.toString(pLatitude) + "," + Double.toString(pLongitude) + "?z=" + Integer.toString(DEFAULT_ZOOM_LEVEL))));

/**** Sample code FOR OTHER ACTIVITY: call PMap up and move the map center to the given point - End */
	
/**** Overlay related - Start */
	
//	private void toggleOverlay(Object overlayInstance)
//	{
//		if(overlayList.contains(overlayInstance))
//			overlayList.remove(overlayInstance);
//		else
//			overlayList.add((Overlay)overlayInstance);
//		mapView.postInvalidate();
//	}
	
/**** Overlay related - End */
	
}