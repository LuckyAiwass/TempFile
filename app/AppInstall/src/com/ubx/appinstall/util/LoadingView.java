package com.ubx.appinstall.util;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubx.appinstall.R;

public class LoadingView extends Dialog {

    private String content;
    private ImageView imageView;
    Animation animation ;
    public LoadingView(Context context, String content) {
        super(context);
        this.content=content;
        initView();
        animation = AnimationUtils.loadAnimation(context, R.anim.loading_animation);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
               return true;
        }
        return true;
    }

    private void initView(){
        setContentView(R.layout.dialog_loading);
        imageView = (ImageView) findViewById(R.id.loadingImg);
        ((TextView)findViewById(R.id.tvcontent)).setText(content);
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha=0.8f;
        getWindow().setAttributes(attributes);
        setCancelable(false);
    }

    public void start(){
        imageView.startAnimation(animation);
    }
    public void stop(){
        imageView.clearAnimation();
    }
}
