package com.ccb.mp.activity.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.ccb.mp.R;
import com.ccb.mp.activity.map.BDRoutePlan;
import com.ccb.mp.activity.oper_loc.AddLocationActivity;
import com.ccb.mp.activity.oper_loc.EditLocationActivity;
import com.ccb.mp.activity.oper_loc.LocationEntity;
import com.ccb.mp.activity.poi.SearchDialogActivity;
import com.ccb.mp.db.DB;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import com.ccb.mp.utils.Utils;
import com.knowyou.ky_sdk.FilesService;
import com.knowyou.ky_sdk.utils.KyUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主界面 2015/6/25 15:01
 */
public class MainActivity extends Activity implements BaiduMap.OnMapClickListener {

    private static Logger logger = LoggerFactory
            .getLogger(MainActivity.class); // 日志对象

    private static Context _context;
    private static DB _db; // 数据库管理

    private MapView _mMapView = null;    // 地图View
    private BaiduMap _mBaidumap = null;
    private BitmapDescriptor _bitmapDescriptor; // 标注的图标
    private BitmapDescriptor _bmpStar; // 收藏的地点图标
    private LocationClient _locationClient = null;
    private LatLng _latLng; // 当前位置
    private LatLng _clkLatLng; // 点击的位置

    private ImageButton _imgBtnPlus; // 放大
    private ImageButton _imgBtnMinus; // 放小
    private Button _btnOperLoc; // 操作位置
    private Button _btnTrack; // 追踪
    private Button _btnCurLocation; // 定位当前位置
    private LinearLayout _ll_search; // 查找布局
    private LinearLayout _ll_loc;  // 显示位置布局
    private TextView _txtLoc; // 显示位置
    private TextView _txtName; // 显示名称

    private boolean _isLoad = false; // 是否加载
    private static Map<Integer, Marker> _mapCollectMarker; // 存放收藏的位置的标记
    private Marker _marker; // 当前位置
    private Marker _curMarker; // 点击的当前位置
    private Circle _corlorCircle; // 颜色标记
    private boolean _isClkCollectMarker; // 是否点击的是收藏的位置
    private String _city; // 当前城市
    private String _locName;
    private int _chkCollectLocId; // 选择的收藏点id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_main);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        if (_db == null) // 数据库管理
            _db = new DB(this);

        _context = this;

        // 初始化地图
        _mMapView = (MapView) this.findViewById(R.id.bmapView);
        _mBaidumap = _mMapView.getMap();

        // 隐藏放大缩小按钮，进行自定义
        _mMapView.showZoomControls(false);

        // 地图点击事件处理
        _mBaidumap.setOnMapClickListener(this);
        _mBaidumap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                logger.debug("On click marker.");
                showCollectLoc(marker.getPosition(), marker.getTitle()); // 显示位置 2015/5/22 15:36

