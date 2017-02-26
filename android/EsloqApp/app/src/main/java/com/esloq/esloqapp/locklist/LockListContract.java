package com.esloq.esloqapp.locklist;

/**
 * This specifies the contract between the view and the presenter.
 */
interface LockListContract {

    interface View {

        void showScan();

        void showRemoveLockError();

        void showLockDetails(String lockMac, String lockName);

        void showLockManagement(String lockMac, String lockName);

    }

    interface UserActionsListener {

        void addLock();

        void removeLock(String lockMac);

        void openLockDetails(String lockMac, String lockName);

        void openLockManagement(String lockMac, String lockName);
    }
}
