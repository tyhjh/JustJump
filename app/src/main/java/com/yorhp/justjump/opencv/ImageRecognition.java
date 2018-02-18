package com.yorhp.justjump.opencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.yorhp.justjump.MainActivity;
import com.yorhp.justjump.app.MyApplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

import static com.yorhp.justjump.service.MyService.bitmapToPath;
import static com.yorhp.justjump.service.MyService.me;

public class ImageRecognition {

    public static ArrayList<Integer> boderColor = MainActivity.boderColor;

    public static boolean first = true;

    public int getDistence(String originmePath, String mePath, String originPath, int heightMe, int height) {

        File file = new File(MyApplication.rootDir + "matching/");
        delete(MyApplication.rootDir + "matching/");
        file.mkdirs();

        LikeResult result1 = matchImageMe(originmePath, mePath);

        android.graphics.Point point1 = result1.getPoint();

        android.graphics.Point point2 = getPoint(originPath);

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


        android.graphics.Point point = new android.graphics.Point();
        point.set(x, y);

        Highgui.imwrite(MyApplication.rootDir + "matching/" + "/原图中的匹配图" + ".jpg", source);

        LikeResult likeResult = new LikeResult();
        likeResult.setPoint(point);
        likeResult.setLikeLevel(mlr.maxVal);

        return likeResult;
    }


    public static android.graphics.Point getPoint(String originPath) {

        if (boderColor.size() == 0) {
            boderColor = borderColor();
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

        int borderFirstY = 0;

        android.graphics.Point firstInPoint = new android.graphics.Point(0, 0);

        //保存出踏板的横坐标
        int widthest = 0;

        boolean lesswidth = false;

        boolean estwidth = false;


        //保存进踏板的坐标
        android.graphics.Point widtdless = new android.graphics.Point(0, 0);


        android.graphics.Point widtdest = new android.graphics.Point(0, 0);

        int firstInx = 0;

        firstPoint = bitmap.getPixel(100, 100);


        for (int i = 0; i < height; i++) {
            boolean out = false;
            boolean in = false;
            boolean isMe = false;

            for (int j = 0; j < width; j++) {//同一条线里面

                int clr = bitmap.getPixel(j, i);


                if (!isMe && isMe(clr)) {
                    isMe = true;
                }

                if (isMe && eque(clr, firstPoint)) {
                    isMe = false;
                }


                if (firstInx == 0 && !eque(clr, firstPoint) && !isMe) {//第一次进入
                    widtdless.x = j;
                    inColor = bitmap.getPixel(j, i + 2);
                    firstInx = j;
                    firstInPoint = new android.graphics.Point(j, i);
                    in = true;
                    System.out.println("第一次进入" + j + "，" + i);
                    canvas.drawPoint(j, i, paint);
                    if (eque(bitmap.getPixel(j, i + 2), firstPoint)) {//出去了
                        //System.out.println("第一次进入马上出去了" + j);
                        firstX = j;
                        out = true;
                    }
                } else if (!isMe && !in && !eque(clr, firstPoint) && !isShadow(clr)) {//可能再次进入了
                    in = true;
                    if (widtdless.x > j && !lesswidth) {
                        widtdless = new android.graphics.Point(j, i);
                    } else if (!lesswidth && findEst(bitmap, inColor, j, i)) {
                        widtdless = new android.graphics.Point(j, i);
                        canvas.drawPoint(j, i, paint);
                        lesswidth = true;
                        System.out.println("找到了最左边的点x：" + widtdless.x + " y：" + widtdless.y);
                        if (estwidth) {
                            return getPoint(firstInx, bitmap, canvas, widtdless, widtdest, inColor);
                        }

                    }
                    //System.out.println("进去了：x" + j + "，y：" + i);
                }

                if (!out && in) {//进去了没出来
                    if (firstInx != 0 && eque(clr, firstPoint) && firstX == 0) {//第一次进去后出来了
                        firstX = (j + firstInx) / 2;
                        System.out.println("第一次进去后出来了");
                        out = true;
                        firstInPoint = new android.graphics.Point((firstInPoint.x + j) / 2, i);
                    }


                    if (!out && in && (eque(clr, firstPoint) || isMe(clr))) {//出来了
                        //System.out.println("出来了x：" + j + "，y：" + i + "/" + width);
                        if (j > (widthest) && !estwidth) {
                            widthest = j;
                            outY = i;
                        } else if (findEst(bitmap, inColor, j, i) && !estwidth) {//
                            widtdest = new android.graphics.Point(j, i);
                            canvas.drawPoint(j, i, paint);
                            estwidth = true;
                            System.out.println("找到了最右边的点x：" + widtdest.x + " y：" + widtdest.y);
                            if (lesswidth) {
                                return getPoint(firstX, bitmap, canvas, widtdless, widtdest, inColor);
                            }
                        }
                        out = true;
                    } else if (!out && in && j == 1079 && !estwidth) {


                        if (borderFirstY == 0) {
                            borderFirstY = i;
                        }

                        if (findEst(bitmap, inColor, j, i)) {
                            widtdest = new android.graphics.Point(j, (borderFirstY + i) / 2);
                            canvas.drawPoint(j, i, paint);
                            estwidth = true;
                            if (lesswidth) {
                                return getPoint(firstX, bitmap, canvas, widtdless, widtdest, inColor);
                            }
                            out = true;
                        }


                        /*System.out.println("出不来了" + (!out) + in + (eque(clr, firstPoint)));
                        System.out.println("R：" + Color.red(clr) + "，G：" + Color.green(clr) + "，B：" + Color.blue(clr));
                        System.out.println("R：" + Color.red(firstPoint) + "，G：" + Color.green(firstPoint) + "，B：" + Color.blue(firstPoint));*/
                    }
                }
            }
            i = i + 1;
        }


        return getPoint(firstX, bitmap, canvas, widtdless, widtdest, inColor);
    }


    @NonNull
    private static android.graphics.Point getPoint(int firstX, Bitmap bitmap, Canvas canvas, android.graphics.Point widtdless, android.graphics.Point widtdest, int inColor) {
        int x = (widtdest.x + widtdless.x) / 2;
        int y = (widtdest.y + widtdless.y) / 2 - 5;
        if (bitmap.getPixel(x, y) != Color.WHITE && inColor != Color.WHITE && !first) {
            x = firstX;
            y = widtdest.y;
        }

        Paint paint2 = new Paint();
        paint2.setColor(Color.YELLOW);
        paint2.setStyle(Paint.Style.STROKE);//不填充
        paint2.setStrokeWidth(4);  //线的宽度

        canvas.drawPoint(x, y, paint2);
        bitmapToPath(bitmap, "img_next_find");
        first = false;
        return new android.graphics.Point(x, y);
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

        if ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < 15) {
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
        // 取中两位
        int blue2 = 61;// 取低两位


        if ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < 45) {
            return true;
        }


        return false;

    }

