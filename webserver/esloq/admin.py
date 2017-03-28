from django.contrib import admin
from esloq.models import Esloq, EsloqUser, LockAccess, Log, FcmRegistration

# Register your models here.

admin.site.register(Esloq)
admin.site.register(EsloqUser)
admin.site.register(LockAccess)
admin.site.register(Log)
admin.site.register(FcmRegistration)
