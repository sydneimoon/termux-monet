package com.termux.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class GradientBorderView extends View {
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();
    /*  top:
        #52fa5a #4dfcff #c64dff
        #4efcd7 #7abbff #c64dff
    */
    private int[] gradientColors = {
        Color.parseColor("#4efcd7"),
        Color.parseColor("#7abbff"),
        Color.parseColor("#c64dff")
    };

    public GradientBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float borderWidth = 8f;
        rectF.set(
            borderWidth / 2,
            borderWidth / 2,
            getWidth() - borderWidth / 2,
            getHeight() - borderWidth / 2
        );

        LinearGradient gradient = new LinearGradient(
            0, 0, getWidth(), 0,
            gradientColors,
            null,
            Shader.TileMode.CLAMP
        );

        borderPaint.setShader(gradient);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        canvas.drawRoundRect(rectF, 0, 0, borderPaint);  // esquinas sólidas

        /*float radius = 12f;
        Path path = new Path();
        path.addRoundRect(
                rectF,
                new float[]{
                        0, 0,           // superior izquierda
                        0, 0,           // superior derecha
                        radius, radius, // inferior derecha
                        radius, radius, // inferior izquierda
                },
                Path.Direction.CW
        );*/
        //canvas.drawPath(path, borderPaint);  // esquinas redondeadas
    }
}
