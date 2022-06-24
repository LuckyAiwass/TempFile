LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := com.ubx.platform
LOCAL_STATIC_JAVA_LIBRARIES := UDIDataParser Zebrajar
# QCOM Project , MTK Project ignore start
ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
LOCAL_JNI_SHARED_LIBRARIES := \
    libbinder
#LOCAL_JNI_SHARED_LIBRARIES := \
#    libHsmKil
LOCAL_MULTILIB := 32
endif
# QCOM Project , MTK Project ignore end
LOCAL_PACKAGE_NAME := ScannerService
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

#force 64-bit
ifeq ($(UROVO_PRODUCT), SQ53H)
$(shell mkdir -p $(PRODUCT_OUT)/system/app/$(LOCAL_PACKAGE_NAME)/lib/arm64)
endif

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    UDIDataParser:libs/UDIDataParser.jar \
    Zebrajar:libs/vendor.mediatek.hardware.zebra-V1.0-java.jar
include $(BUILD_MULTI_PREBUILT)
