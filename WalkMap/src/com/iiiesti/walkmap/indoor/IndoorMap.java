package com.iiiesti.walkmap.indoor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customPanel.BookmarkDialog;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel;
import com.iiiesti.walkmap.database.BookmarkDBManager;
import com.iiiesti.walkmap.database.IndoorMapDBManager;
import com.iiiesti.walkmap.direction.DirectionDisplay;
import com.iiiesti.walkmap.direction.DirectionManager;
import com.iiiesti.walkmap.overlays.Overlay_Location;
import com.iiiesti.walkmap.searchable.IndoorPoiSearch;

public class IndoorMap extends Activity{
	/*This for debug*/
	private static final String TAG = "Map" ; 
	// We can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private static final int LONGPRESS = 3;
//	private static final int CLICK = 4;	// Tony COMMENTED @ 1st merge
	private static int touchMode = NONE;
	
	/* Tony ADDED @ 20110111 - start */
	
	private class BookmarkDialogForIndoorMap extends BookmarkDialog {

		public BookmarkDialogForIndoorMap(Context context,
				SimpleCursorAdapter bookmarkAdapter, Cursor bookmarkCursor,
				Overlay_Location locationOverlay, int operationMode,
				DirectionManager directionSetup) {
			super(context, bookmarkAdapter, bookmarkDB, bookmarkCursor, locationOverlay, operationMode,
					directionSetup);
		}

		@Override
		protected void mBookmarkOperation(int operationMode, int latE6,
				int lonE6, int addrLatE6, int addrLonE6, String name,
				String info, String buildID, int floorID) {
			bookmarkOperation(operationMode, latE6, lonE6, name, info, buildID, floorID);
		}

		@Override
		protected void animateToForOutdoor(GeoPoint targetLocation) {
			// Do nothing
		}
		
	}
	
	private class DirectionDisplayForIndoorMap extends DirectionDisplay {

		public DirectionDisplayForIndoorMap(Context context) {
			super(context);
		}

		@Override
		protected Point projectToPixel(GeoPoint point) {
			return IndoorDrawView.toPixel(point);
		}
		
	}
	
	private class DirectionManagerForIndoorMap extends DirectionManager {

		public DirectionManagerForIndoorMap(Context context, View parentView,
				DirectionDisplay display, int displayPosition) {
			super(context, parentView, display, displayPosition);
		}

		@Override
		protected void clearStartPointDisplay() {
			drawView.hideStartPoint();
		}

		@Override
		protected void clearEndPointDisplay() {
			drawView.hideEndPoint();
		}

		@Override
		protected void setStartPointDisplay(GeoPoint location, String buildID, int floorID, String popupText) {
			drawView.setStartPoint(location, buildID, floorID, popupText);
			showIndoorPanel(false);
		}

		@Override
		protected void setEndPointDisplay(GeoPoint location, String buildID, int floorID, String popupText) {
			drawView.setEndPoint(location, buildID, floorID, popupText);
			showIndoorPanel(false);
		}

		@Override
		protected void hidePopup() {
			drawView.hidePopup();
		}

		@Override
		protected void popupStartPoint() {
			drawView.popupStartPoint();
		}

		@Override
		protected void popupEndPoint() {
			drawView.popupEndPoint();
		}

		@Override
		protected void moveMapTo(int latE6, int lonE6, String buildID, int floorID) {
			if(buildID != null && floorID != 0)
				moveToCenter(lonE6, latE6, floorID, buildID);
			else
			{
				this.dataFinalize();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_MAP_VIEW + ":" + (double)latE6 / 1E6 + 
						"," + (double)lonE6 / 1E6)));
			}
		}

		@Override
		protected void openBookmarkForSetStart() {
			openBookmarkDialog(BOOKMARK_MODE_SET_START);
		}

		@Override
		protected void openBookmarkForSetEnd() {
			openBookmarkDialog(BOOKMARK_MODE_SET_END);
		}

		@Override
		protected void specialMoveToPoint(int pointType,
				GeoPoint location, String buildID, int floorID, String name,
				String info) {
			if(buildID != null && floorID != 0)
			{
				moveToCenter(location.getLongitudeE6(), location.getLatitudeE6(), floorID, buildID);
				
				switch(pointType)
				{
				case DirectionManager.SP_TYPE_START_POINT:
					drawView.popupStartPoint();
					break;
				case DirectionManager.SP_TYPE_END_POINT:
					drawView.popupEndPoint();
					break;
				}
			}
			else
			{
				switch(pointType)
				{
				case DirectionManager.SP_TYPE_START_POINT:
					directionSetup.dataFinalize();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_START_POINT + 
							"&lat=" + location.getLatitudeE6() + 
							"&lon=" + location.getLongitudeE6())));
					break;
				case DirectionManager.SP_TYPE_END_POINT:
					directionSetup.dataFinalize();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_END_POINT + 
							"&lat=" + location.getLatitudeE6() + 
							"&lon=" + location.getLongitudeE6())));
					break;
				}
			}
		}

		@Override
		protected String getDebugTag() {
			return "IndoorDirection";
		}
		
	}
	
	private static final String BUILD_PREFIX = "poi_";
	public static final String IDR_BUNDLE_STR_BUILD_ID = "buildingId";
	public static final String IDR_BUNDLE_INT_FLOOR_ID = "floorId";
	
	public static final int OP_CODE_NORMAL = 0;
	public static final int OP_CODE_BOOKMARK = 1;
	public static final int OP_CODE_MOVE_TO = 3;
	public static final String IDR_BUNDLE_INT_OP_CODE = "operationCode";
	
	public static final String IDR_BUNDLE_INT_BK_OP_MODE = "bookmarkOperationMode";
	public static final String IDR_BUNDLE_STR_PNT_NAME = "pointName";
	public static final String IDR_BUNDLE_STR_PNT_INFO = "pointInfo";
	public static final String IDR_BUNDLE_INT_PNT_LAT = "pointLatE6";
	public static final String IDR_BUNDLE_INT_PNT_LON = "pointLonE6";
	
	private Intent mIntent;
	private Bundle mBundle;
	private int remoteOperationCode;
	
	private boolean hasMovedForResult;
	private boolean transitAvoidStatus[]; 			// Maintains the current status of transit-avoidance
	private DirectionManager directionSetup;		// Direction-setup set
	private DirectionDisplay directionDisplay;		// Direction-display for direction-setup
	
	/* Tony ADDED @ 20110111 - end */
	
	//indoor map dir
	private static final String INDOOR_MAP_DIR = "/sdcard/walkmap/indoormap/";
	
	//connect for database searching
	private static final String CONNECT_OUT = "out";
//	private static final String CONNECT_MASS = "mass";	// Tony COMMENTED @ 1st merge
	
	private String nowBuildID;
	
	/*These matrices will be used to move and zoom image*/
	private Matrix matrix = new Matrix();
