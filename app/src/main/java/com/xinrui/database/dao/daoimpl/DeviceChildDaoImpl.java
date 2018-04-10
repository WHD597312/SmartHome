package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.DeviceChildDao;
import com.xinrui.smart.pojo.DeviceChild;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

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
    public void insertAll(List<DeviceChild> deviceChildren){
        deviceChildDao.insertInTx(deviceChildren);
    }
    public void update(DeviceChild deviceChild){
        deviceChildDao.update(deviceChild);
    }
    /**清空用户所有的设备*/
    public void deleteAll(){
        deviceChildDao.deleteAll();
    }
    public void delete(DeviceChild deviceChild){
        deviceChildDao.delete(deviceChild);
    }
    public DeviceChild findDeviceChild(Long id){
        return deviceChildDao.load(id);
    }

    /**
     * 获取设备类型下的设备 controlled=2时表示设备为主控 controlled=1时表示设备为受控 controlled=0表示外置传感器
     * @param controlled
     * @return
     */
    public List<DeviceChild> findDeviceType(Long groupId,int controlled){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(groupId),DeviceChildDao.Properties.Controlled.eq(controlled));
        return deviceChildDao.queryBuilder().where(whereCondition).list();
    }
//        return deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.GroupId.eq(groupId))
    public List<DeviceChild> findGroupIdAllDevice(Long groupId){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.HouseId.eq(groupId)).list();
    }
    public List<DeviceChild> findAllDevice(){
        return deviceChildDao.loadAll();
    }
}