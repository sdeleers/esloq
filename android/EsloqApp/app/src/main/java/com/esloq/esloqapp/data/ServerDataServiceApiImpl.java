package com.esloq.esloqapp.data;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esloq.esloqapp.BuildConfig;
import com.esloq.esloqapp.util.Tools;
import com.esloq.esloqapp.util.Urls;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of the Server Data Service API used to access the backend server.
 */
class ServerDataServiceApiImpl implements ServerDataServiceApi {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = ServerDataServiceApiImpl.class.getSimpleName();

    private final Context mContext;

    public ServerDataServiceApiImpl(Context context) {
        mContext = context;
    }

    @Override
    public void fetchUserData(ServerDataServiceCallback callback) {
        getAuthTokenAndGetFromServer(Urls.GET_USER_DATA, callback);
    }

    @Override
    public void registerUserDevice(ServerDataServiceCallback callback) {
        String token = FirebaseInstanceId.getInstance().getToken();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("registrationToken", token);
            getAuthTokenAndPostToServer(Urls.ADD_REGISTRATION_TOKEN, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLock(String mac, String name, boolean lockClockwise, ServerDataServiceCallback
                        callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockMac", mac.toLowerCase());
            jsonObject.put("lockName", name);
            jsonObject.put("lockClockwise", lockClockwise);
            getAuthTokenAndPostToServer(Urls.INITIALIZE_LOCK, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeLock(String mac, final ServerDataServiceCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockMac", mac.toLowerCase());
            getAuthTokenAndPostToServer(Urls.RESET_LOCK, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends request to server to add user to lock. When finished <code>onTaskCompleted</code> is
     * called.
     */
    public void addUser(String email, String mac, boolean isAdmin, final
    ServerDataServiceCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("lockMac", mac.toLowerCase());
            jsonObject.put("isAdmin", isAdmin);
            getAuthTokenAndPostToServer(Urls.ADD_USER, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(int userId, String lockMac, ServerDataServiceCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("lockMac", lockMac.toLowerCase());
            getAuthTokenAndPostToServer(Urls.REMOVE_USER, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send request to server to add a new log entry.
     * @param lockMac The mac address of the lock for which to add a log.
     * @param locked The state of the lock.
     */
    @Override
    public void addLog(String lockMac, boolean locked, ServerDataServiceCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockMac", lockMac.toLowerCase());
            jsonObject.put("lockState", locked);
            getAuthTokenAndPostToServer(Urls.ADD_LOG, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request a new session key from the server for the communication with esloq.
     */
    @Override
    public void requestSessionKey(String lockMac, ServerDataServiceCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockMac", lockMac.toLowerCase());
            getAuthTokenAndPostToServer(Urls.REQUEST_SESSION_KEY, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String message, ServerDataServiceCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device", Tools.getDeviceName());
            jsonObject.put("api", android.os.Build.VERSION.SDK_INT);
            jsonObject.put("message", message);
            getAuthTokenAndPostToServer(Urls.LOG, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send data to server.
     * @param url The URL to which the data is sent.
     * @param jsonObject The data that is sent to the server.l
     */
    private void getAuthTokenAndPostToServer(final URL url, final JSONObject jsonObject, final
    ServerDataServiceCallback callback) {
        // Request token
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new IllegalStateException("User is null.");
        Task<GetTokenResult> getTokenResultTask = user.getToken(false);
        getTokenResultTask.addOnCompleteListener(
                new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        // Add token to http request data and send to server.
                        if (task.isSuccessful()) {
                            try {
                                jsonObject.put("idToken", task.getResult().getToken());
                                postToServer(url, jsonObject, callback);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Get data from server.
     *
     * @param url The URL from which the data is received.
     */
    private void getAuthTokenAndGetFromServer(final URL url, final ServerDataServiceCallback
            callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new IllegalStateException("User is null.");
        Task<GetTokenResult> task = user.getToken(false);
        task.addOnCompleteListener(
                new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                URL requestURL = new URL(url, "?idToken=" + task.getResult().getToken());
                                getFromServer(requestURL, callback);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    private void postToServer(final URL url, final JSONObject jsonObject, final
    ServerDataServiceCallback callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "json sent: " + jsonObject);
        if (BuildConfig.DEBUG) Log.d(TAG, "url: " + url);
        final ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SendToServerIntentService.RESULT_OK) {
                    try {
                        JSONObject jsonResponse = new JSONObject(resultData.getString(SendToServerIntentService.RESPONSE));
                        if (jsonResponse.getBoolean("success")) {
                            callback.onResult(true, jsonResponse);
                        } else {
                            Log.e(TAG, "Error: " + jsonResponse.getJSONObject("error").getString("message"));
                            callback.onResult(false, null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onResult(false, null);
                }
            }
        };
        SendToServerIntentService.startActionPostToServer(mContext, receiver, url, jsonObject);
    }

    private void getFromServer(final URL url, final ServerDataServiceCallback callback) {
        final ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SendToServerIntentService.RESULT_OK) {
                    try {
                        JSONObject jsonResponse = new JSONObject(resultData.getString(SendToServerIntentService.RESPONSE));
                        if (jsonResponse.getBoolean("success")) {
                            callback.onResult(true, jsonResponse);
                        } else {
                            Log.e(TAG, "Error: " + jsonResponse.getJSONObject("error").getString("message"));
                            callback.onResult(false, null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onResult(false, null);
                }
            }
        };
        if (BuildConfig.DEBUG) Log.d(TAG, "url: " + url);
        SendToServerIntentService.startActionGetFromServer(mContext, receiver, url);
    }

}
