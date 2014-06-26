package com.iiiesti.pedestrian.pmap.customPanel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.iiiesti.pedestrian.pmap.PMap;
import com.iiiesti.pedestrian.pmap.R;

public abstract class PointsDetailPanel extends Activity {
	
	/* Public constants */
	public static final String BUNDLE_IS_CUSTOM = "bundleIsCustom";
	public static final String BUNDLE_TITLE = "bundleTitle";
	public static final String BUNDLE_INFO = "bundleInfo";
	public static final String BUNDLE_SUB_INFO = "bundleSubInfo";
	public static final String BUNDLE_CUR_LAT = "bundleCurrentLat";
	public static final String BUNDLE_CUR_LON = "bundleCurrentLon";
	public static final String BUNDLE_LAT = "bundleLat";
	public static final String BUNDLE_LON = "bundleLon";
	public static final String BUNDLE_ADDRESS_LAT = "bundleAddrLat";
	public static final String BUNDLE_ADDRESS_LON = "bundleAddrLon";
	
	/* Constants */
	protected static final String D_TAG = "PointInfo";
	
	/* Resource */
	private Bundle mBundle;
	private ViewGroup masterView;
	private ListView listView;
	private ArrayAdapter<String> miscOperationList;
	private double cLatitude;
	private double cLongitude;
	private double pLatitude;
	private double pLongitude;
	private boolean hasAddressLocation;
	private double aLatitude;
	private double aLongitude;
	protected String[] mMiscOperationTags;
	private boolean hasMiscOperation;
	
	/* Variable */
	private String distanceHint = null;
	
	/* Create dummy views */
	private TextView textTitle;
	private TextView textInfo;
	private TextView textSubInfo;
	private Button btnSetStart;
	private Button btnSetEnd;
	private Button btnBookmark;
	private Button btnInOutSW;
	private Button btnStView;
	private Button btnArView;
	private Button btnSrchGoogle;
	private Button btnMapView;
	private boolean isCustom;
	
	public PointsDetailPanel() {
		mMiscOperationTags = null;
		hasMiscOperation = false;
	}
	
	/* Setup connections */
	private void findViews(View view)
	{
		textTitle = (TextView)view.findViewById(R.id.point_name);
		textInfo = (TextView)view.findViewById(R.id.point_info);
		textSubInfo = (TextView)view.findViewById(R.id.point_sub_info);
		btnSetStart = (Button)view.findViewById(R.id.btn_pnt_info_op_set_start);
		btnSetEnd = (Button)view.findViewById(R.id.btn_pnt_info_op_set_end);
		btnBookmark = (Button)view.findViewById(R.id.btn_pnt_info_op_add_bookmark);
		btnInOutSW = (Button)view.findViewById(R.id.btn_pnt_info_op_in_out);
		btnStView = (Button)view.findViewById(R.id.btn_pnt_info_op_street_view);
		btnArView = (Button)view.findViewById(R.id.btn_pnt_info_op_ar_direct);
		btnSrchGoogle = (Button)view.findViewById(R.id.btn_pnt_info_op_srch_google);
		btnMapView = (Button)view.findViewById(R.id.btn_pnt_info_op_map_view);
	}
	
	private void disableGeneralBtn()
	{	
		btnInOutSW.setEnabled(false);
		btnArView.setEnabled(false);
	}
	
