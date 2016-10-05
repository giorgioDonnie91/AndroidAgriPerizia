package org.droidplanner.android.network.request;

import org.droidplanner.android.network.NameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

public abstract class Request{
    private final String requestUrl;
    private final List<NameValuePair> parameters;

    public Request(String requestUrl, List<NameValuePair> parameters) {
        this.requestUrl = requestUrl;
        this.parameters = parameters;
    }

    public String getQuery() {
        StringBuilder result = new StringBuilder();

        Iterator<NameValuePair> iterator = parameters.iterator();
        if(iterator.hasNext())
            iterator.next().writeTo(result);

        while(iterator.hasNext()){
            result.append("&");
            iterator.next().writeTo(result);
        }

        return result.toString();
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public abstract HttpURLConnection connect() throws IOException;

}
