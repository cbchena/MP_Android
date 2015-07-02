package com.ccb.mp.activity.oper_loc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.activity.oper_loc.entity.LocationEntity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编辑位置 2015/5/21 19:30
 */
public class EditLocationActivity extends Activity {

    private static Logger logger = LoggerFactory
            .getLogger(EditLocationActivity.class); // 日志对象

    private TextView _txtName; // 名称
    private TextView _txtCurLoc; // 我的位置
    private TextView _txtTel; // 电话
    private TextView _txtDesc; // 备注
    private ImageButton _btnType; // 类型
    private Button _btnShare;// 分享
    private int _id = 0;
    private int _type = 1;
    private Double _lat;
    private Double _lng;
    private String _name;
    private String _loc;

    public static final int EDIT_LOC_OK = 82;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_edit_loc);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        _txtName = (TextView) this.findViewById(R.id.name); // 名称
        _txtCurLoc = (TextView) this.findViewById(R.id.curLoc); // 我的位置
        _txtTel = (TextView) this.findViewById(R.id.tel); // 电话
        _txtDesc = (TextView) this.findViewById(R.id.desc); // 备注
        _btnType = (ImageButton) this.findViewById(R.id.btnType); // 类型
        _btnShare = (Button) this.findViewById(R.id.btnShare); // 分享

        Bundle bundle = this.getIntent().getBundleExtra(Const.DATA);
        if (bundle != null) { // 获取位置
            _name = bundle.getString(Const.NAME);
            _loc = bundle.getString(Const.LOCATION);
            _txtName.setText(_name); // 设置名称
            _txtCurLoc.setText(_loc); // 设置位置
            _txtTel.setText(bundle.getString(Const.TEL)); // 设置电话
            _txtDesc.setText(bundle.getString(Const.DESC)); // 设置描述
            _id = bundle.getInt(Const.ID);
            _type = bundle.getInt(Const.TYPE);
            _lat = bundle.getDouble(Const.LAT);
            _lng = bundle.getDouble(Const.LNG);
            _refreshIcon(_type);
        }
    }

    /**
     * 刷新类型 2015/5/23 17:03
     * @param type
     */
    private void _refreshIcon(int type) {
        logger.debug("Refresh icon.Type is {}.", type);
        switch (type) {
            case ChoCmnTypeDialogActivity.TYPE_HOME:
                _btnType.setBackgroundResource(R.drawable.tab_home_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_COMPANY:
                _btnType.setBackgroundResource(R.drawable.tab_me_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_TRAFFIC:
                _btnType.setBackgroundResource(R.drawable.tab_home_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_CATERING:
                _btnType.setBackgroundResource(R.drawable.tab_home_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_SHOPPING:
                _btnType.setBackgroundResource(R.drawable.tab_home_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_MEET:
                _btnType.setBackgroundResource(R.drawable.tab_friend_check);
                break;
            case ChoCmnTypeDialogActivity.TYPE_MOTION:
                _btnType.setBackgroundResource(R.drawable.tab_home_check);
                break;
        }
    }

    /**
     * 绑定返回事件 2015/5/21 15:59
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        setResult(EditLocationActivity.RESULT_OK, getIntent()); // 设置返回结果
        finish(); // 结束当前Activity
    }

    /**
     * 分享按钮 2015/5/27 21:30
     * @param view
     */
    public void OnBtnShare(View view) {
//        ShareLocation.getInstance(this).share(_name, _loc, new LatLng(_lat, _lng));
    }

    /**
     * 选择类型 2015/5/23 16:47
     * @param view
     */
    public void OnBtnChoose(View view) {
        Intent intent = new Intent(this, ChoCmnTypeDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Const.TYPE, _type);
        intent.putExtra(Const.DATA, bundle);

        startActivityForResult(intent, ChoCmnTypeDialogActivity.CHOOSE_RESULT_OK);
    }

    /**
     * 删除 2015/5/22 14:30
     * @param view
     */
    public void OnBtnDelete(View view) {
        logger.debug("On click button to delete location.");
        if (MainActivity.get_db().getDbManagerCommonLoc().deleteDataById(String.valueOf(_id)) > 0) {
            Toast.makeText(EditLocationActivity.this,
                    "删除地点成功", Toast.LENGTH_LONG).show();

            setResult(EditLocationActivity.RESULT_OK, getIntent()); // 设置返回结果
            MainActivity.removeMarker(_id);
            finish(); // 结束当前Activity
        } else {
            Toast.makeText(EditLocationActivity.this,
                    "删除地点失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 绑定修改位置事件 2015/5/21 19:36
     * @param view
     */
    public void OnBtnUpdate(View view) {
        logger.debug("On click button to add location.");
        String loc = this._txtCurLoc.getText().toString();
        if (loc == null || loc.length() == 0) {
            Toast.makeText(EditLocationActivity.this,
                    "标注我的位置不能为空", Toast.LENGTH_LONG).show();  // 标注我的位置不能为空

            return;
        }

        String name = this._txtName.getText().toString();
        if (name == null || name.length() == 0) {
            Toast.makeText(EditLocationActivity.this,
                    "备注不能为空", Toast.LENGTH_LONG).show();  // 备注不能为空

            return;
        }

        // 添加到数据库 2015/5/21 19:46
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setId(_id)
                .setLoc(loc)
                .setDesc(_txtDesc.getText().toString())
                .setTel(_txtTel.getText().toString())
                .setName(name)
                .setType(_type)
                .setTime(String.valueOf(System.currentTimeMillis()));

        if (MainActivity.get_db().getDbManagerCommonLoc().update(locationEntity) > 0) {
            Toast.makeText(EditLocationActivity.this,
                    "修改地点成功", Toast.LENGTH_LONG).show();

            setResult(EditLocationActivity.RESULT_OK, getIntent()); // 设置返回结果
            finish(); // 结束当前Activity
        } else {
            Toast.makeText(EditLocationActivity.this,
                    "修改地点失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug("On activity result is {}.", resultCode);
        if(resultCode == RESULT_OK) {  //返回成功
            switch (requestCode) {
                case ChoCmnTypeDialogActivity.CHOOSE_RESULT_OK: // 选择类型
                    logger.info("Choose success.");
                    Bundle bundle = data.getBundleExtra(Const.DATA);
                    if (bundle != null) {
                        _type = bundle.getInt(Const.TYPE);
                        _refreshIcon(_type);
                        logger.info("Choose type is {}.", _type);
                    }

                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }
}
