package com.esloq.esloqapp.data;

/**
 * Defines an interface to the shared preferences service API that is used by the repository. All
 * data request to the shared preferences should be piped through this interface.
 */
interface PreferencesServiceApi {

    boolean isSignedIn();

    void setSignedIn(boolean signedIn);

    int getUserId();

    void setUserId(int userId);

    boolean isRegistrationTokenSentToServer();

    void setRegistrationTokenSentToServer(boolean sentToServer);

}
