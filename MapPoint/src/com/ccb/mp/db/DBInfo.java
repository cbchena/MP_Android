package com.ccb.mp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聊天信息表 2015/2/3 9:37
 */
public class DBInfo extends SQLiteOpenHelper{

    private static Logger logger = LoggerFactory
            .getLogger(DBInfo.class); // 日志对象

    private static final int VERSION = 1;

    // 带全部参数的构造函数，此构造函数必不可少
    public DBInfo(Context context, String name, CursorFactory factory,
                  int version) {
        super(context, name, factory, version);

    }

    // 带两个参数的构造函数，调用的其实是带三个参数的构造函数
    public DBInfo(Context context, String name){
        this(context, name, VERSION);
    }

    // 带三个参数的构造函数，调用的是带所有参数的构造函数
    public DBInfo(Context context, String name, int version){
        this(context, name, null,version);
    }

    //创建数据库
    public void onCreate(SQLiteDatabase db) {
        logger.info("Create a Database");

        String sqlLocation = "create table IF NOT EXISTS common_location_info(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "search_sid varchar(50), lat varchar(50), lng varchar(50), time varchar(50), loc varchar(500), " +
                "name varchar(50), tel varchar(50), desc varchar(500), type int)";

        //执行创建数据库操作
        db.execSQL(sqlLocation);
    }

    /**
     * 更换版本时调用 2015/3/5 16:28
     * @param db
     * @param oldVersion  旧版本号
     * @param newVersion  新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //创建成功，日志输出提示
        logger.info("Update a Database");
//        db.execSQL("alter table msg_urgent_info add search_sid varchar(50) default 16");

//        String sqlLocation = "create table IF NOT EXISTS common_location_info(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "search_sid varchar(50), lat varchar(50), lng varchar(50), time varchar(50), loc varchar(500), " +
//                "name varchar(50), tel varchar(50), desc varchar(500), type int)";
//
//        // 执行创建数据库操作
//        db.execSQL(sqlLocation);
    }

}

