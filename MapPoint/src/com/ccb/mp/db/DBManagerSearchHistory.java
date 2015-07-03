package com.ccb.mp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.baidu.mapapi.model.LatLng;
import com.ccb.mp.activity.poi.SearchLocationEntity;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史信息数据管理 2015/7/3 9:35
 */
public class DBManagerSearchHistory {

    private static Logger logger = LoggerFactory
            .getLogger(DBManagerSearchHistory.class); // 日志对象

    private DBInfo _dbInfo;
    private String table = "search_loc_info";

    public DBManagerSearchHistory(Context context, String dbName) {
        _dbInfo = new DBInfo(context, dbName);
    }

    /**
     * 根据设置条件，获取数据 2015/7/3 9:35
     * @return List<SearchLocationEntity>
     */
    public List<SearchLocationEntity> getData() {
        String sql = "select * from " + table + " where search_sid=? order by time desc";
        return _getData(sql, new String[]{Const.APP_LOGIN_SID});
    }

    /**
     * 获取数据 2015/7/3 9:35
     * @param sql 查询语句
     * @param coditoin 查询条件
     * @return
     */
    private List<SearchLocationEntity> _getData(String sql, String[] coditoin) {
        logger.debug("Get data is sql: {}.", sql);

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, coditoin);
        List<SearchLocationEntity> lstData = new ArrayList<SearchLocationEntity>();
        SearchLocationEntity searchLocationEntity;
        while (cursor.moveToNext()) {
            searchLocationEntity = new SearchLocationEntity();
            searchLocationEntity.setId(cursor.getInt(cursor.getColumnIndex("id")))
                    .setSearchId(cursor.getString(cursor.getColumnIndex("search_sid")))
                    .setLatLng(new LatLng(Double.valueOf(cursor.getString(cursor.getColumnIndex("lat"))),
                            Double.valueOf(cursor.getString(cursor.getColumnIndex("lng")))))
                    .setTime(cursor.getString(cursor.getColumnIndex("time")))
                    .setAddress(cursor.getString(cursor.getColumnIndex("loc")))
                    .setName(cursor.getString(cursor.getColumnIndex("name")))
                    .setCity(cursor.getString(cursor.getColumnIndex("city")));

            lstData.add(searchLocationEntity);
        }

        return lstData;
    }

    /**
     * 添加数据 2015/7/3 9:39
     * @param searchLocationEntity 数据对象
     * @return id
     */
    public long add(SearchLocationEntity searchLocationEntity) {
        logger.debug("Add message search history data.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("search_sid", Const.APP_LOGIN_SID);
        values.put("lat", searchLocationEntity.getLatLng().latitude);
        values.put("lng", searchLocationEntity.getLatLng().longitude);
        values.put("time", searchLocationEntity.getTime());
        values.put("loc", searchLocationEntity.getAddress());
        values.put("name", searchLocationEntity.getName());
        values.put("city", searchLocationEntity.getCity());

        // 数据库执行插入命令
        long id = db.insert(table, null, values);
        if (id > 0)
            logger.debug("Add success.Result is {}.", id);

        db.close();

        return id;
    }

    /**
     * 修改数据 2015/7/2 16:35
     * @param searchLocationEntity 数据对象
     * @return 修改条数
     */
    public int updata(SearchLocationEntity searchLocationEntity) {
        logger.debug("Update message search history data.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("lat", searchLocationEntity.getLatLng().latitude);
        values.put("lng", searchLocationEntity.getLatLng().longitude);
        values.put("time", searchLocationEntity.getTime());
        values.put("loc", searchLocationEntity.getAddress());
        values.put("name", searchLocationEntity.getName());
        values.put("city", searchLocationEntity.getCity());

        int count = db.update(table, values, "id=?",
                new String[]{String.valueOf(searchLocationEntity.getId())});
        if (count > 0)
            logger.debug("Update success.Result is {}.", count);

        db.close();

        return count;
    }

    /**
     * 清空 2015/7/2 16:14
     */
    public void del() {
        logger.debug("Delete message search history.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        db.delete(table, "search_sid=?", new String[]{Const.APP_LOGIN_SID});
        db.close();
    }
}
