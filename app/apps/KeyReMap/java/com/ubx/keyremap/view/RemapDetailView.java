package com.ubx.keyremap.view;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.jplus.view.FlowLayout;
import com.ubx.keyremap.IContract.IRemapDetailView;
import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.IntentExtListAdapter;
import com.ubx.keyremap.data.KeyFlowAdapter;
import com.ubx.keyremap.data.ListItem;
import com.ubx.keyremap.presenter.RemapDetailPresenter;

public class RemapDetailView extends BaseView<RemapDetailPresenter> implements IRemapDetailView {

    private static final String TAG = Utils.TAG + "#" + RemapDetailView.class.getSimpleName();

    protected View mFragmentView;

    private RelativeLayout mRemapLayout;

    private MenuBuilder mActionDownMenu;
    private RelativeLayout mActionDownMenuLayout;

    private MenuBuilder mActionUpMenu;
    private RelativeLayout mActionUpMenuLayout;

    private MenuBuilder mWakeUpMenu;
    private RelativeLayout mWakeupMenuLayout;

    public RemapDetailView(Activity context) {
        mContext = context;
    }

    final class MenuBuilder implements View.OnClickListener {
        RelativeLayout mRootLayout;

        RelativeLayout mBodyLayout;
        TextView mTitleView;
        TextView mSummaryView;

        Switch mSwitch;
        RelativeLayout mMenuView;

        String mTitle = "";
        String mSummary = "";

        boolean isMenuEnabled = false;
        MenuListener mMenuListener;

        TranslateAnimation mShowAction;
        TranslateAnimation mHiddenAction;

        MenuBuilder(RelativeLayout rootLayout, String title, String summary, boolean menuEnabled) {
            if (rootLayout == null)
                return;
            mTitle = title;
            mSummary = summary;
            isMenuEnabled = menuEnabled;

            mRootLayout = rootLayout;
            mBodyLayout = (RelativeLayout) mRootLayout.findViewById(R.id.title_ly);
            mBodyLayout.setOnClickListener(this);

            mTitleView = (TextView) mBodyLayout.findViewById(R.id.title);
            mTitleView.setText(mTitle);
            mSummaryView = (TextView) mBodyLayout.findViewById(R.id.summary);
            mSummaryView.setText(mSummary);

            mSwitch = (Switch) mBodyLayout.findViewById(R.id.enable_st);
            mSwitch.setChecked(false);

            mMenuView = (RelativeLayout) mRootLayout.findViewById(R.id.settings_ly);
            mMenuView.setOnClickListener(this);

            mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            mShowAction.setDuration(500);

            mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            mHiddenAction.setDuration(500);
            mHiddenAction.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mMenuView.setVisibility(View.GONE);
                    mSwitch.setChecked(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        void setSummary(String summary) {
            if (mSummaryView != null && summary != null) {
                mSummaryView.setText(summary);
            }
        }

        boolean isChecked() {
            return mSwitch.isChecked();
        }

        void setListener(MenuListener listener) {
            mMenuListener = listener;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.title_ly: {
                    if (mSwitch.isChecked()) {
                        mSummaryView.setText(mSummary);
                        if (isMenuEnabled) {
                            mMenuView.startAnimation(mHiddenAction);
                        } else {
                            mSwitch.setChecked(false);
                        }
                    } else {
                        if (mMenuListener != null) {
                            mSummaryView.setText(mMenuListener.updateSummary());
                        }
                        mSwitch.setChecked(true);
                        if (isMenuEnabled) {
                            mMenuView.setVisibility(View.VISIBLE);
                            mMenuView.startAnimation(mShowAction);
                        }
                    }
                }
                break;
                case R.id.settings_ly: {
                    if (mMenuListener != null) {
                        mMenuListener.onMenuClick();
                    }
                }
                break;
            }
        }
    }

    interface MenuListener {
        void onMenuClick();

        String updateSummary();
    }

