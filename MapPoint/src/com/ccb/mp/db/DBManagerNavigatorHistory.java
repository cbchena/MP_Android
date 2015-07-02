package com.ccb.mp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ccb.mp.activity.oper_loc.entity.NavigatorEntity;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 导航历史信息数据管理 2015/7/2 15:59
 */
public class DBManagerNavigatorHistory {

    private static Logger logger = LoggerFactory
            .getLogger(DBManagerNavigatorHistory.class); // 日志对象

    private DBInfo _dbInfo;
    private String table = "navigator_config_info";

    public DBManagerNavigatorHistory(Context context, String dbName) {
        _dbInfo = new DBInfo(context, dbName);
    }

    /**
     * 根据设置条件，获取数据 2015/7/2 16:11
     * @return List<NavigatorEntity>
     */
    public List<NavigatorEntity> getData() {
        String sql = "select * from " + table + " where search_sid=? order by time desc";
        return _getData(sql, new String[]{Const.APP_LOGIN_SID});
    }

    /**
     * 获取数据 2015/7/2 16:02
     * @param sql 查询语句
     * @param coditoin 查询条件
     * @return
     */
    private List<NavigatorEntity> _getData(String sql, String[] coditoin) {
        logger.debug("Get data is sql: {}.", sql);

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, coditoin);
        List<NavigatorEntity> lstData = new ArrayList<NavigatorEntity>();
        NavigatorEntity navigatorEntity;
        while (cursor.moveToNext()) {
            navigatorEntity = new NavigatorEntity();
            navigatorEntity.setId(cursor.getInt(cursor.getColumnIndex("id")))
                    .setSearchId(cursor.getString(cursor.getColumnIndex("search_sid")))
                    .setLat(cursor.getString(cursor.getColumnIndex("lat")))
                    .setLng(cursor.getString(cursor.getColumnIndex("lng")))
                    .setTime(cursor.getString(cursor.getColumnIndex("time")))
                    .setSrcLoc(cursor.getString(cursor.getColumnIndex("src_loc")))
                    .setDecLoc(cursor.getString(cursor.getColumnIndex("dec_loc")));

            lstData.add(navigatorEntity);
        }

        return lstData;
    }

    /**
     * 添加数据 2015/7/2 16:11
     * @param navigatorEntity 数据对象
     * @return id
     */
    public long add(NavigatorEntity navigatorEntity) {
        logger.debug("Add message navigator history data.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("search_sid", Const.APP_LOGIN_SID);
        values.put("lat", navigatorEntity.getLat());
        values.put("lng", navigatorEntity.getLng());
        values.put("time", navigatorEntity.getTime());
        values.put("src_loc", navigatorEntity.getSrcLoc());
        values.put("dec_loc", navigatorEntity.getDecLoc());

        // 数据库执行插入命令
        long id = db.insert(table, null, values);
        if (id > 0)
            logger.debug("Add success.Result is {}.", id);

        db.close();

        return id;
    }

    /**
     * 修改数据 2015/7/2 16:35
     * @param navigatorEntity 数据对象
     * @return 修改条数
     */
    public int updata(NavigatorEntity navigatorEntity) {
        logger.debug("Update message navigator history data.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("lat", navigatorEntity.getLat());
        values.put("lng", navigatorEntity.getLng());
        values.put("time", navigatorEntity.getTime());
        values.put("src_loc", navigatorEntity.getSrcLoc());
        values.put("dec_loc", navigatorEntity.getDecLoc());

        int count = db.update(table, values, "id=?",
                new String[]{String.valueOf(navigatorEntity.getId())});
        if (count > 0)
            logger.debug("Update success.Result is {}.", count);

        db.close();

        return count;
    }

    /**
     * 清空 2015/7/2 16:14
     */
    public void del() {
        logger.debug("Delete message navigator history.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        db.delete(table, "search_sid=?", new String[]{Const.APP_LOGIN_SID});
        db.close();
    }
}
