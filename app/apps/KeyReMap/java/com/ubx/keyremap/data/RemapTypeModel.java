package com.ubx.keyremap.data;

import android.content.Context;
import android.device.KeyMapManager;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.IContract.IModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemapTypeModel implements IModel {

    private static final String TAG = Utils.TAG + "#" + RemapTypeModel.class.getSimpleName();

    private List<ListItem.Common> mRemapTypes = new ArrayList<>();

    private KeyMapManager mKeyMapManager = null;
    private Context mContext = null;

    public static final Map<String, Integer> REMAP_TYPE_NAME = new HashMap<>();

    static {
        REMAP_TYPE_NAME.put("code", R.string.remap_as_keycode);
        REMAP_TYPE_NAME.put("activity", R.string.remap_as_activity);
        REMAP_TYPE_NAME.put("broadcast", R.string.remap_as_broadcast);
    }

    public RemapTypeModel(Context context) {
        mContext = context;
        mKeyMapManager = new KeyMapManager(mContext);
        initRemapTypes();
    }

    public void initRemapTypes(){
        ListItem.Common item;
        item= new ListItem.Common();
        item.clickKey = "code";
        item.titleText = mContext.getString(REMAP_TYPE_NAME.get("code"));
        item.deliverHide = false;
        mRemapTypes.add(item);

        item = new ListItem.Common();
        item.clickKey = "activity";
        item.titleText =  mContext.getString(REMAP_TYPE_NAME.get("activity"));
        item.deliverHide = false;
        mRemapTypes.add(item);

        item = new ListItem.Common();
        item.clickKey = "broadcast";
        item.titleText =  mContext.getString(REMAP_TYPE_NAME.get("broadcast"));
        item.deliverHide = true;
        mRemapTypes.add(item);
    }

    public List getRemapTypes() {
        return mRemapTypes;
    }

    public ListItem.Common getRemapType(int position) {
        return mRemapTypes.get(position);
    }

    public KeyMapManager getKeyMapManager() {
        return mKeyMapManager;
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   