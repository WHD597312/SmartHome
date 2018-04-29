package com.xinrui.database.dao.daoimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xinrui.database.dao.DBManager;
import com.xinrui.database.dao.DaoMaster;
import com.xinrui.database.dao.DaoSession;
import com.xinrui.database.dao.TimeTaskDao;
import com.xinrui.database.dao.TimerDao;
import com.xinrui.smart.pojo.Timer;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

public class TimeDaoImpl {
    private Context context;
    private SQLiteDatabase db;
    private DaoMaster master;
    private TimerDao timerDao;

    public TimeDaoImpl(Context context) {
        this.context = context;
        db= DBManager.getInstance(context).getWritableDasebase();
        master=new DaoMaster(db);
        DaoSession session=master.newSession();
        timerDao=session.getTimerDao();
    }
    public void insert(Timer timer){
        timerDao.insert(timer);
    }

    public List<Timer> findAll(long deviceId,int week){
        WhereCondition whereCondition=timerDao.queryBuilder().and(TimerDao.Properties.DeviceId.eq(deviceId),TimerDao.Properties.Week.eq(week));
        return timerDao.queryBuilder().where(whereCondition).list();
    }
    public void deleteAll(long deviceId,int week){
        List<Timer> timers=findAll(deviceId,week);
        timerDao.deleteInTx(timers);
    }
    public void updateAll(List<Timer> timers){
        timerDao.updateInTx(timers);
    }
}
