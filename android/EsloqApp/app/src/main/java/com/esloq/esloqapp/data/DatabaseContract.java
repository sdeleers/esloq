package com.esloq.esloqapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class that defines the database schemas.
 */
abstract class DatabaseContract {

    public static final String AUTHORITY = "com.esloq.android.app.provider.lockdatacontentprovider";

    public static abstract class Lock implements BaseColumns {
        private static final String BASE_PATH = "locks";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/" + BASE_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + BASE_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + BASE_PATH;

        public static final String TABLE_NAME = "lock";
        public static final String COLUMN_NAME_MAC = "mac";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LOCK_CLOCKWISE = "lock_clockwise";
    }

    public static abstract class User implements BaseColumns {
        private static final String BASE_PATH = "users";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/" + BASE_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + BASE_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + BASE_PATH;

        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALIDATED = "validated";
    }

    public static abstract class LockAccess implements BaseColumns {
        private static final String BASE_PATH = "lockaccess";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/" + BASE_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + BASE_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + BASE_PATH;

        public static final String TABLE_NAME = "lock_access";
        public static final String COLUMN_NAME_LOCKMAC = "lock_mac";
        public static final String COLUMN_NAME_USERID = "user_id";
        public static final String COLUMN_NAME_ISADMIN = "is_admin";
    }

    public static abstract class Log implements BaseColumns {
        private static final String BASE_PATH = "logs";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/" + BASE_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + BASE_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + BASE_PATH;

        public static final String TABLE_NAME = "log";
        public static final String COLUMN_NAME_LOCKMAC = "lock_mac";
        public static final String COLUMN_NAME_USERID = "user_id";
        public static final String COLUMN_NAME_LOCKSTATE = "lock_state";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

}
