package com.ccb.mp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ccb.mp.activity.oper_loc.LocationEntity;
import com.ccb.mp.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用地点表管理 2015/5/21 20:23
 */
public class DBManagerCommonLoc {

    private static Logger logger = LoggerFactory
            .getLogger(DBManagerCommonLoc.class); // 日志对象

    private DBInfo _dbInfo; // 紧急信息表
    private String table = "common_location_info";

    public DBManagerCommonLoc(Context context, String dbName) {
        _dbInfo = new DBInfo(context, dbName);
    }

    /**
     * 获取数据条数 2015/5/21 20:23
     * @return 数量
     */
    public int getCount(String search_sid) {
        String sql = "select count(*) from " + table + " where search_sid=?";
        logger.debug("Get count is sql: {}.", sql);

        SQLiteDatabase db = _dbInfo.getReadableDatabase();

        // 查询注定用户Id的条数
        Cursor cursor = db.rawQuery(sql, new String[]{search_sid});

        //游标移到第一条记录准备获取数据
        cursor.moveToFirst();

        return (int) cursor.getLong(0); // 获取数据中的LONG类型数据
    }

    /**
     * 根据指定type获取数据条数 2015/5/21 20:23
     * @return 数量
     */
    public int getCountByType(String search_sid, int type) {
        String sql = "select count(*) from " + table + " where search_sid=? and type=?";
        logger.debug("Get count is sql: {}.", sql);

        SQLiteDatabase db = _dbInfo.getReadableDatabase();

        // 查询注定用户Id的条数
        Cursor cursor = db.rawQuery(sql, new String[]{search_sid, String.valueOf(type)});

        //游标移到第一条记录准备获取数据
        cursor.moveToFirst();

        return (int) cursor.getLong(0); // 获取数据中的LONG类型数据
    }

    /**
     * 根据设置条件，获取数据 2015/3/5 15:20
     * @return List<LocationEntity>
     */
    public List<LocationEntity> getData() {
        String sql = "select * from " + table + " where search_sid=? order by time asc";
        return _getData(sql, new String[]{Const.APP_LOGIN_SID});
    }

    /**
     * 根据设置条件，获取数据 2015/3/5 15:20
     * @param type 类型
     * @return List<LocationEntity>
     */
    public List<LocationEntity> getDataByType(int type) {
        String sql = "select * from " + table + " where search_sid=? and type=? order by time asc";
        return _getData(sql, new String[]{Const.APP_LOGIN_SID, String.valueOf(type)});
    }

    /**
     * 根据设置条件，获取数据 2015/3/5 15:20
     * @param id  id
     * @return List<LocationEntity>
     */
    public List<LocationEntity> getDataById(int id) {
        String sql = "select * from " + table + " where search_sid=? and id=?";
        return _getData(sql, new String[]{Const.APP_LOGIN_SID, String.valueOf(id)});
    }

    /**
     * 获取数据 2015/3/6 10:06
     * @param sql 查询语句
     * @param coditoin 查询条件
     * @return
     */
    private List<LocationEntity> _getData(String sql, String[] coditoin) {
        logger.debug("Get data is sql: {}.", sql);

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, coditoin);
        List<LocationEntity> lstData = new ArrayList<LocationEntity>();
        LocationEntity locationEntity;
        while(cursor.moveToNext()){ // 利用游标遍历所有数据对象
            locationEntity = new LocationEntity();
            locationEntity.setId(cursor.getInt(cursor.getColumnIndex("id")))
                    .setSearchId(cursor.getString(cursor.getColumnIndex("search_sid")))
                    .setTime(cursor.getString(cursor.getColumnIndex("time")))
                    .setLoc(cursor.getString(cursor.getColumnIndex("loc")))
                    .setLat(cursor.getString(cursor.getColumnIndex("lat")))
                    .setLng(cursor.getString(cursor.getColumnIndex("lng")))
                    .setType(cursor.getInt(cursor.getColumnIndex("type")))
                    .setDesc(cursor.getString(cursor.getColumnIndex("desc")))
                    .setName(cursor.getString(cursor.getColumnIndex("name")))
                    .setTel(cursor.getString(cursor.getColumnIndex("tel")));

            lstData.add(locationEntity);
        }

        db.close();

        return lstData;
    }

    /**
     * 保存数据 2015/5/21 20:40
     */
    public long add(LocationEntity locationEntity) {
        logger.debug("Add message common location.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();

        // 创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();
        values.put("time", locationEntity.getTime());
        values.put("loc", locationEntity.getLoc());
        values.put("lat", locationEntity.getLat());
        values.put("lng", locationEntity.getLng());
        values.put("type", locationEntity.getType());
        values.put("desc", locationEntity.getDesc());
        values.put("name", locationEntity.getName());
        values.put("tel", locationEntity.getTel());
        values.put("search_sid", Const.APP_LOGIN_SID);

        //数据库执行插入命令
        long id = db.insert(table, null, values);
        if (id > 0)
            logger.debug("Add success.Result is {}.", id);

        db.close();

        return id;
    }

    /**
     * 修改数据 2015/2/4 17:35
     * @param locationEntity 数据对象
     */
    public int update(LocationEntity locationEntity) {
        logger.debug("Update message common location.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();

        // 创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();
        values.put("time", locationEntity.getTime());
        values.put("loc", locationEntity.getLoc());
        values.put("type", locationEntity.getType());
        values.put("desc", locationEntity.getDesc());
        values.put("name", locationEntity.getName());
        values.put("tel", locationEntity.getTel());

        int count = db.update(table, values, "id=?",
                new String[]{String.valueOf(locationEntity.getId())});
        if (count > 0)
            logger.debug("Update success.Result is {}.", count);

        db.close();

        return count;
    }

    /**
     * 删除数据 2015/3/28 12:21
     * @param id
     */
    public int deleteDataById(String id) {
        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        logger.debug("Delete data.It is id={}.", Const.APP_LOGIN_SID);

        //数据库执行插入命令
        int count = db.delete(table, "search_sid=? and id=?", new String[]{Const.APP_LOGIN_SID, id});
        if (count > 0)
            logger.debug("Delete success.");

        db.close();

        return count;
    }

    public void del() {
        logger.debug("Delete message common location.");

        SQLiteDatabase db = _dbInfo.getReadableDatabase();
        db.delete(table, "", new String[]{});
        db.close();
    }
}
