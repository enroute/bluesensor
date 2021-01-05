package com.ztfun.module.plot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.ztfun.bluesensor.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZtPlotView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = ZtPlotView.class.getSimpleName();

    List<DataSet> dataSets;

    public static class ZtPlotOption {
        public Rect margin;
        public int xGrids, yGrids;
        public Paint gridPaint;

        public Paint getGridPaint() {
            return gridPaint;
        }

        public void setGridPaint(Paint gridPaint) {
            this.gridPaint = gridPaint;
        }

        public ZtPlotOption() {
            margin = new Rect(100, 100, 100, 100);
            xGrids = yGrids = 10;
        }

        public Rect getMargin() {
            return margin;
        }

        public void setMargin(Rect margin) {
            this.margin = margin;
        }

        public int getXGrids() {
            return xGrids;
        }

        public void setXGrids(int xGrids) {
            this.xGrids = xGrids;
        }

        public int getYGrids() {
            return yGrids;
        }

        public void setYGrids(int yGrids) {
            this.yGrids = yGrids;
        }
    }

    public ZtPlotOption option;
    private Rect dataRect;
    private int xMajorStep, yMajorStep;

    public ZtPlotOption getOption() {
        return option;
    }

    public void setOption(ZtPlotOption option) {
        this.option = option;
    }

    public ZtPlotView(Context context) {
        super(context);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private void setup() {
        getHolder().addCallback(this);
        setWillNotDraw(false);
        dataSets = new ArrayList<>();
    }

    public void addDataSet(DataSet dataSet) {
        dataSets.add(dataSet);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (option == null) {
            option = new ZtPlotOption();
        }

        xMajorStep = (int)Math.floor((canvas.getWidth() - option.margin.left - option.margin.right) * 1.0 / option.xGrids);
        yMajorStep = (int)Math.floor((canvas.getHeight() - option.margin.top - option.margin.bottom) * 1.0 / option.yGrids);

        dataRect = new Rect(option.margin.left, option.margin.top,
                option.margin.left + xMajorStep * option.xGrids,
                option.margin.top + yMajorStep * option.yGrids);

        Log.d(TAG, "dataRect=" + dataRect.toString());

        drawGrids(canvas);

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(DataSet dataSet : dataSets) {
            drawDataSet(canvas, dataSet);
        }
    }

    private void drawDataSet(Canvas canvas, DataSet dataSet) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(dataSet.option.paintColor);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.oscillo_text_size));

        // y tick labels
        int align;
        float cx;
        float extraMargin = 10;
        if (dataSet.option.dataSetType == DATA_SET_PRIMARY) {
            align = DRAW_TEXT_ALIGN_RIGHT;
            cx = dataRect.left - extraMargin;
        } else { // if (dataSet.option.dataSetType == DATA_SET_SECONDARY) {
            align = DRAW_TEXT_ALIGN_LEFT;
            cx = dataRect.right + extraMargin;
        }
        float cy = dataRect.bottom;
        for(int i = 0; i < dataSet.yTickLabels.size(); i ++) {
            drawText(canvas, dataSet.yTickLabels.get(i), cx, cy, paint, align);
            cy -= yMajorStep;
            Log.d(TAG, "drawText " + dataSet.yTickLabels.get(i) + " on " + cx + " , " + cy);
        }

        // rotate to draw y labels
        Rect textBounds = new Rect();
        paint.getTextBounds(dataSet.label, 0, dataSet.label.length(), textBounds);
        cy = canvas.getHeight() / 2;
        if (dataSet.option.dataSetType == DATA_SET_PRIMARY) {
            cx = extraMargin + textBounds.height();
        } else {
            cx = canvas.getWidth() - extraMargin;
        }

        canvas.save();
        canvas.rotate(-90, cx, cy);
        canvas.drawText(dataSet.label, cx, cy, paint);
        canvas.restore();

        // data lines
        // todo;
    }

    private void drawGrids(Canvas canvas) {
        Paint paint = option.gridPaint;
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.parseColor("#FFFFFF"));
        }

        int x, y;
        for (int i = 0; i <= option.xGrids; i++) {
            x = dataRect.left + xMajorStep * i;
            canvas.drawLine(x, dataRect.top, x, dataRect.bottom, paint);
        }

        for (int i = 0; i <= option.yGrids; i++) {
            y = dataRect.top + yMajorStep * i;
            canvas.drawLine(dataRect.left, y, dataRect.right, y, paint);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public static final int DRAW_TEXT_ALIGN_LEFT = 1;
    public static final int DRAW_TEXT_ALIGN_RIGHT = 2;
    public static final int DRAW_TEXT_ALIGN_TOP = 4;
    public static final int DRAW_TEXT_ALIGN_BOTTOM = 8;
    public static final int DRAW_TEXT_ALIGN_CENTER_HORIZONTAL = 16;
    public static final int DRAW_TEXT_ALIGN_CENTER_VERTICAL = 32;
    private void drawText(String text, int cx, int cy, Canvas canvas, Paint paint) {
        drawText(canvas, text, cx, cy, paint, DRAW_TEXT_ALIGN_CENTER_VERTICAL);
    }
    private void drawText( Canvas canvas, String text, float cx, float cy, Paint paint, int align) {
        float x = cx, y = cy;
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);

        if ((align & DRAW_TEXT_ALIGN_TOP) != 0) {
            y += textBounds.height();
        }
        if ((align & DRAW_TEXT_ALIGN_BOTTOM) != 0) {

        }
        if ((align & DRAW_TEXT_ALIGN_CENTER_VERTICAL) != 0) {
            y -= textBounds.exactCenterY();
        }
        if ((align & DRAW_TEXT_ALIGN_RIGHT) != 0) {
            x -= textBounds.width();
        }
        if ((align & DRAW_TEXT_ALIGN_CENTER_HORIZONTAL) != 0) {
            x -= textBounds.exactCenterX();
        }

        canvas.drawText(text, x, y, paint);
    }


    // DataSet
    // primary data set on left
    public static final int DATA_SET_PRIMARY = 0;
    // secondary data set on right
    public static final int DATA_SET_SECONDARY = 1;
    public static class DataSetOption {
        public int paintColor;
        public int dataSetType;

        public DataSetOption() {
            dataSetType = DATA_SET_PRIMARY;  // default to primary
            paintColor = Color.parseColor("#FFFF00");           // default to black
        }
    }

    public static class DataEntry {
        public double x;
        public double y;

        public DataEntry(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class DataSet {
        DataSetOption option;
        public String label;

        public List<String> yTickLabels;
        public List<DataEntry> dataEntries;

        public DataSetOption getOption() {
            return option;
        }

        public void setOption(DataSetOption option) {
            this.option = option;
            dataEntries = new CopyOnWriteArrayList<>();
        }

        public DataSet(String label) {
            this.label = label;
            option = new DataSetOption();
        }

        public void addDataEntry(DataEntry entry) {
            dataEntries.add(entry);
        }

        public void addDataEntry(double x, double y) {
            addDataEntry(new DataEntry(x, y));
        }

        public List<String> getYTickLabels() {
            return yTickLabels;
        }

        public void setYTickLabels(List<String> yTickLabels) {
            this.yTickLabels = yTickLabels;
        }
    }
}