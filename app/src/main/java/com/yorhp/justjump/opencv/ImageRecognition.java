package com.yorhp.justjump.opencv;

import com.yorhp.justjump.app.MyApplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class ImageRecognition {


    public int getDistence(String originmePath, String mePath, String originPath, String templateFilePath[], int heightMe, int height) {


        File file = new File(MyApplication.rootDir + "matching/");
        delete(MyApplication.rootDir + "matching/");
        file.mkdirs();


        LikeResult result1 = matchImageMe(originmePath, mePath);

        LikeResult result2 = matchImage(originPath, templateFilePath);


        android.graphics.Point point1 = result1.getPoint();

        android.graphics.Point point2 = result2.getPoint();

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
        int y = (int) (matchLoc.y + template.height() * 0.88);


        android.graphics.Point point = new android.graphics.Point();
        point.set(x, y);

        Highgui.imwrite(MyApplication.rootDir + "matching/" + "/原图中的匹配图" + ".jpg", source);

        LikeResult likeResult = new LikeResult();
        likeResult.setPoint(point);
        likeResult.setLikeLevel(mlr.maxVal);

        return likeResult;
    }


    public LikeResult matchImage(String originalFilePath, String templateFilePath1) {

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
        int y = (int) (matchLoc.y + template.height() / 2);


        android.graphics.Point point = new android.graphics.Point();
        point.set(x, y);

        if (mlr.maxVal > 0.7)
            Highgui.imwrite(MyApplication.rootDir + "matching/" + "/原图中的匹配图" + mlr.maxVal + ".jpg", source);

        LikeResult likeResult = new LikeResult();
        likeResult.setPoint(point);
        likeResult.setLikeLevel(mlr.maxVal);

        return likeResult;
    }


    public LikeResult matchImage(String originalFilePath, String templateFilePath[]) {

        double likeLevel = 0;
        int position = 0;
        LikeResult likeResult1 = new LikeResult();
        likeResult1.setLikeLevel(0);

        while (likeLevel < 0.86 && position < templateFilePath.length) {
            LikeResult likeResult = matchImage(originalFilePath, templateFilePath[position]);
            likeLevel = likeResult.getLikeLevel();
            position++;
            if (likeLevel > likeResult1.getLikeLevel()) {
                likeResult1 = likeResult;
            }
        }
        System.out.println("最终相似度为：" + likeResult1.getLikeLevel());
        if(likeResult1.getLikeLevel()<0.5)
            return null;
        return likeResult1;
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
}
