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
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 7/1/15.
 */
public class CalView extends View {
    static final String[] Headers = new String[]{"日", "一", "二", "三", "四", "五", "六"};
    final static int kTouchState_None = 0;
    final static int kTouchState_Started = 1;
    final static int kTouchState_Moving = 2;
    final static int kTouchState_Stopped = 3;
    private static final String TAG = CalView.class.getSimpleName();
    private final List<CalCell> headerCells = new ArrayList<>(7);
    private final List<DateCell> dateCells = new ArrayList<>(5 * 7); // 5 rows x 7 days
    Rect textBound = new Rect();
    TouchInfo touchDown;
    TouchInfo touchUp;
    private Paint headerTextPaint;
    private Paint dateTextPaint;
    private Paint todayCyclePaint;
    private Paint selectedCyclePaint;
    private Paint prevSelectedPointPaint;
    private float calMargin, headerCellWidth, headerCellHeight, cellRadius;
    private float dateCellWidth, dateCellHeight;
    private boolean showHeader;
    private float dateTextSize;
    private int minRowCount;
    private int maxRowCount;
    private int todayCellBgColor;
    private int selectedCellBgColor;
    private int prevSelectedCellColor;
    private int prevSelectedCellOffsetY;
    private int headerTextSize;
    private int headerTextColor;
    private int prevSelectedCellRadius;
    private int dateTextColor;
    private int rowSpace;
    private int selectedCellTextColor;
    private int dateToHeaderSpace;
    private int offset;
    private int rowCount;
    private OnSelectedDateChangeListener onSelectedDateChangeListener;
    private boolean cellsInitialized = false;
    private DateCell selectedCell;
    private DateCell prevSelectedCell;

    public CalView(Context context) {
        super(context);
    }

