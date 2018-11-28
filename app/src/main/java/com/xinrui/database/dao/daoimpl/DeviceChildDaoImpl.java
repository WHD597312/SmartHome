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

import java.util.ArrayList;
import java.util.List;

public class DeviceChildDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private DeviceChildDao deviceChildDao;
    private DaoSession session;
    public DeviceChildDaoImpl(Context context) {
        this.context = context;
        db= DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        session=master.newSession();
       deviceChildDao=session.getDeviceChildDao();
    }

    /**
     * 添加设备
     * @param deviceChild
     */
    public void insert(DeviceChild deviceChild){
        deviceChildDao.insert(deviceChild);
    }
    public void insertAll(List<DeviceChild> deviceChildren){
        deviceChildDao.insertInTx(deviceChildren);
    }

    /**
     * 更新设备
     * @param deviceChild
     */
    public void update(DeviceChild deviceChild){
        deviceChildDao.update(deviceChild);
    }

    /**
     * 批量更新设备
     * @param deviceChildren
     */
    public void updateAll(List<DeviceChild> deviceChildren){
        deviceChildDao.updateInTx(deviceChildren);
    }
    /**清空用户所有的设备*/
    public void deleteAll(){
        deviceChildDao.deleteAll();
    }

    /**
     * 删除某个家的所有设备
     * @param deviceChildren
     */
    public void deleteGroupDevice(List<DeviceChild> deviceChildren){
        deviceChildDao.deleteInTx(deviceChildren);
    }

    /**
     * 删除设备
     * @param deviceChild
     */
    public void delete(DeviceChild deviceChild){
        deviceChildDao.delete(deviceChild);
    }

    /**
     * 删除某个家中的所有设备
     * @param deviceChildren
     */
    public void deleteGroup(List<DeviceChild> deviceChildren){
        deviceChildDao.deleteInTx(deviceChildren);
    }

    /**
     * 根据设备Id查询固定设备
     * @param id
     * @return
     */
    public DeviceChild findDeviceChild(Long id){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.Id.eq(id)).unique();
    }

    /**
     * 获取设备类型下的设备 controlled=2时表示设备为主控 controlled=1时表示设备为受控 controlled=0表示外置传感器
     * @param controlled
     * @return
     */
    public List<DeviceChild> findDeviceControl(Long groupId,int type,int controlled){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(groupId),DeviceChildDao.Properties.Type.eq(type),DeviceChildDao.Properties.Controlled.notEq(controlled));
        return deviceChildDao.queryBuilder().where(whereCondition).list();
    }

    //        return deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.GroupId.eq(groupId))

    /**
     *
     * @param houseId
     * @param type
     * @return
     */
    public List<DeviceChild> findDeviceType(Long houseId,int type){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(houseId),DeviceChildDao.Properties.Type.eq(type));
        return deviceChildDao.queryBuilder().where(whereCondition).list();
    }

    /**
     * 查询某个家里面设备类型为1且是在线的设备
     * @param houseId
     * @param type
     * @param online
     * @return
     */
    public List<DeviceChild> findDeviceType(Long houseId,int type,boolean online){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(houseId),DeviceChildDao.Properties.Type.eq(type),DeviceChildDao.Properties.OnLint.eq(online));
        return deviceChildDao.queryBuilder().where(whereCondition).list();
    }

    /**
     * 根据macAddress查询某一个设备
     * @param macAddress
     * @return
     */
    public DeviceChild findDeviceByMacAddress2(String macAddress){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.MacAddress.eq(macAddress)).unique();
    }

    /**
     * 查询某个家的主控设备
     * @param houseId
     * @param type
     * @param controlled
     * @return
     */
    public DeviceChild findMainControlDevice(long houseId,int type,int controlled){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(houseId),DeviceChildDao.Properties.Type.eq(type),DeviceChildDao.Properties.Controlled.eq(controlled));
        return deviceChildDao.queryBuilder().where(whereCondition).unique();
    }

    /**
     * 查询某个家的外置传感器
     * @param houseId
     * @param type
     * @param controlled
     * @return
     */
    public DeviceChild findEstControlDevice(long houseId,int type,int controlled){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(houseId),DeviceChildDao.Properties.Type.eq(type),DeviceChildDao.Properties.Controlled.eq(controlled));
        return deviceChildDao.queryBuilder().where(whereCondition).unique();
    }

    /**
     * 根据macAddress来查询设备
     * @param macAddress
     * @return
     */
    public List<DeviceChild> findDeviceByMacAddress(String macAddress){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.MacAddress.eq(macAddress)).list();
    }

    /**
     * 批量删除设备
     * @param list
     */
    public void deleteDevices(List<DeviceChild> list){
        deviceChildDao.deleteInTx(list);
    }

    /**
     * 获取在house下面类型为1，在线的且机械类型不是M的设备
     * @param houseId
     * @param type
     * @param online
     * @param machAttr
     * @return
     */
    public List<DeviceChild> findDeviceMayControl(long houseId,int type,boolean online,String machAttr){
        WhereCondition whereCondition=deviceChildDao.queryBuilder().and(DeviceChildDao.Properties.HouseId.eq(houseId),DeviceChildDao.Properties.Type.eq(type),DeviceChildDao.Properties.OnLint.eq(online),DeviceChildDao.Properties.MachAttr.notEq(machAttr));
        return deviceChildDao.queryBuilder().where(whereCondition).orderAsc(DeviceChildDao.Properties.Id).list();
    }


    /**
     * 查询某个家里面的所有设备
     * @param groupId
     * @return
     */
    public List<DeviceChild> findGroupIdAllDevice(Long groupId){
        List<DeviceChild> children=deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.HouseId.eq(groupId)).orderAsc(DeviceChildDao.Properties.Id).list();
        return children;
    }

    /**
     * 根据设备Id查询固定设备
     * @param id
     * @return
     */
    public DeviceChild findDeviceById(Long id){
        return deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.Id.eq(id)).unique();
    }

    /**
     * 查询所有设备
     * @return
     */
    public List<DeviceChild> findAllDevice(){
        return deviceChildDao.loadAll();
    }

    /**
     * 查询设备类型为0的所有设备
     * @param type
     * @return
     */
    public List<DeviceChild> findZerosType(int type){
        List<DeviceChild> children=deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.Type.eq(type)).orderAsc(DeviceChildDao.Properties.Id).list();
        return children;
    }

    /**
     * 关闭数据库本次对话
     */
    public void closeDaoSession(){
        if (session!=null){
            session.clear();
            session=null;
        }
    }

    /**
     * 查询某个家中的所有设备
     * @param group
     * @return
     */
    public List<DeviceChild> findHouseDevices(int group){
        List<DeviceChild> deviceChildren=deviceChildDao.queryBuilder().where(DeviceChildDao.Properties.GroupPosition.eq(group)).orderAsc(DeviceChildDao.Properties.Id).list();
        return deviceChildren;
    }
    public List<Long> findAllDeviceKey(Long groupId){
        List<Long> list=new ArrayList<>();
        List<DeviceChild> deviceChildren=findGroupIdAllDevice(groupId);
        for (int i = 0; i < deviceChildren.size(); i++) {
            list.add(deviceChildren.get(i).getId());
        }
        return list;
    }
}
