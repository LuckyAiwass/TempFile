/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.ubx.appinstall.util;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageParserException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.UserHandle;
import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * This is a utility class for defining some utility methods and constants
 * used in the package installer application.
 */
public class PackageUtil {
    public static final String PREFIX="com.android.packageinstaller.";
    public static final String INTENT_ATTR_INSTALL_STATUS = PREFIX+"installStatus";
    public static final String INTENT_ATTR_APPLICATION_INFO=PREFIX+"applicationInfo";
    public static final String INTENT_ATTR_PERMISSIONS_LIST=PREFIX+"PermissionsList";
    //intent attribute strings related to uninstall
    public static final String INTENT_ATTR_PACKAGE_NAME=PREFIX+"PackageName";
    public static final int INSTALL_SUCCEEDED = 1;
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
    public static final int INSTALL_FAILED_INVALID_APK = -2;
    public static final int INSTALL_FAILED_INVALID_URI = -3;
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;


    /**
     * Utility method to get package information for a given {@link File}
     */
    public static PackageParser.Package getPackageInfo(File sourceFile) {
        final PackageParser parser = new PackageParser();
        try {
            return parser.parsePackage(sourceFile, 0);
        } catch (PackageParserException e) {
            return null;
        }
    }

    public static View initSnippet(View snippetView, CharSequence label, Drawable icon) {
//        ((ImageView)snippetView.findViewById(R.id.app_icon)).setImageDrawable(icon);
//        ((TextView)snippetView.findViewById(R.id.app_name)).setText(label);
        return snippetView;
    }

    /**
     * Utility method to display a snippet of an installed application.
     * The content view should have been set on context before invoking this method.
     * appSnippet view should include R.id.app_icon and R.id.app_name
     * defined on it.
     *
     * @param pContext context of package that can load the resources
     * @param componentInfo ComponentInfo object whose resources are to be loaded
     * @param snippetView the snippet view
     */
    public static View initSnippetForInstalledApp(Activity pContext,
            ApplicationInfo appInfo, View snippetView) {
        return initSnippetForInstalledApp(pContext, appInfo, snippetView, null);
    }

    /**
     * Utility method to display a snippet of an installed application.
     * The content view should have been set on context before invoking this method.
     * appSnippet view should include R.id.app_icon and R.id.app_name
     * defined on it.
     *
     * @param pContext context of package that can load the resources
     * @param componentInfo ComponentInfo object whose resources are to be loaded
     * @param snippetView the snippet view
     * @param UserHandle user that the app si installed for.
     */
    public static View initSnippetForInstalledApp(Activity pContext,
            ApplicationInfo appInfo, View snippetView, UserHandle user) {
        final PackageManager pm = pContext.getPackageManager();
        Drawable icon = appInfo.loadIcon(pm);
        if (user != null) {
            icon = pContext.getPackageManager().getUserBadgedIcon(icon, user);
        }
        return initSnippet(
                snippetView,
                appInfo.loadLabel(pm),
                icon);
    }

    /**
     * Utility method to display application snippet of a new package.
     * The content view should have been set on context before invoking this method.
     * appSnippet view should include R.id.app_icon and R.id.app_name
     * defined on it.
     *
     * @param pContext context of package that can load the resources
     * @param appInfo ApplicationInfo object of package whose resources are to be loaded
     * @param snippetId view id of app snippet view
     */
    public static View initSnippetForNewApp(Activity pContext, AppSnippet as,
            int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
//        ((ImageView)appSnippet.findViewById(R.id.app_icon)).setImageDrawable(as.icon);
//        ((TextView)appSnippet.findViewById(R.id.app_name)).setText(as.label);
        return appSnippet;
    }

    public static boolean isPackageAlreadyInstalled(Activity context, String pkgName) {
        List<PackageInfo> installedList = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        int installedListSize = installedList.size();
        for(int i = 0; i < installedListSize; i++) {
            PackageInfo tmp = installedList.get(i);
            if(pkgName.equalsIgnoreCase(tmp.packageName)) {
                return true;
            }
        }
        return false;
    }

