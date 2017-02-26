package com.esloq.esloqapp.home;

import com.esloq.esloqapp.data.LockDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link HomePresenter}.
 */
public class HomePresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private HomeContract.View mHomeView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

    private HomePresenter mHomePresenter;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mHomePresenter = new HomePresenter(mLockDataRepository, mHomeView);
    }

    @Test
    public void testOpenLogin() {
        mHomePresenter.openLogin();
        verify(mHomeView).showLogin();
    }

    @Test
    public void testSignOut() {
        mHomePresenter.onSignOut();
        verify(mHomeView).signOut();
        verify(mHomeView).showLogin();
        verify(mLockDataRepository).clearData();
        verify(mLockDataRepository).setSignedIn(false);
    }

    @Test
    public void testSignInSuccess_fetchDataSuccess() {
        mHomePresenter.signInSuccess();

        // Callback success is captured
        verify(mLockDataRepository).fetchData(mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(true);

        verify(mHomeView).setProgressIndicator(true);
        verify(mLockDataRepository).setSignedIn(true);
        verify(mHomeView).setProgressIndicator(false);
        verify(mHomeView, never()).showFetchDataError();
    }

    @Test
    public void testSignInSuccess_fetchDataFail() {
        mHomePresenter.signInSuccess();

        // Callback success is captured
        verify(mLockDataRepository).fetchData(mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(false);

        verify(mHomeView).setProgressIndicator(true);
        verify(mLockDataRepository).setSignedIn(true);
        verify(mHomeView).setProgressIndicator(false);
        verify(mHomeView).showFetchDataError();
    }

    @Test
    public void testSignInSuccess_registerUserDeviceSuccess() {
        mHomePresenter.signInSuccess();

        // Callback success is captured
        verify(mLockDataRepository).fetchData(mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(true);

        verify(mHomeView).setProgressIndicator(true);
        verify(mLockDataRepository).setSignedIn(true);
        verify(mHomeView, never()).showRegistrationError();
    }

    @Test
    public void testSignInSuccess_registerUserDeviceFail() {
        mHomePresenter.signInSuccess();

        // Callback success is captured
        verify(mLockDataRepository).registerUserDevice(mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(false);

        verify(mHomeView).setProgressIndicator(true);
        verify(mLockDataRepository).setSignedIn(true);
        verify(mHomeView).showRegistrationError();
    }

    @Test
    public void testSignInFailure() {
        mHomePresenter.signInFailure();

        verify(mLockDataRepository).setSignedIn(false);
        verify(mHomeView).showLoginError();
    }

}