                return false;
            }
        });

        this._imgBtnPlus = (ImageButton) this.findViewById(R.id.btnPlus);
        this._imgBtnPlus.setOnClickListener(new View.OnClickListener() { // 添加放大事件
            @Override
            public void onClick(View view) {
                OnPlus(view);
            }
        });

        this._imgBtnMinus = (ImageButton) this.findViewById(R.id.btnMinus);
        this._imgBtnMinus.setOnClickListener(new View.OnClickListener() { // 添加缩小事件
            @Override
            public void onClick(View view) {
                OnMinus(view);
            }
        });

        this._btnOperLoc = (Button) this.findViewById(R.id.btnOperLocation);
        this._btnOperLoc.setOnClickListener(new View.OnClickListener() { // 添加位置事件
            @Override
            public void onClick(View view) {
                OnOperLoc(view);
            }
        });

        this._btnTrack = (Button) this.findViewById(R.id.btnTrack);
        this._btnTrack.setOnClickListener(new View.OnClickListener() { // 添加追踪事件
            @Override
            public void onClick(View view) {
                OnTrack(view);
            }
        });

        this._btnCurLocation = (Button) this.findViewById(R.id.btnCurLocation);
        this._btnCurLocation.setOnClickListener(new View.OnClickListener() { // 添加定位当前位置事件
            @Override
            public void onClick(View view) {
                OnCurLocation(view);
            }
        });

        _mapCollectMarker = new HashMap<Integer, Marker>();

        // 显示的坐标icon
        _bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map_point);
        _bmpStar = BitmapDescriptorFactory.fromResource(R.drawable.common_star);
        _showCollectLocs(); // 显示收藏的位置
        _showCurrentLoc(); // 显示当前位置

        _ll_search = (LinearLayout) this.findViewById(R.id.ll_search);
        _ll_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnOpenSearch(view);
            }
        });

        _ll_loc = (LinearLayout) this.findViewById(R.id.ll_loc);
        _txtLoc = (TextView) this.findViewById(R.id.txtLoc);
        _txtName = (TextView) this.findViewById(R.id.txtName);
        _locName = "当前位置";
        _txtName.setText("当前位置");

    }

    /**
     * 显示当前位置 2015/5/21 16:35
     */
    private void _showCurrentLoc() {
        logger.debug("Show current location.");

        _locationClient = new LocationClient(this);

        // 设置定位条件
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                                //是否打开GPS
        option.setAddrType("all");
        option.setCoorType("bd09ll");                           //设置返回值的坐标类型。
        option.setProdName("LocationDemo");                     //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(60000);                        //设置定时定位的时间间隔。单位毫秒

        /**
         * 1、高精度模式定位策略：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
         2、低功耗模式定位策略：该定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
         3、仅用设备模式定位策略：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
         Hight_Accuracy高精度、Battery_Saving低功耗、Device_Sensors仅设备(GPS)
         */
        boolean isNetwork = KyUtils.networkStatusOK(this);
        if (isNetwork) // 有网络情况(wifi或者网络数据)下，使用网络发送
            option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        else // 否则使用gps定位发送
            option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);

        _locationClient.setLocOption(option);

        //注册位置监听器
        _locationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub
                if (location == null) {
                    return;
                }

                boolean isSuccess = false;
                logger.debug("LocType: {}.", location.getLocType());
                if (location.getLocType() == BDLocation.TypeGpsLocation) { // GPS定位结果
                    isSuccess = true;
                    logger.debug("GpsLocation: {}, {}.", location.getLatitude(), location.getLongitude());
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) { // 网络定位结果
                    isSuccess = true;
                    logger.debug("NetWorkLocation: {}, {}.", location.getLatitude(), location.getLongitude());
                }

                _locationClient.stop();
                if (isSuccess) {
                    logger.debug("Get local location success.");
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    _latLng = new LatLng(lat, lng); // 当前位置 2015/5/21 16:39
                    _city = location.getCity(); // 存放当前城市

                    // 显示详细信息 2015/5/23 15:33
                    _locName = "当前位置";
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("location", location.getAddrStr());
                    data.putString("distance", "0");

                    msg.setData(data);
                    handler.sendMessage(msg);

                    // 定位当前位置 2015/5/21 16:57
                    if (_curMarker != null)
                        _curMarker.remove();

                    if (_marker != null)
                        _marker.remove();

                    _curMarker = null;
                    _clkLatLng = null;
                    _isClkCollectMarker = false;
                    _handlerBtnOper.sendEmptyMessage(0); // 变更操作按钮状态 2015/5/28 18:52

                    MapStatusUpdate mapStatusUpdate_zoom = MapStatusUpdateFactory.zoomTo(18);
                    _mBaidumap.setMapStatus(mapStatusUpdate_zoom);
                    OverlayOptions overlayOptions_marker = new MarkerOptions().position(_latLng).icon(_bitmapDescriptor);
                    _marker = (Marker) _mBaidumap.addOverlay(overlayOptions_marker);
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(_latLng);
                    _mBaidumap.animateMapStatus(mapStatusUpdate);

                    // 以当前点为原点，获取XX米的半径范围内的所有收藏点 2015/6/1 14:53
                    _getCollectByRadius(200);
                }
            }
        });

        _locationClient.start();
        _locationClient.requestLocation();
    }

    /**
     * 显示收藏的所有位置 2015/5/22 14:43
     */
    private void _showCollectLocs() {
        logger.debug("Show collect location.");
        List<LocationEntity> lstLocs = MainActivity.get_db().getDbManagerCommonLoc().getData();
        for(LocationEntity locationEntity:lstLocs) {
            LatLng latLng = new LatLng(Double.valueOf(locationEntity.getLat()),
                    Double.valueOf(locationEntity.getLng()));

            OverlayOptions overlayOptions_marker = new MarkerOptions().position(latLng).icon(_bmpStar);
            Marker marker = (Marker) _mBaidumap.addOverlay(overlayOptions_marker);
            marker.setTitle(locationEntity.getName() + "**" + locationEntity.getLoc() + "**" + locationEntity.getId());

            _mapCollectMarker.put(locationEntity.getId(), marker); // 将收藏的标记记录下来
        }

        _updateInfo(_chkCollectLocId); // 更新当前面板信息 2015/5/28 19:21
    }

    /**
     * 删除标注 2015/5/22 14:49
     * @param id
     */
    public static void removeMarker(int id) {
        logger.debug("Remove marker by id.It is {}.", id);
        if (_mapCollectMarker.containsKey(id)) {
            Marker marker = _mapCollectMarker.remove(id);
            marker.remove();
        }
    }

    /**
     * 刷新标注 2015/5/22 15:14
     */
    public void refreshMarker() {
        logger.debug("Refresh marker.");
        for (Integer key:_mapCollectMarker.keySet()) {
            Marker marker = _mapCollectMarker.get(key);
            marker.remove();
        }

        _mapCollectMarker.clear();
        _showCollectLocs(); // 重新显示 2015/5/22 15:19
    }

    /**
     * 显示收藏的位置 2015/5/22 14:16
     * @param latLng
     */
    public void showCollectLoc(LatLng latLng, String loc) {
        logger.debug("Show location.");
        if (_curMarker != null)
            _curMarker.remove();
        else if (_marker != null)
            _marker.remove();

        _isClkCollectMarker = true;
        _handlerBtnOper.sendEmptyMessage(0); // 变更操作按钮状态 2015/5/28 18:52

        _clkLatLng = latLng;
        MapStatusUpdate mapStatusUpdate_zoom = MapStatusUpdateFactory.zoomTo(18);
        _mBaidumap.setMapStatus(mapStatusUpdate_zoom);
        OverlayOptions overlayOptions_marker = new MarkerOptions().position(_clkLatLng).icon(_bitmapDescriptor);
        _curMarker = (Marker) _mBaidumap.addOverlay(overlayOptions_marker);
        _curMarker.setTitle(loc);

        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(_clkLatLng);
        _mBaidumap.animateMapStatus(mapStatusUpdate);

        if (loc != null && loc.length() > 0) { // 显示地点详细信息 2015/5/23 17:13
            Message msg = new Message();
            Bundle data = new Bundle();
            String[] str = loc.split("\\*\\*");
            if (str.length == 3) {
                _locName = str[0];
                data.putString("location", str[1]);
                msg.setData(data);
                handler.sendMessage(msg);

                _chkCollectLocId = Integer.parseInt(str[2]);
            }

            _getCollectByRadius(200); // 获取圆圈内的收藏点 2015/6/2 14:17
        }
    }

    /**
     * 更新信息 2015/5/28 19:14
     * @param id
     */
    private void _updateInfo(int id) {
        List<LocationEntity> lstLoc = MainActivity.get_db().getDbManagerCommonLoc().getDataById(id);
        if (lstLoc.size() > 0) {
            LocationEntity locationEntity = lstLoc.get(0);
            showCollectLoc(new LatLng(Double.valueOf(locationEntity.getLat()),
                    Double.valueOf(locationEntity.getLng())), locationEntity.getName()
                    + "**" + locationEntity.getLoc() + "**" + locationEntity.getId());
        }
    }

    /**
     * 变更操作按钮状态 2015/5/28 18:52
     */
    private Handler _handlerBtnOper = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (_isClkCollectMarker) {
                _btnOperLoc.setText("编辑");
            } else {
                _btnOperLoc.setText("收藏");
            }
        }
    };

    /**
     * 显示位置 2015/5/23 11:44
     * @param latLng 坐标
     */
    public void showLoc(LatLng latLng) {
        logger.debug("Show location.Lat:{} and Lng:{}.", latLng.latitude, latLng.longitude);
        if (_marker != null)
            _marker.remove();

        if (_curMarker != null)
            _curMarker.remove();

        _isClkCollectMarker = false;
        _handlerBtnOper.sendEmptyMessage(0); // 变更操作按钮状态 2015/5/28 18:52
        _clkLatLng = latLng;

        MapStatusUpdate mapStatusUpdate_zoom = MapStatusUpdateFactory.zoomTo(18);
        _mBaidumap.setMapStatus(mapStatusUpdate_zoom);
        OverlayOptions overlayOptions_marker = new MarkerOptions().position(latLng).icon(_bitmapDescriptor);
        _curMarker = (Marker) _mBaidumap.addOverlay(overlayOptions_marker);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(_clkLatLng);
        _mBaidumap.animateMapStatus(mapStatusUpdate);

        _getCollectByRadius(200); // 获取圆圈内的收藏点 2015/6/2 14:22
    }

    /**
     * 绘画圆圈 2015/6/2 14:14
     * @param latLng 原点
     * @param radius 半径
     */
    private void _drawCircle(LatLng latLng, int radius) {
        if (_corlorCircle != null)
            _corlorCircle.remove();

        OverlayOptions overlayOptions = new CircleOptions()
                .radius(radius)
                .center(latLng)
                .fillColor(0x000066FF);
//                .fillColor(0x330066FF);

        _corlorCircle = (Circle) _mBaidumap.addOverlay(overlayOptions);
    }

    /**
     * 以当前点为原点，获取半径内的收藏点 2015/6/1 14:38
     * @param radius 半径
     */
    private void _getCollectByRadius(int radius) {

        // 获取当前点
        LatLng latLng;
        int curId = -1;
        if (_clkLatLng == null) {
            latLng = new LatLng(_latLng.latitude, _latLng.longitude);
        } else {
            curId = _chkCollectLocId;
            latLng = new LatLng(_clkLatLng.latitude, _clkLatLng.longitude);
        }

        _drawCircle(latLng, radius); // 画圆 2015/6/2 14:14

        // 计算所有符合当前点的半径内的点
        LatLng tmp;
        Marker marker;
        List<Marker> lstMarker = new ArrayList<Marker>();
        for (Integer id:_mapCollectMarker.keySet()) {
//            if (curId == id) // 排除当前原点 2015/6/1 14:50
//                continue;

            marker = _mapCollectMarker.get(id);
            tmp = marker.getPosition();
            double distance = Utils.getShortDistance(tmp.longitude, tmp.latitude,
                    latLng.longitude, latLng.latitude);
            if (distance <= radius) {
                System.out.println("================  111   " + marker.getTitle());
                System.out.println("================  222   " + tmp.longitude + "," + tmp.latitude);
                System.out.println("================  333   " + distance);
                lstMarker.add(marker);
            }
        }

        System.out.println("================  size  " + lstMarker.size());
    }

    /**
     * 放大 2015/4/23 17:28
     * @param view
     */
    public void OnPlus(View view) {
        logger.debug("Click button map's plus.");
        float zoomLevel = _mBaidumap.getMapStatus().zoom;
        if(zoomLevel <= 18) {
            _mBaidumap.setMapStatus(MapStatusUpdateFactory.zoomIn());
            this._imgBtnPlus.setEnabled(true);
        } else {
            Toast.makeText(this, "已经放至最大！", Toast.LENGTH_SHORT).show();
            this._imgBtnPlus.setEnabled(false);
        }

        this._imgBtnMinus.setEnabled(true);
    }

    /**
     * 缩小 2015/4/23 17:28
     * @param view
     */
    public void OnMinus(View view) {
        logger.debug("Click button map's minus.");
        float zoomLevel = _mBaidumap.getMapStatus().zoom;
        if(zoomLevel > 4) {
            _mBaidumap.setMapStatus(MapStatusUpdateFactory.zoomOut());
            this._imgBtnMinus.setEnabled(true);
        } else {
            Toast.makeText(this, "已经缩至最小！", Toast.LENGTH_SHORT).show();
            this._imgBtnMinus.setEnabled(false);
        }

        this._imgBtnPlus.setEnabled(true);
    }

    /**
     * 操作位置 2015/5/21 18:55
     * @param view
     */
    public void OnOperLoc(View view) {
        logger.debug("Click button add location.");
        Intent intent;
        Bundle bundle;
        if (!_isClkCollectMarker) { // 添加
            intent = new Intent(this, AddLocationActivity.class);
            bundle = new Bundle();
            if (_clkLatLng == null) {
                bundle.putDouble("lat", _latLng.latitude);
                bundle.putDouble("lng", _latLng.longitude);
            } else {
                bundle.putDouble("lat", _clkLatLng.latitude);
                bundle.putDouble("lng", _clkLatLng.longitude);
            }

            bundle.putString("name", _txtName.getText().toString());
            bundle.putString("location", _txtLoc.getText().toString());

            intent.putExtra(Const.DATA, bundle);
            startActivityForResult(intent, AddLocationActivity.ADD_LOCATION_OK);
        } else { // 编辑
            intent = new Intent(this, EditLocationActivity.class);
            List<LocationEntity> lstLoc = MainActivity.get_db().getDbManagerCommonLoc().getDataById(_chkCollectLocId);
            if (lstLoc.size() > 0) {
                LocationEntity locationEntity = lstLoc.get(0);
                bundle = new Bundle();
                bundle.putString(Const.LOCATION, locationEntity.getLoc());
                bundle.putString(Const.DESC, locationEntity.getDesc());
                bundle.putInt(Const.TYPE, locationEntity.getType());
                bundle.putInt(Const.ID, locationEntity.getId());
                bundle.putString(Const.NAME, locationEntity.getName());
                bundle.putString(Const.TEL, locationEntity.getTel());
                bundle.putDouble(Const.LAT, Double.parseDouble(locationEntity.getLat()));
                bundle.putDouble(Const.LNG, Double.parseDouble(locationEntity.getLng()));

                intent.putExtra(Const.DATA, bundle);
                startActivityForResult(intent, EditLocationActivity.EDIT_LOC_OK);
            }
        }
    }

    /**
     * 添加追踪 2015/5/21 18:58
     * @param view
     */
    public void OnTrack(View view) {
        logger.debug("Click button track location.");
//        if (!_isClkCollectMarker) {
//            logger.debug("Not choose track location.");
//            Toast.makeText(getActivity(), "请选择已收藏的位置进行追踪", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // 打开路线规划界面 2015/5/22 15:46
        Intent intent = new Intent(this, BDRoutePlan.class);
        Bundle bundle = new Bundle();
        if (_clkLatLng == null) {
            bundle.putDouble("lat", _latLng.latitude);
            bundle.putDouble("lng", _latLng.longitude);
        } else {
            bundle.putDouble("lat", _clkLatLng.latitude);
            bundle.putDouble("lng", _clkLatLng.longitude);
        }

        // 将Bundle添加到Intent里面
        intent.putExtra(Const.DATA, bundle);
        startActivity(intent);
    }

    /**
     * 添加定位当前位置 2015/5/21 18:58
     * @param view
     */
    public void OnCurLocation(View view) {
        logger.debug("Click button back current location.");
        _locationClient.start();
        _locationClient.requestLocation();
    }

    /**
     * 绑定打开搜索事件 2015/5/23 10:34
     * @param view
     */
    public void OnOpenSearch(View view) {
        logger.debug("Click button open search.");
        Intent intent = new Intent(this, SearchDialogActivity.class);
        Bundle bundle = new Bundle();
        if (_city != null) {
            bundle.putString("city", _city);
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
                case AddLocationActivity.ADD_LOCATION_OK: // 添加数据
                    logger.info("Add success.");
                    _isClkCollectMarker = true;
                    bundle = data.getBundleExtra(Const.DATA);
                    _chkCollectLocId = bundle.getInt(Const.ID); // 获取数据ID
                    _handlerBtnOper.sendEmptyMessage(0); // 变更操作按钮状态 2015/5/28 18:52
                    refreshMarker(); // 刷新标记点
                    break;
                case SearchDialogActivity.SEARCH_RESULT_OK: // 搜查结果
                    bundle = data.getBundleExtra(Const.DATA);
                    if (bundle != null) {
                        double lat = bundle.getDouble(Const.LAT);
                        double lng = bundle.getDouble(Const.LNG);
                        _able(lat, lng, true);
                        showLoc(new LatLng(lat, lng));
                    }

                    break;
                case EditLocationActivity.EDIT_LOC_OK: // 编辑成功
                    logger.info("Edit success.");
                    _isClkCollectMarker = true;
                    _handlerBtnOper.sendEmptyMessage(0); // 变更操作按钮状态 2015/5/28 18:52
                    refreshMarker(); // 刷新标记点
                    _updateInfo(_chkCollectLocId); // 更新详细信息面板 2015/5/28 19:15
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_locationClient != null) {
            _locationClient.stop();
            _locationClient = null;
        }

    }

    /**
     * 地图点击事件 2015/5/21 19:23
     * @param point 点击当前位置的坐标
     */
    @Override
    public void onMapClick(LatLng point) {
        logger.debug("Click button map.");
        _able(point.latitude, point.longitude, true);
        showLoc(point);
    }

    /**
     * 处理地图的图标点击事件 2015/5/21 19:24
     * @param mapPoi
     * @return boolean
     */
    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        logger.debug("Click button map poi.");
        _locName = mapPoi.getName();
        LatLng latLng = mapPoi.getPosition();
        _able(latLng.latitude, latLng.longitude, false);
        showLoc(latLng);

        return false;
    }

    /**
     * 处理位置 2015/5/21 19:42
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            _txtName.setText(_locName);
            _txtLoc.setText(bundle.getString("location")); // 设置位置
//            _txtLoc.setText(bundle.getString("distance") + "米  "
//                    + bundle.getString("location")); // 设置位置
        }
    };

    /**
     * 获取位置 2015/5/21 19:40
     * @param lat 经度
     * @param lng 纬度
     */
    private void _able(final double lat, final double lng, final boolean isNeedName) {
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

                    if (isNeedName)
                        _locName = objAddressComponent.getString("street");

                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("location", objResult.getString("formatted_address"));
                    data.putString("distance", objAddressComponent.getString("distance"));

                    msg.setData(data);
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    Looper.prepare();
                    logger.error("Get address IO error.", e);
                    Toast.makeText(MainActivity.this,
                            "获取百度位置信息异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (JSONException e) {
                    Looper.prepare();
                    logger.error("Get address JSON error.", e);
                    Toast.makeText(MainActivity.this,
                            "解析百度json数据异常。", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        };

        new Thread(runnable).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true); // 放置后台
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取DB 2015/4/17 10:19
     * @return
     */
    public static DB get_db() {
        if (_db == null) {
            _db = new DB(_context);
        }

        return _db;
    }

    /**
     * 获取DB 2015/4/17 10:19
     * @return
     */
    public static DB get_db(Context context) {
        if (_db == null) {
            _db = new DB(context);
        }

        return _db;
    }

    public static Context getMainContext() {
        return _context;
    }
}
