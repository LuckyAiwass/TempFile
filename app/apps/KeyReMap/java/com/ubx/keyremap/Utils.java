package com.ubx.keyremap;

import android.view.KeyEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

public abstract class Utils {

    /**
     * debug key
     */
    public static final boolean DEBUG = true;

    /**
     * logging tag
     */
    public static final String TAG = "KeyReMap";

    /**
     * database name
     */
    public static final String DB_NAME = "keymap.db";

    /**
     * default keymap path
     */
    public static final String DEFAULT_KEYMAP_PATH = "/system/etc/default_keymap.xml";

    /**
     * remap activity action
     */
    public static final String ACTION_REMAP_ACTIVITY = "android.intent.action.KEY_REMAP_TYPE";

    /**
     * MappedKeys list activity action
     */
    public static final String ACTION_RESULT_ACTIVITY = "android.intent.action.KEY_REMAP_VIEWER";

    /**
     * intent edit activity action
     */
    public static final String ACTION_EDIT_INTENT_ACTIVITY = "android.intent.action.EDIT_INTENT_EXTRAS";

    /**
     * KeyMaps
     */
    public static Map NumberKeyMap = null;
    public static Map AlphabetKeyMap = null;
    public static Map FunctionKeyMap = null;
    public static Map OtherKeyMap = null;

    /**
     * construct
     */
    private Utils() {

    }

    static {
        initKeyMaps();
    }

    public static void toWriteCap_map(String value) throws IOException {
        final String path = "/sys/class/tca8418_ctrl/tca8418/cap_map";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));
            String CapValue = value;
            fos.write(CapValue.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null)
            fos.close();
        }
    }

    public static String toReadAa1() {
        String value = null;
        final String path = "/sys/class/tca8418_ctrl/tca8418/isaA";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path));
            byte[] capValue = new byte[1];
            fis.read(capValue);
            value = new String(capValue);
            return value;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Initializes keyNames, metaNames, keyAdapter, and
     * metaAdapter. Keys are taken from KeyEvent fields starting with KEYCODE,
     * and metas are taken from KeyEvent fields starting with META.
     * --Note--keyPopulate is assumed to be called only once, so this method is
     * not optimized for multiple calls.--
     */
    public static void initKeyMaps() {

        Field[] keyFields = KeyEvent.class.getDeclaredFields();
        NumberKeyMap = new TreeMap<Integer, String>();
        AlphabetKeyMap = new TreeMap<Integer, String>();
        FunctionKeyMap = new TreeMap<Integer, String>();
        OtherKeyMap = new TreeMap<Integer, String>();

        String tmpName = null;
        try {
            for (int i = 0; i < keyFields.length; i++) {
                tmpName = keyFields[i].getName();
                int modi = keyFields[i].getModifiers();
                if (Modifier.isStatic(modi) && Modifier.isPublic(modi)) {
                    if (tmpName.startsWith("KEYCODE_")) {
                        int keycode = (Integer) keyFields[i].get(null);
                        if (keycode > KeyEvent.KEYCODE_UNKNOWN && keycode <= KeyEvent.getMaxKeyCode()
                                && keycode != KeyEvent.KEYCODE_POWER) {
                            tmpName = tmpName.replace("KEYCODE_", "");
                            if (keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9) {
                                NumberKeyMap.put((Integer) keyFields[i].get(null), tmpName);
                            } else if (keycode >= KeyEvent.KEYCODE_A && keycode <= KeyEvent.KEYCODE_Z) {
                                AlphabetKeyMap.put((Integer) keyFields[i].get(null), tmpName);
                            } else if (keycode >= KeyEvent.KEYCODE_F1 && keycode <= KeyEvent.KEYCODE_F12) {
                                FunctionKeyMap.put((Integer) keyFields[i].get(null), tmpName);
                            } else {
                                OtherKeyMap.put((Integer) keyFields[i].get(null), tmpName);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            ULog.w(TAG, "Non-static field : " + tmpName);
        } catch (IllegalArgumentException e1) {
            ULog.w(TAG, "Type mismatch : " + tmpName);
        } catch (IllegalAccessException e2) {
            ULog.w(TAG, "Non-public field : " + tmpName);
        }
    }
}
