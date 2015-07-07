package com.ccb.mp.activity.offline_map;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * 离线地图 2015/5/26 11:33
 */
public class OfflineMap {

    private static Logger logger = LoggerFactory
            .getLogger(OfflineMap.class); // 日志对象

    private static OfflineMap _offlineMap;
    private static OfflineBaiduActivity _context;
    public static int NOW_DOWNLOAD_CITY_ID = 0; // 正在下载的城市ID

    /**
     * 离线地图功能
     */
    private static MKOfflineMap mOfflineMap;

    /**
     * 离线地图单例模式 2015/5/26 11:33
     * @param context 上下文环境
     * @return OfflineMap
     */
    public static OfflineMap getInstance(final OfflineBaiduActivity context) {
        if (_offlineMap == null) {
            _offlineMap = new OfflineMap();
            mOfflineMap = new MKOfflineMap();
        }

        _context = context;

        return _offlineMap;
    }

    /**
     * 初始化 2015/5/26 11:52
     */
    public void init() {
        mOfflineMap.init(new MKOfflineMapListener() { // 设置监听
            @Override
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: // 离线地图下载更新
                        if (!OfflineBaiduActivity.isHere)
                            return;

                        MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
                        NOW_DOWNLOAD_CITY_ID = update.cityID; // 记录正在下载的城市id
                        if (update.ratio == 100)
                            NOW_DOWNLOAD_CITY_ID = 0;

                        logger.debug("Update name:{}.Update progress:{}.", update.cityName, update.ratio);
                        _context.downloadUpdate(update);
                        break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        logger.debug("有新的离线地图。");
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE: // 版本更新提示
                        logger.debug("版本更新提示。");
                        break;
                }
            }
        });
    }

    /**
     * 获取所有已下载的城市 2015/5/26 11:51
     * @return
     */
    public ArrayList<MKOLUpdateElement> getAllUpdateInfo() {
        return mOfflineMap.getAllUpdateInfo();
    }

    /**
     * 获得所有支持离线下载的城市 2015/5/26 11:54
     * @return
     */
    public ArrayList<MKOLSearchRecord> getOfflineCityList() {
        return mOfflineMap.getOfflineCityList();
    }

    /**
     * 启动下载 2015/5/26 11:53
     * @param cityId 城市id
     * @return
     */
    public boolean start(int cityId) {
        return mOfflineMap.start(cityId);
    }

    /**
     * 删除地图 2015/5/26 14:32
     * @param cityId 城市id
     * @return
     */
    public boolean remove(int cityId) {
        return mOfflineMap.remove(cityId);
    }

    /**
     * 销毁 2015/5/26 14:20
     */
    public static void onDestroy() {
        if (mOfflineMap != null)
            mOfflineMap.destroy();
    }

}
