package com.esloq.esloqapp.lock;

/**
 * Interface to control the lock.
 */
interface LockController {

    void setListener(Listener listener);

    void initialize();

    void lock();

    void unlock();

    void close();

    interface Listener {

        void initializationError();

        void deviceNotFound();

        void hasNetwork(boolean isOnline);

        void hasBluetooth(boolean bluetoothEnabled);

        void onConnecting();

        void lockReady();

        void onLocked();

        void onUnlocked();

        void lowBattery();
    }

}
