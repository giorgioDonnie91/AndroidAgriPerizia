package org.droidplanner.android.network;

import android.os.Handler;
import android.os.Looper;

import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import org.droidplanner.android.WaypointUtils;
import org.droidplanner.android.network.request.Get;
import org.droidplanner.android.network.request.Post;
import org.droidplanner.android.network.request.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;

public class ComunicazioneConServerThread extends Thread{

    private static final String BASE_URL = "http://giorgiopavarini.altervista.org/";

    private Request request;
    private RequestListener requestListener;

    public ComunicazioneConServerThread(Request request, RequestListener requestListener) {
        this.request = request;
        this.requestListener = requestListener;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection urlConnection = request.connect();

            final int responseCode = urlConnection.getResponseCode();
            InputStream in;
            if(responseCode == 200)
                in = urlConnection.getInputStream();
            else
                in = urlConnection.getErrorStream();

            byte[] buffer = new byte[8192];
            int numRead;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((numRead = in.read(buffer))!=-1){
                baos.write(buffer, 0, numRead); }
            in.close();
            final String responseContent = new String(baos.toByteArray());

            Handler mainHandler = new Handler(Looper.getMainLooper());
            if(responseCode == 200){
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestListener.onSuccess(responseContent);
                    }
                });
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestListener.onError(responseCode, responseContent);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Request selectPolizzeByCodiceClienteRequest(String codiceCliente){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("codC", codiceCliente));
        return new Get(BASE_URL + "selectPolizze.php", parameter);
    }

    public static Request selectPercorsiByCodicePolizzaRequest(String codicePolizza){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("CodPZ", codicePolizza));
        return new Get(BASE_URL + "getPercorsiByCodPZ.php", parameter);
    }


    public static Request createPercorso(String codicePolizza){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("codPR", String.valueOf( (int)(Math.random()*1000000) )));
        parameter.add(new NameValuePair("codPZ", codicePolizza));
        return new Post(BASE_URL + "newPercorso.php", parameter);
    }

    public static Request createPercorso(String codicePercorso, String codicePolizza){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("codPR", codicePercorso));
        parameter.add(new NameValuePair("codPZ", codicePolizza));
        return new Post(BASE_URL + "newPercorso.php", parameter);
    }

    public static Request selectSinistriByCodicePolizzaRequest(String codicePolizza){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("CodPZ", codicePolizza));
        return new Get(BASE_URL + "getSinistriByCodPZ.php", parameter);
    }

    public static Request selectWaypoints(String codicePercorso){
        LinkedList<NameValuePair> parameter = new LinkedList<>();
        parameter.add(new NameValuePair("CodPR", codicePercorso));
        return new Get(BASE_URL + "getWPByCodPR.php", parameter);
    }

    public static Request createWayPoints(String codicePercorso, List<Waypoint> waypoints) {
        Post post = new Post(BASE_URL + "addWayPoints.php", null);
        JSONArray jsonArray = new JSONArray();
        int i=0;
        try {
            for(Waypoint waypoint : waypoints){
                JSONObject jsonWaypoint = WaypointUtils.waypointToJSON(waypoint);
                jsonWaypoint.put("CodWP", i++);
                jsonWaypoint.put("CodPR", codicePercorso);
                jsonArray.put(jsonWaypoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post.setPayload(jsonArray.toString());
        return post;
    }

    public static Request uploadPhotoRequest(String codicePercorso, int index){
        return null;
        //return new MultiPart(BASE_URL + "testFoto.php", new FilePart(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/images.jpg"), "userfile"));
    }

    public interface RequestListener{
        void onSuccess(String response);
        void onError(int responseCode, String response);
    }

}
