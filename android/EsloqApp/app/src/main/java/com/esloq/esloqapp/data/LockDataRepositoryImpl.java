package com.esloq.esloqapp.data;

import android.util.Base64;

import com.esloq.esloqapp.Cryptography;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of the lock data repository used to access lock data.
 */
public class LockDataRepositoryImpl implements LockDataRepository {

    private final LocalDataServiceApi mLocalApi;
    private final ServerDataServiceApi mServerApi;
    private final PreferencesServiceApi mPreferencesApi;

    public LockDataRepositoryImpl(LocalDataServiceApi localApi, ServerDataServiceApi serviceApi,
                                  PreferencesServiceApi preferencesApi) {
        mLocalApi = localApi;
        mServerApi = serviceApi;
        mPreferencesApi = preferencesApi;
    }

    @Override
    public void registerUserDevice(final OnResultCallback callback) {
        mServerApi.registerUserDevice(new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    mPreferencesApi.setRegistrationTokenSentToServer(true);
                } else {
                    mPreferencesApi.setRegistrationTokenSentToServer(false);
                }
                callback.onResult(success);
            }
        });
    }

    @Override
    public void fetchData(final OnResultCallback callback) {
        mServerApi.fetchUserData(new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    parseAndStore(jsonObject);
                }
                callback.onResult(success);
            }
        });
    }

    @Override
    public void addLock(final String mac, final String name, final boolean lockClockwise, final
    OnResultCallback callback) {
        mServerApi.addLock(mac, name, lockClockwise, new ServerDataServiceApi
                .ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    mLocalApi.addLock(mac, name, lockClockwise);
                    int myUserId = mPreferencesApi.getUserId();
                    mLocalApi.addUserToLock(myUserId, "me", true, mac, true);
                }
                callback.onResult(success);
            }
        });
    }

    /**
     * Sends request to server to add user to lock. When finished <code>onTaskCompleted</code> is
     * called.
     */
    @Override
    public void addUser(final String email, final String mac, final boolean isAdmin, final
    OnResultCallback callback) {
        mServerApi.addUser(email, mac, isAdmin, new ServerDataServiceApi.
                ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    try {
                        JSONObject jsonUser = jsonObject.getJSONObject("user");
                        int id = jsonUser.getInt("id");
                        String firstName = jsonUser.getString("firstName");
                        boolean validated = jsonUser.getBoolean("validated");
                        mLocalApi.addUserToLock(id, firstName, validated, mac, isAdmin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callback.onResult(success);
            }
        });
    }

    @Override
    public void addLog(String mac, boolean locked) {
        mServerApi.addLog(mac, locked, new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                // Logs are stored in local database through GCM
            }
        });
    }

    @Override
    public void removeLock(final String mac, final OnResultCallback callback) {
        mServerApi.removeLock(mac, new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    mLocalApi.removeLock(mac);
                }
                callback.onResult(success);
            }
        });
    }

    @Override
    public void removeUser(final int userId, final String lockMac, final OnResultCallback
            callback) {
        mServerApi.removeUser(userId, lockMac, new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    mLocalApi.removeUserFromLock(userId, lockMac);
                }
                callback.onResult(success);
            }
        });
    }

    @Override
    public void requestSessionKey(String lockMac, final OnResultCallback callback) {
        mServerApi.requestSessionKey(lockMac, new ServerDataServiceApi
                .ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                if (success) {
                    try {
                        String sessionKeyB64 = jsonObject.getString("sessionKey");
                        byte[] sessionKey = Base64.decode(sessionKeyB64.getBytes(), Base64.DEFAULT);

                        String lockTicketB64 = jsonObject.getString("lockTicket");
                        byte[] lockTicket = Base64.decode(lockTicketB64.getBytes(), Base64.DEFAULT);

                        Cryptography.setSessionKey(sessionKey);
                        Cryptography.setLockTicket(lockTicket);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callback.onResult(success);
            }
        });

    }

    @Override
    public void log(final String message) {
        mServerApi.log(message, new ServerDataServiceApi.ServerDataServiceCallback() {
            @Override
            public void onResult(boolean success, JSONObject jsonObject) {
                // No need for a callback
            }
        });
    }

    @Override
    public boolean locksClockwise(String lockMac) {
        return mLocalApi.locksClockwise(lockMac);
    }

    @Override
    public void clearData() {
        mLocalApi.clearDatabase();
    }

    @Override
    public boolean isSignedIn() {
        return mPreferencesApi.isSignedIn();
    }

    @Override
    public void setSignedIn(boolean signedIn) {
        mPreferencesApi.setSignedIn(signedIn);
    }

    private void parseAndStore(JSONObject jsonObject) {
            try {
                mPreferencesApi.setUserId(jsonObject.getInt("userId"));

                JSONArray jsonLocks = jsonObject.getJSONArray("locks");
                for (int i = 0; i < jsonLocks.length(); i++) {
                    JSONObject jsonLock = jsonLocks.getJSONObject(i);
                    String lockMac = jsonLock.getString("lockMac").toUpperCase();

                    JSONArray jsonUsers = jsonLock.getJSONArray("users");
                    mLocalApi.addLock(lockMac, jsonLock.getString("name"), jsonLock.getBoolean("lockClockwise"));
                    for (int j = 0; j < jsonUsers.length(); j++) {
                        JSONObject jsonUser = jsonUsers.getJSONObject(j);
                        int userId = Integer.parseInt(jsonUser.getString("userId"));
                        mLocalApi.addUser(userId, jsonUser.getString("name"), jsonUser.getBoolean
                                ("validated"));
                        mLocalApi.addLockAccess(userId, lockMac, jsonUser.getBoolean("isAdmin"));
                    }

                    /* We can only parse the logs once all the locks and users have been added to the lock file.
                     * Because we need to get the log's user names from the user IDs. */
                    JSONArray jsonLogs = jsonLock.getJSONArray("logs");
                    for (int j = 0; j < jsonLogs.length(); j++) {
                        JSONObject jsonLog = jsonLogs.getJSONObject(j);
                        mLocalApi.addLogToLock(jsonLog.getInt("userId"), lockMac, jsonLog
                                        .getBoolean("lockState"), jsonLog.getLong("timestamp"));
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
}
