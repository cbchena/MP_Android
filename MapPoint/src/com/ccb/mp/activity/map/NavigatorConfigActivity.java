package com.ccb.mp.activity.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ccb.mp.R;
import com.ccb.mp.activity.poi.SearchDialogActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import com.knowyou.ky_sdk.FilesService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 导航配置界面 2015/6/30 10:50
 */
public class NavigatorConfigActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(NavigatorConfigActivity.class); // 日志对象

    private Button _btnSearch; // 搜索
    private TextView _txtSource; // 来源
    private TextView _txtTarget; // 目标

    private String _strloc; // 当前位置
    private String _strCity; // 城市
    private Double _lat;
    private Double _lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_navigator_config);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        _btnSearch = (Button) this.findViewById(R.id.btnSearch);
        _txtSource = (TextView) this.findViewById(R.id.txtSource);
        _txtTarget = (TextView) this.findViewById(R.id.txtTarget);
        _txtTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _onOpenSearch(); // 打开搜索界面
            }
        });

        // 设置数据 2015/6/30 11:17
        Bundle bundle = this.getIntent().getBundleExtra(Const.DATA);
        if (bundle != null) {
            _strloc = bundle.getString(Const.LOCATION);
            _strCity = bundle.getString(Const.CITY);
            _lat = bundle.getDouble(Const.LAT);
            _lng = bundle.getDouble(Const.LNG);

            _txtTarget.setText(_strloc);
        }

    }

    /**
     * 更新UI 2015/6/30 11:13
     */
    private Handler _handlerTxt = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle != null) {
                _txtTarget.setText(bundle.getString(Const.LOCATION));
            }
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
                    JSONObject objAddressComponent = objResult.getJSONObject("addressComponent");

                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString(Const.LOCATION, objResult.getString("formatted_address"));
                    data.putString(Const.NAME, objAddressComponent.getString("street"));

                    msg.setData(data);
                    _handlerTxt.sendMessage(msg);

                } catch (IOException e) {
                    Looper.prepare();
                    logger.error("Get address IO error.", e);
                    Toast.makeText(NavigatorConfigActivity.this,
                            "获取百度位置信息异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (JSONException e) {
                    Looper.prepare();
                    logger.error("Get address JSON error.", e);
                    Toast.makeText(NavigatorConfigActivity.this,
                            "解析百度json数据异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        };

        new Thread(runnable).start();
    }

    /**
     * 搜索 2015/6/30 11:05
     * @param view
     */
    public void OnSearch(View view) {
        logger.debug("On click button to search location.");

        // 打开路线规划界面 2015/5/22 15:46
        Intent intent = new Intent(this, BDRoutePlan.class);
        Bundle bundle = new Bundle();
        bundle.putDouble(Const.LAT, _lat);
        bundle.putDouble(Const.LNG, _lng);

        // 将Bundle添加到Intent里面
        intent.putExtra(Const.DATA, bundle);
        startActivity(intent);
    }

    /**
     * 绑定打开搜索事件 2015/6/30 11:21
     */
    private void _onOpenSearch() {
        logger.debug("Click button open search.");
        Intent intent = new Intent(this, SearchDialogActivity.class);
        Bundle bundle = new Bundle();
        if (_strCity != null) {
            bundle.putString("city", _strCity);
        } else {
//            bundle.putString("city", HeartbeatLocation.city);
        }

        intent.putExtra(Const.DATA, bundle);

        startActivityForResult(intent, SearchDialogActivity.SEARCH_RESULT_OK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug("On activity result is {}.", resultCode);
        Bundle bundle;
        if(resultCode == RESULT_OK) {  //返回成功
            switch (requestCode) {
                case SearchDialogActivity.SEARCH_RESULT_OK: // 搜查结果
                    bundle = data.getBundleExtra(Const.DATA);
                    if (bundle != null) {
                        _lat = bundle.getDouble(Const.LAT);
                        _lng = bundle.getDouble(Const.LNG);
                        _able(_lat, _lng);
                    }

                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 绑定返回 2015/6/30 15:04
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On back.");
        finish();
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }
}
