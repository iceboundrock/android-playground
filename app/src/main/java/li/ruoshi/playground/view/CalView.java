package li.ruoshi.playground.view;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 7/1/15.
 */
public class CalView extends View {
    private static final String TAG = CalView.class.getSimpleName();

    private Paint headerTextPaint;
    private Paint dateTextPaint;

    private Paint todayCyclePaint;
    private Paint selectedCyclePaint;
    private Paint prevSelectedPointPaint;

    private float calMargin, headerCellWidth, headerCellHeight, cellRadius;
    private float dateCellWidth, dateCellHeight;


    private final List<CalCell> headerCells = new ArrayList<>(7);
    private final List<CalCell> dateCells = new ArrayList<>(5 * 7); // 5 rows x 7 days

    final float yOffset = 200f;

    private boolean showHeader;

    private float dateTextSize;
    private int rowCount;
    private int todayCellBgColor;
    private int selectedCellBgColor;
    private int prevSelectedCellColor;
    private int prevSelectedCellOffsetY;
    private int headerTextSize;
    private int headerTextColor;
    private int prevSelectedCellRadius;
    private int dateTextColor;
    private int weeksFromToday;
    private int rowSpace;
    private int selectedCellTextColor;
    private int dateToHeaderSpace;


    private static class CalCell {
        public final float x;
        public final float y;
        public final float r;
        public final DateTime date;

        public final String title;

        public final boolean disabled;

        public CalCell(float x, float y, float r, String title) {
            this(x, y, r, null, title);
        }

        public CalCell(float x, float y, float r, DateTime date) {
            this(x, y, r, date, "");
        }

        private CalCell(float x, float y, float r, DateTime date, String title) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.date = date;
            this.title = title;

            disabled = date == null || date.isBeforeNow();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CalCell calCell = (CalCell) o;

            boolean sameDate = !(date != null ? !(date.getDayOfYear() == calCell.date.getDayOfYear())
                    : calCell.date != null);
            boolean sameTitle = !(title != null ? !title.equals(calCell.title) : calCell.title != null);

            return (date != null && sameDate) || ((!TextUtils.isEmpty(title)) && sameTitle);
        }

        @Override
        public int hashCode() {
            int result = date != null ? date.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CalCell{" +
                    "date=" + date +
                    ", x=" + x +
                    ", y=" + y +
                    ", r=" + r +
                    ", title='" + title + '\'' +
                    ", disabled=" + disabled +
                    '}';
        }
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.CalView, defStyleAttr, 0);
            showHeader = typedArray.getBoolean(
                    R.styleable.CalView_showHeader, false);

