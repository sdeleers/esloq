from django.db import models
from rest_framework import serializers
from esloq.models import Esloq, EsloqUser, LockAccess, Log, FcmRegistration

class EsloqUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = EsloqUser
        fields = ('id', 'firebase_id', 'first_name', 'last_name', 'email',)

class EsloqUserIdAndEmailSerializer(serializers.ModelSerializer): 
    class Meta:
        model = EsloqUser
        fields = ('id', 'email',)

class LockSerializer(serializers.ModelSerializer):
    class Meta:
        model = Esloq
        fields = ('id', 'name', 'mac',)
        
    # Only allow lock's name to be updated
    def update(self, instance, validated_data):
        instance.name = validated_data.get('name', instance.name)
        instance.save()
        return instance

class UpdateLockSerializer(serializers.ModelSerializer):
    class Meta:
        model = Esloq
        fields = ('name',)

class LockAccessSerializer(serializers.ModelSerializer):
    class Meta:
        model = LockAccess

class LogSerializer(serializers.ModelSerializer):
    class Meta:
        model = Log

class FcmRegistrationSerializer(serializers.ModelSerializer):
    class Meta:
        model = FcmRegistration
