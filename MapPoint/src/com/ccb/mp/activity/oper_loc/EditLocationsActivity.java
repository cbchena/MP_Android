package com.ccb.mp.activity.oper_loc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ccb.mp.R;
import com.ccb.mp.activity.main.MainActivity;
import com.ccb.mp.activity.oper_loc.entity.LocationEntity;
import com.ccb.mp.task.activity_manager.ActivityManager;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑所有位置界面 2015/5/22 11:05
 */
public class EditLocationsActivity extends Activity{

    private static Logger logger = LoggerFactory
            .getLogger(EditLocationsActivity.class); // 日志对象

    private ListView _lstView; // 列表
    private CommonLocListAdapter _mCommonLocListAdapter; // 蓝牙列表适配器
    public static final int EDIT_LOCATOINS_OK = 83; // 编辑界面成功返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.info("On create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_edit_locs);
        ActivityManager.getInstance().addActivity(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout); // 淡入淡出跳转方式

        _lstView = (ListView) this.findViewById(R.id.lstItem);
        _lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                logger.debug("On click item.Position is {}.", i);
            }
        });

        _mCommonLocListAdapter = new CommonLocListAdapter(); // 初始化适配器
        _lstView.setAdapter(_mCommonLocListAdapter);

        _refreshUI(); // 刷新数据
    }

    /**
     * 刷新UI 2015/5/22 11:13
     */
    private void _refreshUI() {
        logger.debug("Refresh ui.");

        _mCommonLocListAdapter.clear(); // 清除

        // 刷新数据  PS：后续需要做分类 2015/5/22 11:04
        List<LocationEntity> lstLocs = ChoCmnTypeDialogActivity
                .sortByType(MainActivity.get_db().getDbManagerCommonLoc().getData());
        for(LocationEntity locationEntity:lstLocs) {
            _mCommonLocListAdapter.addLoc(locationEntity);
        }

        _mCommonLocListAdapter.notifyDataSetChanged();
    }

    /**
     * 清空历史 2015/7/2 16:42
     * @param view
     */
    public void OnClear(View view) {
        logger.debug("On click button to clear search history.");
        MainActivity.get_db().getDbManagerCommonLoc().del();
        _mCommonLocListAdapter.clear();
        _mCommonLocListAdapter.notifyDataSetChanged();
    }

    /**
     * 绑定返回事件 2015/5/21 15:59
     * @param view
     */
    public void OnBack(View view) {
        logger.debug("On click button to back.");
        setResult(EditLocationsActivity.RESULT_OK, getIntent()); // 设置返回结果
        finish(); // 结束当前Activity
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(EditLocationsActivity.RESULT_OK, getIntent()); // 设置返回结果
            finish(); // 结束当前Activity
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug("On activity result is {}.", resultCode);
        if(resultCode == RESULT_OK) {  //返回成功
            switch (requestCode) {
                case EditLocationActivity.EDIT_LOC_OK: // 编辑成功
                    logger.info("Edit success.");
                    _refreshUI();
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

    /**
     * 列表适配器 2015/5/18 16:40
     */
    private class CommonLocListAdapter extends BaseAdapter {
        private ArrayList<LocationEntity> mLoc;
        private LayoutInflater mInflator;

        public CommonLocListAdapter() {
            super();
            mLoc = new ArrayList<LocationEntity>();
            mInflator = EditLocationsActivity.this.getLayoutInflater();
        }

        public void addLoc(LocationEntity locationEntity) {
            mLoc.add(locationEntity);
        }

        public LocationEntity getLocBypositon(int position) {
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
                view = mInflator.inflate(R.layout.mp_edit_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.txtName = (TextView) view
                        .findViewById(R.id.name);
                viewHolder.txtLoc = (TextView) view
                        .findViewById(R.id.loc);
                viewHolder.btnEdit = (Button) view
                        .findViewById(R.id.btnEdit);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }



            final LocationEntity locationEntity = mLoc.get(i);
            viewHolder.txtName.setText(locationEntity.getName());
            viewHolder.txtLoc.setText(locationEntity.getLoc());
            viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logger.debug("On click button to edit location.");
                    Intent intent = new Intent(EditLocationsActivity.this,
                            EditLocationActivity.class);
                    Bundle bundle = new Bundle();
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
            });

            return view;
        }
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtLoc;
        Button btnEdit;
    }
}
