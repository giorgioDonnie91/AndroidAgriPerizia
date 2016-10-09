package org.droidplanner.android.network.request;


import org.droidplanner.android.network.NameValuePair;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

        String postParameters = getQuery();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");

        DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

        dataOutputStream.writeBytes(postParameters);
        dataOutputStream.flush();
        dataOutputStream.close();

        urlConnection.connect();
        return urlConnection;
    }

}
