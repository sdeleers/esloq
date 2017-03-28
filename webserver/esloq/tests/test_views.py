from esloq.tests.db_helper import setup_testdb, get_valid_token_user1, get_valid_token_user2
from django.test import TestCase, Client
from json import loads, dumps
from rest_framework import status
from esloq.models import *

class TestLockAccess(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated.
    def test_get_lockaccesses_authenticated(self):
        url = "/users/1/lockaccesses/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is not authenticated.
    def test_get_lockaccesses_not_authenticated(self):
        url = "/users/1/lockaccesses/"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated as a different user than specified in the url.
    def test_get_lockaccesses_authenticated_as_different_user(self):
        url = "/users/2/lockaccesses/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated, and is admin on lock.
    def test_get_lockaccess_admin(self):
        url = "/users/1/lockaccesses/3/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated, has access on lock but is no admin. Get other user's lock access.
    def test_get_lockaccess_not_admin(self):
        url = "/users/2/lockaccesses/3/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # GET. User is authenticated, has access on lock. Get user's lock access.
    def test_get_own_lockaccess_not_admin(self):
        url = "/users/2/lockaccesses/2/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated. Requested lockaccess does not exist.
    def test_get_lockaccess_does_not_exist(self):
        url = "/users/1/lockaccesses/10/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # POST. User is authenticated and admin.
    def test_post_lockaccesses_admin(self):
        url = "/users/2/lockaccesses/"
        payload = dumps({'is_admin': True, 'lock_id': 2, 'user_id': 1})
        response = self.client.post(url, data=payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # POST. User is authenticated and no admin.
    def test_post_lockaccesses_not_admin(self):
        url = "/users/2/lockaccesses/"
        payload = dumps({'is_admin': True, 'lock_id': 3, 'user_id': 1})
        response = self.client.post(url, data=payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # # POST. User is authenticated and admin. The user we're adding to the lock does not exist
    # def test_post_lockaccesses_not_admin(self):
    #     url = "/users/2/lockaccesses/"
    #     payload = dumps({'is_admin': True, 'lock_id': 2, 'user_id': 1000})
    #     response = self.client.post(url, data=payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
    #     self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # PUT. User authenticated and admin.
    def test_put_lockaccess_admin(self):
        url = "/users/1/lockaccesses/2/"
        payload = dumps({'id': 2, 'is_admin': True, 'lock_id': 1, 'user_id': 2})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # PUT. User authenticated, has access but no admin.
    def test_put_lockaccess_not_admin(self):
        url = "/users/2/lockaccesses/2/"
        payload = dumps({'id': 2, 'is_admin': True, 'lock_id': 1, 'user_id': 2})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # DELETE. User is authenticated, and is admin on lock.
    def test_delete_lockaccess_admin(self):
        url = "/users/1/lockaccesses/2/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

    # DELETE. User is authenticated, has access but is no admin.
    def test_delete_lockaccess_not_admin(self):
        url = "/users/2/lockaccesses/2/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # DELETE. User is authenticated, no access.
    def test_delete_lockaccess_no_access(self):
        url = "/users/1/lockaccesses/4/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

class TestLock(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated.
    def test_get_locks_authenticated(self):
        url = "/users/1/locks/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is not authenticated.
    def test_get_locks_not_authenticated(self):
        url = "/users/1/locks/"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated as a different user than specified in the url.
    def test_get_locks_authenticated_as_different_user(self):
        url = "/users/2/locks/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated, and is admin on lock.
    def test_get_lock_admin(self):
        url = "/users/1/locks/1/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated, has access on lock but is no admin.
    def test_get_lock_not_admin(self):
        url = "/users/2/locks/1/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated but has no access on the lock.
    def test_get_lock_no_access(self):
        url = "/users/1/locks/2/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # GET. User is authenticated. Requested lock does not exist.
    def test_get_lock_does_not_exist(self):
        url = "/users/1/locks/10/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # PUT. User is authenticated.
    def test_put_lock_authenticated(self):
        url = "/users/1/locks/1/"
        payload = dumps({"id": 1, "mac": "00:11:22:33:44:59", "name": "New lock name"})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # PATCH. User is authenticated and updates lock on which he is admin.
    def test_patch_lock_name_authenticated_admin(self):
        url = "/users/1/locks/1/"
        payload = dumps({"name": "new name"})
        response = self.client.patch(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        lock = Esloq.objects.get(id=1)
        self.assertEqual(lock.name, "new name")
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # PATCH. User is authenticated and updates lock mac on which he is admin.
    def test_patch_lock_mac_authenticated_admin(self):
        url = "/users/1/locks/1/"
        payload = dumps({"mac": "66:55:44:33:22:11"})
        response = self.client.patch(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        lock = Esloq.objects.get(id=1)
        self.assertEqual(lock.mac, "00:00:00:00:00:01")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
    # PATCH. User is authenticated and updates lock name on which he is no admin.
    def test_patch_lock_name_authenticated_no_admin(self):
        url = "/users/2/locks/1/"
        payload = dumps({"name": "new name"})
        response = self.client.patch(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        lock = Esloq.objects.get(id=1)
        self.assertEqual(lock.name, "Lock One")
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # POST. User is authenticated. 
    def test_post_lock_authenticated(self):
        url = "/users/1/locks/"
        payload = dumps({"mac": "00:11:22:33:44:59", "name": "New lock name"})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # DELETE. User is authenticated, and is admin on lock.
    def test_delete_lock_authenticated(self):
        url = "/users/1/locks/1/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

class TestAllLock(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated. Get lock by mac address.
    def test_get_lock_by_mac(self):
        url = "/locks/?mac=00:00:00:00:00:01/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated. Get lock by mac address, lock does not exist
    def test_get_lock_by_mac_does_not_exist(self):
        url = "/locks/?mac=99:99:99:99:99:99/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

class TestUser(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated.
    def test_get_users_authenticated(self):
        url = "/users/1/users/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is not authenticated.
    def test_get_locks_not_authenticated(self):
        url = "/users/1/users/"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated as a different user than specified in the url.
    def test_get_locks_authenticated_as_different_user(self):
        url = "/users/2/users/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # POST. User is authenticated. Wrong url for posting user
    def test_post_user_authenticated_wrong_url(self):
        url = "/users/1/users/"
        payload = dumps({"email": "new@email.com", "first_name": "new", "last_name": "name"})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # POST. User is not authenticated. Correct url for posting user.
    def test_post_user_authenticated_correct_url(self):
        url = "/users/"
        payload = dumps({"email": "new@email.com", "first_name": "new", "last_name": "name"})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # POST. User is not authenticated. Correct url for posting user.
    def test_post_user_not_authenticated_correct_url(self):
        url = "/users/"
        payload = dumps({"email": "new@email.com", "first_name": "new", "last_name": "name"})
        response = self.client.post(url, payload, content_type="application/json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # POST. User is authenticated. Posted user with that email already exists.
    def test_post_user_authenticated_user_exists(self):
        url = "/users/"
        payload = dumps({"email": "one@user.com", "first_name": "new", "last_name": "name"})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    # GET. User is authenticated. Get user by email address.
    def test_get_user_by_email(self):
        url = "/users/?email=one@user.com/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(set(['id', 'email']), response.data[0].keys())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is not authenticated (should work). Get user by email address.
    def test_get_user_by_email(self):
        url = "/users/?email=one@user.com/"
        response = self.client.get(url)
        self.assertEqual(set(['id', 'email']), response.data[0].keys())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated. Get user by email address, user does not exist.
    def test_get_user_by_email_does_not_exist(self):
        url = "/users/?email=doesnot@exist.com/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # GET. User is authenticated, and is admin on lock.
    def test_get_user_admin(self):
        url = "/users/1/users/2/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated, has access on lock this user has access to but is no admin.
    def test_get_user_not_admin(self):
        url = "/users/2/users/1/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # GET. User is authenticated but has no access on a lock this user has access to.
    def test_get_user_no_access(self):
        url = "/users/2/users/3/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # GET. User is authenticated. Requested user does not exist.
    def test_get_user_does_not_exist(self):
        url = "/users/1/users/10/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # PUT. User is authenticated. Wrong URL.
    def test_put_user_authenticated(self):
        url = "/users/1/users/1/"
        payload = dumps({"id": 1, "email": "new@email.com", "first_name": "new", "last_name": "name"})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # DELETE. User is authenticated
    def test_delete_user_authenticated(self):
        url = "/users/1/users/2/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

class TestLog(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated.
    def test_get_logs_authenticated(self):
        url = "/users/1/logs/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is not authenticated.
    def test_get_logs_not_authenticated(self):
        url = "/users/1/logs/"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated as a different user than specified in the url.
    def test_get_logs_authenticated_as_different_user(self):
        url = "/users/2/logs/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # GET. User is authenticated, and is admin on lock.
    def test_get_log_admin(self):
        url = "/users/1/logs/1/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated, has access on lock but is no admin. 
    def test_get_log_not_admin(self):
        url = "/users/2/logs/2/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    def test_get_log_does_not_exist(self):
        url = "/users/1/logs/10/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # POST. User is authenticated. 
    def test_post_log_authenticated(self):
        url = "/users/1/logs/"
        payload = dumps({"lock_state": False, "lock_id": 1, "user_id": 1, "access_time": 0})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # POST. User is authenticated as a different user than specified in the url.
    def test_post_log_authenticated_as_different_user(self):
        url = "/users/2/logs/"
        payload = dumps({"lock_state": False, "lock_id": 1, "user_id": 1, "access_time": 0})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # POST. User is not authenticated. 
    def test_post_log_not_authenticated(self):
        url = "/users/1/logs/"
        payload = dumps({"lock_state": False, "lock_id": 1, "user_id": 1, "access_time": 0})
        response = self.client.post(url, payload, content_type="application/json")
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # POST. User is authenticated. Other user_id in POST request than authenticated user's id
    def test_post_log_for_other_user(self):
        url = "/users/1/logs/"
        payload = dumps({"lock_state": False, "lock_id": 1, "user_id": 2, "access_time": 0})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # PUT. User is authenticated.
    def test_put_log_authenticated(self):
        url = "/users/1/logs/1/"
        payload = dumps({"id": 1, "lock_state": False, "lock_id": 1, "user_id": 1, "access_time": 0})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # DELETE. User is authenticated
    def test_delete_log_authenticated(self):
        url = "/users/1/logs/1/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # GET. User is authenticated, log does not exist. 
    def test_get_log_does_not_exist_authenticated(self):
        url = "/users/1/logs/10/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

class TestGcmRegistration(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated.
    def test_get_fcmregistration_authenticated(self):
        url = "/users/1/fcmregistrations/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # POST. User is authenticated. 
    def test_post_fcmregistration_authenticated(self):
        url = "/users/1/fcmregistrations/"
        payload = dumps({"registration_token": "2", "user_id": 1})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    # POST. User is authenticated. Other user_id in POST request than authenticated user's id
    def test_post_fcmregistration_for_other_user(self):
        url = "/users/1/fcmregistrations/"
        payload = dumps({"registration_token": "2", "user_id": 2})
        response = self.client.post(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    # PUT. User is authenticated.
    def test_put_fcmregistration_authenticated(self):
        url = "/users/1/fcmregistrations/1/"
        payload = dumps({"id": 1, "lock_state": False, "lock_id": 1, "user_id": 1, "access_time": 0})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # DELETE. User is authenticated
    def test_delete_fcmregistration_authenticated(self):
        url = "/users/1/fcmregistration/1/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

class TestLockTicket(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    # GET. User is authenticated. User has access to lock.
    def test_get_ticket_authenticated_hasaccess(self):
        url = "/users/1/locks/1/ticket/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    # GET. User is authenticated. User has no access to lock.
    def test_get_ticket_authenticated_noaccess(self):
        url = "/users/1/locks/2/ticket/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    # POST. User is authenticated. 
    def test_post_ticket_authenticated(self):
        url = "/users/1/locks/1/ticket/"
        payload = dumps({})
        response = self.client.post(url, data=payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # PUT. User is authenticated.
    def test_put_ticket_authenticated(self):
        url = "/users/1/locks/1/ticket/"
        payload = dumps({})
        response = self.client.put(url, payload, content_type="application/json", HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

    # DELETE. User is authenticated. 
    def test_delete_ticket_authenticated(self):
        url = "/users/1/locks/1/ticket/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

class TestLockLogs(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    def test_delete_locklogs_authenticated_isadmin(self):
        url = "/users/1/locks/1/logs/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertEqual(Log.objects.filter(lock_id=1).count(), 0)

    def test_delete_locklogs_authenticated_isnoadmin(self):
        url = "/users/2/locks/1/logs/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_get_locklogs_authenticated_isadmin(self):
        url = "/users/1/locks/1/logs/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)

class TestLockLockAccesses(TestCase):
    def setUp(self):
        self.client = Client()
        setup_testdb()

    def test_delete_locklockaccesses_authenticated_isadmin(self):
        url = "/users/1/locks/1/lockaccesses/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertEqual(LockAccess.objects.filter(lock_id=1).count(), 0)

    def test_delete_locklockaccesses_authenticated_isnoadmin(self):
        url = "/users/2/locks/1/lockaccesses/"
        response = self.client.delete(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user2())
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_get_locklockaccesses_authenticated_isadmin(self):
        url = "/users/1/locks/1/lockaccesses/"
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer ' + get_valid_token_user1())
        self.assertEqual(response.status_code, status.HTTP_405_METHOD_NOT_ALLOWED)
