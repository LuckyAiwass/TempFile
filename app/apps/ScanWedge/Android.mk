
LOCAL_PATH := $(call my-dir)

my_archs := arm
my_src_arch := $(call get-prebuilt-src-arch, $(my_archs))
ifeq ($(my_src_arch),arm)
my_src_abi := armeabi
else ifeq ($(my_src_arch),arm64)
my_src_abi := armeabi
endif

include $(CLEAR_VARS)
LOCAL_MODULE := ScanWedge
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false
#LOCAL_PRIVILEGED_MODULE :=
#LOCAL_CERTIFICATE := $(DEFAULT_SYSTEM_DEV_CERTIFICATE)
#LOCAL_OVERRIDES_PACKAGES :=
LOCAL_SRC_FILES := ScanWedge.apk
LOCAL_MODULE_TARGET_ARCH := $(my_src_arch)
LOCAL_MULTILIB := both

LOCAL_PRIVATE_PLATFORM_APIS := true

include $(BUILD_PREBUILT)

