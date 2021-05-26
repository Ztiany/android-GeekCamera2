LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_USE_AAPT2 := true

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.legacy_legacy-support-v13 \
    androidx.legacy_legacy-support-v4 \
    androidx.exifinterface_exifinterface \
    androidx.core_core

LOCAL_SRC_FILES := \
    $(call all-java-files-under, app/src/main/java) \
    $(call all-renderscript-files-under, app/src/main/rs) \

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/app/src/main/res

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml

#LOCAL_PROGUARD_FLAG_FILES := app/proguard-rules.pro

LOCAL_PACKAGE_NAME := GeekCamera2

LOCAL_SDK_VERSION := current

LOCAL_PRODUCT_MODULE := true

include $(BUILD_PACKAGE)