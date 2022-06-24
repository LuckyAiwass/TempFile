// IRestrictionPolicy.aidl
package com.ubx.usdk.profile.aidl;

// Declare any non-default types here with import statements

interface IRestrictionPolicy {
        boolean setSettingProperty(String name, String value);
        String getSettingProperty(String name);

        int getRestrictionPolicy(int action);
        int setRestrictionPolicy(int faction, int status);

        void setUserRestriction(String key, boolean value);
        boolean hasUserRestriction(String restrictionKey);
}