            headerTextSize = typedArray.getDimensionPixelSize(R.styleable.CalView_headerTextSize,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_SP, 12));

            headerTextColor = typedArray.getColor(R.styleable.CalView_headerTextColor,
                    getResources().getColor(R.color.cal_header_text_color));

            calMargin = typedArray.getDimensionPixelSize(R.styleable.CalView_calMargin,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_DIP, 22));

            dateTextSize = typedArray.getDimensionPixelSize(R.styleable.CalView_dateTextSize,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_SP, 15));

            rowCount = typedArray.getColor(
                    R.styleable.CalView_rowCount, 1);

            cellRadius = typedArray.getDimensionPixelSize(
                    R.styleable.CalView_cellRadius,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_DIP, 13));

            dateTextColor = typedArray.getColor(R.styleable.CalView_dateTextColor, 0);

            todayCellBgColor = typedArray.getColor(
                    R.styleable.CalView_todayCellBgColor, 0);

            selectedCellBgColor = typedArray.getColor(
                    R.styleable.CalView_selectedCellBgColor, Color.WHITE);

            prevSelectedCellColor = typedArray.getColor(
                    R.styleable.CalView_prevSelectedCellColor, Color.WHITE);

            prevSelectedCellRadius = typedArray.getDimensionPixelSize(
                    R.styleable.CalView_prevSelectedCellRadius,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_DIP, 2));

            prevSelectedCellOffsetY = typedArray.getDimensionPixelOffset(
                    R.styleable.CalView_prevSelectedCellOffsetY,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_DIP, 4));

            weeksFromToday = typedArray.getInt(R.styleable.CalView_weeksFromToday, 0);

            rowSpace = typedArray.getDimensionPixelSize(R.styleable.CalView_rowSpace,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_DIP, 7));

            dateTextSize = typedArray.getDimensionPixelSize(R.styleable.CalView_dateTextSize,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_SP, 15));

            selectedCellTextColor = typedArray.getColor(
                    R.styleable.CalView_selectedCellTextColor, 0);

            dateToHeaderSpace = typedArray.getDimensionPixelSize(R.styleable.CalView_dateToHeaderSpace,
                    (int) getPixels(TypedValue.COMPLEX_UNIT_SP, 10));

        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    public CalView(Context context) {
        super(context);
    }

    public CalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initPaints();
    }


    static float getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(unit, size, metrics);
    }

    static final String[] Headers = new String[]{"日", "一", "二", "三", "四", "五", "六"};

    private void initPaints() {


        selectedCyclePaint = new Paint();
        selectedCyclePaint.setColor(selectedCellBgColor);
        selectedCyclePaint.setStyle(Paint.Style.FILL);
        selectedCyclePaint.setAntiAlias(true);

        headerTextPaint = new Paint();
        headerTextPaint.setColor(headerTextColor);
        headerTextPaint.setAntiAlias(true);
        headerTextPaint.setTextSize(headerTextSize);
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        dateTextPaint = new Paint();
        dateTextPaint.setColor(dateTextColor);
        dateTextPaint.setAntiAlias(true);
        dateTextPaint.setTextSize(dateTextSize);
        dateTextPaint.setTextAlign(Paint.Align.CENTER);
        dateTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


        prevSelectedPointPaint = new Paint();
        prevSelectedPointPaint.setColor(prevSelectedCellColor);
        prevSelectedPointPaint.setAntiAlias(true);
        prevSelectedPointPaint.setStyle(Paint.Style.FILL);

        todayCyclePaint = new Paint();
        todayCyclePaint.setColor(selectedCellBgColor);
        todayCyclePaint.setStyle(Paint.Style.STROKE);
        todayCyclePaint.setAntiAlias(true);
    }

    private boolean cellsInitialized = false;

    private void initCalCells() {
        if (cellsInitialized) {
            return;
        }
        initHeaderCells();
        initDateCells();
        cellsInitialized = true;
    }

    private void initHeaderCells() {
        if (!showHeader) {
            return;
        }

        for (int i = 0; i < Headers.length; i++) {

            float x = calMargin + ((float) i + 0.5f) * headerCellWidth;
            float y = yOffset + headerCellHeight / 2f;

            CalCell cell = new CalCell(x, y, cellRadius, Headers[i]);

            this.headerCells.add(cell);
        }
    }

    private DateTime getFirstCellDate() {
        final DateTime today = DateTime.now();
        final int dayOfWeek = today.getDayOfWeek();
        Log.d(TAG, "dayOfWeek of today: " + dayOfWeek);
        if (dayOfWeek == 7) {
            return today;
        }

        return today.minusDays(dayOfWeek);
    }

    private void initDateCells() {
        if (dateCells.size() != 0) {
            Log.w(TAG, "dateCells is not empty.", new Exception("just for stack trace..."));
            dateCells.clear();
        }

        final DateTime firstCellDate = getFirstCellDate();

        final int cellCount = rowCount * 7;

        final int offset = weeksFromToday * 7;

        for (int i = 0; i < cellCount; i++) {

            float x = calMargin + ((float) (i % 7) + 0.5f) * dateCellWidth;
            float y = yOffset + ((rowSpace + dateCellHeight) * (i / 7))
                    + dateToHeaderSpace + dateCellHeight
                    + dateCellHeight / 2f;

            dateCells.add(new CalCell(x, y, cellRadius, firstCellDate.plusDays(i + offset)));
        }


        // 日 一 二 三 四 五 六
        //  7 1  2 3  4  5 6
    }


    private void drawDateCells(Canvas canvas) {
        Log.d(TAG, "date cells count: " + dateCells.size());

        for (int i = 0; i < dateCells.size(); i++) {

            final CalCell cell = dateCells.get(i);

            if (cell == null) {
                continue;
            }

            final boolean isSelected = cell.equals(selectedCell);

            Log.d(TAG, "current cell: " + cell);
            if (selectedCell != null) {
                Log.d(TAG, "selected cell: " + selectedCell);
            }

            if (isSelected) {
                canvas.drawCircle(selectedCell.x,
                        selectedCell.y - (dateCellHeight / 2f),
                        cellRadius,
                        selectedCyclePaint);
            } else if(cell.date.getDayOfYear() == DateTime.now().getDayOfYear()) {
                canvas.drawCircle(cell.x,
                        cell.y - (dateCellHeight / 2f),
                        cellRadius,
                        todayCyclePaint);
            }



            dateTextPaint.setColor(isSelected ? selectedCellTextColor : dateTextColor);

            Log.d(TAG, "Draw text for " + cell.date + ", isSelected=" + isSelected);
            canvas.drawText(String.format("%02d", cell.date.getDayOfMonth()),
                    cell.x,
                    cell.y,
                    dateTextPaint);
        }
    }

    private void drawHeaders(final Canvas canvas) {
        if (!showHeader) {
            return;
        }

        for (int i = 0; i < headerCells.size(); i++) {

            final CalCell cell = headerCells.get(i);

            if (cell == null) {
                continue;
            }

            canvas.drawText(cell.title,
                    cell.x,
                    cell.y,
                    headerTextPaint);
        }
    }

    Rect textBound = new Rect();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed) {
            return;
        }

        headerTextPaint.getTextBounds(Headers[0], 0, 1, textBound);

        calMargin = getPixels(TypedValue.COMPLEX_UNIT_DIP, 16);
        headerCellWidth = (getWidth() - 2 * calMargin) / Headers.length;
        dateCellWidth = headerCellWidth;

        headerCellHeight = (textBound.bottom - textBound.top);

        dateTextPaint.getTextBounds("00", 0, 2, textBound);
        dateCellHeight = (textBound.bottom - textBound.top);

        initCalCells();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawHeaders(canvas);

        drawDateCells(canvas);


        if (prevSelectedCell != null
                && prevSelectedCell != selectedCell) {
            canvas.drawCircle(prevSelectedCell.x,
                    prevSelectedCell.y + prevSelectedCellOffsetY,
                    prevSelectedCellRadius,
                    prevSelectedPointPaint);
        }

    }

    final static int kTouchState_None = 0;
    final static int kTouchState_Started = 1;
    final static int kTouchState_Moving = 2;
    final static int kTouchState_Stopped = 3;

    TouchInfo touchDown;

    TouchInfo touchUp;

    static class TouchInfo {
        public final int state;
        public final float x;
        public final float y;
        public final long eventTime;


        public TouchInfo(int state, final MotionEvent event) {
            this.state = state;
            x = event.getX();
            y = event.getY();
            eventTime = System.currentTimeMillis();
        }

        public float distanceTo(TouchInfo touchInfo) {
            return distanceTo(touchInfo.x, touchInfo.y);
        }

        public float distanceTo(final float otherX, final float otherY) {
            final float dx = this.x - otherX;
            final float dy = this.y - otherY;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 只处理单指操作
        if (event.getPointerCount() != 1) {
            return super.onTouchEvent(event);
        }

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDown = new TouchInfo(kTouchState_Started, event);
                touchUp = null;
                break;
            case MotionEvent.ACTION_UP:
                touchUp = new TouchInfo(kTouchState_Stopped, event);
                final float d = touchUp.distanceTo(touchDown);
                final float t = touchUp.eventTime - touchDown.eventTime;

                if (d < 100 && t < 200) {
                    Log.d(TAG, "single click");
                    onSingleClick(touchDown);
                }

                touchDown = null;
                touchUp = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                touchDown = null;
                touchUp = null;
                break;
        }


        return true;
    }

    private CalCell todayCell;
    private CalCell selectedCell;
    private CalCell prevSelectedCell;

    private void onSingleClick(final TouchInfo ti) {
        for (int i = 0; i < dateCells.size(); i++) {
            final CalCell cell = dateCells.get(i);
            final float d = ti.distanceTo(cell.x, cell.y);

            if (d < cellRadius) {
                prevSelectedCell = selectedCell;
                selectedCell = cell;

                Log.d(TAG, "on clicked, redraw");
                postInvalidate();
                break;
            }
        }
    }
}
