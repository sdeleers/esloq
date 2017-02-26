package com.esloq.esloqapp.lock;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.esloq.esloqapp.BuildConfig;
import com.esloq.esloqapp.Cryptography;
import com.esloq.esloqapp.util.Tools;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * Service for communicating with an esloq device over Bluetooth Low Energy.
 */
public class BluetoothService extends Service {

    /**
     * UUID's for the esloq GATT service and the RX/TX characteristics that are used for
     * communicating with the lock over BLE. The local device writes to the S_LOCK_RX characteristic
     * to send data to the lock and receives notifications through the C_ESLOQ_TX characteristic.
     * The UUID for the descriptor is used to enable notifications for the C_ESLOQ_TX characteristic.
     */
    public static final String ESLOQ_SERVICE = "302cf927-d510-4597-882a-caee2ae8d45b";
    private static final String C_ESLOQ_TX = "5562c9f9-11b0-4b79-aa58-25ffd87c8d5d";
    private static final String C_ESLOQ_RX = "3284c360-45ec-421a-952a-0928384e412c";
    private static final String C_ESLOQ_TX_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    /**
     * String representations of the broadcasts that the service can send. These broadcasts are
     * picked up by receivers in various Activities.
     */
    public final static String ACTION_ERROR = "com.esloq.bluetooth.le.ACTION_ERROR";
    public final static String ACTION_CONNECTED = "com.esloq.bluetooth.le.ACTION_CONNECTED";
    public final static String ACTION_DISCONNECTED = "com.esloq.bluetooth.le.ACTION_DISCONNECTED";
    public final static String ACTION_UNLOCKED = "com.esloq.bluetooth.le.ACTION_UNLOCKED";
    public final static String ACTION_LOCKED = "com.esloq.bluetooth.le.ACTION_LOCKED";
    public final static String ACTION_INVALID_REQUEST = "com.esloq.bluetooth.le.ACTION_INVALID_REQUEST";
    public final static String ACTION_COMMUNICATION_READY = "com.esloq.bluetooth.le.ACTION_COMMUNICATION_READY";
    public final static String ACTION_TICKET_RCV_SUCCESS = "com.esloq.bluetooth.le.ACTION_TICKET_RCV_SUCCESS";
    public final static String ACTION_TICKET_RCV_SUCCESS_LOW_BAT = "com.esloq.bluetooth.le.ACTION_TICKET_RCV_SUCCESS_LOW_BAT";
    public final static String ACTION_DEVICE_NOT_FOUND = "com.esloq.bluetooth.le.ACTION_DEVICE_NOT_FOUND";

    private static ByteBuffer incomingMessageBuffer;
    private final static int INCOMING_MESSAGE_LENGTH = 41;

    /**
     * Name of this class used for logging.
     */
    private static final String TAG = BluetoothService.class.getSimpleName();

    /**
     * Binder object that allows other classes to bind with the service.
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * High level manager used to obtain an instance of an BluetoothAdapter and to conduct overall
     * Bluetooth Management.
     */
    private BluetoothManager mBluetoothManager;

    /**
     * Represents the local device Bluetooth adapter. Lets the service instantiate a BluetoothDevice
     * object whose GATT server the service can then connect to.
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * MAC address of the esloq's Bluetooth chip that the service needs to connect to.
     */
    private String mBluetoothDeviceAddress;

    /**
     * Used to connect to an esloq's GATT server.
     */
    private BluetoothGatt bluetoothGatt;

    /**
     * Handler object used to add runnables to the UI thread's message queue. Used to stop
     * connecting with postDelayed method.
     */
    private final Handler handler = new Handler();

    /**
     * Duration to try to connect to device in milliseconds.
     */
    private static final long CONNECT_PERIOD_MS = 7000;

