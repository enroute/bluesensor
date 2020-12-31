package com.ztfun.bluesensor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ztfun.bluesensor.R;

import java.util.concurrent.CopyOnWriteArrayList;

public class OscilloscopeView extends SurfaceView implements SurfaceHolder.Callback {
    private DataSet dataSet, dataSetBuffered;
    private Paint paintLine, paintDark, paintDim, paintText;
    private SurfaceHolder holder;
    private static final String TAG = "OscilloscopeView";

    int minorCount = 50;
    int ticksCount = 5;
    int stepMajor;

    int marginTop, marginBottom, marginLeft, marginRight;
    int originX, originY, dataTopRightX, dataTopRightY;

    int canvasWidth, canvasHeight;
    int dataWidth, dataHeight;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    public OscilloscopeView(Context context) {
        super(context);
        setup();
    }

    public OscilloscopeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public OscilloscopeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public OscilloscopeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private int getColor(int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    private void setup() {
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(getColor(R.color.osciloText));
        int scaledSize = getResources().getDimensionPixelSize(R.dimen.oscillo_text_size);
        paintText.setTextSize(scaledSize);

        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(getColor(R.color.osciloLine));

        paintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDark.setColor(getColor(R.color.osciloDark));

        paintDim = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDim.setColor(getColor(R.color.osciloDim));

        paintLine.setStyle(Paint.Style.FILL);

        holder = getHolder();
        holder.addCallback(this);

        dataSet = new DataSet("Demo");

        setWillNotDraw(false);

        marginTop = 60;
        marginBottom = marginLeft = marginRight = 30;

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCurve(canvas);
        // Log.d(TAG, "onDraw");
    }

    public void registerData(DataSet dataSet) {
        this.dataSet = dataSet;
        this.dataSet.setParentView(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        drawFrame(canvas);

//        dataSet.addEntry(0, 1);
//        dataSet.addEntry(2, 3);
//        dataSet.addEntry(5, 8);

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawFrame(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int x, y;

        originX = marginLeft;
        originY = height - marginBottom;

        dataTopRightX = width - marginRight;
        dataTopRightY = marginTop;

        canvasWidth = width;
        canvasHeight = height;

        dataWidth = width - marginLeft - marginRight;
        dataHeight = height - marginTop - marginBottom;

        // background to dim color
        canvas.drawColor(getColor(R.color.osciloDark));

        /**
        int xStepMinor = Math.round(width / minorCount);
        int yStepMinor = Math.round(height / minorCount);
        int stepMinor = Math.min(xStepMinor, yStepMinor);
         **/
        // always divide height to 10
//        int stepMinor = Math.round(dataHeight / minorCount) - 1;
//        stepMajor = stepMinor * 5;
        stepMajor = Math.round(dataHeight / 10);

        // adjust data height
        dataHeight = stepMajor * 10;

        // dim color rect starting from origin
        x = originX;
        for (int i = 0; ; i++) {
            if (x > dataTopRightX)
                break;
            y = originY - (i % 2 == 0 ? 0 : stepMajor);
            while (y >= dataTopRightY) {
                canvas.drawRect(x, y - stepMajor, x + stepMajor, y, paintDim);
                y -= (stepMajor + stepMajor);
            }

            x += stepMajor;
        }

        // data frame
        canvas.drawLine(originX, originY, originX, dataTopRightY, paintText);
        canvas.drawLine(originX, originY, dataTopRightX, originY, paintText);
        canvas.drawLine(dataTopRightX, originY, dataTopRightX, dataTopRightY, paintText);
        canvas.drawLine(originX, dataTopRightY, dataTopRightX, dataTopRightY, paintText);

        // major grids
        x = originX;
        while (x < originX + dataWidth) {
            canvas.drawLine(x, originY, x, marginTop, paintText);
            x += stepMajor;
        }

        y = originY;
        while (y > marginTop) {
            canvas.drawLine(originX, y, originX + dataWidth, y, paintText);
            y -= stepMajor;
        }

        // label (legend)
        drawText(dataSet.label, canvasWidth / 2, 10, canvas, paintText, DRAW_TEXT_ALIGN_TOP | DRAW_TEXT_ALIGN_CENTER_HORIZONTAL);
    }

    private void drawCurve(Canvas canvas) {
        // draw nothing if no data
        if (dataSet.data.size() <= 0) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        Point pt1, pt2;
        pt1 = coordinateToCanvasPoint(dataSet.data.get(0));
        // todo: draw pt1 too
        for (int i = 1; i < dataSet.data.size(); i ++) {
        // for (Coordinate coord : dataSet.data) {
            pt2 = coordinateToCanvasPoint(dataSet.data.get(i));
            // ignore data out of range
            if (pt2.x < originX || pt2.x > originX + dataWidth) continue;
            if (pt2.y < marginTop || pt2.y > originY) continue;

            canvas.drawCircle(pt2.x, pt2.y, 4, paintLine);
            canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paintLine);
            pt1 = pt2;
            //Toast.makeText(getContext(), "x=" + pt.x + ", y=" + pt.y, Toast.LENGTH_LONG).show();
        }

        // axis label
        int y = originY;
        while (y >= dataTopRightY) {
//            canvas.drawText("" + canvasPointToCoordinate(new Point(0, y), canvas).y,
////                    5,
////                    y,
////                    paintText);
            drawText(String.format("%.1f", canvasPointToCoordinate(new Point(0, y), canvas).y),
                    5,
                    y,
                    canvas,
                    paintText);
            y -= stepMajor;
        }

        // Latest data
        drawText("" + dataSet.data.get(dataSet.data.size() - 1).y,
                dataTopRightX - 150,
                10,
                canvas,
                paintText,
                DRAW_TEXT_ALIGN_TOP
                );
    }

//    public static final int DRAW_TEXT_ALIGN_LEFT = 1;
    public static final int DRAW_TEXT_ALIGN_RIGHT = 2;
    public static final int DRAW_TEXT_ALIGN_TOP = 4;
    public static final int DRAW_TEXT_ALIGN_BOTTOM = 8;
    public static final int DRAW_TEXT_ALIGN_CENTER_HORIZONTAL = 16;
    public static final int DRAW_TEXT_ALIGN_CENTER_VERTICAL = 32;
    private void drawText(String text, int cx, int cy, Canvas canvas, Paint paint) {
        drawText(text, cx, cy, canvas, paint, DRAW_TEXT_ALIGN_CENTER_VERTICAL);
    }
    private void drawText(String text, int cx, int cy, Canvas canvas, Paint paint, int align) {
        int x = cx, y = cy;
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

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private Point coordinateToCanvasPoint(Coordinate coordinate) {
        return coordinateToCanvasPoint(coordinate.x, coordinate.y);
    }

    private Point coordinateToCanvasPoint(float cx, float cy) {
//        // Don't change x
//        int x = Math.round((cx - dataSet.minX) / (dataSet.maxX - dataSet.minX) * dataWidth) + originX;
//        // scale y
//        float yGap = (dataSet.maxY - dataSet.minY) / scaleFactor;
//        int y = originY - Math.round(dataHeight - (cy - dataSet.minY) / yGap * dataHeight);
//
//        return new Point(x, y);

//        float scaleX = 100;
//
        return new Point(Math.round((cx - dataSet.minX) / (dataSet.maxX - dataSet.minX) * dataWidth) + originX,
                originY - Math.round((cy - dataSet.minY) / (dataSet.maxY - dataSet.minY) * dataHeight));
    }

    private Coordinate canvasPointToCoordinate(Point pt, Canvas canvas) {
        float x = dataSet.minX + (dataSet.maxX - dataSet.minX) * (pt.x - originX) / (dataWidth);
        float y = dataSet.minY + (dataSet.maxY - dataSet.minY) * (originY - pt.y) / (dataHeight);

        // Log.d(TAG, "(x,y)=" + pt.x + "," + pt.y + ", ==> (" + x + ", " + y + ")");

        return new Coordinate(x, y);
    }

    static public class DataSet {
        private OscilloscopeView parentView;
        private CopyOnWriteArrayList<Coordinate> data;
        private float maxX, maxY, minX, minY;
        private float stepX, stepY;
        private String label;
        public DataSet(String label) {
            data = new CopyOnWriteArrayList<Coordinate>();
            minX = minY = 0;
            maxX = 1000;
            stepX = 1000;
            maxY = 10;
            this.label = label;
        }
        public void setParentView(OscilloscopeView view) {
            parentView = view;
        }
        public void addEntry(float x, float y) {
            data.add(new Coordinate(x, y));
            if (x > maxX) {
                maxX = x;
                minX = maxX - stepX;
            }
            if (y > maxY) {
                maxY = y;
            } else if (y < minY) {
                minY = y;
            }
            if (parentView != null) {
                parentView.invalidate();
                // Log.d(OscilloscopeView.TAG, "add data (" + x + ", " + y + ")");
            }
        }
    }

    static class Coordinate {
        float x;
        float y;
        public Coordinate(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
        // Let the ScaleGestureDetector inspect all events.
        // scaleGestureDetector.onTouchEvent(event);
        // return true;
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }
}
