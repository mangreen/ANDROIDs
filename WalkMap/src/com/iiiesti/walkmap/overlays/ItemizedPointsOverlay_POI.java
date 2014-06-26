package com.iiiesti.walkmap.overlays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.MapView;
import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel;
import com.iiiesti.walkmap.customPanel.PointsDetailPanel_POI;

public class ItemizedPointsOverlay_POI extends ItemizedPointsOverlay {
	
	/* Constants */
	private static final String D_TAG = "PoiPin";
	
	/* Resources */
	private Overlay_Location mLocation;
	
	public ItemizedPointsOverlay_POI(Context context, MapView mapView, Overlay_Location location) {
		super(context, mapView, mapView.getResources().getDrawable(R.drawable.ic_point_poi_general), false);
		mLocation = location;
	}
	
	@Override
	protected boolean getSTAAFlag() {
		return true;
	}

	@Override
	protected Runnable getPopupRunnable() {
		
		return new Runnable() {	
			@Override
			public void run() {
				try
				{
					Log.d(D_TAG, "Switch to PointsDetailPanel now");
					
					OverlayItem_POI poi = ((OverlayItem_POI)currentItem);
					
					Bundle bundle = new Bundle();
					bundle.putBoolean(PointsDetailPanel.BUNDLE_IS_CUSTOM, true);
					bundle.putString(PointsDetailPanel.BUNDLE_TITLE, poi.mAttribute.name);
					bundle.putString(PointsDetailPanel.BUNDLE_INFO, poi.mAttribute.info);
					bundle.putDouble(PointsDetailPanel.BUNDLE_LAT, (double)(poi.mAttribute.latE6 / 1e6));
					bundle.putDouble(PointsDetailPanel.BUNDLE_LON, (double)(poi.mAttribute.lonE6 / 1e6));

					if(null != mLocation.getCurrentLocation())
					{
						bundle.putDouble(PointsDetailPanel.BUNDLE_CUR_LAT, mLocation.getCurrentLocation().getLatitude());
						bundle.putDouble(PointsDetailPanel.BUNDLE_CUR_LON, mLocation.getCurrentLocation().getLongitude());
					}
					
					if(null != poi.mAttribute.buildID)
					{
						bundle.putString(PointsDetailPanel.BUNDLE_BUILD_ID, poi.mAttribute.buildID);
						bundle.putInt(PointsDetailPanel.BUNDLE_FLOOR, 0);
					}
		
					((Activity)mContext).startActivityForResult(new Intent(mContext, PointsDetailPanel_POI.class).putExtras(bundle), PMap.REQUEST_POINT_DETAIL);	
				}
				catch (Exception e)
				{
					Log.e(D_TAG, e.getMessage());
				}
			}
		};
	}
}
