/**
 * Copyright (c) 2011-2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import java.io.Serializable;

public class UpdateInfo implements Serializable {

    static class Delta implements Serializable {
        private static final long serialVersionUID = 1L;

        int from;

        int to;
    }

    private static final long serialVersionUID = 1L;

    public static final String QNAME_UPDATE = "update";

    public static final String QNAME_VERSION = "version";

    public static final String QNAME_FILE = "file";

    public static final String QNAME_DES = "description";

    public static final String QNAME_DELTA = "delta";

    public static final String QNAME_CONTENTS = "contents";

    public static final String UPDATE_URL = "updURL";

    public static final String UPDATE_LOAD = "isload";

    public static final String UPDATE_FILE_SIZE = "size";

    public static final String UPDATE_FORCE = "isForce";

    public static final String UPDATE_SILENT = "isSilent";

    public static final String UPDATE_IMMEDIATE = "isImmediate";

    public static final String UPDATE_REMIND = "remind";

    public static final String UPDATE_RESULT = "successful";

    public static final String UPDATE_FAILEDMSG = "failedMsg";

    public static final String UPDATE_PERIOD = "period";
    /*<?xml version='1.0' encoding='UTF-8'?>
    <Firmwares>
        <Firmware>
        <Name>Saga2</Name>
        <AndroidVersion>5.0</AndroidVersion>
        <BuildNumber>18062201</BuildNumber>
         <size>82398782</size>
        <URL>http://download1.tw.ute.com/cs/firmware/ea300/SQ42T_UTE__180622_01_N_P1_AX_U_16+16_EN_AB.zip</URL>
        </Firmware>
    </Firmwares>*/
    public static final String UTE_UPDATE_TAG_Firmware = "Firmware";
    public static final String UTE_UPDATE_TAG_Name = "Name";
    public static final String UTE_UPDATE_TAG_BuildNumber = "BuildNumber";
    public static final String UTE_UPDATE_TAG_URL = "URL";
    public static final String UTE_UPDATE_TAG_AndroidVersion = "AndroidVersion";

    private String version;

    private String fileName;

    private String description;

    private long size;

    private Delta delta;

    private boolean isload;

    private String updURL;

    private String name;

    private boolean isforce = false;

    private boolean issilent = false;

    private boolean isimmediate = false;

    private String remind = "";

    private boolean issuccess = false;

    private String failedMsg = "";

    private String period = "";
    
    public Delta getDelta() {
        return delta;
    }

    public void setDelta(Delta delta) {
        this.delta = delta;
    }

    public String getName() {
        return name;
    }


    public boolean getIsSuccess() {
        return issuccess;
    }

    public String getFailedMeg() {
        return failedMsg;
    }

    public void setSuccess(boolean success) {
        this.issuccess = success;
    }

    public void setFailedMeg(String msg) {
        this.failedMsg = msg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadURL() {
        return updURL;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String per) {
        this.period = per;
    }

    public void setDownloadURL(String url) {
        this.updURL = url;
    }

    public boolean hasNewVersion() {
        return isload;
    }

    public void setNewVersion(boolean isload) {
        this.isload = isload;
    }

    public boolean getForceUpdate() {
        return isforce;
    }

    public void setForceUpdate(boolean force) {
         this.isforce = force;
    }

    public boolean getSilentUpdate() {
        return issilent;
    }

    public void setSilentUpdate(boolean im) {
         this.issilent = im;
    }

    public boolean getImmediateUpdate() {
        return isimmediate;
    }

    public void setImmediateUpdate(boolean immediate) {
         this.isimmediate = immediate;
    }

    public String getReaindInfo() {
        return remind;
    }

    public void setRemindUpdate(String remind) {
         this.remind = remind;
    }

    public String toString() {
        return "version:" + version + "\tfileName:" + fileName;
    }

}
