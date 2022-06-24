/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dialer.dialpadview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.common.io.MoreCloseables;
import com.android.contacts.common.database.NoNullCursorAsyncQueryHandler;
import com.android.contacts.common.util.ContactDisplayUtils;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment.SelectPhoneAccountListener;
import com.android.contacts.common.widget.SelectPhoneAccountDialogOptionsUtil;
import com.android.dialer.common.Assert;
import com.android.dialer.common.LogUtil;
import com.android.dialer.compat.telephony.TelephonyManagerCompat;
import com.android.dialer.oem.MotorolaUtils;
import com.android.dialer.oem.TranssionUtils;
import com.android.dialer.telecom.TelecomUtil;
import com.android.dialer.util.PermissionsUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.SystemProperties;

// urovo add luolin begin 2018-12-03
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.os.Build;
import android.os.SystemProperties;
import java.util.ArrayList;
// urovo add luolin end 2018-12-03

/**
 * Helper class to listen for some magic character sequences that are handled specially by Dialer.
 */
public class SpecialCharSequenceMgr {

  private static final String TAG = "SpecialCharSequenceMgr";

  private static final String TAG_SELECT_ACCT_FRAGMENT = "tag_select_acct_fragment";

  @VisibleForTesting static final String MMI_IMEI_DISPLAY = "*#06#";
  private static final String MMI_REGULATORY_INFO_DISPLAY = "*#07#";
  // urovo add luolin begin 2018-12-03
  // private static final String TAG                    = "SpecialCharSequenceMgr";
  private static final String PRL_VERSION_DISPLAY    = "*#0000#";
  private static final String SCAN_AGE               = "*#1260*#";
  private static final String TESTING_SCAN_TYPE      = "*#1261*#";
  private static final String TESTING_TOOL_UROVO     = "*#1262*#";
  private static final String WIFI_TOOL              = "*#1267*#";
  private static final String GPS_TOOL               = "*#311";
  private static final String DEVICEINFO_DISPLAY     = "*#316";
  private static final String TESTING_MOBILE_NETWORK = "*#410";
  private static final String SET_USB_CONFIG         = "*#3258";
  private static final String SET_USB_CONFIG_3260    = "*#3260";
  private static final String LOGCAT                 = "*#3333#*";
  // urovo add luolin end 2018-12-03

  /** ***** This code is used to handle SIM Contact queries ***** */
  private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";

  private static final String ADN_NAME_COLUMN_NAME = "name";
  private static final int ADN_QUERY_TOKEN = -1;
  private static final String PRL_HIDDENMENU_TEST_DISPLAY = "*#1262*#";//wangyinghua_20181107 for FacrotyKit
  private static final String AGING_TEST_DISPLAY = "*#1260*#";//wangyinghua 20180323 S450 add for AgingTest
  private static final String HIDDEN_FULL_PREFERNETWORK = "*#23744#"; //wendylcj add for fullpreferlist
  private static  boolean isTrue = false;//wangyinghua_20190412
    private static  boolean mode = false;

  @VisibleForTesting
  static final List<String> TRANSSION_CODES =
      new ArrayList<String>() {
        {
          add("*#07#");
          add("*#87#");
          add("*#43#");
          add("*#2727#");
          add("*#88#");
        }
      };

  /**
   * Remembers the previous {@link QueryHandler} and cancel the operation when needed, to prevent
   * possible crash.
   *
   * <p>QueryHandler may call {@link ProgressDialog#dismiss()} when the screen is already gone,
   * which will cause the app crash. This variable enables the class to prevent the crash on {@link
   * #cleanup()}.
   *
   * <p>TODO: Remove this and replace it (and {@link #cleanup()}) with better implementation. One
   * complication is that we have SpecialCharSequenceMgr in Phone package too, which has *slightly*
   * different implementation. Note that Phone package doesn't have this problem, so the class on
   * Phone side doesn't have this functionality. Fundamental fix would be to have one shared
   * implementation and resolve this corner case more gracefully.
   */
  private static QueryHandler previousAdnQueryHandler;

  /** This class is never instantiated. */
  private SpecialCharSequenceMgr() {}