    /**
     * Callback that gets passed along when connecting to an esloq's GATT server. Whenever the state
     * of the connection with the GATT server changes, services are discovered on the device, a GATT
     * characteristic changes or a descriptor gets written the callback's corresponding method gets
     * called.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /**
         * Gets called when the state of the connection with the GATT server changes. Broadcasts
         * the change which will be picked up by the receiver in the LockActivity class.
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handler.removeCallbacksAndMessages(null);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            bluetoothGatt.discoverServices());
                    Intent intent = new Intent(ACTION_CONNECTED);
                    sendBroadcast(intent);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    Intent intent = new Intent(ACTION_DISCONNECTED);
                    sendBroadcast(intent);
                }
            } else {
                String errorMsg = "Error changing GATT state: " + String.valueOf(status);
                Log.e(TAG, errorMsg);
                Crashlytics.log(Log.ERROR, TAG, errorMsg);
                Intent intent = new Intent(ACTION_ERROR);
                sendBroadcast(intent);
            }
        }

        /**
         * Gets called when GATT services are discovered on the device. If the esloq service was
         * discovered the service subscribes to the C_ESLOQ_TX notification that is used to send
         * data from the esloq to the local device.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // After services are discovered, subscribe to notification.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (bluetoothGattHasService(ESLOQ_SERVICE)) {
                    setCharacteristicNotification(ESLOQ_SERVICE, C_ESLOQ_TX, true);
                } else { //if (BuildConfig.DEBUG) {
                    throw new RuntimeException("Device does not have ESLOQ_SERVICE.");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * Gets called whenever a characteristic changes on the esloq's GATT server. If the
         * characteristic is C_ESLOQ_TX, the handleMessage method gets called which decodes the
         * message and takes the appropriate action.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // When lock status is read (after services are discovered), this is called.
            if(C_ESLOQ_TX.equals(characteristic.getUuid().toString())) {
                handleMessage(characteristic.getValue());
            }
        }

        /**
         * Gets called after a characteristic's descriptor has been successfully written. This
         * happens in the setCharacteristicNotification method to subscribe to a notification.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // After services are discovered, setCharacteristicNotification is called which writes a descriptor.
            // After the descriptor is written, the current state of the lock has to be requested.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                incomingMessageBuffer = ByteBuffer.allocate(INCOMING_MESSAGE_LENGTH);
                Intent intent = new Intent(ACTION_COMMUNICATION_READY);
                sendBroadcast(intent);
            }
        }
    };

    /**
     * Returns if bluetooth is enabled.
     *
     * @return true if bluetooth is enabled, false otherwise.
     */
    public boolean bluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Send lock ticket to the device.
     */
    public void sendTicket() {
        if(!sendMessage(Cryptography.getLockTicket())) {
            Log.e(TAG, "Error sending ticket");
        }
    }

    /**
     * Send rotate clockwise command to the device.
     */
    public void rotateClockwise() {
        if (!sendEncryptedMessage(new byte[]{(byte) LockRequestCode.ROTATE_CLOCKWISE.getIntValue()})) {
            Log.e(TAG, "Error sending rotate clockwise command");
        }
    }

    /**
     * Send rotate counter clockwise command to the device.
     */
    public void rotateCounterClockwise() {
        if (!sendEncryptedMessage(new byte[]{(byte) LockRequestCode.ROTATE_COUNTER_CLOCKWISE.getIntValue()})) {
            Log.e(TAG, "Error sending rotate counter clockwise command");
        }
    }

    /**
     * Local binder class so that other classes can bind to this service.
     */
    public class LocalBinder extends Binder {

