package com.esloq.esloqapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Class that provides an interface to access the lock data while making abstraction of where and
 * how it is stored.
 */
class LocalDataServiceApiImpl implements LocalDataServiceApi{

    //TODO USE RETURN TYPES THAT INDICATE SUCCESS!!! db.insert e.g return -1 if failure, CHECK!! or throw exception

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = LocalDataServiceApiImpl.class.getSimpleName();

    /**
     * Reference to the context of this application.
     */
    private final Context context;

    /**
     * Construct an instance to access the lock data repository.
     *
     * @param context The context of this application.
     */
    public LocalDataServiceApiImpl(Context context) {
        this.context = context;
    }

    /**
     * Add a lock to the repository.
     *
     * @param mac The mac address of the lock.
     * @param name The name of the lock.
     */
    @Override
    public void addLock(String mac, String name, boolean lockClockwise) {
        mac = mac.toUpperCase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.Lock.COLUMN_NAME_MAC, mac);
        contentValues.put(DatabaseContract.Lock.COLUMN_NAME_NAME, name);
        contentValues.put(DatabaseContract.Lock.COLUMN_NAME_LOCK_CLOCKWISE, lockClockwise);
        context.getContentResolver().insert(DatabaseContract.Lock.CONTENT_URI, contentValues);
    }

    /**
     * Add a user to the repository
     *
     * @param id The id of the user
     * @param name The name of the user.
     */
    @Override
    public void addUser(int id, String name, boolean validated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.User.COLUMN_NAME_ID, id);
        contentValues.put(DatabaseContract.User.COLUMN_NAME_NAME, name);
        contentValues.put(DatabaseContract.User.COLUMN_NAME_VALIDATED, validated ? 1 : 0);
        context.getContentResolver().insert(DatabaseContract.User.CONTENT_URI, contentValues);
    }

    /**
     * Add a lock access to the repository. This corresponds to adding an existing user to an
     * existing lock and setting their authorization (admin or guest).
     *
     * @param userId The id of the user.
     * @param mac The mac address of the lock.
     * @param isAdmin Indicates whether the new user is admin or not.
     */
    @Override
    public void addLockAccess(int userId, String mac, boolean isAdmin) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_USERID, userId);
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC, mac);
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN, isAdmin ? 1 : 0);
        context.getContentResolver().insert(DatabaseContract.LockAccess.CONTENT_URI, contentValues);
    }

    /**
     * Add user (new or existing) to lock.
     *
     * @param userId The id of the user.
     * @param name The name of the lock.
     * @param mac The mac address of the lock.
     * @param isAdmin Indicates whether the new user is admin or not.
     */
    @Override
    public void addUserToLock(int userId, String name, boolean validated, String mac, boolean
            isAdmin) {
        addUser(userId, name, validated);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_USERID, userId);
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC, mac);
        contentValues.put(DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN, isAdmin ? 1 : 0);
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(mac).appendPath("users")
                .build();
        context.getContentResolver().insert(uri, contentValues);
    }

    /**
     * Add a new log to the repository.
     *
     * @param userId The id of the user.
     * @param mac The mac address of the lock.
     * @param locked Indicates whether the lock has been locked or unlocked.
     * @param timestamp Timestamp of when the action took place.
     */
    @Override
    public void addLogToLock(int userId, String mac, boolean locked, long timestamp) {
        mac = mac.toUpperCase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.Log.COLUMN_NAME_USERID, userId);
        contentValues.put(DatabaseContract.Log.COLUMN_NAME_LOCKMAC, mac);
        contentValues.put(DatabaseContract.Log.COLUMN_NAME_LOCKSTATE, locked);
        contentValues.put(DatabaseContract.Log.COLUMN_NAME_TIMESTAMP, timestamp);
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(mac).appendPath("logs")
                .build();
        context.getContentResolver().insert(uri, contentValues);
    }

    /**
     * Remove an existing lock from the repository.
     *
     * @param mac The mac address of the lock to be removed.
     */
    @Override
    public void removeLock(String mac) {
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(mac).build();
        context.getContentResolver().delete(uri, null, null);
    }

    /**
     * Remove an existing user from a lock.
     *
     * @param userId The id of the user.
     * @param lockMac The mac address of the lock from which to remove the user.
     */
    @Override
    public void removeUserFromLock(int userId, String lockMac) {
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(lockMac).appendPath("users").appendPath(Integer.toString(userId)).build();
        context.getContentResolver().delete(uri, null, null);
    }

    @Override
    public void updateUser(int id, String name, boolean validated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.User.COLUMN_NAME_ID, id);
        contentValues.put(DatabaseContract.User.COLUMN_NAME_NAME, name);
        contentValues.put(DatabaseContract.User.COLUMN_NAME_VALIDATED, validated ? 1 : 0);
        Uri uri = DatabaseContract.User.CONTENT_URI.buildUpon().appendPath(Integer.toString(id))
                .build();
        context.getContentResolver().update(uri, contentValues, null, null);
    }

    /**
     * Clear all tables from database.
     */
    @Override
    public void clearDatabase() {
        //Automatically happens with ON DELETE CASCADE enabled.
//        context.getContentResolver().delete(DatabaseContract.LockAccess.CONTENT_URI, null, null);
//        context.getContentResolver().delete(DatabaseContract.Log.CONTENT_URI, null, null);
        context.getContentResolver().delete(DatabaseContract.Lock.CONTENT_URI, null, null);
        context.getContentResolver().delete(DatabaseContract.User.CONTENT_URI, null, null);
    }

    @Override
    public boolean locksClockwise(String lockMac) {
        String[] projection = {
                DatabaseContract.Lock.COLUMN_NAME_LOCK_CLOCKWISE,
        };
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(lockMac).build();
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        boolean locksClockwise;
        if (cursor != null  && cursor.moveToFirst()) {
            locksClockwise = cursor.getInt(0) == 1;
            cursor.close();
        } else {
            throw new IllegalArgumentException("Lock not found.");
        }

        return locksClockwise;
    }

    public void printDatabase() {
//        Log.e("Lock", DatabaseUtils.dumpCursorToString(context.getContentResolver().query
//                (DatabaseContract.Lock.CONTENT_URI,
//                        null, null, null, null)));
//        Log.e("User", DatabaseUtils.dumpCursorToString(context.getContentResolver().query
//                (DatabaseContract.User.CONTENT_URI,
//                        null, null, null, null)));
//        Log.e("LockAccess", DatabaseUtils.dumpCursorToString(context.getContentResolver().query
//                (DatabaseContract.LockAccess.CONTENT_URI,
//                        null, null, null, null)));
//        Log.e("Log", DatabaseUtils.dumpCursorToString(context.getContentResolver().query
//                (DatabaseContract.Log.CONTENT_URI,
//                        null, null, null, null)));
    }
}
