package org.droidplanner.android.fragments;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import com.o3dr.services.android.lib.drone.property.Home;

import org.droidplanner.android.WaypointUtils;
import org.droidplanner.android.activities.EditorActivity;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.network.ComunicazioneConServerThread;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.PolygonMarkerInfo;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.wrapperPercorso.WrapperPercorso;
import org.droidplanner.android.wrapperPercorso.WrapperPercorsoMarkerInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditorMapFragment extends DroneMap implements DPMap.OnMapLongClickListener,
		DPMap.OnMarkerDragListener, DPMap.OnMapClickListener, DPMap.OnMarkerClickListener, DroneMap.MapMarkerProvider, LocationListener {


    public static final float MAX_USER_DRONE_DISTANCE = 500; //m

    WrapperPercorso wrapperPercorso;

	private OnEditorInteraction editorListener;

    public WrapperPercorso getWrapperPercorso() {
        return wrapperPercorso;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

        mMapFragment.setOnMarkerDragListener(this);
		mMapFragment.setOnMarkerClickListener(this);
		mMapFragment.setOnMapClickListener(this);
		mMapFragment.setOnMapLongClickListener(this);

        if(!((EditorActivity)getActivity()).isPrimaPerizia()){
            new ComunicazioneConServerThread(
                    ComunicazioneConServerThread.selectPercorsiByCodicePolizzaRequest(((EditorActivity) getActivity()).getCodicePolizza()),
                    new ComunicazioneConServerThread.RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                Log.d("PERCORSI", response);
                                JSONObject responseJSON = new JSONObject(response);
                                JSONArray percorsiJSON = responseJSON.optJSONArray("percorsi");

                                new ComunicazioneConServerThread(
                                        ComunicazioneConServerThread.selectWaypoints(percorsiJSON.getJSONObject(0).getString("CodPR")),
                                        new ComunicazioneConServerThread.RequestListener() {
                                            @Override
                                            public void onSuccess(String response) {
                                                try {
                                                    Log.d("WAYPOINTS", response);
                                                    JSONObject responseJSON = new JSONObject(response);
                                                    JSONArray waypointsJSON = responseJSON.optJSONArray("wp");
                                                    ArrayList<Waypoint> waypoints = new ArrayList<>();
                                                    for(int i=0; i<waypointsJSON.length(); i++){
                                                        waypoints.add(WaypointUtils.jsonToWaypoint(waypointsJSON.getJSONObject(i)));
                                                    }
                                                    missionProxy.mAddWaypoints(waypoints);
                                                } catch(JSONException e){
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onError(int responseCode, String response) {
                                                Log.d("WAYPOINTS", "ERROR");
                                            }
                                        }
                                ).start();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(int responseCode, String response) {
                            Log.d("PERCORSI", "ERRORE");
                        }
                    }
            ).start();
        }

		return view;
	}

	@Override
	public void onMapLongClick(LatLong point) {
	}

	@Override
	public void onMarkerDrag(MarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
        checkForWrapperPercorsoUpdate(markerInfo);
	}

	@Override
	public void onMarkerDragStart(MarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
        checkForWrapperPercorsoUpdate(markerInfo);
	}

    private void checkForWrapperPercorsoUpdate(MarkerInfo markerInfo) {
        if (markerInfo instanceof WrapperPercorsoMarkerInfo) {
            WrapperPercorsoMarkerInfo wrapperPercorsoMarkerInfo = (WrapperPercorsoMarkerInfo)markerInfo;
            WrapperPercorso wrapperPercorso = wrapperPercorsoMarkerInfo.getWrapperPercorso();

            mMapFragment.updateWrapperPercorso(wrapperPercorso);
        }
    }

    private void checkForWaypointMarkerMoving(MarkerInfo markerInfo) {
		if (markerInfo instanceof MissionItem.SpatialItem) {
			LatLong position = markerInfo.getPosition();

			// update marker source
			MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) markerInfo;
            LatLong waypointPosition = waypoint.getCoordinate();
            waypointPosition.setLatitude(position.getLatitude());
            waypointPosition.setLongitude(position.getLongitude());

			// update flight path
			mMapFragment.updateMissionPath(missionProxy);
		}
	}

	@Override
	public void onMarkerDragEnd(MarkerInfo markerInfo) {
		checkForWaypointMarker(markerInfo);
	}

	private void checkForWaypointMarker(MarkerInfo markerInfo) {
		if ((markerInfo instanceof MissionItemMarkerInfo)) {
			missionProxy.move(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(),
					markerInfo.getPosition());
		}else if ((markerInfo instanceof PolygonMarkerInfo)) {
			PolygonMarkerInfo marker = (PolygonMarkerInfo) markerInfo;
			missionProxy.movePolygonPoint(marker.getSurvey(), marker.getIndex(), markerInfo.getPosition());
		}
	}

    @Override
    public void onApiConnected(){
        super.onApiConnected();
        zoomToFit();
    }

	@Override
	public void onMapClick(LatLong point) {
		editorListener.onMapClick(point);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        if(!(activity instanceof OnEditorInteraction)){
            throw new IllegalStateException("Parent activity must implement " +
                    OnEditorInteraction.class.getName());
        }

		editorListener = (OnEditorInteraction) activity;
	}

	@Override
	public boolean setAutoPanMode(AutoPanMode target) {
		if (target == AutoPanMode.DISABLED)
			return true;

		Toast.makeText(getActivity(), "Auto pan is not supported on this map.", Toast.LENGTH_LONG)
				.show();
		return false;
	}

	@Override
	public boolean onMarkerClick(MarkerInfo info) {
		if (info instanceof MissionItemMarkerInfo) {
			editorListener.onItemClick(((MissionItemMarkerInfo) info).getMarkerOrigin(), false);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isMissionDraggable() {
		return true;
	}

	public void zoomToFit() {
		// get visible mission coords
		final List<LatLong> visibleCoords = missionProxy == null ? new ArrayList<LatLong>() : missionProxy.getVisibleCoords();

		// add home coord if visible
		if(drone != null) {
			Home home = drone.getAttribute(AttributeType.HOME);
			if (home != null && home.isValid()) {
				final LatLong homeCoord = home.getCoordinate();
				if (homeCoord.getLongitude() != 0 && homeCoord.getLatitude() != 0)
					visibleCoords.add(homeCoord);
			}
		}

		if (!visibleCoords.isEmpty())
			zoomToFit(visibleCoords);
	}

    public void zoomToFit(List<LatLong> itemsToFit){
        if(!itemsToFit.isEmpty()){
            mMapFragment.zoomToFit(itemsToFit);
        }
    }

    @Override
    public MarkerInfo[] getMapMarkers() {
        if(wrapperPercorso == null)
            return new MarkerInfo[0];

        return wrapperPercorso.getMarkersInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapFragment.setLocationListener(null);
        removeMapMarkerProvider(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapFragment.setLocationListener(this);
        addMapMarkerProvider(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(wrapperPercorso != null || !((EditorActivity)getActivity()).isPrimaPerizia())
            return;
        Log.i("location", "editor fragment");
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        wrapperPercorso = new WrapperPercorso(getMaxSquare(position));
        mMapFragment.updateWrapperPercorso(wrapperPercorso);
        postUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private ArrayList<LatLng> getMaxSquare(LatLng center){
        ArrayList<LatLng> vertices = new ArrayList<>();

        vertices.add(SphericalUtil.computeOffset(center, MAX_USER_DRONE_DISTANCE, 315));
        vertices.add(SphericalUtil.computeOffset(center, MAX_USER_DRONE_DISTANCE, 45));
        vertices.add(SphericalUtil.computeOffset(center, MAX_USER_DRONE_DISTANCE, 135));
        vertices.add(SphericalUtil.computeOffset(center, MAX_USER_DRONE_DISTANCE, 225));

        return vertices;
    }

}
