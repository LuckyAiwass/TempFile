package com.ubx.featuresettings.util;

import android.content.ContentResolver;
import com.android.internal.statusbar.IStatusBarService;
import android.os.ServiceManager;
import android.content.Context;
import android.net.Uri;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.app.StatusBarManager;
import android.util.Log;

public class SettingsContentObserver extends ContentObserver {
	
	private IBinder mToken = new Binder();
	private Context mContext;

    private Handler mHandler;  //此Handler用来更新UI线程
        private IStatusBarService mStatusBarService;


    public SettingsContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                if (uri.toString().endsWith("UROVO_STATUSBAR_ENABLE")) {
                    int statusBarFlags = android.provider.Settings.System.getInt(mContext.getContentResolver(),"UROVO_STATUSBAR_ENABLE", StatusBarManager.DISABLE_NONE);
                    mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService(Context.STATUS_BAR_SERVICE));
                    if (mStatusBarService != null) {
                        mStatusBarService.disable(statusBarFlags, mToken, mContext.getPackageName());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          