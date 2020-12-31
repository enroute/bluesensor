package com.ztfun.bluesensor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "blue.db";

    public static class DeviceEntry implements  BaseColumns {
        public static final String TABLE_NAME = "device";
        public static final String COLUMN_NAME_DEVICE_NAME = "name";
        public static final String COLUMN_NAME_DEVICE_ADDRESS = "address";
        public static final String COLUMN_NAME_DEVICE_NOTE = "note";
    }

    private static final String SQL_CREATE_DEVICE_TABLE =
            "CREATE TABLE " + DeviceEntry.TABLE_NAME + " (" +
                    DeviceEntry._ID + " INTEGER PRIMARY KEY," +
                    DeviceEntry.COLUMN_NAME_DEVICE_NAME + " TEXT," +
                    DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS + " TEXT," +
                    DeviceEntry.COLUMN_NAME_DEVICE_NOTE + " TEXT)";

    private static final String SQL_DELETE_DEVICE_TABLE =
            "DROP TABLE IF EXISTS " + DeviceEntry.TABLE_NAME;

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "data";
        public static final String COLUMN_NAME_DEVICE_ID = "did";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
    }

    private static final String SQL_CREATE_DATA_TABLE =
            "CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
                    DataEntry._ID + " INTEGER PRIMARY KEY," +
                    DataEntry.COLUMN_NAME_DEVICE_ID + " INTEGER," +
                    DataEntry.COLUMN_NAME_X + " REAL," +
                    DataEntry.COLUMN_NAME_Y + " REAL)";

    private static final String SQL_DELETE_DATA_TABLE =
            "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;

    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public DbHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DEVICE_TABLE);
        db.execSQL(SQL_CREATE_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DEVICE_TABLE);
        db.execSQL(SQL_DELETE_DATA_TABLE);
        onCreate(db);
    }

    private ContentValues createDeviceContentValues(String address, String name, String note) {
        ContentValues cv = new ContentValues();
        cv.put(DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS, address);
        cv.put(DeviceEntry.COLUMN_NAME_DEVICE_NAME, name);
        cv.put(DeviceEntry.COLUMN_NAME_DEVICE_NOTE, note);
        return cv;
    }

    public class Device {
        int _id;
        String address;
        String name;
        String note;

        public Device(int _id, String address, String name, String note) {
            this._id = _id;
            this.address = address;
            this.name = name;
            this.note = note;
        }

        @NonNull
        @Override
        public String toString() {
            return String.valueOf(_id) + "/" + address + "/"  + name + "/" + note;
        }
    }

    public List<Device> getDevices() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(DeviceEntry.TABLE_NAME,
                new String[] {
                        DeviceEntry._ID,
                        DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS,
                        DeviceEntry.COLUMN_NAME_DEVICE_NAME,
                        DeviceEntry.COLUMN_NAME_DEVICE_NOTE},
                null,
                null,
                null,
                null,
                null,
                null);
        List<Device> address = new ArrayList<>();
        while(cursor.moveToNext()) {
            Device device = new Device(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DeviceEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DeviceEntry.COLUMN_NAME_DEVICE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DeviceEntry.COLUMN_NAME_DEVICE_NOTE)));
            address.add(device);
        }
        return address;
    }

    public boolean insertDevice(String address, String name, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = createDeviceContentValues(address, name, note);

        // try update first
        int updateRows = db.update(DeviceEntry.TABLE_NAME,
                cv,
                DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS + " = ?",
                new String[] {address});
        if (updateRows == 1) {
            return true;
        }

        // insert if update failed
        return db.insert(DeviceEntry.TABLE_NAME, null, cv) != -1;
    }

    public int getDeviceIdByAddress(String address) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(DeviceEntry.TABLE_NAME,
                new String[] {DeviceEntry._ID},
                DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS + " = ?",
                new String[] {address},
                null,
                null,
                null,
                null
                );
        int did = -1;
        while(cursor.moveToNext()) {
            did = cursor.getInt(cursor.getColumnIndexOrThrow(DeviceEntry._ID));
            // we need only one
            break;
        }
        cursor.close();
        return did;
    }

    public boolean insertData(String address, float x, float y) {
        SQLiteDatabase db = getWritableDatabase();

        // get `did`, device id by address


        // record device
        ContentValues cv = new ContentValues();
        cv.put(DeviceEntry.COLUMN_NAME_DEVICE_ADDRESS, address);
        return true;
    }
}