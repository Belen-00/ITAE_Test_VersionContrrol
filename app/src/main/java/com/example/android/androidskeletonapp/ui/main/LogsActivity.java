package com.example.android.androidskeletonapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.ui.base.ListActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogsActivity extends ListActivity {


    public static Intent getIntent(Context context) {
        return new Intent(context,LogsActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp(R.layout.logs_activity, R.id.imagesToolbar, R.id.imagesProgress);
        //setContentView(R.layout.main);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append("\n"+line);
            }
            TextView tv = (TextView)findViewById(R.id.logs);
            tv.setText(log.toString());
            tv.setMovementMethod(new ScrollingMovementMethod());
        } catch (IOException e) {
        }
    }
}
