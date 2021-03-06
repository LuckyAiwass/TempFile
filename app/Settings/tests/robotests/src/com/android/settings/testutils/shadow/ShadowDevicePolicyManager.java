package com.android.settings.testutils.shadow;

import static android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Implements(DevicePolicyManager.class)
public class ShadowDevicePolicyManager extends org.robolectric.shadows.ShadowDevicePolicyManager {

    private final Map<Integer, Long> mProfileTimeouts = new HashMap<>();
    private Map<Integer, CharSequence> mSupportMessagesMap = new HashMap<>();
    private boolean mIsAdminActiveAsUser = false;
    private ComponentName mDeviceOwnerComponentName;
    private int mDeviceOwnerUserId = -1;
    private int mPasswordMinQuality = PASSWORD_QUALITY_UNSPECIFIED;
    private int mPasswordMaxLength = 16;
    private int mPasswordMinLength = 0;
    private int mPasswordMinSymbols = 0;

    public void setShortSupportMessageForUser(ComponentName admin, int userHandle, String message) {
        mSupportMessagesMap.put(Objects.hash(admin, userHandle), message);
    }

    @Implementation
    protected @Nullable CharSequence getShortSupportMessageForUser(@NonNull ComponentName admin,
            int userHandle) {
        return mSupportMessagesMap.get(Objects.hash(admin, userHandle));
    }

    @Implementation
    protected boolean isAdminActiveAsUser(@NonNull ComponentName admin, int userId) {
        return mIsAdminActiveAsUser;
    }

    @Implementation
    protected int getDeviceOwnerUserId() {
        return mDeviceOwnerUserId;
    }

    @Implementation
    protected long getMaximumTimeToLock(ComponentName admin, @UserIdInt int userHandle) {
        return mProfileTimeouts.getOrDefault(userHandle, 0L);
    }

    @Implementation
    protected ComponentName getDeviceOwnerComponentOnAnyUser() {
        return mDeviceOwnerComponentName;
    }

    public void setIsAdminActiveAsUser(boolean active) {
        mIsAdminActiveAsUser = active;
    }

    public void setDeviceOwnerUserId(int id) {
        mDeviceOwnerUserId = id;
    }

    public void setMaximumTimeToLock(@UserIdInt int userHandle, Long timeout) {
        mProfileTimeouts.put(userHandle, timeout);
    }

    public void setDeviceOwnerComponentOnAnyUser(ComponentName admin) {
        mDeviceOwnerComponentName = admin;
    }

    @Implementation
    public int getPasswordQuality(ComponentName admin, int userHandle) {
        return mPasswordMinQuality;
    }

    public void setPasswordQuality(int quality) {
        mPasswordMinQuality = quality;
    }

    @Implementation
    public int getPasswordMinimumLength(ComponentName admin, int userHandle) {
        return mPasswordMinLength;
    }

    public void setPasswordMinimumLength(int length) {
        mPasswordMinLength = length;
    }

    @Implementation
    public int getPasswordMinimumSymbols(ComponentName admin, int userHandle) {
        return mPasswordMinSymbols;
    }

    public void setPasswordMinimumSymbols(int numOfSymbols) {
        mPasswordMinSymbols = numOfSymbols;
    }

    @Implementation
    public int getPasswordMaximumLength(int quality) {
        return mPasswordMaxLength;
    }

    public void setPasswordMaximumLength(int length) {
        mPasswordMaxLength = length;
    }

    public static ShadowDevicePolicyManager getShadow() {
        return (ShadowDevicePolicyManager) Shadow.extract(
                RuntimeEnvironment.application.getSystemService(DevicePolicyManager.class));
    }
}
                                                                                                                                                                                                                                                                                                            