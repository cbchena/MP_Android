package com.ccb.mp.activity.offline_map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.ccb.mp.R;
import com.ccb.mp.task.activity_manager.ActivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 删除城市提示框 2015/5/26 14:24
 */
public class DelDialogActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(DelDialogActivity.class); // 日志对象

    public static final int DEL_RESULT_OK = 80; // 退出

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_city_del_dialog);
        ActivityManager.getInstance().addActivity(this);
    }

    /**
     * 绑定按钮取消事件 2015/4/8 11:15
     * @param view
     */
    public void OnBtnCancel(View view) {
        logger.debug("Click button to cancel.");
        finish();
    }

    /**
     * 绑定按钮确定事件 2015/4/8 11:15
     * @param view
     */
    public void OnBtnOk(View view) {
        logger.debug("Click button to ok.");
        setResult(OfflineBaiduActivity.RESULT_OK, getIntent()); // 设置返回结果
        finish(); // 结束当前Activity
    }
}
