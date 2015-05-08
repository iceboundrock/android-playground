package li.ruoshi.playground.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import li.ruoshi.playground.R;


/**
 * Created by ruoshili on 4/9/15.
 */
public class DotProgressBar extends View {
    private Drawable innerBackgroundDrawable;

    public DotProgressBar(Context context) {
        super(context);
    }

    public DotProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    final Handler handler = new Handler();
    private static final int MIN_DOT_COUNT = 3;
    private static final float FLOAT_ACCURACY = 0.05f;
    private static final int MIN_CHANGE_DOT_INTERVAL = 300;
    private static final int DEFAULT_CHANGE_DOT_INTERVAL = 300;

    private final Paint paint = new Paint();
    private float dotRadius;
    private int dotCount;
    private int maxDotCount;
    private int changeDotInterval;

    private float lastCornerRadius = 0f;


    public DotProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.DotProgressBar);

        //获取自定义属性和默认值
        int dotColor = typedArray.getColor(R.styleable.DotProgressBar_dotColor, Color.TRANSPARENT);
        int innerBackground = typedArray.getResourceId(R.styleable.DotProgressBar_innerBackground, 0);
        dotRadius = typedArray.getDimensionPixelSize(R.styleable.DotProgressBar_dotRadius, 0);
        maxDotCount = typedArray.getInt(R.styleable.DotProgressBar_maxDotCount, MIN_DOT_COUNT);
        maxDotCount = maxDotCount < MIN_DOT_COUNT ? MIN_DOT_COUNT : maxDotCount;
        changeDotInterval = typedArray.getInt(R.styleable.DotProgressBar_changeDotInterval, DEFAULT_CHANGE_DOT_INTERVAL);
        typedArray.recycle();

        innerBackgroundDrawable = innerBackground == 0 ? null : getResources().getDrawable(innerBackground);
        changeDotInterval = changeDotInterval < MIN_CHANGE_DOT_INTERVAL ? MIN_CHANGE_DOT_INTERVAL : changeDotInterval;

        paint.setColor(dotColor);
        paint.setStyle(Paint.Style.FILL);

        changeDotCount();
    }




    private void changeDotCount() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getVisibility() == VISIBLE) {
                    dotCount = ++dotCount % (maxDotCount + 1);
                    invalidate();
                    changeDotCount();
                } else {
                    dotCount = 0;
                }
            }
        }, changeDotInterval);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == VISIBLE) {
            changeDotCount();
        } else {
            dotCount = 0;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        final int height = getHeight();

        // 画外围的框
        if(innerBackgroundDrawable != null) {
            Rect r = innerBackgroundDrawable.getBounds();
            if (r.left != 0 || r.top != 0 || r.right != width || r.bottom != height) {
                innerBackgroundDrawable.setBounds(0, 0, width, height);
            }
            float cornerRadius = height / 2f;
            if (lastCornerRadius > -1 * FLOAT_ACCURACY && Math.abs(lastCornerRadius - cornerRadius) > FLOAT_ACCURACY) {
                if (innerBackgroundDrawable instanceof GradientDrawable) {
                    GradientDrawable gd = (GradientDrawable) innerBackgroundDrawable;
                    gd.setCornerRadius(cornerRadius);
                    lastCornerRadius = cornerRadius;
                } else {
                    lastCornerRadius = -1f;
                }
            }
            innerBackgroundDrawable.draw(canvas);
        }

        if (dotCount < 1) {
            return;
        }

        dotCount = dotCount > maxDotCount ? maxDotCount : dotCount;

        for (int i = 1; i <= dotCount; i++) {
            canvas.drawCircle(width / ((float) maxDotCount + 1f) * (float) i,
                    height / 2f,
                    dotRadius,
                    paint);
        }
    }
}
