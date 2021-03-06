package com.xinrui.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xinrui.smart.pojo.TimeTask;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TIME_TASK".
*/
public class TimeTaskDao extends AbstractDao<TimeTask, Long> {

    public static final String TABLENAME = "TIME_TASK";

    /**
     * Properties of entity TimeTask.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DeviceId = new Property(1, long.class, "deviceId", false, "DEVICE_ID");
        public final static Property Week = new Property(2, int.class, "week", false, "WEEK");
        public final static Property Start = new Property(3, int.class, "start", false, "START");
        public final static Property End = new Property(4, int.class, "end", false, "END");
        public final static Property Temp = new Property(5, int.class, "temp", false, "TEMP");
    }


    public TimeTaskDao(DaoConfig config) {
        super(config);
    }
    
    public TimeTaskDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TIME_TASK\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"DEVICE_ID\" INTEGER NOT NULL ," + // 1: deviceId
                "\"WEEK\" INTEGER NOT NULL ," + // 2: week
                "\"START\" INTEGER NOT NULL ," + // 3: start
                "\"END\" INTEGER NOT NULL ," + // 4: end
                "\"TEMP\" INTEGER NOT NULL );"); // 5: temp
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TIME_TASK\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TimeTask entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDeviceId());
        stmt.bindLong(3, entity.getWeek());
        stmt.bindLong(4, entity.getStart());
        stmt.bindLong(5, entity.getEnd());
        stmt.bindLong(6, entity.getTemp());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TimeTask entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDeviceId());
        stmt.bindLong(3, entity.getWeek());
        stmt.bindLong(4, entity.getStart());
        stmt.bindLong(5, entity.getEnd());
        stmt.bindLong(6, entity.getTemp());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public TimeTask readEntity(Cursor cursor, int offset) {
        TimeTask entity = new TimeTask( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // deviceId
            cursor.getInt(offset + 2), // week
            cursor.getInt(offset + 3), // start
            cursor.getInt(offset + 4), // end
            cursor.getInt(offset + 5) // temp
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TimeTask entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDeviceId(cursor.getLong(offset + 1));
        entity.setWeek(cursor.getInt(offset + 2));
        entity.setStart(cursor.getInt(offset + 3));
        entity.setEnd(cursor.getInt(offset + 4));
        entity.setTemp(cursor.getInt(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(TimeTask entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(TimeTask entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TimeTask entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
