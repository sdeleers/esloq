package com.esloq.esloqapp.home;

import com.esloq.esloqapp.data.LockDataRepository;

/**
 * Listens to user actions from the UI ({@link HomeActivity}), retrieves the data and updates
 * the UI as required.
 */
class HomePresenter implements HomeContract.UserActionsListener{

    private final LockDataRepository mLockDataRepository;
    private final HomeContract.View mHomeView;

    public HomePresenter(LockDataRepository lockDataRepository, HomeContract.View
            homeView) {
        mLockDataRepository = lockDataRepository;
        mHomeView = homeView;
    }

    @Override
    public void openLogin() {
        mHomeView.showLogin();
    }

    @Override
    public void onSignOut() {
        mHomeView.signOut();
        mHomeView.showLogin();
        mLockDataRepository.clearData();
        mLockDataRepository.setSignedIn(false);
    }

    @Override
    public void signInSuccess() {
        mHomeView.setProgressIndicator(true);
        mLockDataRepository.setSignedIn(true);
        mLockDataRepository.fetchData(new LockDataRepository.OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                mHomeView.setProgressIndicator(false);
                if (!success) {
                    mHomeView.showFetchDataError();
                }
            }
        });
        mLockDataRepository.registerUserDevice(new LockDataRepository.OnResultCallback() {
            @Override
            public void onResult(boolean success) {
                if (!success) {
                    mHomeView.showRegistrationError();
                }
            }
        });
    }

    @Override
    public void signInFailure() {
        mLockDataRepository.setSignedIn(false);
        mHomeView.showLoginError();
    }

}
