package com.yorhp.justjump.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yorhp.justjump.R;
import com.yorhp.justjump.app.MyApplication;
import com.yorhp.justjump.opencv.ImageRecognition;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class MyService extends Service {


    int distence = 0;
    private String screenPath = MyApplication.rootDir + "/screenshots.png";

    ImageRecognition imageRecognition;


    String me = MyApplication.rootDir + "/opencv_me/me.png";


    ImageView btnView1, btnView2, btnView3;
    boolean isAdded;
    Thread thread;

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
        imageRecognition = new ImageRecognition();


        String[] templateFilePath = null;

        File file = new File(MyApplication.rootDir + "opencv_template/");


        if (file.isDirectory()) {

            File[] array = file.listFiles();

            templateFilePath = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                templateFilePath[i] = array[i].getPath();
            }
        }

        System.out.println("模板个数为：" + templateFilePath.length);

        String[] finalTemplateFilePath = templateFilePath;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i >= 0) {
                    File file1 = new File(MyApplication.rootDir + "/screenshots.png");
                    if (file1.exists())
                        file1.delete();
                    execShellCmd("screencap -p " + MyApplication.rootDir + "/screenshots.png");
                    Bitmap bitmap = BitmapFactory.decodeFile(screenPath);
                    while (bitmap == null) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bitmap = BitmapFactory.decodeFile(screenPath);
                    }
                    Bitmap bitmap_next = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * 0.3125), bitmap.getWidth(), (int) (bitmap.getHeight() * 0.2713));
                    File next = bitmapToPath(bitmap_next, "img_next");

                    Bitmap bitmap_me = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * 0.4166), bitmap.getWidth(), (int) (bitmap.getHeight() * 0.2304));
                    File file_me = bitmapToPath(bitmap_me, "img_me");
                    distence = imageRecognition.getDistence(file_me.getPath(), me, next.getPath(), finalTemplateFilePath, (int) (bitmap.getHeight() * 0.4166), (int) (bitmap.getHeight() * 0.3125));


                    System.out.println("距离为：" + distence);
                    File file = new File(screenPath);
                    if (file.exists()) {
                        file.delete();
                    }
                    int time = (int) (distence * 1.4);
                    String msg = "input touchscreen swipe 170 187 170 187 " + time;
                    execShellCmd(msg);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        btnView1.setImageResource(R.drawable.ic_point_white);
        btnView2.setImageResource(R.drawable.img_chess);
        btnView3.setImageResource(R.drawable.ic_star);


        btnView1.setVisibility(View.GONE);
        btnView2.setVisibility(View.GONE);

        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();

        // 设置Window Type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params.width = 50;
        params.height = 50;
        params.gravity = Gravity.TOP;
        params.x = 540;
        params.y = 540;

        System.out.println("params.x：" + params.x);

        params2 = new WindowManager.LayoutParams();

        // 设置Window Type
        params2.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params2.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params2.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params2.width = 30;
        params2.height = 30;
        params2.gravity = Gravity.START;
        params2.x = dip2px(getApplicationContext(), 540);


        params2.y = 960;


        params3 = new WindowManager.LayoutParams();

        // 设置Window Type
        params3.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params3.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params3.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params3.width = 200;
        params3.height = 200;
        params3.gravity = Gravity.TOP;
        params3.x = 300;
        params3.y = 200;

        /*btnView1.setOnTouchListener(new View.OnTouchListener() {
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
        });*/

        // 设置悬浮框的Touch监听
        /*
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
        });*/


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
/*                distence = (int) (Math.sqrt((params.x - params2.x) * (params.x - params2.x) + (params.y - params2.y) * (params.y - params2.y)));
                Log.e("两点的真正距离为：", distence + "");*/
                thread.start();
            }
        });
        //windowManager.addView(btnView1, params);
        //windowManager.addView(btnView2, params2);
        windowManager.addView(btnView3, params3);
        isAdded = true;
    }

    private void execShellCmd(String cmd) {
        try {
            // 获取输出流
            Process process = Runtime.getRuntime().exec("su");
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


    private File bitmapToPath(Bitmap bitmap, String name) {
        String filepath = MyApplication.rootDir + "/" + name + ".png";
        File file = new File(filepath);
        //3.保存Bitmap
        try {
            //文件

            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    windowManager.updateViewLayout(btnView1, params);
                    break;
                default:
                    break;
            }

        }
    };


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);

    }


}
