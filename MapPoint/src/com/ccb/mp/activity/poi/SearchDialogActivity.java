package com.ccb.mp.activity.poi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.*;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索窗口 2015/5/23 10:35
 */
public class SearchDialogActivity extends Activity implements
        OnGetPoiSearchResultListener {

    private static Logger logger = LoggerFactory
            .getLogger(SearchDialogActivity.class); // 日志对象

    private PoiSearch mPoiSearch = null;
    private TextView _txtKey; // 输入的关键字
    private String _city; // 当前城市

    private ListView _lstView; // 列表
    private SearchLocListAdapter _mSearchLocListAdapter; // 检索列表适配器

    private ListView _lstViewHistory; // 列表
    private SearchLocListAdapter _mSearchHistoryLocListAdapter; // 检索历史记录列表适配器

    public static final int SEARCH_RESULT_OK = 81; // 搜查结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_search_dialog);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 淡入淡出跳转方式

        Bundle bundle = getIntent().getBundleExtra(Const.DATA);
        if (bundle != null) {
            _city = bundle.getString("city");
        }

        _txtKey = (TextView) this.findViewById(R.id.txtKey);

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        _txtKey.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    _mSearchLocListAdapter.clear();
                    _mSearchLocListAdapter.notifyDataSetChanged();

                    _lstViewHistory.setVisibility(View.VISIBLE);
                    _lstView.setVisibility(View.GONE);
                    return;
                }

                _lstViewHistory.setVisibility(View.GONE);
                _lstView.setVisibility(View.VISIBLE);
                _searchProcess(cs.toString());
            }
        });

        _lstView = (ListView) this.findViewById(R.id.lstItem);
        _lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                logger.debug("On click item.Position is {}.", i);
                SearchLocationEntity searchLocationEntity = _mSearchLocListAdapter.getLocBypositon(i);
                _searchLoc(searchLocationEntity, 1);
            }
        });

        _mSearchLocListAdapter = new SearchLocListAdapter(); // 初始化适配器
        _lstView.setAdapter(_mSearchLocListAdapter);

        _lstViewHistory = (ListView) this.findViewById(R.id.lstHistoryItem);
        _lstViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                logger.debug("On click history item.Position is {}.", i);
                SearchLocationEntity searchLocationEntity = _mSearchHistoryLocListAdapter.getLocBypositon(i);
                _searchLoc(searchLocationEntity, 2);
            }
        });

        _mSearchHistoryLocListAdapter = new SearchLocListAdapter();
        _lstViewHistory.setAdapter(_mSearchHistoryLocListAdapter);

        // 获取数据 2015/7/3 9:45
        _getData();
    }

    /**
     * 查找位置 2015/7/3 9:28
     * @param searchLocationEntity 查找的对象实体
     * @param type 1 添加  2 修改
     */
    private void _searchLoc(SearchLocationEntity searchLocationEntity, int type) {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putDouble(Const.LAT, searchLocationEntity.getLatLng().latitude);
        bundle.putDouble(Const.LNG, searchLocationEntity.getLatLng().longitude);
        intent.putExtra(Const.DATA, bundle);
        setResult(SearchDialogActivity.RESULT_OK, intent);

        // 将查找的位置资料记录到历史信息表 2015/7/3 9:47
        if (type == 1) {
            SearchLocationEntity searchLocEntity = new SearchLocationEntity();
            searchLocEntity.setName(searchLocationEntity.getName())
                    .setCity(searchLocationEntity.getCity())
                    .setAddress(searchLocationEntity.getAddress())
                    .setTime(searchLocationEntity.getTime())
                    .setLatLng(searchLocationEntity.getLatLng());

            long id = MainActivity.get_db().getDbManagerSearchHistory().add(searchLocEntity);
            if (id > 0) { // 添加成功，刷新数据 2015/7/2 16:31
                searchLocEntity.setId((int) id);
                _mSearchHistoryLocListAdapter.addLoc(0, searchLocEntity);
                _mSearchHistoryLocListAdapter.notifyDataSetChanged();
            }
        } else if (type == 2) {
            searchLocationEntity.setTime(String.valueOf(System.currentTimeMillis()));
            int count = MainActivity.get_db().getDbManagerSearchHistory().updata(searchLocationEntity);
            if (count > 0) {
                _getData(); // 刷新数据
            }
        }

        finish();
    }

    /**
     * 获取数据 2015/7/3 9:43
     */
    private void _getData() {
        _lstViewHistory.setVisibility(View.VISIBLE);
        _lstView.setVisibility(View.GONE);

        _mSearchHistoryLocListAdapter.clear();

        List<SearchLocationEntity> lstData = MainActivity.get_db().getDbManagerSearchHistory().getData();
        for(SearchLocationEntity searchLocationEntity:lstData) {
            _mSearchHistoryLocListAdapter.addLoc(searchLocationEntity);
        }

        _mSearchHistoryLocListAdapter.notifyDataSetChanged();
    }

    /**
     * 搜索 2015/5/23 11:00
     */
    private void _searchProcess(String key) {
        logger.debug("Search key is {}.", key);
        if (_city == null)
            return;

        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(_city)
                .keyword(key)
                .pageNum(0));
    }

    /**
     * 清空历史 2015/7/2 16:42
     * @param view
     */
    public void OnClear(View view) {
        logger.debug("On click button to clear search history.");
        MainActivity.get_db().getDbManagerSearchHistory().del();
        _mSearchHistoryLocListAdapter.clear();
        _mSearchHistoryLocListAdapter.notifyDataSetChanged();
    }

    /**
     * 绑定返回事件 2015/5/28 10:32
     * @param view
     */
    public void OnBack(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void finish() {
        logger.info("On finish.");
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 淡入淡出跳转方式
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        logger.debug("Get poi result.");
        if (result == null
                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            logger.debug("Get poi not error.");
            _mSearchLocListAdapter.clear();
            for (PoiInfo poiInfo : result.getAllPoi()) {
                if (poiInfo.type == PoiInfo.POITYPE.POINT) { // 必须是一个地点
                    SearchLocationEntity searchLocationEntity = new SearchLocationEntity();
                    searchLocationEntity.setName(poiInfo.name)
                            .setAddress(poiInfo.address)
                            .setCity(poiInfo.city)
                            .setLatLng(poiInfo.location)
                            .setTime(String.valueOf(System.currentTimeMillis()));

                    _mSearchLocListAdapter.addLoc(searchLocationEntity);
                }
            }

            _mSearchLocListAdapter.notifyDataSetChanged();
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            logger.debug("Get poi not found.");

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(SearchDialogActivity.this, strInfo, Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(SearchDialogActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(SearchDialogActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * 列表适配器 2015/5/18 16:40
     */
    private class SearchLocListAdapter extends BaseAdapter {
        private ArrayList<SearchLocationEntity> mLoc;
        private LayoutInflater mInflator;

        public SearchLocListAdapter() {
            super();
            mLoc = new ArrayList<SearchLocationEntity>();
            mInflator = SearchDialogActivity.this.getLayoutInflater();
        }

        public void addLoc(SearchLocationEntity searchLocationEntity) {
            mLoc.add(searchLocationEntity);
        }

        public void addLoc(int idx, SearchLocationEntity searchLocationEntity) {
            mLoc.add(idx, searchLocationEntity);
        }

        public SearchLocationEntity getLocBypositon(int position) {
            return mLoc.get(position);
        }

        public void clear() {
            mLoc.clear();
        }

        @Override
        public int getCount() {
            return mLoc.size();
        }

        @Override
        public Object getItem(int i) {
            return mLoc.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_search_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtName = (TextView) view
                        .findViewById(R.id.name);
                viewHolder.txtLoc = (TextView) view
                        .findViewById(R.id.loc);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }



            final SearchLocationEntity searchLocationEntity = mLoc.get(i);
            viewHolder.txtName.setText(searchLocationEntity.getName());
            viewHolder.txtLoc.setText(searchLocationEntity.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtLoc;
    }
}
