package li.ruoshi.playground.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ruoshili on 5/8/15.
 */
public class PathView extends View {
    private static final String TAG = PathView.class.getSimpleName();

    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Path path = new Path();       // your path
    Paint paint = new Paint();    // your paint
    PointF beginPoint;

    private static final int PointCounts = 30;

    private void init() {

        Log.d(TAG, "init");

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(px);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout");
        if (!changed) {
            return;
        }

        super.onLayout(changed, left, top, right, bottom);
        if (pathData == null) {
            pathData = generatePoints(PointCounts, right - left, bottom - top);
            beginPoint = pathData.get(0);
        }
        path.moveTo(beginPoint.x, beginPoint.y);

    }


    List<PointF> generatePoints(final int count, final int maxX, final int maxY) {
        Log.d(TAG, String.format("generatePoints, count: %d, maxX: %d, maxY: %d", count, maxX, maxY));
        ArrayList<PointF> ret = new ArrayList<>(count);
        Random r = new Random(System.currentTimeMillis());
        PointF prevPoint = new PointF(0, 0);
        for (int i = 0; i < count; i++) {
            PointF p = new PointF(prevPoint.x + r.nextInt(maxX / count),
                    prevPoint.y + r.nextInt(maxY / count));
            ret.add(p);
            prevPoint = p;
        }

        return ret;
    }


    List<PointF> pathData;

    static final int FramePerSecond = 60;

    private int step;

    @Override
    protected void onDraw(Canvas canvas) {
        if (step >= PointCounts) {
            return;
        }
        PointF prevViewPoint = pathData.get(step);
        PointF currViewPoint = pathData.get(++step);

        Log.d(TAG, "onDraw, Step: " + step + "next point: " + currViewPoint);


        path.quadTo(prevViewPoint.x, prevViewPoint.y, (currViewPoint.x + prevViewPoint.x) / 2, (currViewPoint.y + prevViewPoint.y) / 2);
        //path.moveTo(currViewPoint.x, currViewPoint.y);

        canvas.drawPath(path, paint);
        super.onDraw(canvas);
        if (step < PointCounts - 1) {

            postInvalidateDelayed(1000 / FramePerSecond,
                    (int)beginPoint.x,
                    (int)beginPoint.y,
                    (int)currViewPoint.x,
                    (int)currViewPoint.y);
            //postInvalidateDelayed(1000 / FramePerSecond);
        }

    }

    public void redraw() {
        step = 0;
        invalidate();
    }
}
