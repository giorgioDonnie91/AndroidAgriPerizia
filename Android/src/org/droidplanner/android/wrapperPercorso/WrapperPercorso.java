package org.droidplanner.android.wrapperPercorso;


import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;


import java.util.ArrayList;
import java.util.List;

public class WrapperPercorso {

    WrapperPercorsoMarkerInfo[] markers;

    public WrapperPercorso(ArrayList<LatLng> vertices) {
        markers = new WrapperPercorsoMarkerInfo[vertices.size()];
        for(int i=0; i< markers.length; i++){
            LatLng vertex = vertices.get(i);
            markers[i] = new WrapperPercorsoMarkerInfo(new LatLong(vertex.latitude, vertex.longitude), this);
        }
    }

    public ArrayList<LatLng> getVertices() {
        ArrayList<LatLng> vertices = new ArrayList<>(4);
        for(int i=0; i< markers.length; i++) {
            vertices.add(new LatLng(markers[i].getPosition().getLatitude(), markers[i].getPosition().getLongitude()));
        }

        return vertices;
    }

    public LatLng[] getVerticesArray() {
        LatLng[] vertices = new LatLng[markers.length];
        for(int i=0; i< markers.length; i++) {
            vertices[i] = new LatLng(markers[i].getPosition().getLatitude(), markers[i].getPosition().getLongitude());
        }

        return vertices;
    }

    public WrapperPercorsoMarkerInfo[] getMarkersInfo() {
        return markers;
    }
}
