package com.esloq.esloqapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class to manage the creation and versioning of the database.
 */
class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "esloq.db";

    private static final String SQL_CREATE_LOCK = "CREATE TABLE " + DatabaseContract.Lock.TABLE_NAME + " (" +
            DatabaseContract.Lock.COLUMN_NAME_MAC + " TEXT PRIMARY KEY," +
            DatabaseContract.Lock.COLUMN_NAME_NAME + " TEXT, " +
            DatabaseContract.Lock.COLUMN_NAME_LOCK_CLOCKWISE + " INTEGER" + ")";

    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + DatabaseContract.User.TABLE_NAME + " (" +
                    DatabaseContract.User.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.User.COLUMN_NAME_NAME + " TEXT," +
                    DatabaseContract.User.COLUMN_NAME_VALIDATED + " INTEGER)";

    private static final String SQL_CREATE_LOCKACCESS =
            "CREATE TABLE " + DatabaseContract.LockAccess.TABLE_NAME + " (" +
                    DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + " TEXT," +
                    DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN + " INTEGER," +
                    DatabaseContract.LockAccess.COLUMN_NAME_USERID +  " INTEGER," +
                    "PRIMARY KEY (" + DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + ", " +
                    DatabaseContract.LockAccess.COLUMN_NAME_USERID + ")" +
                    "FOREIGN KEY (" + DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + ")" +
                    "REFERENCES " + DatabaseContract.Lock.TABLE_NAME + "(" +
                    DatabaseContract.Lock.COLUMN_NAME_MAC+") ON DELETE CASCADE," +
                    "FOREIGN KEY (" + DatabaseContract.LockAccess.COLUMN_NAME_USERID + ")" +
                    "REFERENCES " + DatabaseContract.User.TABLE_NAME + "(" +
                    DatabaseContract.User.COLUMN_NAME_ID+") ON DELETE CASCADE" + ")";

    private static final String SQL_CREATE_LOG =
            "CREATE TABLE " + DatabaseContract.Log.TABLE_NAME + " (" +
                    DatabaseContract.Log.COLUMN_NAME_LOCKMAC + " TEXT," +
                    DatabaseContract.Log.COLUMN_NAME_USERID +  " INTEGER," +
                    DatabaseContract.Log.COLUMN_NAME_LOCKSTATE + " INTEGER," +
                    DatabaseContract.Log.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    "FOREIGN KEY (" + DatabaseContract.Log.COLUMN_NAME_LOCKMAC + ")" +
                    "REFERENCES " + DatabaseContract.Lock.TABLE_NAME + "(" +
                    DatabaseContract.Lock.COLUMN_NAME_MAC+") ON DELETE CASCADE" + ")";
                    //TODO should be uncommented, but there is still a bug that users present in
                    // logs are not retrieved from server.
//                    "FOREIGN KEY (" + DatabaseContract.Log.COLUMN_NAME_USERID + ")" +
//                    "REFERENCES " + DatabaseContract.User.TABLE_NAME + "(" +
//                    DatabaseContract.User.COLUMN_NAME_ID+")" + ")";

    private static final String SQL_DELETE_LOCK =
            "DROP TABLE IF EXISTS " + DatabaseContract.Lock.TABLE_NAME;

    private static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + DatabaseContract.User.TABLE_NAME;

    private static final String SQL_DELETE_LOCKACCESS =
            "DROP TABLE IF EXISTS " + DatabaseContract.LockAccess.TABLE_NAME;

    private static final String SQL_DELETE_LOG =
            "DROP TABLE IF EXISTS " + DatabaseContract.Log.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOCK);
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_LOCKACCESS);
        db.execSQL(SQL_CREATE_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_LOG);
        db.execSQL(SQL_DELETE_LOCKACCESS);
        db.execSQL(SQL_DELETE_USER);
        db.execSQL(SQL_DELETE_LOCK);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
