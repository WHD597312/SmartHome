package com.xinrui.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xinrui.smart.pojo.DeviceGroup;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DEVICE_GROUP".
*/
public class DeviceGroupDao extends AbstractDao<DeviceGroup, Long> {

    public static final String TABLENAME = "DEVICE_GROUP";

    /**
     * Properties of entity DeviceGroup.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserId = new Property(1, int.class, "userId", false, "USER_ID");
        public final static Property Header = new Property(2, String.class, "header", false, "HEADER");
        public final static Property Footer = new Property(3, String.class, "footer", false, "FOOTER");
        public final static Property Color = new Property(4, int.class, "color", false, "COLOR");
        public final static Property HouseName = new Property(5, String.class, "houseName", false, "HOUSE_NAME");
        public final static Property Location = new Property(6, String.class, "location", false, "LOCATION");
        public final static Property MasterControllerDeviceId = new Property(7, int.class, "masterControllerDeviceId", false, "MASTER_CONTROLLER_DEVICE_ID");
        public final static Property ExternalSensorsId = new Property(8, int.class, "externalSensorsId", false, "EXTERNAL_SENSORS_ID");
        public final static Property Layers = new Property(9, String.class, "layers", false, "LAYERS");
    }


    public DeviceGroupDao(DaoConfig config) {
        super(config);
    }
    
    public DeviceGroupDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEVICE_GROUP\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"USER_ID\" INTEGER NOT NULL ," + // 1: userId
                "\"HEADER\" TEXT," + // 2: header
                "\"FOOTER\" TEXT," + // 3: footer
                "\"COLOR\" INTEGER NOT NULL ," + // 4: color
                "\"HOUSE_NAME\" TEXT," + // 5: houseName
                "\"LOCATION\" TEXT," + // 6: location
                "\"MASTER_CONTROLLER_DEVICE_ID\" INTEGER NOT NULL ," + // 7: masterControllerDeviceId
                "\"EXTERNAL_SENSORS_ID\" INTEGER NOT NULL ," + // 8: externalSensorsId
                "\"LAYERS\" TEXT);"); // 9: layers
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE_GROUP\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DeviceGroup entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUserId());
 
        String header = entity.getHeader();
        if (header != null) {
            stmt.bindString(3, header);
        }
 
        String footer = entity.getFooter();
        if (footer != null) {
            stmt.bindString(4, footer);
        }
        stmt.bindLong(5, entity.getColor());
 
        String houseName = entity.getHouseName();
        if (houseName != null) {
            stmt.bindString(6, houseName);
        }
 
        String location = entity.getLocation();
        if (location != null) {
            stmt.bindString(7, location);
        }
        stmt.bindLong(8, entity.getMasterControllerDeviceId());
        stmt.bindLong(9, entity.getExternalSensorsId());
 
        String layers = entity.getLayers();
        if (layers != null) {
            stmt.bindString(10, layers);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DeviceGroup entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUserId());
 
        String header = entity.getHeader();
        if (header != null) {
            stmt.bindString(3, header);
        }
 
        String footer = entity.getFooter();
        if (footer != null) {
            stmt.bindString(4, footer);
        }
        stmt.bindLong(5, entity.getColor());
 
        String houseName = entity.getHouseName();
        if (houseName != null) {
            stmt.bindString(6, houseName);
        }
 
        String location = entity.getLocation();
        if (location != null) {
            stmt.bindString(7, location);
        }
        stmt.bindLong(8, entity.getMasterControllerDeviceId());
        stmt.bindLong(9, entity.getExternalSensorsId());
 
        String layers = entity.getLayers();
        if (layers != null) {
            stmt.bindString(10, layers);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DeviceGroup readEntity(Cursor cursor, int offset) {
        DeviceGroup entity = new DeviceGroup( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // userId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // header
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // footer
            cursor.getInt(offset + 4), // color
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // houseName
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // location
            cursor.getInt(offset + 7), // masterControllerDeviceId
            cursor.getInt(offset + 8), // externalSensorsId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // layers
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DeviceGroup entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserId(cursor.getInt(offset + 1));
        entity.setHeader(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setFooter(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setColor(cursor.getInt(offset + 4));
        entity.setHouseName(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setLocation(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setMasterControllerDeviceId(cursor.getInt(offset + 7));
        entity.setExternalSensorsId(cursor.getInt(offset + 8));
        entity.setLayers(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DeviceGroup entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DeviceGroup entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DeviceGroup entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
