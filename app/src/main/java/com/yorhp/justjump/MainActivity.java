package com.yorhp.justjump;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yorhp.justjump.app.MyApplication;
import com.yorhp.justjump.service.MyService;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static com.yorhp.justjump.opencv.ImageRecognition.getPoint;

public class MainActivity extends AppCompatActivity {


    private boolean bProc = false;
    private Button btnProc;
    private ImageView lenaView;
    private Bitmap bmp;

    String filepath = MyApplication.rootDir + "/" + "img_next" + ".png";


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
        btnProc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startService(intent);
                /*if (!bProc) {
                    Bitmap grayBmp = toGray(bmp);
                    lenaView.setImageBitmap(grayBmp);
                    btnProc.setText(R.string.undo);
                    bProc = true;
                } else {
                    lenaView.setImageBitmap(bmp);
                    btnProc.setText(R.string.gray_proc);
                    bProc = false;
                }*/

                //int clr1 = getPicturePixel(filepath, 340, 165);
                //int clr2 = getPicturePixel(filepath, 0, 0);
                //eque(clr1, clr2);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Point point = getPoint(filepath);
                        System.out.println("x：" + point.x + "，y：" + point.y);
                    }
                });

                //thread.start();
            }
        });


    }


    public int getPicturePixel(String originPath, int x, int y) {
        Bitmap workingBitmap = BitmapFactory.decodeFile(originPath);
        int clr = workingBitmap.getPixel(x, y);
        System.out.println("R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));
        return clr;
    }

    public void getPicturePixel(Bitmap workingBitmap, int x, int y) {
        int clr = workingBitmap.getPixel(x, y);
        System.out.println("x："+x+"，y："+(y)+"，R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));
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
