package org.droidplanner.android.network;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NameValuePair{
    public final String name;
    public final String value;

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void writeTo(StringBuilder stringBuilder){
        try {
            stringBuilder.append(URLEncoder.encode(name, "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}