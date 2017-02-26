package com.esloq.esloqapp.locklist;

import com.esloq.esloqapp.data.LockDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link LockListPresenter}.
 */
public class LockListPresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private LockListContract.View mLockListView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

    private LockListPresenter mLockListPresenter;
    private static final String LOCK_MAC = "00:00:00:00:00:00";
    private static final String LOCK_NAME = "My Lock";

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mLockListPresenter = new LockListPresenter(mLockDataRepository, mLockListView);
    }

    @Test
    public void testRemoveLock_success(){
        mLockListPresenter.removeLock(LOCK_MAC);

        // Callback success is captured
        verify(mLockDataRepository).removeLock(eq(LOCK_MAC), mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(true);

        verify(mLockListView, never()).showRemoveLockError();
    }

    @Test
    public void testRemoveLock_fail() {
        mLockListPresenter.removeLock(LOCK_MAC);

        // Callback success is captured
        verify(mLockDataRepository).removeLock(eq(LOCK_MAC), mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(false);

        verify(mLockListView).showRemoveLockError();
    }

    @Test
    public void testOpenLockDetails(){
        mLockListPresenter.openLockDetails(LOCK_MAC, LOCK_NAME);
        verify(mLockListView).showLockDetails(LOCK_MAC, LOCK_NAME);
    }

    @Test
    public void testOpenLockManagement() throws Exception {
        mLockListPresenter.openLockDetails(LOCK_MAC, LOCK_NAME);
        mLockListView.showLockManagement(LOCK_MAC, LOCK_NAME);
    }
}