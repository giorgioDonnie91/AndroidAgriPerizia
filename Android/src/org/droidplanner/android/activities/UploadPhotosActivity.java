package org.droidplanner.android.activities;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.droidplanner.android.R;
import org.droidplanner.android.network.ComunicazioneConServerRunnable;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UploadPhotosActivity extends DrawerNavigationUI{


    private ProgressBar progressBar;
    private ListView uploadedListView;
    private ArrayAdapter<String> adapter;

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_photos);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        uploadedListView = (ListView)findViewById(R.id.uploaded_list_view);
        adapter = new ArrayAdapter<>(this, R.layout.photo_file_layout, R.id.path);
        uploadedListView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().toString();
                ExecutorService executor = Executors.newFixedThreadPool(3);
                startUpload(new File(root+"/drone"), executor);
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startUpload(File file, ExecutorService executor) {
        if(!file.exists())
            return;

        if(file.isDirectory()){
            Log.i("NAVIGATE", file.getName());
            File[] childrenFiles = file.listFiles();
            for(File childFile : childrenFiles){
                startUpload(childFile, executor);
            }
        } else {
            uploadFile(file, executor);
        }
    }

    private void uploadFile(final File file, ExecutorService executor) {
        String tmp = file.getName();
        final String waypointIndex = tmp.substring(0, tmp.indexOf('.'));

        File parent = file.getParentFile();
        final String codiceSinistro = parent.getName();

        parent = parent.getParentFile();
        final String codicePercorso = parent.getName();


        executor.execute(new ComunicazioneConServerRunnable(
                ComunicazioneConServerRunnable.uploadPhotoRequest(file, codiceSinistro, codicePercorso, Integer.parseInt(waypointIndex)),
                new ComunicazioneConServerRunnable.RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        adapter.add(file.getPath());
                        Log.i("UPLOAD SUCCESS", "sinistro: " + codiceSinistro + "; percorso: " + codicePercorso + "; waypoint: " + waypointIndex + ". " + response);
                        File parent = file.getParentFile();
                        File grandParent = parent.getParentFile();

                        if(!file.delete() || parent.listFiles().length != 0)
                            return;

                        if(!parent.delete() || grandParent.listFiles().length != 0)
                            return;

                        grandParent.delete();
                    }

                    @Override
                    public void onError(int responseCode, String response) {
                        Log.i("UPLOAD ERROR", "sinistro: " + codiceSinistro + "; percorso: " + codicePercorso + "; waypoint: " + waypointIndex + ". " + response);
                    }
                }
        ));

    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_server_sync;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void addToolbarFragment(){}


}
