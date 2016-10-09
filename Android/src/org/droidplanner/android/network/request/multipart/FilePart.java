package org.droidplanner.android.network.request.multipart;

import android.webkit.MimeTypeMap;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FilePart implements MultiPart.Part {

    private final String fileNameField;
    private File file;


    public FilePart(File file, String fileNameField) {
        this.fileNameField = fileNameField;
        this.file = file;
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileNameField + "\"; filename=\""+file.getName()+"\"" + LINE_END);
        String fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getName()));
        dataOutputStream.writeBytes("Content-Type: "+ fileMimeType + LINE_END);
        dataOutputStream.writeBytes(LINE_END);

        int read;
        byte[] buffer = new byte[8192];
        FileInputStream fileInputStream = new FileInputStream(file);
        while((read = fileInputStream.read(buffer)) != -1){
            dataOutputStream.write(buffer, 0, read);
        }

        dataOutputStream.writeBytes(LINE_END);
    }
}
