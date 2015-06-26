package com.ccb.mp.activity.oper_loc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import com.knowyou.ky_sdk.FilesService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 添加位置 2015/5/21 19:30
 */
public class AddLocationActivity extends Activity {

    private static Logger logger = LoggerFactory
            .getLogger(AddLocationActivity.class); // 日志对象

    private TextView _txtName; // 名称
    private TextView _txtCurLoc; // 我的位置
    private TextView _txtTel; // 电话
    private TextView _txtDesc; // 备注
    private ImageButton _btnType; // 类型
    private Double _lat;
    private Double _lng;
    private int _type = 1; // 类型

    public static final int ADD_LOCATION_OK = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_add_loc);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        _txtCurLoc = (TextView) this.findViewById(R.id.curLoc); // 我的位置
        _txtDesc = (TextView) this.findViewById(R.id.desc); // 备注
        _txtName = (TextView) this.findViewById(R.id.name); // 姓名
        _txtTel = (TextView) this.findViewById(R.id.tel); // 电话
        _btnType = (ImageButton) this.findViewById(R.id.btnType); // 类型

        Bundle bundle = this.getIntent().getBundleExtra(Const.DATA);
        if (bundle != null) { // 获取位置
            _lat = bundle.getDouble("lat");
            _lng = bundle.getDouble("lng");

            _txtName.setText(bundle.getString("name"));
            _txtCurLoc.setText(bundle.getString("location"));

//            _able(_lat, _lng);
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
     * 处理位置 2015/5/21 19:42
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            _txtCurLoc.setText(bundle.getString("location")); // 设置位置
        }
    };

    /**
     * 获取位置 2015/5/21 19:40
     * @param lat 经度
     * @param lng 纬度
     */
    private void _able(final double lat, final double lng) {
        logger.debug("Get address.");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String ll = lat + "," + lng;
                    byte[] json = FilesService.getBytes("http://api.map.baidu.com/geocoder?location="
                            + ll + "&output=json&key=" + Const.BAIDU_KEY);

                    JSONObject objJson = new JSONObject(new String(json)); // 解析JSON
                    JSONObject objResult = objJson.getJSONObject("result");

                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("location", objResult.getString("formatted_address"));

                    msg.setData(data);
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    Looper.prepare();
                    logger.error("Get address IO error.", e);
                    Toast.makeText(AddLocationActivity.this,
                            "获取百度位置信息异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (JSONException e) {
                    Looper.prepare();
                    logger.error("Get address JSON error.", e);
                    Toast.makeText(AddLocationActivity.this,
                            "解析百度json数据异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        };

        new Thread(runnable).start();
    }

    /**
     * 选择类型 2015/5/23 16:47
     * @param view
     */
    public void OnBtnChoose(View view) {
        Intent intent = new Intent(this, ChoCmnTypeDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Const.TYPE, 1);
        intent.putExtra(Const.DATA, bundle);

        startActivityForResult(intent, ChoCmnTypeDialogActivity.CHOOSE_RESULT_OK);
    }

    /**
     * 绑定返回事件 2015/5/21 15:59
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        finish();
    }

    /**
     * 绑定添加位置事件 2015/5/21 19:36
     * @param view
     */
    public void OnBtnAdd(View view) {
        logger.debug("On click button to add location.");
        String loc = this._txtCurLoc.getText().toString();
        if (loc == null || loc.length() == 0) {
            Toast.makeText(AddLocationActivity.this,
                    "标注我的位置不能为空", Toast.LENGTH_LONG).show();  // 标注我的位置不能为空

            return;
        }

        String name = this._txtName.getText().toString();
        if (name == null || name.length() == 0) {
            Toast.makeText(AddLocationActivity.this,
                    "名称不能为空", Toast.LENGTH_LONG).show();  // 名称不能为空

            return;
        }

        // 添加到数据库 2015/5/21 19:46
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setLoc(loc)
                .setName(name)
                .setType(_type)
                .setDesc(_txtDesc.getText().toString())
                .setTel(_txtTel.getText().toString())
                .setLat(String.valueOf(_lat))
                .setLng(String.valueOf(_lng))
                .setTime(String.valueOf(System.currentTimeMillis()));

        long id = MainActivity.get_db().getDbManagerCommonLoc().add(locationEntity);
        if (id > 0) {
            Toast.makeText(AddLocationActivity.this,
                    "添加地点成功", Toast.LENGTH_LONG).show();

            Intent intent = getIntent();
            Bundle bundle = new Bundle();
            bundle.putInt(Const.ID, (int) id);
            intent.putExtra(Const.DATA, bundle);

            setResult(AddLocationActivity.RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(AddLocationActivity.this,
                    "添加地点失败", Toast.LENGTH_LONG).show();
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
