package com.esloq.esloqapp.data;

import org.json.JSONObject;

/**
 * Defines an interface to the backend server service API that is used by the repository. All
 * data request to the server should be piped through this interface.
 */
interface ServerDataServiceApi {


    interface ServerDataServiceCallback {
        void onResult(boolean success, JSONObject jsonObject);
    }

    void fetchUserData(ServerDataServiceCallback callback);

    void registerUserDevice(ServerDataServiceCallback callback);

    void addLock(String mac, String name, boolean lockClockwise, ServerDataServiceCallback
            callback);

    void removeLock(String mac, ServerDataServiceCallback callback);

    void addUser(String email, String mac, boolean isAdmin, ServerDataServiceCallback callback);

    void removeUser(int userId, String lockMac, ServerDataServiceCallback callback);

    void addLog(String lockMac, boolean locked, ServerDataServiceCallback callback);

    void requestSessionKey(String lockMac, ServerDataServiceCallback callback);

    void log(String message, ServerDataServiceCallback callback);

}
