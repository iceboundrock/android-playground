package li.ruoshi.playground.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import li.ruoshi.playground.R;


/**
 * Created by ruoshili on 4/9/15.
 */
public class RunningDotsBar extends View {
    private static final int MIN_DOT_COUNT = 3;
    private static final float FLOAT_ACCURACY = 0.05f;
    private static final int MIN_CHANGE_DOT_INTERVAL = 300;
    private static final int DEFAULT_CHANGE_DOT_INTERVAL = 300;

    private final Paint paint = new Paint();
    private float dotRadius;
    private int maxDotCount;
    private int updateInterval;
    private int nextDot = 0;

    private float lastCornerRadius = 0f;
    private Drawable innerBackgroundDrawable;

    public RunningDotsBar(Context context) {
        super(context);
    }

    public RunningDotsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RunningDotsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RunningDotsBar);

        //获取自定义属性和默认值
        int dotColor = typedArray.getColor(R.styleable.RunningDotsBar_dotColor, Color.TRANSPARENT);
        int innerBackground = typedArray.getResourceId(R.styleable.RunningDotsBar_innerBackground, 0);
        dotRadius = typedArray.getDimensionPixelSize(R.styleable.RunningDotsBar_dotRadius, 0);
        maxDotCount = typedArray.getInt(R.styleable.RunningDotsBar_maxDotCount, MIN_DOT_COUNT);
        maxDotCount = maxDotCount < MIN_DOT_COUNT ? MIN_DOT_COUNT : maxDotCount;
        updateInterval = typedArray.getInt(R.styleable.RunningDotsBar_updateInterval, DEFAULT_CHANGE_DOT_INTERVAL);
        typedArray.recycle();

        innerBackgroundDrawable = innerBackground == 0 ? null : getResources().getDrawable(innerBackground);
        updateInterval = updateInterval < MIN_CHANGE_DOT_INTERVAL ? MIN_CHANGE_DOT_INTERVAL : updateInterval;

        paint.setColor(dotColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        final int height = getHeight();

        // 画外围的框
        if (innerBackgroundDrawable != null) {
            Rect r = innerBackgroundDrawable.getBounds();
            if (r.left != 0 || r.top != 0 || r.right != width || r.bottom != height) {
                innerBackgroundDrawable.setBounds(0, 0, width, height);
            }
            float cornerRadius = height / 2f;
            if (lastCornerRadius > -1f * FLOAT_ACCURACY
                    && Math.abs(lastCornerRadius - cornerRadius) > FLOAT_ACCURACY) {
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


        float centerX = width / ((float) maxDotCount + 1f) * (float) nextDot;
        final float centerY = height / 2f;

        if (nextDot > 0) {
            canvas.drawCircle(centerX,
                    centerY,
                    dotRadius,
                    paint);
        }

        nextDot++;

        // 如果要画的下一个点是低一个点，就重绘所有区域，如果是画其他点，就只重绘点的区域
        if (nextDot > maxDotCount) {
            nextDot = 0;
            postInvalidateDelayed(updateInterval);
        } else {
            centerX = width / ((float) maxDotCount + 1f) * (float) nextDot;
            int left = (int) (centerX - dotRadius - 1);
            int top = (int) (centerY - dotRadius - 1);
            int right = (int) (centerX + dotRadius + 1);
            int bottom = (int) (centerY + dotRadius + 1);
            postInvalidateDelayed(updateInterval, left, top, right, bottom);
        }

    }
}
