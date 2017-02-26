package com.esloq.esloqapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Content provider for the database containing the lock data.
 */
public class LockDataContentProvider extends ContentProvider {

    /**
     * URI types, each URI type (int) is matched to a URI.
     */
    private static final int LOCKS = 1;
    private static final int LOCKS_ID = 2;
    private static final int LOCKS_USERS = 3;
    private static final int LOCKS_USERS_ID = 4;
    private static final int LOCKS_LOGS = 5;
    private static final int LOCKS_LOGS_ID = 6;
    private static final int USERS = 7;
    private static final int USERS_ID = 8;
    private static final int LOCK_ACCESS = 9;
    private static final int LOCK_ACCESS_ID = 10;
    private static final int LOGS = 11;
    private static final int LOGS_ID = 12;


    /**
     * URI matcher which matches URI's to their types (int).
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Provides access to the database.
     */
    private DatabaseHelper databaseHelper;

    /**
     * Provides access to the shared preferences
     */
    private PreferencesServiceApi preferences;

    /**
     * Match URI's with URI types.
     */
    static
    {
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks", LOCKS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks/*", LOCKS_ID);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks/*/users", LOCKS_USERS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks/*/users/#", LOCKS_USERS_ID);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks/*/logs", LOCKS_LOGS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "locks/*/logs/#", LOCKS_LOGS_ID);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "users", USERS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "users/#", USERS_ID);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "lockaccess", LOCK_ACCESS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "lockaccess/#", LOCK_ACCESS_ID);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "logs", LOGS);
        sURIMatcher.addURI(DatabaseContract.AUTHORITY, "logs/#", LOGS_ID);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        preferences = new PreferencesServiceApiImpl(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

//        checkColumns(projection);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case LOCKS:
//                queryBuilder.setTables(DatabaseContract.Lock.TABLE_NAME);
                queryBuilder.setTables(DatabaseContract.Lock.TABLE_NAME + " INNER JOIN " +
                        DatabaseContract.LockAccess.TABLE_NAME + " ON " +
                        DatabaseContract.Lock.TABLE_NAME + "." +
                        DatabaseContract.Lock.COLUMN_NAME_MAC + "=" +
                        DatabaseContract.LockAccess.TABLE_NAME + "." +
                        DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC);
                int userId = preferences.getUserId();
                queryBuilder.appendWhere(DatabaseContract.LockAccess.COLUMN_NAME_USERID + "=" + userId);
                break;
            case LOCKS_ID:
                queryBuilder.setTables(DatabaseContract.Lock.TABLE_NAME);
                queryBuilder.appendWhere(DatabaseContract.Lock.COLUMN_NAME_MAC + "=");
                queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
                break;
            case LOCKS_LOGS:
                queryBuilder.setTables(DatabaseContract.Log.TABLE_NAME + " INNER JOIN " +
                        DatabaseContract.User.TABLE_NAME + " ON " +
                        DatabaseContract.Log.TABLE_NAME + "." +
                        DatabaseContract.Log.COLUMN_NAME_USERID + "=" +
                        DatabaseContract.User.TABLE_NAME + "." +
                        DatabaseContract.User.COLUMN_NAME_ID);
                queryBuilder.appendWhere(DatabaseContract.Log.TABLE_NAME + "." + DatabaseContract.Log.COLUMN_NAME_LOCKMAC + "=");
                queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
                break;
            case LOCKS_USERS:
                queryBuilder.setTables(DatabaseContract.LockAccess.TABLE_NAME + " INNER JOIN " +
                        DatabaseContract.User.TABLE_NAME + " ON " +
                        DatabaseContract.LockAccess.TABLE_NAME + "." +
                        DatabaseContract.LockAccess.COLUMN_NAME_USERID + "=" +
                        DatabaseContract.User.TABLE_NAME + "." +
                        DatabaseContract.User.COLUMN_NAME_ID);
                queryBuilder.appendWhere(DatabaseContract.LockAccess.TABLE_NAME + "." + DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + "=");
                queryBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
                int loggedInUserId = preferences.getUserId();
                queryBuilder.appendWhere(" AND " + DatabaseContract.User.TABLE_NAME + "." + DatabaseContract.User.COLUMN_NAME_ID + " != " + loggedInUserId);
                break;
            case USERS:
                queryBuilder.setTables(DatabaseContract.User.TABLE_NAME);
                break;
            case LOCK_ACCESS:
                queryBuilder.setTables(DatabaseContract.LockAccess.TABLE_NAME);
                break;
            case LOGS:
                queryBuilder.setTables(DatabaseContract.Log.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        Context context = getContext();
        assert context != null;
        cursor.setNotificationUri(context.getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case LOCKS:
                id = db.insertWithOnConflict(DatabaseContract.Lock.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            case LOCKS_USERS:
                id = db.insert(DatabaseContract.LockAccess.TABLE_NAME, null, values);
                break;
            case LOCKS_LOGS:
                id = db.insert(DatabaseContract.Log.TABLE_NAME, null, values);
                break;
            case USERS:
                // ignore insert of constraint violation occurs
                id = db.insertWithOnConflict(DatabaseContract.User.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            case LOCK_ACCESS:
                id = db.insert(DatabaseContract.LockAccess.TABLE_NAME, null, values);
                break;
            case LOGS:
                id = db.insert(DatabaseContract.Log.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri + "/" + id); //TODO SHOULD BE MAC INSTEAD OF ID
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case LOCKS:
                rowsDeleted = db.delete(DatabaseContract.Lock.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case LOCKS_ID: {
                String lockMac = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(DatabaseContract.Lock.TABLE_NAME,
                            DatabaseContract.Lock.COLUMN_NAME_MAC + "=" + "\"" + lockMac + "\"",
                            null);
                } else {
                    rowsDeleted = db.delete(DatabaseContract.Lock.TABLE_NAME,
                            DatabaseContract.Lock.COLUMN_NAME_MAC + "=" + "\"" + lockMac + "\"" +
                                    " AND " + selection,
                            selectionArgs);
                }
                break;
            }
            case LOCKS_USERS_ID: {
                String lockMac = uri.getPathSegments().get(1);
                String userId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(DatabaseContract.LockAccess.TABLE_NAME,
                            DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + "=" +
                                    "\"" + lockMac  + "\"" + " AND " +
                                    DatabaseContract.LockAccess.COLUMN_NAME_USERID + "=" + userId,
                            null);
                } else {
                    rowsDeleted = db.delete(DatabaseContract.LockAccess.TABLE_NAME,
                            DatabaseContract.LockAccess.COLUMN_NAME_LOCKMAC + "=" +
                                    "\"" + lockMac  + "\"" + " AND " +
                                    DatabaseContract.LockAccess.COLUMN_NAME_USERID + "=" + userId
                                    + " AND " + selection,
                            selectionArgs);
                }
                break;
            }
            case LOCK_ACCESS:
                rowsDeleted = db.delete(DatabaseContract.LockAccess.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case USERS:
                rowsDeleted = db.delete(DatabaseContract.User.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case LOGS:
                rowsDeleted = db.delete(DatabaseContract.Log.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
//            case LOCKS:
//                rowsUpdated = db.update(DatabaseContract.Lock.TABLE_NAME,
//                        values,
//                        selection,
//                        selectionArgs);
//                break;
//            case LOCKS_ID:
//                String id = uri.getLastPathSegment();
//                if (TextUtils.isEmpty(selection)) {
//                    rowsUpdated = db.update(DatabaseContract.Lock.TABLE_NAME,
//                            values,
//                            DatabaseContract.Lock.COLUMN_NAME_MAC+ "=" + id,
//                            null);
//                } else {
//                    rowsUpdated = db.update(DatabaseContract.Lock.TABLE_NAME,
//                            values,
//                            DatabaseContract.Lock.COLUMN_NAME_MAC + "=" + id
//                                    + " and "
//                                    + selection,
//                            selectionArgs);
//                }
//                break;
            case USERS_ID:
                String userId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(DatabaseContract.User.TABLE_NAME, values,
                            DatabaseContract.User.COLUMN_NAME_ID + "=" + "\"" + userId  + "\"",
                            null);
                } else {
                    rowsUpdated = db.update(DatabaseContract.User.TABLE_NAME, values,
                            DatabaseContract.User.COLUMN_NAME_ID + "=" + "\"" + userId  + "\"" +
                                    " AND " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }


//    private void checkColumns(String[] projection) {
//        String[] available = { TodoTable.COLUMN_CATEGORY,
//                TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION,
//                TodoTable.COLUMN_ID };
//        if (projection != null) {
//            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
//            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
//            // check if all columns which are requested are available
//            if (!availableColumns.containsAll(requestedColumns)) {
//                throw new IllegalArgumentException("Unknown columns in projection");
//            }
//        }
//    }
}
