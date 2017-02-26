package com.esloq.esloqapp.lockmanagment;

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
 * Unit tests for the implementation of {@link UserListPresenter}.
 */
public class UserListPresenterTest {

    @Mock
    private LockDataRepository mLockDataRepository;

    @Mock
    private UserListContract.View mUserListView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LockDataRepository.OnResultCallback> mOnResultCallback;

    private UserListPresenter mUserListPresenter;
    private static final int USER_ID = 1;
    private static final String LOCK_MAC = "00:00:00:00:00:00";

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this); // mockito rule can be used instead

        // Get a reference to the class under test
        mUserListPresenter = new UserListPresenter(mLockDataRepository, mUserListView);
    }

    @Test
    public void testOpenAddUser() {
        mUserListPresenter.openAddUser(LOCK_MAC);
        verify(mUserListView).showAddUser(eq(LOCK_MAC));
    }

    @Test
    public void testRemoveUser_success() {
        // Given an initialized AddLockPresenter
        mUserListPresenter.removeUser(USER_ID, LOCK_MAC);

        // Callback is captured and invoked with stubbed success
        verify(mLockDataRepository).removeUser(eq(USER_ID), eq(LOCK_MAC), mOnResultCallback
                .capture());
        mOnResultCallback.getValue().onResult(true);

        // Then lock list is shown in UI
        verify(mUserListView, never()).showRemoveUserError();
    }

    @Test
    public void testRemoveUser_fail() {
        // Given an initialized AddLockPresenter
        mUserListPresenter.removeUser(USER_ID, LOCK_MAC);

        // Callback is captured and invoked with stubbed success
        verify(mLockDataRepository).removeUser(eq(USER_ID), eq(LOCK_MAC), mOnResultCallback
                .capture());
        mOnResultCallback.getValue().onResult(false);

        // Then lock list is shown in UI
        verify(mUserListView).showRemoveUserError();
    }

}