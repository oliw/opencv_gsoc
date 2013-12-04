# There is one Android.mk per module
ANDROID_LOCAL_PATH := $(call my-dir)
ANDROID_NDK_PROFILER_ENABLED := false

# OpenCV For Android
include $(OPENCV4AROOT)/sdk/native/jni/OpenCV-tegra3.mk

LOCAL_PATH := $(ANDROID_LOCAL_PATH)

LOCAL_MODULE := ar-jni 
LOCAL_STATIC_LIBRARIES := markerless_ar_desktop
LOCAL_SRC_FILES := NativeFrameProcessor_jni.cpp
LOCAL_LDLIBS +=  -llog -ldl -landroid
ifeq ($(ANDROID_NDK_PROFILER_ENABLED),true)
LOCAL_CFLAGS += -pg 
LOCAL_CFLAGS += -DANDROID_NDK_PROFILER_ENABLED
LOCAL_STATIC_LIBRARIES += android-ndk-profiler
endif
include $(BUILD_SHARED_LIBRARY)

$(call import-add-path, $(ANDROID_LOCAL_PATH)/../..)
$(call import-module, MarkerlessAR)

ifeq ($(ANDROID_NDK_PROFILER_ENABLED),true)
$(call import-module, android-ndk-profiler) 
endif