    public CalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);

        rowCount = minRowCount;

        initPaints();
    }

    static float getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return TypedValue.applyDimension(unit, size, metrics);
    }

    public OnSelectedDateChangeListener getOnSelectedDateChangeListener() {
        return onSelectedDateChangeListener;
    }

    public void setOnSelectedDateChangeListener(OnSelectedDateChangeListener onSelectedDateChangeListener) {
        this.onSelectedDateChangeListener = onSelectedDateChangeListener;
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

            minRowCount = typedArray.getColor(
                    R.styleable.CalView_minRowCount, 1);

            maxRowCount = typedArray.getColor(
                    R.styleable.CalView_minRowCount, 5);

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

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        if (rowCount >= minRowCount && rowCount <= maxRowCount && this.rowCount != rowCount) {
            this.rowCount = rowCount;
            //postInvalidate();
            Log.d(TAG, "setRowCount to: " + rowCount);
        }
    }

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
        todayCyclePaint.setStrokeWidth(getPixels(TypedValue.COMPLEX_UNIT_DIP, 1));
        todayCyclePaint.setStyle(Paint.Style.STROKE);
        todayCyclePaint.setAntiAlias(true);
    }

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


            CalCell cell = new HeaderCell(i, Headers[i]);

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

        final int cellCount = maxRowCount * 7;

        for (int i = 0; i < cellCount; i++) {

            dateCells.add(new DateCell(i, firstCellDate.plusDays(i)));
        }
    }

    public int getMinHeight() {
        return (int) ((rowSpace + dateCellHeight) * (minRowCount - 1)
                + dateToHeaderSpace
                + headerCellHeight
                + (3 * dateCellHeight / 2f)
                + getPixels(TypedValue.COMPLEX_UNIT_DIP, 15));
    }

    public int getMaxHeight() {
        return (int) ((rowSpace + dateCellHeight) * (maxRowCount - 1)
                + dateToHeaderSpace + headerCellHeight
                + (3 * dateCellHeight / 2f)
                + getPixels(TypedValue.COMPLEX_UNIT_DIP, 15));
    }

    public int getMaxRowCount() {
        return maxRowCount;
    }

    public int getMinRowCount() {
        return minRowCount;
    }

    private void drawDateCells(Canvas canvas) {
        Log.d(TAG, "date cells count: " + dateCells.size());


        if (rowCount < maxRowCount && selectedCell != null) {
            // TODO: 找出最合适显示选中单元的位置
            // 如果选中单元在最后一行，
            final DateTime firstCellDate = dateCells.get(0).date;
            Duration diff = new Duration(firstCellDate, selectedCell.date);

            final int diffDays = (int) diff.getStandardDays();
            offset = diffDays - (diffDays % 7);

            if (offset + 7 * rowCount >= dateCells.size()) {
                offset = dateCells.size() - 7 * rowCount;
            }
        } else {
            offset = 0;
        }

        Log.d(TAG, "offset: " + offset);

        for (int i = 0; i < (7 * rowCount); i++) {

            final CalCell cell = dateCells.get(i + offset);

            if (cell == null) {
                continue;
            }

            final boolean isSelected = cell.equals(selectedCell);

            Log.d(TAG, "current cell: " + cell);
            if (selectedCell != null) {
                Log.d(TAG, "selected cell: " + selectedCell);
            }

            if (isSelected) {
                canvas.drawCircle(selectedCell.getX(),
                        selectedCell.getY() - (dateCellHeight / 2f),
                        cellRadius,
                        selectedCyclePaint);
            } else if (cell.date.getDayOfYear() == DateTime.now().getDayOfYear()) {
                canvas.drawCircle(cell.getX(),
                        cell.getY() - (dateCellHeight / 2f),
                        cellRadius,
                        todayCyclePaint);
            }


            dateTextPaint.setColor(isSelected ? selectedCellTextColor :
                    (cell.disabled ? headerTextColor : dateTextColor));

            Log.d(TAG, "Draw text for " + cell.date + ", isSelected=" + isSelected);

            String text;
            if (cell.date.getDayOfMonth() == 1) {
                text = cell.date.getMonthOfYear() + "月";
                dateTextPaint.setTextSize(dateTextSize / 1.35f);
            } else {
                text = String.format("%02d", cell.date.getDayOfMonth());
                dateTextPaint.setTextSize(dateTextSize);
            }

            canvas.drawText(text,
                    cell.getX(),
                    cell.getY(),
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
                    cell.getX(),
                    cell.getY(),
                    headerTextPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed || this.cellsInitialized) {
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
                && prevSelectedCell != selectedCell
                && (rowCount == getMaxRowCount() || prevSelectedCell.inSameRow(selectedCell))) {
            canvas.drawCircle(prevSelectedCell.getX(),
                    prevSelectedCell.getY() + prevSelectedCellOffsetY,
                    prevSelectedCellRadius,
                    prevSelectedPointPaint);
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

                // 按下和抬起的距离小于100像素，切按下时间小于200ms，认为是单击
                if (d < 100 && t < 200) {
                    Log.d(TAG, "single click");
                    onSingleClick(touchDown);
                } else if (Math.abs(touchDown.y - touchUp.y) < 100 && Math.abs((touchDown.x - touchUp.x)) > 100) {
                    if (touchDown.x > touchUp.x) {
                        onSwipeLeft();
                    } else {
                        onSwipeRight();
                    }
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

    private void onSwipeRight() {

    }

    private void onSwipeLeft() {

    }

    private void onSingleClick(final TouchInfo ti) {
        for (int i = 0; i < dateCells.size(); i++) {

            final DateCell cell = dateCells.get(i);

            if (cell.disabled) {
                continue;
            }

            final float d = ti.distanceTo(cell.getX(), cell.getY());

            if (d < cellRadius) {
                prevSelectedCell = selectedCell;
                selectedCell = cell;

                Log.d(TAG, "on clicked, redraw");

                final OnSelectedDateChangeListener l = onSelectedDateChangeListener;
                if (l != null) {
                    l.onSelectedDateChange(cell.date.getYear(), cell.date.getMonthOfYear(), cell.date.getDayOfMonth());
                }

                postInvalidate();
                break;
            }
        }
    }

    public interface OnSelectedDateChangeListener {
        void onSelectedDateChange(int year, int month, int day);
    }

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

    private abstract class CalCell {
        public final int index;

        public final DateTime date;

        public final String title;

        public final boolean disabled;

        public CalCell(int index, String title) {
            this(index, null, title);
        }

        public CalCell(int index, DateTime date) {
            this(index, date, "");
        }

        private CalCell(int index, DateTime date, String title) {
            this.index = index;
            this.date = date;
            this.title = title;

            final DateTime now = DateTime.now();

            disabled = date == null
                    || date.getYear() < now.getYear()
                    || date.getDayOfYear() < now.getDayOfYear();
        }

        protected abstract int getX(int offset);

        protected abstract int getY(int offset);

        public int getX() {
            return getX(offset);
        }

        public int getY() {
            return getY(offset);
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
                    ", index=" + index +
                    ", title='" + title + '\'' +
                    ", disabled=" + disabled +
                    '}';
        }
    }

    private class HeaderCell extends CalCell {

        public HeaderCell(int index, String title) {
            super(index, title);
        }

        @Override
        protected int getX(int offset) {
            return (int) (calMargin + ((float) index + 0.5f) * headerCellWidth);
        }

        @Override
        protected int getY(int offset) {
            return (int) (3 * headerCellHeight / 2f);
        }
    }

    private class DateCell extends CalCell {

        public DateCell(int index, DateTime date) {
            super(index, date);
        }

        @Override
        protected int getX(int offset) {
            final float x = calMargin + ((float) (index % 7) + 0.5f) * dateCellWidth;

            return (int) x;
        }

        @Override
        protected int getY(int offset) {
            final float y = ((rowSpace + dateCellHeight) * (int) ((index - offset) / 7))
                    + dateToHeaderSpace + headerCellHeight
                    + 3 * dateCellHeight / 2f;

            Log.d(TAG, String.format("getY, index: %d， offset: %d, actual index: %d, y: %d",
                    index,
                    offset,
                    index - offset,
                    (int) y));

            return (int) y;
        }

        public boolean inSameRow(final DateCell cell) {
            if (cell == null) {
                return false;
            }


            int myOffset = date.getDayOfWeek() % 7;
            int othersOffset = cell.date.getDayOfWeek() & 7;

            return (date.minusDays(myOffset).getDayOfYear() == cell.date.minusDays(othersOffset).getDayOfYear());
        }
    }
}
