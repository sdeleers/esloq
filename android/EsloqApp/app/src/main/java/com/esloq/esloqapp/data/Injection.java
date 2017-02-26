package com.esloq.esloqapp.data;

import android.content.Context;

/**
 * Enables injection of mock implementations for {@link LockDataRepository} at compile time. This
 * is useful for testing, since it allows us to use a fake instance of the class to isolate the
 * dependencies and run a test hermetically.
 */
public class Injection {

    public static LockDataRepository provideLockDataRepository(Context context) {
        return new LockDataRepositoryImpl(new LocalDataServiceApiImpl(context), new
                ServerDataServiceApiImpl(context), new PreferencesServiceApiImpl(context));
    }
}
