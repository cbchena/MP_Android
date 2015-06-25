package com.ccb.mp.activity.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.ccb.mp.R;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.knowyou.ky_sdk.utils.KyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主界面 2015/6/25 15:01
 */
public class MainActivity extends Activity implements BaiduMap.OnMapClickListener {

    private static Logger logger = LoggerFactory
            .getLogger(MainActivity.class); // 日志对象

    private MapView _mMapView = null;    // 地图View
    private BaiduMap _mBaidumap = null;
    private BitmapDescriptor _bitmapDescriptor; // 标注的图标

    private LocationClient _locationClient = null;
    private LatLng _latLng; // 当前位置
    private Marker _marker; // 当前位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_main);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

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

                return false;
            }
        });

        // 显示的坐标icon
        _bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map_point);

        // 显示当前位置 2015/6/25 17:01
        _showCurrentLoc();
    }

    /**
     * 显示当前位置 2015/6/25 17:01
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
                    _latLng = new LatLng(lat, lng); // 当前位置 2015/6/25 17:01

                    MapStatusUpdate mapStatusUpdate_zoom = MapStatusUpdateFactory.zoomTo(18);
                    _mBaidumap.setMapStatus(mapStatusUpdate_zoom);
                    OverlayOptions overlayOptions_marker = new MarkerOptions().position(_latLng).icon(_bitmapDescriptor);
                    _marker = (Marker) _mBaidumap.addOverlay(overlayOptions_marker);
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(_latLng);
                    _mBaidumap.animateMapStatus(mapStatusUpdate);
                }
            }
        });

        _locationClient.start();
        _locationClient.requestLocation();
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
     * 点击地图点 2015/6/25 16:56
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {

    }

    /**
     * 点击地图图标 2015/6/25 16:56
     * @param mapPoi
     * @return
     */
    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }
}
