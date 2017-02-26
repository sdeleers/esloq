package com.esloq.esloqapp.addlock;

import com.esloq.esloqapp.data.LockDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddLockPresenter}.
 */
public class AddLockPresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private AddLockContract.View mAddLockView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

//    @Rule
//    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private AddLockPresenter mAddLockPresenter;
    private static final String LOCK_MAC = "00:00:00:00:00:00";
    private static final String LOCK_NAME = "My Lock";
    private static final boolean LOCK_CLOCKWISE = true;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this); // mockito rule can be used instead

        // Get a reference to the class under test
        mAddLockPresenter = new AddLockPresenter(mLockDataRepository, mAddLockView);
    }

    @Test
    public void testAddLock_success() {
        // Given an initialized AddLockPresenter
        mAddLockPresenter.addLock(LOCK_MAC, LOCK_NAME, LOCK_CLOCKWISE);

        // Callback is captured and invoked with stubbed success
        verify(mLockDataRepository).addLock(eq(LOCK_MAC), eq(LOCK_NAME), eq(LOCK_CLOCKWISE),
                mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(true);

        // Then lock list is shown in UI
        verify(mAddLockView).showLockList();
    }

    @Test
    public void testAddLock_fail() {
        // Given an initialized AddLockPresenter
        mAddLockPresenter.addLock(LOCK_MAC, LOCK_NAME, LOCK_CLOCKWISE);

        // Callback is captured and invoked with stubbed success
        verify(mLockDataRepository).addLock(eq(LOCK_MAC), eq(LOCK_NAME), eq(LOCK_CLOCKWISE),
                mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(false);

        // Then add lock error is shown in UI
        verify(mAddLockView).showAddLockError();
    }

}