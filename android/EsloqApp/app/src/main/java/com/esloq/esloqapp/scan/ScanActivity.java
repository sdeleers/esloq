package com.esloq.esloqapp.scan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esloq.esloqapp.util.DividerItemDecoration;
import com.esloq.esloqapp.R;
import com.esloq.esloqapp.cursorrecycleradapter.ViewClickListener;
import com.esloq.esloqapp.addlock.AddLockActivity;
import com.esloq.esloqapp.lock.BluetoothService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

/**
 * Activity for mScanning and displaying available Bluetooth LE devices. This activity gets called
 * when a user adds a lock in <code>LockListActivity</code>. When the user clicks on a device in the
 * list the <code>AddLockActivity</code> is started that will add the user as admin to the esloq
 * device, if there is no admin on the device yet.
 */
public class ScanActivity extends AppCompatActivity implements ScanContract.View,
        ViewClickListener {

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = ScanActivity.class.getSimpleName();

    /**
     * Constants to determine which task returns.
     */
    private static final int RC_ENABLE_LOCATION = 0;
    private static final int RC_ENABLE_BT = 1;
    private static final int RC_PERMISSION_LOCATION = 2;

    /**
     * Listener for the user's UI actions.
     */
    private ScanContract.UserActionsListener mActionListener;

    /**
     * Bluetooth Adapter used to check if bluetooth is enabled and to get the scanner.
     */
    private BluetoothAdapter mBluetoothAdapter;

//    /**
//     * Used to scan for bluetooth devices.
//     */
//    private BluetoothLeScanner mBluetoothLeScanner;

    /**
     * Whether the BluetoothAdapter is currently mScanning or not.
     */
    private boolean mScanning;

    /**
     * Handler object used to add runnables to the UI thread's message queue. Used to stop mScanning
     * with postDelayed method.
     */
    private final Handler handler = new Handler();

    /**
     * Scan duration in milliseconds.
     */
    private static final long SCAN_PERIOD_MS = 1000;

    private ScanAdapter mScanAdapter;

    private GoogleApiClient mGoogleApiClient;

    /* Device scan callback. */
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mScanAdapter.addDevice(result.getDevice());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.scan_list);
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mScanAdapter = new ScanAdapter();
        recyclerView.setAdapter(mScanAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Add Up to toolbar */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Show dialog to enable location if it is turned off
        mGoogleApiClient = getGoogleApiClient();

        mActionListener = new ScanPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * If Bluetooth is not enabled on the phone, a dialog is presented to the user with the question
     * whether he wants to enable it. Then a BLE scan is started.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Marshmallow and newer require the location permission to be granted and location
        // setting to be on.
        // TODO the flow should be location permission, location setting, bluetooth and close
        // activity on any decline. note that onresume is called after any "activity/dialog for
        // result"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestLocationPermission()) {
                requestLocationSettings();
                mActionListener.onStartScan();
            }
        } else {
            mActionListener.onStartScan();
        }
    }

    /**
     * Stops scanning for devices and clears the list of devices in the ListView.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mActionListener.onStopScan();
        handler.removeCallbacksAndMessages(null);
        mScanAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            mActionListener.openLockList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mActionListener.onStartScan();
                break;
            case R.id.menu_stop:
                mActionListener.onStopScan();
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it. E.g.: for up button
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void setProgressIndicator(boolean active) {
        View view = findViewById(R.id.toolbar_progress_bar);
        assert view != null;
        if (active) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Start scanning. A scan is started after <code>SCAN_PERIOD_MS</code> milliseconds.
     */
    @Override
    public void startScan() {
        if (mBluetoothAdapter.isEnabled()) {
            mScanAdapter.clear(); // Clear all currently listed locks.

            /* When bluetooth is not enabled, mBluetoothLeScanner will
            * be null. Also, without permissions we cannot scan. */
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(BluetoothService.ESLOQ_SERVICE))
                    .build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(scanFilter);

            ScanSettings settings = new ScanSettings.Builder()
                    //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH) // API 23+
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build();

            mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mScanCallback);
            /* Stops scanning after a pre-defined scan period. */
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mActionListener.onStopScan();
                }
            }, SCAN_PERIOD_MS);

            mScanning = true;
            invalidateOptionsMenu();

        } else {
            mActionListener.openEnableBluetooth();
        }
    }

    /**
     * Stop scanning.
     */
    @Override
    public void stopScan() {
        /* This check is needed because when bluetooth adapter is disabled, mBluetoothLeScanner will
         * be null. The user then gets a dialog to enable it which makes it non-null. */
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            invalidateOptionsMenu();
        }
    }

    @Override
    public void showAddLock(String mac) {
        final Intent intent = new Intent(this, AddLockActivity.class);
        intent.putExtra("lockMac", mac);
        startActivity(intent);
        finish();
    }

    @Override
    public void showEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, RC_ENABLE_BT);
    }

    @Override
    public void showLockList() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted. Do location related task.
            } else {
                // Permission denied, disable the functionality that depends on this permission.
                mActionListener.openLockList();
            }
        }
    }

    @Override
    public void onViewClicked(int position, View view) {
        final BluetoothDevice device = mScanAdapter.getDevice(position);
        if (device == null) throw new RuntimeException("Device is null");
        if (mScanning) {
            mActionListener.onStopScan();
        }
        mActionListener.openAddLock(device.getAddress());
    }


    /**
     * Returns whether location permissions were set at time of calling. If not set, they are
     * requested from the user with a dialog.
     *
     * @return True if location permissions were granted, false otherwise.
     */
    private boolean requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Permission Required.")
                        .setMessage("Android requires the location permission to be granted in " +
                                "order to perform Bluetooth scans.")
                        .setIcon(R.drawable.ic_action_warning)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showLocationPermissionDialog();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                // No explanation needed, we can request the permission.
                showLocationPermissionDialog();
            }
            return false;
        }
        return true;
    }

    private void requestLocationSettings() {
        LocationRequest locationRequest = new LocationRequest().setPriority
                (LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder locationRequestBuilder = new LocationSettingsRequest
                .Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, locationRequestBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
//                        final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(ScanActivity.this, RC_ENABLE_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                        builder.setTitle("Location setting not on.")
                                .setMessage("Android requires your location to be on in order to " +
                                        "perform Bluetooth scans.")
                                .setIcon(R.drawable.ic_action_warning)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mActionListener.openLockList();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                }
            }
        });
    }

    private GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this, new GoogleApiClient
                .ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
