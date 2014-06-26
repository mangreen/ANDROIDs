package com.iiiesti.pedestrian.pmap.searchable;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.iiiesti.pedestrian.pmap.R;

public class X_Deprecated_Searchable_Location extends Activity {

	/* Constants */
	private static final String D_TAG = "SearchLocation";
	
	/* Resources */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(D_TAG, "SearchLocation: onCreate");

        super.onCreate(savedInstanceState);
		
        // Get the search key from the search framework
        Intent intent = getIntent();
        
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        setContentView(R.layout.point_detail_info_layout);
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	
	    	Log.d(D_TAG, "search key: " + query);
	    	
	    	/** Search something - begin */
	    	
	    	/** Search something - end */
	    	
	    	// Store the search results
	    	
	    	// Switch to the last activity
//	    	this.finish();
	    }
	    else
	    	this.finish();
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
}
