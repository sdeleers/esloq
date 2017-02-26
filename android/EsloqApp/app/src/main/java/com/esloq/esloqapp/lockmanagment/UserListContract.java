package com.esloq.esloqapp.lockmanagment;

/**
 * This specifies the contract between the view and the presenter.
 */
interface UserListContract {
    interface View {

        void showAddUser(String lockMac);

        void showRemoveUserError();
//
//        void showLockDetails(String lockMac, String lockName);
//
//        void showLockManagement(String lockMac, String lockName);

    }

    interface UserActionsListener {

        void openAddUser(String lockMac);

        void removeUser(int userId, String lockMac);

//        void openLockDetails(String lockMac, String lockName);
//
//        void openLockManagement(String lockMac, String lockName);
    }
}
