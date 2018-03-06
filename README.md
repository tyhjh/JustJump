原文链接：https://www.zybuluo.com/Tyhj/note/1041200

最近微信跳一跳很火，跳一跳外挂在网上也很火，自己是搞APP开发的，不做一个跳一跳的外挂都不好意思说是程序员了。

![C45CFBAB8F6B0AEE91CE30C767F515BD.jpg](http://upload-images.jianshu.io/upload_images/4906791-a6e103c656c28e0b.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/240)


### 实现要点
其实就是两点，一是判断距离，二是自动点击，至于距离和点击时间的关系，其实就是距离（px）乘以1.4毫秒（在1080P上，其他差不多）。

### 距离判断
判断距离两种也有几种方法，第一种自己测量，第二种图象识别，还有第三种，利用Android的悬浮框来测量。

### 自动点击
就算我们知道了点击的时间，但是我们手动来控制时间也是不可能的，所以需要自动点击。三种方法，一种是用连接电脑，adb命令来进行控制点击时间的长短；第二种，自动化的同学可以控制机械进行点击；第三种，Android程序来执行adb命令进行模拟点击，但是该方法需要root权限。

### 实现方法
网上的外挂大概是这几种：
1.图象识别+外部adb命令，adb命令可以是各种语言写的脚本，并且需要手机与电脑连接
2.图象识别+机械手臂点击，需要机械手臂
3.自己测量+外部adb命令
4.自己测量+机械手臂
5.通过API直接修改数据，这方法太容易挂掉（已经挂掉）

都需要借助外部工具，而且我再去模仿别人就没有什么意思了，那我就做一个不需要外部工具的

##### 半自动版本
首先我还没有看过图象识别，而且看起来java写图象识别也不是很容易，手动测量就更不用讲了，所以我采用其他人忽略的方法，用两个悬浮框来测量如下图，用Android开启两个悬浮框（还故意用和游戏相同的图片），通过移动它们的位置来获取跳跃的距离，
```java
//在service里面开启悬浮框核心代码
windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 设置Window Type，这个设置可以保证悬浮框一直不消失，并且在最上层
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // 设置悬浮框不可触摸
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|FLAG_LAYOUT_INSET_DECOR;
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params.format = PixelFormat.RGBA_8888;
        // 设置悬浮框的宽高
        params.width = 350;
        params.height = 350;
        params.gravity = Gravity.LEFT;
        params.x = 200;
        params.y = 000;
 btnView1 = new ImageView(getApplicationContext());
 windowManager.addView(btnView1, params);
 

```
然后点击第三个悬浮框（五星）来执行adb命令。
```java
//执行adb命令代码
int time= (int) (distance*1.4);
String cmd="input touchscreen swipe 170 187 170 187 "+time;
// 申请获取root权限，这一步很重要，不然会没有作用
Process process = Runtime.getRuntime().exec("su");
// 获取输出流
OutputStream outputStream = process.getOutputStream();
DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
dataOutputStream.writeBytes(cmd);
dataOutputStream.flush();
dataOutputStream.close();
outputStream.close();

```


优点就是想跳哪里跳哪里，想跳多少分跳多少分，还可以故意跳偏躲过反外挂机制；缺点就是需要自己动手，花时间，而且执行adb命令会有一点点的延迟，其实外部adb命令也是有延迟的，避免不了

![image](http://upload-images.jianshu.io/upload_images/4906791-c41e25bd71fe9d2a.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/140)

#####全自动版本
第一个版本做出来后我自己去跳了几次，几千分没问题，但是要动手太麻烦，那就只能用图象识别了。图象识别我看了一下OpenCV，有Android版本要自己编译一下，还要用到NDK编程，也有Java版本可以直接拿来用，就是用到其中的图象匹配，有两种方法，一种简单点是模板匹配，一种复杂一点是特征匹配。就是一张截图用模板来匹配找到模板在截图中的位置。根据跳一跳程序的特点，我们只需要截取图象的一部分而已。
```java
//截图
execShellCmd("screencap -p " + MyApplication.rootDir + "/screenshots.png");
Bitmap bitmap = BitmapFactory.decodeFile(screenPath);
```


```java
//剪裁出下一个踏板所在的图
Bitmap bitmap_next = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * 0.3125), bitmap.getWidth(), (int) (bitmap.getHeight() * 0.2713));
```

![image](http://upload-images.jianshu.io/upload_images/4906791-b5be88bf23670ede.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/240)


```java
//剪裁出跳的棋子所在的图
Bitmap bitmap_me = bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight() * 0.4166), bitmap.getWidth(), (int) (bitmap.getHeight() * 0.2304));
```

![image](http://upload-images.jianshu.io/upload_images/4906791-f0040f1dddeaa7ea.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/240)


我试了一下特征匹配，Android程序会崩，我查了一下原因可能是运算量比较大，反正搞不定，我继续试了模板匹配，还行，如果是一个取出来的部分图片去匹配原图那还是可以识别成功，但是有些图片是跳一跳那个跳的棋子或者上一个踏板遮挡了下一个踏板，那识别会有一点小问题，而且模板也要添加许多，每个模板匹配一下，然后取匹配程度最大的来计算距离（当匹配度大于某一个值直接取值），模板添加越多，跳的越准确，用的时间也越长（30多个模板最长用时大概4、5秒）。

```java
//模板匹配实现代码
Mat template = Highgui.imread(templateFilePath1, Highgui.CV_LOAD_IMAGE_COLOR);
Mat source = Highgui.imread(originalFilePath, Highgui.CV_LOAD_IMAGE_COLOR);
//创建于原图相同的大小，储存匹配度
Mat result = Mat.zeros(source.rows() - template.rows() + 1, source.cols() - template.cols() + 1, CvType.CV_8UC1);
//调用模板匹配方法
Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);
//获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
Core.MinMaxLocResult mlr = Core.minMaxLoc(result);
System.out.println("相似度：" + mlr.maxVal);
Point matchLoc = mlr.maxLoc;
//在原图上的对应模板可能位置画一个绿色矩形
Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
        
```

反正就点击开始，手机仍在那里，哪个跳不准了，就把哪个图象加到匹配组里面去。我添加了30多个模板以后可以跳到500多分。优点就是自动，缺点就是识别不准，用时长。

#####完美版本
把两个版本都给其他小伙伴看，会编程的还好觉得还行，不会编程的觉得我这个太low了，觉得还是连接电脑，自己跳那个炫酷一点。所以最近买了一本OpenCV的资料好好看一下，JIN/NDK自己也搞过，准备先看一下书，想想办法，过几天重新搞一下

##### 新方法
其实图象识别可能有点大材小用了，我最近不小心看了另一个跳一跳外挂的代码，发现用的是读取截图的RGB颜色来对比定位的方法，原理其实很简单，我之前用java做图象也比较少，所以自己没有想到这种方法，简单的实现一个还行吧，做到十分精确的话也是看自己的算法了。

先来看一下简单的能不能搞定，首先拿到截图的大小，按像素一个一个去读取RGB颜色。先看读取到的信息是怎样的，
```java
//j和i就是像素位置
int clr = bitmap.getPixel(j,i);

//三个参数用来对比颜色
int red = Color.red(clr); 
int green = Color.green(clr); 
int blue =Color.blue(clr);

```
###### 找棋子
棋子其实很好找，棋子颜色就一种，而且还不会被其他东西遮挡，就一直遍历颜色，找到棋子的底线在加个20左右像素就是棋子的中心点了。但是由于我之前用的那个图象识别找棋子也特别准，我就还是用那个了。



###### 第一个点
看上面的截图，上部分都是背景色，也就是说，我们可以先拿到背景色（第一次读取），读取到踏板的时候，颜色会和之前的大不一样（以为之前的背景色读取出来也有可能有一点点小差别），这样我们就会读取到最上面的点的X坐标，如果是个圆的踏板的话也有可能是一条线，取中间点就可以了。

###### 第二个点
然后踏板最右边的点也很好取，我们读取颜色的时候是每一行从左到右读取下来的，我们在每一行的时候，如果颜色和背景色不一样，那就是进入踏板了，那下次又和背景颜色一样了，那就是出去了，我们可以拿到出去的点，对比每次出去的点的X坐标，当出去的点X坐标第一次不再增大的时候，那就是我们要的最右边的点了。


###### 第二个点
其实这两个点已经够了，第一个点的横坐标和第二个点的纵坐标可以认为是中心点坐标了，当然有些时候可能不是，但是也差不了多少。找出最左边的点的位置，这样更准一点，方法和第二个一样，但是有时候前一个踏板和后一个隔得太近这两个点容易被挡住，其实很简单不难，我们需要保存第一个点的颜色，判断当前颜色是不是和第一次一样，也可以判断是不是棋子的颜色。

![img_next_find(5).png](http://upload-images.jianshu.io/upload_images/4906791-b14356d24e02a830.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/340)


方法肯定是可以的，晚上试了一次，不知道什么原因，灰色和绿色的图片不行，第一点都找不到，还不清楚哪里出了问题，等一下再搞一下。


##### 注意问题
问题解决了，用rgb颜色对比来进行识别，注意颜色有时候有点细微的差别（尤其是背景色有差别），所以我们判断颜色是否相同的时候可以设置一个范围，当两个像素颜色的R、G、B三个颜色分别相减的**绝对值**再求和小于15大概可以认为是同一个颜色。

反正还有其他一些问题需要自己去解决，才能够跳更多的分，我先在这个已经可以超过100k分了，其实都不难，关键看自己解决方法。

![tyhj_100k.gif](http://upload-images.jianshu.io/upload_images/4906791-218698ce03fa27c8.gif?imageMogr2/auto-orient/strip)


项目地址（三个版本）：https://github.com/tyhjh/justJump.git

