  public static boolean handleChars(Context context, String input, EditText textField) {
    // get rid of the separators so that the string gets parsed correctly
    String dialString = PhoneNumberUtils.stripSeparators(input);

    if (handleDeviceIdDisplay(context, dialString)
        // urovo add luolin begin 2018-12-03
        || handlePRLVersion(context, dialString)
        || handleScanAgeTest(context, dialString)
        || handleScanType(context, dialString)
        || handleTestingEntry(context, dialString)
        || handlewifiToolTest(context, dialString)
        || handleGpsTool(context, dialString)
        || handleDeviceInfoEntry(context, dialString)
        || handleShowNetworkSetting(context, dialString)
        || handleSetUsbConfig(context, dialString)
        || handleLogcat(context, dialString)
        // urovo add luolin end 2018-12-03
        || handleRegulatoryInfoDisplay(context, dialString)
        || handlePinEntry(context, dialString)
        //shaoweijie hide adn.20201222
        //|| handleAdnEntry(context, dialString, textField)
        || handleSecretCode(context, dialString)
        || handleAgingTestDisplay(context, dialString)
        || handleHiddenMneuTestDisplay(context, dialString)//wangyinghua_20181107 add forFactoryKit
        || handleHiddenPreferNetwork(context, dialString)//wendylcj add for fullpreferlist
	 ) {
      return true;
    }

    if (MotorolaUtils.handleSpecialCharSequence(context, input)) {
      return true;
    }

    return false;
  }

  /**
   * Cleanup everything around this class. Must be run inside the main thread.
   *
   * <p>This should be called when the screen becomes background.
   */
  public static void cleanup() {
    Assert.isMainThread();

    if (previousAdnQueryHandler != null) {
      previousAdnQueryHandler.cancel();
      previousAdnQueryHandler = null;
    }
  }

  //wangyinghua 20180323 S450 add for AgingTest [S]
  static private boolean handleAgingTestDisplay(Context context, String input) {
		  if (input.equals(AGING_TEST_DISPLAY)) {
			  try {
				  Intent intent = new Intent("com.udroid.intent.action.AGEING_DEVICE");
				  context.startActivity(intent);
				  return true;
			  } catch (ActivityNotFoundException e) {
				  Log.d(TAG, "no activity to handle showing AgingTest");
			  }
		  }
		  return false;
	  }
 //wangyinghua 20180323 S450 add for AgingTest [E]

  //wangyinghua_20181107 add for FactoryKit [S]
	  static private boolean handleHiddenMneuTestDisplay(Context context, String input) {
		   
		  if (input.equals(PRL_HIDDENMENU_TEST_DISPLAY)) {
			  try {
				  //Intent intent = new Intent("unicair.intent.action.ENGINEER_MODE_HIDDENMENU");
				  Intent intent = new Intent();
                  intent.setComponent(new ComponentName("com.ubx.factorykit", "com.ubx.factorykit.Framework.Framework"));
				  context.startActivity(intent);
				  return true;
			  } catch (ActivityNotFoundException e) {
				  Log.d(TAG, "no activity to handle showing HiddenMenu");
			  }
		  }
		  return false;
	  }
  //wangyinghua_20181107 add for FactoryKit [E]

  /**
   * Handles secret codes to launch arbitrary activities in the form of
   * *#*#<code>#*#* or *#<code_starting_with_number>#.
   *
   * @param context the context to use
   * @param input the text to check for a secret code in
   * @return true if a secret code was encountered and handled
   */
  static boolean handleSecretCode(Context context, String input) {
    // Secret code specific to OEMs should be handled first.
    if (TranssionUtils.isTranssionSecretCode(input)) {
      TranssionUtils.handleTranssionSecretCode(context, input);
      return true;
    }

    // Secret codes are accessed by dialing *#*#<code>#*#* or "*#<code_starting_with_number>#"
    if (input.length() > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
      String secretCode = input.substring(4, input.length() - 4);
      TelephonyManagerCompat.handleSecretCode(context, secretCode);
      return true;
    }

    return false;
  }

