package com.urovo.smartpos.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.lang.reflect.Method;

public class SystemProperties {

    private static volatile Method set = null;

    public static boolean get(String name, boolean defValue) {
        try {
            final Class<?> systemPropertyClass = Class
                    .forName("android.os.SystemProperties");
            final Method getMethod = systemPropertyClass.getDeclaredMethod(
                    "getBoolean", String.class, Boolean.class);
            return (Boolean) getMethod.invoke(null, name, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static String get(String name, String defValue) {
        try {
            final Class<?> systemPropertyClass = Class
                    .forName("android.os.SystemProperties");
            final Method getMethod = systemPropertyClass.getDeclaredMethod(
                    "get", String.class, String.class);
            return (String) getMethod.invoke(null, name, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static void set(String prop, String value) {

        try {
            if (null == set) {
                synchronized (SystemProperties.class) {
                    if (null == set) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        set = cls.getDeclaredMethod("set", new Class<?>[]{String.class, String.class});
                    }
                }
            }
            set.invoke(null, new Object[]{prop, value});
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        