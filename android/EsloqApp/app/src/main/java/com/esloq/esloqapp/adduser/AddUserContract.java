package com.esloq.esloqapp.adduser;

/**
 * This specifies the contract between the view and the presenter.
 */
interface AddUserContract {

    interface View {

        void showAddUserError();

        void showUserList();

    }

    interface UserActionsListener {

        void addUser(String lockMac, String lockName, boolean isAdmin);

    }
}
