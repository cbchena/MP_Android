package com.ccb.mp.activity.oper_loc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import com.ccb.mp.R;
import com.ccb.mp.activity.oper_loc.entity.LocationEntity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选择地点类型 2015/5/23 16:22
 * Choose common type dialog activity
 */
public class ChoCmnTypeDialogActivity extends Activity{
    private static Logger logger = LoggerFactory
            .getLogger(ChoCmnTypeDialogActivity.class); // 日志对象

    public static final int CHOOSE_RESULT_OK = 80; // 选择成功

    public static final int TYPE_HOME = 1; // 家
    public static final int TYPE_COMPANY = 2; // 公司
    public static final int TYPE_TRAFFIC = 3; // 交通
    public static final int TYPE_CATERING = 4; // 餐饮
    public static final int TYPE_SHOPPING = 5; // 购物
    public static final int TYPE_MEET = 6; // 聚会
    public static final int TYPE_MOTION = 7; // 运动

    private int _curType = 1; // 当前类型

    /**
     * 根据类型排序 2015/5/25 10:04
     * @param lstLocs 需要排序的列表
     * @return
     */
    public static List<LocationEntity> sortByType(List<LocationEntity> lstLocs) {
        Collections.sort(lstLocs, new Comparator<LocationEntity>() {
            public int compare(LocationEntity arg0, LocationEntity arg1) {
                int result = 0;
                if (arg0.getType() > arg1.getType())
                    result = 1;
                else if (arg0.getType() < arg1.getType())
                    result = -1;

                return result;
            }
        });

        return lstLocs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_choose_dialog);
        ActivityManager.getInstance().addActivity(this);

        Bundle bundle = getIntent().getBundleExtra(Const.DATA);
        if (bundle != null) {
            _curType = bundle.getInt(Const.TYPE);
        }

    }

    /**
     * 绑定按钮确定事件 2015/4/8 11:15
     * @param view
     */
    public void OnBtnChoose(View view) {
        logger.debug("Click button to Choose.ID is {}.", view.getId());
        int type = TYPE_HOME;
        switch (view.getId()) {
            case R.id.btnHome:
                type = TYPE_HOME;
                break;
            case R.id.btnCompany:
                type = TYPE_COMPANY;
                break;
            case R.id.btnTraffic:
                type = TYPE_TRAFFIC;
                break;
            case R.id.btnCatering:
                type = TYPE_CATERING;
                break;
            case R.id.btnShopping:
                type = TYPE_SHOPPING;
                break;
            case R.id.btnMeet:
                type = TYPE_MEET;
                break;
            case R.id.btnMotion:
                type = TYPE_MOTION;
                break;
        }

        _finish(type);
    }

    /**
     * 结束 2015/5/23 16:37
     */
    private void _finish(int type) {
        logger.debug("Finish.Type is {}.", type);
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putInt(Const.TYPE, type);
        intent.putExtra(Const.DATA, bundle);

        setResult(ChoCmnTypeDialogActivity.RESULT_OK, intent); // 设置返回结果
        finish(); // 结束当前Activity
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _finish(_curType);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
