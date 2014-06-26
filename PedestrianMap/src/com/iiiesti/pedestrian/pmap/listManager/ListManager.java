package com.iiiesti.pedestrian.pmap.listManager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.iiiesti.pedestrian.pmap.R;

public class ListManager {
	// Constants
	private static final String D_TAG = "ListManager";
	
	// Flags
	private boolean isEnabled;
	
	// Resources
	private Context mContext;
	private View mParentView;
	private RelativeLayout mLayout;
	
	// Buttons
	private Button btnListMode;
	private Button btnPrev;
	private Button btnNext;
	
	private void findViews()
	{
		btnListMode = (Button)mLayout.findViewById(R.id.btn_list_mode);
		btnPrev = (Button)mLayout.findViewById(R.id.btn_prev);
		btnNext = (Button)mLayout.findViewById(R.id.btn_next);
	}
	
	private void setListener()
	{
		btnListMode.setOnClickListener(listMode);
		btnPrev.setOnClickListener(getPrev);
		btnNext.setOnClickListener(getNext);
	}
	
	/*
	 * Constructor
	 */
	public ListManager(Context context, View parentView) {
		mContext = context;
		
		// Get the main layout
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mParentView = parentView;
        mLayout = (RelativeLayout)inflater.inflate(R.layout.list_manager, null);
        
        // Setup connections
        findViews();
        setListener();
        
        // Data initialization
        dataInitialize();
	}
	
	/*
	 * Data initialization
	 */
	private void dataInitialize()
	{
		((ViewGroup)mParentView).addView(mLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mLayout.setVisibility(View.GONE);
		isEnabled = false;
	}
	
	public void show()
	{
		if(!isEnabled)
		{
			mLayout.setVisibility(View.VISIBLE);
			isEnabled = true;
		}
	}
	
	public void hide()
	{
		mLayout.setVisibility(View.GONE);
		isEnabled = false;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}
	
	// Listener
	private Button.OnClickListener listMode = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "listMode");
		}
	};
	
	private Button.OnClickListener getPrev = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "getPrev");
		}
	};
	
	private Button.OnClickListener getNext = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.v(D_TAG, "getNext");
		}
	};
}
