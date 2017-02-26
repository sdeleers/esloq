package com.esloq.esloqapp.data;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link LockDataRepositoryImpl}.
 */
public class LockDataRepositoryImplTest {

    //TODO JSONobject/JSONArray is part of Android SDK, use POJO to enable local unit tests.

    @Mock
    private LocalDataServiceApi mLocalDataApi;

    @Mock
    private ServerDataServiceApi mServerDataApi;

    @Mock
    private PreferencesServiceApi mPreferencesApi;

    @Mock
    private LockDataRepository.OnResultCallback mOnResultCallback;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<ServerDataServiceApi.ServerDataServiceCallback>
            mServerDataCallbackCaptor;

    private LockDataRepository mLockDataRepository;
    private static final String LOCK_MAC = "00:00:00:00:00:00";
    private static final String LOCK_NAME = "My Lock";
    private static final boolean LOCK_CLOCKWISE = true;
    private static final int USER_ID = 1;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mLockDataRepository = new LockDataRepositoryImpl(mLocalDataApi,
                mServerDataApi, mPreferencesApi);
    }

    @Test
    public void testRegisterUserDevice_success() {
        mLockDataRepository.registerUserDevice(mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).registerUserDevice(mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(true, null);

        verify(mPreferencesApi).setRegistrationTokenSentToServer(true);
        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testRegisterUserDevice_fail() {
        mLockDataRepository.registerUserDevice(mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).registerUserDevice(mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(false, null);

        verify(mPreferencesApi).setRegistrationTokenSentToServer(false);
        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testFetchData_success() {
//        mLockDataRepository.fetchData(mOnResultCallback);
//
//        // Callback success is captured
//        verify(mServerDataApi).fetchUserData(mServerDataCallbackCaptor.capture());
//        mServerDataCallbackCaptor.getValue().onResult(true, jsonObject);
//
//        //TODO verify that paresAndStore is called.
//        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testFetchData_fail() {
//        mLockDataRepository.fetchData(mOnResultCallback);
//
//        // Callback success is captured
//        verify(mServerDataApi).fetchUserData(mServerDataCallbackCaptor.capture());
//        mServerDataCallbackCaptor.getValue().onResult(true, jsonObject);
//
//        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testAddLock_success() {
        mLockDataRepository.addLock(LOCK_MAC, LOCK_NAME, LOCK_CLOCKWISE, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).addLock(eq(LOCK_MAC), eq(LOCK_NAME), eq(LOCK_CLOCKWISE),
                mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(true, null);

        verify(mLocalDataApi).addLock(LOCK_MAC, LOCK_NAME, LOCK_CLOCKWISE);
        verify(mPreferencesApi).getUserId();
        verify(mLocalDataApi).addUserToLock(0, "me", true, LOCK_MAC, true);
        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testAddLock_fail() {
        mLockDataRepository.addLock(LOCK_MAC, LOCK_NAME, LOCK_CLOCKWISE, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).addLock(eq(LOCK_MAC), eq(LOCK_NAME), eq(LOCK_CLOCKWISE),
                mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(false, null);

        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testAddUser_success() {
        String email = "hello@world.com";
        mLockDataRepository.addUser(email, LOCK_MAC, true, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).addUser(eq(email), eq(LOCK_MAC), eq(true), mServerDataCallbackCaptor
                .capture());
//        mServerDataCallbackCaptor.getValue().onResult(true, null);

        //TODO JSONObject is dependent on andriod.
//        verify(mLocalDataApi).removeUserFromLock(eq(USER_ID), eq(LOCK_MAC));
//        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testAddUser_fail() {
        String email = "hello@world.com";
        mLockDataRepository.addUser(email, LOCK_MAC, true, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).addUser(eq(email), eq(LOCK_MAC), eq(true), mServerDataCallbackCaptor
                .capture());
        mServerDataCallbackCaptor.getValue().onResult(false, null);

        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testAddLog() {
        mLockDataRepository.addLog(LOCK_MAC, true);

        // Callback success is captured
        verify(mServerDataApi).addLog(eq(LOCK_MAC), eq(true), mServerDataCallbackCaptor.capture());
//        mServerDataCallbackCaptor.getValue().onResult(true, null);
    }

    @Test
    public void testRemoveLock_success() {
        mLockDataRepository.removeLock(LOCK_MAC, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).removeLock(eq(LOCK_MAC), mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(true, null);

        verify(mLocalDataApi).removeLock(eq(LOCK_MAC));
        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testRemoveLock_fail() {
        mLockDataRepository.removeLock(LOCK_MAC, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).removeLock(eq(LOCK_MAC), mServerDataCallbackCaptor.capture());
        mServerDataCallbackCaptor.getValue().onResult(false, null);

        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testRemoveUser_success() {
        mLockDataRepository.removeUser(USER_ID, LOCK_MAC, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).removeUser(eq(USER_ID), eq(LOCK_MAC), mServerDataCallbackCaptor
                .capture());
        mServerDataCallbackCaptor.getValue().onResult(true, null);

        verify(mLocalDataApi).removeUserFromLock(eq(USER_ID), eq(LOCK_MAC));
        verify(mOnResultCallback).onResult(true);
    }

    @Test
    public void testRemoveUser_fail() {
        mLockDataRepository.removeUser(USER_ID, LOCK_MAC, mOnResultCallback);

        // Callback success is captured
        verify(mServerDataApi).removeUser(eq(USER_ID), eq(LOCK_MAC), mServerDataCallbackCaptor
                .capture());
        mServerDataCallbackCaptor.getValue().onResult(false, null);

        verify(mOnResultCallback).onResult(false);
    }

    @Test
    public void testRequestSessionKey() {
        //TODO base64 is android class
    }

    @Test
    public void testLog() throws Exception {
        String message = "message";
        mLockDataRepository.log(message);

        // Callback success is captured
        verify(mServerDataApi).log(eq(message), mServerDataCallbackCaptor.capture());
//        mServerDataCallbackCaptor.getValue().onResult(true, null);
    }

    @Test
    public void testIsSignedIn() {
        mLockDataRepository.isSignedIn();
        verify(mPreferencesApi).isSignedIn();
    }

    @Test
    public void tesSetSignedIn() {
        mLockDataRepository.setSignedIn(true);
        verify(mPreferencesApi).setSignedIn(eq(true));
    }
}