    static public class AppSnippet {
        CharSequence label;
        Drawable icon;
        public AppSnippet(CharSequence label, Drawable icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    public static String installStatusToString(int status) {
        switch (status) {
            case PackageManager.INSTALL_SUCCEEDED: return "Install Success";
            case PackageManager.INSTALL_FAILED_ALREADY_EXISTS: return "The package is already installed";
            case PackageManager.INSTALL_FAILED_INVALID_APK: return "The package is not available";
            case PackageManager.INSTALL_FAILED_INVALID_URI: return "The package address is not available";
            case PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE: return "Space is not enough to install the application";
            case PackageManager.INSTALL_FAILED_DUPLICATE_PACKAGE: return "An installer with the same name already exists";
            case PackageManager.INSTALL_FAILED_NO_SHARED_USER: return "Shared user does not exist";
            case PackageManager.INSTALL_FAILED_UPDATE_INCOMPATIBLE: return "Incompatible check updates";
            case PackageManager.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: return "Shared user mismatch";
            case PackageManager.INSTALL_FAILED_MISSING_SHARED_LIBRARY: return "Shared library is incorrect";
            case PackageManager.INSTALL_FAILED_REPLACE_COULDNT_DELETE: return "Shared library internal error";
            case PackageManager.INSTALL_FAILED_DEXOPT: return "Insufficient space or verification failure (internal DEX file error)";
            case PackageManager.INSTALL_FAILED_OLDER_SDK: return "The SDK version of the package is too old";
            case PackageManager.INSTALL_FAILED_CONFLICTING_PROVIDER: return "A content provider with the same name exists";
            case PackageManager.INSTALL_FAILED_NEWER_SDK: return "The SDK version of the installation package is too new";
            case PackageManager.INSTALL_FAILED_TEST_ONLY: return "The caller is not allowed to test the test program";
            case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE: return "Native library and system architecture are inconsistent";
            case PackageManager.INSTALL_FAILED_MISSING_FEATURE: return "The package has referenced new features";
            case PackageManager.INSTALL_FAILED_CONTAINER_ERROR: return "The secure container mount point cannot be accessed on external media";
            case PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION: return "The package cannot be installed in the specified installation location";
            case PackageManager.INSTALL_FAILED_MEDIA_UNAVAILABLE: return "The package cannot be installed in the specified installation location because the media is not available";
            case PackageManager.INSTALL_FAILED_VERIFICATION_TIMEOUT: return "Unable to install new package due to validation timeout";
            case PackageManager.INSTALL_FAILED_VERIFICATION_FAILURE: return "The new package cannot be installed because the validation was not successful";
            case PackageManager.INSTALL_FAILED_PACKAGE_CHANGED: return "The package changes the expectations of the calling program";
            case PackageManager.INSTALL_FAILED_UID_CHANGED: return "The new package is assigned a different uid than previously saved";
            case PackageManager.INSTALL_FAILED_VERSION_DOWNGRADE: return "The old version code of the new package is older than the currently installed package";
            case PackageManager.INSTALL_PARSE_FAILED_NOT_APK: return "The parser is given a path that is not a file or does not end with the expected '.apk' extension";
            case PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST: return "The parser cannot retrieve the AndroidManifest.xml";
            case PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION: return "The parser encountered an unexpected exception";
            case PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES: return "The parser could not find any certificates in the '.apk'";
            case PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES: return "The parser found an inconsistent certificate on the file in '.apk'";
            case PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING: return "The parser encountered a CertificateEncodingException in a file in '.apk'";
            case PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME: return "The parser encountered an error or missing package name in the manifest";
            case PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID: return "The parser encountered an incorrect shared userid name in the manifest";
            case PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED: return "The parser encountered some structural problems in the list";
            case PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY: return "The parser did not find any actionable tags (instrumentation or applications) in the manifest";
            case PackageManager.INSTALL_FAILED_INTERNAL_ERROR: return "The system cannot install the package due to system problems";
            case PackageManager.INSTALL_FAILED_USER_RESTRICTED: return "The system cannot install the package because the user cannot install the application";
            case PackageManager.INSTALL_FAILED_DUPLICATE_PERMISSION: return "The system cannot install a package because it is trying to define permissions that are already defined by an existing package";
            case PackageManager.INSTALL_FAILED_NO_MATCHING_ABIS: return "The system cannot install the package because its packaged native code does not match any ABI supported by the system";
            case PackageManager.INSTALL_FAILED_ABORTED: return "The package was terminated by the system during installation";
            case PackageManager.INSTALL_FAILED_AUTH_CERTIFICATE: return "The package verification failed";
            default: return Integer.toString(status);
        }
    }


    /**
     * Utility method to load application label
     *
     * @param pContext context of package that can load the resources
     * @param appInfo ApplicationInfo object of package whose resources are to be loaded
     * @param snippetId view id of app snippet view
     */
    public static AppSnippet getAppSnippet(
            Context pContext, ApplicationInfo appInfo, File sourceFile) {
        final String archiveFilePath = sourceFile.getAbsolutePath();
        Resources pRes = pContext.getResources();
        AssetManager assmgr = new AssetManager();
        assmgr.addAssetPath(archiveFilePath);
        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
        CharSequence label = null;
        // Try to load the label from the package's resources. If an app has not explicitly
        // specified any label, just use the package name.
        if (appInfo.labelRes != 0) {
            try {
                label = res.getText(appInfo.labelRes);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (label == null) {
            label = (appInfo.nonLocalizedLabel != null) ?
                    appInfo.nonLocalizedLabel : appInfo.packageName;
        }
        Drawable icon = null;
        // Try to load the icon from the package's resources. If an app has not explicitly
        // specified any resource, just use the default icon for now.
        if (appInfo.icon != 0) {
            try {
                icon = res.getDrawable(appInfo.icon);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (icon == null) {
            icon = pContext.getPackageManager().getDefaultActivityIcon();
        }
        return new PackageUtil.AppSnippet(label, icon);
    }
}
