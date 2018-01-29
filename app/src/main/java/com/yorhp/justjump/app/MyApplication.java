package com.yorhp.justjump.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Tyhj on 2017/5/23.
 */

public class MyApplication extends Application {
    private static MyApplication instance;
    public static final boolean ISDEBUG = true;
    public static ArrayList<Activity> activities = new ArrayList<>();
    public static String rootDir;
    public static boolean isFirstLog=false;


    public void onCreate() {
        super.onCreate();
        instance = this;
        initPicasso();
        initDir();
    }

    //Picasso初始化
    private void initPicasso() {
        Picasso picasso = new Picasso.Builder(this)
                .memoryCache(new LruCache(10 << 20))//设置内存缓存大小10M
                //.indicatorsEnabled(false) //设置左上角标记，主要用于测试
                .build();
        Picasso.setSingletonInstance(picasso);
    }

    //文件夹初始化
    public void initDir() {
        rootDir = Environment.getExternalStorageDirectory() + "/ATranscribeScreen/";
        File f1 = new File(rootDir);
        if (!f1.exists()) {
            f1.mkdirs();
        }
        File f2 = new File(rootDir + "gif/");
        if (!f2.exists()) {
            f2.mkdirs();
        }
        File f3 = new File(rootDir + "mp4/");
        if (!f3.exists()) {
            f3.mkdirs();
        }

        File f4 = new File(rootDir + "download/");
        if (!f4.exists()) {
            f4.mkdirs();
        }

        File f5 = new File(rootDir + "matching/");
        if (!f5.exists()) {
            f5.mkdirs();
        }


        File f6 = new File(rootDir + "opencv_template/");
        if (!f6.exists()) {
            f6.mkdirs();
        }


    }

    //打印初始化
    public static void log(String key, String value) {
        if (ISDEBUG)
            Log.e(key, value + "");
    }

    public static Context getAppContexts() {
        return instance;
    }

    public static void closeAllActivity() {
        int leanth = activities.size();
        for (int i = 0; i < leanth; i++) {
            activities.get(i).finish();
        }
    }





}


//
//                               _oo0oo_
//                             o8888888o
//                              88" . "88
//                               (|  -_-  |)
//                              0\  =  /0
//                            ___/`---'\___
//                          .' \\|          |// '.
//                        / \\|||     :    |||// \
//                      / _ |||||    -:-   ||||| - \
//                     |   | \\\     -    /// |   |
//                     | \_ |  ''\ --- /''  |_ / |
//                     \  .- \__  '-'  ___/-. /
//                     ___'. .'  /--.--\  `. .'___
//              ."" '<  `.___\_<|>_/___.' >' "".
//            | | :  `- \ `.;` \     _    / `;.` / - ` : | |
//            \  \   `_.   \_  __\ /__  _/   .-`  /  /
// =====`-.____`.___ \_____/___.-`___.-'=====
//                             `=---='
//
//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//
//
//
