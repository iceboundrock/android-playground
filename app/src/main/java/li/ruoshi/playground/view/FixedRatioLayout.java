package li.ruoshi.playground.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 8/28/15.
 */
public class FixedRatioLayout extends RelativeLayout {
    private static final String TAG = "FixedRatioLayout";
    int widthRatio, heightRatio;

    public FixedRatioLayout(Context context) {
        super(context);
    }

    public FixedRatioLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedRatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public FixedRatioLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.FixedRatioLayout, defStyleAttr, 0);
            widthRatio = typedArray.getInteger(R.styleable.FixedRatioLayout_widthRatio, 1);
            heightRatio = typedArray.getInteger(R.styleable.FixedRatioLayout_heightRatio, 1);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.d(TAG, String.format("widthMeasureSpec: %d, %s; heightMeasureSpec: %d, %s",
                widthMeasureSpec,
                MeasureSpec.toString(widthMeasureSpec),
                heightMeasureSpec,
                MeasureSpec.toString(heightMeasureSpec)));


        final boolean portrait = (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        if (portrait) {
            int h = MeasureSpec.getSize(widthMeasureSpec) * heightRatio / widthRatio;
            int newHeightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, newHeightSpec);
        } else {
            final int w = MeasureSpec.getSize(heightMeasureSpec) * widthRatio / heightRatio;
            int newWidthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, heightMeasureSpec);
        }

    }
}
