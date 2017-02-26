package com.esloq.esloqapp.scan;

/**
 * This specifies the contract between the view and the presenter.
 */
interface ScanContract {

    interface View {

        void setProgressIndicator(boolean active);

        void startScan();

        void stopScan();

        void showAddLock(String mac);

        void showEnableBluetooth();

        void showLockList();

    }

    interface UserActionsListener {

        void onStartScan();

        void onStopScan();

        void openAddLock(String lockMac);

        void openEnableBluetooth();

        void openLockList();

    }
}
