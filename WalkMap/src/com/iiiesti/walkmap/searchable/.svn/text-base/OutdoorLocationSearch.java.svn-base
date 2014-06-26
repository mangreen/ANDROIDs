package com.iiiesti.walkmap.searchable;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;

import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;

public class OutdoorLocationSearch extends Activity {

	/* Constants */
	private static final String D_TAG = "SearchLocation";
	public static final String SERVER_DOMAIN = "140.92.13.231";	// 10.0.2.2
//	public static final String SERVER_DOMAIN = "140.92.13.111";	// 10.0.2.2
	public static final String STR_NULL = "null";
	public static final int HTTP_TIMEOUT = 3000;
	public static final int HTTP_SO_TIMEOUT = 15000;
	private static final int ADDRESS_RESULT_MAX_COUNT = 10;
	public static final int DEFAULT_POI_SEARCH_RADIUS = 15000;		// unit: 0.1 meter
	public static final int LBS_COLUMN_COUNT = 7;					// _id, latitude, longitude, name, address, telephone, building_id
//	public static final int LBS_IDX_ID = 0;
	public static final int LBS_IDX_LAT = 1;
	public static final int LBS_IDX_LON = 2;
	public static final int LBS_IDX_NAME = 3;
	public static final int LBS_IDX_ADDR = 4;
//	private static final int LBS_IDX_TEL = 5;
	public static final int LBS_IDX_BID = 6;
	private static final int ICON_ADDR = R.drawable.ic_list_address;
	private static final int ICON_POI = R.drawable.ic_list_normal_poi;
	private static final int ICON_POI_W_INDOOR = R.drawable.ic_list_poi_w_indoor;
	private static final String MAP_INT_ICON = "pointIconID";
	private static final String MAP_INT_LATE6 = "pointLatE6";
	private static final String MAP_INT_LONE6 = "pointLonE6";
	private static final String MAP_STR_TITLE = "pointTitle";
	private static final String MAP_STR_INFO = "pointInfo";
	private static final String MAP_STR_BUILDING_ID = "buildingID";
	private static final String MAP_INT_FLOOR_ID = "floorID";
	
	public static final String BUNDLE_CENTER_LATE6 = "centerLatE6";
	public static final String BUNDLE_CENTER_LONE6 = "centerLonE6";
	public static final String BUNDLE_LBOTTOM_LATE6 = "leftBottomLatE6";
	public static final String BUNDLE_LBOTTOM_LONE6 = "lefBottomLonE6";
	public static final String BUNDLE_RTOP_LATE6 = "rightTopLatE6";
	public static final String BUNDLE_RTOP_LONE6 = "rightTopLonE6";
	
	/* Resource */
    private int centerLatE6;
    private int centerLonE6;
    private int lBottomLatE6;
    private int lBottomLonE6;
    private int rTopLatE6;
    private int rTopLonE6;
    private int radius;
    private String queryKey;
    private boolean hasQuery;
    private boolean queryCurrentView;
    
    private List<Map<String, Object>> mResultList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(D_TAG, "SearchLocation: onCreate");

        super.onCreate(savedInstanceState);
		
        // Get the search key from the search framework
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(SearchManager.APP_DATA);
        
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	
	    	mResultList = new ArrayList<Map<String, Object>>();
	    	hasQuery = false;
	    	queryCurrentView = true;
	    	
	        setContentView(new View(OutdoorLocationSearch.this));	// A transparent empty view
	        
	        // Retrieve the query info
	    	queryKey = intent.getStringExtra(SearchManager.QUERY);
	    	centerLatE6 = bundle.getInt(BUNDLE_CENTER_LATE6, 91000000);
	    	centerLonE6 = bundle.getInt(BUNDLE_CENTER_LONE6, 181000000);
	    	lBottomLatE6 = bundle.getInt(BUNDLE_LBOTTOM_LATE6, 91000000);
	    	lBottomLonE6 = bundle.getInt(BUNDLE_LBOTTOM_LONE6, 181000000);
	    	rTopLatE6 = bundle.getInt(BUNDLE_RTOP_LATE6, 91000000);
	    	rTopLonE6 = bundle.getInt(BUNDLE_RTOP_LONE6, 181000000);
	    	
