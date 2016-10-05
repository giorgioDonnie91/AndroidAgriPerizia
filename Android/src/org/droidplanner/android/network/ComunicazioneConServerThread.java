package org.droidplanner.android.network;

import android.os.Handler;
import android.os.Looper;

import org.droidplanner.android.network.request.Get;
import org.droidplanner.android.network.request.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedList;

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

            InputStream in = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((numRead=in.read(buffer))!=-1){
                baos.write(buffer, 0, numRead); }
            in.close();

            final int responseCode = urlConnection.getResponseCode();
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

    public interface RequestListener{
        void onSuccess(String response);
        void onError(int responseCode, String response);
    }

}
