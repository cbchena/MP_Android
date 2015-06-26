package com.ccb.mp.db;

import android.content.Context;
import com.ccb.mp.utils.Const;

/**
 * 数据库管理 2015/3/5 16:12
 */
public class DB {

    private DBManagerCommonLoc dbManagerCommonLoc; // 常用地点管理

    public DB(Context context) {
        if (dbManagerCommonLoc == null)
            dbManagerCommonLoc = new DBManagerCommonLoc(context, Const.DB_NAME);
    }

    public DBManagerCommonLoc getDbManagerCommonLoc() {
        return dbManagerCommonLoc;
    }
}
