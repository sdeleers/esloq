package com.esloq.esloqapp.data;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Listens for and handles new instance IDs.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        LockDataRepository repository = Injection.provideLockDataRepository(getApplicationContext());
        if (repository.isSignedIn()) {
            repository.registerUserDevice(new LockDataRepository.OnResultCallback() {
                @Override
                public void onResult(boolean success) {}
            });
        }
    }
}
