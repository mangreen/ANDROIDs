package com.iiiesti.walkmap.customPanel;

import android.content.Intent;
import android.util.Log;

import com.iiiesti.walkmap.PMap;
import com.iiiesti.walkmap.R;


public class PointsDetailPanel_CUSTOM extends PointsDetailPanel {

	@Override
	protected String[] getMiscOperationTags() {
		return this.getResources().getStringArray(R.array.point_operations_for_custom_pin);
	}

	@Override
	protected void onMiscOperation(int operationId) {
		Log.d(D_TAG, Integer.toString(operationId));
		
		switch(operationId)
		{
		case 1:
			setResult(PMap.RESULT_PDETAIL_CLEAR_CUSTOMPIN_OVERLAY, new Intent().putExtra(PMap.INTENT_INT_1, PMap.OVERLAY_CUSTOM_PIN));
			PointsDetailPanel_CUSTOM.this.finish();
			break;
		}
	}
}
