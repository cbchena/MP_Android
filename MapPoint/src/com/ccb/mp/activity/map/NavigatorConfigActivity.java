package com.ccb.mp.activity.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.activity.oper_loc.entity.NavigatorEntity;
import com.ccb.mp.activity.poi.SearchDialogActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import com.knowyou.ky_sdk.FilesService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 导航配置界面 2015/6/30 10:50
 */
public class NavigatorConfigActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(NavigatorConfigActivity.class); // 日志对象

    private Button _btnSearch; // 搜索
    private TextView _txtSource; // 来源
    private TextView _txtTarget; // 目标

    private ListView _lstView; // 列表
    private NavigatorListAdapter _mNavigatorListAdapter; // 导航列表适配器

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

        _lstView = (ListView) this.findViewById(R.id.lstItem);
        _lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NavigatorEntity navigatorEntity = _mNavigatorListAdapter.getNavigator(i);

                // 打开路线规划界面 2015/7/2 16:33
                Intent intent = new Intent(NavigatorConfigActivity.this, BDRoutePlan.class);
                Bundle bundle = new Bundle();
                bundle.putDouble(Const.LAT, Double.parseDouble(navigatorEntity.getLat()));
                bundle.putDouble(Const.LNG, Double.parseDouble(navigatorEntity.getLng()));

                // 将Bundle添加到Intent里面
                intent.putExtra(Const.DATA, bundle);
                startActivity(intent);

                navigatorEntity.setTime(String.valueOf(System.currentTimeMillis()));
                int count = MainActivity.get_db().getDbManagerNavigatorHistory().updata(navigatorEntity);
                if (count > 0) {
                    _getData(); // 刷新数据
                }
            }
        });

        _mNavigatorListAdapter = new NavigatorListAdapter(); // 初始化适配器
        _lstView.setAdapter(_mNavigatorListAdapter);

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

        _getData(); // 获取历史数据 2015/7/2 16:27
    }

    /**
     * 获取数据 2015/7/2 16:26
     */
    private void _getData() {
        _mNavigatorListAdapter.clear();
        List<NavigatorEntity> lstData = MainActivity.get_db().getDbManagerNavigatorHistory().getData();
        for(NavigatorEntity navigatorEntity:lstData) {
            _mNavigatorListAdapter.add(navigatorEntity);
        }

        _mNavigatorListAdapter.notifyDataSetChanged();
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

        // 将搜索记录添加到历史记录中 2015/7/2 16:28
        NavigatorEntity navigatorEntity = new NavigatorEntity();
        navigatorEntity.setLat(String.valueOf(_lat))
                .setLng(String.valueOf(_lng))
                .setTime(String.valueOf(System.currentTimeMillis()))
                .setSrcLoc(_txtSource.getText().toString())
                .setDecLoc(_txtTarget.getText().toString());

        long id = MainActivity.get_db().getDbManagerNavigatorHistory().add(navigatorEntity);
        if (id > 0) { // 添加成功，刷新数据 2015/7/2 16:31
            navigatorEntity.setId((int) id);
            _mNavigatorListAdapter.add(0, navigatorEntity);
            _mNavigatorListAdapter.notifyDataSetChanged();
        }
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

    /**
     * 清空历史 2015/7/2 16:42
     * @param view
     */
    public void OnClear(View view) {
        logger.debug("On click button to clear navigator history.");
        MainActivity.get_db().getDbManagerNavigatorHistory().del();
        _mNavigatorListAdapter.clear();
        _mNavigatorListAdapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }

    /**
     * 列表适配器 2015/5/18 16:40
     */
    private class NavigatorListAdapter extends BaseAdapter {
        private ArrayList<NavigatorEntity> _lstNavigators;
        private LayoutInflater mInflator;

        public NavigatorListAdapter() {
            super();
            _lstNavigators = new ArrayList<NavigatorEntity>();
            mInflator = NavigatorConfigActivity.this.getLayoutInflater();
        }

        public void add(NavigatorEntity navigator) {
            for (NavigatorEntity navigatorEntity: _lstNavigators) {
                if (navigatorEntity.getId() == navigator.getId())
                    return;
            }

            _lstNavigators.add(navigator);
        }

        public void add(int idx, NavigatorEntity navigator) {
            for (NavigatorEntity navigatorEntity: _lstNavigators) {
                if (navigatorEntity.getId() == navigator.getId())
                    return;
            }

            _lstNavigators.add(idx, navigator);
        }

        public NavigatorEntity getNavigator(int position) {
            return _lstNavigators.get(position);
        }

        public NavigatorEntity getDevice(int id) {
            for (NavigatorEntity navigatorEntity: _lstNavigators) {
                if (navigatorEntity.getId() == id)
                    return navigatorEntity;
            }

            return null;
        }

        public void clear() {
            _lstNavigators.clear();
        }

        @Override
        public int getCount() {
            return _lstNavigators.size();
        }

        @Override
        public Object getItem(int i) {
            return _lstNavigators.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_navigator_config_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtSrcLoc = (TextView) view
                        .findViewById(R.id.txtSrcLoc);
                viewHolder.txtDecLoc = (TextView) view
                        .findViewById(R.id.txtDecLoc);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NavigatorEntity navigatorEntity = _lstNavigators.get(i);
            viewHolder.txtSrcLoc.setText(navigatorEntity.getSrcLoc());
            viewHolder.txtDecLoc.setText(navigatorEntity.getDecLoc());

            return view;
        }
    }

    static class ViewHolder {
        TextView txtSrcLoc;
        TextView txtDecLoc;
    }
}
