package com.ccb.mp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Vibrator;
import android.widget.Toast;
import com.knowyou.ky_sdk.FilesService;
import com.knowyou.ky_sdk.imagecache.ImageGetFromHttp;
import com.knowyou.ky_sdk.utils.KyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 工具类
 */
public class Utils {

    private static Logger logger = LoggerFactory
            .getLogger(Utils.class); // 日志对象

    /**
     * 清空APP数据 2015/4/8 11:43
     */
    public static void clearApp(Context context) {
        logger.debug("Clear app data.");

        // 清空本地紧急联系人
//        LocalContact localContact = new LocalContact();
//        localContact.delContact();

//        Const.APP_LOGIN_SID = ""; // 登录SID
//        Const.APP_LOGIN_NAME = ""; // 登录账号
//        Const.IS_LOGIN = false; // 是否已经登录
//        Const.IS_START = false; // 是否已经开启
//        Const.isGetContact = false; // 是否已经请求过，获取紧急联系人
//        Const.lstData.clear(); // 紧急联系人
//        App.getInstance().clearLockPatternUtils(); // 清除手势
//
//        SharedPreferences preferences = context.getSharedPreferences(Const.USER_INFO, PreferenceActivity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.remove(Const.USERNAME);
//        editor.remove(Const.PASSWORD);
//        editor.remove(Const.DATE);
//        editor.remove(Const.SID);
//        editor.commit();
    }

    /**
     * 判断字符串是否为空 2015/1/17 11:01
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        logger.debug("Is empty.");
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    /**
     * 发送url到后台  2014/10/23 15:44
     * @param url   地址
     * @param lstKey   参数key
     * @param lstValue   参数值
     * @return
     */
    public static String sendUrl(String url, List<String> lstKey, List<String> lstValue) throws IOException {
        logger.debug("Send url is {}.", url);
        if (lstKey != null && lstKey.size() > 0
                && lstValue != null && lstValue.size() > 0
                && lstKey.size() == lstValue.size()) {

            url += lstKey.get(0) + "=" + lstValue.get(0);
            for (int i = 1; i < lstKey.size(); i++) {
                url += "&" + lstKey.get(i) + "=" + lstValue.get(i);
            }

            logger.debug("URL={}.", url);
        }

        byte[] json = FilesService.getBytes(url); // 获取json数据

        return new String(json);
    }

    /**
     * 获取图片 2014/10/31 10:22
     * @param url 网络地址
     * @return 位图
     */
    public static Bitmap getBitmap(String url, Context context) {
        logger.debug("Get bitmap.Url is {}.", url);
        Bitmap result = KyUtils.getMemoryCache(context).getBitmapFromCache(url); // 从内存缓存中获取图片
        if (result == null) {
            // 文件缓存中获取
            result = KyUtils.getFileCache().getImage(url);
            if (result == null) { // 从网络获取
                try {
                    result = ImageGetFromHttp.downloadBitmap(url);
                } catch (IOException e) {
                    /*使线程拥有自己的消息列队，主线程拥有自己的消息列队，一般线程创建时没有自己的消息列队，
                        消息处理时就在主线程中完成，如果线程中使用Looper.prepare()和Looper.loop()创建了
                        消息队列就可以让消息处理在该线程中完成*/
                    Looper.prepare();
                    logger.error("Get bitmap IO error.", e);
                    Toast.makeText(context, "网络异常", Toast.LENGTH_LONG).show();  // 通知用户连接超时信息
                    Looper.loop();
                }

                if (result != null) {
                    KyUtils.getFileCache().saveBitmap(result, url);
                    KyUtils.getMemoryCache(context).addBitmapToCache(url, result);
                }
            } else {
                // 添加到内存缓存
                KyUtils.getMemoryCache(context).addBitmapToCache(url, result);
            }
        }
        return result;
    }

