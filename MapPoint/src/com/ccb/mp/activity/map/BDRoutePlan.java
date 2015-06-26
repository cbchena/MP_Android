package com.ccb.mp.activity.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.CommonParams;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.MapParams;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.routeplan.IRouteResultObserver;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams;
import com.baidu.navisdk.model.NaviDataEngine;
import com.baidu.navisdk.model.RoutePlanModel;
import com.baidu.navisdk.model.datastruct.RoutePlanNode;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.util.common.ScreenUtil;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.utils.Const;
import com.knowyou.ky_sdk.utils.KyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * 百度路径规划 2015/3/7 10:45
 */
public class BDRoutePlan extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(BDRoutePlan.class); // 日志对象

    private MapGLSurfaceView _mMapView = null;
    private static RoutePlanModel _mRoutePlanModel = null;

    private static LocationClient _locationClient = null;
    private static Context _context;

    private ImageButton _imgBtnPlus; // 放大
    private ImageButton _imgBtnMinus; // 放小

    public static boolean isInHere = false;

    private static double _startX, _startY, _endX, _endY;
    private static boolean _isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_baidu_routeplan);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        this._imgBtnPlus = (ImageButton) this.findViewById(R.id.btnPlus);
        this._imgBtnMinus = (ImageButton) this.findViewById(R.id.btnMinus);

        _context = this;
        isInHere = true;

        Bundle bundle = this.getIntent().getBundleExtra(Const.DATA);
        routePlan(bundle.getDouble(Const.LAT), bundle.getDouble(Const.LNG)); // 路线规划
    }

    /**
     * 初始化界面 2015/3/7 10:55
     */
    private void _initMapView(Bundle bundle) {
        logger.debug("Initial views.");

        if (Build.VERSION.SDK_INT < 14) {
            BaiduNaviManager.getInstance().destroyNMapView();
        }

        _mMapView = BaiduNaviManager.getInstance().createNMapView(this);
        BNMapController.getInstance().setLevel(14);
        BNMapController.getInstance().setLayerMode(
                MapParams.Const.LayerMode.MAP_LAYER_MODE_BROWSE_MAP);
        _updateCompassPosition();

        // 初始化的时候，将地图控制在这个point
        BNMapController.getInstance().locateWithAnimation((int) (bundle.getDouble(Const.LNG) * 1e5),
                (int) (bundle.getDouble(Const.LAT) * 1e5));

    }

    /**
     * 获取当前位置的Bitmap图
     */
    public static void routePlan(final double endLat, final double endLng) {
        logger.debug("Start route plan.");
//        if (_locationClient != null) {
//            _locationClient = null;
//        }

        _endX = endLat;
        _endY = endLng;
        if (_locationClient == null) {
            _locationClient = new LocationClient(MainActivity.getMainContext());

            // 设置定位条件
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);                                //是否打开GPS
            option.setCoorType("bd09ll");                           //设置返回值的坐标类型。
            option.setProdName("LocationDemo");                     //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
            option.setScanSpan(60000);                        //设置定时定位的时间间隔。单位毫秒

            /**
             * 1、高精度模式定位策略：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
             2、低功耗模式定位策略：该定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
             3、仅用设备模式定位策略：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
             Hight_Accuracy高精度、Battery_Saving低功耗、Device_Sensors仅设备(GPS)
             */
            boolean isNetwork = KyUtils.networkStatusOK(_context);
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
                    Log.i("BDRoutePlan", "LocType: " + location.getLocType());
                    if (location.getLocType() == BDLocation.TypeGpsLocation) { // GPS定位结果
                        isSuccess = true;
                        Log.i("BDRoutePlan", "GpsLocation: " + location.getLatitude() + ", " + location.getLongitude());
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) { // 网络定位结果
                        isSuccess = true;
                        Log.i("BDRoutePlan", "NetWorkLocation: " + location.getLatitude() + ", " + location.getLongitude());
                    }

                    if (isSuccess) {
                        logger.debug("Get local location success.");
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        // 开始规划
                        _startX = lat;
                        _startY = lng;
                        _isStart = true;

                        _start(0, lat, lng);
                    }

                    _locationClient.stop();
                }
            });
        }

        _locationClient.start();
        _locationClient.requestLocation();
    }

    /**
     * 更新指南针位置 2015/3/7 10:55
     */
    private void _updateCompassPosition(){
        int screenW = this.getResources().getDisplayMetrics().widthPixels;
        BNMapController.getInstance().resetCompassPosition(
                screenW - ScreenUtil.dip2px(this, 30),
                ScreenUtil.dip2px(this, 126), -1);
    }

    /**
     * 开始规划 2015/3/7 13:46
     * @param mode 0 规划, 1 导航
     */
    private static void _start(final int mode, double startX, double startY) {
        logger.debug("Start.");

        BNaviPoint startPoint = new BNaviPoint(startY, startX,
                "起点", BNaviPoint.CoordinateType.BD09_MC);

        BNaviPoint endPoint = new BNaviPoint(_endY, _endX,
                "终点", BNaviPoint.CoordinateType.BD09_MC);

        BaiduNaviManager.getInstance().launchNavigator((Activity) _context,
                startPoint,                                      // 起点（可指定坐标系）
                endPoint,                                        // 终点（可指定坐标系）
                RoutePlanParams.NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,       // 算路方式
                true,                                            // 真实导航
                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
                new BaiduNaviManager.OnStartNavigationListener() {                // 跳转监听

                    @Override
                    public void onJumpToNavigator(Bundle configParams) {

                        if (mode == 0) {
                            RoutePlanModel mRoutePlanModel = (RoutePlanModel) NaviDataEngine.getInstance()
                                    .getModel(CommonParams.Const.ModelName.ROUTE_PLAN);

                            BaiduNaviManager.getInstance().dismissWaitProgressDialog();

                            // 规划 2015/3/7 13:46
                            _startCalcRoute(mRoutePlanModel.getStartNode(), mRoutePlanModel.getEndNode());
                        } else { // 导航
                            Intent intent = new Intent(_context, BNavigatorActivity.class);
                            intent.putExtras(configParams);
                            _context.startActivity(intent);
                        }

                    }

                    @Override
                    public void onJumpToDownloader() {
                    }
                });
    }

    /**
     * 开始规划 2015/3/6 18:07
     */
    private static void _startCalcRoute(RoutePlanNode startNode, RoutePlanNode endNode) {
        logger.debug("Start calc route.");

        // 将起终点添加到nodeList
        ArrayList<RoutePlanNode> nodeList = new ArrayList<RoutePlanNode>(2);
        nodeList.add(startNode);
        nodeList.add(endNode);

        BNRoutePlaner.getInstance().setObserver(new RoutePlanObserver((Activity)_context, null));

        // 设置算路方式，最少时间
        BNRoutePlaner.getInstance().setCalcMode(RoutePlanParams.NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME);

        // 设置算路结果回调
        BNRoutePlaner.getInstance().setRouteResultObserver(mRouteResultObserver);

        // 设置起终点并算路
        boolean ret = BNRoutePlaner.getInstance().setPointsToCalcRoute(
                nodeList, CommonParams.NL_Net_Mode.NL_Net_Mode_OnLine);
        if(!ret){
            Toast.makeText(_context, "规划失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 路径规划回调 2015/3/6 17:55
     */
    private static IRouteResultObserver mRouteResultObserver = new IRouteResultObserver() {

        @Override
        public void onRoutePlanYawingSuccess() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRoutePlanYawingFail() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRoutePlanSuccess() {
            // TODO Auto-generated method stub
            BNMapController.getInstance().setLayerMode(
                    MapParams.Const.LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
            _mRoutePlanModel = (RoutePlanModel) NaviDataEngine.getInstance()
                    .getModel(CommonParams.Const.ModelName.ROUTE_PLAN);

            logger.debug("Total distance: {}", _mRoutePlanModel.getTotalDistanceInt()); // 单位: 米
            logger.debug("Total time: {}",  _mRoutePlanModel.getTotalTime()); // 显示：XX分钟
            logger.debug("Total second time: {}", _mRoutePlanModel.getTotalTimeInt()); // 单位: 秒
        }

        @Override
        public void onRoutePlanFail() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onRoutePlanCanceled() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onRoutePlanStart() {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 放大 2015/4/23 17:28
     * @param view
     */
    public void OnPlus(View view) {
        logger.debug("Click button map's plus.");
        float zoomLevel = BNMapController.getInstance().getZoomLevel();
        if(zoomLevel <= 18) {
            zoomLevel++;
            _mMapView.setZoomLevel((int) zoomLevel);
            BNMapController.getInstance().setLevel((int) zoomLevel);
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
        float zoomLevel = BNMapController.getInstance().getZoomLevel();
        if(zoomLevel > 4) {
            zoomLevel--;
            _mMapView.setZoomLevel((int) zoomLevel);
            BNMapController.getInstance().setLevel((int) zoomLevel);
            this._imgBtnMinus.setEnabled(true);
        } else {
            Toast.makeText(this, "已经缩至最小！", Toast.LENGTH_SHORT).show();
            this._imgBtnMinus.setEnabled(false);
        }

        this._imgBtnPlus.setEnabled(true);
    }

    /**
     * 按钮绑定导航事件 2015/3/7 14:03
     * @param view
     */
    public void OnNavigation(View view) {
        _start(1, _startX, _startY);
    }

    /**
     * 按钮绑定返回事件 2015/3/23 14:42
     * @param view
     */
    public void OnBack(View view) {
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        logger.info("Finish.");
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
        isInHere = false;
        _isStart = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        BNRoutePlaner.getInstance().setRouteResultObserver(null);
        ((ViewGroup) (findViewById(R.id.mapview_layout))).removeAllViews();
        BNMapController.getInstance().onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("On resume.");

        Bundle bundle = this.getIntent().getBundleExtra(Const.DATA);
        _initMapView(bundle); // 初始化界面

        ((ViewGroup) (findViewById(R.id.mapview_layout))).addView(_mMapView);
        BNMapController.getInstance().onResume();
        if (_isStart) // 开始过规划，所以恢复的时候，重新显示规划 2015/3/7 14:09
            _start(0, _startX, _startY);
    }
}