  /**
   * Handle ADN requests by filling in the SIM contact number into the requested EditText.
   *
   * <p>This code works alongside the Asynchronous query handler {@link QueryHandler} and query
   * cancel handler implemented in {@link SimContactQueryCookie}.
   */
  static boolean handleAdnEntry(Context context, String input, EditText textField) {
    /* ADN entries are of the form "N(N)(N)#" */
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (telephonyManager == null
        || telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM) {
      return false;
    }

    // if the phone is keyguard-restricted, then just ignore this
    // input.  We want to make sure that sim card contacts are NOT
    // exposed unless the phone is unlocked, and this code can be
    // accessed from the emergency dialer.
    KeyguardManager keyguardManager =
        (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    if (keyguardManager.inKeyguardRestrictedInputMode()) {
      return false;
    }

    int len = input.length();
    if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
      try {
        // get the ordinal number of the sim contact
        final int index = Integer.parseInt(input.substring(0, len - 1));

        // The original code that navigated to a SIM Contacts list view did not
        // highlight the requested contact correctly, a requirement for PTCRB
        // certification.  This behaviour is consistent with the UI paradigm
        // for touch-enabled lists, so it does not make sense to try to work
        // around it.  Instead we fill in the the requested phone number into
        // the dialer text field.

        // create the async query handler
        final QueryHandler handler = new QueryHandler(context.getContentResolver());

        // create the cookie object
        final SimContactQueryCookie sc =
            new SimContactQueryCookie(index - 1, handler, ADN_QUERY_TOKEN);

        // setup the cookie fields
        sc.contactNum = index - 1;
        sc.setTextField(textField);

        // create the progress dialog
        sc.progressDialog = new ProgressDialog(context);
        sc.progressDialog.setTitle(R.string.simContacts_title);
        sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
        sc.progressDialog.setIndeterminate(true);
        sc.progressDialog.setCancelable(true);
        sc.progressDialog.setOnCancelListener(sc);
        sc.progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        List<PhoneAccountHandle> subscriptionAccountHandles =
            TelecomUtil.getSubscriptionPhoneAccounts(context);
        Context applicationContext = context.getApplicationContext();
        boolean hasUserSelectedDefault =
            subscriptionAccountHandles.contains(
                TelecomUtil.getDefaultOutgoingPhoneAccount(
                    applicationContext, PhoneAccount.SCHEME_TEL));

        if (subscriptionAccountHandles.size() <= 1 || hasUserSelectedDefault) {
          Uri uri = TelecomUtil.getAdnUriForPhoneAccount(applicationContext, null);
          handleAdnQuery(handler, sc, uri);
        } else {
          SelectPhoneAccountListener callback =
              new HandleAdnEntryAccountSelectedCallback(applicationContext, handler, sc);
          DialogFragment dialogFragment =
              SelectPhoneAccountDialogFragment.newInstance(
                  SelectPhoneAccountDialogOptionsUtil.builderWithAccounts(
                          subscriptionAccountHandles)
                      .build(),
                  callback);
          dialogFragment.show(((Activity) context).getFragmentManager(), TAG_SELECT_ACCT_FRAGMENT);
        }

        return true;
      } catch (NumberFormatException ex) {
        // Ignore
      }
    }
    return false;
  }

  private static void handleAdnQuery(QueryHandler handler, SimContactQueryCookie cookie, Uri uri) {
    if (handler == null || cookie == null || uri == null) {
      LogUtil.w("SpecialCharSequenceMgr.handleAdnQuery", "queryAdn parameters incorrect");
      return;
    }

    // display the progress dialog
    cookie.progressDialog.show();

    // run the query.
    handler.startQuery(
        ADN_QUERY_TOKEN,
        cookie,
        uri,
        new String[] {ADN_PHONE_NUMBER_COLUMN_NAME},
        null,
        null,
        null);

    if (previousAdnQueryHandler != null) {
      // It is harmless to call cancel() even after the handler's gone.
      previousAdnQueryHandler.cancel();
    }
    previousAdnQueryHandler = handler;
  }

