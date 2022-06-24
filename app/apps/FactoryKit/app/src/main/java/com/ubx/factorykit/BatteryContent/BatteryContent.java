package com.ubx.factorykit.BatteryContent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import android.util.Log;

import java.io.ByteArrayOutputStream;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.IOException;  
import java.io.InputStream; 
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import android.os.Handler;
import android.widget.Toast;

import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ53H;

public class BatteryContent extends Activity {
	private TextView chargesate;
	private TextView currentpower;
	private TextView batterypower;
	private TextView powerstate;
	private TextView powertemperature;
	private TextView batterytype;
	private TextView batteryvoltage;
	private TextView batteryElectric;
	private TextView battery_sn;
	private TextView baterry_health;
	private TextView charge_count;
	private TextView battery_id;
	Button passButton, failButton;
	private Handler mHandler;
	final private static int UPDATE_INTERVAL = 100;
	private boolean mStop = false;
	private static final String BATTERY_ELECTRONIC = "/sys/class/power_supply/bms/current_now";
	private static final String BATTERY_V = "/sys/class/power_supply/bms/voltage_ocv";
	static String TAG = "BatteryContent";
	private static final String BATTERY_DESIGN_POWER = "/sys/class/power_supply/battery/charge_full_design";
	private static final String BATTERY_CHARGE_COUNT = "/sys/class/power_supply/battery/cycle_count";
	private static final String BATTERY_SN = "/sys/class/power_supply/battery/serial_number";
	private static final String BATTERY_HEALTH = "/sys/class/power_supply/battery/battery_soh";
	private static final String BATTERY_TYPE_NODE =SystemProperties.get("persist.sys.battery.type",PRODUCT_SQ53H ? "/sys/class/power_supply/battery/uevent" :  "/sys/class/power_supply/bms/battery_type");
	private static final String BATTERY_ID_NODE = SystemProperties.get("persist.sys.battery.id","/sys/class/power_supply/bms/resistance_id");
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.battery_test);
		bindView();
		chargesate = (TextView) findViewById(R.id.chargesate);
		currentpower = (TextView) findViewById(R.id.currentpower);
		batterypower = (TextView) findViewById(R.id.batterypower);
		charge_count = (TextView) findViewById(R.id.charge_count);
		battery_sn = (TextView) findViewById(R.id.battery_sn);
		baterry_health = (TextView) findViewById(R.id.baterry_health);
		battery_id = (TextView) findViewById(R.id.baterry_id);
		batterytype = (TextView) findViewById(R.id.baterry_type);
		mHandler = new Handler(getMainLooper());

	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();

		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mBroadcastReceiver);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra("status", 0);
				int health = intent.getIntExtra("health", 0);
				boolean present = intent.getBooleanExtra("present", false);
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 0);
				int icon_small = intent.getIntExtra("icon-small", 0);
				int plugged = intent.getIntExtra("plugged", 0);
				int voltage = intent.getIntExtra("voltage", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				String technology = intent.getStringExtra("technology");
				String statusString = "";

				switch (status) {
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					statusString = "unknown";
					break;
				case BatteryManager.BATTERY_STATUS_CHARGING:
					statusString = "charging";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					statusString = "discharging";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					statusString = "not charging";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					statusString = "full";
					break;
				}
				String healthString = "";

				switch (health) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					healthString = "unknown";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					healthString = "good";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					healthString = "overheat";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					healthString = "dead";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					healthString = "voltage";
					break;
				case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
					healthString = "unspecified failure";
					break;
				}
				String acString = "";

				switch (plugged) {
				case BatteryManager.BATTERY_PLUGGED_AC:
					acString = "plugged ac";
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					acString = "plugged usb";
					break;
				default:
					acString = "Not plugged";
					break;
				}
				
				chargesate.setText( statusString);
				currentpower.setText( String.valueOf(level));
				try {
					batterypower.setText(readCurrentFile(BATTERY_DESIGN_POWER));
					charge_count.setText(readCurrentFile(BATTERY_CHARGE_COUNT));
					battery_sn.setText(readCurrentFile(BATTERY_SN));
					baterry_health.setText(readCurrentFile(BATTERY_HEALTH));
					battery_id.setText(readCurrentFile(BATTERY_ID_NODE));
					batterytype.setText(readCurrentFile(BATTERY_TYPE_NODE));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!statusString.equals("charging")) {
						mHandler.removeCallbacks(mUpdateRunner);
						mStop = true;
					} else {
						//Log.d("zml   ","     mBatteryCurrentSupport");
						mHandler.postDelayed(mUpdateRunner, UPDATE_INTERVAL);
						mStop = false;
					}
				
			}
		}
	};
	
	private Runnable mUpdateRunner = new Runnable() {

		@Override
		public void run() {
			try {
				FileReader fr = new FileReader(BATTERY_ELECTRONIC);
				BufferedReader br = new BufferedReader(fr);
				int current = Math.round(getMeanCurrentVal(BATTERY_ELECTRONIC, 5, 0) / 1000.0f);//this data is uA , 1mA=1000uA
				br.close();
				fr.close();
				//if(current<0)
				//batteryElectric.setText(getString(R.string.batteryElectric) + ": " + (-current) + "mA");
				//else
				//batteryElectric.setText(getString(R.string.batteryElectric) + ": " + current + "mA");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}

			if (!mStop)
				mHandler.postDelayed(mUpdateRunner, UPDATE_INTERVAL);

		}
	};
	public String readCurrentFile(String filePath) throws IOException {
		 if(TextUtils.isEmpty(filePath))
		 	return "null";
		 File file = new File(filePath);
         InputStream input = new FileInputStream(file);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            input.close();
        }
  }

	private float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {  
        float meanVal = 0.0f;  
        if (totalCount <= 0) {  
            return 0.0f;  
        }  
        for (int i = 0; i < totalCount; i++) {  
            try {  
                float f = Float.valueOf(readFile(filePath, 0));  
                meanVal += f / totalCount;  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            if (intervalMs <= 0) {  
                continue;  
            }  
            try {  
                Thread.sleep(intervalMs);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return meanVal;  
    }  
  
    private int readFile(String path, int defaultValue) {  
        try {  
            BufferedReader bufferedReader = new BufferedReader(new FileReader(  
                    path));  
            int i = Integer.parseInt(bufferedReader.readLine(), 10);  
            bufferedReader.close();  
            return i;  
        } catch (Exception localException) {  
        }  
        return defaultValue;  
    }
	@Override
	public void finish() {
		super.finish();
	}

	void bindView() {
		passButton = (Button) findViewById(R.id.pass);
		failButton = (Button) findViewById(R.id.fail);
		passButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				pass();
			}
		});

		failButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				fail(null);
			}
		});
	}

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		Utilities.writeCurMessage(this, TAG, "Failed");
		finish();
	}

	void pass() {
		setResult(RESULT_OK);
		Utilities.writeCurMessage(this, TAG, "Pass");
		finish();
	}

	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
