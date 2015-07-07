package com.ccb.mp.activity.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ccb.mp.R;
import com.ccb.mp.activity.offline_map.OfflineBaiduActivity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心 2015/7/6 14:22
 */
public class MeActivity extends Activity {

    private static Logger logger = LoggerFactory
            .getLogger(MeActivity.class); // 日志对象

    private static final int ITEM_EXIT = 1; // 退出
    private static final int ITEM_CLEAR = 2; // 清空数据
    private static final int ITEM_OFFLINE_MAP = 3; // 离线地图

    private ListView _lvItem; // 显示消息列表
    private MeMainItemAdapter _itemAdapter; // 适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_me_main);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        this._lvItem = (ListView) this.findViewById(R.id.lstItem);
        this._lvItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MeItemEntity meItemEntity = _itemAdapter.getItemBypositon(i);
                Intent intent;
                switch (meItemEntity.getId()) {
                    case ITEM_EXIT: // 退出
                        intent = new Intent();
                        intent.setClass(MeActivity.this, ExitDialogActivity.class);
                        startActivityForResult(intent, ExitDialogActivity.EXIT_RESULT_OK);
                        break;
                    case ITEM_CLEAR: // 重置图点
                        startActivity(new Intent(MeActivity.this, ResetMapPointActivity.class));
                        break;
                    case ITEM_OFFLINE_MAP: // 离线地图
                        startActivity(new Intent(MeActivity.this, OfflineBaiduActivity.class));
                        break;
                }
            }
        });

        _itemAdapter = new MeMainItemAdapter();
        this._lvItem.setAdapter(this._itemAdapter); // 设置适配器

        _init();
    }

    /**
     * 初始化 2015/7/6 14:46
     */
    private void _init() {
        logger.debug("initial item data.");
        List<MeItemEntity> lstData = _getData();
        for(MeItemEntity meItemEntity:lstData) {
            _itemAdapter.addItem(meItemEntity);
        }

        _itemAdapter.notifyDataSetChanged();
    }

    /**
     * 获取数据 2015/7/6 14:42
     */
    private List<MeItemEntity> _getData() {
        logger.debug("Get adapter data.");

        List<MeItemEntity> lstData = new ArrayList<MeItemEntity>();
        MeItemEntity itemEntity;

        itemEntity = new MeItemEntity();
        itemEntity.setId(ITEM_CLEAR);
        itemEntity.setTitle("重置图点");
        lstData.add(itemEntity);

        itemEntity = new MeItemEntity();
        itemEntity.setId(ITEM_OFFLINE_MAP);
        itemEntity.setTitle("离线地图");
        lstData.add(itemEntity);

        itemEntity = new MeItemEntity();
        itemEntity.setId(ITEM_EXIT);
        itemEntity.setTitle("退出");
        lstData.add(itemEntity);

        return lstData;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug("On activity result code is {} and request code is {}.", resultCode, requestCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ExitDialogActivity.EXIT_RESULT_OK:
                    ActivityManager.getInstance().exit();
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 返回事件 2015/7/6 14:35
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        finish();
    }

    @Override
    public void finish() {
        logger.info("Finish.");
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式
    }

    /**
     * 列表适配器 2015/7/6 14:41
     */
    private class MeMainItemAdapter extends BaseAdapter {
        private ArrayList<MeItemEntity> _lstItems;
        private LayoutInflater mInflator;

        public MeMainItemAdapter() {
            super();
            _lstItems = new ArrayList<MeItemEntity>();
            mInflator = MeActivity.this.getLayoutInflater();
        }

        public void addItem(MeItemEntity meItemEntity) {
            _lstItems.add(meItemEntity);
        }

        public MeItemEntity getItemBypositon(int position) {
            return _lstItems.get(position);
        }

        public void clear() {
            _lstItems.clear();
        }

        @Override
        public int getCount() {
            return _lstItems.size();
        }

        @Override
        public Object getItem(int i) {
            return _lstItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.mp_me_main_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView) view
                        .findViewById(R.id.title);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }



            MeItemEntity meItemEntity = _lstItems.get(i);
            viewHolder.txtTitle.setText(meItemEntity.getTitle());

            return view;
        }
    }

    static class ViewHolder {
        TextView txtTitle;
    }
}
