package org.droidplanner.android.network.request.multipart;


import java.io.DataOutputStream;
import java.io.IOException;


public class FieldPart implements MultiPart.Part {

    private String name;
    private String value;

    public FieldPart(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
        dataOutputStream.writeBytes(LINE_END);

        dataOutputStream.writeBytes(value);

        dataOutputStream.writeBytes(LINE_END);
    }
}
