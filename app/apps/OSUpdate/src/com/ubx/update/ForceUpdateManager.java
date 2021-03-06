package com.ubx.update;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ForceUpdateManager {
    private static ForceUpdateManager mManager;
    private List<Activity> activtyList;
    private boolean forceUpdate = false;

    public static ForceUpdateManager getInstance() {
        if (mManager == null) {
            mManager = new ForceUpdateManager();
        }
        return mManager;
    }

    public void addActivity(Activity activity) {
        if (activtyList == null) {
            activtyList = new ArrayList<Activity>();
        }
        activtyList.add(activity);
    }

    public void removeActivity(String name) {
        if (activtyList == null) {
            activtyList = new ArrayList<Activity>();
        }
        for (int j = 0; j < activtyList.size(); j++) {
            if (activtyList.get(j).getApplicationInfo().className.equals(name)) {
                activtyList.remove(j);
            }
        }
    }

    public void exit() {
        try {
            for (Activity activity : activtyList) {
                if(activity != null){
                    activity.finish();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            //System.exit(0);
        }
    }

    public boolean getforceUpdate(){
        return forceUpdate;
    }
	
    public void setforceUpdate(boolean force){
        android.util.Log.d("ForceUpdateManager","setforceUpdate---->"+force);
        forceUpdate = force;
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        