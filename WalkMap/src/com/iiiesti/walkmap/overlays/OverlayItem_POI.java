package com.iiiesti.walkmap.overlays;

import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;
import com.iiiesti.walkmap.customType.POI_SimplePoint;
import com.iiiesti.walkmap.searchable.OutdoorLocationSearch;

public class OverlayItem_POI extends OverlayItem {
	
	public POI_SimplePoint mAttribute;
	
	public OverlayItem_POI(View mapView, POI_SimplePoint poiAttribute) {
		super(new GeoPoint(poiAttribute.latE6, poiAttribute.lonE6), poiAttribute.name, null);
		
		switch(poiAttribute.classIdx)
		{
		case PMap.OVERLAY_POI_EAT:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_eat)));
			break;
		case PMap.OVERLAY_POI_PLAY:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_play)));
			break;
		case PMap.OVERLAY_POI_BUY:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_buy)));
			break;
		case PMap.OVERLAY_POI_HOTEL:
			setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_hotel)));
			break;
		case PMap.OVERLAY_POI_MRT:
			if(poiAttribute.buildID != null && !poiAttribute.buildID.equals(OutdoorLocationSearch.STR_NULL))
				setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_mrt_w_indoor)));
			else
				setMarker(PMap.boundCenterBottom(mapView.getResources().getDrawable(R.drawable.ic_point_poi_mrt)));
			break;
		}
		
		mAttribute = poiAttribute;
	}
}
