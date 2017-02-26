package com.esloq.esloqapp.data;

/**
 * Defines an interface to the lock data repository API that is used to access data. All
 * data requests should be piped through this interface.
 */
public interface LockDataRepository {

    interface OnResultCallback {
        void onResult(boolean success);
    }

    void registerUserDevice(final OnResultCallback callback);

    void fetchData(final OnResultCallback callback);

    void addLock(final String mac, final String name, final boolean lockClockwise, final
            OnResultCallback callback);

    void addUser(final String email, final String mac, final boolean isAdmin, final
    OnResultCallback callback);

    void addLog(final String mac, final boolean isAdmin);

    void removeLock(final String mac, final OnResultCallback callback);

    void removeUser(final int userId, final String lockMac, final OnResultCallback callback);

    void requestSessionKey(final String lockMac, final OnResultCallback callback);

    void log(final String message);

    boolean locksClockwise(String lockMac);

    void clearData();

    boolean isSignedIn();

    void setSignedIn(boolean signedIn);

}