//    private Matrix originalMatrix = new Matrix();	// Tony COMMENTED @ 1st merge
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist;
    private int scaleID = 2;
    private static float mapScale[] = {0.5f, 0.75f, 1f, 1.5f, 2f};
	
	/*UI Parameter claim*/
	private ImageView mapImageView;
	private IndoorDrawView drawView;
	private GestureDetector gestureDetector;
	
	//mangreen modify 20110114
	private ImageView btnOutdoorSwitchBg;
	private TextView textMapInfo;
	private Button btnOutdoorSwitch;
	
	private Button btnFloorList;
	private Button btnDownstair;
	private Button btnUpStair;
	private Button btnZoomIn;
	private Button btnZoomOut;
	private Button btnOutdoorSubSwitch;
	private RelativeLayout layoutIndoor;
	private Bitmap bmpMap;
	//private int id=0;
	public static int displayWidth;
	public static int displayHeight;
	private float scaleWidth=1;
	private float scaleHeight=1;
	
	/*set bitmap topleft and downright point*/
	private Storey mainStorey = new Storey();
	private Storey tempStorey = new Storey();
	public static BitMapPoint topleft;
	public static BitMapPoint bottomright;
	private int maxFloor = 0;
	private int minBasement = 0;
	 
	// Related data adapter of floorlist
	private Cursor floorCursor;
	private SimpleCursorAdapter floorAdapter;
	
	public static final int REQUEST_POINT_DETAIL = 2;
	public static final int RESULT_PDETAIL_HAS_CENTER = 1;
	public static final int RESULT_PDETAIL_CLEAR_CUSTOMPIN_OVERLAY = 2;
	public static final int RESULT_PDETAIL_ADD_BOOKMARK = 3;
	public static final int RESULT_PDETAIL_SET_START = 4;
	public static final int RESULT_PDETAIL_SET_END = 5;
	public static final int BOOKMARK_MODE_NORMAL = 1;
	public static final int BOOKMARK_MODE_SET_START = 2;
	public static final int BOOKMARK_MODE_SET_END = 3;
	public static final int BOOKMARK_MODE_DELETE = 4;
	public static final int BOOKMARK_MODE_EDIT = 5;
	public static final int OVERLAY_CUSTOM_PIN = -1;
	public static final String INTENT_INT_1 = "intent_key_integer_1";
	
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
	
	
	//private static final int iii_13f = R.drawable.iii_13f;
	//private static final int iii_1f = R.drawable.iii_1f;
	//private static int drawableID = 0;
	
	/* Create DB resources */
	// DB master
	private IndoorMapDBManager idrmapDB;
	private BookmarkDBManager bookmarkDB;
	
	// Related data adapter
	private Cursor bookmarkCursor;
	private SimpleCursorAdapter bookmarkAdapter;
	
	public static final String INDOOR_SERVER_URL="http://140.92.13.231:8080/LBSServer/IndoorLBS.jsp";
	//public static final String INDOOR_SERVER_URL="http://140.92.13.128:8080/LBSServer/IndoorLBS.jsp";
	//public static final String INDOOR_SERVER_URL="http://10.0.2.2:8080/LBSServer/IndoorLBS.jsp";

	public static String indoorPoi="";
	public static int indoorPoiAmount = 0;
	public static boolean indoorPOIStatus[];
	public static String poi[];
	public static boolean indoorPOIReadyFlag = false;
	
	public static boolean REQUEST_POI_SEARCH = false;
	public static int poi_long=0;
	public static int poi_lat=0;
	public static int poi_floor=0;
	public static String poi_name="";
	
	private class openBookmarkDBTask extends AsyncTask<BookmarkDBManager, Void, BookmarkDBManager>
	{
		@Override
		protected BookmarkDBManager doInBackground(BookmarkDBManager... db) {
			try {
				Log.d(TAG, "openDBTask: Opening Bookmark data bases");
				db[0].open();
				db[0].setAvailable(true);
				return db[0];
			} catch (SQLiteException e) {
				Log.e(TAG, e.getMessage());
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
					
					bookmarkAdapter = new SimpleCursorAdapter(IndoorMap.this, 
							R.layout.simple_info, 
							bookmarkCursor, 
							new String[] {BookmarkDBManager.SPOINT_FIELD_NAME, BookmarkDBManager.SPOINT_FIELD_INFO}, 
							new int[] {R.id.simple_info_title, R.id.simple_info_info});					
				}
			}
			super.onPostExecute(db);
		}
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        /* Tony ADDED @ 20110111 */
        // Retrieve the bundle information, the intent and the corresponding bundle SHOULD NOT be null
        mIntent = this.getIntent();
        if(mIntent != null)
        {
        	mBundle = mIntent.getExtras();
        	if(mBundle != null)
        	{
        		nowBuildID = mBundle.getString(IDR_BUNDLE_STR_BUILD_ID).replaceAll(BUILD_PREFIX, "");
        		poi_floor = mBundle.getInt(IDR_BUNDLE_INT_FLOOR_ID, 0);
        		remoteOperationCode = mBundle.getInt(IDR_BUNDLE_INT_OP_CODE, OP_CODE_NORMAL);
        	}
        }
        
        initDB();
        
        /* Tony ADDED @ 20110113 - start */
        drawView = new IndoorDrawView(this);	// Tony MOVED @ 20110113
        
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layoutBase = (RelativeLayout)inflater.inflate(R.layout.idr_main, null);
        
        directionDisplay = new DirectionDisplayForIndoorMap(IndoorMap.this);
        directionSetup = new DirectionManagerForIndoorMap(IndoorMap.this, layoutBase, directionDisplay, 1);
        transitAvoidStatus = directionSetup.getTransitAvoidArray();
        hasMovedForResult = false;
        /* Tony ADDED @ 20110113 - end */
        
        /*Loading main.xml Layout*/
        setContentView(layoutBase);		// Tony MODIFIED @ 20110113
        
        /*Get screen dpi*/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;
        
        /*Display Height subtract Button Height*/
        displayHeight = dm.heightPixels - 80;
        
        /*Initial Parameters*/
        //bmpMap = BitmapFactory.decodeResource(getResources(), iii_13f);
        
        topleft = null;
    	bottomright = null;
        bmpMap = null;
        
        /* Tony ADDED @ 20110111 */
		if(0 != poi_floor)
			mainStorey = getStorey(nowBuildID, poi_floor);
		else
			mainStorey = getStorey(nowBuildID, CONNECT_OUT);
		
		indoorPOIStatus = new boolean[IndoorMap.this.getResources().getStringArray(R.array.indoor_layer_menu).length];
		poiLayerInit();
//        mainStorey = getStorey(nowBuildID, -1);	// Tony COMMENTED @ 20110111
//        setBitmap(mainStorey);					// Tony COMMENTED @ 20110111
        mapImageView = (ImageView)findViewById(R.id.mapImageView);
        mapImageView.setOnTouchListener(imageTouch);
