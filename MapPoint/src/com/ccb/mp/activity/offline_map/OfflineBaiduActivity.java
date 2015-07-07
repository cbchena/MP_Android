package com.ccb.mp.activity.offline_map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.ccb.mp.R;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度离线地图 2015/5/26 11:23
 */
public class OfflineBaiduActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(OfflineBaiduActivity.class); // 日志对象

    public static boolean isHere = false; // 是否在当前界面

    private ExpandableListView _lstView; // 列表
    private ListView _lstUpdateItem; // 下载管理列表
    private CityListAdapter _mCityListAdapter; // 二级城市适配器
    private UpdateListAdapter _updateListAdapter; // 下载城市适配器
    private Button _btnDwnManager; // 下载管理
    private Button _btnCityList; // 城市列表

    private Map<Integer, CityEntity> _mapDownload; // 已经下载的城市
    private Map<Integer, CityEntity> _mapDwning; // 正在下载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.mp_city_main);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        isHere = true;

        // 初始化离线地图对象 2015/5/26 11:56
        OfflineMap.getInstance(this).init();

        _btnDwnManager = (Button) this.findViewById(R.id.btnDwnManager); // 下载管理
        _btnCityList = (Button) this.findViewById(R.id.btnCityList); // 城市列表

        _lstView = (ExpandableListView) this.findViewById(R.id.lstItem);
        _mCityListAdapter = new CityListAdapter();
        _lstView.setAdapter(_mCityListAdapter);
        _lstView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() { // 点击子城市
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                CityEntity cityEntity = (CityEntity) _mCityListAdapter.getChild(i, i2);
                logger.debug("On click item is {}.", cityEntity.getCityName());
                if (!_mapDownload.containsKey(cityEntity.getCityID())
                        && !_mapDwning.containsKey(cityEntity.getCityID())) { // 判断是否存在，不存在就启动下载

                    // 启动下载指定城市ID的离线地图
                    OfflineMap.getInstance(OfflineBaiduActivity.this).start(cityEntity.getCityID());
                    _mapDwning.put(cityEntity.getCityID(), cityEntity);
                } else if (_mapDwning.containsKey(cityEntity.getCityID())) {
                    Toast.makeText(OfflineBaiduActivity.this, "该城市正在下载", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OfflineBaiduActivity.this, "该城市已经下载", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

        _lstUpdateItem = (ListView) this.findViewById(R.id.lstUpdateItem);
        _updateListAdapter = new UpdateListAdapter();
        _lstUpdateItem.setAdapter(_updateListAdapter);
        _lstUpdateItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CityEntity cityEntity = _updateListAdapter.getCityBypositon(i);
                logger.debug("On click item is {}.", cityEntity.getCityName());

                Intent intent = new Intent(OfflineBaiduActivity.this,
                        DelDialogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Const.ID, cityEntity.getCityID());
                intent.putExtra(Const.DATA, bundle);
                startActivityForResult(intent, DelDialogActivity.DEL_RESULT_OK);
            }
        });

        _mapDownload = new HashMap<Integer, CityEntity>();
        _mapDwning = new HashMap<Integer, CityEntity>();

        // 设置所有已经下载的城市列表
        setDownloadList();

        // 设置离线下载的城市列表
        setOfflineCityList();
    }

    /**
     * 设置所有已经下载的城市列表 2015/5/25 15:35
     */
    private void setDownloadList() {
        logger.debug("Set download list.");
        ArrayList<MKOLUpdateElement> allUpdateInfo = OfflineMap.getInstance(this).getAllUpdateInfo();
        if (allUpdateInfo == null)
            return;

        for(MKOLUpdateElement mkolUpdateElement:allUpdateInfo) {
            if (mkolUpdateElement.cityID == OfflineMap.NOW_DOWNLOAD_CITY_ID) // 判断是否正在下载
                continue;

            CityEntity cityEntity = new CityEntity();
            cityEntity.setCityID(mkolUpdateElement.cityID)
                    .setCityName(mkolUpdateElement.cityName);

            _mapDownload.put(mkolUpdateElement.cityID, cityEntity);
            _updateListAdapter.addCity(cityEntity);
        }

        _updateListAdapter.notifyDataSetChanged();
    }

    /**
     * 设置所有支持离线下载的城市列表 2015/5/25 14:27
     */
    private void setOfflineCityList() {
        logger.debug("Set offline city list.");
        ArrayList<MKOLSearchRecord> offlineCityList = OfflineMap.getInstance(this).getOfflineCityList();
        for(MKOLSearchRecord mkolSearchRecord:offlineCityList) { // 遍历所有支持离线下载的城市
            CityEntity cityEntity = new CityEntity();
            cityEntity.setCityID(mkolSearchRecord.cityID)
                    .setCityName(mkolSearchRecord.cityName)
                    .setSize(mkolSearchRecord.size);
            List<CityEntity> lstChildCities = new ArrayList<CityEntity>();

//            mOfflineMap.remove(mkolSearchRecord.cityID); // 删除指定城市ID的离线地图
            if (mkolSearchRecord.childCities != null) { // 判断是否有子城市
                cityEntity.setChildCities(true);
                for (MKOLSearchRecord mkolSearchRecord1 : mkolSearchRecord.childCities) { // 遍历子城市
                    CityEntity childCityEntity = new CityEntity();
                    childCityEntity.setCityID(mkolSearchRecord1.cityID)
                            .setCityName(mkolSearchRecord1.cityName)
                            .setChildCities(false)
                            .setSize(mkolSearchRecord1.size);
                    lstChildCities.add(childCityEntity);

//                    mOfflineMap.remove(mkolSearchRecord1.cityID); // 删除指定城市ID的离线地图
                }
            } else { // 将本身添加进去
                cityEntity.setChildCities(false);
                CityEntity childCityEntity = new CityEntity();
                childCityEntity.setCityID(cityEntity.getCityID())
                        .setCityName(cityEntity.getCityName())
                        .setChildCities(false)
                        .setSize(cityEntity.getSize());

                lstChildCities.add(childCityEntity);
            }

            _mCityListAdapter.addCity(cityEntity);
            _mCityListAdapter.addSubCityList(lstChildCities);
        }

        _mCityListAdapter.notifyDataSetChanged();
    }

    /**
     * 下载更新 2015/5/26 11:47
     * @param update 更新对象
     */
    public void downloadUpdate(MKOLUpdateElement update) {
        CityEntity cityEntity = _mCityListAdapter.getSubCity(update.cityID);
        cityEntity.setRatio(update.ratio);
        if (update.ratio == 100) { // 下载完成 2015/5/26 10:57
            CityEntity city = new CityEntity();
            city.setCityID(update.cityID)
                    .setCityName(update.cityName);

            _mapDownload.put(update.cityID, city);
            _updateListAdapter.addCity(city);
            _updateListAdapter.notifyDataSetChanged();
            _mapDwning.remove(update.cityID);
        }

        _mCityListAdapter.notifyDataSetChanged();
    }

    /**
     * 格式化大小 2015/5/26 11:31
     * @param size
     * @return
     */
    private String _formatDataSize(int size) {
        logger.debug("Size is {}.", size);
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    /**
     * 下载管理 2015/5/26 10:28
     * @param view
     */
    public void OnDownloadManager(View view) {
        logger.debug("On click button to download manager.");
        _btnDwnManager.setBackgroundResource(R.drawable.register_get_code_btn_bg);
        _btnDwnManager.setEnabled(false);

        _btnCityList.setBackgroundResource(R.drawable.register_icon_bg);
        _btnCityList.setEnabled(true);

        _lstUpdateItem.setVisibility(View.VISIBLE);
        _lstView.setVisibility(View.GONE);

        _updateListAdapter.notifyDataSetChanged();
    }

    /**
     * 城市列表 2015/5/26 10:28
     * @param view
     */
    public void OnCityList(View view) {
        logger.debug("On click button to city list.");
        _btnCityList.setBackgroundResource(R.drawable.register_get_code_btn_bg);
        _btnCityList.setEnabled(false);

        _btnDwnManager.setBackgroundResource(R.drawable.register_icon_bg);
        _btnDwnManager.setEnabled(true);

        _lstUpdateItem.setVisibility(View.GONE);
        _lstView.setVisibility(View.VISIBLE);

        _mCityListAdapter.notifyDataSetChanged();

    }

    /**
     * 返回 2015/5/26 14:17
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug("On activity result is {}.", resultCode);
        if(resultCode == RESULT_OK) {  //返回成功
            switch (requestCode) {
                case DelDialogActivity.DEL_RESULT_OK: // 删除成功
                    logger.info("Delete success.");
                    Bundle bundle = data.getBundleExtra(Const.DATA);
                    if (bundle != null) {
                        int cityId = bundle.getInt(Const.ID);
                        boolean result = OfflineMap.getInstance(OfflineBaiduActivity.this)
                                .remove(cityId);
                        if (result) {
                            logger.info("Delete city id is {}.", cityId);
                            _mapDownload.remove(cityId);
                            _updateListAdapter.removeCity(cityId);
                            _updateListAdapter.notifyDataSetChanged();
                            CityEntity cityEntity = _mCityListAdapter.getSubCity(cityId);
                            if (cityEntity != null)
                                cityEntity.setRatio(0);

                            _mCityListAdapter.notifyDataSetChanged();
                        }
                    }

                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 列表适配器 2015/5/18 16:40
     */
    private class CityListAdapter extends BaseExpandableListAdapter {
        private ArrayList<CityEntity> mCityEntity;
        private ArrayList<List<CityEntity>> mSubCityEntity;
        private LayoutInflater mInflator;

        public CityListAdapter() {
            super();
            mCityEntity = new ArrayList<CityEntity>();
            mSubCityEntity = new ArrayList<List<CityEntity>>();
            mInflator = OfflineBaiduActivity.this.getLayoutInflater();
        }

        public void addCity(CityEntity cityEntity) {
            mCityEntity.add(cityEntity);
        }

        public void addSubCityList(List<CityEntity> lstCitys) {
            mSubCityEntity.add(lstCitys);
        }

        public CityEntity getSubCity(int cityID) {
            for(List<CityEntity> lstSub:mSubCityEntity) {
                for(CityEntity cityEntity:lstSub) {
                    if (cityEntity.getCityID() == cityID)
                        return cityEntity;
                }
            }

            return null;
        }

        @Override
        public int getGroupCount() {
            return mCityEntity.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return mSubCityEntity.get(i).size();
        }

        @Override
        public Object getGroup(int i) {
            return mCityEntity.get(i);
        }

        @Override
        public Object getChild(int i, int i2) {
            return mSubCityEntity.get(i).get(i2);
        }

        @Override
        public long getGroupId(int i) {
            return mCityEntity.get(i).getCityID();
        }

        @Override
        public long getChildId(int i, int i2) {
            return mSubCityEntity.get(i).get(i2).getCityID();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_city_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtName = (TextView) view
                        .findViewById(R.id.name);
                viewHolder.imageView = (ImageView) view
                        .findViewById(R.id.icon);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            CityEntity cityEntity = mCityEntity.get(i);
            viewHolder.txtName.setText(cityEntity.getCityName());
            if (b) { // 判断是否扩展
                viewHolder.imageView.setImageResource(R.drawable.icon_2);
            } else {
                viewHolder.imageView.setImageResource(R.drawable.icon_1);
            }

            return view;
        }

        @Override
        public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_city_sub_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtName = (TextView) view
                        .findViewById(R.id.name);
                viewHolder.txtStatus = (TextView) view
                        .findViewById(R.id.txtStatus);
                viewHolder.txtSize = (TextView) view
                        .findViewById(R.id.txtSize);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            CityEntity cityEntity = mSubCityEntity.get(i).get(i2);
            viewHolder.txtName.setText(cityEntity.getCityName());
//            if (b) { // 判断是否扩展
//                System.out.println("已扩展");
//            } else {
//                System.out.println("未扩展");
//            }

            viewHolder.txtSize.setText(_formatDataSize(cityEntity.getSize()));

            if (cityEntity.getRatio() == 0 || cityEntity.getRatio() == 100) { // 判断是否正在下载 2015/5/25 16:50
                if (_mapDownload.containsKey(cityEntity.getCityID())) { // 判断是否已经下载
                    viewHolder.txtStatus.setText("已下载");
                    viewHolder.txtName.setTextColor(0xffc3c3c3);
                    viewHolder.txtStatus.setTextColor(0xffc3c3c3);
                } else {
                    viewHolder.txtStatus.setText("未下载");
                    viewHolder.txtName.setTextColor(0xff000000);
                    viewHolder.txtStatus.setTextColor(0xff000000);
                }
            } else {
                viewHolder.txtStatus.setText(cityEntity.getRatio() + "%");
            }

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return true;
        }
    }

    /**
     * 列表适配器 2015/5/26 10:22
     */
    private class UpdateListAdapter extends BaseAdapter {
        private ArrayList<CityEntity> mCity;
        private LayoutInflater mInflator;

        public UpdateListAdapter() {
            super();
            mCity = new ArrayList<CityEntity>();
            mInflator = OfflineBaiduActivity.this.getLayoutInflater();
        }

        public void addCity(CityEntity cityEntity) {
            mCity.add(cityEntity);
        }

        public void removeCity(int cityId) {
            CityEntity cityEntity;
            boolean isFound = false;
            int i;
            for (i = 0; i < mCity.size(); i++) {
                cityEntity = mCity.get(i);
                if (cityEntity.getCityID() == cityId) {
                    isFound = true;
                    break;
                }
            }

            if (isFound) // 判断是否找到
                mCity.remove(i);
        }

        public CityEntity getCityBypositon(int position) {
            return mCity.get(position);
        }

        public void clear() {
            mCity.clear();
        }

        @Override
        public int getCount() {
            return mCity.size();
        }

        @Override
        public Object getItem(int i) {
            return mCity.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_city_update_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtName = (TextView) view
                        .findViewById(R.id.name);
                viewHolder.txtStatus = (TextView) view
                        .findViewById(R.id.txtStatus);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            CityEntity cityEntity = mCity.get(i);
            viewHolder.txtName.setText(cityEntity.getCityName());

//            if (cityEntity.getRatio() == 100) { // 判断是否正在下载 2015/5/25 16:50
            if (_mapDownload.containsKey(cityEntity.getCityID())) { // 判断是否已经下载
                viewHolder.txtStatus.setText("完成");
            }
//            } else {
//                viewHolder.txtStatus.setText(cityEntity.getRatio() + "%");
//            }

            return view;
        }
    }


    static class ViewHolder {
        TextView txtName;
        TextView txtStatus;
        TextView txtSize;
        ImageView imageView;
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        isHere = false;
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }

}
