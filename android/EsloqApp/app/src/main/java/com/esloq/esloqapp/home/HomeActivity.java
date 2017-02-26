package com.esloq.esloqapp.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.data.LockDataRepository;
import com.esloq.esloqapp.signinprovider.GoogleIDPProvider;
import com.esloq.esloqapp.signinprovider.IDPProvider;
import com.esloq.esloqapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;


/**
 * Contains methods that are needed the first time a user logs into the application.
 */
public abstract class HomeActivity extends AppCompatActivity implements HomeContract.View {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * Constants to determine which request returns.
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Listener for the user's UI actions.
     */
    private HomeContract.UserActionsListener mActionListener;

    /**
     * Dialog that shows loading when requesting a sign in.
     */
    private ProgressDialog mProgressDialog;

    /**
     * The authentication provider.
     */
    private IDPProvider mIDPProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPlayServices()) {
            LockDataRepository repository = Injection.provideLockDataRepository(getApplicationContext());
            mActionListener = new HomePresenter(repository, this);
            mIDPProvider = new GoogleIDPProvider(this, new IDPProvider.IDPCallback() {
                @Override
                public void onSuccess() {
                    mActionListener.signInSuccess();
                }

                @Override
                public void onFailure() {
                    mActionListener.signInFailure();
                }
            });
            if (!repository.isSignedIn()) {
                mActionListener.openLogin();
            }
            //TODO maybe request token? no info on when this happens instantly and when this
            // happens asynchronously
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIDPProvider.close();
        dismissProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIDPProvider.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mActionListener.onSignOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        if (active) {
            showProgressDialog();
        } else {
            dismissProgressDialog();
        }
    }

    @Override
    public void showLogin() {
        mIDPProvider.startLogin();
    }

    //TODO, make more robust, should broadcast logout and all activities should close themselves.
    //See http://stackoverflow.com/questions/3007998/on-logout-clear-activity-history-stack-preventing-back-button-from-opening-l
    @Override
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        mIDPProvider.signOut();
    }

    @Override
    public void showLoginError() {
        Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFetchDataError() {
        Toast.makeText(this, "Unable to retrieve data.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRegistrationError() {
        Toast.makeText(this, "Unable to register device.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a progress dialog.
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Retrieving data...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    /**
     * Dismisses the progress dialog.
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