    @Override
    public void initViews() {
        mRemapLayout = (RelativeLayout) mContext.findViewById(R.id.key_remap_ly);
        mRemapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.doRemap();
            }
        });

        mActionDownMenuLayout = (RelativeLayout) mContext.findViewById(R.id.action_down_ly);
        mActionDownMenu = new MenuBuilder(mActionDownMenuLayout,
                mContext.getString(R.string.title_action_down_broadcast),
                mContext.getString(R.string.summary_action_enable_alert), true);
        mActionDownMenu.setListener(new MenuListener() {
            @Override
            public void onMenuClick() {
                mPresenter.startEditBroadcastActivity(RemapDetailPresenter.EDIT_KEY_DOWN_BROADCAST);
            }

            @Override
            public String updateSummary() {
                String action = mPresenter.getKeyDownAction();
                return action == null ? mContext.getString(R.string.summary_action_edit_alert) : action;
            }
        });

        mActionUpMenuLayout = (RelativeLayout) mContext.findViewById(R.id.action_up_ly);
        mActionUpMenu = new MenuBuilder(mActionUpMenuLayout,
                mContext.getString(R.string.title_action_up_broadcast),
                mContext.getString(R.string.summary_action_enable_alert), true);
        mActionUpMenu.setListener(new MenuListener() {
            @Override
            public void onMenuClick() {
                mPresenter.startEditBroadcastActivity(RemapDetailPresenter.EDIT_KEY_UP_BROADCAST);
            }

            @Override
            public String updateSummary() {
                String action = mPresenter.getKeyUpAction();
                return action == null ? mContext.getString(R.string.summary_action_edit_alert) : action;
            }
        });

        mWakeupMenuLayout = (RelativeLayout) mContext.findViewById(R.id.wakeup_ly);
        mWakeUpMenu = new MenuBuilder(mWakeupMenuLayout,
                mContext.getString(R.string.title_wakeup_enable),
                mContext.getString(R.string.summary_wakeup_enable_alert), false);
        mWakeUpMenu.setListener(new MenuListener() {
            @Override
            public void onMenuClick() {

            }

            @Override
            public String updateSummary() {
                return mContext.getString(R.string.summary_wakeup_disable_alert);
            }
        });
    }

    @Override
    public void setFragment(Fragment fragment) {
        if (fragment != null) {
            mContext.getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void setFragmentView(View view) {
        mFragmentView = view;
    }

    @Override
    public void setMapCodeName(int code, String name) {

    }

    @Override
    public void setAppContent(Drawable icon, String label, String pkg) {

    }

    @Override
    public void setBroadcastSummary(String down, String up) {
        mActionDownMenu.setSummary(down);
        mActionUpMenu.setSummary(up);
    }

    @Override
    public void notifyIntentExtList() {

    }

    @Override
    public int wakeupEnable() {
        return mWakeUpMenu.isChecked() ? 1 : 0;
    }

    @Override
    public void setWakeUpVisibility(boolean visible){
        if(mWakeupMenuLayout != null && visible) {
            mWakeupMenuLayout.setVisibility(View.VISIBLE);
        } else if(mWakeupMenuLayout != null && !visible){
            mWakeupMenuLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean isKeyDownBroadcastEnable() {
        return mActionDownMenu.isChecked();
    }

    @Override
    public boolean isKeyUpBroadcastEnable() {
        return mActionUpMenu.isChecked();
    }

    private static class MapAsCodeView extends RemapDetailView implements FlowLayout.OnItemClickListener {

        private static final String TAG = Utils.TAG + "#" + MapAsCodeView.class.getSimpleName();

        private static final int MAX_SHOW_FLOW_LINES = 4;

        //view of code
        private FlowLayout mKeyNamesView;
        private TextView mKeyNameView;
        private TextView mKeyCodeView;

        public MapAsCodeView(Activity context) {
            super(context);
        }

        @Override
        public void setFragmentView(View view) {
            super.setFragmentView(view);
            mKeyNamesView = (FlowLayout) view.findViewById(R.id.key_names_ly);
            mKeyNamesView.setShowLines(MAX_SHOW_FLOW_LINES);
            mKeyNamesView.setItemDefaultDrawable(mContext.getDrawable(R.drawable.key_name_unpress_bg));
            mKeyNamesView.setItemPressedDrawable(mContext.getDrawable(R.drawable.key_name_press_bg));
            mKeyNamesView.setAdapter(new KeyFlowAdapter(mContext, mPresenter.getAllKeyNames()));
            mKeyNamesView.setOnItemClickListener(this);
            mKeyNameView = (TextView) view.findViewById(R.id.fg_key_name);
            mKeyCodeView = (TextView) view.findViewById(R.id.fg_key_code);
        }

        @Override
        public void onItemClick(View view, int position) {
            String keyName = mPresenter.findKeyName(position);
            mPresenter.setMapCodeName(mPresenter.findKeyCode(keyName), keyName);
            mPresenter.refreshUI();
            //Toast.makeText(mContext, "Key " + keyName + " clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void setMapCodeName(int code, String name) {
            if (mKeyCodeView != null) {
                mKeyCodeView.setText(mContext.getString(R.string.selected_key_code) + ": " + code);
            }
            if (mKeyNameView != null) {
                mKeyNameView.setText(mContext.getString(R.string.selected_key_name) + ": " + name);
            }
        }
    }

    private static class MapAsActivityView extends RemapDetailView implements AdapterView.OnItemLongClickListener, View.OnClickListener {

        private static final String TAG = Utils.TAG + "#" + MapAsActivityView.class.getSimpleName();

        private static final int MAX_SHOW_LIST_LINES = 3;

        //view of start activity
        private ImageView mAppIconView;
        private TextView mAppLabelView;
        private TextView mAppClassView;
        private RelativeLayout mAppSelectLayout;

        private RelativeLayout mIntentAddView;
        private ListView mIntentExtList;
        private IntentExtListAdapter mListAdapter;

        public MapAsActivityView(Activity context) {
            super(context);
        }

        @Override
        public void setFragmentView(View view) {
            super.setFragmentView(view);
            mAppIconView = (ImageView) view.findViewById(R.id.fg_activity_icon);
            mAppLabelView = (TextView) view.findViewById(R.id.fg_activity_label);
            mAppClassView = (TextView) view.findViewById(R.id.fg_activity_class);
            mAppSelectLayout = (RelativeLayout) view.findViewById(R.id.fg_activity_ly);
            mAppSelectLayout.setOnClickListener(this);

            mIntentAddView = (RelativeLayout) view.findViewById(R.id.fg_intent_ly);
            mIntentAddView.setOnClickListener(this);
            mIntentExtList = (ListView) mContext.findViewById(R.id.fg_intent_list);
            mListAdapter = new IntentExtListAdapter(mContext, mPresenter.getIntentExtras());
            mIntentExtList.setAdapter(mListAdapter);
            mIntentExtList.setOnItemLongClickListener(this);
        }

        @Override
        public void setAppContent(Drawable icon, String label, String pkg) {
            if (mAppIconView != null && icon != null) {
                mAppIconView.setImageDrawable(icon);
            }
            if (mAppLabelView != null && label != null) {
                mAppLabelView.setText(label);
            }
            if (mAppClassView != null && pkg != null) {
                mAppClassView.setText(pkg);
            }
        }

        @Override
        public void notifyIntentExtList() {
            if (mListAdapter != null) {
                reSetListViewHeight();
                mListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            final int pos = position;
            new SimpleDialog(mContext, R.string.dialog_title_attention, R.string.dialog_message_intent_delete) {
                @Override
                public void onDialogOK() {
                    mPresenter.removeIntentExtra(pos);
                }
            }.show();
            return true;
        }

        private void reSetListViewHeight() {
            if (mIntentExtList == null) {
                return;
            }

            ListAdapter listAdapter = mIntentExtList.getAdapter();
            if (listAdapter == null) {
                return;
            }

            LinearLayout.LayoutParams layoutParams = null; //进行布局参数的设置
            int layout_width = ViewGroup.LayoutParams.MATCH_PARENT;
            int layout_height = ViewGroup.LayoutParams.WRAP_CONTENT;

            int itemCount = listAdapter.getCount();
            if (itemCount >= MAX_SHOW_LIST_LINES) {
                View itemView = listAdapter.getView(0, null, mIntentExtList);
                itemView.measure(0, 0);
                int itemHeight = itemView.getMeasuredHeight(); //一项的高度
                layout_height = itemHeight * MAX_SHOW_LIST_LINES;
            }

            layoutParams = new LinearLayout.LayoutParams(layout_width, layout_height);
            mIntentExtList.setLayoutParams(layoutParams);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fg_activity_ly: {
                    mPresenter.startPickActivity();
                    break;
                }
                case R.id.fg_intent_ly: {
                    new ExtraAddDialog(mContext, R.string.intent_ext) {
                        @Override
                        public void onDialogOK(String key, String value) {
                            ListItem.IntentExtra intentExtra = new ListItem.IntentExtra();
                            intentExtra.key = key;
                            intentExtra.value = value;
                            intentExtra.deliverHide = true;
                            mPresenter.addIntentExtra(intentExtra);
                            mIntentExtList.smoothScrollToPosition(mListAdapter.getCount() - 1);
                        }
                    }.show();
                    break;
                }
            }
        }
    }

    public static class Builder {
        private Activity mContext;

        public Builder(Activity context) {
            mContext = context;
        }

        public RemapDetailView create(String type) {
            if (type != null && !type.isEmpty()) {
                switch (type) {
                    case "code":
                        return new MapAsCodeView(mContext);
                    case "activity":
                        return new MapAsActivityView(mContext);
                    case "broadcast":
                        return new RemapDetailView(mContext);
                }
            }
            return null;
        }
    }
}
