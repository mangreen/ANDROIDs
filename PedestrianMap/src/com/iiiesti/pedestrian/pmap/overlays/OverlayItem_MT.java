package com.iiiesti.pedestrian.pmap.overlays;

import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.iiiesti.pedestrian.pmap.PMap;
import com.iiiesti.pedestrian.pmap.R;
import com.iiiesti.pedestrian.pmap.customType.MT_Constant;
import com.iiiesti.pedestrian.pmap.customType.MT_SimpleStop;

public class OverlayItem_MT extends OverlayItem {
	
	private MT_SimpleStop mStopAttribute;
	
	public OverlayItem_MT(View mapView, MT_SimpleStop transitStopAttribute) {
		super(new GeoPoint(transitStopAttribute.latE6, transitStopAttribute.lonE6), transitStopAttribute.name, null);
		
		switch(transitStopAttribute.type)
		{
		case MT_Constant.TYPE_BUS:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_bus)));
			break;
		case MT_Constant.TYPE_HSR:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_hsr)));
			break;
		case MT_Constant.TYPE_MRT:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_mrt)));
			break;
		case MT_Constant.TYPE_PLANE:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_plane)));
			break;
		case MT_Constant.TYPE_SHUTTLE:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_shuttle)));
			break;
		case MT_Constant.TYPE_RAIL:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_rail)));
			break;
		default:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_masstransit_bus)));
		}
		
		mStopAttribute = transitStopAttribute;
	}

	public String getStopName()
	{
		return mStopAttribute.name;
	}
}
