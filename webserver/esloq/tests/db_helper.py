from esloq.models import Esloq, EsloqUser, LockAccess, Log
from time import time
import jwt

def setup_testdb():
    user1 = EsloqUser(password="a", first_name="User", last_name="One", email="one@user.com", firebase_id="1111111111111111111111111111")
    user1.full_clean()
    user1.save()

    user2 = EsloqUser(password="a", first_name="User", last_name="Two", email="two@user.com", firebase_id="2222222222222222222222222222")
    user2.full_clean()
    user2.save()

    user3 = EsloqUser(password="a", first_name="User", last_name="Three", email="three@user.com", firebase_id="3333333333333333333333333333")
    user3.full_clean()
    user3.save()

    lock1 = Esloq(mac="00:00:00:00:00:01", key=b"\x00"*32, name="Lock One")
    lock2 = Esloq(mac="00:00:00:00:00:02", key=b"\x00"*32, name="Lock Two")
    lock3 = Esloq(mac="00:00:00:00:00:03", key=b"\x00"*32, name="Lock Three")

    lock1.full_clean()
    lock1.save()
    lock2.full_clean()
    lock2.save()
    lock3.full_clean()
    lock3.save()

    lock_access1 = LockAccess(lock_id=lock1, user_id=user1, is_admin=True)
    lock_access2 = LockAccess(lock_id=lock1, user_id=user2, is_admin=False)
    lock_access3 = LockAccess(lock_id=lock1, user_id=user3, is_admin=False)
    lock_access4 = LockAccess(lock_id=lock2, user_id=user2, is_admin=True)
    lock_access5 = LockAccess(lock_id=lock3, user_id=user3, is_admin=True)

    lock_access1.full_clean()
    lock_access1.save()
    lock_access2.full_clean()
    lock_access2.save()
    lock_access3.full_clean()
    lock_access3.save()
    lock_access4.full_clean()
    lock_access4.save()
    lock_access5.full_clean()
    lock_access5.save()

    log1 = Log(user_id=user1, lock_id=lock1, lock_state=True, access_time=1)
    log2 = Log(user_id=user2, lock_id=lock1, lock_state=True, access_time=1)
    log3 = Log(user_id=user3, lock_id=lock1, lock_state=True, access_time=1)
    log4 = Log(user_id=user2, lock_id=lock2, lock_state=True, access_time=1)
    log5 = Log(user_id=user3, lock_id=lock3, lock_state=True, access_time=1)

    log1.full_clean()
    log1.save()
    log2.full_clean()
    log2.save()
    log3.full_clean()
    log3.save()
    log4.full_clean()
    log4.save()
    log5.full_clean()
    log5.save()

def get_valid_token_user1():
    return get_token("1111111111111111111111111111", "User One", "one@user.com", True)

def get_valid_token_user2():
    return get_token("2222222222222222222222222222", "User Two", "two@user.com", True)

def get_valid_token_user3():
    return get_token("3333333333333333333333333333", "User Three", "three@user.com", True)

def get_token(firebase_id, name, email, is_valid):
    key_location = 'esloq/tests/key.pem' if is_valid else 'esloq/tests/key_fake.pem'
    with open(key_location, 'r') as f:
        key = f.read()
    data = {
        "iss": "https://securetoken.google.com/decoded-totem-95010",
        "name": name,
        "aud": "decoded-totem-95010",
        "auth_time": 1466172321,
        "user_id": firebase_id,
        "sub": firebase_id,
        "iat": time(),
        "exp": time()+600,
        "email": email 
    }
    return jwt.encode(data, key, algorithm='RS256', headers={'kid': '0'}).decode("utf-8")
