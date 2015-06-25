package com.ccb.mp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.baidu.mapapi.SDKInitializer;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 启动界面 2015/6/25 14:11
 */
public class StartActivity extends Activity {

    private static Logger logger = LoggerFactory
            .getLogger(StartActivity.class); // 日志对象

    private boolean _isLogin = false;
    private Timer _timer; // 计时器
    private TimerTask _task = new TimerTask() {
        @Override
        public void run() {
            logger.debug("Running time task.");
            _timer.cancel();
            if (_isLogin) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_start);
        ActivityManager.getInstance().addActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 全屏显示

        // 判断是否登录过 2015/6/25 15:03
        _isLogin = true;

        _timer = new Timer();
        _timer.schedule(_task, 3000);

        SDKInitializer.initialize(getApplicationContext());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
