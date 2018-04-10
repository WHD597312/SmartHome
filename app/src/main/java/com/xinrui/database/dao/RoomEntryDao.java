package com.xinrui.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xinrui.smart.pojo.RoomEntry;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ROOM_ENTRY".
*/
public class RoomEntryDao extends AbstractDao<RoomEntry, Long> {

    public static final String TABLENAME = "ROOM_ENTRY";

    /**
     * Properties of entity RoomEntry.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property X = new Property(1, int.class, "x", false, "X");
        public final static Property Y = new Property(2, int.class, "y", false, "Y");
        public final static Property Width = new Property(3, int.class, "width", false, "WIDTH");
        public final static Property Height = new Property(4, int.class, "height", false, "HEIGHT");
    }


    public RoomEntryDao(DaoConfig config) {
        super(config);
    }
    
    public RoomEntryDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ROOM_ENTRY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"X\" INTEGER NOT NULL ," + // 1: x
                "\"Y\" INTEGER NOT NULL ," + // 2: y
                "\"WIDTH\" INTEGER NOT NULL ," + // 3: width
                "\"HEIGHT\" INTEGER NOT NULL );"); // 4: height
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ROOM_ENTRY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RoomEntry entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getX());
        stmt.bindLong(3, entity.getY());
        stmt.bindLong(4, entity.getWidth());
        stmt.bindLong(5, entity.getHeight());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RoomEntry entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getX());
        stmt.bindLong(3, entity.getY());
        stmt.bindLong(4, entity.getWidth());
        stmt.bindLong(5, entity.getHeight());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RoomEntry readEntity(Cursor cursor, int offset) {
        RoomEntry entity = new RoomEntry( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // x
            cursor.getInt(offset + 2), // y
            cursor.getInt(offset + 3), // width
            cursor.getInt(offset + 4) // height
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RoomEntry entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setX(cursor.getInt(offset + 1));
        entity.setY(cursor.getInt(offset + 2));
        entity.setWidth(cursor.getInt(offset + 3));
        entity.setHeight(cursor.getInt(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RoomEntry entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RoomEntry entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RoomEntry entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}