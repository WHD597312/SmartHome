package com.xinrui.database.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.xinrui.smart.pojo.DeviceHome;
import com.xinrui.smart.pojo.TaskTime;

import com.xinrui.database.dao.DeviceHomeDao;
import com.xinrui.database.dao.TaskTimeDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig deviceHomeDaoConfig;
    private final DaoConfig taskTimeDaoConfig;

    private final DeviceHomeDao deviceHomeDao;
    private final TaskTimeDao taskTimeDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        deviceHomeDaoConfig = daoConfigMap.get(DeviceHomeDao.class).clone();
        deviceHomeDaoConfig.initIdentityScope(type);

        taskTimeDaoConfig = daoConfigMap.get(TaskTimeDao.class).clone();
        taskTimeDaoConfig.initIdentityScope(type);

        deviceHomeDao = new DeviceHomeDao(deviceHomeDaoConfig, this);
        taskTimeDao = new TaskTimeDao(taskTimeDaoConfig, this);

        registerDao(DeviceHome.class, deviceHomeDao);
        registerDao(TaskTime.class, taskTimeDao);
    }
    
    public void clear() {
        deviceHomeDaoConfig.clearIdentityScope();
        taskTimeDaoConfig.clearIdentityScope();
    }

    public DeviceHomeDao getDeviceHomeDao() {
        return deviceHomeDao;
    }

    public TaskTimeDao getTaskTimeDao() {
        return taskTimeDao;
    }

}
