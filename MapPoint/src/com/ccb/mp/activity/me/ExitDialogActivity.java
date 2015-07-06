package com.ccb.mp.activity.me;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.ccb.mp.R;
import com.ccb.mp.task.activity_manager.ActivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 退出 2015/7/6 14:48
 */
public class ExitDialogActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(ExitDialogActivity.class); // 日志对象

    public static final int EXIT_RESULT_OK = 80; // 退出

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_exit_dialog);
        ActivityManager.getInstance().addActivity(this);
    }

    /**
     * 绑定按钮取消事件 2015/7/6 14:49
     * @param view
     */
    public void OnBtnCancel(View view) {
        logger.debug("Click button to cancel.");
        finish();
    }

    /**
     * 绑定按钮确定事件 2015/7/6 14:49
     * @param view
     */
    public void OnBtnOk(View view) {
        logger.debug("Click button to ok.");
        setResult(MeActivity.RESULT_OK, getIntent()); // 设置返回结果
        finish(); // 结束当前Activity
    }
}
