ifneq ($(UROVO_PRODUCT), SQ29Z)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PACKAGE_NAME := UBXSettingsProvider
LOCAL_CERTIFICATE := platform
#LOCAL_STATIC_JAVA_LIBRARIES := qcrilhook qti-telephony-utils
ifeq ($(UROVO_PRODUCT), SQ52M)
LOCAL_STATIC_JAVA_LIBRARIES := qcnvitems
else
LOCAL_STATIC_JAVA_LIBRARIES := qcrilhook qti-telephony-utils qcnvitems
endif
LOCAL_JAVA_LIBRARIES := com.ubx.platform
LOCAL_DEX_PREOPT := false
include $(BUILD_PACKAGE)

endif
