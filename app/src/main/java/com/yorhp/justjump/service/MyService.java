package com.yorhp.justjump.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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
import java.io.IOException;


public class MyService extends Service {

    public static boolean DEBUG = false;

    long spendTime = 0;
    static Process process = null;
    static DataOutputStream os = null;

    int distence = 0;
    public static String screenPath = MyApplication.rootDir + "/screenshots.png";

    ImageRecognition imageRecognition;


    public static String mePath = MyApplication.rootDir + "/opencv_me/me.png";

    boolean start;

    ImageView btnView3;
    boolean isAdded;
    Thread thread;

    public static final int FLAG_LAYOUT_INSET_DECOR = 0x00000200;

    WindowManager.LayoutParams params3;

    WindowManager windowManager;

    public MyService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification(getApplicationContext(), 0, "JustJump", "程序正在运行中");
        createWindowView();

        File file1 = new File(screenPath);
        if (file1.exists())
            file1.delete();
        imageRecognition = new ImageRecognition();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (start) {
                    Log.e("跳一下所用时间为",(System.currentTimeMillis() - spendTime)+"\n");
                    spendTime = System.currentTimeMillis();
                    execShellCmd("screencap -p " + screenPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(screenPath);
                    while (bitmap == null) {
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bitmap = BitmapFactory.decodeFile(screenPath);
                    }
                    distence = imageRecognition.getDistence(bitmap);
                    System.out.println("距离为：" + distence);

                    if (distence == 0) {
                        continue;
                    }

                    int time = getTime();
                    String msg = "input touchscreen swipe 560 1600 560 1600 " + time;
                    execShellCmd(msg);
                    try {
                        Thread.sleep(time + 1770);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (DEBUG)
                        start = false;
                }
            }
        });
    }

    private int getTime() {
        int time = 0;
        double k = (distence * (-0.00020) + 1.485);
        if (k > 1.416) {
            k = 1.416;
        }
        time = (int) (k * distence);
        System.out.println("系数为：" + k);
        return time;
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
        btnView3 = new ImageView(getApplicationContext());
        btnView3.setImageResource(R.drawable.ic_star);

        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

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

                if (DEBUG) {
                    start = true;
                    thread.start();
                } else {
                    start = true;
                    thread.start();
                    btnView3.setVisibility(View.INVISIBLE);
                }
            }
        });
        windowManager.addView(btnView3, params3);
        isAdded = true;
    }

    public static void execShellCmd(String cmd) {
        try {
            if (process == null) {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
            }
            os.writeBytes(cmd);
            os.writeBytes("\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File bitmapToPath(Bitmap bitmap, String name) {
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

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);

    }


    private void showNotification(Context context, int id, String title, String text) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_triangle);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setVisibility(Notification.VISIBILITY_SECRET);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
        startForeground(id, notification);
    }


}
