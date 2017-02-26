package com.esloq.esloqapp.addlock;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link AddLockActivity}), retrieves the data and updates
 * the UI as required.
 */
class AddLockPresenter implements AddLockContract.UserActionsListener {

    private final LockDataRepository mLockDataRepository;
    private final AddLockContract.View mAddLockView;

    public AddLockPresenter(LockDataRepository lockDataRepository, AddLockContract.View
            addLockView) {
        mLockDataRepository = lockDataRepository;
        mAddLockView = addLockView;
    }

    @Override
    public void addLock(String mac, String name, boolean lockClockwise) {
        mLockDataRepository.addLock(mac, name, lockClockwise, new LockDataRepository
                .OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                if (success) {
                    mAddLockView.showLockList();
                } else {
                    mAddLockView.showAddLockError();
                }
            }
        });
    }
}
