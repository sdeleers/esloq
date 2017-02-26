package com.esloq.esloqapp.lock;

import com.esloq.esloqapp.data.LockDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link LockPresenter}.
 */
public class LockPresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private LockController mLockController;

    @Mock
    private LockContract.View mLockView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

    private LockPresenter mLockPresenter;
    private static final String LOCK_MAC = "00:00:00:00:00:00";

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this); // mockito rule can be used instead

        // Get a reference to the class under test
        mLockPresenter = new LockPresenter(mLockDataRepository, mLockController, mLockView);
    }

    @Test
    public void testOnBindService() {
        mLockPresenter.onBindService();

        verify(mLockView).setProgressIndicator(true);
        verify(mLockView).setLockButtonState(false);
        verify(mLockController).initialize();
    }

    @Test
    public void testOnUnbindService() {
        mLockPresenter.onUnbindService();
        verify(mLockController).close();
    }

    @Test
    public void testOpenLockList() throws Exception {
        mLockPresenter.openLockList();
        verify(mLockView).showLockList();
    }

    @Test
    public void testDeviceNotFound() {
        mLockPresenter.deviceNotFound();

        verify(mLockView).setProgressIndicator(false);
        verify(mLockView).showDeviceNotFound();
    }

    @Test
    public void testHasNetwork_true() {
        mLockPresenter.hasNetwork(true);
        verify(mLockView).setNetworkSnackbar(false);
    }

    @Test
    public void testHasNetwork_false() {
        mLockPresenter.hasNetwork(false);
        verify(mLockView).setProgressIndicator(false);
        verify(mLockView).setLockButtonState(false);
        verify(mLockView).setNetworkSnackbar(true);
    }

    @Test
    public void testHasBluetooth_true() {
        mLockPresenter.hasBluetooth(true);
    }

    @Test
    public void testHasBluetooth_false() {
        mLockPresenter.hasBluetooth(false);
        verify(mLockView).setProgressIndicator(false);
        verify(mLockView).setLockButtonState(false);
        verify(mLockView).showEnableBluetooth();
    }

    @Test
    public void testOnConnecting() {
        mLockPresenter.onConnecting();
        verify(mLockView).setProgressIndicator(true);
    }

    @Test
    public void testLockReady() {
        mLockPresenter.lockReady();
        verify(mLockView).setProgressIndicator(false);
        verify(mLockView).setLockButtonState(true);
    }

    @Test
    public void testOnLock() throws Exception {
        mLockPresenter.onLock(LOCK_MAC);

        verify(mLockView).setLockButtonState(false);
        verify(mLockController).lock();
        verify(mLockDataRepository).addLog(LOCK_MAC, true);
    }

    @Test
    public void testOnUnlock() throws Exception {
        mLockPresenter.onUnlock(LOCK_MAC);

        verify(mLockView).setLockButtonState(false);
        verify(mLockController).unlock();
        verify(mLockDataRepository).addLog(LOCK_MAC, false);
    }

    @Test
    public void testOnLocked() throws Exception {
        mLockPresenter.onLocked();
        verify(mLockView).setLockButtonState(true);
    }

    @Test
    public void testOnUnlocked() throws Exception {
        mLockPresenter.onUnlocked();
        verify(mLockView).setLockButtonState(true);
    }

}