    /**
     * 比较时间大小  2014/11/11 10:42
     * @param nowTime  当前时间
     * @param oldTime  记录时间
     * @param delayDay oldTime可以延迟多少天进行比对
     * @return 大小
     */
    public static int judgeTime(String nowTime, String oldTime, int delayDay) {
        logger.debug("Judge time.");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Calendar c1 = java.util.Calendar.getInstance();
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        try {

            Date d1 = df.parse(nowTime);
            Date d2 = df.parse(oldTime);
            d2.setDate(d2.getDate() + delayDay);

            c1.setTime(d1);
            c2.setTime(d2);
        } catch (java.text.ParseException e) {
            logger.debug("Parse error.Now:{} and Old:{} and Delay: {}.", nowTime, oldTime, delayDay);
        }

        return c1.compareTo(c2);
    }

    /**
     * 获取当前时间  2014/11/11 10:48
     * @return
     */
    public static String getCurTime() {
        logger.debug("Get current time.");

        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = formatter.format(d); // 当前时间

        return time;
    }

    /**
     * 获取当前日期  2014/11/11 10:48
     * @return
     */
    public static String getCurDate() {
        logger.debug("Get current date.");

        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String time = formatter.format(d); // 当前时间

        return time;
    }

    /**
     * 删除文件或文件夹下面所有的文件 2015/1/22 19:05
     * @param file
     */
    public static void deleteFile(File file) {
        logger.debug("Delete file is {}.", file.getPath());
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }

