package com.salatart.memeticame.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.salatart.memeticame.Utils.MemeUtils;

/**
 * Created by sasalatart on 11/29/16.
 */

public class CanvasView extends View {
    private Bitmap mBitmap;
    private int mCentreX;
    private int mCentreY;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasView(Context context) {
        super(context);
    }

    /**
     * This method updates the instance of Canvas (View)
     *
     * @param canvas the new instance of Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        if (this.mBitmap != null) {
            canvas.drawBitmap(this.mBitmap, mCentreX, mCentreY, new Paint());
        }
    }

    public void drawBitmap(Bitmap bitmap) {
        Point scaledDimensions = MemeUtils.getBitmapScaledDimensions(this, bitmap);
        this.mCentreX = (getWidth() - scaledDimensions.x) / 2;
        this.mCentreY = (getHeight() - scaledDimensions.y) / 2;
        this.mBitmap = Bitmap.createScaledBitmap(bitmap, scaledDimensions.x, scaledDimensions.y, false);
        this.invalidate();
    }

    /**
     * This method initializes canvas.
     *
     * @return
     */
    public void clear() {
        mBitmap = null;
        this.invalidate();
    }
}
