package com.hz.android.imageselector.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 可设置宽高比例的自定义ImageView
 * Created by Administrator on 2018/1/29.
 */

public class RatioImageView extends ImageView {

    private float ratio;//宽高比
    private boolean isKnowWidth;

    public RatioImageView(Context context) {
        this(context, null);
    }

    public RatioImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //高度
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (isKnowWidth) {
            if (width <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);// 走默认逻辑 直到测量出宽度
                return;
            }
            height = (int) (width / ratio);
        } else {
            if (height <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            width = (int) (height * ratio);
        }

        setMeasuredDimension(width, height);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RatioImageView);
        ratio = typedArray.getFloat(R.styleable.RatioImageView_ratio, 1.0f);
        isKnowWidth = typedArray.getBoolean(R.styleable.RatioImageView_is_know_view_width, true);
        typedArray.recycle();
    }

}
