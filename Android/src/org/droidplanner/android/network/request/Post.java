package org.droidplanner.android.network.request;


import android.text.TextUtils;

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

    public String payload;

    public Post(String requestUrl, List<NameValuePair> parameters) {
        super(requestUrl, parameters);
    }

    public void setPayload(String payload){
        this.payload = payload;
    }

    @Override
    public HttpURLConnection connect() throws IOException {
        URL url = new URL(getRequestUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        String postParameters;
        if(TextUtils.isEmpty(payload)){
            postParameters = getQuery();
        } else {
            postParameters = payload;
        }

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
