package com.esloq.esloqapp.lock;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link LockActivity}), retrieves the data and updates
 * the UI as required.
 */
class LockPresenter implements LockContract.UserActionsListener, LockController.Listener {

    private final LockDataRepository mLockDataRepository;
    private final LockContract.View mLockView;
    private final LockController mLockController;

    public LockPresenter(LockDataRepository lockDataRepository, LockController lockController,
                         LockContract.View lockView) {
        mLockDataRepository = lockDataRepository;
        mLockController = lockController;
        mLockController.setListener(this);
        mLockView = lockView;
    }

    @Override
    public void onBindService() {
        mLockView.setProgressIndicator(true);
        mLockView.setLockButtonState(false);
        mLockController.initialize();
    }

    @Override
    public void onUnbindService() {
        mLockController.close();
    }

    @Override
    public void openLockList() {
        mLockView.showLockList();
    }

    @Override
    public void initializationError() {
        mLockView.showLockList();
        mLockDataRepository.log("Initialization error.");
        throw new RuntimeException("Error initializing communication with lock.");
    }

    @Override
    public void deviceNotFound() {
        mLockView.setProgressIndicator(false);
        mLockView.showDeviceNotFound();
        mLockDataRepository.log("Device not found.");
    }

    @Override
    public void hasNetwork(boolean isOnline) {
        // When not online, dismiss progress dialog and set lock button state to false
        if (!isOnline) {
            mLockView.setProgressIndicator(false);
            mLockView.setLockButtonState(false);
        }
        mLockView.setNetworkSnackbar(!isOnline);
    }

    @Override
    public void hasBluetooth(boolean bluetoothEnabled) {
        if (!bluetoothEnabled) {
            mLockView.setProgressIndicator(false);
            mLockView.setLockButtonState(false);
            mLockView.showEnableBluetooth();
        }
    }

    @Override
    public void onConnecting() {
//        mLockView.setLockButtonState(false);
//        mLockView.setNetworkSnackbar(false);
        mLockView.setProgressIndicator(true);
    }

    @Override
    public void lockReady() {
        mLockView.setProgressIndicator(false);
        mLockView.setLockButtonState(true);
    }

    @Override
    public void onLock(String lockMac) {
        mLockView.setLockButtonState(false);
        mLockController.lock();
        mLockDataRepository.addLog(lockMac, true);
    }

    @Override
    public void onUnlock(String lockMac) {
        mLockView.setLockButtonState(false);
        mLockController.unlock();
        mLockDataRepository.addLog(lockMac, false);
    }

    @Override
    public void onLocked() {
        mLockView.setLockButtonState(true);
    }

    @Override
    public void onUnlocked() {
        mLockView.setLockButtonState(true);
    }

    @Override
    public void lowBattery() {
        mLockView.showLowBatteryWarning();
    }

}
