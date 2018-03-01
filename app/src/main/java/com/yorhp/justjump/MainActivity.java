package com.yorhp.justjump;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.yorhp.justjump.opencv.ImageRecognition;
import com.yorhp.justjump.service.MyService;


public class MainActivity extends AppCompatActivity {


    private Button btnProc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        System.loadLibrary("opencv_java");
        final Intent intent = new Intent(this, MyService.class);

        btnProc = (Button) findViewById(R.id.btn_proc);
        btnProc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageRecognition.ok=true;
                        com.yorhp.justjump.util.Test.erroTest();
                    }
                });

                //thread.start();
                startService(intent);
                moveTaskToBack(true);
            }
        });


    }


}
