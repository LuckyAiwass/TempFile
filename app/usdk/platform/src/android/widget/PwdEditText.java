package android.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by luolin on 2017/8/4.
 */
public class PwdEditText extends RelativeLayout{

    private EditText editText; //文本编辑框
    private Context context;

    private LinearLayout linearLayout; //文本密码的文本
    private TextView[] textViews; //文本数组

    private int pwdlength = 6; //密码长度， 默认6

    private boolean mShowLine = false;

    private OnTextFinishListener onTextFinishListener;

    private TransformationMethod mTransformationMethod;

    public PwdEditText(Context context) {
        this(context, null);
    }

    public PwdEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PwdEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * @param bgdrawable 背景drawable
     * @param pwdlength 密码长度
     * @param splilinewidth 分割线宽度
     * @param splilinecolor 分割线颜色
     * @param pwdcolor 密码字体颜色
     * @param pwdsize 密码字体大小
     */
    public void initStyle(int bgdrawable, int pwdlength, float splilinewidth, int splilinecolor, boolean showline, int pwdcolor, int pwdsize)
    {
        this.mShowLine = showline;
        this.pwdlength = pwdlength;
        initEdit(bgdrawable, pwdcolor, pwdsize);
        initShowInput(bgdrawable, pwdlength, splilinewidth, splilinecolor, pwdcolor, pwdsize);
    }

    /**
     * 初始化编辑框
     * @param bgcolor
     */
    private void initEdit(int bgcolor, int pwdcolor, int pwdsize)
    {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        editText = new EditText(context);
        editText.setBackgroundResource(bgcolor);
        editText.setCursorVisible(false);

        editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(pwdlength)});

        if(mShowLine){
            editText.setTextSize(0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        }else{
            editText.setTextColor(context.getResources().getColor(pwdcolor));
            editText.setTextSize(pwdsize);
            editText.setGravity(Gravity.CENTER);
            editText.setPadding(0, 0, 0, 0);
            editText.setMaxLines(1);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Editable etext = editText.getText();
                Selection.setSelection(etext, etext.length());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initDatas(s);
                if(s.length() == pwdlength)
                {
                    if(onTextFinishListener != null)
                    {
                        onTextFinishListener.onFinish(s.toString().trim());
                    }
                }
            }
        });

        addView(editText, lp);

    }

    /**
     * @param bgcolor 背景drawable
     * @param pwdlength 密码长度
     * @param slpilinewidth 分割线宽度
     * @param splilinecolor 分割线颜色
     * @param pwdcolor 密码字体颜色
     * @param pwdsize 密码字体大小
     */
    public void initShowInput(int bgcolor, int pwdlength, float slpilinewidth, int splilinecolor, int pwdcolor, int pwdsize)
    {
        //添加密码框父布局
        linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(bgcolor);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout);

        //添加密码框
        textViews = new TextView[pwdlength];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT);
        params.weight = 1;
        params.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(dip2px(context, slpilinewidth),LayoutParams.MATCH_PARENT);
        for(int i = 0; i < textViews.length; i++)
        {
            final int index = i;
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textViews[i] = textView;
            textViews[i].setTextSize(pwdsize);
            textViews[i].setTextColor(context.getResources().getColor(pwdcolor));
            textViews[i].setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
            linearLayout.addView(textView, params);


            if(i < textViews.length - 1)
            {
                View view = new View(context);
                view.setBackgroundColor(context.getResources().getColor(splilinecolor));
                linearLayout.addView(view, params2);
            }
        }

        if(!mShowLine){
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 是否显示明文
     * @param showPwd
     */
    public void setShowPwd(boolean showPwd) {
        int length = textViews.length;
        if(mShowLine) {
            for (int i = 0; i < length; i++) {
                if (showPwd) {
                    textViews[i].setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    if (mTransformationMethod != null) {
                        textViews[i].setTransformationMethod(mTransformationMethod);
                    } else {
                        textViews[i].setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }
                }
            }
        }else{
            if (showPwd) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                if (mTransformationMethod != null) {
                    editText.setTransformationMethod(mTransformationMethod);
                } else {
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        }
    }

    /**
     * 设置显示类型
     * @param type
     */
    public void setInputType(int type)
    {
        int length = textViews.length;
        for(int i = 0; i < length; i++) {
            textViews[i].setInputType(type);
        }
        editText.setInputType(type);
    }

	/**
     * 清除文本框
     */
    public void clearText()
    {
        editText.setText("");
        for(int i = 0; i < pwdlength; i++)
        {
            textViews[i].setText("");
        }
    }

    public void setOnTextFinishListener(OnTextFinishListener onTextFinishListener) {
        this.onTextFinishListener = onTextFinishListener;
    }

    /**
     * 根据输入字符，显示密码个数
     * @param s
     */
    public void initDatas(Editable s)
    {
        if(s.length() > 0)
        {
            int length = s.length();
            for(int i = 0; i < pwdlength; i++)
            {
                if(i < length)
                {
                    for(int j = 0; j < length; j++)
                    {
                        char ch = s.charAt(j);
                        textViews[j].setText(String.valueOf(ch));
                    }
                }
                else
                {
                    textViews[i].setText("");
                }
            }
        }
        else
        {
            for(int i = 0; i < pwdlength; i++)
            {
                textViews[i].setText("");
            }
        }
    }

    public String getText()
    {
        if(editText != null)
            return editText.getText().toString().trim();
        return "";
    }

    public void setText(String s)
    {
        if(editText != null && s != null)
             editText.setText(s);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnTextFinishListener
    {
        void onFinish(String str);
    }

    public void setTransformationMethod(TransformationMethod transformationMethod){
        mTransformationMethod = transformationMethod;
        setShowPwd(false);
    }

}