        /**
         * Returns an this instance of BluetoothService.
         *
         * @return This service.
         */
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        handler.removeCallbacksAndMessages(null); // Wont work when multiple classes are bound.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return True if the initialization is successful, false otherwise.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the onConnectionStateChange callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.");
            return bluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        if (bluetoothGatt != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                    final Intent intent = new Intent(ACTION_DEVICE_NOT_FOUND);
                    sendBroadcast(intent);
                }
            }, CONNECT_PERIOD_MS);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Gets called whenever the C_ESLOQ_TX characteristic on the esloq's GATT server changes.
     * Reads the message's opcode and takes the appropriate action based on this opcode. The
     * method broadcasts an intent which will be picked up by an activity's broadcast receiver.
     * The method contains an implementation for every opcode that the esloq might send, all of
     * them ending in '_RSP'.
     *
     * @param message   the message's byte stream
     */
    private void handleMessage(byte[] message) {
        if (BuildConfig.DEBUG) Log.d(TAG,"Incoming message: " + Arrays.toString(message) + ", " +
                "length = " + message.length);

        if(incomingMessageBuffer.position() == 40) {
            /* Because uC always sends 20 bytes, even though last packet should only be 1 byte long (the 41st byte). */
            incomingMessageBuffer.put(message[0]);
        }
        else {
            incomingMessageBuffer.put(message);
        }

        if(incomingMessageBuffer.position() == incomingMessageBuffer.limit()) {
            /* Last packet of message, so process message */

            byte[] incomingMessage = new byte[INCOMING_MESSAGE_LENGTH];

            incomingMessageBuffer.clear(); /* Set position to 0, needs to happen before and after calling get apparently. */
            incomingMessageBuffer.get(incomingMessage);
            incomingMessageBuffer.clear(); /* Set position to 0, needs to happen before and after calling get apparently. */

            byte[] nonce = Arrays.copyOfRange(incomingMessage, 0, Cryptography.NONCE_LENGTH);
            Cryptography.setSessionNonce(nonce);

            byte[] ciphertext = Arrays.copyOfRange(incomingMessage, Cryptography.NONCE_LENGTH, Cryptography.NONCE_LENGTH + Cryptography.MAC_LENGTH + 1);
            byte[] plaintext = Cryptography.authDecrypt(ciphertext);

            if (plaintext.length == 0) {
                throw new RuntimeException("No bluetooth message received.");
            }
            LockResponseCode lockResponseCode = LockResponseCode.fromInteger(plaintext[0]);
            final Intent intent;
            switch (lockResponseCode) {
                case UNLOCKED:
                    intent = new Intent(ACTION_UNLOCKED);
                    sendBroadcast(intent);
                    break;
                case LOCKED:
                    intent = new Intent(ACTION_LOCKED);
                    sendBroadcast(intent);
                    break;
                case TICKET_RCV_SUCCESS:
                    intent = new Intent(ACTION_TICKET_RCV_SUCCESS);
                    sendBroadcast(intent);
                    break;
                case TICKET_RCV_SUCCESS_LOW_BAT:
                    intent = new Intent(ACTION_TICKET_RCV_SUCCESS_LOW_BAT);
                    sendBroadcast(intent);
                    break;
                case TICKET_RCV_FAILURE:
                    break;
                case INVALID_REQUEST:
                    intent = new Intent(ACTION_INVALID_REQUEST);
                    sendBroadcast(intent);
                    break;
                default:
                    Log.w(TAG, "Invalid lock message code.");
                    break;
            }
        }
    }

    /**
     * Returns whether the GATT server on the device (with MAC address
     * <code>mBluetoothDeviceAddress</code>) contains the specified service.
     *
     * @param service_uuid  The UUID of the GATT service.
     * @return  True if the device's GATT server contains the service, false otherwise.
     */
    private boolean bluetoothGattHasService(String service_uuid) {
        return bluetoothGatt.getService(UUID.fromString(service_uuid)) != null;
    }

    /**
     * Subscribes or unsubscribes to the notification of a GATT characteristic.
     *
     * @param service_uuid  UUID of the service that the characteristic belongs to.
     * @param characteristic_uuid   UUID of the characteristic.
     * @param enabled   Enable or disable the notifications
     */
    private void setCharacteristicNotification(String service_uuid, String characteristic_uuid,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(service_uuid));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristic_uuid));
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(C_ESLOQ_TX_DESCRIPTOR));

        if(enabled) {
            //Enable local notifications
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            // Enable remote notifications
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
        else {
            // Disable local notifications
            bluetoothGatt.setCharacteristicNotification(characteristic, false);
            // Disable remote notifications
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Writes a value to a GATT characteristic.
     *
     * @param service_uuid  UUID of the service that the characteristic belongs to.
     * @param characteristic_uuid   UUID of the characteristic.
     * @param value Byte array to be written to the characteristic.
     */
    private boolean writeCharacteristic(String service_uuid, String characteristic_uuid, byte[]
            value) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(service_uuid));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristic_uuid));
        characteristic.setValue(value);
        return bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Sends message to device
     *
     * @param message The message that will be sent to the device.
     * @return True if the message was successfully sent, false otherwise.
     */
    private boolean sendMessage(byte[] message) {
        if (BuildConfig.DEBUG) Log.d(TAG,"Outgoing message: " + Arrays.toString(message) + ", " +
                "length = " + message.length);
        return writeCharacteristic(ESLOQ_SERVICE, C_ESLOQ_RX, message);
    }

    /**
     * Sends an encrypted and authenticated message to device.
     *
     * @param message The plaintext message that needs to be sent to the device, after encryption/authentication.
     * @return True if the message was successfully sent, false otherwise.
     */
    private boolean sendEncryptedMessage(byte[] message) {
        byte[] nonce = Cryptography.getNextSessionNonce();
        byte[] encryptedMessage = Tools.concatenateBytes(nonce, Cryptography.authEncrypt(message));
        return sendMessage(encryptedMessage);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the onConnectionStateChange callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return;
        }
        bluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

}

