package com.esloq.esloqapp.data;

/**
 * Defines an interface to the local database service API that is used by the repository. All data
 * request to the local database should be piped through this interface.
 */
interface LocalDataServiceApi {

    void addLock(String mac, String name, boolean lockClockwise);

    void addLockAccess(int userId, String mac, boolean isAdmin);

    void addLogToLock(int userId, String mac, boolean locked, long timestamp);

    void addUser(int id, String name, boolean validated);

    void addUserToLock(int userId, String name, boolean validated, String mac, boolean isAdmin);

    void removeLock(String mac);

    void removeUserFromLock(int userId, String lockMac);

    void updateUser(int id, String name, boolean validated);

    void clearDatabase();

    boolean locksClockwise(String lockMac);

}
