package com.yorhp.justjump.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.yorhp.justjump.app.MyApplication;

import java.util.ArrayList;

import static com.yorhp.justjump.opencv.ImageRecognition.delete;
import static com.yorhp.justjump.service.MyService.bitmapToPath;
import static com.yorhp.justjump.service.MyService.execShellCmd;

/**
 * Created by Tyhj on 2018/3/1.
 */

public class Test {

    public static boolean ok = false;
    public static String img_clear = MyApplication.rootDir + "/img_clear.png";
    public static ArrayList<String> imgs_find = new ArrayList<>();

    public static android.graphics.Point erroTest() {

        String filepath = MyApplication.rootDir + "/" + "next_erro" + ".png";

        String img_find = "x_find" + System.currentTimeMillis();

        Bitmap workingBitmap = BitmapFactory.decodeFile(filepath);

        Bitmap bitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(bitmap);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(2);  //线的宽度


        Paint paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.STROKE);//不填充
        paint2.setStrokeWidth(2);  //线的宽度

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


        for (int y = 0; y < height; y++) {
            boolean out = false;
            boolean in = false;

            for (int x = 0; x < width; x++) {//同一条线里面

                int clr = bitmap.getPixel(x, y);

                if (backgrundColor == 0)//保存背景色
                    backgrundColor = clr;

                //进入
                if (topPoint == null && !eque(clr, backgrundColor, 10)) {//--------------------第一次进入
                    if(x>1){
                        backgrundColor=bitmap.getPixel(x-1,y);
                    }
                    tabColor = bitmap.getPixel(x, y+2);
                    topPoint = new android.graphics.Point(0, 0);
                    topPoint.x = x;
                    in = true;
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
                    } else if (!out && in && equeOutbgColor(backgrundColor, x, y, bitmap) && !eque(clr, 107, 156, 248)) {//---------出来了,防止魔方出错
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

                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);

                        int centerX = (leftPoint.x + rightPoint.x) / 2;
                        int centerY = (leftPoint.y + rightPoint.y) / 2;

                        if (isWall) {
                            centerX = topPoint.x;
                            centerY = leftPoint.y;
                        }

                        canvas.drawPoint(centerX, centerY, paint2);
                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                } else {
                    if (rightFind) {
                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
                        int centerX = topPoint.x;
                        int centerY = rightPoint.y;
                        canvas.drawPoint(centerX, centerY, paint2);
                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                }

            }
            y = y + 1;
        }

