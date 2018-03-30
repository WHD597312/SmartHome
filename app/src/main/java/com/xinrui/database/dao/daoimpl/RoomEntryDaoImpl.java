package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.content.Entity;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.RoomEntryDao;
import com.xinrui.database.dao.TaskTimeDao;
import com.xinrui.smart.pojo.RoomEntry;

import java.util.List;

/**
 * Created by win7 on 2018/3/28.
 */

public class RoomEntryDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;

    public RoomEntryDaoImpl(Context context){
        this.context=context;
        db= DBManager.getInstance(context).getReadableDatabase();
        master=new DaoMaster(db);
    }
    public boolean insert(RoomEntry roomEntry){
        long n=0;
        DaoSession session=master.newSession();
        RoomEntryDao roomEntryDao=session.getRoomEntryDao();
        n=roomEntryDao.insert(roomEntry);
        return n>0?true:false;
    }
    public void insertAll(List<RoomEntry> list){
        if (list==null || list.isEmpty()){
            return;
        }
        DaoSession session=master.newSession();
        RoomEntryDao roomEntryDao=session.getRoomEntryDao();
        roomEntryDao.insertInTx(list);
    }
    public void deleteAll(List<RoomEntry> list){
        DaoSession session=master.newSession();
        RoomEntryDao roomEntryDao=session.getRoomEntryDao();
        roomEntryDao.deleteInTx(list);
    }
    public List<RoomEntry> findAll(){
        DaoSession session=master.newSession();
        RoomEntryDao roomEntryDao=session.getRoomEntryDao();
        return roomEntryDao.loadAll();
    }
}