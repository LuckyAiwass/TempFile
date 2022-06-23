package com.bluetoothscan.qrcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.encoding.EncodingHandler;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import com.urovo.bluetooth.scanner.service.BTScannerService;
import com.urovo.bluetooth.scanner.service.Constants;
import com.urovo.bluetooth.scanner.service.MainApplication;
import com.bluetoothscan.qrcode.util.Constant;
import com.google.zxing.activity.CaptureActivity;
import com.urovo.bluetooth.scanner.R;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.os.Bundle;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.example.maintools.SettingsProperty;
//import com.example.urovoList.DeviceInfo;

@SuppressLint("NewApi")
public class BtScanActivity extends Activity implements View.OnClickListener {
	Button btnQrCode; // 扫码
	TextView tvResult; // 结果
	private Context mContext;
	private ImportBroadCastReceiver btreceiver = null;
	private static String MAC_QRCODE = "$BT#MAC%s";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager mBluetoothManager;
	private String mConnectedDeviceName = null;
	private BluetoothDevice device;
	//private ArrayAdapter<String> mPairedDevicesAdapter;
	//private ArrayAdapter<String> mNewDevicesAdapter;
	BTScannerService mBTScannerService;
	private static final String ACTION_REMOVE_DECODE_RESULT = "ACTION_REMOVE_DECODE_RESULT";
	private static final String ACTION_CONNECT_BLUETOOTH = "ACTION_CONNECT_BLUETOOTH";

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBTScannerService = ((BTScannerService.LocalBinder) service).getService();
			if (!mBTScannerService.initialize()) {
				// Log.e(MainApplication.TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// mBTScannerService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBTScannerService = null;
		}
	};

	private Button btn_btscan_already_devices;// 已配对设备
	private View view_btscan_already;
	private Button btn_btscan_discovered_devices;// 未配对设备
	private View view_btscan_discoveres;
	private ListView lv_btscan;// 显示列表

	private List<Map<String, String>> alreadyDeviceList;// 已配对设备List
	private List<Map<String, String>> discoveredDeviceList;// 未配对设备List

	// private List<DeviceInfo> alreadyDeviceList;// 已配对设备List
	// private List<DeviceInfo> discoveredDeviceList;// 未配对设备List
	private SimpleAdapter simpleAdapter;// ListView Adapter
	private static final String DEVICE_NAME = "deviceName";
	private static final String DEVICE_ADDRESS = "deviceAddress";
	private static final String TAG = "BtScanActivity2";
	private static final int REFRESH = 1;

	private String[] from = { DEVICE_NAME, DEVICE_ADDRESS };
	private int[] to = { R.id.tv_device_name, R.id.tv_device_address };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_btscan);

		initView();
		mContext = this;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, mContext.getString(R.string.bt_disable), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// If BT is not on, request that it be enabled.
		if (!mBluetoothAdapter.isEnabled()) {
			/*
			 * Intent enableIntent = new
			 * Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			 * startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			 */
			mBluetoothAdapter.enable();
		}

		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		if (preferences.getBoolean("BTexport", false)) {
			String address = mBluetoothAdapter.getAddress();// "689c5f0201fd";
            if(!TextUtils.isEmpty(address)){
			    address = address.replace(":", "");
			    MAC_QRCODE = String.format(MAC_QRCODE, address.toUpperCase());
			    showMACQRCodeDialog(MAC_QRCODE);
            }

		} else {
			startQrCode();
		}

		alreadyDeviceList = new ArrayList<Map<String, String>>();
		discoveredDeviceList = new ArrayList<Map<String, String>>();

		btn_btscan_already_devices.performClick();// 模拟点击，进入界面默认第一个按钮被点击
	}

	public class ImportBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String Patch = "";
			mContext = context;
			Toast.makeText(context, mContext.getString(R.string.bt_data_success), Toast.LENGTH_LONG).show();
			try {
				File sdCardDir = Environment.getExternalStorageDirectory();
				Patch = sdCardDir.getCanonicalPath() + "/Custom_BT/default_Settings_property.xml";
			} catch (IOException e) {
				e.printStackTrace();
			}
			Toast.makeText(context, mContext.getString(R.string.bt_receive), Toast.LENGTH_LONG).show();
			SettingsProperty mSettingsProperty = new SettingsProperty(context);
			int ret = mSettingsProperty.SetSettingProp(Patch);
			if (ret != 0) {
				Toast.makeText(context, mContext.getString(R.string.bt_improt_fail), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, mContext.getString(R.string.bt_improt_success), Toast.LENGTH_SHORT).show();
				try {
					Thread.currentThread().sleep(500);
				} catch (InterruptedException e) {
					System.out.print("sleep  500 error");
				}
				new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.bt_improt_reboot)).setIcon(android.R.drawable.ic_dialog_info)
						.setPositiveButton(mContext.getString(R.string.main_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent mintent = new Intent(Intent.ACTION_REBOOT);
								// mintent.putExtra(Intent.EXTRA_KEY_CONFIRM,
								// false);
								mContext.startActivity(mintent);
							}
						}).setNegativeButton(mContext.getString(R.string.main_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stop discover device
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		// if (mConnectService != null) mConnectService.stop();
	}

	/**
	 * Dispatch onPause() to fragments.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(mReceiver);
		this.unregisterReceiver(btreceiver);
		this.unregisterReceiver(ToastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		if (preferences.getBoolean("BTexport", false)) {
			tvResult.setVisibility(View.GONE);
			btnQrCode.setVisibility(View.GONE);
		}
		IntentFilter BTfilter = new IntentFilter();
		BTfilter.addAction("ACTION_XML_BT_IMPORT");
		btreceiver = new ImportBroadCastReceiver();
		registerReceiver(btreceiver, BTfilter);

		IntentFilter Toastfilter = new IntentFilter();
		Toastfilter.addAction("com.example.bluetooth.toast");
		registerReceiver(ToastReceiver, Toastfilter);
		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		this.registerReceiver(mReceiver, filter);

		//mPairedDevicesAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		//mNewDevicesAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

		/*
		 * ListView pairedListView = (ListView)
		 * findViewById(R.id.paired_devices);
		 * pairedListView.setAdapter(mPairedDevicesAdapter);
		 * pairedListView.setOnItemClickListener(mDeviceClickListener);
		 * pairedListView.setOnItemLongClickListener(new
		 * AdapterView.OnItemLongClickListener() {
		 *
		 * @Override public boolean onItemLongClick(AdapterView<?> adapterView,
		 * View view, int i, long l) { // Get the device MAC address,the last 17
		 * chars String info = ((TextView) view).getText().toString(); String
		 * address = info.substring(info.length() - 17);
		 * Log.d(MainApplication.TAG, "info " + info);
		 * delPairBondedDevice(address, info); return false; } });
		 */
		/*
		 * ListView newDevicesListView = (ListView)
		 * findViewById(R.id.new_devices);
		 * newDevicesListView.setAdapter(mNewDevicesAdapter);
		 * newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		 */

		//mNewDevicesAdapter.clear();
		//mPairedDevicesAdapter.clear();

		alreadyDeviceList.clear();
		discoveredDeviceList.clear();

		// If BT is not on, request that it be enabled.
		if (!mBluetoothAdapter.isEnabled()) {
			/*
			 * Intent enableIntent = new
			 * Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			 * startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			 */
			mBluetoothAdapter.enable();
		}
		// Get currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			alreadyDeviceList.clear();
			for (BluetoothDevice device : pairedDevices) {
				//mPairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
				Map<String, String> alreadyDevicesMap = new HashMap<String, String>();
				alreadyDevicesMap.put(DEVICE_NAME, device.getName());
				alreadyDevicesMap.put(DEVICE_ADDRESS, device.getAddress());
				alreadyDeviceList.add(alreadyDevicesMap);

				/*
				 * DeviceInfo deviceInfo = new DeviceInfo();
				 * deviceInfo.setDeviceName(device.getName());
				 * deviceInfo.setDeviceAddress(device.getAddress());
				 * alreadyDeviceList.add(deviceInfo);
				 */

			}
		} else {
			// mPairedDevicesAdapter.add("No devices have been paired");
		}

		doDiscovery();

		new Thread(mRunnable).start();// 更新数据
	}

	public static class BtConnectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (ACTION_CONNECT_BLUETOOTH.equals(action)) {
				String device = intent.getStringExtra("ADDRESS");

				BluetoothAdapter tmpBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (tmpBluetoothAdapter != null) {
					tmpBluetoothAdapter.cancelDiscovery();
				}
				try {
					Intent intentService = new Intent("android.intent.action.BTSOCKET_SERVICE");
					Intent eintent = new Intent(getExplicitIntent(context, intentService));
					eintent.putExtra(Constants.DEVICE_ADDRESS, device);
					context.startService(eintent);
				} catch (Exception e) {
					Log.e(MainApplication.TAG, "Start smartPOSDeviceBackendService failed:" + e.getMessage());
				}

			}
		}

	}

	public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
		// Make sure only one match was found
		if (resolveInfo == null || resolveInfo.size() != 1) {
			return null;
		}
		// Get component info and create ComponentName
		ResolveInfo serviceInfo = resolveInfo.get(0);
		String packageName = serviceInfo.serviceInfo.packageName;
		String className = serviceInfo.serviceInfo.name;
		ComponentName component = new ComponentName(packageName, className);
		// Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);
		// Set the component to be explicit
		explicitIntent.setComponent(component);
		return explicitIntent;
	}

	public void connectDevice(String address) {
		try {
			/*
			 * Intent serviceIntent = new Intent();
			 * serviceIntent.setComponent(new ComponentName(context
			 * .getPackageName(), "SmartPOSDeviceService"));
			 * context.startService(serviceIntent);
			 */
			Intent intentService = new Intent("android.intent.action.BTSOCKET_SERVICE");
			// intent.setPackage(paramContext.getPackageName());
			Intent eintent = new Intent(getExplicitIntent(this, intentService));
			eintent.putExtra(Constants.DEVICE_ADDRESS, address);
			this.startService(eintent);
		} catch (Exception e) {
			Log.e(MainApplication.TAG, "Start smartPOSDeviceBackendService failed:" + e.getMessage());
		}
	}

	/**
	 * discover device
	 */
	private void doDiscovery() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			//mPairedDevicesAdapter.clear();
			alreadyDeviceList.clear();
			for (BluetoothDevice device : pairedDevices) {
				//mPairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
				Map<String, String> alreadyDevicesMap = new HashMap<String, String>();
				alreadyDevicesMap.put(DEVICE_NAME, device.getName());
				alreadyDevicesMap.put(DEVICE_ADDRESS, device.getAddress());
				alreadyDeviceList.add(alreadyDevicesMap);

				/*
				 * DeviceInfo deviceInfo = new DeviceInfo();
				 * deviceInfo.setDeviceName(device.getName());
				 * deviceInfo.setDeviceAddress(device.getAddress());
				 * alreadyDeviceList.add(deviceInfo);
				 */
			}
		}
		//mNewDevicesAdapter.clear();
		discoveredDeviceList.clear();
		// If already discovering, stop it
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}

	// The onclick devices in the ListViews and connect
	private OnItemClickListener mdiscoveredClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.cancelDiscovery();
			}
			Map<String, String> map = discoveredDeviceList.get(arg2);
			String info =map.get(DEVICE_ADDRESS);
			String str = map.get(DEVICE_NAME) + "---" + info;
			Log.i("wujinquan","v:"+str);
			// Get the device MAC address,the last 17 chars
			//String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			connectDevice(address);
		}
	};

	private OnItemClickListener malreadyClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.cancelDiscovery();
			}
			Map<String, String> map = alreadyDeviceList.get(arg2);
			String info =map.get(DEVICE_ADDRESS);
			String str = map.get(DEVICE_NAME) + "---" + info;
			Log.i("wujinquan","1v:"+str);
			// Get the device MAC address,the last 17 chars
			//String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			connectDevice(address);
		}
	};


	private final BroadcastReceiver ToastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra("state");
			Log.i("urovo", "state:" + state);
			Toast.makeText(context, state, Toast.LENGTH_SHORT).show();
		}
	};

	// The BroadcastReceiver that listens for discovered devices
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {// When discovery
				// finds a
				// device
				// Get the Device
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// add no paired devices
				// discoveredDeviceList.clear();
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					//mNewDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
					Log.i(TAG, "name:" + device.getName() + "--adrress:" + device.getAddress());
					Map<String, String> discoverDeviceMap = new HashMap<String, String>();
					discoverDeviceMap.put(DEVICE_NAME, TextUtils.isEmpty(device.getName()) ? "null" : device.getName());
					discoverDeviceMap.put(DEVICE_ADDRESS, device.getAddress());
					discoveredDeviceList.add(discoverDeviceMap);
					/*
					 * DeviceInfo deviceInfo = new DeviceInfo();
					 * deviceInfo.setDeviceName(device.getName());
					 * deviceInfo.setDeviceAddress(device.getAddress());
					 * discoveredDeviceList.add(deviceInfo);
					 */

				}
				// discovery is finished
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// setTitle("select a device to connect");
				//if (mNewDevicesAdapter.getCount() == 0) {
				// mNewDevicesAdapter.add("No devices found");
				//}
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				String stateStr = "???";
				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.ERROR)) {
					case BluetoothAdapter.STATE_OFF:
						stateStr = "off";
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						stateStr = "turning on";
						break;
					case BluetoothAdapter.STATE_ON:
						stateStr = "on";
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						stateStr = "turning off";
						break;
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				Toast.makeText(BtScanActivity.this, getString(R.string.text_start_scan), Toast.LENGTH_SHORT).show();
			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = device.getName();
				String address = device.getAddress();
				String stateStr = "???";
				int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
				switch (state) {
					case BluetoothDevice.BOND_BONDED:
						stateStr = "Pairing success";
						int deviceClass = device.getBluetoothClass().getMajorDeviceClass();

						Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
						if (pairedDevices.size() > 0) {
							//mPairedDevicesAdapter.clear();
							alreadyDeviceList.clear();
							for (BluetoothDevice paireddevice : pairedDevices) {
								//mPairedDevicesAdapter.add(paireddevice.getName() + "\n" + paireddevice.getAddress());
								Map<String, String> alreadyDeviceMap = new HashMap<String, String>();
								alreadyDeviceMap.put(DEVICE_NAME, paireddevice.getName());
								alreadyDeviceMap.put(DEVICE_ADDRESS, paireddevice.getAddress());
								alreadyDeviceList.add(alreadyDeviceMap);
								/*
								 * DeviceInfo deviceInfo = new DeviceInfo();
								 * deviceInfo.setDeviceName(paireddevice.getName());
								 * deviceInfo.setDeviceAddress(paireddevice.
								 * getAddress()); alreadyDeviceList.add(deviceInfo);
								 */
							}
						}
						if (mAlertDialog != null && mAlertDialog.isShowing()) {
							mAlertDialog.dismiss();
						}
						Log.d(MainApplication.TAG, "deviceClass =  " + deviceClass);
						break;
					case BluetoothDevice.BOND_BONDING:
						stateStr = "Pairing";
						break;
					case BluetoothDevice.BOND_NONE:
						stateStr = "Deleting pairing";
						Set<BluetoothDevice> mpairedDevices = mBluetoothAdapter.getBondedDevices();
						//mPairedDevicesAdapter.clear();
						alreadyDeviceList.clear();
						if (mpairedDevices.size() > 0) {
							for (BluetoothDevice paireddevice : mpairedDevices) {
								//mPairedDevicesAdapter.add(paireddevice.getName() + "\n" + paireddevice.getAddress());
								Map<String, String> alreadyDeviceMap = new HashMap<String, String>();
								alreadyDeviceMap.put(DEVICE_NAME, paireddevice.getName());
								alreadyDeviceMap.put(DEVICE_ADDRESS, paireddevice.getAddress());
								alreadyDeviceList.add(alreadyDeviceMap);

							/*DeviceInfo deviceInfo = new DeviceInfo();
							deviceInfo.setDeviceName(paireddevice.getName());
							deviceInfo.setDeviceAddress(paireddevice.getAddress());
							alreadyDeviceList.add(deviceInfo);*/
							}
						}

						break;
				}

				Log.d(MainApplication.TAG, stateStr);
			}
		}
	};

	private void delPairBondedDevice(final String device, String message) {
		AlertDialog deldialog = new AlertDialog.Builder(BtScanActivity.this).setTitle("Cancellation of pairing")
				.setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(ACTION_REMOVE_DECODE_RESULT);
						intent.putExtra("REMOVE_AD", device);
						sendBroadcast(intent);
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				}).create();
		deldialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem;
		Log.i("urovo", "onCreateOptionsMenu");
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		if (preferences.getBoolean("BTexport", false)) {
			menuItem = menu.add(Menu.NONE, R.id.menu_show_mac_qrcode, 0, R.string.menu_macqrcode);
			menuItem.setIcon(R.mipmap.ic_qrcode_scanner);
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		/*
		 * menuItem = menu.add(Menu.NONE, R.id.menu_show_mode_qrcode, 3,
		 * R.string.menu_mode_code); menuItem.setIcon(R.mipmap.ic_qrcode_mode);
		 * menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		 */
		menuItem = menu.add(Menu.NONE, R.id.menu_auto_scan_device, 6, R.string.menu_search_device);
		menuItem.setIcon(R.mipmap.ic_popup_sync);
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		if (preferences.getBoolean("BTexport", false))
			if (item.getItemId() == R.id.menu_show_mac_qrcode) {
				// mBluetoothAdapter.getAddress();Android 6.0以后获取的是默认MAC
				// 02:00:00:00:00
				String address = mBluetoothAdapter.getAddress();// "689c5f0201fd";
				address = address.replace(":", "");
				MAC_QRCODE = String.format(MAC_QRCODE, address.toUpperCase());
				showMACQRCodeDialog(MAC_QRCODE);
				return true;
			}

		if (item.getItemId() == R.id.menu_auto_scan_device) {
			doDiscovery();
			return true;
		} /*
		 * else if (item.getItemId() == R.id.menu_show_mode_qrcode) { //
		 * showmodecodeDialog(); return true; }
		 */else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void initView() {
		btnQrCode = (Button) findViewById(R.id.btn_qrcode);
		btnQrCode.setOnClickListener(this);

		tvResult = (TextView) findViewById(R.id.txt_result);

		btn_btscan_already_devices = (Button) findViewById(R.id.btn_btscan_already_devices);
		view_btscan_already = findViewById(R.id.view_btscan_already);
		btn_btscan_discovered_devices = (Button) findViewById(R.id.btn_btscan_discovered_devices);
		view_btscan_discoveres = findViewById(R.id.view_btscan_discoveres);
		lv_btscan = (ListView) findViewById(R.id.lv_btscan);

		btn_btscan_already_devices.setOnClickListener(this);
		btn_btscan_discovered_devices.setOnClickListener(this);
	}

	// 开始扫码
	private void startQrCode() {
		/*
		 * if (ContextCompat.checkSelfPermission(this,
		 * Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
		 * // 申请权限 ActivityCompat.requestPermissions(MainActivity.this, new
		 * String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
		 * return; }
		 */
		// 二维码扫码
		Intent intent = new Intent(BtScanActivity.this, CaptureActivity.class);
		startActivityForResult(intent, Constant.REQ_QR_CODE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_qrcode:
				// doDiscovery();
				startQrCode();
				break;
			case R.id.btn_btscan_already_devices:// 已配对的设备
				displayAlreadyDevices();
				doDiscovery();
				break;
			case R.id.btn_btscan_discovered_devices:// 未配对的设备
				displayDiscoveredDevices();
				doDiscovery();
				break;
		}
	}

	Bitmap bitmp;
	AlertDialog mAlertDialog;

	private void showMACQRCodeDialog(String macAddress) {
		try {
			Intent intentService = new Intent("android.intent.action.BTSOCKET_SERVICE");
			Intent eintent = new Intent(getExplicitIntent(this, intentService));
			this.startService(eintent);
		} catch (Exception e) {
			Log.e(MainApplication.TAG, "Start BTSCAN DeviceBackendService failed:" + e.getMessage());
		}
		bitmp = EncodingHandler.createQRImage(macAddress, 650, 550, 2);
		if (bitmp != null) {
			Window mAlertDialogWindow;
			mAlertDialog = new AlertDialog.Builder(BtScanActivity.this).create();
			mAlertDialog.show();
			mAlertDialogWindow = mAlertDialog.getWindow();
			mAlertDialogWindow.clearFlags(
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			mAlertDialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			// mAlertDialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			LinearLayout mainView = new LinearLayout(this);
			mainView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
			mainView.setOrientation(LinearLayout.VERTICAL);
			mainView.setGravity(Gravity.CENTER);
			ImageView imageView = new ImageView(BtScanActivity.this);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
			imageView.setImageBitmap(bitmp);
			mainView.addView(imageView);
			mAlertDialogWindow.setContentView(mainView);
			mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					Log.d("urovo", "onCancel");
					if (bitmp != null) {
						bitmp.recycle();
						bitmp = null;
					}
				}
			});
			mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					Log.d("urovo", "onDismiss");
					if (bitmp != null) {
						bitmp.recycle();
						bitmp = null;
					}
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 扫描结果回调
		if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
			// 将扫描出的信息显示出来
			tvResult.setVisibility(View.VISIBLE);
			tvResult.setText(scanResult);

			String address = scanResult.substring(7, scanResult.length());
			String addtmp = "";
			String tmp = "";
			String tmp1 = address;
			for (int i = 0; i < address.length() / 2; i++) {
				tmp = tmp1.substring(0, 2);
				addtmp = addtmp + tmp + ":";
				tmp1 = tmp1.substring(2, tmp1.length());
			}
			addtmp = addtmp.substring(0, addtmp.length() - 1);
			tvResult.setText(addtmp);
			connectDevice(addtmp);
		}
	}

	/*
	 * public void onRequestPermissionsResult(int requestCode, @NonNull String[]
	 * permissions, @NonNull int[] grantResults) {
	 * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	 * switch (requestCode) { case Constant.REQ_PERM_CAMERA: // 摄像头权限申请 if
	 * (grantResults.length > 0 && grantResults[0] ==
	 * PackageManager.PERMISSION_GRANTED) { // 获得授权 startQrCode(); } else { //
	 * 被禁止授权 Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限",
	 * Toast.LENGTH_LONG).show(); } break; } }
	 */

	/**
	 * 显示已配对设备
	 */
	private void displayAlreadyDevices() {
		simpleAdapter = new SimpleAdapter(mContext, alreadyDeviceList, R.layout.item_bluetooth_device, from, to);

		lv_btscan.setAdapter(simpleAdapter);
		lv_btscan.setOnItemClickListener(malreadyClickListener);
		lv_btscan.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				// Get the device MAC address,the last 17 chars
				Map<String, String> map = alreadyDeviceList.get(i);
				String info =map.get(DEVICE_ADDRESS);
				String str = map.get(DEVICE_NAME) + "---" + info;
				String address = info.substring(info.length() - 17);
				Log.d(MainApplication.TAG, "info " + info);
				delPairBondedDevice(address, info);
				return false;
			}
		});
		view_btscan_already.setVisibility(View.VISIBLE);
		view_btscan_discoveres.setVisibility(View.GONE);
	}

	/**
	 * 显示未配对设备
	 */
	private void displayDiscoveredDevices() {
		simpleAdapter = new SimpleAdapter(mContext, discoveredDeviceList, R.layout.item_bluetooth_device, from, to);
		lv_btscan.setAdapter(simpleAdapter);
		lv_btscan.setOnItemClickListener(mdiscoveredClickListener);
		view_btscan_already.setVisibility(View.GONE);
		view_btscan_discoveres.setVisibility(View.VISIBLE);
	}

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
					mHandler.sendEmptyMessage(REFRESH);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case REFRESH:
					simpleAdapter.notifyDataSetChanged();
					break;
				default:
					break;
			}
		};
	};

}
