define all-c-files-under
$(patsubst ./%,%, \
  $(shell cd $(LOCAL_PATH) ; \
          find $(1) -name "*.c" -and -not -name ".*") \
 )
endef

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := crypto_nacl_jni
LOCAL_CFLAGS    := -Wall -Werror #-pedantic
LOCAL_SRC_FILES := crypto_nacl_jni.c $(call all-c-files-under, avrnacl) $(call all-c-files-under, randombytes)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/avrnacl $(LOCAL_PATH)/avrnacl/include $(LOCAL_PATH)/avrnacl/include $(LOCAL_PATH)/randombytes

LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)