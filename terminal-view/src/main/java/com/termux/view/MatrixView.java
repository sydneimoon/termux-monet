package com.termux.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class MatrixView extends View {

    private static final Random RANDOM = new Random();
    private int width, height;
    private Canvas canvas;
    private Bitmap canvasBmp;
    private int fontSize = 26;
    private int columnSize;
    //private char[] cars = "+-*/!^'([])#@&?,=$€°|%".toCharArray();
    private char[] cars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private int[] txtPosByColumn;
    private Paint paintTxt, paintBg, paintBgBmp, paintInitBg;
    private float spacingFactor = 1.3f;

    private int[] delayByColumn;
    private int[] delayCounter;

    public MatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintTxt = new Paint();
        paintTxt.setStyle(Paint.Style.FILL);
        paintTxt.setColor(0xFFB5B5B5);
        paintTxt.setTextSize(fontSize);

        paintBg = new Paint();
        paintBg.setColor(Color.TRANSPARENT);
        paintBg.setAlpha(5);
        paintBg.setStyle(Paint.Style.FILL);

        paintBgBmp = new Paint();
        paintBgBmp.setColor(0xCC000000);

        paintInitBg = new Paint();
        paintInitBg.setColor(Color.TRANSPARENT);
        paintInitBg.setAlpha(255);
        paintInitBg.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        canvasBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(canvasBmp);
        //canvas.drawRect(0, 0, width, height, paintInitBg);  // iniciar con fondo sólido
        canvas.drawColor(Color.TRANSPARENT);  // iniciar con fondo transparente
        columnSize = (int) (width / (fontSize * spacingFactor));
        txtPosByColumn = new int[columnSize + 1];
        /*for (int x = 0; x < columnSize; x++) {
            txtPosByColumn[x] = RANDOM.nextInt(height / 2) + 1;
        }*/
        delayByColumn = new int[columnSize + 1];
        delayCounter = new int[columnSize + 1];
        for (int x = 0; x < columnSize; x++) {
            txtPosByColumn[x] = RANDOM.nextInt(height / 2) + 1;
            delayByColumn[x] = RANDOM.nextInt(5) + 3;  // valores entre 3 y 7 frames de espera
            delayCounter[x] = 0;
        }
    }

    private void drawText() {
        /*for (int i = 0; i < txtPosByColumn.length; i++) {
            canvas.drawText("" + cars[RANDOM.nextInt(cars.length)], i * fontSize * spacingFactor, txtPosByColumn[i] * fontSize, paintTxt);

            if (txtPosByColumn[i] * fontSize > height && Math.random() > 0.975) {
                txtPosByColumn[i] = 0;
            }

            txtPosByColumn[i]++;
        }*/
        for (int i = 0; i < txtPosByColumn.length; i++) {
            if (delayCounter[i] >= delayByColumn[i]) {
                canvas.drawText("" + cars[RANDOM.nextInt(cars.length)], i * fontSize * spacingFactor, txtPosByColumn[i] * fontSize, paintTxt);
                if (txtPosByColumn[i] * fontSize > height && Math.random() > 0.975) {
                    txtPosByColumn[i] = 0;
                }
                txtPosByColumn[i]++;
                delayCounter[i] = 0;
            } else {
                delayCounter[i]++;
            }
        }
    }

    private void drawCanvas() {
        canvas.drawRect(0, 0, width, height, paintBg);
        drawText();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBmp, 0, 0, paintBgBmp);
        drawCanvas();
        invalidate();
    }
}