//        mapImageView.setImageBitmap(bmpMap);		// Tony COMMENTED @ 20110111
    
        //drawView.setAnchorPoint(topleft, bottomright, mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
        drawView.setAnchorPoint(mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
        //drawView.setOnTouchListener(drawTouch);
        gestureDetector = new GestureDetector(onGestureListener);
        //setContentView(drawView);
        
        //drawImageView.setOnTouchListener(this); 
        layoutIndoor = (RelativeLayout)findViewById(R.id.layoutIndoor);
        layoutIndoor.addView(drawView, 1);
        
        textMapInfo = (TextView)findViewById(R.id.textMapInfo);
        btnOutdoorSwitchBg = (ImageView)findViewById(R.id.btnOutdoorSwitchBg);
        
        btnOutdoorSwitch = (Button)findViewById(R.id.btnOutdoorSwitch);
        btnOutdoorSwitch.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				//IndoorMap.this.finish();		
				directionSetup.quitSubStep();
				directionSetup.dataFinalize();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_START_POINT + 
						"&lat=" + (mainStorey.bottom+(mainStorey.top-mainStorey.bottom)/2) + 
						"&lon=" + (mainStorey.left+(mainStorey.right-mainStorey.left)/2))));
				IndoorMap.this.finish();
			}
        });
        
        btnOutdoorSubSwitch = (Button)findViewById(R.id.btnOutdoorSwitch_sub);
        btnOutdoorSubSwitch.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				//IndoorMap.this.finish();		
				directionSetup.quitSubStep();
				directionSetup.dataFinalize();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_START_POINT + 
						"&lat=" + (mainStorey.bottom+(mainStorey.top-mainStorey.bottom)/2) + 
						"&lon=" + (mainStorey.left+(mainStorey.right-mainStorey.left)/2))));
				IndoorMap.this.finish();
			}
        });
        
        btnFloorList = (Button)findViewById(R.id.btnFloorList);
    	btnDownstair = (Button)findViewById(R.id.btnDownstair);
    	btnUpStair = (Button)findViewById(R.id.btnUpstair);
        
    	if(minBasement == mainStorey.floor){
    		btnDownstair.setEnabled(false);
    	}
    	if(maxFloor == mainStorey.floor){
    		btnUpStair.setEnabled(false);
    	}
    	
        btnZoomIn = (Button)findViewById(R.id.btnZoomIn);
        btnZoomOut = (Button)findViewById(R.id.btnZoomOut);
        
        btnFloorList.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				//moveToCenter(121554344, 25058814, 1, "iii");
				openFloorListDialog();
				//Log.d(TAG, "IndoorPOI: "+indoorPoi);
			}
        });

        btnDownstair.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				downStair();
			}
        });
        
        btnUpStair.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				upStair();
			}
        });
        
        /*Shrink button onClickListener*/
        btnZoomIn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				zoomIn();
			}
        });
        /*Blow-up button onClickListener*/
        btnZoomOut.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				zoomOut();
			}
        });
    }
    
    @Override
    protected void onStart() {
    	try
    	{
    		Log.v(TAG, "onStart");
    		
    		System.gc();
        	
        	/* Tony MOVED @ 20110111 */
        	setBitmap(mainStorey);
        	mapImageView.setImageBitmap(bmpMap);
        	if(!hasMovedForResult)
        	{
        		moveToCenter(mainStorey.left+(mainStorey.right-mainStorey.left)/2, 
            			mainStorey.bottom+(mainStorey.top-mainStorey.bottom)/2,
            			mainStorey.floor,
            			mainStorey.buildID);
        		hasMovedForResult = false;
        	}
        	
        	super.onStart();
    	}
    	catch (OutOfMemoryError e) {
			Log.e(TAG, "OutOfMemoryError @ onStart(): " + e.toString());
		}
    }
    
	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
	    
	    if(true == REQUEST_POI_SEARCH){
	    	Log.d(TAG, "onResume: " + poi_long +", "+ poi_lat +", "+ poi_floor +", "+ poi_name);
	    	
	    	if(moveToCenter(poi_long, poi_lat, poi_floor, nowBuildID)){
	    		drawView.setPressPoint(poi_long, poi_lat, poi_name);
	    	}else{
	    		new AlertDialog.Builder(this)
				.setTitle(R.string.indoor_no_map)
				.setIcon(R.drawable.ic_dialog_info)
				.setMessage(R.string.indoor_no_map_message)
				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            })
				.show();
	    	}
	    	REQUEST_POI_SEARCH = false;
	    }
	    
	    /* Tony ADDED @ 20110112 - start */
	    
	    // Data restoration
        directionSetup.dataRestoration();
        transitAvoidStatus = directionSetup.getTransitAvoidArray();
	    
	    if(mBundle != null)
	    {
	    	int latE6 = mBundle.getInt(IDR_BUNDLE_INT_PNT_LAT, 91000000);
	    	int lonE6 = mBundle.getInt(IDR_BUNDLE_INT_PNT_LON, 181000000);
	    	if(latE6 != 91000000 && lonE6 != 181000000)
	    	{
	    		switch(remoteOperationCode)
			    {
			    case OP_CODE_BOOKMARK:
			    	bookmarkOperation(mBundle.getInt(IDR_BUNDLE_INT_BK_OP_MODE, BOOKMARK_MODE_NORMAL),
			    			latE6, lonE6,
			    			mBundle.getString(IDR_BUNDLE_STR_PNT_NAME),
			    			mBundle.getString(IDR_BUNDLE_STR_PNT_INFO),
			    			nowBuildID,
			    			poi_floor);
			    	break;
			    case OP_CODE_MOVE_TO:
			    	moveToCenter(lonE6, latE6, poi_floor, nowBuildID);
			    	break;
			    }
	    	}
	    	mBundle.clear();				// Clear the old settings
	    	mIntent.putExtras(mBundle);
	    }
	    
	    if(directionSetup.isEnabled())
	    	showIndoorPanel(false);
	    else
	    	showIndoorPanel(true);
	    	
	    /* Tony ADDED @ 20110112 - end */
	    super.onResume();
	}
    
    @Override
    protected void onPause() {
    	Log.v(TAG, "onPause");
    	
    	// Tony ADDED @ 20110113
    	// Store the last direction settings
        directionSetup.dataFinalize();
        
        super.onPause();
        System.gc();
    }
    
    @Override
    protected void onStop() {
    	Log.v(TAG, "onStop");
    	
    	/* Tony ADDED @ 20110111 */
		if(bmpMap != null)
		{
			bmpMap.recycle();
			bmpMap = null;
		}
		
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.v(TAG, "onDestroy");
    	closeDB();
    	super.onDestroy();
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	Log.v(TAG, "onConfigurationChanged");
    	
    	// [NOTE] When you declare your Activity to handle a configuration change, 
    	// you are responsible for resetting any elements for which you provide alternatives. 
    	// If you declare your Activity to handle the orientation change and have images that should change 
    	// between landscape and portrait, you must re-assign each resource to each element during onConfigurationChanged().
    	
		super.onConfigurationChanged(newConfig);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.v(TAG, "onActivityResult");
    	drawView.pressFlag = false;
    	drawView.pin.animateDone = true;
    	
    	Bundle bundle = null;
    	
    	if(intent != null){
    		bundle = intent.getExtras();
    	}
    	
    	if(resultCode != RESULT_CANCELED)
			switch(requestCode)
			{
			case REQUEST_POINT_DETAIL:
				hasMovedForResult = true;
				
				switch(resultCode){
				
				case RESULT_PDETAIL_HAS_CENTER:
					Log.d(TAG, "RESULT_PDETAIL_HAS_CENTER");
					moveToCenter(drawView.pin.getIntLontitude(), drawView.pin.getIntLatitude(), drawView.pin.getFloor(), drawView.pin.getBuildID());
					break;
					
				case RESULT_PDETAIL_CLEAR_CUSTOMPIN_OVERLAY:
					drawView.pin.pinFlag = false;
					break;
					
				case RESULT_PDETAIL_ADD_BOOKMARK:
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
						Log.d(TAG, "RESULT_PDETAIL_ADD_BOOKMARK");
					}
					break;
					
				case RESULT_PDETAIL_SET_START:
					Log.d(TAG, "RESULT_PDETAIL_SET_START");
					if(bundle != null){
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
					}
					break;
				case RESULT_PDETAIL_SET_END:
					Log.d(TAG, "RESULT_PDETAIL_SET_END");
					if(bundle != null){
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
					}
					break;
				}
				break;
			}
    	
    	super.onActivityResult(requestCode, resultCode, intent);
	}
    
    /* Tony ADDED @ 20110113 : Direction setup control */
    private void showIndoorPanel(boolean show)
    {
    	if(show)
    	{
    		btnOutdoorSwitch.setVisibility(View.VISIBLE);
    		textMapInfo.setVisibility(View.VISIBLE);
    		btnOutdoorSwitchBg.setVisibility(View.VISIBLE);
    		btnOutdoorSubSwitch.setVisibility(View.GONE);
    	}
    	else
    	{
    		btnOutdoorSwitch.setVisibility(View.GONE);
    		textMapInfo.setVisibility(View.GONE);
    		btnOutdoorSwitchBg.setVisibility(View.GONE);
    		btnOutdoorSubSwitch.setVisibility(View.VISIBLE);
    	}
    }
    
    public void exitDirectionSetup()
    {
    	directionSetup.hide();
    	directionSetup.dataFinalize();
    }
    
    @Override
	public void onBackPressed() {
    	Log.v(TAG, "onBackPressed");
    	
    	// Tony ADDED @ 20110113
    	if(directionSetup.isEnabled())
    	{
    		directionSetup.quitSubStep();
			exitDirectionSetup();
			showIndoorPanel(true);
    	}
		else
			super.onBackPressed();
	}
    
    @Override
	public boolean onSearchRequested() {
    	Log.v(TAG, "onSearchRequested");
    	searchPoi();
		return false;			// Disable the default launch
	}
    
    /* OptionsMenu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)	// Executed once only at the menu first created
	{
		Log.v(TAG, "onCreateOptionsMenu");
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pmap_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 	// Executed each time the menu is going to be opened
	{
		Log.v(TAG, "onPrepareOptionsMenu");

		// Tony ADDED @ 20110113
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
		Log.v(TAG, "onOptionsItemSelected");
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId())
		{
		case MENUITEM_SEARCH:
			/*
			new AlertDialog.Builder(PMap.this)
			.setTitle(R.string.pmap_menu_search)
			.setItems(R.array.search_menu, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	switch(which)
                	{
                	case 0:*/
			searchPoi();
         
            			/*
            			break;
                	}
                }
            })
            .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })
			.show();
			*/
			break;
		case MENUITEM_DIRECTION:
			// Tony ADDED @ 20110113
			showIndoorPanel(false);
			directionSetup.show();
			break;
		case MENUITEM_LAYER:
			
			new AlertDialog.Builder(IndoorMap.this)
			.setIcon(R.drawable.ic_dialog_dialer)
            .setTitle(R.string.pmap_menu_layer)
            .setMultiChoiceItems(R.array.indoor_layer_menu, indoorPOIStatus,
            new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                	if(0 == whichButton && true == indoorPOIStatus[whichButton]){
                		Arrays.fill(indoorPOIStatus, true);
      	
                	}else if(0 == whichButton && false == indoorPOIStatus[whichButton]){
                		
                		Arrays.fill(indoorPOIStatus, false);
                		for(int i=0; i<indoorPOIStatus.length; i++){  
                			((AlertDialog)dialog).getListView().setItemChecked(i, false);
                		}      
                	}
                }
            })
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	drawView.invalidate();
                	//itemizedOverlaysUpdate();
                }
            })
            .setNegativeButton(R.string.btn_clear, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	Arrays.fill(indoorPOIStatus, false);
                	drawView.invalidate();
                	//itemizedOverlaysUpdate();
                }
            })
            .show();
            
			break;
		case MENUITEM_BOOKMARK:
			openBookmarkDialog(BOOKMARK_MODE_NORMAL);
			break;
		case MENUITEM_CLEANMAP:
			Arrays.fill(indoorPOIStatus, false);
			drawView.hide();
			drawView.invalidate();
			break;
			
		case MENUITEM_ABOUT:
			new AlertDialog.Builder(IndoorMap.this)
				.setTitle(R.string.pmap_about_title)
				.setIcon(R.drawable.ic_dialog_info)
				.setMessage(R.string.pmap_about_message)
				.setPositiveButton(R.string.btn_ok, null)
				.show();
			break;
			
		// Tony ADDED @ 20110113
		// For direction setup menu
		case MENUITEM_DIRECTION_QUIT:
			directionSetup.quitSubStep();
			exitDirectionSetup();
			showIndoorPanel(true);
			break;
		case MENUITEM_DIRECTION_REVERSE:
			directionSetup.reverseStartEnd();
			break;
		case MENUITEM_DIRECTION_CLEAR:
			directionSetup.dataInitialize();
			getSharedPreferences(PMap.PREFNAME_DIRECTION, 0).edit().clear().commit();	// Clear the old data
			break;
		case MENUITEM_DIRECTION_MT_OPTION:
			new AlertDialog.Builder(IndoorMap.this)
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
    
    /*Method of down stair*/
    private void downStair(){
    	Cursor cursor = idrmapDB.selectDownstairs(mainStorey.buildID, mainStorey.floor);
    	
    	if(0 != cursor.getCount()){
			cursor.moveToLast();
			setStorey(mainStorey, cursor);
		}
    	setBitmap(mainStorey);
    	mapImageView.setImageBitmap(bmpMap);
    	//drawView.setAnchorPoint(topleft, bottomright, mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
    	drawView.setAnchorPoint(mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
    	
    	// Tony ADDED @ 20110125
    	if(directionSetup.getCurIndoorSubFloor() != mainStorey.floor)
    		directionDisplay.hideLine();
    	else
    		directionDisplay.showLine();
    	
    	drawView.invalidate();
    	
    	if(minBasement == mainStorey.floor){
    		btnDownstair.setEnabled(false);
    	}
    	if(maxFloor != mainStorey.floor){
    		btnUpStair.setEnabled(true);
    	}
    	cursor.close();
    }
    
    /*Method of up stair*/
    private void upStair(){
    	Cursor cursor = idrmapDB.selectUpstairs(mainStorey.buildID, mainStorey.floor);
    	
    	if(0 != cursor.getCount()){
			cursor.moveToFirst();
			setStorey(mainStorey, cursor);
		}
    	setBitmap(mainStorey);
    	mapImageView.setImageBitmap(bmpMap);
    	textMapInfo.setText(mainStorey.floorname);
    	//drawView.setAnchorPoint(topleft, bottomright, mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
    	drawView.setAnchorPoint(mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
    	
    	// Tony ADDED @ 20110125
    	if(directionSetup.getCurIndoorSubFloor() != mainStorey.floor)
    		directionDisplay.hideLine();
    	else
    		directionDisplay.showLine();
    	
    	drawView.invalidate();
    	
    	if(maxFloor == mainStorey.floor){
    		btnUpStair.setEnabled(false);
    	}
    	if(minBasement != mainStorey.floor){
    		btnDownstair.setEnabled(true);
    	}
    	cursor.close();
    }
    
    /*Method of shrinking images*/
    private void zoomOut(){

    	// Tony COMMENTED @ 1st merge
//    	int bmpWidth = bmpMap.getWidth();
//    	int bmpHeight = bmpMap.getHeight();
//    	
//    	/*Set image shrinking scale*/
//    	double scale = 0.5;
//    	
//    	/*Calculate shrinking scale*/
//    	scaleWidth = (float)(scaleWidth*scale);
//    	scaleHeight = (float)(scaleHeight*scale);
    	
    	/*Product Bitmap after reSize*/
    	//Matrix bmpMatrix = new Matrix();
    	//bmpMatrix.postScale(scaleWidth, scaleHeight);
    	//Bitmap resizeBmp = Bitmap.createBitmap(bmpMap, 0, 0, bmpWidth, bmpHeight, bmpMatrix, true);
    	
    	/*
    	if(id == 0){
    		//If first push, remove original ImageView/
    		layout1.removeView(mImageView);
    	}else{
    		//If not, remove last ImageView/
    		layout1.removeView((ImageView)findViewById(id));
    	}
    	//produce new ImageView, push to Bitmap of reSize, and then push to Layout/
    	id++;
    	ImageView imageView = new ImageView(IndoorMap.this);
    	imageView.setId(id);
    	imageView.setImageBitmap(resizeBmp);
    	imageView.setScaleType(ImageView.ScaleType.MATRIX);
    	layout1.addView(imageView,0);
    	setContentView(layout1);
    	*/
    	//mapImageView.setImageBitmap(resizeBmp);
    	if(1 == scaleID){
    		scaleID = 0;
    		matrix.postScale((1f/mapScale[1]), (1f/mapScale[1]), displayWidth/2, displayHeight/2);
    		topleft.postScale((1f/mapScale[1]), (1f/mapScale[1]), displayWidth/2, displayHeight/2);
        	bottomright.postScale((1f/mapScale[1]), (1f/mapScale[1]), displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		mapImageView.setImageMatrix(matrix);
        	btnZoomOut.setEnabled(false);
    	}else if(2 == scaleID){
    		scaleID = 1;
    		
    		matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		mapImageView.setImageMatrix(matrix);
    	}else if(3 == scaleID){
    		scaleID = 2;
    		matrix.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
    		topleft.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
        	bottomright.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		mapImageView.setImageMatrix(matrix);
    	}else if(4 == scaleID){
    		scaleID = 3;
    		matrix.postScale(1f/mapScale[4], 1f/mapScale[4], displayWidth/2, displayHeight/2);
    		topleft.postScale(1f/mapScale[4], 1f/mapScale[4], displayWidth/2, displayHeight/2);
        	bottomright.postScale(1f/mapScale[4], 1f/mapScale[4], displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    		mapImageView.setImageMatrix(matrix);
        	btnZoomIn.setEnabled(true);
    	}

    	//drawView.setAnchorPoint(topleft, bottomright);

    	//Log.d(TAG, "dx:" + (event.getX()-start.x) + ", " + ", dy:" + (event.getY()-start.y));
	    Log.d(TAG, "top(" + topleft.x + ", " + topleft.y +"), down(" + bottomright.x + ", " + bottomright.y);
    	//Matrix pmatrix = new Matrix();
    	//pmatrix.postTranslate(50f, 50f);
    	//mImageView.setImageMatrix(pmatrix);
    	
    	/*enable blow-up button*/
    	//btnZoomOut.setEnabled(true);
    }
    
    /*Method of blow-up images*/
    private void zoomIn(){

    	int bmpWidth = bmpMap.getWidth();
    	int bmpHeight = bmpMap.getHeight();
    	
    	/*Set blow-up scale*/
    	double scale = 2;
    	
    	/*Calculate blow-up scale*/
    	scaleWidth = (float)(scaleWidth*scale);
    	scaleHeight = (float)(scaleHeight*scale);
    	
    	/*Product Bitmap after reSize*/
    	//Matrix bmpMatrix = new Matrix();
    	//bmpMatrix.postScale(scaleWidth, scaleHeight);
    	//Bitmap resizeBmp = Bitmap.createBitmap(bmpMap, 0, 0, bmpWidth, bmpHeight, bmpMatrix, true);
    	
    	/*
    	if(id == 0){
    		//If first push, remove original ImageView
    		layout1.removeView(mImageView);
    	}else{
    		//If not, remove last ImageView
    		layout1.removeView((ImageView)findViewById(id));
    	}
    	//produce new ImageView, push to Bitmap of reSize, and then push to Layout
    	id++;
    	ImageView imageView = new ImageView(IndoorMap.this);
    	imageView.setId(id);
    	imageView.setImageBitmap(resizeBmp);
    	imageView.setScaleType(ImageView.ScaleType.MATRIX);
    	layout1.addView(imageView,0);
    	setContentView(layout1);
    	*/
    	
    	//mapImageView.setImageBitmap(resizeBmp);

    	if(0 == scaleID){
    		scaleID = 1;
    		matrix.postScale(1f/mapScale[0], 1f/mapScale[0], displayWidth/2, displayHeight/2);
    		topleft.postScale(1f/mapScale[0], 1f/mapScale[0], displayWidth/2, displayHeight/2);
        	bottomright.postScale(1f/mapScale[0], 1f/mapScale[0], displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
        	btnZoomOut.setEnabled(true);
    	}else if(1 == scaleID){
    		scaleID = 2;
    		matrix.postScale(1f/mapScale[1], 1f/mapScale[1], displayWidth/2, displayHeight/2);
    		topleft.postScale(1f/mapScale[1], 1f/mapScale[1], displayWidth/2, displayHeight/2);
        	bottomright.postScale(1f/mapScale[1], 1f/mapScale[1], displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	}else if(2 == scaleID){
    		scaleID = 3;
    		
    		matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	}else if(3 == scaleID){
    		scaleID = 4;
    		matrix.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
    		topleft.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
        	bottomright.postScale(1f/mapScale[3], 1f/mapScale[3], displayWidth/2, displayHeight/2);
        	
        	matrix.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    topleft.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
    	    bottomright.postScale(mapScale[scaleID], mapScale[scaleID], displayWidth/2, displayHeight/2);
        	btnZoomIn.setEnabled(false);
    	}
    	
    	mapImageView.setImageMatrix(matrix);
    	//drawView.setAnchorPoint(topleft, bottomright);
    	//Log.d(TAG, "dx:" + (event.getX()-start.x) + ", " + ", dy:" + (event.getY()-start.y));
	    Log.d(TAG, "top(" + topleft.x + ", " + topleft.y +"), down(" + bottomright.x + ", " + bottomright.y);
    	
    	/*If blow-up over the display size, disable blow-up button*/
    	if(scaleWidth*scale*bmpWidth > displayWidth || scaleHeight*scale*bmpHeight > displayHeight){
    		//mButton02.setEnabled(false);
    	}
    }
    
    public boolean moveToCenter(int longtitude, int latitude, int floor, String buildID){
    	boolean setBitmapFlag = true;
    	
    	if(mainStorey.buildID != buildID || mainStorey.floor != floor){
    		
    		if(mainStorey.buildID != buildID){
    			setMaxAndMinFloor(buildID);
    		}
    		
    		tempStorey = mainStorey;
    		mainStorey = getStorey(buildID, floor);
    		
    		if(true == setBitmap(mainStorey)){
	        	mapImageView.setImageBitmap(bmpMap);
	        	//drawView.setAnchorPoint(topleft, bottomright, mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
	        	drawView.setAnchorPoint(mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
	        	if(maxFloor == mainStorey.floor){
		    		btnUpStair.setEnabled(false);
		    	}else{
		    		btnUpStair.setEnabled(true);
		    	}
		    	if(minBasement == mainStorey.floor){
		    		btnDownstair.setEnabled(false);
		    	}else{
		    		btnDownstair.setEnabled(true);
		    	}
    		}else{
    			mainStorey = tempStorey;
    			setBitmapFlag = false;
    		}
    	}

    	float offsetX = displayWidth/2 - IndoorDrawView.longitudeToScreenX(longtitude);
    	float offsetY = displayHeight/2 - IndoorDrawView.latitudeToScreenY(latitude);
    	
    	// 設置位移
		matrix.postTranslate(offsetX, offsetY);
		topleft.postTranslate(offsetX, offsetY);
	    bottomright.postTranslate(offsetX, offsetY);
	    mapImageView.setImageMatrix(matrix);
	    //drawView.setAnchorPoint(topleft, bottomright);
	    drawView.invalidate();
	    
	    return setBitmapFlag;
    }
    
    private View.OnTouchListener imageTouch = new View.OnTouchListener() {
	    public boolean onTouch(View v, MotionEvent event) {
	        // Handle touch events here...
	        ImageView view = (ImageView) v;
	
	        // Handle touch events here...
	        switch (event.getAction() & MotionEvent.ACTION_MASK) {
	        	//设置?��?模�?
	          	case MotionEvent.ACTION_DOWN:
	          		savedMatrix.set(matrix);
	          		start.set(event.getX(), event.getY());
	          		topleft.setTempPlace();
	          		bottomright.setTempPlace();
	          		Log.d(TAG, "mode=DRAG");
	          		touchMode = DRAG;
	          		break;

	          	case MotionEvent.ACTION_UP:
	          	case MotionEvent.ACTION_POINTER_UP:
	          		touchMode = NONE;
	          		Log.d(TAG, "mode=NONE" );
	          		break;
	          	//设置多点触摸模�?
	          	case MotionEvent.ACTION_POINTER_DOWN:
	          		oldDist = spacing(event);
	          		Log.d(TAG, "oldDist=" + oldDist);
	          		if(oldDist > 10f){
	          			savedMatrix.set(matrix);
	          			midPoint(mid, event);
	          			touchMode = ZOOM;
	          			Log.d(TAG, "mode=ZOOM" );
	          		}
	          		break;
	            //?�為DRAG模�?，�?移�??��?
	          	case MotionEvent.ACTION_MOVE:
	          		if(touchMode == DRAG){
	          			//設�?起�?觸控位置
	          			matrix.set(savedMatrix);
	          			topleft.restorePlace();
	          			bottomright.restorePlace();
	          			// 設置位移
	          			matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
	          			topleft.postTranslate(event.getX() - start.x, event.getY() - start.y);
	          	        bottomright.postTranslate(event.getX() - start.x, event.getY() - start.y);
	          	        //drawView.setAnchorPoint(topleft, bottomright);
	          	        Log.d(TAG, "top(" + topleft.x + ", " + topleft.y +"), down(" + bottomright.x + ", " + bottomright.y);
	          		}else if(touchMode == ZOOM){//?�為ZOOM模�?，�?多�?觸控縮放
	          			float newDist = spacing(event);
	          			Log.d(TAG, "newDist=" + newDist);
	          			if (newDist > 10f) {
	          				//matrix.set(savedMatrix);
	          				//topleft.restore();
	              			//downright.restore();
	          				float scale = newDist / oldDist;
	          				//設置說縮?��?例�??�中點�?�
	          				//matrix.postScale(scale, scale, mid.x, mid.y);
	          				//topleft.postScale((float)scale, (float)scale, displayWidth/2, displayHeight/2);
	          		    	//downright.postScale((float)scale, (float)scale, displayWidth/2, displayHeight/2);
	          		    	//drawView.setAnchorPoint(topleft, downright);
	          				
	          		    	if(scale < 0.5f){
	          					zoomOut();
	          					return true; // indicate event was handled
	          				}else if(scale > 2f){
	          					zoomIn();
	          					return true; // indicate event was handled
	          				}
	          			}
	          		}
	          		break;
	        }
	
	        // Perform the transformation
	        view.setImageMatrix(matrix);
	        gestureDetector.onTouchEvent(event);
	        return true; // indicate event was handled
	    }
    };
    
    //Calculate moving distance
    private float spacing(MotionEvent event) {
    	float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
     
    //Calculate center point position
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    /*
    // Connect to the MotionEvent input
	private View.OnTouchListener drawTouch = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			//gestureDetector.onTouchEvent(event);		
			return false;
		}
	};*/
	
	// Create the gesture detector
	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
		
		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "click!!!!!!!!!!!");
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
				drawView.setPressPoint(e.getX(), e.getY());
				touchMode = LONGPRESS;
				Log.d(TAG, "press!!!!!!!!!!!");
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			drawView.checkClickPoint(e.getX(), e.getY());
			return true;
		}
	};
	
	private void initDB(){
		Log.d(TAG, "initDB");
		
		bookmarkCursor = null;
		bookmarkAdapter = null;
		
        bookmarkDB = new BookmarkDBManager(IndoorMap.this);
        
        new openBookmarkDBTask().execute(bookmarkDB);
		
        idrmapDB = new IndoorMapDBManager(this);
        //idrmapDB.getReadableDatabase();
        try{
        	idrmapDB.insertData();
        }catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
        //idrmapDB.getAll();
        //Cursor cursor = idrmapDB.selectMapByFloor("iii", 13);
        
        floorCursor = idrmapDB.getAll(nowBuildID);
        startManagingCursor(floorCursor);
        
        setMaxAndMinFloor(nowBuildID);
	}
	
	private void closeDB(){
		Log.d(TAG, "closeDB");
		
		idrmapDB.close();
		
		if(bookmarkDB.availability())
			bookmarkDB.close();
		
	}
	
	private void setMaxAndMinFloor(String buildID){
        setFloorAdapter(buildID);
        floorCursor.moveToFirst();
        maxFloor = floorCursor.getInt(2);
        floorCursor.moveToLast();
        minBasement = floorCursor.getInt(2);
	}
	
	private Storey getStorey(String buildID, String connect){
		Storey tempStorey = new Storey();
		Cursor cursor = idrmapDB.selectMapByConnect(buildID, connect);
		if(0 != cursor.getCount()){
			cursor.moveToFirst();
			tempStorey.buildID = buildID;
			setStorey(tempStorey, cursor);
		}
		cursor.close();
		return tempStorey;
	}
	
	private Storey getStorey(String buildID, int floor){
		Storey tempStorey = new Storey();
		Cursor cursor = idrmapDB.selectMapByFloor(buildID, floor);
		if(0 != cursor.getCount()){
			cursor.moveToFirst();
			tempStorey.buildID = buildID;
			setStorey(tempStorey, cursor);
		}
		cursor.close();
		return tempStorey;
	}
	
	private void setStorey(Storey storey, Cursor cursor){

		storey.filename = cursor.getString(cursor.getColumnIndexOrThrow("picname"));
		storey.floor = cursor.getInt(cursor.getColumnIndexOrThrow("floor"));
		storey.left = cursor.getInt(cursor.getColumnIndexOrThrow("left_lon"));
		storey.top = cursor.getInt(cursor.getColumnIndexOrThrow("top_lat"));
		storey.right = cursor.getInt(cursor.getColumnIndexOrThrow("right_lon"));
		storey.bottom = cursor.getInt(cursor.getColumnIndexOrThrow("bottom_lat"));
		storey.floorname = cursor.getString(cursor.getColumnIndexOrThrow("floor_name"));
		storey.address = cursor.getString(cursor.getColumnIndexOrThrow("build_address"));
		/*
		if(maxFloor == storey.floor){
			storey.maxFloor = true;
		}else if(minBasement == storey.floor){
			storey.minBasement = true;
		}*/
	}
	
	private boolean setBitmap(Storey storey){
		String filepath = INDOOR_MAP_DIR + storey.filename +".jpg";
		
		/* Tony ADDED @ 20110111 */
		if(bmpMap != null)
		{
			bmpMap.recycle();
			bmpMap = null;
		}
		
        File f = new File(filepath);
        if(f.exists()){
        	bmpMap = BitmapFactory.decodeFile(filepath);
        	
        	if(null == topleft && null == bottomright){
            	topleft = new BitMapPoint(storey.left, storey.top, 0, 0);
                bottomright  = new BitMapPoint(storey.right, storey.bottom, bmpMap.getWidth(), bmpMap.getHeight());
                //Log.e(TAG, storey.right+", "+ storey.bottom+" ; "+bmpMap.getWidth()+", "+bmpMap.getHeight());
            }else{
            	topleft.setTempPlace();
                topleft.setCoordinate(storey.left, storey.top, topleft.tx, topleft.ty);
                bottomright.setCoordinate(storey.right, storey.bottom, bmpMap.getWidth()*mapScale[scaleID]+topleft.tx, bmpMap.getHeight()*mapScale[scaleID]+topleft.ty);
                //Log.e(TAG, storey.right+", "+ storey.bottom+" ; "+bmpMap.getWidth()+", "+bmpMap.getHeight());
            }
        	textMapInfo.setText(storey.floorname);
        	return true;
        }
        return false;
	}
	
	public void setFloorAdapter(String buildID){
		if(floorCursor != null)
		{
			stopManagingCursor(floorCursor);
			floorCursor.close();
		}
		
		floorCursor = idrmapDB.getAll(buildID);
        startManagingCursor(floorCursor);
		
		if(floorCursor != null){
			floorAdapter = new SimpleCursorAdapter(IndoorMap.this, 
					R.layout.simple_info, 
					floorCursor, 
					new String[] {IndoorMapDBManager.SPOINT_FIELD_FLOOR_NAME, IndoorMapDBManager.SPOINT_FIELD_INFO}, 
					new int[] {R.id.simple_info_title, R.id.simple_info_info});					
		}
	}
	
	public void openFloorListDialog(){
		
		if(floorCursor != null && floorAdapter != null)
		{
			new AlertDialog.Builder(IndoorMap.this)
			.setIcon(R.drawable.ic_dialog_floorlist)
			.setTitle(R.string.indoor_floorlist)
			.setNeutralButton(R.string.btn_cancel, null)
	        .setAdapter(floorAdapter, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					floorCursor.moveToPosition(arg1);
					
			    	if(mainStorey.floor != floorCursor.getInt(floorCursor.getColumnIndexOrThrow("floor"))){
						setStorey(mainStorey, floorCursor);
						setBitmap(mainStorey);
				    	mapImageView.setImageBitmap(bmpMap);
				    	//drawView.setAnchorPoint(topleft, bottomright, mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
				    	drawView.setAnchorPoint(mainStorey.floor, mainStorey.buildID, mainStorey.floorname);
				    	
				    	// Tony ADDED @ 20110125
				    	if(directionSetup.getCurIndoorSubFloor() != mainStorey.floor)
				    		directionDisplay.hideLine();
				    	else
				    		directionDisplay.showLine();
				    	
				    	drawView.invalidate();
				    	
				    	if(maxFloor == mainStorey.floor){
				    		btnUpStair.setEnabled(false);
				    	}else{
				    		btnUpStair.setEnabled(true);
				    	}
				    	if(minBasement == mainStorey.floor){
				    		btnDownstair.setEnabled(false);
				    	}else{
				    		btnDownstair.setEnabled(true);
				    	}
					}
				}
            	
            })
			.show();
		}else{
			new AlertDialog.Builder(IndoorMap.this)
			.setIcon(R.drawable.ic_dialog_bookmark)
			.setTitle(R.string.indoor_floorlist)
			.setMessage(R.string.indoor_floorlist_none)
			.setPositiveButton(R.string.btn_ok, null)
			.show();
		}	
	}
	
	private class queryIndoorPOI extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... param) {

			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(INDOOR_SERVER_URL);  

			try {
				//建�?要�?交�?表單?��?  
	    		StringEntity reqEntity = new StringEntity("build="+param[0]);  
	    		//設置類�?  
	    		reqEntity.setContentType("application/x-www-form-urlencoded");  
	    		//設置请�??�数?? 
	    		httppost.setEntity(reqEntity);  
				
				HttpResponse response = httpclient.execute(httppost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
					
					String content = EntityUtils.toString(response.getEntity());
					
					if(-1 != content.indexOf("#")){	
						String str[] = content.split("#");
						if(str[1].length()>1){
							String strbuf[] = str[1].split("@");
							indoorPoiAmount = Integer.parseInt(strbuf[0]);
							if(strbuf[1].length()>1){
								indoorPoi = strbuf[1];
								poi = IndoorMap.indoorPoi.split(";");
							}
						}
					}
				}  
			} catch (ClientProtocolException e) {

				Log.e(TAG, e.getMessage());
			} catch (IOException e) {

				Log.e(TAG, e.getMessage());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			
			// Tony ADDED @ 20110111
			// When HttpClient instance is no longer needed, 
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        httpclient.getConnectionManager().shutdown();
	        indoorPOIReadyFlag = true;
			return null;
		}
	}
	/** Overlay management */
	private void poiLayerInit()
	{
		indoorPOIReadyFlag = false;
		indoorPoiAmount = 0;
		Arrays.fill(indoorPOIStatus, true);
		String params[] = {mainStorey.buildID};
		new queryIndoorPOI().execute(params);
	}
	
	// Tony ADDED @ 20110112
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
	
	private void bookmarkOperation(int operationMode, int latE6, int lonE6, String name, String info, String buildID, int floorID)
	{
		GeoPoint location = new GeoPoint(latE6, lonE6);
		
		if(floorID == 0)	// Switch to outdoor
		{
			switch(operationMode)
			{
			case BOOKMARK_MODE_NORMAL:
				String  uriStr = PMap.INTENT_FILTER_PIN_POINT + "://?lat=" + latE6 + 
				"&lon=" + lonE6 + 
				"&name=" + name + 
				"&info=" + info + 
				"&floor_id=" + floorID;
				
				if(buildID != null)
					uriStr += "&build_id=" + buildID;
				
				directionSetup.quitSubStep();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)));
				break;
			case BOOKMARK_MODE_SET_START:
				directionSetup.setStart(false, name, info, location, buildID, floorID);
				directionSetup.dataFinalize();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_START_POINT + 
						"&lat=" + latE6 + 
						"&lon=" + lonE6)));
				break;
			case BOOKMARK_MODE_SET_END:
				directionSetup.setEnd(false, name, info, location, buildID, floorID);
				directionSetup.dataFinalize();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PMap.INTENT_FILTER_SHOW_PIN + "://?type=" + DirectionManager.SP_TYPE_END_POINT + 
						"&lat=" + latE6 + 
						"&lon=" + lonE6)));
				break;
			/*
			case BOOKMARK_MODE_DELETE:
				bookmarkDB.SPOINT_delete(key);
				break;
			case BOOKMARK_MODE_EDIT:
				break;
			*/
			}
		}
		else				// Stay in indoor
		{
			switch(operationMode)
			{
			case BOOKMARK_MODE_NORMAL:
				if(moveToCenter(lonE6, latE6, floorID, buildID)){
					poiLayerInit();
					drawView.setPressPoint(lonE6, latE6, name);
					
				}else{
		    		new AlertDialog.Builder(IndoorMap.this)
					.setTitle(R.string.indoor_no_map)
					.setIcon(R.drawable.ic_dialog_info)
					.setMessage(R.string.indoor_no_map_message)
					.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                }
		            })
					.show();
		    	}
				break;
			
			case BOOKMARK_MODE_SET_START:
				directionSetup.setStart(false, name, info, location, buildID, floorID);
				moveToCenter(lonE6, latE6, floorID, buildID);
				break;
			case BOOKMARK_MODE_SET_END:
				directionSetup.setEnd(false, name, info, location, buildID, floorID);
				moveToCenter(lonE6, latE6, floorID, buildID);
				break;
			/*
			case BOOKMARK_MODE_DELETE:
				bookmarkDB.SPOINT_delete(key);
				break;
			case BOOKMARK_MODE_EDIT:
				break;
			*/
			}
		}
	}
	
	public void openBookmarkDialog(final int operationMode)
	{
		if(bookmarkCursor != null && bookmarkAdapter != null)
		{
			bookmarkCursor.requery();
			
			BookmarkDialog bookmarkDialog = new BookmarkDialogForIndoorMap(IndoorMap.this, bookmarkAdapter,
					bookmarkCursor,
					null,
					operationMode,
					directionSetup);
			bookmarkDialog.show();
			
			// Tony COMMENTED @ 20110119
//			new AlertDialog.Builder(IndoorMap.this)
//			.setIcon(R.drawable.ic_dialog_bookmark)
//			.setTitle(R.string.pmap_menu_bookmark)
//			.setNeutralButton(R.string.btn_cancel, null)
//	        .setAdapter(bookmarkAdapter, new DialogInterface.OnClickListener(){
//
//				@Override
//				public void onClick(DialogInterface arg0, int arg1) {
//					bookmarkCursor.moveToPosition(arg1);
//					
//					if(arg1 == 0)
//					{
//						// Tony MODIFIED @ 20110113
//						Toast.makeText(IndoorMap.this, getString(R.string.pmap_bookmark_no_current_location_indoor), Toast.LENGTH_SHORT).show();
//						
//					}else{
//						
//						// Tony ADDED @ 20110112
//						bookmarkOperation(operationMode,
//								bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LAT),
//								bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LON),
//								bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_NAME),
//								bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_INFO),
//								bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_BUILD_ID),
//								bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_FLOOR));
//						
//						// Tony COMMENTED @ 20110112
//						/*
//						// Pin the custom point
//						//GeoPoint location = new GeoPoint(bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LAT), bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LON));
//						
//						switch(operationMode)
//						{
//						case BOOKMARK_MODE_NORMAL:
//							if(moveToCenter(bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LON),
//											bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LAT), 
//											bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_FLOOR), 
//											bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_BUILD_ID))){
//								
//					    		drawView.setPressPoint(bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LON),
//														bookmarkCursor.getInt(BookmarkDBManager.CCOLUMN_SPOINT_INT_LAT), 
//														bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_NAME));
//					    	}else{
//					    		new AlertDialog.Builder(IndoorMap.this)
//								.setTitle(R.string.indoor_no_map)
//								.setIcon(R.drawable.ic_dialog_info)
//								.setMessage(R.string.indoor_no_map_message)
//								.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
//					                public void onClick(DialogInterface dialog, int whichButton) {
//					                }
//					            })
//								.show();
//					    	}
//							break;
//						
//						case BOOKMARK_MODE_SET_START:
////							directionSetup.setStart(false, bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_NAME),
////									bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_INFO), 
////									location);
//							break;
//						case BOOKMARK_MODE_SET_END:
////							directionSetup.setEnd(false, bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_NAME),
////									bookmarkCursor.getString(BookmarkDBManager.CCOLUMN_SPOINT_STR_INFO), 
////									location);
//							break;
//						}
//
//						//mapCtrl.animateTo(location);
//						*/
//					}
//				}
//            	
//            })
//			.show();	
		}else{// [NOTE] According to current design, here should never be reached
			new AlertDialog.Builder(IndoorMap.this)
			.setIcon(R.drawable.ic_dialog_bookmark)
			.setTitle(R.string.pmap_menu_bookmark)
			.setMessage(R.string.pmap_bookmark_none)
			.setPositiveButton(R.string.btn_ok, null)
			.show();
		}	
	}
	
	private void searchPoi(){
		Bundle bundle = new Bundle();
		bundle.putString(IndoorPoiSearch.BUNDLE_BUILD, nowBuildID);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchManager.startSearch(null, true, IndoorMap.this.getComponentName(), bundle, false);
	}
}