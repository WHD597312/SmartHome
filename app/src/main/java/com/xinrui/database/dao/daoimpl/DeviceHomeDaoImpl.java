package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.DeviceHomeDao;
import com.xinrui.smart.pojo.DeviceHome;

/**
 * Created by win7 on 2018/3/22.
 */

public class DeviceHomeDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    public DeviceHomeDaoImpl(Context context) {
        this.context = context;
        db=DBManager.getInstance(context).getWritableDasebase();
       master=new DaoMaster(db);
    }
    public boolean insert(DeviceHome home){
        long n=0;
        DaoSession session=master.newSession();
        DeviceHomeDao deviceHomeDao=session.getDeviceHomeDao();
        n=deviceHomeDao.insert(home);
        return n>0?true:false;
    }
}
