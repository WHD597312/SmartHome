package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.TimeTaskDao;
import com.xinrui.smart.pojo.TimeTask;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by win7 on 2018/3/24.
 */

public class TimeTaskDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private TimeTaskDao timeDao;

    public TimeTaskDaoImpl(Context context){
        this.context=context;
        db=DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        DaoSession session=master.newSession();
        timeDao=session.getTimeTaskDao();

    }
    /**
     * 插入时间段
     * @param timeTask
     * @return
     */
    public boolean insert(TimeTask timeTask){
        long n=timeDao.insert(timeTask);
        return n>0?true:false;
    }
    public void update(TimeTask timeTask){
        timeDao.update(timeTask);
    }
    public TimeTask findById(Long id){
        return timeDao.load(id);
    }


    public void insertTaskTimeList(List<TimeTask> list){
        if (list==null || list.isEmpty()){
            return;
        }
        timeDao.insertInTx(list);
    }
    public TimeTask getTaskTime(Long id){
        return timeDao.load(id);
    }
    public void delete(TimeTask timeTask){
        timeDao.delete(timeTask);
    }
    public List<TimeTask> findWeekAll(long device, int week){
        WhereCondition whereCondition=timeDao.queryBuilder().and(TimeTaskDao.Properties.DeviceId.eq(device),TimeTaskDao.Properties.Week.eq(week));
        return timeDao.queryBuilder().where(whereCondition).list();
    }
    /**
     * 查询所有的TaskTime
     * @return
     */
    public List<TimeTask> findAll(){
        return timeDao.loadAll();
    }
}
