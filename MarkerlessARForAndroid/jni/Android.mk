# There is one Android.mk per module
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include /home/oli/NVPACK/OpenCV-2.4.5-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk

TBB_FULL_PATH := /home/oli/NVPACK/TBB
TBB_LIBRARY_FULL_PATH ?= $(TBB_FULL_PATH)/lib/android

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(TBB_FULL_PATH)/include
LOCAL_LDLIBS += -ltbb -L/home/oli/NVPACK/OpenCV-2.4.5-Tegra-sdk/sdk/native/3rdparty/libs/armeabi-v7a
LOCAL_MODULE := ar-jni
LOCAL_SRC_FILES := NativeFrameProcessor_jni.cpp CameraCalibration.cpp ARPipeline.cpp PatternDetector.cpp Pattern.cpp
include $(BUILD_SHARED_LIBRARY) 