package org.droidplanner.android;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WaypointUtils {

    private static final double DEFAULT_ALTITUDE = 15;

    public static Waypoint newWaypoint(double latitude, double longitude){
        Waypoint waypoint = new Waypoint();
        waypoint.setCoordinate(new LatLongAlt(latitude, longitude, DEFAULT_ALTITUDE));
        waypoint.setDelay(2);
        return waypoint;
    }

    public static Waypoint setCoordinates(Waypoint waypoint, double latitude, double longitude){
        waypoint.setCoordinate(new LatLongAlt(latitude, longitude, DEFAULT_ALTITUDE));
        return waypoint;
    }

    public static double distanza(Waypoint start, Waypoint end) {
        double lon1 = start.getCoordinate().getLongitude();
        double lat1 = start.getCoordinate().getLatitude();
        double lon2 = end.getCoordinate().getLongitude();
        double lat2 = end.getCoordinate().getLatitude();

        return distanza(lon1, lat1, lon2, lat2);
    }


    public static double distanza(double lon1, double lat1, double lon2, double lat2) {
        float[] res = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, res);
        return res[0];

        /*
        final int R = 6371;
        double latDistance = ((lat2-lat1)*Math.PI/180);
        double lonDistance = ((lon2-lon1)*Math.PI/180);
        double a = (Math.sin(latDistance/2)*Math.sin(latDistance/2)+Math.cos((lat1)*Math.PI/180)*Math.cos((lat2)*Math.PI/180)*Math.sin(lonDistance/2)*Math.sin(lonDistance/2));
        double cc = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
        return R*cc*1000;
        */
    }


    public static Waypoint waypointMedio(Waypoint start, Waypoint end){
        return newWaypoint(
                start.getCoordinate().getLatitude() + ((end.getCoordinate().getLatitude()-start.getCoordinate().getLatitude())/2),
                start.getCoordinate().getLongitude() + ((end.getCoordinate().getLongitude()-start.getCoordinate().getLongitude())/2)
        );
    }

    public static List<Waypoint> getPuntiDivisione(Waypoint start, Waypoint end, int segments){
        int divisionPoints = segments - 1;
        double deltaX = (end.getCoordinate().getLongitude() - start.getCoordinate().getLongitude())/segments;
        double deltaY = (end.getCoordinate().getLatitude() - start.getCoordinate().getLatitude())/segments;

        List<Waypoint> waypoints = new ArrayList<>();

        for(int i=0; i<divisionPoints; i++){
            start = WaypointUtils.newWaypoint(
                    start.getCoordinate().getLatitude() + deltaY,
                    start.getCoordinate().getLongitude() + deltaX
            );
            waypoints.add(start);
        }

        return waypoints;
    }

    public static JSONObject waypointToJSON(Waypoint waypoint) throws JSONException {
        JSONObject jsonWaipoint = new JSONObject();
        jsonWaipoint.put("Altezza", waypoint.getCoordinate().getAltitude());
        jsonWaipoint.put("Latitudine", waypoint.getCoordinate().getLatitude());
        jsonWaipoint.put("Longitudine", waypoint.getCoordinate().getLongitude());
        jsonWaipoint.put("TimeOut", waypoint.getDelay());
        return jsonWaipoint;
    }

    public static Waypoint jsonToWaypoint(JSONObject jsonWaipoint) throws JSONException {
        Waypoint waypoint = new Waypoint();
        waypoint.setCoordinate(
                new LatLongAlt(
                        jsonWaipoint.getDouble("Latitudine"),
                        jsonWaipoint.getDouble("Longitudine"),
                        jsonWaipoint.getDouble("Altezza")
                )
        );
        waypoint.setDelay(jsonWaipoint.getDouble("TimeOut"));
        return waypoint;
    }

}
