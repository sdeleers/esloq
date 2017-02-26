package com.esloq.esloqapp.lockmanagment;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link UserListFragment}), retrieves the data and updates
 * the UI as required.
 */
class UserListPresenter implements UserListContract.UserActionsListener {

    private final LockDataRepository mLockDataRepository;
    private final UserListContract.View mUserListView;

    public UserListPresenter(LockDataRepository lockDataRepository, UserListContract.View
            userListView) {
        mLockDataRepository = lockDataRepository;
        mUserListView = userListView;
    }

    @Override
    public void openAddUser(String lockMac) {
        mUserListView.showAddUser(lockMac);
    }

    @Override
    public void removeUser(int userId, String lockMac) {
        mLockDataRepository.removeUser(userId, lockMac, new LockDataRepository.OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                if (!success) {
                    mUserListView.showRemoveUserError();
                }
            }
        });
    }
}
