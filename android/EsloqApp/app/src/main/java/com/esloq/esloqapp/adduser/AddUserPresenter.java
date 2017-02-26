package com.esloq.esloqapp.adduser;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link AddUserActivity}), retrieves the data and updates
 * the UI as required.
 */
class AddUserPresenter implements AddUserContract.UserActionsListener {

    private final LockDataRepository mLockDataRepository;
    private final AddUserContract.View mAddUserView;

    public AddUserPresenter(LockDataRepository lockDataRepository, AddUserContract.View addUserView) {
        mLockDataRepository = lockDataRepository;
        mAddUserView = addUserView;
    }

    @Override
    public void addUser(String email, String lockMac, boolean isAdmin) {
        mLockDataRepository.addUser(email, lockMac, isAdmin, new LockDataRepository.OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                if (success) {
                    mAddUserView.showUserList();
                } else {
                    mAddUserView.showAddUserError();
                }
            }
        });
    }
}
