package com.applexis.aimos_android.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.applexis.aimos_android.R;

/**
 * @author applexis
 */

public class TextViewEx extends TextView {

    public TextViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TextViewEx, 0, 0);

        try {
            String font = a.getString(R.styleable.TextViewEx_typeface);
            this.setTypeface(Typeface.createFromAsset(context.getAssets(), font));
        } finally {
            a.recycle();
        }
    }
}
