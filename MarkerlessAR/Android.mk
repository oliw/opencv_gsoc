# 
# Android NDK Makefile for building MarkerlessAR library in an Android context
#  
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
# Static Version of Desktop Lib
include $(OPENCV4AROOT)/sdk/native/jni/OpenCV-tegra3.mk
LOCAL_MODULE := markerless_ar_desktop
LOCAL_SRC_FILES := src/CameraCalibration.cpp src/ARPipeline.cpp src/PatternDetector.cpp src/Pattern.cpp src/GeometryTypes.cpp
# Ensure our dependees can include our header files too
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/src
ifeq ($(ANDROID_NDK_PROFILER_ENABLED),true)
LOCAL_CFLAGS := -pg 
LOCAL_CFLAGS += -DANDROID_NDK_PROFILER_ENABLED
LOCAL_STATIC_LIBRARIES := android-ndk-profiler
endif
include $(BUILD_STATIC_LIBRARY)

ifeq ($(ANDROID_NDK_PROFILER_ENABLED),true)
$(call import-module, android-ndk-profiler)
endif
