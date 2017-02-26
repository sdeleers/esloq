package com.esloq.esloqapp.data;


import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

/**
 * Provides loaders so the activities can load data asynchronously
 */
public class LoaderProvider {

    private final Context mContext;

    /**
     * Construct a new loader provider.
     *
     * @param context The application's context.
     */
    public LoaderProvider(Context context) {
        mContext = context;
    }

    /**
     * Return a CursorLoader that queries the database for all existing locks and returns their mac
     * address, their name and whether the current user is admin or not.
     *
     * @return CursorLoader to query the lock data.
     */
    public Loader<Cursor> getLocksCursorLoader() {
        String[] projection = {
                DatabaseContract.Lock.COLUMN_NAME_MAC,
                DatabaseContract.Lock.COLUMN_NAME_NAME,
                DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN,
        };
        final String orderBy = DatabaseContract.Lock.COLUMN_NAME_NAME + " ASC";
        return new CursorLoader(mContext, DatabaseContract.Lock.CONTENT_URI, projection, null, null, orderBy);
    }

    /**
     * Return a CursorLoader that queries the database for all existing logs on the requested lock
     * and returns the user's name, the timestamp at which the action took place, and the state
     * in which the lock has been placed.
     *
     * @param mac The lock from which to obtain the logs.
     * @return CursorLoader to query the log data.
     */
    public Loader<Cursor> getLockLogsCursorLoader(String mac) {
        String[] projection = {
                DatabaseContract.User.COLUMN_NAME_NAME,
                DatabaseContract.Log.COLUMN_NAME_TIMESTAMP,
                DatabaseContract.Log.COLUMN_NAME_LOCKSTATE,
        };
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(mac).appendPath("logs").build();
        final String orderBy = DatabaseContract.Log.COLUMN_NAME_TIMESTAMP + " DESC";
        return new CursorLoader(mContext, uri, projection, null, null, orderBy);
    }

    /**
     * Return a CursorLoader that queries the database for all existing users on the requested lock
     * and returns the user's id and name.
     *
     * @param lockMac The lock from which to obtain the users.
     * @return CursorLoader to query the lock's users.
     */
    public Loader<Cursor> getLockUsersCursorLoader(String lockMac) {
        String[] projection = {
                DatabaseContract.User.COLUMN_NAME_ID,
                DatabaseContract.User.COLUMN_NAME_NAME,
                DatabaseContract.User.COLUMN_NAME_VALIDATED,
                DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN,
        };
        Uri uri = DatabaseContract.Lock.CONTENT_URI.buildUpon().appendPath(lockMac).appendPath("users").build();
        final String orderBy = DatabaseContract.LockAccess.COLUMN_NAME_ISADMIN + " DESC, " +
                DatabaseContract.User.COLUMN_NAME_VALIDATED + " DESC, " +
                DatabaseContract.User.COLUMN_NAME_NAME + " ASC";
        return new CursorLoader(mContext, uri, projection, null, null, orderBy);
    }
}
