package com.ubx.keyremap.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.device.KeyMapManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jplus.view.SlideMenuLayout;
import com.ubx.keyremap.R;
import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.presenter.RemapResultPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Allows the listView to display six columns for the database. Depends on res/layout/database_row.xml
 */
public class SextupletAdapter extends BaseAdapter {

    private static final String TAG = Utils.TAG + "#" + SextupletAdapter.class.getSimpleName();

    public static String KEY_TYPE[] = new String[]{"NONE", "KEY_CODE", "KEY_UNICODE", "START_APPLICATION", "SEND_BROADCAST"};

    private Context mContext;
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private KeyMapManager mKeyMapManager;
    private RemapResultPresenter mPresenter;

    /**
     * @param context
     */
    public SextupletAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mKeyMapManager = new KeyMapManager(context);
    }

    /**
     * @param cursor Used to find row values. Column names are currently
     *               hardcoded, and should be changed in KeyboardMapProvider to
     *               be constants.
     */
    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    public void setPresenter(RemapResultPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return -1;
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        if (mCursor != null) {
            return mCursor.getString(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (setRow(position)) {
            int pos = mCursor.getColumnIndex("_id");
            return mCursor.getLong(pos);
        }
        return -1;
    }

    /**
     * Sets the cursor to the given position, if possible.
     *
     * @param position The position to move to.
     * @return True if cursor can move to given position.
     */
    public boolean setRow(int position) {
        if (mCursor != null)
            return mCursor.moveToPosition(position);
        return false;
    }

    public int getItemScanCode(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor.getInt(mCursor.getColumnIndex(KeyMapManager.KEY_SCANCODE));
        }
        return -1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.map_key_list_item, null);
            holder = new ViewHolder();
            // TextView columns to edit.
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.keyCode = (TextView) convertView.findViewById(R.id.key_code_tv);
            holder.keyName = (TextView) convertView.findViewById(R.id.key_name_tv);
            holder.keyType = (TextView) convertView.findViewById(R.id.key_type_tv);
            holder.keyMeta = (TextView) convertView.findViewById(R.id.key_meta_tv);
            holder.keyUpBC = (TextView) convertView.findViewById(R.id.key_up_tv);
            holder.keyDownBC = (TextView) convertView.findViewById(R.id.key_down_tv);
            holder.keyWakeup = (TextView) convertView.findViewById(R.id.key_wakeup_tv);
            holder.keyMetaLayout = (RelativeLayout) convertView.findViewById(R.id.key_meta_ly);
            holder.rootLayout = (LinearLayout) convertView.findViewById(R.id.root_ly);
            holder.menuDelete = (Button) convertView.findViewById(R.id.menu_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // initialize text area and column index for text
        //int scanDex = mCursor.getColumnIndex(KeyMapManager.KEY_SCANCODE);
        int codeDex = mCursor.getColumnIndex(KeyMapManager.KEY_KEYCODE);
        int wakeupDex = mCursor.getColumnIndex(KeyMapManager.KEY_WAKE);
        int codeMetaDex = mCursor.getColumnIndex(KeyMapManager.KEY_KEYCODE_META);
        int activityDex = mCursor.getColumnIndex(KeyMapManager.KEY_ACTIVITY);
        //int characterDex = mCursor.getColumnIndex(KeyMapManager.KEY_CHARACTER);
        int broadcastDex = mCursor.getColumnIndex(KeyMapManager.KEY_BROADCAST);
        int typeDex = mCursor.getColumnIndex(KeyMapManager.KEY_TYPE);

        // set text to text area, from cursor, using column index
        if (setRow(position)) {
            //int scanCode = mCursor.getInt(scanDex);
            int keyCode = mCursor.getInt(codeDex);
            int keyType = mCursor.getInt(typeDex);

            holder.keyCode.setText(mContext.getString(R.string.map_key_code, String.valueOf(keyCode)));
            holder.keyName.setText(mContext.getString(R.string.map_key_name, getKeyName(keyCode)));
            holder.keyType.setText(mContext.getString(R.string.map_key_type, KEY_TYPE[keyType]));

            int keyCodeMeta = mCursor.getInt(codeMetaDex);

            holder.icon.setImageDrawable(null);
            holder.keyMetaLayout.setVisibility(View.VISIBLE);

            switch (keyType) {
                case KeyMapManager.KEY_TYPE_KEYCODE: {
                    if (keyCodeMeta > 0) {
                        holder.keyMeta.setText(mContext.getString(R.string.map_key_meta_code, getKeyName(keyCodeMeta)));
                    } else {
                        holder.keyMeta.setText(mContext.getString(R.string.map_key_meta_code, "UNKNOW"));
                    }
                    holder.icon.setVisibility(View.GONE);
                    holder.rootLayout.setBackground(mContext.getDrawable(R.drawable.key_mapped_item_bg_c));
                    break;
                }
                case KeyMapManager.KEY_TYPE_STARTAC: {
                    String compStr = getACAction(mCursor.getString(activityDex));
                    ComponentName componentName = mKeyMapManager.getComponentNameFromString(compStr);
                    if (componentName == null) {
                        componentName = getMainComponentName(compStr.trim());
                    }
                    if (componentName != null) {
                        holder.keyMeta.setText(componentName.flattenToShortString());
                        try {
                            holder.icon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(componentName.getPackageName()));
                        } catch (final PackageManager.NameNotFoundException e) {
                            ULog.e(TAG, "getApplicationIcon Error:", e);
                        }
                    } else {
                        holder.icon.setImageResource(android.R.drawable.ic_dialog_dialer);
                        holder.keyMeta.setText("UNKNOW");
                    }
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.rootLayout.setBackground(mContext.getDrawable(R.drawable.key_mapped_item_bg_b));
                    break;
                }
                default: {
                    holder.keyMetaLayout.setVisibility(View.GONE);
                    holder.rootLayout.setBackground(mContext.getDrawable(R.drawable.key_mapped_item_bg_a));
                    break;
                }
            }

            String downBroadcast = getBCAction(mCursor.getString(broadcastDex), KeyMapManager.KEY_DOWN_BROADCAST);
            if (downBroadcast != null && !downBroadcast.equals("")) {
                holder.keyDownBC.setText(mContext.getString(R.string.map_key_down_bc, downBroadcast));
                holder.keyDownBC.setVisibility(View.VISIBLE);
            } else {
                holder.keyDownBC.setVisibility(View.GONE);
            }

            String upBroadcast = getBCAction(mCursor.getString(broadcastDex), KeyMapManager.KEY_UP_BROADCAST);
            if (upBroadcast != null && !upBroadcast.equals("")) {
                holder.keyUpBC.setText(mContext.getString(R.string.map_key_up_bc, upBroadcast));
                holder.keyUpBC.setVisibility(View.VISIBLE);
            } else {
                holder.keyUpBC.setVisibility(View.GONE);
            }

            holder.keyWakeup.setText(mContext.getString(R.string.map_key_wakeup, String.valueOf(mCursor.getInt(wakeupDex))));

            final SlideMenuLayout slideBody = (SlideMenuLayout) convertView;
            // slideBody.setLeftSlide(false);
            slideBody.setIOS(false);
            holder.menuDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long id = getItemId(position);
                    if (id != -1 && mPresenter != null) {
                        slideBody.quickClose();
                        mPresenter.deleteAll("_id = " + id);
                        if (getItemScanCode(position) == 217) {
                            mPresenter.notify1aAChanged();
                        }
                    }
                }
            });
        }
        return convertView;
    }

    private String getKeyName(int code) {
        return mKeyMapManager.getKeyFieldNames().get(code, "UNKNOW");
    }

    private String getACAction(String activityInfo) {
        if (activityInfo == null || activityInfo.isEmpty()) {
            return null;
        }
        JSONObject jsonObject = mKeyMapManager.getJSONObjectFromString(activityInfo);
        if (jsonObject != null) {
            try {
                return jsonObject.getString(KeyMapManager.KEY_INTENT_ACTION);
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    private String getBCAction(String broadcastInfo, String type) {
        if (broadcastInfo == null ||
                (!KeyMapManager.KEY_DOWN_BROADCAST.equals(type) &&
                        !KeyMapManager.KEY_UP_BROADCAST.equals(type))) {
            return null;
        }
        JSONObject jsonObject = mKeyMapManager.getJSONObjectFromString(broadcastInfo);
        if (jsonObject != null) {
            try {
                JSONObject bcObject = jsonObject.getJSONObject(type);
                return bcObject.getString(KeyMapManager.KEY_INTENT_ACTION);
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    private ComponentName getMainComponentName(String packageName) {
        if (packageName == null) {
            return null;
        }
        Intent baseIntent = new Intent("android.intent.action.MAIN", null);
        baseIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> appList = mContext.getPackageManager().queryIntentActivities(baseIntent, 0);
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                return new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            }
        }
        return null;
    }

    class ViewHolder {
        ImageView icon;
        TextView keyCode;
        TextView keyName;
        TextView keyType;
        TextView keyMeta;
        TextView keyDownBC;
        TextView keyUpBC;
        TextView keyWakeup;
        RelativeLayout keyMetaLayout;
        LinearLayout rootLayout;

        Button menuDelete;
    }
}
