package com.esloq.esloqapp.lock;

/**
 * This specifies the contract between the view and the presenter.
 */
interface LockContract {

    interface View {

        void setNetworkSnackbar(boolean active);

        void setProgressIndicator(boolean active);

        void setLockButtonState(boolean active);

        void showLockList();

        void showEnableBluetooth();

        void showDeviceNotFound();

        void showLowBatteryWarning();

    }

    interface UserActionsListener {

        void onBindService();

        void onUnbindService();

        void onLock(String lockMac);

        void onUnlock(String lockMac);

        void openLockList();

    }
}
