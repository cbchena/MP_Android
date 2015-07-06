package com.ccb.mp.activity.me;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重置图点 2015/7/6 15:31
 */
public class ResetMapPointActivity extends Activity {

    private static Logger logger = LoggerFactory
            .getLogger(ResetMapPointActivity.class); // 日志对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_me_reset_map_point);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }

    /**
     * 清空搜索历史 2015/7/6 15:44
     * @param view
     */
    public void OnClearSearch(View view) {
        logger.debug("On click button to clear search history.");
        MainActivity.get_db().getDbManagerSearchHistory().del();
        Toast.makeText(this, "清空搜索历史成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 清空导航历史 2015/7/6 15:44
     * @param view
     */
    public void OnClearNavigator(View view) {
        logger.debug("On click button to clear navigator history.");
        MainActivity.get_db().getDbManagerNavigatorHistory().del();
        Toast.makeText(this, "清空导航历史成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 清空图点 2015/7/6 15:47
     * @param view
     */
    public void OnClearMP(View view) {
        logger.debug("On click button to clear map point history.");
        MainActivity.get_db().getDbManagerCommonLoc().del();
        Toast.makeText(this, "清空图点信息成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 一键清空 2015/7/6 15:48
     * @param view
     */
    public void OnClear(View view) {
        logger.debug("On click button to clear all history.");
        OnClearSearch(view);
        OnClearNavigator(view);
        OnClearMP(view);
    }

    /**
     * 返回事件 2015/7/6 14:35
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        finish();
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }
}
