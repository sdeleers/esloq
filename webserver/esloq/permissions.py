from rest_framework import permissions
from esloq.models import Esloq, EsloqUser, LockAccess, Log

class AuthenticatedUserIsURLUser(permissions.BasePermission):
    """
    The authenticated user is the same as the user in the URL.
    """
    def has_permission(self, request, view):
        user_id = request.user.id
        return view.kwargs.get('id', -1) == str(user_id)

# class IsAuthenticatedUserOrPost(permissions.BasePermission):
#     """
#     The authenticated user is the same as the user in the URL, or is a POST request.
#     """
#     def has_permission(self, request, view):
#         user_id = request.user.id
#         return ((request.method == 'POST') or (view.kwargs.get('id', -1) == str(user_id)))

class UserIdIsResourceIdOrNoPatch(permissions.BasePermission):
    """
    The authenticated user's id is the same as the resource user id on which the 
    action is taking place, otherwise PATCH is not allowed on the resource.
    """
    def has_permission(self, request, view):
        if request.method == 'PATCH':
            user_id = request.user.id
            resource_user_id = request.data.get('user_id', -1)
            return (user_id == post_user_id)
        return True

class PostOnlyForSelf(permissions.BasePermission):
    """
    The authenticated user's id is the same as the user id specified in the POST request. 
    """
    def has_permission(self, request, view):
        if request.method == 'POST':
            user_id = request.user.id
            post_user_id = request.data.get('user_id', -1)
            return (user_id == post_user_id)
        return True

class IsAdmin(permissions.BasePermission):
    """
    The authenticated user is admin on the lock.
    """
    def has_permission(self, request, view):
        user_id = request.user.id
        lock_id = view.kwargs.get('pk', -1)
        return isAdmin(user_id=user_id, lock_id=lock_id)

class IsAdminOrNoPatch(permissions.BasePermission):
    """
    If the user is no admin on the lock patch resource is not allowed. 
    """
    def has_permission(self, request, view):
        if request.method == 'PATCH':
            user_id = request.user.id
            lock_id = view.kwargs.get('pk', -1)
            return isAdmin(user_id=user_id, lock_id=lock_id)
        return True

class HasAccessOrNoPost(permissions.BasePermission):
    """
    If the user has no access on the lock create resource is not allowed. 
    """
    def has_permission(self, request, view):
        user_id = request.user.id
        if request.method not in permissions.SAFE_METHODS:
            if request.method == 'POST':
                lock_id = request.data.get('lock_id', -1)
            else:
                lock_access_id = view.kwargs.get('pk', -1)
                lock_id = LockAccess.objects.get(id=lock_access_id).lock_id.id
            return hasAccess(user_id=user_id, lock_id=lock_id)
        return True

class ReadOnlyIfNoAdminAndUsers(permissions.BasePermission):
    """
    If the user is no admin on the lock and there are users (lockaccesses) on the 
    lock, only HTTP read methods will be allowed. If there are no users, WRITE 
    methods are allowed.
    """
    def has_permission(self, request, view):
        user_id = request.user.id
        if request.method not in permissions.SAFE_METHODS:
            if request.method == 'POST':
                lock_id = request.data.get('lock_id', -1)
                if hasUsers(lock_id=lock_id):   
                    return isAdmin(user_id=user_id, lock_id=lock_id)
                return True
            else:
                lock_access_id = view.kwargs.get('pk', -1)
                lock_id = LockAccess.objects.get(id=lock_access_id).lock_id.id
                # Extra test of hasAccess is necessary, otherwise a 403 will be 
                # returned instead of a 404 when user has access but is no admin.
                if hasAccess(user_id=user_id, lock_id=lock_id): 
                    return isAdmin(user_id=user_id, lock_id=lock_id)
        return True

class IsAuthenticatedOrPost(permissions.BasePermission):
    """
    The request is authenticated as a user, or is a POST request.
    """
    def has_permission(self, request, view):
        return ((request.method == 'POST') or
            (request.user and request.user.is_authenticated()))

def hasAccess(user_id, lock_id):
    """
    Returns whether the user with id <user_id> has access on the lock with id <lock_id>
    """
    count = LockAccess.objects.filter(user_id=user_id, lock_id=lock_id).count()
    return count > 0

def isAdmin(user_id, lock_id):
    """
    Returns whether the user with id <user_id> is admin on the lock with id <lock_id>
    """
    count = LockAccess.objects.filter(user_id=user_id, lock_id=lock_id, is_admin=True).count()
    return count > 0

def hasUsers(lock_id):
    """
    Returns wether the lock has LockAccess objects associated with it.
    """
    count = LockAccess.objects.filter(lock_id=lock_id).count()
    return count > 0
