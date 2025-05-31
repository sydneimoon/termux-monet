package com.termux.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Color;

public class AnimatedGradientView extends View {
    private Paint paint;
    private LinearGradient linearGradient;
    private Matrix gradientMatrix;
    private float translateX = 0;
    private int viewWidth = 0;
    private final int animationSpeed = 15;

    public AnimatedGradientView(Context context) {
        super(context);
        init();
    }

    public AnimatedGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedGradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        gradientMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;

        float gradientLength = viewWidth * 1.2f; // más compacto

        linearGradient = new LinearGradient(
                0, 0, gradientLength, 0,
                new int[]{
                        Color.parseColor("#52fa5a"),
                        Color.parseColor("#4dfcff"),
                        Color.parseColor("#c64dff"),
                        Color.parseColor("#52fa5a") // repetir para ciclo perfecto
                },
                null,
                Shader.TileMode.REPEAT
        );

        paint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        translateX += animationSpeed;
        if (translateX > viewWidth * 1.2f) {
            translateX = 0;
        }

        gradientMatrix.setTranslate(translateX, 0);
        linearGradient.setLocalMatrix(gradientMatrix);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        invalidate();
    }
}
