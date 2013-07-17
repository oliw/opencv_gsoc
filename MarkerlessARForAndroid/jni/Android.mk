# There is one Android.mk per module
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := hello-jni
include $(BUILD_SHARED_LIBRARY)