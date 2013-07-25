# There is one Android.mk per module
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include /home/oli/NVPACK/OpenCV-2.4.5-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk

LOCAL_MODULE := ar-jni
LOCAL_SRC_FILES := NativeFrameProcessor_jni.cpp CameraCalibration.cpp ARPipeline.cpp PatternDetector.cpp Pattern.cpp GeometryTypes.cpp
include $(BUILD_SHARED_LIBRARY)