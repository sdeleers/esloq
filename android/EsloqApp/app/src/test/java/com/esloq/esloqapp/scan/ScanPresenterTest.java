package com.esloq.esloqapp.scan;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link ScanPresenter}.
 */
public class ScanPresenterTest {

    @Mock
    private ScanContract.View mScanView;

    private ScanPresenter mScanPresenter;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this); // mockito rule can be used instead

        // Get a reference to the class under test
        mScanPresenter = new ScanPresenter(mScanView);
    }

    @Test
    public void testOnStartScan() throws Exception {
        mScanPresenter.onStartScan();
        verify(mScanView).startScan();
    }

    @Test
    public void testOnStopScan() throws Exception {
        mScanPresenter.onStopScan();
        verify(mScanView).stopScan();
    }

    @Test
    public void testOpenAddLock() throws Exception {
        String lockMac = "00:00:00:00:00:00";
        mScanPresenter.openAddLock(lockMac);
        verify(mScanView).showAddLock(lockMac);
    }

    @Test
    public void testOpenEnableBluetooth() throws Exception {
        mScanPresenter.openEnableBluetooth();
        verify(mScanView).showEnableBluetooth();
    }
}