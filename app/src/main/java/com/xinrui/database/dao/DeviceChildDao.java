package com.xinrui.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xinrui.smart.pojo.DeviceChild;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DEVICE_CHILD".
*/
public class DeviceChildDao extends AbstractDao<DeviceChild, Long> {

    public static final String TABLENAME = "DEVICE_CHILD";

    /**
     * Properties of entity DeviceChild.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DeviceName = new Property(1, String.class, "deviceName", false, "DEVICE_NAME");
        public final static Property MacAddress = new Property(2, String.class, "macAddress", false, "MAC_ADDRESS");
        public final static Property Img = new Property(3, int.class, "img", false, "IMG");
        public final static Property Direction = new Property(4, int.class, "direction", false, "DIRECTION");
        public final static Property HouseId = new Property(5, Long.class, "houseId", false, "HOUSE_ID");
        public final static Property MasterControllerUserId = new Property(6, int.class, "masterControllerUserId", false, "MASTER_CONTROLLER_USER_ID");
        public final static Property Controlled = new Property(7, int.class, "controlled", false, "CONTROLLED");
        public final static Property Type = new Property(8, int.class, "type", false, "TYPE");
        public final static Property IsUnlock = new Property(9, int.class, "isUnlock", false, "IS_UNLOCK");
        public final static Property Version = new Property(10, int.class, "version", false, "VERSION");
    }


    public DeviceChildDao(DaoConfig config) {
        super(config);
    }
    
    public DeviceChildDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEVICE_CHILD\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DEVICE_NAME\" TEXT," + // 1: deviceName
                "\"MAC_ADDRESS\" TEXT," + // 2: macAddress
                "\"IMG\" INTEGER NOT NULL ," + // 3: img
                "\"DIRECTION\" INTEGER NOT NULL ," + // 4: direction
                "\"HOUSE_ID\" INTEGER," + // 5: houseId
                "\"MASTER_CONTROLLER_USER_ID\" INTEGER NOT NULL ," + // 6: masterControllerUserId
                "\"CONTROLLED\" INTEGER NOT NULL ," + // 7: controlled
                "\"TYPE\" INTEGER NOT NULL ," + // 8: type
                "\"IS_UNLOCK\" INTEGER NOT NULL ," + // 9: isUnlock
                "\"VERSION\" INTEGER NOT NULL );"); // 10: version
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE_CHILD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DeviceChild entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(2, deviceName);
        }
 
        String macAddress = entity.getMacAddress();
        if (macAddress != null) {
            stmt.bindString(3, macAddress);
        }
        stmt.bindLong(4, entity.getImg());
        stmt.bindLong(5, entity.getDirection());
 
        Long houseId = entity.getHouseId();
        if (houseId != null) {
            stmt.bindLong(6, houseId);
        }
        stmt.bindLong(7, entity.getMasterControllerUserId());
        stmt.bindLong(8, entity.getControlled());
        stmt.bindLong(9, entity.getType());
        stmt.bindLong(10, entity.getIsUnlock());
        stmt.bindLong(11, entity.getVersion());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DeviceChild entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(2, deviceName);
        }
 
        String macAddress = entity.getMacAddress();
        if (macAddress != null) {
            stmt.bindString(3, macAddress);
        }
        stmt.bindLong(4, entity.getImg());
        stmt.bindLong(5, entity.getDirection());
 
        Long houseId = entity.getHouseId();
        if (houseId != null) {
            stmt.bindLong(6, houseId);
        }
        stmt.bindLong(7, entity.getMasterControllerUserId());
        stmt.bindLong(8, entity.getControlled());
        stmt.bindLong(9, entity.getType());
        stmt.bindLong(10, entity.getIsUnlock());
        stmt.bindLong(11, entity.getVersion());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DeviceChild readEntity(Cursor cursor, int offset) {
        DeviceChild entity = new DeviceChild( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // deviceName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // macAddress
            cursor.getInt(offset + 3), // img
            cursor.getInt(offset + 4), // direction
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // houseId
            cursor.getInt(offset + 6), // masterControllerUserId
            cursor.getInt(offset + 7), // controlled
            cursor.getInt(offset + 8), // type
            cursor.getInt(offset + 9), // isUnlock
            cursor.getInt(offset + 10) // version
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DeviceChild entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDeviceName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setMacAddress(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setImg(cursor.getInt(offset + 3));
        entity.setDirection(cursor.getInt(offset + 4));
        entity.setHouseId(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setMasterControllerUserId(cursor.getInt(offset + 6));
        entity.setControlled(cursor.getInt(offset + 7));
        entity.setType(cursor.getInt(offset + 8));
        entity.setIsUnlock(cursor.getInt(offset + 9));
        entity.setVersion(cursor.getInt(offset + 10));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DeviceChild entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DeviceChild entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DeviceChild entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