  static boolean handlePinEntry(final Context context, final String input) {
    if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
      List<PhoneAccountHandle> subscriptionAccountHandles =
          TelecomUtil.getSubscriptionPhoneAccounts(context);
      boolean hasUserSelectedDefault =
          subscriptionAccountHandles.contains(
              TelecomUtil.getDefaultOutgoingPhoneAccount(context, PhoneAccount.SCHEME_TEL));

      if (subscriptionAccountHandles.size() <= 1 || hasUserSelectedDefault) {
        // Don't bring up the dialog for single-SIM or if the default outgoing account is
        // a subscription account.
        return TelecomUtil.handleMmi(context, input, null);
      } else {
        SelectPhoneAccountListener listener = new HandleMmiAccountSelectedCallback(context, input);

        DialogFragment dialogFragment =
            SelectPhoneAccountDialogFragment.newInstance(
                SelectPhoneAccountDialogOptionsUtil.builderWithAccounts(subscriptionAccountHandles)
                    .build(),
                listener);
        dialogFragment.show(((Activity) context).getFragmentManager(), TAG_SELECT_ACCT_FRAGMENT);
      }
      return true;
    }
    return false;
  }

  // TODO: Use TelephonyCapabilities.getDeviceIdLabel() to get the device id label instead of a
  // hard-coded string.
  @SuppressLint("HardwareIds")
  static boolean handleDeviceIdDisplay(Context context, String input) {
	  TelephonyManager telephonyManager =
		  (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	  SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
	  if (telephonyManager != null && input.equals(MMI_IMEI_DISPLAY)) {
		isTrue = false;
		String label = context.getResources().getString(R.string.meid) + " & " +
		  context.getResources().getString(R.string.imei);
        Log.d(TAG,"label=="+label);
		List<String> deviceIds = new ArrayList<String>();
		Log.d(TAG,"getPhoneCount=="+TelephonyManagerCompat.getPhoneCount(telephonyManager));
		deviceIds.add("MEID:");
		if (TelephonyManagerCompat.getPhoneCount(telephonyManager) > 1) {			
		  // Add MEID or ESN
		  String deviceId = null;
		  for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
			String meidOrEsn = getMeidOrEsn(telephonyManager, subscriptionManager, slot);
			if ((deviceId == null && isValidMeid(meidOrEsn))
				|| (deviceId != null && !deviceId.equals(meidOrEsn)
				&& isValidMeid(meidOrEsn))) {
			  deviceIds.add(meidOrEsn);
			}
			
			deviceId = meidOrEsn;
		  }
		  // Add IMEI
		  //deviceIds.add("IMEI:");
		  for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
			String imei = telephonyManager.getImei(slot);			
			String str = null;
			if(isTrue){
				str = "IMEI2:";
			}else{
				str = "IMEI1:";
			}
			if (!TextUtils.isEmpty(imei)) {	
              deviceIds.add(str);			
			  deviceIds.add(imei);
			  isTrue = true;
			}
			
		  }
		} else {		  
		  String meidOrEsn = getMeidOrEsn(telephonyManager, subscriptionManager, 0);
		  if (isValidMeid(meidOrEsn)) {
			deviceIds.add(meidOrEsn);
		  }
		  String imei = telephonyManager.getImei();
		  Log.d(TAG,"imei=="+imei);
		  if (!TextUtils.isEmpty(imei)) {
			
			deviceIds.add(imei);
		  }
		}

		new AlertDialog.Builder(context)
			.setTitle(label)
			.setItems(deviceIds.toArray(new String[deviceIds.size()]), null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  ((Activity)context).finish();//yangpeng
					  }
				  })
			.setCancelable(false)
			.show();
		return true;
	  }
	  return false;
	}

   private static boolean isValidMeid(String meid) {
    if (!TextUtils.isEmpty(meid) && !meid.equals("0")
        && !meid.startsWith("000000")) {
      return true;
    }
    return false;
  }

  private static String getMeidOrEsn(TelephonyManager tm, SubscriptionManager sm, int slot) {
    if (tm != null) {
      String deviceId = tm.getMeid(slot);
      if (!isValidMeid(deviceId)) {
        SubscriptionInfo subInfo = sm
            .getActiveSubscriptionInfoForSimSlotIndex(slot);
        if (subInfo != null) {
          try {
            Method getEsn = tm.getClass()
              .getDeclaredMethod("getEsn", new Class[]{int.class});
            getEsn.setAccessible(true);
            deviceId = (String) getEsn.invoke(tm, subInfo.getSubscriptionId());
          } catch (NoSuchMethodException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
      return deviceId;
    }
    return "";
  }

  private static void addDeviceIdRow(
      ViewGroup holder, String deviceId, boolean showDecimal, boolean showBarcode) {
    if (TextUtils.isEmpty(deviceId)) {
      return;
    }

    ViewGroup row =
        (ViewGroup)
            LayoutInflater.from(holder.getContext()).inflate(R.layout.row_deviceid, holder, false);
    holder.addView(row);

    // Remove the check digit, if exists. This digit is a checksum of the ID.
    // See https://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity
    // and https://en.wikipedia.org/wiki/Mobile_equipment_identifier
    String hex = deviceId.length() == 15 ? deviceId.substring(0, 14) : deviceId;

    // If this is the valid length IMEI or MEID (14 digits), show it in all formats, otherwise fall
    // back to just showing the raw hex
    if (hex.length() == 14 && showDecimal) {
      ((TextView) row.findViewById(R.id.deviceid_hex)).setText(hex);
      ((TextView) row.findViewById(R.id.deviceid_dec)).setText(getDecimalFromHex(hex));
      row.findViewById(R.id.deviceid_dec_label).setVisibility(View.VISIBLE);
    } else {
      row.findViewById(R.id.deviceid_hex_label).setVisibility(View.GONE);
      ((TextView) row.findViewById(R.id.deviceid_hex)).setText(deviceId);
    }

    final ImageView barcode = row.findViewById(R.id.deviceid_barcode);
    if (showBarcode) {
      // Wait until the layout pass has completed so we the barcode is measured before drawing. We
      // do this by adding a layout listener and setting the bitmap after getting the callback.
      barcode
          .getViewTreeObserver()
          .addOnGlobalLayoutListener(
              new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                  barcode.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                  Bitmap barcodeBitmap =
                      generateBarcode(hex, barcode.getWidth(), barcode.getHeight());
                  if (barcodeBitmap != null) {
                    barcode.setImageBitmap(barcodeBitmap);
                  }
                }
              });
    } else {
      barcode.setVisibility(View.GONE);
    }
  }

  private static String getDecimalFromHex(String hex) {
    final String part1 = hex.substring(0, 8);
    final String part2 = hex.substring(8);

    long dec1;
    try {
      dec1 = Long.parseLong(part1, 16);
    } catch (NumberFormatException e) {
      LogUtil.e("SpecialCharSequenceMgr.getDecimalFromHex", "unable to parse hex", e);
      return "";
    }

    final String manufacturerCode = String.format(Locale.US, "%010d", dec1);

    long dec2;
    try {
      dec2 = Long.parseLong(part2, 16);
    } catch (NumberFormatException e) {
      LogUtil.e("SpecialCharSequenceMgr.getDecimalFromHex", "unable to parse hex", e);
      return "";
    }

    final String serialNum = String.format(Locale.US, "%08d", dec2);

    StringBuilder builder = new StringBuilder(22);
    builder
        .append(manufacturerCode, 0, 5)
        .append(' ')
        .append(manufacturerCode, 5, manufacturerCode.length())
        .append(' ')
        .append(serialNum, 0, 4)
        .append(' ')
        .append(serialNum, 4, serialNum.length());
    return builder.toString();
  }

  /**
   * This method generates a 2d barcode using the zxing library. Each pixel of the bitmap is either
   * black or white painted vertically. We determine which color using the BitMatrix.get(x, y)
   * method.
   */
  private static Bitmap generateBarcode(String hex, int width, int height) {
    MultiFormatWriter writer = new MultiFormatWriter();
    String data = Uri.encode(hex);

    try {
      BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, 1);
      Bitmap bitmap = Bitmap.createBitmap(bitMatrix.getWidth(), height, Config.RGB_565);

      for (int i = 0; i < bitMatrix.getWidth(); i++) {
        // Paint columns of width 1
        int[] column = new int[height];
        Arrays.fill(column, bitMatrix.get(i, 0) ? Color.BLACK : Color.WHITE);
        bitmap.setPixels(column, 0, 1, i, 0, 1, height);
      }
      return bitmap;
    } catch (WriterException e) {
      LogUtil.e("SpecialCharSequenceMgr.generateBarcode", "error generating barcode", e);
    }
    return null;
  }

  /**********************************urovo SecretCode**********************************/
  //*#0000# 无实际参考意义，后续启用
  static private boolean handlePRLVersion(Context context, String input) {
    if (input.equals(PRL_VERSION_DISPLAY)) {
      try {
        Intent intent = new Intent("org.codeaurora.intent.action.ACTION_DEVICEINFO");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
      } catch (ActivityNotFoundException e) {
        LogUtil.d(
            "SpecialCharSequenceMgr.handlePRLVersion", "no activity to handle showing device info");
      }
    }
    return false;
  }

  //*#1260*#
  static boolean handleScanAgeTest(Context context, String input) {
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleScanAgeTest , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if (len > 4 && (input.equals(SCAN_AGE))) {
        Intent intent = new Intent("com.udroid.intent.action.AGEING_DEVICE");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    return false;
  }

  //*#1261*#
  static boolean handleScanType(Context context, String input) {
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleScanType , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if (len > 4 && (input.equals(TESTING_SCAN_TYPE))) {
        Intent intent = new Intent("com.android.intent.action.SCANNER_TYPE_SETTINGS");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
    return false;
  }

  //*#1262#
  static boolean handleTestingEntry(Context context, String input) {
    int len = input.length();
    if(len > 4 && input.equals(TESTING_TOOL_UROVO)) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.ubx.factorykit", "com.ubx.factorykit.Framework.Framework"));
        intent.putExtra("msg",true);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return true;
    }
    return false;
  }

  //*#1267#
  static boolean handlewifiToolTest(Context context ,String input){
    Log.i(TAG, "handlewifiToolTest");
    int len = input.length();
    if(len > 4 && (input.equals(WIFI_TOOL))) {
        Log.i(TAG, "handlewifiToolTest in !!!!!!!!!!!!!!!!!");
        Intent intent = new Intent("android.intent.action.wifiSetting");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    return false;
  }

  //*#311
  static boolean handleGpsTool(Context context, String input) {
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleGpsTool , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if (len > 4 && input.equals(GPS_TOOL)) {
        Intent intent = new Intent("action.TEST_LOCATION_GPS");
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
  }

  //*#316
  static boolean handleDeviceInfoEntry(Context context, String input) {
    //Secret codes are in the form *#*#<code>#*#*
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleDeviceInfoEntry , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if (len > 4 && input.equals(DEVICEINFO_DISPLAY)) {
        List<String> deviceInfos = new ArrayList<String>();
        int labelResId = R.string.device_version_info;

        String mfirmwareBuildNumber = SystemProperties.get("ro.vendor.build.id", ""); // urovo yuanwei 2019.09.11
        String deviceInfo =  mfirmwareBuildNumber != "" ? mfirmwareBuildNumber : Build.ID;
        Log.e(TAG, "handleDeviceVersionInfoDisplay AP version " +  deviceInfo);
        deviceInfo = "AP version:\n" + deviceInfo;
        deviceInfos.add(deviceInfo);

        deviceInfo = SystemProperties.get("gsm.version.baseband");
        Log.e(TAG, "handleDeviceVersionInfoDisplay  MP version " +  deviceInfo);
        deviceInfo = "MP version:\n" + deviceInfo;
        deviceInfos.add(deviceInfo);

        //qcn
        deviceInfo = SystemProperties.get("persist.sys.qcn.version", SystemProperties.get("persist.radio.qcn.version"));
        Log.e(TAG, "handleDeviceVersionInfoDisplay  QCN version " +  deviceInfo);
        if(deviceInfo == null || "".equals(deviceInfo.trim())) {
            deviceInfo = context.getResources().getString(R.string.unknown);
        }/*else{
            // parserQcn(deviceInfo);
            DeviceManager mDeviceManager = new DeviceManager();
            if(mDeviceManager != null){
                deviceInfo = mDeviceManager.parserQcn(deviceInfo);
            }
        }*/
        deviceInfo = "QCN version:\n" + deviceInfo;
        deviceInfos.add(deviceInfo);

        //hw
        deviceInfo = SystemProperties.get("ro.boot.hardwareversion", SystemProperties.get("persist.sys.hw.version", ""));
        if(deviceInfo == null || "".equals(deviceInfo.trim())) {
            deviceInfo = context.getResources().getString(R.string.unknown);
        }
        deviceInfo = "Hardware version:\n" + deviceInfo;
        deviceInfos.add(deviceInfo);

        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(labelResId)
                .setItems(deviceInfos.toArray(new String[deviceInfos.size()]), null)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
        return true;
    }
    return false;
  }

  //*#410
  static private boolean handleShowNetworkSetting(Context context, String input) {
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleShowNetworkSetting , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if (len > 4 && input.equals(TESTING_MOBILE_NETWORK)) {
        Intent intent = new Intent();
        //intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
        intent.setClassName("com.qualcomm.qti.networksetting", "com.qualcomm.qti.networksetting.MobileNetworkSettings"); //urovo yuanwei
        intent.putExtra("networksetting", true);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    return false;
  }

  //*#3258和*#3260(PDA)
  static boolean handleSetUsbConfig(Context context, String input) {
    Log.i(TAG, "handleSetUsbConfig");
    int len = input.length();
    if(len > 4 && (input.equals(SET_USB_CONFIG) || input.equals(SET_USB_CONFIG_3260))) {
        String vendorconfig = SystemProperties.get("persist.vendor.usb.config", "");
        if(TextUtils.isEmpty(vendorconfig)) vendorconfig = "diag,serial_cdev,rmnet,adb";
        SystemProperties.set("persist.sys.usb.config", vendorconfig);
        SystemProperties.set("sys.usb.state", vendorconfig);
        return true;
    }
    return false;
  }

  //*#3333#*
  static boolean handleLogcat(Context context ,String input){
    if (context == null || input == null) {
        Log.d(TAG, "Error!!! handleLogcat , context:" + context + ",input:" + input);
        return false;
    }
    int len = input.length();
    if(len > 4 && (input.equals(LOGCAT))) {
        Intent intent = new Intent();
        if(isAppInstall(context, "com.un.logredirect")){
            Log.d(TAG, "Info!!! handleLogcat , com.un.logredirect");
            intent.setClassName("com.un.logredirect", "com.un.logredirect.LogRedirectorSettings");//uner logger
        }else if(isAppInstall(context, "com.meig.logkit")){
            Log.d(TAG, "Info!!! handleLogcat , com.meig.logkit");
            intent.setClassName("com.meig.logkit", "com.meig.logkit.LogkitSetings");//urovo logger
        }else if(isAppInstall(context, "com.mediatek.mtklogger")){
            Log.d(TAG, "Info!!! handleLogcat , com.mediatek.mtklogger");
            intent.setClassName("com.mediatek.mtklogger", "com.mediatek.mtklogger.MainActivity");//Mtklogger
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
  }

  //是否存在应用
  private static boolean isAppInstall(Context context, String name) {
    PackageInfo packageInfo = null;
    try {
        packageInfo = context.getPackageManager().getPackageInfo(name, 0);
    } catch (Exception e) {
        packageInfo = null;
    }
    return packageInfo != null;
  }
  /**********************************urovo SecretCode**********************************/

  private static boolean handleRegulatoryInfoDisplay(Context context, String input) {
    if (input.equals(MMI_REGULATORY_INFO_DISPLAY)) {
      LogUtil.i(
          "SpecialCharSequenceMgr.handleRegulatoryInfoDisplay", "sending intent to settings app");
      Intent showRegInfoIntent = new Intent(Settings.ACTION_SHOW_REGULATORY_INFO);
      try {
        context.startActivity(showRegInfoIntent);
      } catch (ActivityNotFoundException e) {
        LogUtil.e(
            "SpecialCharSequenceMgr.handleRegulatoryInfoDisplay", "startActivity() failed: ", e);
      }
      return true;
    }
    return false;
  }

  public static class HandleAdnEntryAccountSelectedCallback extends SelectPhoneAccountListener {

    private final Context context;
    private final QueryHandler queryHandler;
    private final SimContactQueryCookie cookie;

    public HandleAdnEntryAccountSelectedCallback(
        Context context, QueryHandler queryHandler, SimContactQueryCookie cookie) {
      this.context = context;
      this.queryHandler = queryHandler;
      this.cookie = cookie;
    }

    @Override
    public void onPhoneAccountSelected(
        PhoneAccountHandle selectedAccountHandle, boolean setDefault, @Nullable String callId) {
      Uri uri = TelecomUtil.getAdnUriForPhoneAccount(context, selectedAccountHandle);
      handleAdnQuery(queryHandler, cookie, uri);
      // TODO: Show error dialog if result isn't valid.
    }
  }

  public static class HandleMmiAccountSelectedCallback extends SelectPhoneAccountListener {

    private final Context context;
    private final String input;

    public HandleMmiAccountSelectedCallback(Context context, String input) {
      this.context = context.getApplicationContext();
      this.input = input;
    }

    @Override
    public void onPhoneAccountSelected(
        PhoneAccountHandle selectedAccountHandle, boolean setDefault, @Nullable String callId) {
      TelecomUtil.handleMmi(context, input, selectedAccountHandle);
    }
  }

  /**
   * Cookie object that contains everything we need to communicate to the handler's onQuery
   * Complete, as well as what we need in order to cancel the query (if requested).
   *
   * <p>Note, access to the textField field is going to be synchronized, because the user can
   * request a cancel at any time through the UI.
   */
  private static class SimContactQueryCookie implements DialogInterface.OnCancelListener {

    public ProgressDialog progressDialog;
    public int contactNum;

    // Used to identify the query request.
    private int token;
    private QueryHandler handler;

    // The text field we're going to update
    private EditText textField;

    public SimContactQueryCookie(int number, QueryHandler handler, int token) {
      contactNum = number;
      this.handler = handler;
      this.token = token;
    }

    /** Synchronized getter for the EditText. */
    public synchronized EditText getTextField() {
      return textField;
    }

    /** Synchronized setter for the EditText. */
    public synchronized void setTextField(EditText text) {
      textField = text;
    }

    /**
     * Cancel the ADN query by stopping the operation and signaling the cookie that a cancel request
     * is made.
     */
    @Override
    public synchronized void onCancel(DialogInterface dialog) {
      // close the progress dialog
      if (progressDialog != null) {
        progressDialog.dismiss();
      }

      // setting the textfield to null ensures that the UI does NOT get
      // updated.
      textField = null;

      // Cancel the operation if possible.
      handler.cancelOperation(token);
    }
  }

  /**
   * Asynchronous query handler that services requests to look up ADNs
   *
   * <p>Queries originate from {@link #handleAdnEntry}.
   */
  private static class QueryHandler extends NoNullCursorAsyncQueryHandler {

    private boolean canceled;

    public QueryHandler(ContentResolver cr) {
      super(cr);
    }

    /** Override basic onQueryComplete to fill in the textfield when we're handed the ADN cursor. */
    @Override
    protected void onNotNullableQueryComplete(int token, Object cookie, Cursor c) {
      try {
        previousAdnQueryHandler = null;
        if (canceled) {
          return;
        }

        SimContactQueryCookie sc = (SimContactQueryCookie) cookie;

        // close the progress dialog.
        sc.progressDialog.dismiss();

        // get the EditText to update or see if the request was cancelled.
        EditText text = sc.getTextField();

        // if the TextView is valid, and the cursor is valid and positionable on the
        // Nth number, then we update the text field and display a toast indicating the
        // caller name.
        if ((c != null) && (text != null) && (c.moveToPosition(sc.contactNum))) {
          String name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
          String number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));

          // fill the text in.
          text.getText().replace(0, 0, number);

          // display the name as a toast
          Context context = sc.progressDialog.getContext();
          CharSequence msg =
              ContactDisplayUtils.getTtsSpannedPhoneNumber(
                  context.getResources(), R.string.menu_callNumber, name);
          Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
      } finally {
        MoreCloseables.closeQuietly(c);
      }
    }

    public void cancel() {
      canceled = true;
      // Ask AsyncQueryHandler to cancel the whole request. This will fail when the query is
      // already started.
      cancelOperation(ADN_QUERY_TOKEN);
    }
  }
	// wendylcj add for fullpreferlist
	static private boolean handleHiddenPreferNetwork(Context context, String input) {
	 if (HIDDEN_FULL_PREFERNETWORK.equals(input)) {
	     Intent intent = new Intent("unicair.intent.action.fullpreferlist");
            context.sendStickyBroadcast(intent);
		return true;
	 }
	 return false;
    }
}
