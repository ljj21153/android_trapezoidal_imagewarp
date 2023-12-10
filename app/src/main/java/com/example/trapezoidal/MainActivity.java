package com.example.trapezoidal;

import static android.content.ContentValues.TAG;

import com.example.trapezoid.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap; 
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decor_View = getWindow().getDecorView();

        int ui_Options = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decor_View.setSystemUiVisibility(ui_Options);
        setContentView(/*new PerspectiveTransformDistortView(this)*/ R.layout.activity_main);
        PerspectiveTransformDistortView perview1 = findViewById(R.id.perview);
        View v = findViewById(R.id.fab);
        v.setOnClickListener(v1 -> perview1.onAction(1));
    }
}
