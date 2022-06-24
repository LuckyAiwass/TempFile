package  com.ubx.featuresettings.util;

import android.device.admin.SettingProperty;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class SettingsUtils {
    public final static String TAG = SettingsUtils.class.getSimpleName();

    public static ArrayList<String> readStringListFormFile(String filePath) {
        Log.i(TAG, "readStringListFormFile list from file [" + filePath + "]");
        ArrayList<String> list = new ArrayList();
        File f = new File(filePath);
        if (!f.exists()) {
            return list;
        }
        BufferedReader reader = null;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                if (!TextUtils.isEmpty(line.trim())) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static boolean writeListStringToFile(List<String> stringList, String filePath) {
        Log.i(TAG, "writeListStringToFile list to file [" + filePath + "]");

        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File f = new File(filePath);

        if (stringList == null || stringList.size() == 0) {
            if (f.exists() && f.isFile()){
                if (f.delete()) {
                    Log.i(TAG,"Success delete files:"+filePath);
                }
            }
        }


        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        BufferedWriter writer = null;
        boolean ret = false;
        try {
            if (!f.exists()) {
                boolean result = f.createNewFile();
                if (!result) return false;
            }

            writer = new BufferedWriter(new FileWriter(f, false));

            for (String str : stringList) {
                writer.write(str);
                writer.newLine();
            }
            ret = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (IOException e2) {
                // TODO: handle exception
            }
        }
        return ret;
    }

    public static List<String> addStringToStringListFile(String str, String filePath) {
        ArrayList<String> stringList = readStringListFormFile(filePath);
        Set<String> stringSet = new HashSet<>(stringList);
        stringSet.add(str);
        writeListStringToFile(new ArrayList<>(stringSet), filePath);
        return readStringListFormFile(filePath);
    }

    public static List<String> removeStringFromStringListFile(String str, String filePath) {
        ArrayList<String> stringList = readStringListFormFile(filePath);
        Set<String> stringSet = new HashSet<>(stringList);
        stringSet.remove(str);
        writeListStringToFile(new ArrayList<>(stringSet), filePath);
        return readStringListFormFile(filePath);
    }

    public static boolean isWifiAllowed(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        ArrayList<String> list = readStringListFormFile(SettingProperty.WIFI_WHITELIST_FILE);
        ArrayList<String> blacklist = readStringListFormFile(SettingProperty.WIFI_BLACKLIST_FILE);
        if (list != null && list.size() > 0 && !list.contains(ssid)) {
            return false;
        }

        if (blacklist != null && blacklist.size() > 0 && blacklist.contains(ssid)) {
            return false;
        }

        return true;
    }

    public static void setWifiWhiteList(String ssid, int mode, int action) {
        String filepath = SettingProperty.WIFI_WHITELIST_FILE;
        if (mode == 0) {
            filepath = SettingProperty.WIFI_WHITELIST_FILE;
        } else if (mode == 1) {
            filepath = SettingProperty.WIFI_BLACKLIST_FILE;
        }

        if (action == 0) {
            File file = new File(filepath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    Log.d(TAG, "Successful delete of files:" + filepath);
                }
            }
        } else if (action == 1) {
            if (!TextUtils.isEmpty(ssid)) {
                addStringToStringListFile(ssid, filepath);
            }
        } else if (action == 2) {
            if (!TextUtils.isEmpty(ssid)) {
                removeStringFromStringListFile(ssid, filepath);
            }
        }
    }


    public static boolean isBTAllowed(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        ArrayList<String> list = readStringListFormFile(SettingProperty.BT_WHITELIST_FILE);
        ArrayList<String> blacklist = readStringListFormFile(SettingProperty.BT_BLACKLIST_FILE);
        if (list != null && list.size() > 0 && !list.contains(ssid)) {
            return false;
        }

        if (blacklist != null && blacklist.size() > 0 && blacklist.contains(ssid)) {
            return false;
        }

        return true;
    }

    public static void setBTWhiteList(String ssid, int mode, int action) {
        String filepath = SettingProperty.BT_WHITELIST_FILE;
        if (mode == 0) {
            filepath = SettingProperty.BT_WHITELIST_FILE;
        } else if (mode == 1) {
            filepath = SettingProperty.BT_BLACKLIST_FILE;
        }

        if (action == 0) {
            File file = new File(filepath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    Log.d(TAG, "Successful delete of files:" + filepath);
                }
            }
        } else if (action == 1) {
            if (!TextUtils.isEmpty(ssid)) {
                addStringToStringListFile(ssid, filepath);
            }
        } else if (action == 2) {
            if (!TextUtils.isEmpty(ssid)) {
                removeStringFromStringListFile(ssid, filepath);
            }
        }
    }



    public static String addPackageName(String packageNames, String packageName) {
        if (TextUtils.isEmpty(packageNames)) {
            return packageName;
        }
        if (!containsPackageName(packageNames, packageName)) {
            if (packageNames.length() > 0) {
                packageNames += ",";
            }
            packageNames += packageName;
        }
        return packageNames;
    }


    public static String removePackageName(String packageNames, String packageName) {
        String[] split = packageNames.split(",");
        for (int i = 0; i < split.length; i++) {
            if (packageName.equals(split[i])) {
                split[i] = null;
            }
        }
        if (split.length == 1 && split[0] == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(s);
            }
        }
        return builder.toString();
    }


    public static boolean containsPackageName(String packageNames, String packageName) {
        if (packageNames == null) return false;
        int index = packageNames.indexOf(packageName);
        if (index < 0) return false;
        if (index > 0 && packageNames.charAt(index - 1) != ',') return false;
        int charAfter = index + packageName.length();
        if (charAfter < packageNames.length() && packageNames.charAt(charAfter) != ',')
            return false;
        return true;
    }

    public static String join(Collection<?> s, String delimiter) {

        StringBuilder builder = new StringBuilder();

        Iterator iter = s.iterator();

        while (iter.hasNext()) {

            builder.append(iter.next());

            if (!iter.hasNext()) {

                break;

            }

            builder.append(delimiter);

        }

        return builder.toString();
    }

    public static List<String> stringToStringList(String string) {
        if (!TextUtils.isEmpty(string)) {
            String[] split = string.split(",");
            return java.util.Arrays.asList(split);
        }
        return null;
    }
}