    public static boolean isMes(int clr) {
        for (int i = 0; i < boderColor.size(); i++) {
            if (isLike(clr, boderColor.get(i)))
                return true;
        }

        return false;
    }

    public static boolean isShadow(int clr) {
        int all = Color.blue(clr) + Color.green(clr) + Color.red(clr);
        if (Math.abs(all - 477) < 15) {
            return true;
        }
        return false;
    }

    public static boolean isLike(int clr, int clr2) {
        int red = Color.red(clr); // 取高两位
        int green = Color.green(clr);
        // 取中两位
        int blue = Color.blue(clr);// 取低两位


        int red2 = Color.red(clr2); // 取高两位
        int green2 = Color.green(clr2);
        ; // 取中两位
        int blue2 = Color.blue(clr2);// 取低两位


        if ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < 40) {
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


    public void delete(String path) {
        File f = new File(path);
        if (f.isDirectory()) {//如果是目录，先递归删除
            String[] list = f.list();
            for (int i = 0; i < list.length; i++) {
                delete(path + "//" + list[i]);//先删除目录下的文件
            }
        }
        f.delete();
    }


    public static boolean findEst(Bitmap bitmap, int inColor, int x, int y) {
        for (int i = 0; i < 5; i++) {
            if (isLike(bitmap.getPixel(x, y + i + 1), inColor)) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<Integer> borderColor() {
        ArrayList<Integer> boderColor = new ArrayList<Integer>();
        Bitmap bitmap = BitmapFactory.decodeFile(me);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int firstColor = bitmap.getPixel(1, 1);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int clr = bitmap.getPixel(j, i);
                if (firstColor != clr) {
                    boderColor.add(clr);
                    break;
                }
            }
        }
        return boderColor;
    }


}
