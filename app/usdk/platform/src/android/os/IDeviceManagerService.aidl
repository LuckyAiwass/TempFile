package android.os;


interface IDeviceManagerService {
    void setLockTaskMode(String packageName, boolean enable);

    boolean setSettingProperty(String name, String value);
    String getSettingProperty(String name);

    int getRestrictionPolicy(int action);
    int setRestrictionPolicy(int faction, int status);

    void setUserRestriction(String key, boolean value);
    boolean hasUserRestriction(String restrictionKey);


    void setKeyEventAllowed(int keycode, boolean allowed);
    boolean isKeyEventAllowed(int keycode);

    void disableStatusBar(int what);
    int getStatusBarFlags();
}
