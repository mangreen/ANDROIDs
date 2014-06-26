package com.iiiesti.pedestrian.pmap.customPanel;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

public class PointsDetailAdapter extends BaseAdapter {
	
    public View pointBasicInfo;
    public Adapter pointAdvOperation;

    public PointsDetailAdapter(View basicInfoView, Adapter advOperationAdapter) {
    	pointBasicInfo = basicInfoView;
    	pointAdvOperation = advOperationAdapter;
	}
    
    public Object getItem(int position) {
    	if(position == 0)
    		return pointBasicInfo;
    	else
    		return pointAdvOperation.getItem(position - 1);
    }

    public int getCount() {
        return pointAdvOperation.getCount() + 1;
    }

    public int getViewTypeCount() {
    	return pointAdvOperation.getViewTypeCount() + 1;
    }

    public int getItemViewType(int position) {
    	if(position == 0)
    		return 0;
    	else
    		return pointAdvOperation.getItemViewType(position - 1) + 1;
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    public boolean isEnabled(int position) {
    	if(position == 0)
    		return false;
    	else
    		return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(position == 0) 
    		return pointBasicInfo;
    	else
    		return pointAdvOperation.getView(position - 1, convertView, parent);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
