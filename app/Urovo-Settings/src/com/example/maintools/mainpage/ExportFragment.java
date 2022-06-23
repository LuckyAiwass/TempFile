package com.example.maintools.mainpage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.example.maintools.SettingsProperty;
import com.example.maintools.WifiInfo;
import com.example.saxparsexml.Packagelist;
import com.example.saxparsexml.PropertySax;
import com.example.saxparsexml.Propertylist;
import com.example.saxparsexml.Tablelist;
import com.example.urovoList.Globallist;
import com.example.urovoList.Scanlist;
import com.example.urovoList.Securelist;
import com.example.urovoList.Systemlist;
import com.urovo.bluetooth.scanner.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("NewApi")
public class ExportFragment extends BaseFragment implements OnClickListener{

	private ListView lv_check_list;
	private LinearLayout layout;

	String  TAG="urovo";

	private ArrayList<Systemlist> newSystemlists;
	private ArrayList<Globallist> newGloballists;
	private ArrayList<Securelist> newSecurelists;
	private ArrayList<Scanlist> newScanlists;


	private ArrayList<Systemlist> oldSystemlists;
	private ArrayList<Globallist> oldGloballists;
	private ArrayList<Securelist> oldSecurelists;
	private ArrayList<Scanlist> oldScanlists;

	private ArrayList<Systemlist> tmpSystemlists;
	private ArrayList<Globallist> tmpGloballists;
	private ArrayList<Securelist> tmpSecurelists;
	private ArrayList<Scanlist> tmpScanlists;


	private ArrayList<Systemlist> setSystemlists;
	private ArrayList<Globallist> setGloballists;
	private ArrayList<Securelist> setSecurelists;
	private ArrayList<Scanlist> setScanlists;


	Uri systemuri = Settings.System.CONTENT_URI;
	Uri Globauri = Settings.Global.CONTENT_URI;
	Uri Secureuri = Settings.Secure.CONTENT_URI;
	Uri Scanuri= Uri.parse("content://com.urovo.provider.settings/settings");


	private Context  mContext;
	SettingsProperty  mSettingsProperty;

	@Override
	protected int getContentLayoutRes() {
		return R.layout.fragment_export;
	}

