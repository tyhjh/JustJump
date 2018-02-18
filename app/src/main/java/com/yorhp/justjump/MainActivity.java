package com.yorhp.justjump;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.yorhp.justjump.app.MyApplication;
import com.yorhp.justjump.service.MyService;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static com.yorhp.justjump.opencv.ImageRecognition.borderColor;
import static com.yorhp.justjump.opencv.ImageRecognition.getPoint;

public class MainActivity extends AppCompatActivity {


    private Button btnProc, btn_test;
    private ImageView lenaView;
    private Bitmap bmp;

    String filepath = MyApplication.rootDir + "/" + "img_next" + ".png";

    public static ArrayList<Integer> boderColor =new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.loadLibrary("opencv_java");
        final Intent intent = new Intent(this, MyService.class);

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.lena);
        lenaView = (ImageView) findViewById(R.id.imageview_lena);
        lenaView.setImageBitmap(bmp);

        btnProc = (Button) findViewById(R.id.btn_proc);
        btn_test = (Button) findViewById(R.id.btn_test);
        btnProc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(borderColor().size()==0){
                    Toast.makeText(MainActivity.this,"初始化中",Toast.LENGTH_SHORT).show();
                }else {
                    startService(intent);
                }
            }
        });

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Point point = getPoint(filepath);
                        System.out.println("x：" + point.x + "，y：" + point.y);
                    }
                }).start();
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                boderColor=borderColor();
                Log.e("数量为：",""+boderColor.size());
            }
        }).start();

    }


    public int getPicturePixel(String originPath, int x, int y) {
        Bitmap workingBitmap = BitmapFactory.decodeFile(originPath);
        int clr = workingBitmap.getPixel(x, y);
        System.out.println("R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));
        return clr;
    }

    public void getPicturePixel(Bitmap workingBitmap, int x, int y) {
        int clr = workingBitmap.getPixel(x, y);
        System.out.println("x：" + x + "，y：" + (y) + "，R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));
    }


    public static Bitmap toGray(Bitmap bmp) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Utils.bitmapToMat(bmp, rgbMat);
        //OpenCV的java API
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Bitmap grayBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(grayMat, grayBmp);
        return grayBmp;
    }


}
