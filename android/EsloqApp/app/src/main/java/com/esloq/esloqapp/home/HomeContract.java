package com.esloq.esloqapp.home;

/**
 * This specifies the contract between the view and the presenter.
 */
interface HomeContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showLogin();

        void signOut();

        void showLoginError();

        void showFetchDataError();

        void showRegistrationError();

    }

    interface UserActionsListener {

        void openLogin();

        void onSignOut();

        void signInSuccess();

        void signInFailure();

    }
}
