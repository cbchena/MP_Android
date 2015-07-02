package com.ccb.mp.db;

import android.content.Context;
import com.ccb.mp.utils.Const;

/**
 * 数据库管理 2015/3/5 16:12
 */
public class DB {

    private DBManagerCommonLoc dbManagerCommonLoc; // 常用地点管理
    private DBManagerNavigatorHistory dbManagerNavigatorHistory; // 导航历史信息管理

    public DB(Context context) {
        if (dbManagerCommonLoc == null)
            dbManagerCommonLoc = new DBManagerCommonLoc(context, Const.DB_NAME);

        if (dbManagerNavigatorHistory == null)
            dbManagerNavigatorHistory = new DBManagerNavigatorHistory(context, Const.DB_NAME);
    }

    public DBManagerCommonLoc getDbManagerCommonLoc() {
        return dbManagerCommonLoc;
    }

    public DBManagerNavigatorHistory getDbManagerNavigatorHistory() {
        return dbManagerNavigatorHistory;
    }
}
