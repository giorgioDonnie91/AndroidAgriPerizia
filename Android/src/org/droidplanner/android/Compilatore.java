package org.droidplanner.android;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Compilatore {

    private static ArrayList<Waypoint> getInitialWaypoints(List<LatLng> vertices) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (LatLng vertex : vertices) {
            waypoints.add(WaypointUtils.newWaypoint(vertex.latitude, vertex.longitude));
        }
        return waypoints;
    }

    public static List<Waypoint> generaItinerario(List<LatLng> vertices) {

        ArrayList<Waypoint> initial = getInitialWaypoints(vertices);
        List<Waypoint> itinerario = new ArrayList<>();
        List<Waypoint> retta1 = new ArrayList<>();
        List<Waypoint> retta2 = new ArrayList<>();

        //retta 1
        Waypoint wp1 = initial.get(0);
        Waypoint wp2 = initial.get(1);
        double distanza = WaypointUtils.distanza(wp1, wp2);
        if (distanza < 30) {
            Waypoint waypointMedio = WaypointUtils.waypointMedio(wp1, wp2);

            //retta1.add(wp1);
            retta1.add(waypointMedio);
            //retta1.add(wp2);
        } else {
            int segments = (int) Math.ceil(distanza / 30);

            List<Waypoint> puntiDivisione = WaypointUtils.getPuntiDivisione(wp1, wp2, segments);
            puntiDivisione.add(0, wp1);
            puntiDivisione.add(wp2);

            retta1 = puntiDivisione;
        }

        //retta 2
        wp1 = initial.get(2);
        wp2 = initial.get(3);
        if (distanza < 30) {
            Waypoint waypointMedio = WaypointUtils.waypointMedio(wp1, wp2);

            //retta2.add(wp1);
            retta2.add(waypointMedio);
            //retta2.add(wp2);

        } else {

            int segments = (int) Math.ceil(distanza / 30);

            List<Waypoint> puntiDivisione = WaypointUtils.getPuntiDivisione(wp1, wp2, segments);
            puntiDivisione.add(0, wp1);
            puntiDivisione.add(wp2);

            retta2 = puntiDivisione;
        }

        //definizione itinerario
        int puntiRettaMaggiore;
        if (retta1.size() >= retta2.size()) {
            puntiRettaMaggiore = retta1.size();
        } else {
            puntiRettaMaggiore = retta2.size();
        }

        int contatore = 1;
        for (int i=0; i < puntiRettaMaggiore; i++) {
            Log.i("LOG", i+"; "+ contatore);

            Waypoint wpp1 = i > retta1.size() ? retta1.get(retta1.size() - 1) : retta1.get(i);
            Waypoint wpp2 = i < 0 ? retta2.get(0) : retta2.get(retta1.size() - i -1);

            //itinerario.add(wpp1);

            distanza = WaypointUtils.distanza(wpp1, wpp2);
            if (distanza < 30) {
                Waypoint waypointMedio = WaypointUtils.waypointMedio(wpp1, wpp2);
                itinerario.add(waypointMedio);
            } else {
                itinerario.add(wpp1);
                int segments = (int) Math.ceil(distanza / 30);
                List<Waypoint> puntiDivisione = WaypointUtils.getPuntiDivisione(wpp1, wpp2, segments);
                for (Waypoint waypoint : puntiDivisione) {
                    itinerario.add(waypoint);
                }
                itinerario.add(wpp2);
            }
            //itinerario.add(wpp2);
        }


        //controllo della distanza totale
        double tot = 0;
        Waypoint start = itinerario.get(0);
        for (int i = 1; i < itinerario.size(); i++) {
            tot += WaypointUtils.distanza(start, itinerario.get(i));
            start = itinerario.get(i);
        }
/*
        if (tot > 4719.92)
            return null;
*/
        return itinerario;
    }

}
