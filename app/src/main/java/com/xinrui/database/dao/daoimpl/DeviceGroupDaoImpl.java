package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.DeviceGroupDao;
import com.xinrui.smart.pojo.DeviceGroup;

import java.util.List;

public class DeviceGroupDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private DeviceGroupDao deviceGroupDao;
    public DeviceGroupDaoImpl(Context context){
        this.context=context;
        db= DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        DaoSession session=master.newSession();
        deviceGroupDao=session.getDeviceGroupDao();
    }
    public void insert(DeviceGroup deviceGroup){
        long n=deviceGroupDao.insert(deviceGroup);
    }
    public void insertAll(List<DeviceGroup> deviceGroups){
        deviceGroupDao.insertInTx(deviceGroups);
    }
    public void update(DeviceGroup deviceGroup){
        deviceGroupDao.update(deviceGroup);
    }
    /**清空所有的用户数据*/
    public void deleteAll(){
        deviceGroupDao.deleteAll();
    }
    public DeviceGroup findById(Long id){
        return deviceGroupDao.load(id);
    }
    public List<DeviceGroup> findAllDevices(){
        return deviceGroupDao.loadAll();
    }
}
