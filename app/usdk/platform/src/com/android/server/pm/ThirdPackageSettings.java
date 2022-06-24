/*
 	author : chenchuanliang@unicair.cn
	data   : 2017.4.16
	effect : for the third app in system/third-app
*/
package com.android.server.pm;  
  
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import android.util.AtomicFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Xml;
import libcore.io.IoUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import com.android.internal.util.FastXmlSerializer; 
import android.util.Log;

public final class ThirdPackageSettings {  
  
    private final HashSet<String> mThirdPackages = new HashSet<String>();
	private AtomicFile mPolicyFile;
	private static final int DB_VERSION = 1;

	private static final String TAG_BODY = "xml-policy";
	private static final String ATTR_VERSION = "version";

	private static final String TAG_BLOCKED = "third-packages";
	private static final String TAG_PACKAGE = "package";
	private static final String ATTR_NAME = "filename";
	
    public ThirdPackageSettings() {
		loadThirdPackage();
    }
  	private void loadThirdPackage() {
        synchronized(mThirdPackages) {
            if (mPolicyFile == null) {
                File dir = new File("/data/system");
                mPolicyFile = new AtomicFile(new File(dir, "third-app.xml"));

                mThirdPackages.clear();

                FileInputStream infile = null;
                try {
                    infile = mPolicyFile.openRead();
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(infile, null);

                    int type;
                    String tag;
                    int version = DB_VERSION;
                    while ((type = parser.next()) != END_DOCUMENT) {
                        tag = parser.getName();
                        if (type == START_TAG) {
                            if (TAG_BODY.equals(tag)) {
                                version = Integer.parseInt(parser.getAttributeValue(null, ATTR_VERSION));
                            } else if (TAG_BLOCKED.equals(tag)) {
                                while ((type = parser.next()) != END_DOCUMENT) {
                                    tag = parser.getName();
                                    if (TAG_PACKAGE.equals(tag)) {
                                        mThirdPackages.add(parser.getAttributeValue(null, ATTR_NAME));
                                    } else if (TAG_BLOCKED.equals(tag) && type == END_TAG) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    // No data yet
                } catch (IOException e) {
                    Log.wtf("chen", "Unable to read third app", e);
                } catch (NumberFormatException e) {
                    Log.wtf("chen", "Unable to parse third app database", e);
                } catch (Exception e) {
                    Log.wtf("chen", "loadThirdPackage error : ", e);
                } finally {
                    IoUtils.closeQuietly(infile);
                }
            }
        }
    }

    private void writeThirdPackage() {
        synchronized(mThirdPackages) {
			if(mThirdPackages == null)
				return;
            FileOutputStream outfile = null;
            try {
                outfile = mPolicyFile.startWrite();

                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(outfile, "utf-8");

                out.startDocument(null, true);

                out.startTag(null, TAG_BODY); {
                    out.attribute(null, ATTR_VERSION, String.valueOf(DB_VERSION));
                    out.startTag(null, TAG_BLOCKED); {
                        // write all known network policies
                        for (String pkg : mThirdPackages) {
                            out.startTag(null, TAG_PACKAGE); {
                                out.attribute(null, ATTR_NAME, pkg);
                            } out.endTag(null, TAG_PACKAGE);
                        }
                    } out.endTag(null, TAG_BLOCKED);
                } out.endTag(null, TAG_BODY);

                out.endDocument();

                mPolicyFile.finishWrite(outfile);
            } catch (Exception e) {
                if (outfile != null) {
                    mPolicyFile.failWrite(outfile);
                }
            }
        }
    }
	
	public boolean isThirdPackageInstall(String name) {
		Log.d("chen","isThirdPackageInstall : "+name+".apk");
		for (String str : mThirdPackages)
		{
		   Log.d("chen","isThirdPackageInstall has: "+str);
		}
		if(mThirdPackages == null)
			return false;
        boolean enabled = mThirdPackages.contains(name+".apk");
        return enabled;
    }
	
    public void removeThirdPackage(String name) {
		Log.d("chen","removeThirdPackage : "+name);
		if(mThirdPackages.contains(name+".apk"))
		{
        	mThirdPackages.remove(name+".apk");
        	writeThirdPackage();
		}
    } 
	public void addThirdPackage(String[] name) {
		if(name == null)
			return;
		int i;
        for (i=0; i<name.length; i++) {
			if(!name[i].equals("oat"))
			{
				Log.d("chen","addThirdPackage : "+name[i]);
        		mThirdPackages.add(name[i]);
			}
        }
		/*for (String str : mThirdPackages)
		{
		   Log.d("chen","isThirdPackageInstall has: "+str);
		}*/
        writeThirdPackage();
    } 
}  