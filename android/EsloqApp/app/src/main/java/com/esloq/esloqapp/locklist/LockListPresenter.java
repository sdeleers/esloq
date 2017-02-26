package com.esloq.esloqapp.locklist;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link LockListActivity}), retrieves the data and updates
 * the UI as required.
 */
class LockListPresenter implements LockListContract.UserActionsListener {

    private final LockDataRepository mLockDataRepository;
    private final LockListContract.View mLockListView;

    public LockListPresenter(LockDataRepository lockDataRepository, LockListContract.View
            lockListView) {
        mLockDataRepository = lockDataRepository;
        mLockListView = lockListView;
    }

    @Override
    public void addLock() {
        mLockListView.showScan();
    }

    @Override
    public void removeLock(String lockMac) {
        mLockDataRepository.removeLock(lockMac, new LockDataRepository.OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                if (!success) {
                    mLockListView.showRemoveLockError();
                }
            }
        });
    }

    @Override
    public void openLockDetails(String lockMac, String lockName) {
        mLockListView.showLockDetails(lockMac,lockName);
    }

    @Override
    public void openLockManagement(String lockMac, String lockName) {
        mLockListView.showLockManagement(lockMac,lockName);
    }
}
