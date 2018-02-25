package com.yorhp.justjump.opencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.yorhp.justjump.app.MyApplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static com.yorhp.justjump.service.MyService.bitmapToPath;
import static com.yorhp.justjump.service.MyService.mePath;
import static com.yorhp.justjump.service.MyService.screenPath;

public class ImageRecognition {

    public static int meX1, meX2, meY1, meY2;
    public static boolean ok = false;
    public static String img_clear=MyApplication.rootDir + "/img_clear.png";


    public int getDistence() {

        Bitmap bitmap = BitmapFactory.decodeFile(screenPath);
        int heightMe = (int) (bitmap.getHeight() * 0.4166);
        int height = (int) (bitmap.getHeight() * 0.3125);


        Bitmap bitmap_me = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * 0.4166), bitmap.getWidth(), (int) (bitmap.getHeight() * 0.2304));
        File file_me = bitmapToPath(bitmap_me, "img_me");
        String originmePath = file_me.getPath();


        LikeResult result1 = matchImageMe(originmePath, mePath);

        android.graphics.Point point1 = result1.getPoint();



        Bitmap workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(workingBitmap);

        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(bitmap.getPixel((int) (bitmap.getHeight()*0.32),500));
        paint.setStyle(Paint.Style.FILL);//不填充
        canvas.drawRect(meX1,meY1+heightMe,meX2,meY2+heightMe,paint);
        bitmapToPath(workingBitmap, "img_clear");


        android.graphics.Point point2 = getPoint();

        System.out.println("width=" + point2.x + "，height=" + point2.y);

        int distence = (int) (Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y + heightMe - point2.y - height) * (point1.y + heightMe - point2.y - height)));

        return distence;
    }


    public LikeResult matchImageMe(String originalFilePath, String templateFilePath1) {

        Mat template = Highgui.imread(templateFilePath1, Highgui.CV_LOAD_IMAGE_COLOR);
        Mat source = Highgui.imread(originalFilePath, Highgui.CV_LOAD_IMAGE_COLOR);

        //创建于原图相同的大小，储存匹配度
        Mat result = Mat.zeros(source.rows() - template.rows() + 1, source.cols() - template.cols() + 1, CvType.CV_8UC1);
        //调用模板匹配方法
        Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);
        //规格化
        //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1);
        //获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
        Core.MinMaxLocResult mlr = Core.minMaxLoc(result);

        System.out.println("相似度：" + mlr.maxVal);

        Point matchLoc = mlr.maxLoc;
        //在原图上的对应模板可能位置画一个绿色矩形
        Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
        //将结果输出到对应位置
        //System.out.println("中心点位置：" + matchLoc.x + template.width() / 2 + "，" + matchLoc.y + template.height() / 2);

        int x = (int) (matchLoc.x + template.width() / 2);
        int y = (int) (matchLoc.y + template.height() * 0.875);

        if (mlr.maxVal > 0.6) {
            ok = true;
            meX1 = (int) matchLoc.x;
            meX2 = (int) (matchLoc.x + template.width());
            meY1 = (int) matchLoc.y;
            meY2 = (int) (matchLoc.y + template.height());
        } else {
            ok = false;
        }


        android.graphics.Point point = new android.graphics.Point();
        point.set(x, y);

        Highgui.imwrite(MyApplication.rootDir + "matching/" + "/原图中的匹配图" + ".jpg", source);

        LikeResult likeResult = new LikeResult();
        likeResult.setPoint(point);
        likeResult.setLikeLevel(mlr.maxVal);

        return likeResult;
    }


    public static android.graphics.Point getPoint() {
        Bitmap bitmapx = BitmapFactory.decodeFile(img_clear);
        Bitmap bitmap_next = bitmapx.createBitmap(bitmapx, 0, (int) (bitmapx.getHeight() * 0.3125), bitmapx.getWidth(), (int) (bitmapx.getHeight() * 0.2713));
        String img_nextPath="img_next";
        File next = bitmapToPath(bitmap_next,img_nextPath);
        String originPath = next.getPath();
        if (!ok) {
            return new android.graphics.Point(0, 0);
        }
        Bitmap workingBitmap = BitmapFactory.decodeFile(originPath);
        Bitmap bitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(2);  //线的宽度

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        System.out.println("宽度为：" + bitmap.getWidth());
        int firstX = 0;
        int outY = 0;

        //保存第一次遇到的颜色
        int firstPoint = 0;

        //第一次进入的颜色
        int inColor = 0;

        android.graphics.Point firstInPoint = new android.graphics.Point(0, 0);

        //保存出踏板的横坐标
        int widthest = 0;

        int firstInx = 0;

        for (int i = 0; i < height; i++) {
            boolean out = false;
            boolean in = false;

            for (int j = 0; j < width; j++) {//同一条线里面

                //getPicturePixel(bitmap, j, i);

                int clr = bitmap.getPixel(j, i);

                if (firstPoint == 0) {//保存背景色
                    firstPoint = clr;
                }



                if (firstInx == 0 && !eque(clr, firstPoint)) {//第一次进入
                    inColor = clr;
                    firstInx = j;
                    firstInPoint = new android.graphics.Point(j, i);
                    in = true;
                    System.out.println("第一次进入" + j + "，" + i);
                    canvas.drawPoint(j, i, paint);
                    if (eque(bitmap.getPixel(j, i + 2), firstPoint)) {//出去了
                        System.out.println("第一次进入马上出去了" + j);
                        firstX = j;
                        out = true;
                    }
                } else if (!in && !eque(clr, firstPoint) && ((eque(clr, inColor) || eque(clr, bitmap.getPixel(firstInPoint.x, firstInPoint.y))))) {//可能再次进入了
                    in = true;
                    System.out.println("进去了：x" + j + "，y：" + i);




                }

                if (!out && in) {//进去了没出来
                    if (firstInx != 0 && eque(clr, firstPoint) && firstX == 0) {//第一次进去后出来了
                        firstX = (j + firstInx) / 2;
                        System.out.println("第一次进去后出来了");
                        out = true;
                        firstInPoint = new android.graphics.Point((firstInPoint.x + j) / 2, i);
                    }


                    if (!out && in && eque(clr, firstPoint)) {//出来了
                        System.out.println("出来了x：" + j + "，y：" + i + "/" + width);
                        if (j > (widthest)) {
                            widthest = j;
                            outY = i;
                        } else if (!eque(bitmap.getPixel(j, i + 2), inColor)) {//
                            canvas.drawPoint(j, i, paint);
                            canvas.drawPoint(firstX, i, paint);
                            bitmapToPath(bitmap, "img_next_find");
                            return new android.graphics.Point(firstX, i);
                        }
                        out = true;
                    } else if (j == 1079) {


                        if (eque(bitmap.getPixel(j, i), inColor)) {
                            widthest = j;
                            outY = i;
                        }

                        System.out.println("出不来了" + (!out) + in + (eque(clr, firstPoint)));

                        System.out.println("R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));

                        System.out.println("R：" + Color.red(firstPoint) + "，G：" + Color.green(firstPoint) + "，B：" + Color.blue(firstPoint));

                    }
                }
            }
            i = i + 1;
        }
        canvas.drawPoint(widthest, outY, paint);
        canvas.drawPoint(firstX, outY, paint);
        bitmapToPath(bitmap, "img_next_find");
        System.out.println("没找到出来了x：" + widthest + "，y：" + outY + "/" + height);



        return new android.graphics.Point(firstX, outY);
    }


    int count = 0;


    public static boolean eque(int clr, int clr2) {
        int red = Color.red(clr); // 取高两位
        int green = Color.green(clr);
        ; // 取中两位
        int blue = Color.blue(clr);// 取低两位


        int red2 = Color.red(clr2); // 取高两位
        int green2 = Color.green(clr2);
        ; // 取中两位
        int blue2 = Color.blue(clr2);// 取低两位


        if (red == red2 && green == green2 && blue == blue2) {
            return true;
        }

        if ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < 25) {
            return true;
        }


        return false;

    }

    public static boolean isMe(int clr) {
        int red = Color.red(clr); // 取高两位
        int green = Color.green(clr);
        ; // 取中两位
        int blue = Color.blue(clr);// 取低两位


        int red2 = 53; // 取高两位
        int green2 = 54;
        ; // 取中两位
        int blue2 = 61;// 取低两位


        if ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < 55) {
            return true;
        }


        return false;

    }

    public class LikeResult {
        android.graphics.Point point;
        double likeLevel;
        int distence;

        public android.graphics.Point getPoint() {
            return point;
        }

        public void setPoint(android.graphics.Point point) {
            this.point = point;
        }

        public double getLikeLevel() {
            return likeLevel;
        }

        public void setLikeLevel(double likeLevel) {
            this.likeLevel = likeLevel;
        }

        public int getDistence() {
            return distence;
        }

        public void setDistence(int distence) {
            this.distence = distence;
        }
    }


    public static void delete(String path) {
        File f = new File(path);
        if (f.isDirectory()) {//如果是目录，先递归删除
            String[] list = f.list();
            for (int i = 0; i < list.length; i++) {
                delete(path + "//" + list[i]);//先删除目录下的文件
            }
        }
        f.delete();
    }


}