	    	radius = 0;
	    	
	    	if(lBottomLatE6 != 91000000 && rTopLatE6 != 91000000)
	    	{
	    		radius = Math.abs(lBottomLatE6 - rTopLatE6);
	    		if(Math.abs(lBottomLonE6 - rTopLonE6) > radius)
	    			radius = Math.abs(lBottomLonE6 - rTopLonE6);
	    		radius >>= 1;
	    	}
	    	
	    	if(radius < DEFAULT_POI_SEARCH_RADIUS)
    			radius = DEFAULT_POI_SEARCH_RADIUS;
	    	
	    	Log.d(D_TAG, "search key: " + queryKey);
	    	Log.d(D_TAG, "search center: (" + centerLatE6 + "," + centerLonE6 + ")");
	    	Log.d(D_TAG, "search right-top: (" + rTopLatE6 + "," + rTopLonE6 + ")");
	    	Log.d(D_TAG, "search left-bottom: (" + lBottomLatE6 + "," + lBottomLonE6 + ")");
	    	Log.d(D_TAG, "search radius: " + radius);
	    	
	    	new GetLocation().execute();
	    }
	    else
	    	this.finish();
    }
	
	private void addResult(int drawableID, int latE6, int lonE6, String title, String info, String buildID, int floorID)
	{
		if(mResultList != null)
		{
			Map<String, Object> result = new HashMap<String, Object>();
			result.put(MAP_INT_ICON, drawableID);
			result.put(MAP_INT_LATE6, latE6);
			result.put(MAP_INT_LONE6, lonE6);
			result.put(MAP_STR_TITLE, title);
			result.put(MAP_STR_INFO, info);
			result.put(MAP_STR_BUILDING_ID, buildID);
			result.put(MAP_INT_FLOOR_ID, floorID);
			mResultList.add(result);
		}
	}
	
	@Override
    public void onResume()
    {
        super.onResume();
        Log.v(D_TAG, "SearchLocation: onResume");
    }
    
	@Override
    public void onPause()
    {
        super.onPause();
        Log.v(D_TAG, "SearchLocation: onPause");
    }
    
	@Override
    public void onStop()
    {
        super.onStop();
        Log.v(D_TAG, "SearchLocation: onStop");
    }
	
	private DialogInterface.OnClickListener onResultSelected = new DialogInterface.OnClickListener(){
		
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			String tStr;
			Map<String, Object> result = mResultList.get(arg1);
			
			// Store the preferences for PMap
			SharedPreferences settings = OutdoorLocationSearch.this.getSharedPreferences(PMap.PREFNAME_PINPOINT, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(PMap.PREFS_BOOL_PINPOINT, true);
			editor.putInt(PMap.PREFS_INT_LATE6, (Integer)result.get(MAP_INT_LATE6));
			editor.putInt(PMap.PREFS_INT_LONE6, (Integer)result.get(MAP_INT_LONE6));
			editor.putString(PMap.PREFS_STR_TITLE, (String)result.get(MAP_STR_TITLE));
			editor.putString(PMap.PREFS_STR_INFO, (String)result.get(MAP_STR_INFO));
			tStr = (String)result.get(MAP_STR_BUILDING_ID);
			if(!tStr.equals(STR_NULL))
			{
				editor.putString(PMap.PREFS_STR_BUILDING_ID, tStr);
				editor.putInt(PMap.PREFS_INT_FLOOR_ID, (Integer)result.get(MAP_INT_FLOOR_ID));
			}
			// Commit the edits
			editor.commit();
			OutdoorLocationSearch.this.finish();
		}
    };
	
	private class GetLocation extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(OutdoorLocationSearch.this, OutdoorLocationSearch.this.getString(R.string.hint_on_search_title), OutdoorLocationSearch.this.getString(R.string.hint_on_search_location_hint), false, true, new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					cancel(true);
				}
				
			});
			mResultList.clear();
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			/** Search the Google geocoding server for address */
			List<Address> address = null;
			Geocoder geocoder = new Geocoder(OutdoorLocationSearch.this, Locale.TRADITIONAL_CHINESE);
			
			try {
				if(queryCurrentView)
					address = geocoder.getFromLocationName(queryKey, ADDRESS_RESULT_MAX_COUNT,
							(double)lBottomLatE6 / 1E6,
							(double)lBottomLonE6 / 1E6,
							(double)rTopLatE6 / 1E6,
							(double)rTopLonE6 / 1E6);
				else
					address = geocoder.getFromLocationName(queryKey, ADDRESS_RESULT_MAX_COUNT);
				
				if(null != address && 0 < address.size())
				{
					Log.d(D_TAG, "Address found: " + address.size());
					for(Address addr : address)
					{
						addResult(ICON_ADDR,
							(int)(addr.getLatitude() * 1E6),
							(int)(addr.getLongitude() * 1E6),
							addr.getAddressLine(0),
							"(" + addr.getLatitude() + "," + addr.getLongitude() + ")",
							STR_NULL,
							0);
					}
				}
				else
					Log.d(D_TAG, "Address found: 0");
			} catch (IOException e) {
				Log.e(D_TAG, e.getMessage());
			} catch (IllegalArgumentException e) {
				Log.e(D_TAG, e.getMessage());
			} catch (Exception e) {
				Log.e(D_TAG, e.getMessage());
			}
			
			/** Search the local LBS server for POIs */
			String tResultArray[];
			String URI;
			String responseBody = null;
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, HTTP_TIMEOUT);
			// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, HTTP_SO_TIMEOUT);
	        HttpClient httpclient = new DefaultHttpClient(httpParameters);
	        
			try {
		        // Compose the request URI
		        // Generate the basic URI
				if(queryCurrentView)
			        URI = "http://" + SERVER_DOMAIN + ":8080/LBSServer/mainLBS.jsp?lat=" + 
			        centerLatE6 + "&lon=" + centerLonE6 + "&r=" + radius + "&ql=," + URLEncoder.encode(queryKey, HTTP.UTF_8);
				else
					URI = "http://" + SERVER_DOMAIN + ":8080/LBSServer/mainLBS.jsp?" + 
			        "ql=," + URLEncoder.encode(queryKey, HTTP.UTF_8);
		        
		        Log.d(D_TAG, URI);
				// Generate the HTTP-REQUEST
		        HttpGet httpget = new HttpGet(URI); 
		        
		        // Create a response handler
		        ResponseHandler<String> responseHandler = new BasicResponseHandler();
		        
		        // Search the local server for nearby POIs
				responseBody = httpclient.execute(httpget, responseHandler);
				
				// Parse the search result (From mangreen's LBS)
				if(-1 != responseBody.indexOf("#")){	
					String str[] = responseBody.split("#");
					if(str[1].length()>1){
						String strbuf[] = str[1].split("@");
						
						if(Integer.parseInt(strbuf[0]) > 0 && strbuf[1].length()>1)
						{
							tResultArray = strbuf[1].split(";"); 
							for(String result : tResultArray)
							{
								String column[] = result.split(",");
								if(column.length == LBS_COLUMN_COUNT)
								{
									if(column[LBS_IDX_BID].equals(STR_NULL))
									{
										addResult(ICON_POI,
											Integer.parseInt(column[LBS_IDX_LAT]),
											Integer.parseInt(column[LBS_IDX_LON]),
											column[LBS_IDX_NAME],
											column[LBS_IDX_ADDR],
											column[LBS_IDX_BID],
											0);
									}
									else
									{
										addResult(ICON_POI_W_INDOOR,
												Integer.parseInt(column[LBS_IDX_LAT]),
												Integer.parseInt(column[LBS_IDX_LON]),
												column[LBS_IDX_NAME],
												column[LBS_IDX_ADDR],
												column[LBS_IDX_BID],
												0);
									}
								}
							}
							Log.d(D_TAG, "POI found: " + tResultArray.length);
						}
					}
				}	
				hasQuery = true;
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
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
				
			// Display the search results
			if(hasQuery)
			{
				if(mResultList.size() > 0)
				{
					if(queryCurrentView)
						new AlertDialog.Builder(OutdoorLocationSearch.this)
						.setIcon(R.drawable.ic_dialog_dialer)
				        .setTitle(R.string.hint_on_search_result_title)
				        .setAdapter(new SimpleAdapter(OutdoorLocationSearch.this, 
				        		mResultList, 
				        		R.layout.simple_info, 
				        		new String[] {MAP_INT_ICON, MAP_STR_TITLE, MAP_STR_INFO}, 
				        		new int[] {R.id.simple_info_icon, R.id.simple_info_title, R.id.simple_info_info}), 
				        		onResultSelected
				        )
				        .setNeutralButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	OutdoorLocationSearch.this.finish();
				            }
				        })
				        .setPositiveButton(R.string.btn_more, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	queryCurrentView = false;
				            	new GetLocation().execute();
				            }
				        })
				        .setCancelable(true)
				        .setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface arg0) {
								OutdoorLocationSearch.this.finish();
							}
						})
				        .show();
					else
						new AlertDialog.Builder(OutdoorLocationSearch.this)
						.setIcon(R.drawable.ic_dialog_dialer)
				        .setTitle(R.string.hint_on_search_result_more_title)
				        .setAdapter(new SimpleAdapter(OutdoorLocationSearch.this, 
				        		mResultList, 
				        		R.layout.simple_info, 
				        		new String[] {MAP_INT_ICON, MAP_STR_TITLE, MAP_STR_INFO}, 
				        		new int[] {R.id.simple_info_icon, R.id.simple_info_title, R.id.simple_info_info}), 
				        		onResultSelected
				        )
				        .setNeutralButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	OutdoorLocationSearch.this.finish();
				            }
				        })
				        .setCancelable(true)
				        .setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface arg0) {
								OutdoorLocationSearch.this.finish();
							}
						})
				        .show();
				}
				else
				{
					if(queryCurrentView)
						new AlertDialog.Builder(OutdoorLocationSearch.this)
						.setIcon(R.drawable.ic_dialog_dialer)
						.setTitle(R.string.hint_on_search_result_title)
						.setMessage(R.string.hint_on_search_result_location_not_found_hint)
			            .setNeutralButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	OutdoorLocationSearch.this.finish();
				            }
				        })
				        .setPositiveButton(R.string.btn_full_srch, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	queryCurrentView = false;
				            	new GetLocation().execute();
				            }
				        })
				        .setCancelable(true)
				        .setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface arg0) {
								OutdoorLocationSearch.this.finish();
							}
						})
						.show();
					else
						new AlertDialog.Builder(OutdoorLocationSearch.this)
						.setIcon(R.drawable.ic_dialog_dialer)
						.setTitle(R.string.hint_on_search_result_title)
						.setMessage(R.string.hint_on_search_result_full_location_not_found_hint)
			            .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				            	OutdoorLocationSearch.this.finish();
				            }
				        })
				        .setCancelable(true)
				        .setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface arg0) {
								OutdoorLocationSearch.this.finish();
							}
						})
						.show();
				}
			}
			else
			{
				new AlertDialog.Builder(OutdoorLocationSearch.this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle(R.string.hint_on_search_result_failed_title)
				.setMessage(R.string.hint_on_search_result_failed_hint)
	            .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	OutdoorLocationSearch.this.finish();
		            }
		        })
		        .setCancelable(true)
		        .setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface arg0) {
						OutdoorLocationSearch.this.finish();
					}
				})
				.show();
			}
			
			super.onPostExecute(result);
		}
	}
}
