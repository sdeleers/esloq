package com.esloq.esloqapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Implementation of the Shared Preferences Service API that is used to access the shared
 * preferences.
 */
class PreferencesServiceApiImpl implements PreferencesServiceApi {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = PreferencesServiceApiImpl.class.getSimpleName();

    private static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private static final String SIGNED_IN = "signedIn";
    private static final String MY_USER_ID = "myUserId";

    // Temporary variable to resignin after update.
    private static final String RE_SIGNED_IN = "reSignedIn_0";


    private final SharedPreferences preferences;

    public PreferencesServiceApiImpl(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean isSignedIn() {
        return preferences.getBoolean(SIGNED_IN, false) && preferences.getBoolean(RE_SIGNED_IN, false);
    }

    @Override
    public void setSignedIn(boolean signedIn) {
        preferences.edit().putBoolean(SIGNED_IN, signedIn).apply();
        preferences.edit().putBoolean(RE_SIGNED_IN, signedIn).apply();
    }

    @Override
    public int getUserId() {
//        if (!isSignedIn()) throw new IllegalStateException("No user is signed in.");
//        int userId = preferences.getInt(MY_USER_ID, -1);
//        if (userId == -1) throw new IllegalStateException("No user id set.");
//        return userId;
        return preferences.getInt(MY_USER_ID, -1);
    }

    @Override
    public void setUserId(int userId) {
        preferences.edit().putInt(MY_USER_ID, userId).apply();
    }

    @Override
    public boolean isRegistrationTokenSentToServer() {
        //TODO https://developers.google.com/cloud-messaging/registration
        // If the app server fails to complete its part of the registration
        // handshake, the client app should retry sending registration token to
        // the server or delete the registration token.
        return preferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
    }

    @Override
    public void setRegistrationTokenSentToServer(boolean sentToServer) {
        if (sentToServer) {
            Log.i(TAG, "Successfully added registration token to server.");
        } else {
            Log.i(TAG, "Unable to add registration token to server.");
        }
        preferences.edit().putBoolean(SIGNED_IN, sentToServer).apply();
    }


}
