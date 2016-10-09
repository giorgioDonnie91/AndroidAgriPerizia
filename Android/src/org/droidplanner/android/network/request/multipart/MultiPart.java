package org.droidplanner.android.network.request.multipart;


import org.droidplanner.android.network.request.Request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiPart extends Request {


    private static final String MIME_TYPE = "multipart/form-data;boundary=" + Part.BOUNDARY;

    private Part[] parts;

    public MultiPart(String requestUrl, Part ... parts) {
        super(requestUrl, null);
        this.parts = parts;
    }

    @Override
    public HttpURLConnection connect() throws IOException {
        URL url = new URL(getRequestUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Cache-Control", "no-cache");
        urlConnection.setRequestProperty("Content-Type", MIME_TYPE);

        DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

        for(Part part : parts){
            part.write(dataOutputStream);
        }

        dataOutputStream.writeBytes(Part.TWO_HYPHENS + Part.BOUNDARY + Part.TWO_HYPHENS + Part.LINE_END);

        dataOutputStream.flush();
        dataOutputStream.close();

        urlConnection.connect();
        return urlConnection;
    }

    public interface Part{

        String TWO_HYPHENS = "--";
        String LINE_END = "\r\n";
        String BOUNDARY = "androidClientBoundary";

        void write(DataOutputStream dataOutputStream) throws IOException;
    }

}
