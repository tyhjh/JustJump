package com.yorhp.justjump.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yorhp.justjump.R;

import java.io.DataOutputStream;
import java.io.OutputStream;


public class MyService extends Service {

    int distence=0;

    ImageView btnView1,btnView2,btnView3;
    boolean isAdded;
    


    public static final int FLAG_LAYOUT_INSET_DECOR = 0x00000200;

    WindowManager.LayoutParams params;
    WindowManager.LayoutParams params2;
    WindowManager.LayoutParams params3;

    WindowManager windowManager;


    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createWindowView();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createWindowView() {
        btnView1 = new ImageView(getApplicationContext());
        btnView2 = new ImageView(getApplicationContext());
        btnView3 = new ImageView(getApplicationContext());
        btnView1.setImageResource(R.drawable.img_chess);
        btnView2.setImageResource(R.drawable.img_chess);
        btnView3.setImageResource(R.drawable.ic_star);
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();

        // 设置Window Type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params.width = 350;
        params.height = 350;
        params.gravity = Gravity.LEFT;
        params.x = 200;
        params.y = 000;



        params2 = new WindowManager.LayoutParams();

        // 设置Window Type
        params2.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params2.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params2.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params2.width = 350;
        params2.height = 350;
        params2.gravity = Gravity.LEFT;
        params2.x = 800;
        params2.y = 000;


        params3 = new WindowManager.LayoutParams();

        // 设置Window Type
        params3.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params3.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params3.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params3.width = 200;
        params3.height = 200;
        params3.gravity = Gravity.TOP;
        params3.x = 300;
        params3.y = 300;

        // 设置悬浮框的Touch监听
        btnView1.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            int paramX, paramY;
            //保存悬浮框最后位置的变量
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        // 更新悬浮窗位置
                        windowManager.updateViewLayout(btnView1, params);
                        break;
                }
                return true;
            }
        });
        btnView2.setOnTouchListener(new View.OnTouchListener() {
            //保存悬浮框最后位置的变量
            int lastX, lastY;
            int paramX, paramY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params2.x;
                        paramY = params2.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params2.x = paramX + dx;
                        params2.y = paramY + dy;
                        // 更新悬浮窗位置
                        windowManager.updateViewLayout(btnView2, params2);
                        break;
                }
                return true;
            }
        });


        btnView3.setOnTouchListener(new View.OnTouchListener() {
            //保存悬浮框最后位置的变量
            int lastX, lastY;
            int paramX, paramY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params3.x;
                        paramY = params3.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params3.x = paramX + dx;
                        params3.y = paramY + dy;
                        // 更新悬浮窗位置
                        windowManager.updateViewLayout(btnView3, params3);
                        break;
                }
                return false;
            }
        });


        btnView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distence= (int) (Math.sqrt((params.x-params2.x)*(params.x-params2.x)+(params.y-params2.y)*(params.y-params2.y)));
                Log.e("两点的距离为：",distence+"");
                touch(distence);

            }
        });
        windowManager.addView(btnView1, params);
        windowManager.addView(btnView2, params2);
        windowManager.addView(btnView3, params3);
        isAdded = true;
    }


    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private void touch(int distance){
        int time= (int) (distance*1.4);
        String msg="input touchscreen swipe 170 187 170 187 "+time;
        execShellCmd(msg);
    }


}