//                requestLocationSettings();
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.i(TAG, "GoogleApiClient connection suspended.");
            }
        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.i(TAG, "GoogleApiClient connection failed.");
            }
        }).addApi(LocationServices.API).build();
    }

    private void showLocationPermissionDialog() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                .ACCESS_COARSE_LOCATION}, RC_PERMISSION_LOCATION);
    }

    /**
     * Adapter class that is responsible for selecting what data to display in each ViewHolder.
     */
    private class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {

        private final ArrayList<BluetoothDevice> scannedDevices = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scan_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BluetoothDevice device = scannedDevices.get(position);
            holder.deviceName.setText(device.getName());
            holder.deviceAddress.setText(device.getAddress());
        }

        @Override
        public int getItemCount() {
            return scannedDevices.size();
        }

        /**
         * Add device to the adapter.
         *
         * @param device The bluetooth device to be added to the list.
         */
        public void addDevice(BluetoothDevice device) {
            if(!scannedDevices.contains(device)) {
                scannedDevices.add(device);
                notifyDataSetChanged();
            }
        }

        public BluetoothDevice getDevice(int position) {
            return scannedDevices.get(position);
        }

        public void clear() {
            scannedDevices.clear();
            notifyDataSetChanged();
        }

        /**
         * Class containing the views contained within a row of the scan list.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final TextView deviceName;
            private final TextView deviceAddress;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                deviceName = (TextView) itemView.findViewById(R.id.device_name);
                deviceAddress = (TextView) itemView.findViewById(R.id.device_address);
            }

            @Override
            public void onClick(View view) {
                onViewClicked(getAdapterPosition(), view);
            }
        }

    }
}
