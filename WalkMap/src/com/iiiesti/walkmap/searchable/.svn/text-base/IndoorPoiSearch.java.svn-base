package com.iiiesti.walkmap.searchable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.indoor.IndoorMap;
import com.iiiesti.walkmap.indoor.PoiPoint;

public class IndoorPoiSearch extends Activity{
	/* Constants */
	private static final String TAG = "Search";
	
	public static final String BUNDLE_BUILD = "bundleBuilding";
	
	/* Resource */
//	private RelativeLayout mLayoutSearch;	// Tony COMMENTED @ 1st merge
	private ListView mListView;
	private Button btnCancel;
	ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter listItemAdapter;
	
	private Bundle mBundle;
	private Intent intent;
	private int indoorPoiAmount=0;
	private String indoorPoi="";
	private String poi[];
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(TAG, "Search: onCreate");

        super.onCreate(savedInstanceState);
        
        // Get the search key from the search framework
        intent = getIntent();
        mBundle = getIntent().getBundleExtra(SearchManager.APP_DATA);
        

        
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        //setContentView(R.layout.point_detail_info_layout);
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	String build = mBundle.getString(BUNDLE_BUILD);
	    	
	    	Log.d(TAG, "build: "+build);
	    	Log.d(TAG, "search key: " + query);
	    	
	    	String params[] = {build, query};
	    	new searchIndoorPOI().execute(params);
	    	/** Search something - begin */
	    	
	    	/** Search something - end */
	    	
	    	// Store the search results
	    	
	    	// Switch to the last activity
//	    	this.finish();
	    }else{
	    	this.finish();
	    }
	    /*Loading main.xml Layout*/
        setContentView(R.layout.idr_search_poi);
//        mLayoutSearch = (RelativeLayout)findViewById(R.id.layoutSearchPoi);	// Tony COMMENTED @ 1st merge
        mListView = (ListView)findViewById(R.id.idrSearchPoiList);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        if(0 != indoorPoiAmount){
        	
        }
        Log.d(TAG, "poi amount:"+indoorPoiAmount);
        
        btnCancel = (Button)findViewById(R.id.idrSearchPoiBtnCancel);
        btnCancel.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				IndoorPoiSearch.this.finish();
			}
        });
    }
	
	@Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "Search: onResume");
    }
    
	@Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "Search: onPause");
    }
    
	@Override
    public void onStop()
    {
        super.onStop();
        Log.v(TAG, "Search: onStop");
    }
	
	private class searchIndoorPOI extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... param) {
			
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(IndoorMap.INDOOR_SERVER_URL);  

			try {
				/*
				//建立要提交的表單數據  
	    		StringEntity reqEntity = new StringEntity("build="+param[0]+"&ql=,"+param[1]);  
	    		//設置類型  
	    		reqEntity.setContentType("application/x-www-form-urlencoded");  
	    		//設置请求的数据  
	    		httppost.setEntity(reqEntity); 
	    		*/
	    		List <NameValuePair> nvps = new ArrayList <NameValuePair>();  
	    		nvps.add(new BasicNameValuePair("build", param[0]));  
	    		nvps.add(new BasicNameValuePair("ql", ","+param[1]));
	    		httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8)); 
				
				HttpResponse response = httpclient.execute(httppost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
					
					String content = EntityUtils.toString(response.getEntity());
					Log.d(TAG, "poi: "+content);
					if(-1 != content.indexOf("#")){	
						String str[] = content.split("#");
						if(str[1].length()>1){
							String strbuf[] = str[1].split("@");
							indoorPoiAmount = Integer.parseInt(strbuf[0]);
							if(strbuf[1].length()>1){
								indoorPoi = strbuf[1];
								Log.d(TAG, "poi: "+indoorPoi);
								poi = indoorPoi.split(";"); 
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

			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setListAdapter();
		}
	}
	
	public void setListAdapter(){
		
		if(0 < indoorPoiAmount){
			//生成动态数组，加入数据  
			listItem = new ArrayList<HashMap<String, Object>>();
			for(int i=0; i<indoorPoiAmount; i++){
				String colume[] = poi[i].split(",");
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Icon", getIconInt(colume[PoiPoint.COLUMN_NUMBER_CATALOG]));//图像资源的ID  
			    map.put("Title", colume[PoiPoint.COLUMN_NUMBER_POI_NAME]);  
			    map.put("Info", getFloorString(colume[PoiPoint.COLUMN_NUMBER_FLOOR]));  
			    listItem.add(map);
			} 
			
			//生成适配器的Item和动态数组对应的元素  
			listItemAdapter = new SimpleAdapter(this,listItem,// 数据源   
					R.layout.simple_info,//ListItem的XML实现  
					//动态数组与ImageItem对应的子项          
					new String[] {"Icon", "Title", "Info"},   
					//ImageItem的XML文件里面的一个ImageView,两个TextView ID  
					new int[] {R.id.simple_info_icon, R.id.simple_info_title, R.id.simple_info_info}  
			);
			
			mListView.setAdapter(listItemAdapter);
			
			mListView.setOnItemClickListener(new OnItemClickListener() {  
				@Override  
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  long arg3){  
					
					Log.d(TAG, "number: "+arg2);
					Log.d(TAG, "poi: "+poi[arg2]);
					String colume[] = poi[arg2].split(",");
					IndoorMap.poi_long = Integer.parseInt(colume[PoiPoint.COLUMN_NUMBER_LONGTITUDE]);
					IndoorMap.poi_lat = Integer.parseInt(colume[PoiPoint.COLUMN_NUMBER_LATITUDE]);
					IndoorMap.poi_floor = Integer.parseInt(colume[PoiPoint.COLUMN_NUMBER_FLOOR]);
					IndoorMap.poi_name =colume[PoiPoint.COLUMN_NUMBER_POI_NAME];
					IndoorMap.REQUEST_POI_SEARCH = true;
					IndoorPoiSearch.this.finish();
				}  
			});  
			mListView.invalidate();
		}else{
			new AlertDialog.Builder(this)
			.setTitle(R.string.indoor_search_no_result)
			.setIcon(R.drawable.ic_dialog_info)
			.setMessage("Please reinput search keyword or check on server and internet.")
			.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	IndoorPoiSearch.this.finish();
                }
            })
			.show();
		}
	}
	
	private int getIconInt(String catalog){
		int drawable = 0;
		
		if(catalog.equals("nursery")){
			drawable = R.drawable.idr_baby;
		}else if(catalog.equals("elevator")){
			drawable = R.drawable.idr_elevator;
		}else if(catalog.equals("escalator")){
			drawable = R.drawable.idr_escalator;
		}else if(catalog.equals("exit")){
			drawable = R.drawable.idr_exit;
		}else if(catalog.equals("gate")){
			drawable = R.drawable.idr_gate;
		}else if(catalog.equals("info")){
			drawable = R.drawable.idr_info;
		}else if(catalog.equals("platform")){
			drawable = R.drawable.idr_platform;
		}else if(catalog.equals("stair")){
			drawable = R.drawable.idr_stair;
		}else if(catalog.equals("ticket")){
			drawable = R.drawable.idr_ticket;
		}else if(catalog.equals("toilet")){
			drawable = R.drawable.idr_toilet;
		}
		
		return drawable;
	}
	
	private String getFloorString(String rawFloor){
		String strFloor="";
		int floor = Integer.parseInt(rawFloor);
		
		if(0 < floor){
			strFloor = floor + "樓";
		}else if(0 > floor){
			
			strFloor = "地下" + Math.abs(floor) + "樓";
		}
		
		return strFloor;
	}
}
