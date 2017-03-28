from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from esloq import views
from esloq.views import EsloqViewSet, EsloqUserViewSet, AllEsloqUserViewSet,  LockAccessViewSet, LogViewSet, FcmRegistrationViewSet, LockTicketViewSet, LockLogsViewSet, LockLockAccessesViewSet, AllEsloqViewSet

lock_list = EsloqViewSet.as_view({
    'get': 'list',
})
lock_detail = EsloqViewSet.as_view({
    'get': 'retrieve',
    'patch': 'partial_update',
})
lock_all = AllEsloqViewSet.as_view({
    'get': 'list',
})
lockaccess_list = LockAccessViewSet.as_view({
    'get': 'list',
    'post': 'create'
})
lockaccess_detail = LockAccessViewSet.as_view({
    'get': 'retrieve',
    'delete': 'destroy'
})
log_list = LogViewSet.as_view({
    'get': 'list',
    'post': 'create'
})
log_detail = LogViewSet.as_view({
    'get': 'retrieve',
})
user_list = EsloqUserViewSet.as_view({
    'get': 'list',
})
user_detail = EsloqUserViewSet.as_view({
    'get': 'retrieve',
})
fcmregistration_list = FcmRegistrationViewSet.as_view({
    'post': 'create',
})
lockticket_detail = LockTicketViewSet.as_view({
    'get': 'retrieve',
})
user_all = AllEsloqUserViewSet.as_view({
    'post': 'create',
    'get': 'list',
})
locklogs = LockLogsViewSet.as_view({
    'delete': 'destroy',
})
locklockaccesses = LockLockAccessesViewSet.as_view({
    'delete': 'destroy',
})

base_url = 'users/(?P<id>[0-9]+)/'

urlpatterns = [
    url(r'^' + base_url + 'locks/$', lock_list, name='lock-list'),
    url(r'^' + base_url + 'locks/(?P<pk>[0-9]+)/$', lock_detail, name='lock-detail'),
    url(r'^' + base_url + 'locks/(?P<pk>[0-9]+)/logs/$', locklogs, name='locklogs'),
    url(r'^' + base_url + 'locks/(?P<pk>[0-9]+)/lockaccesses/$', locklockaccesses, name='locklockaccesses'),
    url(r'^' + base_url + 'locks/(?P<pk>[0-9]+)/ticket/$', lockticket_detail, name='lockticket-detail'),
    url(r'^locks/$', lock_all, name='lock-all'),
    url(r'^users/$', user_all, name='user-all'),
    url(r'^' + base_url + 'users/$', user_list, name='user-list'),
    url(r'^' + base_url + 'users/(?P<pk>[0-9]+)/$', user_detail, name='user-detail'),
    url(r'^' + base_url + 'lockaccesses/$', lockaccess_list, name='lockaccess-list'),
    url(r'^' + base_url + 'lockaccesses/(?P<pk>[0-9]+)/$', lockaccess_detail, name='lockaccess-detail'),
    url(r'^' + base_url + 'logs/$', log_list, name='log-list'),
    url(r'^' + base_url + 'logs/(?P<pk>[0-9]+)/$', log_detail, name='log-detail'),
    url(r'^' + base_url + 'fcmregistrations/$', fcmregistration_list, name='fcmregistration-list'),
]

urlpatterns = format_suffix_patterns(urlpatterns)
