package com.yorhp.justjump.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibility extends AccessibilityService {
    private static final String TAG = "MyAccessibility";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("NewApi")
    @Override  
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub  
        int eventType = event.getEventType();  
        String eventText = "";  
        Log.i(TAG, "==============Start====================");
        switch (eventType) {  
        case AccessibilityEvent.TYPE_VIEW_CLICKED:
            eventText = "TYPE_VIEW_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:  
            eventText = "TYPE_VIEW_LONG_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  
            eventText = "TYPE_WINDOW_STATE_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:  
            eventText = "TYPE_NOTIFICATION_STATE_CHANGED";  
            break;
        }  
        Log.i(TAG, eventText);  
        Log.i(TAG, "=============END=====================");  
    }



  
    @Override  
    public void onInterrupt() {  
        // TODO Auto-generated method stub  
    }  
}  