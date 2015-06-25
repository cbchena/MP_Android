package com.ccb.mp.task.activity_manager;

import android.app.Activity;
import android.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity管理器 2015/6/25 15:19
 */
public class ActivityManager extends Application {

    private static Logger logger = LoggerFactory
            .getLogger(ActivityManager.class); // 日志对象

    private List<Activity> _lstActivities = new LinkedList<Activity>();
    private static ActivityManager _instance;

    public static ActivityManager getInstance() {
        logger.debug("Get instance.");
        if (_instance == null) {
            _instance = new ActivityManager();
        }

        return _instance;
    }

    /**
     * 添加Activity 2015/6/25 15:20
     * @param activity 界面
     */
    public void addActivity(Activity activity) {
        logger.debug("Add activity class name is {}.", activity.getClass().getName());
        _lstActivities.add(activity);
    }

    /**
     * 遍历Activity，全部finish掉 2015/6/25 15:20
     */
    public void exit() {
        logger.debug("System exit.");
        for (Activity activity:_lstActivities) {
            if (activity != null) {
                logger.debug("Finish activity class name is {}.", activity.getClass().getName());
                activity.finish();
            }
        }

        System.exit(0); // 退出系统
    }

}
