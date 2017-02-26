package com.esloq.esloqapp.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.R;

/**
 * Activity where the user can lock or unlock the device.
 */
public class LockActivity extends AppCompatActivity implements LockContract.View {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = LockActivity.class.getSimpleName();

    private LockContract.UserActionsListener mActionListener;

    /**
     * Constant to determine which task returns.
     */
    private static final int RC_ENABLE_BT = 0;

    /**
     * The lock corresponding with this activity.
     */
    private String lockMac;

    /**
     * Indicator that there is no internet connection available.
     */
    private Snackbar mConnectionSnackBar;

    /**
     * Dialog that shows loading when requesting a sign in.
     */
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Add Up button to toolbar */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        /* Get the lock that is represented by this activity */
        lockMac = intent.getStringExtra("lockMac");
        setTitle(intent.getStringExtra("lockName"));

        LockController lockController = new LockControllerImpl(this, lockMac);
        mActionListener = new LockPresenter(Injection.provideLockDataRepository(getApplicationContext()), lockController, this);
    }

    /**
     * Bind to the lock service and register the broadcast receiver.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mActionListener.onBindService();
    }

    /**
     * Unbind to the lock service and unregister the broadcast receiver.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mActionListener.onUnbindService();
        dismissProgressDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_ENABLE_BT && resultCode != Activity.RESULT_OK) {
            mActionListener.openLockList();
        }
    }

    @Override
    public void setNetworkSnackbar(boolean active) {
        if(active) {
            showNoConnectionSnackbar();
        }
        else {
            dismissNoConnectionSnackbar();
        }
    }

    @Override
    public void setProgressIndicator(boolean active) {
        if (active) {
            showProgressDialog();
        } else {
            dismissProgressDialog();
        }
    }

    /**
     * Sets the lock and unlock button to active or not active.
     * @param active Enables or disables the lock and unlock buttons.
     */
    @Override
    public void setLockButtonState(boolean active) {
        ImageButton lockButton = (ImageButton) findViewById(R.id.button_lock);
        ImageButton unlockButton = (ImageButton) findViewById(R.id.button_unlock);
        assert lockButton != null && unlockButton != null;
        lockButton.setEnabled(active);
        unlockButton.setEnabled(active);
    }

    @Override
    public void showLockList() {
        finish();
    }

    @Override
    public void showEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, RC_ENABLE_BT);
    }

    @Override
    public void showDeviceNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device not found.")
                .setMessage("If device is in range, try turning Bluetooth off and on " +
                        "and try again.")
                .setIcon(R.drawable.ic_action_warning)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void showLowBatteryWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Low Battery Warning.")
                .setMessage("Please change the esloq's batteries.")
                .setIcon(R.drawable.ic_action_warning)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * When the lock button is pressed, send lock command to the esloq and deactivate buttons.
     *
     * @param view The View that has been pressed.
     */
    public void onLock(View view) {
        mActionListener.onLock(lockMac);
    }

    /**
     * When the unlock button is pressed, send unlock command to the esloq and deactivate buttons.
     *
     * @param view The View that has been pressed.
     */
    public void onUnlock(View view) {
        mActionListener.onUnlock(lockMac);
    }

    /**
     * Show the snackbar indicating there is no network connection.
     */
    private void showNoConnectionSnackbar() {
        if(mConnectionSnackBar == null) {
            //TODO CHANGE TO main_content??????
            View view = findViewById(android.R.id.content);
            assert view != null;
            mConnectionSnackBar = Snackbar.make(view, "No Internet Connection", Snackbar
                    .LENGTH_INDEFINITE);
        }
        mConnectionSnackBar.show();
    }

    /**
     * Dismiss the snackbar indicating there is no network connection.
     */
    private void dismissNoConnectionSnackbar() {
        if(mConnectionSnackBar != null && mConnectionSnackBar.isShown()) {
            mConnectionSnackBar.dismiss();
        }
    }

    /**
     * Shows a progress dialog.
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Connecting…");
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            mProgressDialog.setCanceledOnTouchOutside(false);
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
}
