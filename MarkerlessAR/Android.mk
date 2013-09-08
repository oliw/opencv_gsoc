# 
# Android NDK Makefile for building MarkerlessAR library in an Android context
#  

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include /home/oli/NVPACK/OpenCV-2.4.5-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk
LOCAL_MODULE := ar
LOCAL_SRC_FILES := src/CameraCalibration.cpp src/ARPipeline.cpp src/PatternDetector.cpp src/Pattern.cpp src/GeometryTypes.cpp
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/src
include $(BUILD_SHARED_LIBRARY)