package com.yorhp.justjump;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yorhp.justjump.service.MyService;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_PERMISSION_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }


}