            file.delete();
        } else {
            System.out.println("文件不存在！"+"\n");
        }
    }

    /**
     * 读取本机图片 2015/1/23 13:22
     * @param pathString 图片位置
     * @return Bitmap
     */
    public static Bitmap getDiskBitmap(String pathString, Context context) {
        logger.debug("Get disk bitmap.Url is {}.", pathString);
        Bitmap result = KyUtils.getMemoryCache(context).getBitmapFromCache(pathString); // 从内存缓存中获取图片
        if (result == null) {

            // 文件缓存中获取
            result = KyUtils.getFileCache().getImage(pathString);
            if (result == null) { // 从本地读取
                File file = new File(pathString);
                if(file.exists()) {
                    result = BitmapFactory.decodeFile(pathString);
                }

                if (result != null) {
                    KyUtils.getMemoryCache(context).addBitmapToCache(pathString, result);
                }
            } else { // 添加到内存缓存
                KyUtils.getMemoryCache(context).addBitmapToCache(pathString, result);
            }
        }

        return result;
    }

    /**
     * 手机震动 2015/4/14 15:21
     * final Activity activity  ：调用该方法的Activity实例
     * long milliseconds ：震动的时长，单位是毫秒
     * long[] pattern  ：自定义震动模式 。数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长。。。]时长的单位是毫秒
     * boolean isRepeat ： 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     */

    public static void vibrate(Context context, long milliseconds) {
        logger.debug("Phone vibrate.");
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static void vibrate(Context context, long[] pattern, boolean isRepeat) {
        logger.debug("Phone vibrate.");
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    /**
     * 获取版本号 2015/3/9 11:48
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        logger.debug("Get app version code.");
        int version = -1;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionCode;
            logger.debug("Current version code is {}. ", version);
        } catch (Exception e) {
            logger.error("Get version code error.", e);
        }

        return version;
    }

    /**
     * 获取版本名称 2015/3/9 11:48
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        logger.debug("Get app version name.");
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            logger.debug("Current version name is {}. ", version);
        } catch (Exception e) {
            logger.error("Get version name error.", e);
        }

        return version;
    }

    /**
     * 下载文件 2014/9/17 19:50
     * @param context  上下文环境
     * @param strUrl  下载地址
     * @param strFolderName  存放目录名称
     * @param strApkName  存放的apk名称
     * @param strShowTitle  限制在通知栏的名称
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void download(Context context, String strUrl, String strFolderName,
                                String strApkName, String strShowTitle) {
        logger.debug("Do download.Url is {}.", strUrl);

        File folder = new File(Environment.getExternalStorageDirectory(), strFolderName);
        boolean isSuccess = (folder.exists() && folder.isDirectory()) || folder.mkdirs();
        if (isSuccess) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(strUrl));
            request.setDestinationInExternalPublicDir(strFolderName, strApkName + ".apk");

            request.setTitle(strShowTitle); // 设置下载中通知栏提示的标题
            request.setDescription("正在下载" + strShowTitle); // 设置下载中通知栏提示的介绍

            /**
             表示下载进行中和下载完成的通知栏是否显示。默认只显示下载中通知。
             VISIBILITY_VISIBLE_NOTIFY_COMPLETED表示下载完成后显示通知栏提示。
             VISIBILITY_HIDDEN表示不显示任何通知栏提示，
             这个需要在AndroidMainfest中添加权限android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
             */
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            /**
             表示下载允许的网络类型，默认在任何网络下都允许下载。
             有NETWORK_MOBILE、NETWORK_WIFI、NETWORK_BLUETOOTH三种及其组合可供选择。
             如果只允许wifi下载，而当前网络为3g，则下载会等待。
             */
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

//            request.setMimeType("application/vnd.android.package-archive");

            // 执行下载，返回downloadId，downloadId可用于后面查询下载信息。若网络不满足条件、Sdcard挂载中、超过最大并发数等异常会等待下载，正常则直接下载。
            long downloadId = downloadManager.enqueue(request);

            Toast.makeText(context, "开始下载", Toast.LENGTH_LONG).show();  // 通知用户下载信息

        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 2015/4/21 10:38
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 2015/4/21 10:38
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 删除文件 2015/4/27 15:28
     * @param path
     */
    public static void deleteFile(String path) {
        logger.debug("Delete file is {}.", path);

        File file = new File(path);
        if (file.exists() && file.isFile())
            file.delete();
    }

    /**
     * 复制单个文件  2015/6/9 15:27
     * @param srcFilePath 待复制的文件名
     * @param descFilePath 目标文件名
     * @param overlay 如果目标文件存在，是否覆盖
     * @return 如果复制成功返回true，否则返回false
     */
    public static boolean copyFile(String srcFilePath, String descFilePath,
                                   boolean overlay) {
        File srcFile = new File(srcFilePath);

        // 判断源文件是否存在
        if (!srcFile.exists()) {
            logger.debug("{} not exists.", srcFilePath);
            return false;
        } else if (!srcFile.isFile()) {
            logger.debug("{} not file.", srcFilePath);
            return false;
        }

        // 判断目标文件是否存在
        File destFile = new File(descFilePath);
        if (destFile.exists()) {
            logger.debug("Desc file exists.");

            // 如果目标文件存在并允许覆盖
            if (overlay) {

                // 删除已经存在的目标文件，无论目标文件是目录还是单个文件
                new File(descFilePath).delete();
            }
        } else {
            logger.debug("Desc file not exists.");

            // 如果目标文件所在目录不存在，则创建目录
            if (!destFile.getParentFile().exists()) {
                // 目标文件所在目录不存在
                if (!destFile.getParentFile().mkdirs()) {
                    // 复制文件失败：创建目标文件所在目录失败
                    return false;
                }
            }
        }

        // 复制文件
        int byteread = 0; // 读取的字节数
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];

            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static double DEF_PI = 3.14159265359; // PI
    static double DEF_2PI= 6.28318530712; // 2*PI
    static double DEF_PI180= 0.01745329252; // PI/180.0
    static double DEF_R =6370693.5; // radius of earth

    /**
     * 适用于近距离 2015/3/3 15:17
     * @return 距离
     */
    public static double getShortDistance(double lon1, double lat1, double lon2, double lat2) {
        logger.debug("Get short distance.");

        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;

        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;

        // 经度差
        dew = ew1 - ew2;

        // 若跨东经和西经180 度，进行调整
        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;

        dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
        dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)

        // 勾股定理求斜边长
        distance = Math.sqrt(dx * dx + dy * dy);

        return distance;
    }

    /**
     * 适用于远距离 2015/3/3 15:17
     * @return 距离
     */
    public static double getLongDistance(double lon1, double lat1, double lon2, double lat2) {
        double ew1, ns1, ew2, ns2;
        double distance;

        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;

        // 求大圆劣弧与球心所夹的角(弧度)
        distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);

        // 调整到[-1..1]范围内，避免溢出
        if (distance > 1.0)
            distance = 1.0;
        else if (distance < -1.0)
            distance = -1.0;

        // 求大圆劣弧长度
        distance = DEF_R * Math.acos(distance);

        return distance;
    }
}
