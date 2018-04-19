package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.TaskTimeDao;
import com.xinrui.smart.pojo.TaskTime;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by win7 on 2018/3/24.
 */

public class TimeTaskDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private TaskTimeDao timeDao;

    public TimeTaskDaoImpl(Context context){
        this.context=context;
        db=DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        DaoSession session=master.newSession();
        timeDao=session.getTaskTimeDao();

    }
    /**
     * 插入时间段
     * @param timeTask
     * @return
     */
    public void insert(TaskTime timeTask){
        timeDao.insert(timeTask);
    }

    public void insertTaskTimeList(List<TaskTime> list){
        if (list==null || list.isEmpty()){
            return;
        }
        timeDao.insertInTx(list);
    }
    public TaskTime getTaskTime(Long id){
        return timeDao.load(id);
    }
    public void delete(TaskTime taskTime){
        timeDao.delete(taskTime);
    }
    public List<TaskTime> findWeekAll(long device,int week){
        WhereCondition whereCondition=timeDao.queryBuilder().and(TaskTimeDao.Properties.DeviceId.eq(device),TaskTimeDao.Properties.Week.eq(week));
        return timeDao.queryBuilder().where(whereCondition).list();
    }
    /**
     * 查询所有的TaskTime
     * @return
     */
    public List<TaskTime> findAll(){
        return timeDao.loadAll();
    }
}
