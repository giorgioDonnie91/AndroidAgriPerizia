package org.droidplanner.android.network.request;


import org.droidplanner.android.network.NameValuePair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Post extends Request{

    public Post(String requestUrl, List<NameValuePair> parameters) {
        super(requestUrl, parameters);
    }

    @Override
    public HttpURLConnection connect() throws IOException {
        URL url = new URL(getRequestUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);

        DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
        dos.writeBytes(getQuery());
        dos.flush();
        dos.close();

        urlConnection.connect();
        return urlConnection;
    }
}
