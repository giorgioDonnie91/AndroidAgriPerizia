package org.droidplanner.android.wrapperPercorso;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.MarkerInfo;


public class WrapperPercorsoMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

	private LatLong mPoint;
    private final WrapperPercorso mWrapperPercorso;

	public WrapperPercorsoMarkerInfo(LatLong point, WrapperPercorso wrapperPercorso) {
		mPoint = point;
        mWrapperPercorso = wrapperPercorso;
	}

    public WrapperPercorso getWrapperPercorso() {
        return mWrapperPercorso;
    }

    @Override
    public Bitmap getIcon(Resources res) {
        return BitmapFactory.decodeResource(res, R.drawable.ic_wp_flag);
    }

    @Override
	public float getAnchorU() {
		return 0.15f;
	}

	@Override
	public float getAnchorV() {
		return 0.98f;
	}

	@Override
	public LatLong getPosition() {
		return mPoint;
	}

	@Override
	public void setPosition(LatLong coord) {
		mPoint = coord;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

    @Override
    public boolean isDraggable() {
        return true;
    }
}
