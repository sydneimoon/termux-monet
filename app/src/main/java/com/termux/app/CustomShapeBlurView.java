package com.termux.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.github.mmin18.widget.RealtimeBlurView;

public class CustomShapeBlurView extends RealtimeBlurView {
	Paint mPaint;
	RectF mRectF;

	public CustomShapeBlurView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mRectF = new RectF();
	}

    @Override
    protected void drawBlurredBitmap(Canvas canvas, Bitmap blurredBitmap, int overlayColor) {
        if (blurredBitmap != null) {
            mRectF.right = getWidth();
            mRectF.bottom = getHeight();

            mPaint.reset();
            mPaint.setAntiAlias(true);

            BitmapShader shader = new BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Matrix matrix = new Matrix();
            matrix.postScale(mRectF.width() / blurredBitmap.getWidth(), mRectF.height() / blurredBitmap.getHeight());
            shader.setLocalMatrix(matrix);
            mPaint.setShader(shader);

            float cornerRadius = 24f;  // ex: 24dp
            canvas.drawRoundRect(mRectF, cornerRadius, cornerRadius, mPaint);

            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setColor(overlayColor);
            canvas.drawRoundRect(mRectF, cornerRadius, cornerRadius, mPaint);
        }
    }
}
