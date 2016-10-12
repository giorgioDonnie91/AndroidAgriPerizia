package org.droidplanner.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.droidplanner.android.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view){
        Intent polizzeIntent = new Intent(this, PolizzeActivity.class);
        startActivity(polizzeIntent);
    }

}
