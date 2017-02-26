package com.esloq.esloqapp.lock;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Implementation of the lock controller, used to control the lock.
 */
class LockControllerImpl implements LockController {

    private final Context mContext;
    private final String mLockMac;
    private final LockDataRepository mRepository;
    private LockController.Listener mListener;

    /**
     * Used to interact with the <code>LockService</code> when communicating with the esloq.
     * over Bluetooth.
     */
    private BluetoothService bluetoothService;

    /**
     * Manages the LockService lifecycle.
     */
    private final ServiceConnection lockServiceConnection = new ServiceConnection() {

        /**
         * Gets executed when successfully bound with the LockService. Initializes the
         * lockService object that will be used to call methods on the LockService. Initializes the
         * LockService's Bluetooth adapter and connects to the GATT server on the lock that
         * corresponds with this activity.
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!bluetoothService.initialize()) {
                mListener.initializationError();
            }
            mListener.hasBluetooth(hasBluetoothConnectivity());
            mListener.hasNetwork(hasNetworkConnectivity());
            onConnectToLock();
        }

        /**
         * Gets executed when the connection to LockService is unexpectedly lost. This is not
         * called when the client unbinds.
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
            throw new RuntimeException("Connection unexpectedly lost.");
        }
    };

    /**
     * Receives broadcasts from the BluetoothService class. If it receives broadcasts that signify a
     * successful lock or unlock action it updates the lock state on the UI. Similarly it receives
     * broadcasts for the connection state and updates the UI accordingly. When the ACTION_CONNECTED
     * broadcast is received, a session key (used for authenticated encrypted communication with the
     * esloq) will be requested from the server. After the user has successfully reset the lock it
     * will receive an ACTION_LOCK_RESET broadcast and remove te lock from the local user's lock list.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    boolean isOnline = hasNetworkConnectivity();
                    mListener.hasNetwork(isOnline);
                    if (isOnline) {
                        onConnectToLock();
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF: // also triggers ACTION_DISCONNECT
                            mListener.hasBluetooth(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            mListener.hasBluetooth(true);
                            onConnectToLock();
                            break;
                    }
                    break;
                case BluetoothService.ACTION_ERROR:
                    mListener.initializationError();
                    break;
                case BluetoothService.ACTION_DISCONNECTED:
                    onConnectToLock();
                    break;
                case BluetoothService.ACTION_TICKET_RCV_SUCCESS:
                    mListener.lockReady();
                    break;
                case BluetoothService.ACTION_TICKET_RCV_SUCCESS_LOW_BAT:
                    mListener.lockReady();
                    mListener.lowBattery();
                    break;
                case BluetoothService.ACTION_UNLOCKED:
                    mListener.onUnlocked();
                    break;
                case BluetoothService.ACTION_LOCKED:
                    mListener.onLocked();
                    break;
                case BluetoothService.ACTION_INVALID_REQUEST:
                    throw new RuntimeException("Invalid request.");
                case BluetoothService.ACTION_COMMUNICATION_READY:
                    mRepository.requestSessionKey(mLockMac, new LockDataRepository.OnResultCallback() {
                        @Override
                        public void onResult(boolean success) {
                            if (success) {
                                bluetoothService.sendTicket();
                            } else {
                                mListener.initializationError();
                            }
                        }
                    });
                    break;
                case BluetoothService.ACTION_DEVICE_NOT_FOUND:
                    mListener.deviceNotFound();
                    break;
            }
        }
    };

    public LockControllerImpl(Context context, String lockMac){
        mContext = context;
        mLockMac = lockMac;
        mRepository = Injection.provideLockDataRepository(context);
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Bind to bluetooth service.
     */
    @Override
    public void initialize() {
        Intent gattServiceIntent = new Intent(mContext, BluetoothService.class);
        mContext.bindService(gattServiceIntent, lockServiceConnection, Context.BIND_AUTO_CREATE);
        mContext.registerReceiver(broadcastReceiver, makeBroadcastReceiverIntentFilter());
    }

    public void lock() {
        if (mRepository.locksClockwise(mLockMac)) {
            bluetoothService.rotateClockwise();
        } else {
            bluetoothService.rotateCounterClockwise();
        }
    }

    public void unlock() {
        if (mRepository.locksClockwise(mLockMac)) {
            bluetoothService.rotateCounterClockwise();
        } else {
            bluetoothService.rotateClockwise();
        }
    }

    /**
     * Unbind from bluetooth service.
     */
    public void close() {
        mContext.unregisterReceiver(broadcastReceiver);
        if(bluetoothService != null) {
            mContext.unbindService(lockServiceConnection);
            bluetoothService = null;
        }
    }

    private void onConnectToLock() {
        if (hasNetworkConnectivity() && hasBluetoothConnectivity()) {
            mListener.onConnecting();
            connect();
        }
    }

    /**
     * Check if there is network connectivity.
     *
     * @return True if there is network connectivity, false otherwise.
     */
    private boolean hasNetworkConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean hasBluetoothConnectivity() {
        return bluetoothService != null && bluetoothService.bluetoothEnabled();
    }

    /**
     * Connects to the GATT server on the esloq, if BT and network are enabled and we're bound to
     * the BluetoothService. To solve the problem when we're out of Bluetooth range a handler is
     * used to keep trying to connect until we are in Bluetooth range. The handler is stopped
     * once we connect to the GATT server on the lock (CONNECTED in broadcastreceiver).
     */
    private void connect() {
        if (!bluetoothService.connect(mLockMac)) {
            throw new RuntimeException("Cannot connect to lock.");
        }
    }

    /**
     * Specifies which broadcasts the broadcast receiver will listen for.
     *
     * @return  The intent filter.
     */
    private static IntentFilter makeBroadcastReceiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_ERROR);
        intentFilter.addAction(BluetoothService.ACTION_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_UNLOCKED);
        intentFilter.addAction(BluetoothService.ACTION_LOCKED);
        intentFilter.addAction(BluetoothService.ACTION_INVALID_REQUEST);
        intentFilter.addAction(BluetoothService.ACTION_COMMUNICATION_READY);
        intentFilter.addAction(BluetoothService.ACTION_TICKET_RCV_SUCCESS);
        intentFilter.addAction(BluetoothService.ACTION_TICKET_RCV_SUCCESS_LOW_BAT);
        intentFilter.addAction(BluetoothService.ACTION_DEVICE_NOT_FOUND);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }
}
