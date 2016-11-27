package org.droidplanner.android.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.droidplanner.android.R;
import org.droidplanner.android.network.ComunicazioneConServerRunnable;
import org.droidplanner.android.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    public void login(View view){
        try {
            String encryptedPassword = encrypt(password.getText().toString());
            String urlEncodedPassword = URLEncoder.encode(encryptedPassword, "utf-8");

            //inserted password
            Log.i("original password", "p: '" + password.getText() + "'");
            //Utils.log("original password: '" + password.getText() + "'");

            //password saved in db
            Log.i("encrypted password", "p: '" + encryptedPassword + "'");
            //Utils.log("encrypted password: '" + encryptedPassword + "'");

            //urlencoded password
            Log.i("urlencoded password", "p: '" + urlEncodedPassword + "'");
            //Utils.log("urlencoded password: '" + urlEncodedPassword + "'");

            progressBar.setVisibility(View.VISIBLE);
            new Thread(new ComunicazioneConServerRunnable(
                    ComunicazioneConServerRunnable.login(username.getText().toString(), encryptedPassword),
                    new ComunicazioneConServerRunnable.RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            Log.i("Log in", "Successo");
                            progressBar.setVisibility(View.GONE);
                            Intent polizzeIntent = new Intent(LoginActivity.this, PolizzeActivity.class);
                            startActivity(polizzeIntent);
                        }

                        @Override
                        public void onError(int responseCode, String response) {
                            Log.i("Log in", "Errore: " + responseCode);
                            progressBar.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                            builder.setMessage(R.string.dialog_wrong_password_message)
                                    .setTitle(R.string.dialog_wrong_password_title);

                            builder.create().show();
                        }
                    })
            ).start();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String value){
        try {
            // Create the digest
            MessageDigest sha = MessageDigest.getInstance("SHA-1");

            // Add the input byte array(s), e.g, i1, i2 and i3 sha.update(i1);
            sha.update(value.getBytes());
            //Compute the hash byte[] hash = sha.digest();

            return Base64.encodeToString(sha.digest(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class jkdsncd implements ComunicazioneConServerRunnable.RequestListener{

        @Override
        public void onSuccess(String response) {

        }

        @Override
        public void onError(int responseCode, String response) {

        }
    }

}
