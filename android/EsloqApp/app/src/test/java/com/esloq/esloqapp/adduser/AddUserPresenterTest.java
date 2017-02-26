package com.esloq.esloqapp.adduser;

import com.esloq.esloqapp.data.LockDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddUserPresenter}.
 */
public class AddUserPresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private AddUserContract.View mAddUserView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

    private AddUserPresenter mAddUserPresenter;
    private static final String EMAIL = "my@email.com";
    private static final String LOCK_MAC = "00:00:00:00:00:00";
    private static final boolean IS_ADMIN = true;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddUserPresenter = new AddUserPresenter(mLockDataRepository, mAddUserView);
    }

    @Test
    public void testAddUser_success() {
        // Initialize AddUserPresenter
        mAddUserPresenter.addUser(EMAIL, LOCK_MAC, IS_ADMIN);

        // Callback success is captured
        verify(mLockDataRepository).addUser(eq(EMAIL), eq(LOCK_MAC), eq(IS_ADMIN),
                mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(true);

        // Then progress indicator is hidden and notes are shown in UI
        verify(mAddUserView).showUserList();
    }

    @Test
    public void testAddUser_fail() {
        // Initialize AddUserPresenter
        mAddUserPresenter.addUser(EMAIL, LOCK_MAC, IS_ADMIN);

        // Callback fail is captured
        verify(mLockDataRepository).addUser(eq(EMAIL), eq(LOCK_MAC), eq(IS_ADMIN),
                mOnResultCallback.capture());
        mOnResultCallback.getValue().onResult(false);

        // Then progress indicator is hidden and notes are shown in UI
        verify(mAddUserView).showAddUserError();
    }

}