        if (pureColor) {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = (leftPoint.x + rightPoint.x) / 2;
            int centerY = (leftPoint.y + rightPoint.y) / 2;
            canvas.drawPoint(centerX, centerY, paint2);
            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        } else {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = topPoint.x;
            int centerY = rightPoint.y;
            canvas.drawPoint(centerX, centerY, paint2);
            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        }
    }

    public static android.graphics.Point erroTest2() {
        String img_clear = MyApplication.rootDir + "/" + "screenshots_erro" + ".png";
        Bitmap bitmapx = BitmapFactory.decodeFile(img_clear);
        Bitmap workingBitmap = bitmapx.createBitmap(bitmapx, 0, (int) (bitmapx.getHeight() * 0.3125), bitmapx.getWidth(), (int) (bitmapx.getHeight() * 0.2713));

        String img_find = "x_find" + System.currentTimeMillis();

        String filepath = MyApplication.rootDir + "/" + img_find + ".png";
        imgs_find.add(filepath);

        if (!ok) {
            execShellCmd("screencap -p " + MyApplication.rootDir + "/screenshots" + System.currentTimeMillis() + ".png");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (imgs_find.size() >= 3) {
                imgs_find.remove(0);
            }

            return new android.graphics.Point(0, 0);
        } else {
            if (imgs_find.size() >= 3) {
                delete(imgs_find.get(0));
                imgs_find.remove(0);
            }
        }


        Bitmap bitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmap);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(2);  //线的宽度


        Paint paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.STROKE);//不填充
        paint2.setStrokeWidth(2);  //线的宽度

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


        for (int y = 0; y < height; y++) {
            boolean out = false;
            boolean in = false;

            for (int x = 0; x < width; x++) {//同一条线里面

                int clr = bitmap.getPixel(x, y);

                if (backgrundColor == 0)//保存背景色
                    backgrundColor = clr;

                //进入
                if (topPoint == null && !eque(clr, backgrundColor, 10)) {//--------------------第一次进入
                    tabColor = clr;
                    topPoint = new android.graphics.Point(0, 0);
                    topPoint.x = x;
                    in = true;
                    System.out.println("第一次进入" + x + "，" + y);
                    if (eque(bitmap.getPixel(x, y + 1), backgrundColor, 10)) {
                        //System.out.println("第一次进入马上出去了" + x);
                        out = true;
                    }
                    pureColor = isPure(bitmap, clr, x, y);
                } else if (!in && topPoint != null && equeTabColor(clr, tabColor)) {//-------------------进入tab
                    in = true;
                    if (pureColor && !leftFind) {//----------------------------------------------------------纯色
                        if (leftPoint.x == 0 || leftPoint.x > x) {//----------进入的坐标在减小
                            leftPoint.x = x;
                            leftPoint.y = y;
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
                    } else if (!out && in && equeOutbgColor(backgrundColor, x, y, bitmap) && !eque(clr, 107, 156, 248)) {//---------出来了,防止魔方出错
                        out = true;
                        if (rightPoint.x < x) {//出去的坐标在增大
                            rightPoint.x = x;
                            rightPoint.y = y;
                        } else if (eque(bitmap.getPixel(x + 1, y + 3), backgrundColor, 10)&&eque(bitmap.getPixel(x + 1, y + 2), backgrundColor, 10)&&eque(bitmap.getPixel(x + 1, y + 4), backgrundColor, 10)) {//-----------------------------------------找到了 rightPoint
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

                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);

                        int centerX = (leftPoint.x + rightPoint.x) / 2;
                        int centerY = (leftPoint.y + rightPoint.y) / 2;

                        if (isWall) {
                            centerX = topPoint.x;
                            centerY = leftPoint.y;
                        }

                        canvas.drawPoint(centerX, centerY, paint2);
                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                } else {
                    if (rightFind) {
                        canvas.drawPoint(topPoint.x, topPoint.y, paint);
                        canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
                        int centerX = topPoint.x;
                        int centerY = rightPoint.y;
                        canvas.drawPoint(centerX, centerY, paint2);
                        bitmapToPath(bitmap, img_find);
                        return new android.graphics.Point(centerX, centerY);
                    }
                }

            }
            y = y + 1;
        }

        if (pureColor) {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(leftPoint.x, leftPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = (leftPoint.x + rightPoint.x) / 2;
            int centerY = (leftPoint.y + rightPoint.y) / 2;
            canvas.drawPoint(centerX, centerY, paint2);
            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        } else {
            canvas.drawPoint(topPoint.x, topPoint.y, paint);
            canvas.drawPoint(rightPoint.x, rightPoint.y, paint);
            int centerX = topPoint.x;
            int centerY = rightPoint.y;
            canvas.drawPoint(centerX, centerY, paint2);
            bitmapToPath(bitmap, img_find);
            return new android.graphics.Point(centerX, centerY);
        }
    }


    //判断出去时候是否真的是出去了
    private static boolean equeOutbgColor(int backgrundColor, int x, int y, Bitmap bitmap) {
        int width = 6;
        for (int i = 0; i < width; i++) {
            if (x + i >= bitmap.getWidth() || !eque(backgrundColor, bitmap.getPixel(x + i, y), 15))
                return false;
        }
        return true;
    }

    //判读是不是纯色
    private static boolean isPure(Bitmap bitmap, int clr, int x, int y) {
        int height = 8;
        int width = 8;
        for (int i = 1; i < width; i++) {
            if (!equeTabColor(bitmap.getPixel(x + i, y + height),clr)|| !equeTabColor(bitmap.getPixel(x - i, y + height),clr)) {
                //System.out.println("第一个："+Color.red(clr)+"，"+Color.green(clr)+"，"+Color.blue(clr)+"x+i："+(x+i)+"y+height："+(y + height));
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

        if ((Math.abs(red - red2) < error && Math.abs(green - green2) < error && Math.abs(blue - blue2) < error)&&((Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue - blue2))<error*2.4)) {
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

        if ((Math.abs(red - red2) < 3 && Math.abs(green - green2) < 3 && Math.abs(blue - blue2) < 3)) {
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
}
