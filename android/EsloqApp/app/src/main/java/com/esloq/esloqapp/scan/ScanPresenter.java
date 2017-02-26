package com.esloq.esloqapp.scan;

/**
 * Listens to user actions from the UI ({@link ScanActivity}), retrieves the data and updates
 * the UI as required.
 */
class ScanPresenter implements ScanContract.UserActionsListener {

    private final ScanContract.View mScanView;

    public ScanPresenter(ScanContract.View scanView) {
        mScanView = scanView;
    }

    @Override
    public void onStartScan() {
        mScanView.setProgressIndicator(true);
        mScanView.startScan();
    }

    @Override
    public void onStopScan() {
        mScanView.stopScan();
        mScanView.setProgressIndicator(false);
    }

    @Override
    public void openAddLock(String lockMac) {
        mScanView.showAddLock(lockMac);
    }

    @Override
    public void openEnableBluetooth() {
        mScanView.showEnableBluetooth();
    }

    @Override
    public void openLockList() {
        mScanView.showLockList();
    }

}
