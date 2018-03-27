package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.TaskTimeDao;
import com.xinrui.smart.pojo.TaskTime;

import java.util.List;

/**
 * Created by win7 on 2018/3/24.
 */

public class TimeTaskDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;

    public TimeTaskDaoImpl(Context context){
        this.context=context;
        db=DBManager.getInstance(context).getReadableDatabase();
        master=new DaoMaster(db);
    }
    /**
     * 插入时间段
     * @param timeTask
     * @return
     */
    public boolean insert(TaskTime timeTask){
        long n=0;
        DaoSession session=master.newSession();
        TaskTimeDao timeDao=session.getTaskTimeDao();
        n=timeDao.insert(timeTask);
        return n>0?true:false;
    }

    public void insertTaskTimeList(List<TaskTime> list){
        if (list==null || list.isEmpty()){
            return;
        }
        DaoSession session=master.newSession();
        TaskTimeDao timeDao=session.getTaskTimeDao();
        timeDao.insertInTx(list);
    }
    public TaskTime getTaskTime(Long id){
        DaoSession session=master.newSession();
        TaskTimeDao timeDao=session.getTaskTimeDao();
        return timeDao.load(id);
    }
    public void delete(TaskTime taskTime){
        DaoSession session=master.newSession();
        TaskTimeDao timeDao=session.getTaskTimeDao();
        timeDao.delete(taskTime);
    }
    public List<TaskTime> findWeekAll(String week){
        DaoSession session=master.newSession();
        TaskTimeDao timeDao=session.getTaskTimeDao();
        return timeDao.queryBuilder().where(TaskTimeDao.Properties.Week.eq(week)).list();
    }
    /**
     * 查询所有的TaskTime
     * @return
     */
    public List<TaskTime> findAll(){
        DaoSession session=master.newSession();
        TaskTimeDao taskTimeDao=session.getTaskTimeDao();
        return taskTimeDao.loadAll();
    }
}