	private Button.OnClickListener setStart = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "setStart");
			setResult(PMap.RESULT_PDETAIL_SET_START, new Intent().putExtras(mBundle));
			PointsDetailPanel.this.finish();
		}
	};
	
	private Button.OnClickListener setEnd = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "setEnd");
			setResult(PMap.RESULT_PDETAIL_SET_END, new Intent().putExtras(mBundle));
			PointsDetailPanel.this.finish();
		}
	};
	
	private Button.OnClickListener addBookmark = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "addBookmark");
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = (View)inflater.inflate(R.layout.add_bookmark, null);
			final EditText titleEdit = (EditText)view.findViewById(R.id.edit_bookmark_title);
			final EditText infoEdit = (EditText)view.findViewById(R.id.edit_bookmark_info);
			titleEdit.setText(mBundle.getString(BUNDLE_TITLE));
			infoEdit.setText(mBundle.getString(BUNDLE_INFO));
			
			new AlertDialog.Builder(PointsDetailPanel.this)
			.setIcon(R.drawable.ic_dialog_bookmark)
            .setTitle(R.string.pmap_bookmark_add_title)
            .setView(view)
            .setPositiveButton(R.string.btn_add, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	mBundle.putString(BUNDLE_TITLE, titleEdit.getText().toString());
                	mBundle.putString(BUNDLE_INFO, infoEdit.getText().toString());
                	
        			setResult(PMap.RESULT_PDETAIL_ADD_BOOKMARK, new Intent().putExtras(mBundle));
        			PointsDetailPanel.this.finish();
                }
            })
            .setNegativeButton(R.string.btn_cancel, null)
            .show();
		}
	};
	
	private Button.OnClickListener inOutSW = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "inOutSW");
		}
	};
	
	private Button.OnClickListener streetView = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "streetView");
			if(hasAddressLocation)
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll=" + Double.toString(aLatitude) + "," + Double.toString(aLongitude) + "&cbp=1,0.0,,0.0,1.0")));
			else
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll=" + Double.toString(pLatitude) + "," + Double.toString(pLongitude) + "&cbp=1,0.0,,0.0,1.0")));
		}
	};
	
	private Button.OnClickListener arView = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "arView");
		}
	};
	
	private Button.OnClickListener srchGoogle = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "srchGoogle");
			SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
			
			if(isCustom)
				searchManager.startSearch(textInfo.getText().toString(), true, PointsDetailPanel.this.getComponentName(), null, true);
			else
				searchManager.startSearch(textTitle.getText().toString(), true, PointsDetailPanel.this.getComponentName(), null, true);
		}
	};
	
	private Button.OnClickListener mapView = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "mapView");
			setResult(PMap.RESULT_PDETAIL_HAS_CENTER, new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + Double.toString(pLatitude) + "," + Double.toString(pLongitude))));
			PointsDetailPanel.this.finish();
		}
	};
	
	/* Setup the Views listener */
	private void setListener()
	{
		btnSetStart.setOnClickListener(setStart);
		btnSetEnd.setOnClickListener(setEnd);
		btnBookmark.setOnClickListener(addBookmark);
		btnInOutSW.setOnClickListener(inOutSW);
		btnStView.setOnClickListener(streetView);
		btnArView.setOnClickListener(arView);
		btnSrchGoogle.setOnClickListener(srchGoogle);
		btnMapView.setOnClickListener(mapView);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(D_TAG, "PointsDetailPanel: onCreate");
		super.onCreate(savedInstanceState);
		
		// Get the main layout
		masterView = (ViewGroup)getLayoutInflater().inflate(R.layout.point_detail_info_layout, masterView);
		
		findViews(masterView);
		setListener();
		
		/** Setup data - start */
		
		mBundle = this.getIntent().getExtras();

		// Determine custom or ready-made pin
		isCustom = mBundle.getBoolean(BUNDLE_IS_CUSTOM, true);
		
		// Retrieve location information
		cLatitude = mBundle.getDouble(BUNDLE_CUR_LAT, 91.0);
        cLongitude = mBundle.getDouble(BUNDLE_CUR_LON, 181.0);
        pLatitude = mBundle.getDouble(BUNDLE_LAT);					// [NOTE] Point location information should not be empty at any "POI" instance
        pLongitude = mBundle.getDouble(BUNDLE_LON);
        aLatitude = mBundle.getDouble(BUNDLE_ADDRESS_LAT, 91.0);		// [NOTE] address location information might be empty
        aLongitude = mBundle.getDouble(BUNDLE_ADDRESS_LON, 181.0);
        
        if(!(cLatitude > 90 || cLongitude > 180))
        {
        	float meters = PMap.calMeter((long)(cLatitude * 1E6), (long)(cLongitude * 1E6), (long)(pLatitude * 1E6), (long)(pLongitude * 1E6));
        	if(meters > 1000)
        	{
        		meters /= 1000;
        		
        		distanceHint = new String(Float.toString(PMap.tDecPlace(meters)) + " " + getString(R.string.unit_kilometer));
        	}
        	else
        		distanceHint = new String(Float.toString(PMap.tDecPlace(meters)) + " " + getString(R.string.unit_meter));
        }
        else
        	distanceHint = new String("Approximate distance: N/A");
        
        if(aLatitude > 90 || aLongitude > 180)
        	hasAddressLocation = false;
        else
        	hasAddressLocation = true;
        	
        // Disable the views according to data instance
        disableGeneralBtn();
        
     	// Inflate the main information		
		textTitle.setText(mBundle.getString(BUNDLE_TITLE));
		textInfo.setText(mBundle.getString(BUNDLE_INFO));
		if(null != mBundle.getString(BUNDLE_SUB_INFO))
			textSubInfo.setText(mBundle.getString(BUNDLE_SUB_INFO) + "\n" + distanceHint);
		else
			textSubInfo.setText(distanceHint);

		// Generate corresponding operation options (if existed)
		if((mMiscOperationTags = getMiscOperationTags()) == null)
			mMiscOperationTags = new String[]{};
		else
			hasMiscOperation = true;
		miscOperationList = new ArrayAdapter<String>(this, R.layout.point_detail_info_op, mMiscOperationTags);
		
        /** Setup data - end */
        
        // Setup the corresponding custom adapter
	    PointsDetailAdapter adapter = new PointsDetailAdapter(masterView, miscOperationList);

	    // Startup the final view
	    listView = new ListView(this);
	    listView.setBackgroundColor(0xFF000000);
	    listView.setAdapter(adapter); 
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				if(hasMiscOperation)
					onMiscOperation(position);
			}
		});
	    this.setContentView(listView);
	}
	
	@Override
    protected void onPause() {
    	Log.v(D_TAG, "PointsDetailPanel: onPause");
        super.onPause();
    }
    
    @Override
    protected void onStop() {
    	Log.v(D_TAG, "PointsDetailPanel: onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.v(D_TAG, "PointsDetailPanel: onDestroy");
    	super.onDestroy();
    }
    
    @Override
    protected void onRestart() {
    	Log.v(D_TAG, "PointsDetailPanel: onRestart");
    	super.onRestart();
    }
	
	protected abstract String[] getMiscOperationTags();				// Set the Misc_operation tag list
	protected abstract void onMiscOperation(int operationId);		// Set the Misc_operation corresponding to the tag list
}
