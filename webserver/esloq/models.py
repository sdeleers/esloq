from django.db import models
from django.core.validators import RegexValidator
from django.core.exceptions import ValidationError
from django.utils.translation import ugettext as _
from time import time
from django.contrib.auth.models import User
from django.contrib.auth.base_user import AbstractBaseUser

# Create your models here.

class Esloq(models.Model):
    """
    Database table representing an Slock.
    
    Every tuple has a unique MAC address, a name, and a symmetric key that 
    is the same as the one stored in memory on the Slock.  

    """
    id = models.AutoField(primary_key=True)
    mac_fmt = RegexValidator("^([0-9a-f]{2}:){5}([0-9a-f]{2})$", "Invalid MAC address")
    mac = models.CharField(max_length=20, unique = True, validators=[mac_fmt])
    key = models.BinaryField(max_length=32)
    name = models.CharField(max_length=20)
    nonce = models.PositiveIntegerField(default=0)

    def __str__(self):
        """
        String representation of this class.
        """
        return self.name

class EsloqUser(AbstractBaseUser):
    """
    Database table representing a user that has access to an esloq.
    
    Every tuple contains the user's name, email and hashed password.

    """
    id = models.AutoField(primary_key=True)
    firebase_id = models.CharField(max_length=28, unique=True, blank=True, null=True)
    first_name = models.CharField(max_length=20)
    last_name = models.CharField(max_length=20)
    email = models.EmailField(max_length=50, unique=True)

    def __str__(self):
        """String representation of this class."""
        return self.first_name + ' ' + self.last_name

class LockAccess(models.Model):
    """
    Database table representing the access of a user on a lock.

    The table contains a tuple for each access of a user on a lock. The tuple 
    contains the user's ID, the lock's ID and whether the user has admin access
    to the lock.

    """
    id = models.AutoField(primary_key=True)
    lock_id = models.ForeignKey(Esloq) 
    user_id = models.ForeignKey(EsloqUser)
    is_admin = models.BooleanField()

    def __str__(self):
        """String representation of this class."""
        return self.user_id.first_name + " on " + self.lock_id.name

    class Meta:
        """Only one tuple can exist with a given lock ID and user ID."""
        unique_together = ('lock_id','user_id')

class Log(models.Model):
    """
    Database table representing a log.

    Every time a user locks or unlocks an Slock a tuple is added to the Log 
    table containing the user's ID, the lock's ID, the access date and time and 
    the lock state (locked/unlocked).

    """
    id = models.AutoField(primary_key=True)
    lock_id = models.ForeignKey(Esloq) 
    user_id = models.ForeignKey(EsloqUser)
    access_time = models.PositiveIntegerField(default=time)
    lock_state = models.BooleanField()

    def __str__(self):
        """String representation of this class."""
        return self.user_id.first_name + " on " + self.lock_id.name
    
    class Meta:
        """Only one tuple can exist with a given lock ID, access date and access time."""
        #unique_together = ('lock_id', 'access_time') # resolution is 1s, problem if quick lock/unlock

class FcmRegistration(models.Model):
    """
    Database table mapping users to a GCM registration ID.

    The table maps user ID's with GCM registration ID's. Each user can have
    multiple registration ID's since since a registration ID identifies a
    device. On the other had, multiple users can also have the same
    registration ID if they use the same device.

    """
    id = models.AutoField(primary_key=True)
    user_id = models.ForeignKey(EsloqUser)
    registration_token = models.CharField(max_length=512);

    def __str__(self):
        """String representation of this class."""
        return self.user_id.first_name + ", " + self.registration_token

    class Meta:
        """Only one tuple can exist with a given user ID and registration ID."""
        unique_together = ('user_id','registration_token')
