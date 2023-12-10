package com.example.trapezoidal;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.trapezoid.R;

import java.util.Arrays;

public class PerspectiveTransformDistortView extends View implements View.OnTouchListener {

    private Paint paintRect, paintCircle, paintFetchCircle;
    public int LEFT;
    public int TOP;
    public int RIGHT;
    public int BOTTOM;

    Paint paint2;
    Point CIRCLE_TOP_LEFT;
    Point CIRCLE_TOP_RIGHT;
    Point CIRCLE_BOTTOM_LEFT;
    Point CIRCLE_BOTTOM_RIGHT;
    private int lastX, lastY;
    Bitmap image;
    Matrix rect2Poly;
    Matrix poly2Rect;

    Point fetchPoint1 = new Point();
    Point fetchPoint2 = new Point();
    Point fetchPoint3 = new Point();
    Point fetchPoint4 = new Point();

    boolean isTouchCirclePoints = true;
    boolean isTouchFetchPoints = false;
    float density;

    boolean isFetchStage = true;

    public PerspectiveTransformDistortView(Context context) {
        super(context);
        init();
    }

    public PerspectiveTransformDistortView( Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PerspectiveTransformDistortView(Context context, AttributeSet attrs,
                                           int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PerspectiveTransformDistortView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void onAction(int cmdType) {
        //cmd 1 : 点设置好后，让其使用设置好的选取点，重新映射绘制到控件
        isFetchStage = !isFetchStage;
        invalidate();
    }

    private void init() {
        this.setOnTouchListener(this);
        paintRect = new Paint();
        paintRect.setColor(0xffff0000);
        paintRect.setAntiAlias(true);
        paintRect.setDither(true);
        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setStrokeJoin(Paint.Join.BEVEL);
        paintRect.setStrokeCap(Paint.Cap.BUTT);
        paintRect.setStrokeWidth(3);
        paintCircle = new Paint();
        paintCircle.setColor(0xff0000ff);
        paintCircle.setAntiAlias(true);
        paintCircle.setDither(true);
        paintCircle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCircle.setStrokeJoin(Paint.Join.BEVEL);
        paintCircle.setStrokeCap(Paint.Cap.BUTT);

        paintFetchCircle = new Paint();
        paintFetchCircle.setColor(Color.GREEN);
        paintFetchCircle.setAntiAlias(true);
        paintFetchCircle.setDither(true);
        paintFetchCircle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintFetchCircle.setStrokeJoin(Paint.Join.BEVEL);
        paintFetchCircle.setStrokeCap(Paint.Cap.BUTT);

        paint2 = new Paint();
        paint2.setColor(Color.WHITE);
        paint2.setStyle(Paint.Style.FILL);

        DisplayManager displayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        displayManager.getDisplays()[0].getRealMetrics(displayMetrics);
        density = displayMetrics.density;
        Log.d(TAG, " density:" + displayMetrics.density);

        int x = 20;
        int y = 20;
        int w = 1000;
        int h = 1400;

        LEFT = x;
        TOP = y;
        RIGHT = x + w;
        BOTTOM = y + h;
        CIRCLE_TOP_LEFT = new Point(LEFT, TOP);
        CIRCLE_TOP_RIGHT = new Point(RIGHT, TOP);
        CIRCLE_BOTTOM_LEFT = new Point(LEFT, BOTTOM);
        CIRCLE_BOTTOM_RIGHT = new Point(RIGHT, BOTTOM);
        initFetchPoints();
        //360x480 image is public domain:
        //https://en.wikipedia.org/wiki/File:Stratus-Cloud-Uetliberg.jpg
        image = BitmapFactory.decodeResource(getResources(), /*R.drawable.stratus_cloud_uetliberg*/R.drawable.book);
        Bitmap b = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Matrix m = new Matrix();
        m.postRotate(180, image.getWidth()/2, image.getHeight()/2);
        c.drawBitmap(image, m, null);
        image = b;
        rect2Poly = buildMatrice(true);
        poly2Rect = buildMatricePoly2Rect(true, null);
    }

    void initFetchPoints() {
        int padding = 40;
        fetchPoint1.set(CIRCLE_TOP_LEFT.x + padding, CIRCLE_TOP_LEFT.y + padding);
        fetchPoint2.set(CIRCLE_BOTTOM_LEFT.x + padding, CIRCLE_BOTTOM_LEFT.y - padding);
        fetchPoint3.set(CIRCLE_BOTTOM_RIGHT.x - padding, CIRCLE_BOTTOM_RIGHT.y - padding);
        fetchPoint4.set(CIRCLE_TOP_RIGHT.x - padding, CIRCLE_TOP_RIGHT.y + padding);
    }

    Matrix buildMatrice(boolean isRect2Poly) {
        if(isRect2Poly) {
            int bw = image.getWidth();
            int bh = image.getHeight();

            float[] pts = {
                    // source
                    0, 0,
                    0, bh,
                    bw, bh,
                    bw, 0,
                    // destination
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0};
            pts[8] = CIRCLE_TOP_LEFT.x;
            pts[9] = CIRCLE_TOP_LEFT.y;
            pts[10] = CIRCLE_BOTTOM_LEFT.x;
            pts[11] = CIRCLE_BOTTOM_LEFT.y;
            pts[12] = CIRCLE_BOTTOM_RIGHT.x;
            pts[13] = CIRCLE_BOTTOM_RIGHT.y;
            pts[14] = CIRCLE_TOP_RIGHT.x;
            pts[15] = CIRCLE_TOP_RIGHT.y;
            Matrix m = new Matrix();
            m.setPolyToPoly(pts, 0, pts, 8, 4);
            return  m;
        }
        return null;
    }

    Matrix buildMatricePoly2Rect(boolean isInverse, float[] pts ){
        Matrix m = new Matrix();
        if(isInverse) {
            boolean isSuccess = rect2Poly.invert(m);
            if(!isSuccess)
                Log.w(TAG, "rect2Poly false ");
        } else {
            float [] bookRectInBitmapPoints = pts;//mapPoint2Rect(false, true);
            //build poly2Rect
            float[] matricePts = new float[2*8];
            Log.d(TAG, "bookRectInBitmapPoints:" + Arrays.toString(bookRectInBitmapPoints));
            System.arraycopy(bookRectInBitmapPoints, 0, matricePts, 0 , 8);
            int offset = 8;
            matricePts[0 + offset] = CIRCLE_TOP_LEFT.x;
            matricePts[1 + offset] = CIRCLE_TOP_LEFT.y;
            matricePts[2 + offset] = CIRCLE_BOTTOM_LEFT.x;
            matricePts[3 + offset] = CIRCLE_BOTTOM_LEFT.y;
            matricePts[4 + offset] = CIRCLE_BOTTOM_RIGHT.x;
            matricePts[5 + offset] = CIRCLE_BOTTOM_RIGHT.y;
            matricePts[6 + offset] = CIRCLE_TOP_RIGHT.x;
            matricePts[7 + offset] = CIRCLE_TOP_RIGHT.y;
            m.setPolyToPoly(matricePts, 0, matricePts, 8, 4);
            //m.setValues(matricePts);
        }
        return m;
    }

    float[] mapPoint2Rect(boolean isFetchPoints, boolean isBackToImageCoordinate) {
        float pts2[] = isFetchPoints ? new float[]{
                fetchPoint1.x, fetchPoint1.y,
                fetchPoint2.x, fetchPoint2.y,
                fetchPoint3.x, fetchPoint3.y,
                fetchPoint4.x, fetchPoint4.y
        } : new float[]{
                CIRCLE_TOP_LEFT.x, CIRCLE_TOP_LEFT.y,
                CIRCLE_BOTTOM_LEFT.x, CIRCLE_BOTTOM_LEFT.y,
                CIRCLE_BOTTOM_RIGHT.x, CIRCLE_BOTTOM_RIGHT.y,
                CIRCLE_TOP_RIGHT.x, CIRCLE_TOP_RIGHT.y
        };
        poly2Rect.mapPoints(pts2);
        if(isBackToImageCoordinate) {
            for (int i = 0; i < pts2.length; i++) {
                pts2[i] = pts2[i] / density;
            }
            Log.d(TAG, " isBackToImageCoordinate:" + Arrays.toString(pts2) + " " + isFetchPoints);
        } else
            Log.d(TAG, " not isBackToImageCoordinate:" + Arrays.toString(pts2) + " " + isFetchPoints);
        return pts2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(paint2);
        // Free Transform bitmap
        if(isFetchStage)
            canvas.drawBitmap(image, rect2Poly, null);
        else
            canvas.drawBitmap(image, poly2Rect, null);

        isTouchCirclePoints = false;
        isTouchFetchPoints = false;
        // line left
        canvas.drawLine(CIRCLE_TOP_LEFT.x, CIRCLE_TOP_LEFT.y, CIRCLE_BOTTOM_LEFT.x, CIRCLE_BOTTOM_LEFT.y, paintRect);
        // line top
        canvas.drawLine(CIRCLE_TOP_LEFT.x, CIRCLE_TOP_LEFT.y, CIRCLE_TOP_RIGHT.x, CIRCLE_TOP_RIGHT.y, paintRect);
        // line right
        canvas.drawLine(CIRCLE_TOP_RIGHT.x, CIRCLE_TOP_RIGHT.y, CIRCLE_BOTTOM_RIGHT.x, CIRCLE_BOTTOM_RIGHT.y, paintRect);
        // line bottom
        canvas.drawLine(CIRCLE_BOTTOM_LEFT.x, CIRCLE_BOTTOM_LEFT.y, CIRCLE_BOTTOM_RIGHT.x, CIRCLE_BOTTOM_RIGHT.y, paintRect);
        // circle top left
        canvas.drawCircle(CIRCLE_TOP_LEFT.x, CIRCLE_TOP_LEFT.y, 10, paintCircle);
        // circle top right
        canvas.drawCircle(CIRCLE_TOP_RIGHT.x, CIRCLE_TOP_RIGHT.y, 10, paintCircle);
        // circle bottom left
        canvas.drawCircle(CIRCLE_BOTTOM_LEFT.x, CIRCLE_BOTTOM_LEFT.y, 10, paintCircle);
        // circle bottom right
        canvas.drawCircle(CIRCLE_BOTTOM_RIGHT.x, CIRCLE_BOTTOM_RIGHT.y, 10, paintCircle);

        canvas.drawCircle(fetchPoint1.x, fetchPoint1.y, 10, paintFetchCircle);
        canvas.drawCircle(fetchPoint2.x, fetchPoint2.y, 10, paintFetchCircle);
        canvas.drawCircle(fetchPoint3.x, fetchPoint3.y, 10, paintFetchCircle);
        canvas.drawCircle(fetchPoint4.x, fetchPoint4.y, 10, paintFetchCircle);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return  fetchingTouch(event);
    }

    private boolean fetchingTouch(MotionEvent event) {

        lastX = (int) event.getX();
        lastY = (int) event.getY();

        if (inCircle(lastX, lastY, CIRCLE_TOP_LEFT.x, CIRCLE_TOP_LEFT.y, 40)) {
            isTouchCirclePoints = true;
            CIRCLE_TOP_LEFT.set(lastX, lastY);
        } else if (inCircle(lastX, lastY, CIRCLE_TOP_RIGHT.x, CIRCLE_TOP_RIGHT.y, 40)) {
            isTouchCirclePoints = true;
            CIRCLE_TOP_RIGHT.set(lastX, lastY);
        } else if (inCircle(lastX, lastY, CIRCLE_BOTTOM_LEFT.x, CIRCLE_BOTTOM_LEFT.y, 40)) {
            isTouchCirclePoints = true;
            CIRCLE_BOTTOM_LEFT.set(lastX, lastY);
        } else if (inCircle(lastX, lastY, CIRCLE_BOTTOM_RIGHT.x, CIRCLE_BOTTOM_RIGHT.y, 40)) {
            isTouchCirclePoints = true;
            CIRCLE_BOTTOM_RIGHT.set(lastX, lastY);
        }

        if (isInCircle(lastX, lastY, fetchPoint1) ||
                isInCircle(lastX, lastY, fetchPoint2) ||
                isInCircle(lastX, lastY, fetchPoint3) ||
                isInCircle(lastX, lastY, fetchPoint4)) {
            isTouchFetchPoints = true;
        }

        if(/*isTouchCirclePoints ||*/ isTouchFetchPoints) {
            rect2Poly = buildMatrice(true);
            poly2Rect = buildMatricePoly2Rect(true, null);
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "matrix2 isAffine:" + map2Poly.isAffine());
            }*/

            fetchCoordinateInBitmap= mapPoint2Rect(true, false);
            poly2Rect = buildMatricePoly2Rect(false, fetchCoordinateInBitmap);
        }

        updateInterval++;
        if (event.getAction() == MotionEvent.ACTION_UP || (updateInterval % 10 == 0)) {
            invalidate();
            return true;
        }
        return true;
    }

    float fetchCoordinateInBitmap[];

    private boolean isInCircle(int x, int y, Point p) {
        if (inCircle(x, y, p.x, p.y, 40)) {
            p.set(x, y);
            return true;
        }
        return false;
    }

    int updateInterval = 0;

    private boolean inCircle(float x, float y, float circleCenterX, float circleCenterY, float circleRadius) {
        double dx = Math.pow(x - circleCenterX, 2);
        double dy = Math.pow(y - circleCenterY, 2);

        if ((dx + dy) < Math.pow(circleRadius, 2)) {
            return true;
        } else {
            return false;
        }
    }
}