	@Override
	protected void initView(View childView) {
		childView.findViewById(R.id.rl_export_nfc).setOnClickListener(this);
		childView.findViewById(R.id.rl_export_scan).setOnClickListener(this);
		childView.findViewById(R.id.rl_export_local).setOnClickListener(this);
		listview = (ListView) childView.findViewById(R.id.lv_check_list);


		layout = (LinearLayout)childView.findViewById(R.id.relative);
		mContext=this.getActivity();
		SharedPreferences preferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
		SharedPreferences.Editor  editor = preferences.edit();
		editor.putBoolean("BTexport", true);

		editor.putBoolean("BTWrite", true);
		editor.commit();

		//listview = (ListView)childView.findViewById(R.id.list);

//        export   = (Button)findViewById(R.id.export);
//        cancel   = (Button)findViewById(R.id.cancel);
//        mexportnfc= (Button)findViewById(R.id.exportnfc);
//        mexportbt=(Button)findViewById(R.id.exportbt);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			reinitData();
		} else {
			//相当于Fragment的onPause
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		reinitData();
	}
	public void reinitData() {



		setSystemlists= new ArrayList<Systemlist>();
		setGloballists= new ArrayList<Globallist>();
		setSecurelists= new ArrayList<Securelist>();
		setScanlists= new ArrayList<Scanlist>();

		oldSystemlists= new ArrayList<Systemlist>();
		oldGloballists= new ArrayList<Globallist>();
		oldSecurelists= new ArrayList<Securelist>();
		oldScanlists= new ArrayList<Scanlist>();

		newSystemlists= new ArrayList<Systemlist>();
		newGloballists= new ArrayList<Globallist>();
		newSecurelists= new ArrayList<Securelist>();
		newScanlists= new ArrayList<Scanlist>();

		tmpSystemlists= new ArrayList<Systemlist>();
		tmpGloballists= new ArrayList<Globallist>();
		tmpSecurelists= new ArrayList<Securelist>();
		tmpScanlists= new ArrayList<Scanlist>();

		mSettingsProperty=new SettingsProperty(mContext);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 获取SD卡的目录
			File sdCardDir = Environment.getExternalStorageDirectory();
			try {
				int a=getoldSettingProp((sdCardDir.getCanonicalPath() +"/Custom_default/"+"default_Settings_property.xml"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getnewSettingProp();
		gettmpSettingProp();
	}

	@Override
	protected void initData() {



		setSystemlists= new ArrayList<Systemlist>();
		setGloballists= new ArrayList<Globallist>();
		setSecurelists= new ArrayList<Securelist>();
		setScanlists= new ArrayList<Scanlist>();

		oldSystemlists= new ArrayList<Systemlist>();
		oldGloballists= new ArrayList<Globallist>();
		oldSecurelists= new ArrayList<Securelist>();
		oldScanlists= new ArrayList<Scanlist>();

		newSystemlists= new ArrayList<Systemlist>();
		newGloballists= new ArrayList<Globallist>();
		newSecurelists= new ArrayList<Securelist>();
		newScanlists= new ArrayList<Scanlist>();

		tmpSystemlists= new ArrayList<Systemlist>();
		tmpGloballists= new ArrayList<Globallist>();
		tmpSecurelists= new ArrayList<Securelist>();
		tmpScanlists= new ArrayList<Scanlist>();

		mSettingsProperty=new SettingsProperty(mContext);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 获取SD卡的目录
			File sdCardDir = Environment.getExternalStorageDirectory();
			try {
				int a=getoldSettingProp((sdCardDir.getCanonicalPath() +"/Custom_default/"+"default_Settings_property.xml"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getnewSettingProp();
		gettmpSettingProp();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_export_nfc://NFC导出点击事件
				new Thread(){
					public void run(){

						localexport();
					}
				}.start();
				Intent intent =new Intent(mContext,com.example.nfcfile.ExprotNfcActivity.class);
				startActivity(intent);
				break;
			case R.id.rl_export_scan://扫描导出点击事件
				new Thread(){
					public void run(){

						localexport();
					}
				}.start();
				SharedPreferences preferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
				SharedPreferences.Editor  editor = preferences.edit();
				editor.putBoolean("BTexport", true);

				editor.putBoolean("BTWrite", true);
				editor.commit();
				Intent btintent =new Intent(mContext,com.bluetoothscan.qrcode.BtScanActivity.class);

				startActivity(btintent);
				break;
			case R.id.rl_export_local://本地导出点击事件
				new Thread(){
					public void run(){
						localexport();
					}
				}.start();
				break;

		}
	}







	public int getoldSettingProp (String patch){
		String   mPackage,mname,mkey,mvalue;
		oldSecurelists.clear();
		oldSystemlists.clear();
		oldGloballists.clear();
		oldScanlists.clear();
		try {
			ArrayList<Packagelist> aPackagelist=	parseXMLFile(patch);
			if(aPackagelist==null)
				return -1;
			Iterator<Packagelist> iPackagelist = aPackagelist.iterator();
			while(iPackagelist.hasNext()){
				Packagelist mPackagelist=iPackagelist.next();
				mPackage=mPackagelist.getpackageName();
				ArrayList<Tablelist> aTablelist=	mPackagelist.getTablelists();
				Iterator<Tablelist> iTablelist= aTablelist.iterator();
				while(iTablelist.hasNext()){
					Tablelist  mTablelist=iTablelist.next();
					mname=mTablelist.getname();
					ArrayList<Propertylist> aPropertylist=	mTablelist.getpropertys();
					Iterator<Propertylist> iPropertylist= aPropertylist.iterator();
					Propertylist mPropertylist = null;
					while(iPropertylist.hasNext()) {
						mPropertylist=iPropertylist.next();
						mkey=  mPropertylist.getkey();
						mvalue= mPropertylist.getvalue();
						Log.i(TAG,"mPackage :"+ mPackage+"  mname:"+mname+"   mkey:"+mkey+"   mvalue:"+mvalue);
						if(!TextUtils.isEmpty(mPackage) && !TextUtils.isEmpty(mname) && !TextUtils.isEmpty(mkey))
							if(mPackage.equals("com.android.providers.settings")){
								if(mname.equals("Secure")) {
									Securelist  mSecurelist=new Securelist();
									mSecurelist.setkey(mkey);
									if(TextUtils.isEmpty(mvalue)){mvalue = "";}
									mSecurelist.setvalue(mvalue);
									oldSecurelists.add(mSecurelist);
								}else if(mname.equals("System")){
									Systemlist  mSystemlist=new Systemlist();
									mSystemlist.setkey(mkey);
									if(TextUtils.isEmpty(mvalue)){mvalue = "";}
									mSystemlist.setvalue(mvalue);
									oldSystemlists.add(mSystemlist);
								}else if(mname.equals("Global")){
									Globallist  mGloballist=new Globallist();
									mGloballist.setkey(mkey);
									if(TextUtils.isEmpty(mvalue)){mvalue = "";}
									mGloballist.setvalue(mvalue);
									oldGloballists.add(mGloballist);
								}
							}else if(mPackage.equals("com.urovo.provider.settings")){
								if(mname.equals("settings")) {
									Scanlist  mScanlist=new Scanlist();
									mScanlist.setkey(mkey);
									mScanlist.setvalue(mvalue);
									oldScanlists.add(mScanlist);
								}
							}
					}
				}
			}
			return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 2;
		}
	}

	ArrayList<Packagelist> parseXMLFile(String patch) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		InputStream is = new FileInputStream(new File(patch));
		PropertySax handle = new PropertySax();
		saxParser.parse(is, handle);
		is.close();
		return handle.getLessons();
	}

	String SwifiInfo="";
	public void getWifiInfo() {
		FileInputStream inputStream;
		ArrayList<WifiInfo> mWifiInfoList = new ArrayList<WifiInfo>();
		try {
			File file = new File("/data/misc/wifi/WifiConfigStore.xml");
			inputStream = new FileInputStream(file);
			byte temp[] = new byte[1024];
			StringBuilder sb = new StringBuilder("");
			int len = 0;
			WifiInfo wifiInfo;
			String name;
			String  Password;
			while ((len = inputStream.read(temp)) > 0){
				String tmp=new String(temp, 0, len);
				int a= tmp.indexOf("SSID");
				//  Log.i("wujinquan","tmp:"+tmp);
				if(a>-1){
					int b=   tmp.indexOf("&quot;<");
					name=tmp.substring(a+12,b);
					tmp=tmp.substring(b+3);
					int x= tmp.indexOf("PreSharedKey");
					if(x>-1){
						int y=   tmp.indexOf("&quot;<");
						Password=tmp.substring(x+20,y);
						wifiInfo = new WifiInfo();
						wifiInfo.setName(name);
						wifiInfo.setPassword(Password);
						mWifiInfoList.add(wifiInfo);
						Log.i("urovo","wifi密码："+"wifiInfo.getName:"+wifiInfo.getName()+"   wifiInfo.setPassword:"+wifiInfo.getPassword());
					}
				}
			}
			if(!mWifiInfoList.isEmpty()){
				Iterator<WifiInfo> newWifiInfoList = mWifiInfoList.iterator();
				while(newWifiInfoList.hasNext()) {
					WifiInfo tmpWifiInfoList= newWifiInfoList.next();
					SwifiInfo=SwifiInfo+tmpWifiInfoList.getName()+","+tmpWifiInfoList.getPassword()+",";
					Log.d("urovo", "SwifiInfo:" +SwifiInfo);
				}
			}else{
				Log.d("urovo", "mWifiInfoList.isEmpty(): ");
			}
			Log.d("urovo", "SwifiInfo:"+SwifiInfo);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


 /*   String SwifiInfo="";
    private void getWifiInfo() {
        FileInputStream inputStream;
        ArrayList<WifiInfo> mWifiInfoList = new ArrayList<WifiInfo>();
        try {
            File file = new File("/data/misc/wifi/WifiConfigStore.xml");

            inputStream = new FileInputStream(file);
            byte temp[] = new byte[1024];
            StringBuilder sb = new StringBuilder("");
            int len = 0;
            while ((len = inputStream.read(temp)) > 0){
                sb.append(new String(temp, 0, len));
            }
            Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
	        Matcher networkMatcher = network.matcher(sb.toString());
	        WifiInfo wifiInfo;
	        while (networkMatcher.find()) {
	            String networkBlock = networkMatcher.group();
	            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
	            Matcher ssidMatcher = ssid.matcher(networkBlock);
	            if (ssidMatcher.find()) {
	                wifiInfo = new WifiInfo();
	                wifiInfo.setName(ssidMatcher.group(1));
	                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
	                Matcher pskMatcher = psk.matcher(networkBlock);
	                if (pskMatcher.find()) {
	                    wifiInfo.setPassword(pskMatcher.group(1));
	                } else {
	                    wifiInfo.setPassword("无密码");
	                }
                    mWifiInfoList.add(wifiInfo);
                    Log.i("urovo","wifi密码："+"wifiInfo.getName:"+wifiInfo.getName()+"   wifiInfo.setPassword:"+wifiInfo.getPassword());
                }
            }
             Log.d("urovo", "readSaveFile: \n" + sb.toString());

            if(!mWifiInfoList.isEmpty()){
                Iterator<WifiInfo> newWifiInfoList = mWifiInfoList.iterator();
		                while(newWifiInfoList.hasNext()) {
		                	WifiInfo tmpWifiInfoList= newWifiInfoList.next();
                            SwifiInfo=SwifiInfo+tmpWifiInfoList.getName()+","+tmpWifiInfoList.getPassword()+",";
                            Log.d("urovo", "SwifiInfo:" +SwifiInfo);
		   	        	  	}

              }else{
                Log.d("urovo", "mWifiInfoList.isEmpty(): ");
              }
            Log.d("urovo", "SwifiInfo:"+SwifiInfo);
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

*/


	public void getnewSettingProp(){

		newSystemlists.clear();
		Cursor systemcursor = mContext.getContentResolver().query(systemuri, null, null, null, null);
		while (systemcursor.moveToNext()) {
			Systemlist  mSystemlist=new Systemlist();
			String  key=  systemcursor.getString(systemcursor.getColumnIndex("name"));
			String  value=          systemcursor.getString(systemcursor.getColumnIndex("value"));
			if(TextUtils.isEmpty(value)){value = "";}
			mSystemlist.setkey(key);
			mSystemlist.setvalue(value);
			newSystemlists.add(mSystemlist);
			Log.i(TAG,"new  System key:"+mSystemlist.getkey()+"              value: "+mSystemlist.getvalue());
		}

		systemcursor.close();


		newGloballists.clear();
		Cursor globacursor = mContext.getContentResolver().query(Globauri, null, null, null, null);
		while (globacursor.moveToNext()) {
			Globallist  mGloballist=new Globallist();
			String  key=  globacursor.getString(globacursor.getColumnIndex("name"));
			String  value=          globacursor.getString(globacursor.getColumnIndex("value"));
			if(TextUtils.isEmpty(value)){value = "";}
			mGloballist.setkey(key);
			mGloballist.setvalue(value);
			newGloballists.add(mGloballist);
			Log.i(TAG,"new   Global key:"+mGloballist.getkey()+"              value: "+mGloballist.getvalue());
		}
		globacursor.close();

		newSecurelists.clear();
		Cursor Securecursor = mContext.getContentResolver().query(Secureuri, null, null, null, null);
		while (Securecursor.moveToNext()) {
			Securelist  mSecurelist=new Securelist();
			String  key=  Securecursor.getString(Securecursor.getColumnIndex("name"));
			String  value=          Securecursor.getString(Securecursor.getColumnIndex("value"));
			if(TextUtils.isEmpty(value)){value = "";}
			mSecurelist.setkey(key);
			mSecurelist.setvalue(value);
			newSecurelists.add(mSecurelist);
			Log.i(TAG,"new Secure   key:"+mSecurelist.getkey()+"              value: "+mSecurelist.getvalue());
		}
		globacursor.close();
		newScanlists.clear();
		Cursor Scancursor = mContext.getContentResolver().query(Scanuri, null, null, null, null);
		while (Scancursor.moveToNext()) {
			Scanlist  mScanlist=new Scanlist();
			String  key=  Scancursor.getString(Securecursor.getColumnIndex("name"));
			String  value=   Scancursor.getString(Securecursor.getColumnIndex("value"));
			mScanlist.setkey(key);
			mScanlist.setvalue(value);
			newScanlists.add(mScanlist);
			Log.i(TAG,"new Scan   key:"+mScanlist.getkey()+"              value: "+mScanlist.getvalue());
		}
		Scancursor.close();



	}


	public void gettmpSettingProp(){

		boolean tmpflag=true;

		tmpSystemlists.clear();
		String mlanguage=android.os.SystemProperties.get("persist.sys.locale","zh-CN"); //语言 wifi账号密码单独处理
		if(!mlanguage.equals("zh-CN")){
			Systemlist  mSystemlist=new Systemlist();
			mSystemlist.setkey("persist.sys.locale");
			mSystemlist.setvalue(mlanguage);
			tmpSystemlists.add(mSystemlist);
		}
		String mcountry=android.os.SystemProperties.get("persist.sys.country","CN");
		if(!mcountry.equals("CN")){
			Systemlist  mSystemlist=new Systemlist();
			mSystemlist.setkey("persist.sys.country");
			mSystemlist.setvalue(mcountry);
			tmpSystemlists.add(mSystemlist);
		}
		getWifiInfo();//获取wifi密码
		if(!TextUtils.isEmpty(SwifiInfo))  {
			Systemlist  mSystemlist=new Systemlist();
			mSystemlist.setkey("wifi_ssid_password");
			mSystemlist.setvalue(SwifiInfo);
			tmpSystemlists.add(mSystemlist);
		}
		Iterator<Systemlist> newlistSystems = newSystemlists.iterator();
		while(newlistSystems.hasNext()) {
			Systemlist newlistSystem= newlistSystems.next();

			Iterator<Systemlist> oldlistSystems = oldSystemlists.iterator();
			while(oldlistSystems.hasNext()) {
				Systemlist oldlistSystem= oldlistSystems.next();
				Log.i(TAG,"oldlistSystem.getkey():"+oldlistSystem.getkey());
				if(oldlistSystem.getkey().equals( newlistSystem.getkey()) &&    !newlistSystem.getvalue().equals(oldlistSystem.getvalue()) ){
					tmpSystemlists.add(newlistSystem);
					tmpflag=false;
					Log.i(TAG,"System key:"+ oldlistSystem.getkey()+"<---->"  +newlistSystem.getkey()+"      value:"+oldlistSystem.getvalue()+"<----->"+newlistSystem.getvalue());
				}
			}
		}

		tmpGloballists.clear();
		Iterator<Globallist> newlistGlobals = newGloballists.iterator();
		while(newlistGlobals.hasNext()) {
			Globallist newlistGlobal= newlistGlobals.next();

			Iterator<Globallist> oldlistGlobals = oldGloballists.iterator();
			while(oldlistGlobals.hasNext()) {
				Globallist oldlistGlobal= oldlistGlobals.next();
				if(newlistGlobal.getkey().equals(oldlistGlobal.getkey())&&! oldlistGlobal.getvalue().equals(newlistGlobal.getvalue()) ){
					tmpGloballists.add(newlistGlobal);
					tmpflag=false;
					Log.i(TAG,"Global   key:"+  oldlistGlobal.getkey()+"<---->"+newlistGlobal.getkey() +"   value:"+ oldlistGlobal.getkey()+"<----->"+newlistGlobal.getvalue());
				}
			}
		}


		tmpSecurelists.clear();
		Iterator<Securelist> newlistSecures= newSecurelists.iterator();
		while(newlistSecures.hasNext()) {
			Securelist newlistSecure= newlistSecures.next();

			Iterator<Securelist> oldlistSecures= oldSecurelists.iterator();
			while(oldlistSecures.hasNext()) {
				Securelist oldlistSecure= oldlistSecures.next();
				if(oldlistSecure.getkey().equals(newlistSecure.getkey()) &&  !oldlistSecure.getvalue().equals(newlistSecure.getvalue())){
					tmpSecurelists.add(newlistSecure);
					tmpflag=false;
					Log.i(TAG,"Secure  key:"+  oldlistSecure.getkey() +"<----->"+newlistSecure.getkey()+"      value:"+ oldlistSecure.getvalue()+"<------->"+newlistSecure.getvalue());
				}
			}
		}

		tmpScanlists.clear();
		Iterator<Scanlist> newlistScans= newScanlists.iterator();
		while(newlistScans.hasNext()) {
			Scanlist newlistScan= newlistScans.next();
			if((newlistScan.getkey().equals("LABEL_PREFIX")||newlistScan.getkey().equals("LABEL_SUFFIX")||
					newlistScan.getkey().equals("LABEL_MATCHER_PATTERN")) && !TextUtils.isEmpty(newlistScan.getvalue())){
				tmpScanlists.add(newlistScan);
			}
			Iterator<Scanlist> oldlistScans= oldScanlists.iterator();
			while(oldlistScans.hasNext()) {
				Scanlist oldlistScan= oldlistScans.next();
				if(newlistScan.getkey().equals(oldlistScan.getkey()) &&    !newlistScan.getvalue().equals(oldlistScan.getvalue()) ){
					tmpScanlists.add(newlistScan);
					tmpflag=false;
					Log.i(TAG,"Secure  key:"+  oldlistScan.getkey() +"<----->"+newlistScan.getkey()+"      value:"+ oldlistScan.getvalue()+"<------->"+newlistScan.getvalue());
				}
			}
		}

		if(oldSystemlists.isEmpty()||oldGloballists.isEmpty() ||oldSecurelists.isEmpty()||oldScanlists.isEmpty()){

			tmpSystemlists=newSystemlists;
			tmpGloballists=newGloballists;
			tmpSecurelists=newSecurelists;
			tmpScanlists=newScanlists;
		}else  if(tmpflag){
			Toast.makeText(mContext, mContext.getString(R.string.exprot_difference), Toast.LENGTH_SHORT).show();
			//this.finish();
		}
		initlistdata(!tmpflag);

	}

	public  void WritetoXml() {
		boolean  flag=true;

		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// 获取SD卡的目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				String path = "/Custom_Local/";
				File dir = new File(sdCardDir+path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				Log.i(TAG,sdCardDir.getCanonicalPath() +path);
				FileWriter fwtmp = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
				fwtmp.write("NULL"+"\r\n");
				fwtmp.close();
				if(setSystemlists.size()>0 || setGloballists.size()>0  ||  setSecurelists.size()>0 || setScanlists.size()>0){
					FileWriter fw = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
					fw.write("<propertygroup>"+"\r\n");
					fw.write("	<package>"+"\r\n");
					fw.write("			<packageName>com.android.providers.settings</packageName>"+"\r\n");
					//Toast.makeText(mContext, "ing", Toast.LENGTH_LONG).show();
					Iterator<Systemlist> msetSystemlists = setSystemlists.iterator();
					while(msetSystemlists.hasNext()) {
						Systemlist setSystemlist= msetSystemlists.next();
						fw.write("			<table >"+"\r\n");
						fw.write("				<name>System</name>"+"\r\n");
						fw.write("				<property>"+"\r\n");
						fw.write("					<key>"+setSystemlist.getkey()+"</key>"+"\r\n");
						fw.write("					<value>"+setSystemlist.getvalue()+"</value>"+"\r\n");
						fw.write("				</property>"+"\r\n");
						fw.write("			</table >"+"\r\n");
					}
					Iterator<Globallist>msetGloballists = setGloballists.iterator();
					while(msetGloballists.hasNext()) {
						Globallist setGloballist= msetGloballists.next();
						fw.write("			<table >"+"\r\n");
						fw.write("				<name>Global</name>"+"\r\n");
						fw.write("				<property>"+"\r\n");
						fw.write("					<key>"+setGloballist.getkey()+"</key>"+"\r\n");
						fw.write("					<value>"+setGloballist.getvalue()+"</value>"+"\r\n");
						fw.write("				</property>"+"\r\n");
						fw.write("			</table >"+"\r\n");
					}
					Iterator<Securelist> msetSecurelists = setSecurelists.iterator();
					while(msetSecurelists.hasNext()) {
						Securelist setSecurelist= msetSecurelists.next();
						fw.write("			<table >"+"\r\n");
						fw.write("				<name>Secure</name>"+"\r\n");
						fw.write("				<property>"+"\r\n");
						fw.write("					<key>"+setSecurelist.getkey()+"</key>"+"\r\n");
						fw.write("					<value>"+setSecurelist.getvalue()+"</value>"+"\r\n");
						fw.write("				</property>"+"\r\n");
						fw.write("			</table >"+"\r\n");
					}
					fw.write("	</package>"+"\r\n");
					fw.write("	<package>"+"\r\n");
					fw.write("			<packageName>com.urovo.provider.settings</packageName>"+"\r\n");
					//Toast.makeText(mContext, "ing", Toast.LENGTH_LONG).show();
					Iterator<Scanlist> msetScanlists = setScanlists.iterator();
					while(msetScanlists.hasNext()) {
						Scanlist setScanlist= msetScanlists.next();
						fw.write("			<table >"+"\r\n");
						fw.write("				<name>settings</name>"+"\r\n");
						fw.write("				<property>"+"\r\n");
						fw.write("					<key>"+setScanlist.getkey()+"</key>"+"\r\n");
						fw.write("					<value>"+setScanlist.getvalue()+"</value>"+"\r\n");
						fw.write("				</property>"+"\r\n");
						fw.write("			</table >"+"\r\n");
					}
					fw.write("	</package>"+"\r\n");

					fw.write("	<package>"+"\r\n");
					fw.write("			<packageName>SystemProperties</packageName>"+"\r\n");
					//Toast.makeText(mContext, "ing", Toast.LENGTH_LONG).show();
					Iterator<Systemlist> lsetSystemlists = setSystemlists.iterator();//语言单独处理 persist.sys.language
					while(lsetSystemlists.hasNext()) {
						Systemlist setSystemlist= lsetSystemlists.next();
						if(setSystemlist.getkey().equals("persist.sys.locale")|| setSystemlist.getkey().equals("persist.sys.country")){
							fw.write("			<table >"+"\r\n");
							fw.write("				<name>persist</name>"+"\r\n");
							fw.write("				<property>"+"\r\n");
							fw.write("					<key>"+setSystemlist.getkey()+"</key>"+"\r\n");
							fw.write("					<value>"+setSystemlist.getvalue()+"</value>"+"\r\n");
							fw.write("				</property>"+"\r\n");
							fw.write("			</table >"+"\r\n");
						}
					}
					fw.write("	</package>"+"\r\n");

					fw.write("</propertygroup>"+"\r\n");

					//关闭文件
					fw.close();
				}
			}
		} catch (Exception e) {
			//Toast.makeText(mContext, "filed", Toast.LENGTH_LONG).show();
			flag=false;
			e.printStackTrace();

			//this.finish();
		}

		//Toast.makeText(mContext, "seccus", Toast.LENGTH_LONG).show();
		Message msg = handler.obtainMessage();
		if(flag)
			msg.what = 2;
		else
			msg.what = 1;
		handler.sendMessage(msg);
		//this.finish();
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// 处理消息
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					Toast.makeText(mContext, mContext.getString(R.string.exprot_write_fail), Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(mContext, mContext.getString(R.string.exprot_write_success), Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};


	/*****************************
	 adapter 相关

	 ******************************/



	private ListView listview;
	private List<String> array = new ArrayList<String>();
	private List<String> arrayzh = new ArrayList<String>();
	private List<String> selectid = new ArrayList<String>();
	private boolean isMulChoice = true; //是否多选
	private Adapter  adapter;
	//private LinearLayout layout;
	private  int intGlobal =0,intSecure=0,intScan=0;
	boolean initarrayzh(String key){
		Log.i("wujinquan","key:"+key);
		int stringid= mContext.getResources().getIdentifier(key, "string", mContext.getPackageName());
		if(stringid>0){
			arrayzh.add(getString(stringid));
			return true;
		}else{
			return false;
		}
            /*
            switch(key){
                case "wifi_on":arrayzh.add("WLAN"); break;
                case "bluetooth_on":arrayzh.add("蓝牙"); break;
                case "airplane_mode_on":arrayzh.add("飞行模式"); break;
                case "TRIGGERING_MODES":arrayzh.add("触发模式"); break;
                case "WEDGE_KEYBOARD_ENABLE":arrayzh.add("键盘方式输出"); break;
                case "WEDGE_KEYBOARD_TYPE":arrayzh.add("键盘类型"); break;
                case "GOOD_READ_BEEP_ENABLE":arrayzh.add("提示音(键盘模式)"); break;
                case "GOOD_READ_VIBRATE_ENABLE":arrayzh.add("震动（键盘模式）"); break;
                case "SEND_GOOD_READ_VIBRATE_ENABLE":arrayzh.add("震动（广播模式）"); break;
                case "SEND_GOOD_READ_BEEP_ENABLE":arrayzh.add("提示音(广播模式)"); break;
                case "LABEL_APPEND_ENTER":arrayzh.add("附加回车键"); break;
                case "WEDGE_INTENT_ACTION_NAME":arrayzh.add("广播动作"); break;
                case "INTENT_DATA_STRING_TAG":arrayzh.add("广播数据标签"); break;
                case "CODING_FORMAT":arrayzh.add("中文编码类型"); break;
                case "SEND_LABEL_PREFIX_SUFFIX":arrayzh.add("附加格式化"); break;
                case "REMOVE_NONPRINT_CHAR":arrayzh.add("删除非打印控制字符"); break;
                case "screen_brightness":arrayzh.add("亮度"); break;
                case "screen_brightness_mode":arrayzh.add("自动调节亮度"); break;
                case "screen_off_timeout":arrayzh.add("休眠"); break;
                case "accelerometer_rotation":arrayzh.add("设备旋转时"); break;
                case "location_providers_allowed":arrayzh.add("位置"); break;
                case "install_non_market_apps":arrayzh.add("未知来源"); break;
                case "default_input_method":arrayzh.add("输入法"); break;
                case "enabled_input_method":arrayzh.add("勾选输入法"); break;
                case "selected_input_method_subtype":arrayzh.add("当前输入法"); break;
                case "auto_time":arrayzh.add("自动确定时间和日期"); break;
                case "auto_time_zone":arrayzh.add("自动确定时区"); break;
                case "auto_pop_softinput":arrayzh.add("自动弹出软键盘"); break;
                case "adb_enabled":arrayzh.add("USB调试"); break;
                case "wifi_ssid_password":arrayzh.add("wifi账号、密码"); break;
                case "volume_music_speaker":arrayzh.add("媒体音量"); break;
                case "volume_alarm_speaker":arrayzh.add("闹钟音量"); break;
                case "volume_ring_speaker":arrayzh.add("铃声音量"); break;
                case "font_scale":arrayzh.add("字体大小"); break;
                case "time_12_24":arrayzh.add("使用24小时格式"); break;
                case "device_nfc":arrayzh.add("NFC"); break;
                case "SUSPENSION_BUTTON":arrayzh.add("悬浮按键开/关"); break;
                case "accessibility_display_inversion_enabled":arrayzh.add("颜色反转"); break;
                //case "persist.sys.country":arrayzh.add("地区（语言）"); break;
                case "persist.sys.locale":arrayzh.add("语言"); break;
                case "LABEL_FORMAT_SEPARATOR_CHAR":arrayzh.add("应用标识分隔符"); break;
                case "LABEL_SEPARATOR_ENABLE":arrayzh.add("应用标识符"); break;
                case "LABEL_PREFIX":arrayzh.add("前缀"); break;
                case "LABEL_SUFFIX":arrayzh.add("后缀"); break;
                case "dtmf_tone":arrayzh.add("拨号键盘提示音"); break;
                case "LABEL_MATCHER_PATTERN":arrayzh.add("格式化"); break;
                case "lockscreen_sounds_enabled":arrayzh.add("屏幕锁定提示音"); break;
                case "sound_effects_enabled":arrayzh.add("触摸提示音"); break;
                case "haptic_feedback_enabled":arrayzh.add("触摸时震动"); break;
                default:   arrayzh.add(key);

            }*/
	}


	void initlistdata(boolean state)
	{  if(!state){
		array.clear();
		arrayzh.clear();
		selectid.clear();
	}else{
		boolean initstate=false;
		array.clear();
		arrayzh.clear();
		selectid.clear();
		Iterator<Systemlist> mtmpSystemlists = tmpSystemlists.iterator();
		int i=0;
		boolean flag=true;
		while(mtmpSystemlists.hasNext()) {
			Systemlist tmpSystemlist= mtmpSystemlists.next();
			if(flag){
				arrayzh.add("System:");
				array.add("System:");
				i++;
			}
			flag=false;
			initstate=initarrayzh(tmpSystemlist.getkey());
			if(initstate){
				i++;
				array.add(tmpSystemlist.getkey());
			}
			initstate=false;

		}
		flag=true;
		boolean gflag=true;
		Iterator<Globallist>mtmpGloballists = tmpGloballists.iterator();
		while(mtmpGloballists.hasNext()) {
			Globallist tmpGloballist= mtmpGloballists.next();
			gflag=true;

			for(int j=0;j<array.size();j++){
				if(array.get(j).equals(tmpGloballist.getkey())){
					gflag=false;
				}
			}
			if(gflag ){
				if(flag){
					intGlobal=i;
					arrayzh.add("Global:");
					array.add("Global:");
					i++;
					flag=false;
				}
				initstate=initarrayzh(tmpGloballist.getkey());
				if(initstate){
					array.add(tmpGloballist.getkey());
					i++;
				}
				initstate=false;

			}
		}
		Log.i("urovo","  arrayy():"+array.size());
		flag=true;
		Iterator<Securelist> mtmpSecurelists = tmpSecurelists.iterator();
		while(mtmpSecurelists.hasNext()) {
			Securelist tmpSecurelist= mtmpSecurelists.next();
			if(flag){
				intScan=i;
				arrayzh.add("Secure:");
				array.add("Secure:");
				i++;
			}
			flag=false;
			initstate=initarrayzh(tmpSecurelist.getkey());
			if(initstate){
				array.add(tmpSecurelist.getkey());
				i++;
			}
			initstate=false;

		}
		flag=true;
		Iterator<Scanlist> mtmpScanlists = tmpScanlists.iterator();
		while(mtmpScanlists.hasNext()) {
			Scanlist tmpScanlist= mtmpScanlists.next();
			if(flag){
				intSecure=i;
				arrayzh.add("Scan:");
				array.add("Scan:");
				i++;
			}
			flag=false;
			initstate=initarrayzh(tmpScanlist.getkey());
			if(initstate){
				array.add(tmpScanlist.getkey());
				i++;
			}
			initstate=false;
		}


		for(int k=0;k<array.size();k++){
			selectid.add(array.get(k));
		}
		adapter = new Adapter(mContext);
		listview.setAdapter(adapter);
	}

	}
	public void localexport(){

		for(int i=0;i<selectid.size();i++){
			for(int j=0;j<array.size();j++){
				if(selectid.get(i).equals(array.get(j))){
					//      array.remove(j);
				}
			}
		}
		Iterator<Systemlist> mtmpSystemlists = tmpSystemlists.iterator();
		setSystemlists.clear();
		while(mtmpSystemlists.hasNext()) {
			Systemlist tmpSystemlist= mtmpSystemlists.next();
			for(int i=0;i<selectid.size();i++){
				if(selectid.get(i).equals(tmpSystemlist.getkey()))
					setSystemlists.add(tmpSystemlist);
			}
		}

		Iterator<Globallist>mtmpGloballists = tmpGloballists.iterator();
		setGloballists.clear();
		while(mtmpGloballists.hasNext()) {
			Globallist tmpGloballist= mtmpGloballists.next();
			for(int i=0;i<selectid.size();i++){
				if(selectid.get(i).equals(tmpGloballist.getkey()))
					setGloballists.add(tmpGloballist);
			}
		}
		Iterator<Securelist> mtmpSecurelists = tmpSecurelists.iterator();
		setSecurelists.clear();
		while(mtmpSecurelists.hasNext()) {
			Securelist tmpSecurelist= mtmpSecurelists.next();
			for(int i=0;i<selectid.size();i++){
				if(selectid.get(i).equals(tmpSecurelist.getkey()))
					setSecurelists.add(tmpSecurelist);
			}
		}
		Iterator<Scanlist> mtmpScanlists = tmpScanlists.iterator();
		setScanlists.clear();
		while(mtmpScanlists.hasNext()) {
			Scanlist tmpScanlist= mtmpScanlists.next();
			for(int i=0;i<selectid.size();i++){
				if(selectid.get(i).equals(tmpScanlist.getkey()))
					setScanlists.add(tmpScanlist);
			}
		}
		WritetoXml();
		//selectid.clear();
		//adapter = new Adapter(mContext);
		//listview.setAdapter(adapter);
		// layout.setVisibility(View.INVISIBLE);




	}


	   /* public void exportBtnClick(){
	    	export.setOnClickListener(new View.OnClickListener() {
	    		@Override
	    		public void onClick(View v) {
                    new Thread(){
                        public void run(){
                            localexport();
                        }
                    }.start();

                }
	    	});
   		}


	    public void cancelBtnClick(){
	    	cancel.setOnClickListener(new View.OnClickListener() {
	    		@Override
	    		public void onClick(View v) {
	    			finish();
	    		}
	    		});
	    	}

	     public void BTexportBtnClick(){
	    	mexportbt.setOnClickListener(new View.OnClickListener() {
	    		@Override
	    		public void onClick(View v) {
                    new Thread(){
                        public void run(){

                            localexport();
                        }
                    }.start();
	    			Intent intent =new Intent(exportActivity.this,com.bluetoothscan.qrcode.BtScanActivity.class);

	            	startActivity(intent);
	    		}
	    		});
	    	}

         public void NFCexportBtnClick(){
	    	mexportnfc.setOnClickListener(new View.OnClickListener() {
	    		@Override
	    		public void onClick(View v) {
                    new Thread(){
                        public void run(){

                            localexport();
                        }
                    }.start();
	    			Intent intent =new Intent(exportActivity.this,com.example.nfcfile.ExprotNfcActivity.class);
	            	startActivity(intent);
	    		}
	    		});
	    	}*/

	class Adapter extends BaseAdapter{
		private Context context;
		private LayoutInflater inflater=null;
		private HashMap<Integer, View> mView ;
		public  HashMap<Integer, Integer> visiblecheck ;//用来记录是否显示checkBox
		public  HashMap<Integer, Boolean> ischeck;
		public Adapter(Context context)
		{
			this.context = context;

			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mView = new HashMap<Integer, View>();
			visiblecheck = new HashMap<Integer, Integer>();
			ischeck      = new HashMap<Integer, Boolean>();
			if(isMulChoice){
				for(int i=0;i<array.size();i++){
					if(selectid.size()>0&& i<selectid.size() && selectid.get(i).equals(array.get(i)))
						ischeck.put(i, true);
					else
						ischeck.put(i, false);
					android.util.Log.i("wujinquan","intGlobal:"+intGlobal+"   intSecure:"+intSecure+"   intScan:"+intScan);
					if(i==intGlobal || i==intSecure || i==0 ||i==intScan)
						visiblecheck.put(i, CheckBox.INVISIBLE);
					else
						visiblecheck.put(i, CheckBox.VISIBLE);
				}
			}else{
				for(int i=0;i<array.size();i++){
					ischeck.put(i, false);
					visiblecheck.put(i, CheckBox.INVISIBLE);
				}
			}
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return array.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return arrayzh.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = mView.get(position);
			if(view==null) {
				view = inflater.inflate(R.layout.listview_item, null);
				TextView txt = (TextView)view.findViewById(R.id.txtName);
				final CheckBox ceb = (CheckBox)view.findViewById(R.id.check);

				txt.setText(arrayzh.get(position));

				ceb.setChecked(ischeck.get(position));
				ceb.setVisibility(visiblecheck.get(position));

				view.setOnLongClickListener(new Onlongclick());

				view.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(isMulChoice){
							if(ceb.isChecked()){
								ceb.setChecked(false);
								selectid.remove(array.get(position));
								Toast.makeText(context, "remove", Toast.LENGTH_SHORT).show();
							}else{
								ceb.setChecked(true);
								Toast.makeText(context, "setChecked", Toast.LENGTH_SHORT).show();
								selectid.add(array.get(position));
							}
						}else {
							//Toast.makeText(context, "点击了"+array.get(position), Toast.LENGTH_LONG).show();
						}
					}
				});
				mView.put(position, view);
			}
			return view;
		}

		class Onlongclick implements OnLongClickListener{

			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				isMulChoice = true;
				selectid.clear();
				layout.setVisibility(View.VISIBLE);
				for(int i=0;i<array.size();i++)
				{
					adapter.visiblecheck.put(i, CheckBox.VISIBLE);
				}
				adapter = new Adapter(context);
				listview.setAdapter(adapter);
				return true;
			}
		}
	}


}
