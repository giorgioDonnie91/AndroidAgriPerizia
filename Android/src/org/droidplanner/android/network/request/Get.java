package org.droidplanner.android.network.request;

import org.droidplanner.android.network.NameValuePair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class Get extends Request {

    public Get(String requestUrl, List<NameValuePair> parameters) {
        super(requestUrl, parameters);
    }

    @Override
    public String getRequestUrl() {
        return super.getRequestUrl() + "?" + getQuery();
    }

    @Override
    public HttpURLConnection connect() throws IOException {
        URL url = new URL(getRequestUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        return urlConnection;
    }


}
