package com.esloq.esloqapp.addlock;

/**
 * This specifies the contract between the view and the presenter.
 */
interface AddLockContract {

    interface View {

        void showAddLockError();

        void showLockList();

    }

    interface UserActionsListener {

        void addLock(String mac, String name, boolean lockClockwise);

    }
}
