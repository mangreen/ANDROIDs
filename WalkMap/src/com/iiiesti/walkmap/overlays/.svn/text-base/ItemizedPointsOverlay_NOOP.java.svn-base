package com.iiiesti.walkmap.overlays;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;

public class ItemizedPointsOverlay_NOOP extends ItemizedPointsOverlay {

	public ItemizedPointsOverlay_NOOP(Context context, MapView mapView,
			Drawable marker, boolean drawShadow) {
		super(context, mapView, marker, drawShadow);
	}

	@Override
	protected boolean getSTAAFlag() {
		return false;
	}

	@Override
	protected Runnable getPopupRunnable() {
		return null;
	}
	
}
