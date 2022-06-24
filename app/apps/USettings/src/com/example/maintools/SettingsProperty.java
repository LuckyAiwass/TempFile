package com.example.maintools;

import android.text.TextUtils;
import android.util.Log;

import com.example.saxparsexml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;

public class SettingsProperty {
	String  TAG="urovo";
	Context  mContext;
	public SettingsProperty(Context mcontext) {
		// TODO Auto-generated constructor stub
		mContext=mcontext;
	}


	public int SetSettingProp (String patch){
		String   mPackage,mname,mkey,mvalue;
		UpdateContentResolver  mUpdateContentResolver=new UpdateContentResolver(mContext);
		boolean flag=false;
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
						if(TextUtils.isEmpty(mvalue))mvalue=" ";
						Log.i(TAG,"mPackage :"+ mPackage+"  mname:"+mname+"   mkey:"+mkey+"   mvalue:"+mvalue);
						if(!TextUtils.isEmpty(mPackage) && !TextUtils.isEmpty(mname) && !TextUtils.isEmpty(mkey) && !TextUtils.isEmpty(mvalue)){
							if(mPackage.equals("com.android.providers.settings")){
								if(mname.equals("Secure")) {
									String  Secureprop=Settings.Secure.getString(mContext.getContentResolver(), mkey);
									//if(!TextUtils.isEmpty(Secureprop))
									Settings.Secure.putString(mContext.getContentResolver(), mkey, mvalue);//Integer.parseInt(mvalue));
									//else
									//	Log.i(TAG,"SaxParseXml, No label Secure exists:"+mkey);
								}else if(mname.equals("System")){
									//String  Systemprop=Settings.System.getString(mContext.getContentResolver(), mkey);
									//if(!TextUtils.isEmpty(Systemprop))
									Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
									//else
									//	Log.i(TAG,"SaxParseXml, No label System exists:"+mkey);
								}else if(mname.equals("Global")){
									String  Globalprop=Settings.Global.getString(mContext.getContentResolver(), mkey);

									//if(!TextUtils.isEmpty(Globalprop)){
									Settings.Global.putString(mContext.getContentResolver(), mkey, mvalue);
									//}else{
									//	Log.i(TAG,"SaxParseXml, No label Global exists:"+mkey);
									//   }
								}else {
									Log.i(TAG,"SaxParseXml error :XML file writing is not standard");
								}
							}else if(mPackage.equals("com.ubx.provider.settings")){
								String  Settingsprop=android.device.provider.Settings.System.getString(mContext.getContentResolver(), mkey);
								if(!TextUtils.isEmpty(Settingsprop)){
									android.device.provider.Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
								}else{
									Log.i(TAG,"SaxParseXml,No label  exists:"+mkey);
									android.device.provider.Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
								}
							}else if(mPackage.equals("SystemProperties")){
								String propvalue=android.os.SystemProperties.get(mkey,"");
								if(!TextUtils.isEmpty(propvalue))
									android.os.SystemProperties.set(mkey,mvalue);
								else
									Log.i(TAG,"SaxParseXml,No label  exists:"+mkey);
							}else{
								Log.i(TAG,"SaxParseXml  error: XML file writing is not standard");
							}
							boolean state=mUpdateContentResolver.UpdateSystem(mkey,mvalue);
							if(state)flag=true;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		if(flag)    return 0;
		else        return 1;
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

}
