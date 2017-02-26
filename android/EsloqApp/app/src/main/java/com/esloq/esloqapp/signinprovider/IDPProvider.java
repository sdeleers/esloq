package com.esloq.esloqapp.signinprovider;

import android.content.Intent;

/**
 * Provider interface that allows for easily adding new sign in providers.
 */
public interface IDPProvider {

    /**
     * Start the login process.
     */
    void startLogin();

    /**
     * Sign out of this provider.
     */
    void signOut();

    /**
     * If the provider decides to start an activity to obtain login credentials, the results of
     * that activity is forwarded to the provider.
     *
     * @param requestCode The request code originally supplied to startActivityForResult() that
     *                    allows for identifying which request was made.
     * @param resultCode The result code returned by the child activity through setResult().
     * @param data An Intent which can return result data to the caller.
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Close the connection to the provider.
     */
    void close();

    /**
     * Callbacks indicating if the provider was able to successfully login the user.
     */
    interface IDPCallback {
        void onSuccess();
        void onFailure();
    }

}
