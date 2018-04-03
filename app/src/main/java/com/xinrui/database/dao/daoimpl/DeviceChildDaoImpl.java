package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.DeviceChildDao;
import com.xinrui.smart.pojo.DeviceChild;

import java.util.List;

public class DeviceChildDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private DeviceChildDao deviceChildDao;
    public DeviceChildDaoImpl(Context context) {
        this.context = context;
        db= DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        DaoSession session=master.newSession();
        deviceChildDao=session.getDeviceChildDao();
    }
    public void insert(DeviceChild deviceChild){
        deviceChildDao.insert(deviceChild);
    }
    public void update(DeviceChild deviceChild){
        deviceChildDao.update(deviceChild);
    }
    public void delete(DeviceChild deviceChild){
        deviceChildDao.delete(deviceChild);
    }
    public List<DeviceChild> findGroupIdAllDevice(Long groupId){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.GroupId.eq(groupId)).list();
    }
}
