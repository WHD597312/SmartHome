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
        public final static Property RatedPower = new Property(11, int.class, "ratedPower", false, "RATED_POWER");
        public final static Property MatTemp = new Property(12, int.class, "MatTemp", false, "MAT_TEMP");
        public final static Property WorkMode = new Property(13, String.class, "workMode", false, "WORK_MODE");
        public final static Property LockScreen = new Property(14, String.class, "LockScreen", false, "LOCK_SCREEN");
        public final static Property BackGroundLED = new Property(15, String.class, "BackGroundLED", false, "BACK_GROUND_LED");
        public final static Property DeviceState = new Property(16, String.class, "deviceState", false, "DEVICE_STATE");
        public final static Property TempState = new Property(17, String.class, "tempState", false, "TEMP_STATE");
        public final static Property OutputMod = new Property(18, String.class, "outputMod", false, "OUTPUT_MOD");
        public final static Property CurTemp = new Property(19, int.class, "curTemp", false, "CUR_TEMP");
        public final static Property ProtectEnable = new Property(20, String.class, "protectEnable", false, "PROTECT_ENABLE");
        public final static Property CtrlMode = new Property(21, String.class, "ctrlMode", false, "CTRL_MODE");
        public final static Property PowerValue = new Property(22, int.class, "powerValue", false, "POWER_VALUE");
        public final static Property VoltageValue = new Property(23, int.class, "voltageValue", false, "VOLTAGE_VALUE");
        public final static Property CurrentValue = new Property(24, int.class, "currentValue", false, "CURRENT_VALUE");
        public final static Property MachineFall = new Property(25, String.class, "machineFall", false, "MACHINE_FALL");
        public final static Property ProtectSetTemp = new Property(26, int.class, "protectSetTemp", false, "PROTECT_SET_TEMP");
        public final static Property ProtectProTemp = new Property(27, int.class, "protectProTemp", false, "PROTECT_PRO_TEMP");
        public final static Property WifiVersion = new Property(28, String.class, "wifiVersion", false, "WIFI_VERSION");
        public final static Property MCUVerion = new Property(29, String.class, "MCUVerion", false, "MCUVERION");
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
                "\"VERSION\" INTEGER NOT NULL ," + // 10: version
                "\"RATED_POWER\" INTEGER NOT NULL ," + // 11: ratedPower
                "\"MAT_TEMP\" INTEGER NOT NULL ," + // 12: MatTemp
                "\"WORK_MODE\" TEXT," + // 13: workMode
                "\"LOCK_SCREEN\" TEXT," + // 14: LockScreen
                "\"BACK_GROUND_LED\" TEXT," + // 15: BackGroundLED
                "\"DEVICE_STATE\" TEXT," + // 16: deviceState
                "\"TEMP_STATE\" TEXT," + // 17: tempState
                "\"OUTPUT_MOD\" TEXT," + // 18: outputMod
                "\"CUR_TEMP\" INTEGER NOT NULL ," + // 19: curTemp
                "\"PROTECT_ENABLE\" TEXT," + // 20: protectEnable
                "\"CTRL_MODE\" TEXT," + // 21: ctrlMode
                "\"POWER_VALUE\" INTEGER NOT NULL ," + // 22: powerValue
                "\"VOLTAGE_VALUE\" INTEGER NOT NULL ," + // 23: voltageValue
                "\"CURRENT_VALUE\" INTEGER NOT NULL ," + // 24: currentValue
                "\"MACHINE_FALL\" TEXT," + // 25: machineFall
                "\"PROTECT_SET_TEMP\" INTEGER NOT NULL ," + // 26: protectSetTemp
                "\"PROTECT_PRO_TEMP\" INTEGER NOT NULL ," + // 27: protectProTemp
                "\"WIFI_VERSION\" TEXT," + // 28: wifiVersion
                "\"MCUVERION\" TEXT);"); // 29: MCUVerion
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
        stmt.bindLong(12, entity.getRatedPower());
        stmt.bindLong(13, entity.getMatTemp());
 
        String workMode = entity.getWorkMode();
        if (workMode != null) {
            stmt.bindString(14, workMode);
        }
 
        String LockScreen = entity.getLockScreen();
        if (LockScreen != null) {
            stmt.bindString(15, LockScreen);
        }
 
        String BackGroundLED = entity.getBackGroundLED();
        if (BackGroundLED != null) {
            stmt.bindString(16, BackGroundLED);
        }
 
        String deviceState = entity.getDeviceState();
        if (deviceState != null) {
            stmt.bindString(17, deviceState);
        }
 
        String tempState = entity.getTempState();
        if (tempState != null) {
            stmt.bindString(18, tempState);
        }
 
        String outputMod = entity.getOutputMod();
        if (outputMod != null) {
            stmt.bindString(19, outputMod);
        }
        stmt.bindLong(20, entity.getCurTemp());
 
        String protectEnable = entity.getProtectEnable();
        if (protectEnable != null) {
            stmt.bindString(21, protectEnable);
        }
 
        String ctrlMode = entity.getCtrlMode();
        if (ctrlMode != null) {
            stmt.bindString(22, ctrlMode);
        }
        stmt.bindLong(23, entity.getPowerValue());
        stmt.bindLong(24, entity.getVoltageValue());
        stmt.bindLong(25, entity.getCurrentValue());
 
        String machineFall = entity.getMachineFall();
        if (machineFall != null) {
            stmt.bindString(26, machineFall);
        }
        stmt.bindLong(27, entity.getProtectSetTemp());
        stmt.bindLong(28, entity.getProtectProTemp());
 
        String wifiVersion = entity.getWifiVersion();
        if (wifiVersion != null) {
            stmt.bindString(29, wifiVersion);
        }
 
        String MCUVerion = entity.getMCUVerion();
        if (MCUVerion != null) {
            stmt.bindString(30, MCUVerion);
        }
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
        stmt.bindLong(12, entity.getRatedPower());
        stmt.bindLong(13, entity.getMatTemp());
 
        String workMode = entity.getWorkMode();
        if (workMode != null) {
            stmt.bindString(14, workMode);
        }
 
        String LockScreen = entity.getLockScreen();
        if (LockScreen != null) {
            stmt.bindString(15, LockScreen);
        }
 
        String BackGroundLED = entity.getBackGroundLED();
        if (BackGroundLED != null) {
            stmt.bindString(16, BackGroundLED);
        }
 
        String deviceState = entity.getDeviceState();
        if (deviceState != null) {
            stmt.bindString(17, deviceState);
        }
 
        String tempState = entity.getTempState();
        if (tempState != null) {
            stmt.bindString(18, tempState);
        }
 
        String outputMod = entity.getOutputMod();
        if (outputMod != null) {
            stmt.bindString(19, outputMod);
        }
        stmt.bindLong(20, entity.getCurTemp());
 
        String protectEnable = entity.getProtectEnable();
        if (protectEnable != null) {
            stmt.bindString(21, protectEnable);
        }
 
        String ctrlMode = entity.getCtrlMode();
        if (ctrlMode != null) {
            stmt.bindString(22, ctrlMode);
        }
        stmt.bindLong(23, entity.getPowerValue());
        stmt.bindLong(24, entity.getVoltageValue());
        stmt.bindLong(25, entity.getCurrentValue());
 
        String machineFall = entity.getMachineFall();
        if (machineFall != null) {
            stmt.bindString(26, machineFall);
        }
        stmt.bindLong(27, entity.getProtectSetTemp());
        stmt.bindLong(28, entity.getProtectProTemp());
 
        String wifiVersion = entity.getWifiVersion();
        if (wifiVersion != null) {
            stmt.bindString(29, wifiVersion);
        }
 
        String MCUVerion = entity.getMCUVerion();
        if (MCUVerion != null) {
            stmt.bindString(30, MCUVerion);
        }
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
            cursor.getInt(offset + 10), // version
            cursor.getInt(offset + 11), // ratedPower
            cursor.getInt(offset + 12), // MatTemp
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // workMode
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // LockScreen
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // BackGroundLED
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // deviceState
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // tempState
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // outputMod
            cursor.getInt(offset + 19), // curTemp
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // protectEnable
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // ctrlMode
            cursor.getInt(offset + 22), // powerValue
            cursor.getInt(offset + 23), // voltageValue
            cursor.getInt(offset + 24), // currentValue
            cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25), // machineFall
            cursor.getInt(offset + 26), // protectSetTemp
            cursor.getInt(offset + 27), // protectProTemp
            cursor.isNull(offset + 28) ? null : cursor.getString(offset + 28), // wifiVersion
            cursor.isNull(offset + 29) ? null : cursor.getString(offset + 29) // MCUVerion
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
        entity.setRatedPower(cursor.getInt(offset + 11));
        entity.setMatTemp(cursor.getInt(offset + 12));
        entity.setWorkMode(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setLockScreen(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setBackGroundLED(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setDeviceState(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setTempState(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setOutputMod(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setCurTemp(cursor.getInt(offset + 19));
        entity.setProtectEnable(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setCtrlMode(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setPowerValue(cursor.getInt(offset + 22));
        entity.setVoltageValue(cursor.getInt(offset + 23));
        entity.setCurrentValue(cursor.getInt(offset + 24));
        entity.setMachineFall(cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25));
        entity.setProtectSetTemp(cursor.getInt(offset + 26));
        entity.setProtectProTemp(cursor.getInt(offset + 27));
        entity.setWifiVersion(cursor.isNull(offset + 28) ? null : cursor.getString(offset + 28));
        entity.setMCUVerion(cursor.isNull(offset + 29) ? null : cursor.getString(offset + 29));
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
