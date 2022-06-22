package com.ubx.appinstall.bean;

import android.content.pm.PackageInfo;

public class PowerBootBean {

    private PackageInfo packageInfo;
    private int num;

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public PackageInfo getPackageInfo() {

        return packageInfo;
    }

    public int getNum() {
        return num;
    }
}
