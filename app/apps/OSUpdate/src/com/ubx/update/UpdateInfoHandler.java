/**
 * Copyright (c) 2011-2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ubx.update.UpdateInfo.Delta;

public class UpdateInfoHandler extends DefaultHandler {
    private static final String TAG = "UpdateInfoHandler";
    private List<UpdateInfo> updates;

    private UpdateInfo info;

    private String tagName = null;

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (UpdateInfo.QNAME_CONTENTS.equals(qName) || UpdateInfo.UTE_UPDATE_TAG_Firmware.equals(qName)) {
            updates.add(info);
        }
        this.tagName = null;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (UpdateInfo.QNAME_CONTENTS.equals(qName) || UpdateInfo.UTE_UPDATE_TAG_Firmware.equals(qName)) {
            info = new UpdateInfo();
        }
        //android.util.Log.d(TAG, "===============localName=" + localName);
        if (UpdateInfo.QNAME_VERSION.equals(qName)) {
            // info.setVersion(attributes.getValue("data"));
            tagName = localName;
        } else if (UpdateInfo.QNAME_FILE.equals(qName)) {
            info.setFileName(attributes.getValue("data"));
        } else if (UpdateInfo.QNAME_DES.equals(qName)) {
            info.setDescription(attributes.getValue("data"));
        } else if (UpdateInfo.UPDATE_URL.equals(qName)) {
            tagName = localName;
        } else if (UpdateInfo.UPDATE_LOAD.equals(qName)) {
            tagName = localName;
        } else if (UpdateInfo.UPDATE_FILE_SIZE.equals(qName)) {
            tagName = localName;
        } else if (UpdateInfo.QNAME_DELTA.equals(qName)) {
            Delta delta = new Delta();
            try {
                delta.from = Integer.parseInt(attributes.getValue("from"));
                delta.to = Integer.parseInt(attributes.getValue("to"));
            } catch (Exception e) {
                return;
            }
            info.setDelta(delta);
        } else if(UpdateInfo.UTE_UPDATE_TAG_Name.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UTE_UPDATE_TAG_AndroidVersion.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UTE_UPDATE_TAG_BuildNumber.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UTE_UPDATE_TAG_URL.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_FORCE.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_SILENT.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_IMMEDIATE.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_REMIND.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_RESULT.equals(qName)) {
            tagName = localName;
        } else if(UpdateInfo.UPDATE_FAILEDMSG.equals(qName)) {
            tagName = localName;
        } else if (UpdateInfo.UPDATE_PERIOD.equals(qName)) {
            tagName = localName;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tagName != null) {
            String data = new String(ch, start, length);
            if (tagName.equals(UpdateInfo.UPDATE_URL)) {
                // android.util.Log.d("UpdateInfoHandler",
                // "===============UPDATE_URL=" + data);
                if (data != null) {
                    String[] urlInfo = data.split("/");
                    info.setFileName(urlInfo[urlInfo.length - 1]);
                    info.setDescription(urlInfo[urlInfo.length - 2]);
                }
                info.setDownloadURL(data);
            } else if (tagName.equals(UpdateInfo.UPDATE_LOAD)) {
                // android.util.Log.d("UpdateInfoHandler",
                // "===============UPDATE_LOAD=" + data);
                if (data != null && data.equals("true"))
                    info.setNewVersion(true);
                else
                    info.setNewVersion(false);
            } else if (tagName.equals(UpdateInfo.QNAME_VERSION)) {
                android.util.Log.d("UpdateInfoHandler", "===============QNAME_VERSION=" + data);
                info.setVersion(data);
            } else if (tagName.equals(UpdateInfo.UPDATE_FORCE)) {
                android.util.Log.d(TAG, "===============isForce=" + data);
                if (data != null && data.equals("true"))
                    info.setForceUpdate(true);
                else
                    info.setForceUpdate(false);
            } else if (tagName.equals(UpdateInfo.UPDATE_SILENT)) {
                android.util.Log.d(TAG, "===============silent=" + data);
                if (data != null && data.equals("true"))
                    info.setSilentUpdate(true);
                else
                    info.setSilentUpdate(false);
            } else if (tagName.equals(UpdateInfo.UPDATE_IMMEDIATE)) {
                android.util.Log.d(TAG, "===============immediate=" + data);
                if (data != null && data.equals("true"))
                    info.setImmediateUpdate(true);
                else
                    info.setImmediateUpdate(false);
            } else if (tagName.equals(UpdateInfo.UPDATE_RESULT)) {
                android.util.Log.d(TAG, "===============result=" + data);
                if (data != null && data.equals("true"))
                    info.setSuccess(true);
                else
                    info.setSuccess(false);
            } else if (tagName.equals(UpdateInfo.UPDATE_FAILEDMSG)) {
                android.util.Log.d(TAG, "===============remind=" + data);
                info.setFailedMeg(data);
            } else if (tagName.equals(UpdateInfo.UPDATE_PERIOD)) {
                android.util.Log.d(TAG,"================period" + data);
                info.setPeriod(data);
            } else if (tagName.equals(UpdateInfo.UPDATE_REMIND)) {
                android.util.Log.d(TAG, "===============remind=" + data);
                info.setRemindUpdate(data);
            } else if (tagName.equals(UpdateInfo.UPDATE_FILE_SIZE)) {
                android.util.Log.d(TAG, "===============size=" + data);
                try {
                    long size = Long.parseLong(data);
                    info.setSize(size);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if(UpdateInfo.UTE_UPDATE_TAG_Name.equals(tagName)) {
                android.util.Log.d(TAG, "===============Name=" + data);
            } else if(UpdateInfo.UTE_UPDATE_TAG_AndroidVersion.equals(tagName)) {
                android.util.Log.d("UpdateInfoHandler", "===============AndroidVersion=" + data);
            } else if(UpdateInfo.UTE_UPDATE_TAG_BuildNumber.equals(tagName)) {
                android.util.Log.d("UpdateInfoHandler", "===============BuildNumber=" + data);
                String curVer = UpdateUtil.getCurrentBuildVersion();
                System.out.println( " getCurrentVersion ============================" +curVer);
                try{
                    int curVersion = Integer.parseInt(curVer);
                    int newVersion = Integer.parseInt(data);
                    if(newVersion > curVersion) {
                        info.setNewVersion( true);
                    }
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    info.setNewVersion( false);
                } catch (Exception e) {
                    e.printStackTrace();
                    info.setNewVersion( false);
                }
                info.setVersion(data);
            } else if(UpdateInfo.UTE_UPDATE_TAG_URL.equals(tagName)) {
                android.util.Log.d("UpdateInfoHandler", "===============URL=" + data);
                if(data != null) {
                    String[] urlInfo = data.split("/");
                    if(urlInfo != null && urlInfo.length >2) {
                        info.setFileName(urlInfo[urlInfo.length-1]);
                        info.setDescription(urlInfo[urlInfo.length-2]);
                    }
                }
                info.setDownloadURL(data);
            }
        }
    }

    public void startDocument() throws SAXException {
        updates = new ArrayList<UpdateInfo>();
    }

    public List<UpdateInfo> getUpdates() {
        return updates;
    }
}
