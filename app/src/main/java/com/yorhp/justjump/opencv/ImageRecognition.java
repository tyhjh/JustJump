package com.yorhp.justjump.opencv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.yorhp.justjump.app.MyApplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

import static com.yorhp.justjump.service.MyService.bitmapToPath;
import static com.yorhp.justjump.service.MyService.execShellCmd;
import static com.yorhp.justjump.service.MyService.mePath;

public class ImageRecognition {

    public static int meX1, meX2, meY1, meY2;
    public static boolean ok = false;
    public static String img_clear = MyApplication.rootDir + "/img_clear.png";
    public static ArrayList<String> imgs_find = new ArrayList<>();
    public static ArrayList<String> nexts_find = new ArrayList<>();

    public static double shotHeight = 0.23;
    public static double shotStart = 0.3;

    public static double shotHeightMe = 0.29;//截图比例
    public static double shotStartMe = 0.4;//开始截图的位置

    public static int heightMe = 0;
    public static int height = 0;

    public int getDistence(Bitmap bitmap) {

        if (heightMe == 0 || height == 0) {
            heightMe = (int) (bitmap.getHeight() * shotStartMe);
            height = (int) (bitmap.getHeight() * shotStart);
        }


        Bitmap bitmap_me = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * shotStartMe), bitmap.getWidth(), (int) (bitmap.getHeight() * shotHeightMe));
        File file_me = bitmapToPath(bitmap_me, "img_me");
        LikeResult result1 = matchImageMe(file_me.getPath(), mePath);

        android.graphics.Point point1 = result1.getPoint();

        /*Bitmap workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(workingBitmap);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(bitmap.getPixel(500, height + 20));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(meX1, meY1 + heightMe - 10, meX2, meY2 + heightMe, paint);
        bitmapToPath(workingBitmap, "img_clear");*/

        android.graphics.Point point2 = getPoint(bitmap);

        bitmap.recycle();
        int distence = (int) (Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y + heightMe - point2.y - height) * (point1.y + heightMe - point2.y - height)));

        return distence;
    }


    public static LikeResult matchImageMe(String originalFilePath, String templateFilePath1) {

        long time = System.currentTimeMillis();

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
        //Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
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

        //System.out.println("识别所用时间为"+(System.currentTimeMillis()-time));

        return likeResult;
    }


    public static android.graphics.Point getPoint(Bitmap bitmapx) {
        Bitmap workingBitmap = bitmapx.createBitmap(bitmapx, 0, (int) (bitmapx.getHeight() * shotStart), bitmapx.getWidth(), (int) (bitmapx.getHeight() * shotHeight));
        Canvas canvas2 = new Canvas(workingBitmap);
        Paint paint2 = new Paint();
        paint2.setColor(workingBitmap.getPixel(0, 0));
        paint2.setStyle(Paint.Style.FILL);

        int top = meY1 + heightMe - 10 - height;
        if (top < 0) {
            top = 0;
        }

        int bottom = meY2 + heightMe - height;
        if (bottom > workingBitmap.getHeight()) {
            bottom = workingBitmap.getHeight();
        }


        canvas2.drawRect(meX1, top, meX2, bottom, paint2);

        String next_find = "check/next_" + System.currentTimeMillis();
        bitmapToPath(workingBitmap, next_find);

        String next_find_path = MyApplication.rootDir + next_find + ".png";
        String img_find = "check/x_find" + System.currentTimeMillis();

        String filepath = MyApplication.rootDir + img_find + ".png";
        imgs_find.add(filepath);
        nexts_find.add(next_find_path);

        if (!ok) {
            execShellCmd("screencap -p " + MyApplication.rootDir + "grade/screenshots" + System.currentTimeMillis() + ".png");

            if (imgs_find.size() >= 4) {
                imgs_find.clear();
            }
            if (nexts_find.size() >= 4) {
                nexts_find.clear();
            }


            return new android.graphics.Point(0, 0);
        } else {
            if (imgs_find.size() >= 4) {
                delete(imgs_find.get(0));
                imgs_find.remove(0);
            }

            if (nexts_find.size() >= 4) {
                delete(nexts_find.get(0));
                nexts_find.remove(0);
            }

        }


        Bitmap bitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(2);  //线的宽度


        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //System.out.println("宽度为：" + bitmap.getWidth());

        //保存第一次遇到的颜色
        int backgrundColor = 0;

        //第一次进入tab的颜色
        int tabColor = 0;

        //顶点
        android.graphics.Point topPoint = null;

        //左边的点
        android.graphics.Point leftPoint = new android.graphics.Point(0, 0);

        //右边的点
        android.graphics.Point rightPoint = new android.graphics.Point(0, 0);

        //是不是纯色
        boolean pureColor = true;

        boolean isWall = false;

        //找到了左边的
        boolean leftFind = false;

        //找到了右边的
        boolean rightFind = false;


        long time = System.currentTimeMillis();
        for (int y = 0; y < height; y++) {
            boolean out = false;
            boolean in = false;

            for (int x = 0; x < width; x++) {//同一条线里面

                int clr = bitmap.getPixel(x, y);

                if (backgrundColor == 0)//保存背景色
                    backgrundColor = clr;

                //进入
                if (topPoint == null && !eque(clr, backgrundColor, 10)) {//--------------------第一次进入
                    tabColor = bitmap.getPixel(x, y + 2);


                    if (x > 1) {
                        backgrundColor = bitmap.getPixel(x - 1, y);
                    }
                    topPoint = new android.graphics.Point(0, 0);
                    topPoint.x = x;
                    topPoint.y = 0;
                    in = true;
                    System.out.println("第一次进入所用时间为" + (System.currentTimeMillis() - time));
                    System.out.println("第一次进入" + x + "，" + y);
                    if (eque(bitmap.getPixel(x, y + 1), backgrundColor, 10)) {
                        //System.out.println("第一次进入马上出去了" + x);
                        out = true;
                    }
                    pureColor = isPure(bitmap, clr, x, y);

                    if (pureColor)
                        System.out.println("是纯色");
                    else
                        System.out.println("不是纯色");


                } else if (!in && topPoint != null && equeTabColor(clr, tabColor)) {//-------------------进入tab
                    in = true;
                    if (pureColor && !leftFind) {//----------------------------------------------------------纯色
                        if (leftPoint.x == 0 || leftPoint.x > x) {//----------进入的坐标在减小
                            leftPoint.x = x;
                            leftPoint.y = y;
                            if (leftPoint.x < 2) {
                                pureColor = false;
                            }
                        } else if (!equeTabColor(bitmap.getPixel(x, y + 2), tabColor) && !leftFind) {//-------------------------------------找到了 leftPoint
                            leftPoint.x = x;
                            leftPoint.y = y;
                            leftFind = true;
                            System.out.println("纯色，leftPoint：" + x + "，" + y);
                        }
                    } else if (!pureColor) {//-------------------------------------------------------------------非纯色，不用 leftPoint
                        //System.out.println("非纯色，不用 leftPoint");
                    }
                }


                //出来
                if (!out && in && !rightFind) {
                    if (topPoint.y == 0 && eque(clr, backgrundColor, 10)) {//--------------------------第一次进去后出来
                        //System.out.println("第一次进去后出来了");
                        topPoint.x = (topPoint.x + x) / 2;
                        topPoint.y = y;
                        out = true;
                    } else if (!out && in && !equeTabColor(clr, tabColor) && equeOutbgColor(backgrundColor, x, y, bitmap) && !eque(clr, 107, 156, 248)) {//---------出来了,防止魔方出错
                        out = true;
                        if (rightPoint.x < x) {//出去的坐标在增大
                            rightPoint.x = x;
                            rightPoint.y = y;
                        } else if (y < bitmap.getHeight() - 5 && eque(bitmap.getPixel(x + 1, y + 3), backgrundColor, 14) && eque(bitmap.getPixel(x + 1, y + 2), backgrundColor, 14) && eque(bitmap.getPixel(x + 1, y + 4), backgrundColor, 14)) {//-----------------------------------------找到了 rightPoint
                            rightPoint.y = y;
                            rightPoint.x = x;
                            rightFind = true;
                            System.out.println("找到了 rightPoint：" + rightPoint.x + "，" + rightPoint.y);
                        }
                    } else if (x == width - 1) {//-------------------------------------------------碰壁了
                        rightFind = true;
                        isWall = true;
                        rightPoint = new android.graphics.Point(x, y);
                    }
                }
                //开始判断是否结束
                if (pureColor) {
                    if (leftFind && rightFind) {

                        int centerX = (leftPoint.x + rightPoint.x) / 2;
                        int centerY = (leftPoint.y + rightPoint.y) / 2;

                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);


                        if (isWall) {
                            centerX = topPoint.x;
                            centerY = leftPoint.y;
                        }
                        //paint.setColor(Color.BLACK);
                        //canvas.drawPoint(centerX, centerY, paint);

                        android.graphics.Point center = getPointCenter(centerX, centerY, tabColor, topPoint.y, bitmap);
                        centerX = center.x;
                        centerY = center.y;
                        paint.setColor(Color.MAGENTA);
                        canvas.drawPoint(centerX, centerY, paint);

                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                } else {
                    if (rightFind) {
                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
                        int centerX = topPoint.x;
                        int centerY = rightPoint.y;
                        //paint.setColor(Color.BLACK);
                        //canvas.drawPoint(centerX, centerY, paint);

                        android.graphics.Point center = getPointCenter(centerX, centerY, tabColor, topPoint.y, bitmap);
                        centerX = center.x;
                        centerY = center.y;
                        paint.setColor(Color.MAGENTA);
                        canvas.drawPoint(centerX, centerY, paint);

                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                }
            }
        }

        if (pureColor) {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = (leftPoint.x + rightPoint.x) / 2;
            int centerY = (leftPoint.y + rightPoint.y) / 2;
            //paint.setColor(Color.BLACK);
            //canvas.drawPoint(centerX, centerY, paint);

            android.graphics.Point center = getPointCenter(centerX, centerY, tabColor, topPoint.y, bitmap);
            centerX = center.x;
            centerY = center.y;
            paint.setColor(Color.MAGENTA);
            canvas.drawPoint(centerX, centerY, paint);

            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        } else {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = topPoint.x;
            int centerY = rightPoint.y;
            //paint.setColor(Color.BLACK);
            //canvas.drawPoint(centerX, centerY, paint);

            android.graphics.Point center = getPointCenter(centerX, centerY, tabColor, topPoint.y, bitmap);
            centerX = center.x;
            centerY = center.y;
            paint.setColor(Color.MAGENTA);
            canvas.drawPoint(centerX, centerY, paint);

            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        }
    }


    //判断白点，找中点
    private static android.graphics.Point getPointCenter(int centerX, int centerY, int tabColor, int topPointY, Bitmap bitmap) {
        int x = centerX, y = centerY;
        int topY = 0, bottomY = 0;
        int leftX = 0, rightX = 0;


        if (!eque(tabColor, 245, 245, 245)) {//有白点
            if (eque(bitmap.getPixel(x, y), 245, 245, 245)) {//坐标计算小错
                for (int i = centerY; i > centerY - 24; i--) {//找到topY
                    if (!eque(bitmap.getPixel(x, i), 245, 245, 245)) {
                        topY = i;
                        break;
                    }
                }

                for (int i = centerY; i < centerY + 24; i++) {//找到bottomY
                    if (!eque(bitmap.getPixel(x, i), 245, 245, 245)) {
                        bottomY = i;
                        break;
                    }
                }

                if (bottomY != 0 && topY != 0 && bottomY - topY < 26
                        && eque(bitmap.getPixel(x, (bottomY + topY) / 2), 245, 245, 245)) {//找到y
                    y = (bottomY + topY) / 2;
                }


                for (int i = centerX; i > centerX - 41; i--) {//找到leftX
                    if (!eque(bitmap.getPixel(i, y), 245, 245, 245)) {
                        leftX = i;
                        break;
                    }
                }


                for (int i = centerX; i < centerX + 41; i++) {//找到rightX
                    if (!eque(bitmap.getPixel(i, y), 245, 245, 245)) {
                        rightX = i;
                        break;
                    }
                }

                if (rightX != 0 && leftX != 0 && rightX - leftX < 41
                        && eque(bitmap.getPixel((rightX + leftX) / 2, y), 245, 245, 245)) {//找到x
                    x = (leftX + rightX) / 2;
                }

            } else {//坐标计算错误

                for (int i = centerY; i > topPointY; i--) {
                    if (bottomY == 0 && eque(bitmap.getPixel(centerX, i), 245, 245, 245)) {
                        bottomY = i;
                        i--;
                    }

                    if (bottomY != 0 && !eque(bitmap.getPixel(centerX, i), 245, 245, 245)) {
                        topY = i;
                        break;
                    }
                }

                if (bottomY - topY < 26 && eque(bitmap.getPixel(centerX, (bottomY + topY) / 2), 245, 245, 245)) {
                    y = (bottomY + topY) / 2;
                    System.out.println("Y出错了，并且纠正了");
                }


                for (int i = centerX; i > centerX - 41; i--) {//找到leftX
                    if (!eque(bitmap.getPixel(i, y), 245, 245, 245)) {
                        leftX = i;
                        break;
                    }
                }


                for (int i = centerX; i < centerX + 41; i++) {//找到rightX
                    if (!eque(bitmap.getPixel(i, y), 245, 245, 245)) {
                        rightX = i;
                        break;
                    }
                }

                if (rightX != 0 && leftX != 0 && rightX - leftX < 41
                        && eque(bitmap.getPixel((rightX + leftX) / 2, y), 245, 245, 245)) {//找到x
                    x = (leftX + rightX) / 2;
                }


            }
        }

        return new android.graphics.Point(x, y);
    }

    //判断出去时候是否真的是出去了
    private static boolean equeOutbgColor(int backgrundColor, int x, int y, Bitmap bitmap) {
        int width = 6;
        for (int i = 0; i < width; i++) {
            if (x + i >= bitmap.getWidth() || !eque(backgrundColor, bitmap.getPixel(x + i, y), 10))
                return false;
        }
        return true;
    }

    //判读是不是纯色
    private static boolean isPure(Bitmap bitmap, int clr, int x, int y) {
        int height = 8;
        int width = 6;
        for (int i = 1; i < width; i++) {
            if (bitmap.getPixel(x + i, y + height) != clr || bitmap.getPixel(x - i, y + height) != clr) {
                return false;
            }
        }
        return true;
    }


    public static boolean eque(int clr, int clr2, int error) {
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

        if ((Math.abs(red - red2) < error && Math.abs(green - green2) < error && Math.abs(blue - blue2) < error) && ((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2)) < error * 2.4)) {
            return true;
        }
        return false;
    }


    public static boolean equeTabColor(int clr, int clr2) {
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

        if ((Math.abs(red - red2) < 5 && Math.abs(green - green2) < 5 && Math.abs(blue - blue2) < 5)) {
            return true;
        }


        return false;

    }

    public static boolean eque(int clr, int red2, int green2, int blue2) {
        int red = Color.red(clr); // 取高两位
        int green = Color.green(clr);
        ; // 取中两位
        int blue = Color.blue(clr);// 取低两位


        if (red == red2 && green == green2 && blue == blue2) {
            return true;
        }

        if (Math.abs(red - red2) < 4 && Math.abs(green - green2) < 4 && Math.abs(blue - blue2) < 4) {
            return true;
        }


        return false;

    }


    public static class LikeResult {
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
