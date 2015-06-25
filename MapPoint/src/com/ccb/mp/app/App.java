package com.ccb.mp.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import com.ccb.mp.task.log.CrashHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动程序时，调用的类 2015/6/25 16:36
 */
public class App  extends Application {

    private static Logger logger = LoggerFactory
            .getLogger(App.class); // 日志对象

    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        logger.debug("On create.");

        super.onCreate();
        mInstance = this;

        // 启动全局日志异常监听 2015/6/25 16:39
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
