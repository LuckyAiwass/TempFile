package com.ubx.keyremap.data;

import android.content.Context;
import android.device.KeyMapManager;

import com.ubx.keyremap.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemapDetailModel extends IntentExtrasModel {

    private static final String TAG = Utils.TAG + "#" + RemapDetailModel.class.getSimpleName();

    private KeyMapManager mKeyMapManager = null;
    private Context mContext = null;
    private List<String> mKeyList = null;
    private Map<String, Integer> mKeyMap = null;

    private String mKeyDownAction = null;
    private List<ListItem.IntentExtra> mKeyDownActionExtras = null;
    private String mKeyUpAction = null;
    private List<ListItem.IntentExtra> mKeyUpActionExtras = null;

    public RemapDetailModel(Context context) {
        mContext = context;
        mKeyMapManager = new KeyMapManager(mContext);
    }

    public KeyMapManager getKeyMapManager() {
        return mKeyMapManager;
    }

    public List getKeyList() {
        if (mKeyList == null) {
            mKeyList = new ArrayList<>();
            mKeyMap = new HashMap<>();
            int keyCode;
            String keyName;
            java.util.Iterator<Integer> it;
            it = Utils.NumberKeyMap.keySet().iterator();
            while (it.hasNext()) {
                keyCode = it.next();
                keyName = (String) Utils.NumberKeyMap.get(keyCode);
                mKeyList.add(keyName);
                mKeyMap.put(keyName, keyCode);
            }
            it = Utils.AlphabetKeyMap.keySet().iterator();
            while (it.hasNext()) {
                keyCode = it.next();
                keyName = (String) Utils.AlphabetKeyMap.get(keyCode);
                mKeyList.add(keyName);
                mKeyMap.put(keyName, keyCode);
            }
            it = Utils.FunctionKeyMap.keySet().iterator();
            while (it.hasNext()) {
                keyCode = it.next();
                keyName = (String) Utils.FunctionKeyMap.get(keyCode);
                mKeyList.add(keyName);
                mKeyMap.put(keyName, keyCode);
            }
            it = Utils.OtherKeyMap.keySet().iterator();
            while (it.hasNext()) {
                keyCode = it.next();
                keyName = (String) Utils.OtherKeyMap.get(keyCode);
                mKeyList.add(keyName);
                mKeyMap.put(keyName, keyCode);
            }
        }
        return mKeyList;
    }

    public int getKeyCode(String name) {
        if (mKeyMap == null) {
            return -1;
        }
        return mKeyMap.get(name);
    }

    public String getKeyName(int position) {
        if (mKeyList == null) {
            return null;
        }
        return mKeyList.get(position);
    }

    public String getKeyDownAction() {
        return mKeyDownAction;
    }

    public void setKeyDownAction(String action) {
        this.mKeyDownAction = action;
    }

    public ArrayList<ListItem.IntentExtra> getKeyDownActionExtras() {
        return (ArrayList) mKeyDownActionExtras;
    }

    public void setKeyDownActionExtras(List<ListItem.IntentExtra> actionExtras) {
        this.mKeyDownActionExtras = actionExtras;
    }

    public String getKeyUpAction() {
        return mKeyUpAction;
    }

    public void setKeyUpAction(String action) {
        this.mKeyUpAction = action;
    }

    public ArrayList<ListItem.IntentExtra> getKeyUpActionExtras() {
        return (ArrayList) mKeyUpActionExtras;
    }

    public void setKeyUpActionExtras(List<ListItem.IntentExtra> actionExtras) {
        this.mKeyUpActionExtras = actionExtras;
    }

    public JSONObject getKeyDownBroadcastExtrasJSON(){
        return getExtrasJSON(mKeyDownActionExtras);
    }

    public JSONObject getKeyUpBroadcastExtrasJSON(){
        return getExtrasJSON(mKeyUpActionExtras);
    }
}
                                                                                                                                                                                                      