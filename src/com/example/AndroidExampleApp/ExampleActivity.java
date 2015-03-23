package com.example.AndroidExampleApp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.AndroidExampleApp.R;

import java.util.Timer;
import java.util.TimerTask;

public class ExampleActivity extends Activity {

    private String name="World";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(savedInstanceState != null){
            name = (String) savedInstanceState.get("name");
            TextView hello = (TextView) findViewById(R.id.helloText);
            hello.setText("Hello "+name+", ExampleActivity");
        }

        final Button button = (Button) findViewById(R.id.buTimer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int time = 5;
                beforeTimer(time);
                Handler handler = new Handler();
                handler.postDelayed(runnable, time*1000);
            }
        });

        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show()
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart(){
        super.onStart();

        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause(){
        super.onPause();

        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop(){
        super.onStop();

        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        EditText usernameEt = (EditText) findViewById(R.id.nameInput);
        String username = usernameEt.getText().toString();

        savedInstanceState.putString("name", username);
    }

    private void beforeTimer(int time){
        TextView hello = (TextView) findViewById(R.id.helloText);
        hello.setText("Timer started and set to "+time+" seconds");
        Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT).show();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            afterTimer();
        }
    };

    public void afterTimer(){
        TextView hello = (TextView) findViewById(R.id.helloText);
        hello.setText("Timer finished");
        Toast.makeText(this, "Timer finished", Toast.LENGTH_SHORT).show();
    }

}
