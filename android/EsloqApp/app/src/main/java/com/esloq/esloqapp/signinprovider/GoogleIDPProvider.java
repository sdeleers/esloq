package com.esloq.esloqapp.signinprovider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esloq.esloqapp.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Provider for signing in with Google.
 */
public class GoogleIDPProvider implements IDPProvider {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = GoogleIDPProvider.class.getSimpleName();

    /**
     * Constant to determine which request returned.
     */
    private static final int RC_SIGN_IN = 0;

    /**
     * The Activity that called this provider.
     */
    private final Activity mActivity;

    /**
     * The interface to which success and failure is reported via a callback.
     */
    private final IDPCallback mIDPCallback;

    /**
     * Firebase auth instance in which to sign in using a Google credential.
     */
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Google API Client through which sign in requests are handled.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Construct a GoogleIDPProvider.
     *
     * @param activity The activity from which this provider is called
     * @param idpCallback The callback interface to report success/failure.
     */
    public GoogleIDPProvider(Activity activity, IDPProvider.IDPCallback idpCallback) {
        mActivity = activity;
        mIDPCallback = idpCallback;

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        mGoogleApiClient.connect();
    }

    @Override
    public void startLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                mIDPCallback.onFailure();
            }
        } else {
            startLogin();
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    /**
     * Use GoogleSignInAccount to sign into Firebase.
     *
     * @param acct The account used to sign into Firebase.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Use the token received via the google sign in to obtain a credential that is then used
        // to sign into Firebase.
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            mIDPCallback.onSuccess();
                        } else {
                            mIDPCallback.onFailure();
                        }
                    }
                });
    }

    private void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

}
