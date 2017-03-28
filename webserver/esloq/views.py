from esloq.models import Esloq, EsloqUser, LockAccess, Log, FcmRegistration
from esloq.serializers import *
from esloq.authentication import FirebaseAuthentication
from esloq.permissions import *
from rest_framework import generics
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework import permissions
from rest_framework import viewsets
from os import urandom
from esloq.nacl import auth_encrypt
from base64 import b64encode
from rest_framework import status
from django.shortcuts import get_object_or_404
from django.http import Http404

"""
ViewSet representing the esloqs of a given user.
"""
class EsloqViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - Only an admin can update the lock's name (IsAdminOrNoPatch)
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, IsAdminOrNoPatch, )
    serializer_class = LockSerializer

    def get_queryset(self):
        """
        Returns all locks the user has access on.
        """
        user_id = self.kwargs['id']
        lock_accesses = LockAccess.objects.filter(user_id=user_id)
        return Esloq.objects.filter(id__in=lock_accesses.values_list('lock_id', flat=True)) 

"""
ViewSet representing all esloqs.
"""
class AllEsloqViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
    """
    # Bit of a hack for allowing trailing slashes, how to do this properly?
    permission_classes = (permissions.IsAuthenticated, )
    serializer_class = LockSerializer

    def get_queryset(self):
        """
        If a mac address query parameter was specified, the lock with mac <mac> 
        is returned if he exists. Otherwise a 404 is thrown.
        """
        mac = self.request.query_params.get('mac', -1)
        # Bit of a hack for allowing trailing slashes, how to do this properly?
        if mac[-1] == "/":
            mac = mac.split("/")[0]
        return [get_object_or_404(Esloq, mac=mac)]

"""
ViewSet representing the users of a given user.
"""
class EsloqUserViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, )
    serializer_class = EsloqUserSerializer

    def get_queryset(self):
        """
        Returns users of user with id <id>. These are the users that have access 
        on the locks that the user with id <id> is admin on, plus the user's own user resource.
        """
        user_id = self.kwargs['id']
        admin_lock_ids = LockAccess.objects.filter(user_id=user_id, is_admin=True).values_list('lock_id', flat=True)
        user_ids_on_admin_locks = LockAccess.objects.filter(lock_id__in=admin_lock_ids).values_list('user_id', flat=True)
        user_ids = list(user_ids_on_admin_locks)+[user_id]
        return EsloqUser.objects.filter(id__in=user_ids)

"""
ViewSet representing all esloq users.
"""
class AllEsloqUserViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Everyone can post a new user, or get a user's id from his/her email address
    """
    permission_classes = ()

    def get_serializer_class(self):
        """
        GET request on user by email should only return the user's id and email.  
        """
        if self.action == 'list':
            return EsloqUserIdAndEmailSerializer
        return EsloqUserSerializer

    def get_queryset(self):
        """
        If an email query parameter was specified, the user with email <email> 
        is returned if he exists. Otherwise a 404 is thrown.
        """
        email = self.request.query_params.get('email', -1)
        # Bit of a hack for allowing trailing slashes, how to do this properly?
        if email[-1] == "/":
            email = email.split("/")[0]
        return [get_object_or_404(EsloqUser, email=email)]

"""
ViewSet representing the lockaccesses of a given user.
"""
class LockAccessViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - If lock has users we cannot write to its lockaccess resources if we aren't admin (ReadOnlyIfNoAdminAndUsers)
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, ReadOnlyIfNoAdminAndUsers, )
    serializer_class = LockAccessSerializer

    def get_queryset(self):
        """
        Returns all lockaccesses of the user with user id <id>. These are his own 
        lockaccesses and all lockaccesses of the locks he is admin on.
        """
        user_id = self.kwargs['id']
        non_admin_lock_accesses = LockAccess.objects.filter(user_id=user_id, is_admin=False)
        admin_lock_ids = LockAccess.objects.filter(user_id=user_id, is_admin=True).values_list('lock_id', flat=True)
        lock_accesses_on_admin_locks = LockAccess.objects.filter(lock_id__in=admin_lock_ids)
        return non_admin_lock_accesses | lock_accesses_on_admin_locks

"""
ViewSet representing the logs of a given user.
"""
class LogViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - Only users with access to the lock can post a log (HasAccessOrNoPost)
        - Not possible to post a log for another user (PostOnlyForSelf) 
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, HasAccessOrNoPost, PostOnlyForSelf, )
    serializer_class = LogSerializer

    def get_queryset(self):
        """
        Returns all logs of the user with user id <id>. These are the logs from 
        all the lock's he is admin on.
        """
        user_id = self.kwargs['id']
        admin_lock_ids = LockAccess.objects.filter(user_id=user_id, is_admin=True).values_list('lock_id', flat=True)
        logs = Log.objects.filter(lock_id__in=admin_lock_ids)
        return logs

"""
ViewSet representing the logs of a given lock for a given user.
"""
class LockLogsViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - User must be admin (IsAdmin)
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, IsAdmin )
    serializer_class = LogSerializer

    def get_queryset(self):
        """
        Returns all logs of the lock with id <pk>.
        """
        logs = Log.objects.filter(lock_id=self.kwargs['pk'])
        return logs

    """
    Override destroy method to support bulk delete.
    """
    def destroy(self, request, *args, **kwargs):
        logs = self.get_queryset()
        logs.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

"""
ViewSet representing the lockaccesses of a given lock for a given user.
"""
class LockLockAccessesViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - User must be admin (IsAdmin)
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, IsAdmin )
    serializer_class = LockAccessSerializer

    def get_queryset(self):
        """
        Returns all lock accesses of the lock with id <pk>.
        """
        accesses = LockAccess.objects.filter(lock_id=self.kwargs['pk'])
        return accesses 

    """
    Override destroy method to support bulk delete.
    """
    def destroy(self, request, *args, **kwargs):
        accesses = self.get_queryset()
        accesses.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

class FcmRegistrationViewSet(viewsets.ModelViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
        - Not possible to post an fcm registration for another user (PostOnlyForSelf) 
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser, PostOnlyForSelf, )
    serializer_class = FcmRegistrationSerializer

class LockTicketViewSet(viewsets.ViewSet):
    """
    Permissions:
        - Only authenticated users can use this viewset (IsAuthenticated)
        - Authenticated user id must match URL user id (AuthenticatedUserIsURLUser) 
    """
    permission_classes = (permissions.IsAuthenticated, AuthenticatedUserIsURLUser )

    def retrieve(self, request, *args, **kwargs):
        """
        Returns a ticket if user has access on the lock.
        """
        user_id = self.kwargs['id']
        lock_id = self.kwargs['pk']
        get_object_or_404(LockAccess, user_id=user_id, lock_id=lock_id)
        ticket = getlockticket(lock_id)
        return Response(ticket, status=status.HTTP_200_OK)

def getlockticket(lock_id):
    session_key = urandom(32)
    lock = Esloq.objects.get(id=lock_id)
    lock.nonce += 1
    lock.save()
    nonce = (lock.nonce).to_bytes(24, byteorder='big')
    # ticket = nonce + auth_encrypt(session_key, nonce, bytes(lock.key))
    # key = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f}
    # ticket = nonce + auth_encrypt(session_key, nonce, bytes(key))
    ticket = nonce + auth_encrypt(session_key, nonce, bytes(lock.key))
    return {"session_key": b64encode(session_key).decode("utf-8"), "ticket": b64encode(ticket).decode("utf-8")}
