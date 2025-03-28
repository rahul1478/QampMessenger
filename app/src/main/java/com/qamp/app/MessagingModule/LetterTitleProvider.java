/*
 * *
 *  * Created by Shivam Tiwari on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 2:35 AM
 *
 */

package com.qamp.app.MessagingModule;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;

import com.qamp.app.R;

public class LetterTitleProvider {
    private final Bitmap mBitmap;
    private final Rect mBounds = new Rect();
    private final Canvas mCanvas = new Canvas();
    private int[] mColors = {-957596, -686759, -416706, -1784274, -9977996, -10902850, -14642227, -5414233};
    private final char[] mFirstChar = new char[1];
    private final TextPaint mPaint = new TextPaint();
    private final int mTileLetterFontSize;
    private final int mTileSize;

    public LetterTitleProvider(Context context, int tileSize, int[] colors) {
        if (colors != null) {
            this.mColors = colors;
        }
        this.mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        this.mPaint.setColor(-1);
        this.mPaint.setTextAlign(Paint.Align.CENTER);
        this.mPaint.setAntiAlias(true);
        this.mTileLetterFontSize = 25;
        this.mTileSize = tileSize;
        this.mBitmap = Bitmap.createBitmap(this.mTileSize, this.mTileSize, Bitmap.Config.ARGB_8888);
    }

    public Bitmap getLetterTile(String displayName, boolean newBitmap, Context context) {
        int width = this.mTileSize;
        int height = this.mTileSize;
        Bitmap bmp = this.mBitmap;
        if (newBitmap) {
            bmp = Bitmap.createBitmap(this.mTileSize, this.mTileSize, Bitmap.Config.ARGB_8888);
        }
        char firstChar = '*';
        Canvas c = this.mCanvas;
        c.setBitmap(bmp);
        if (!TextUtils.isEmpty(displayName)) {
            firstChar = displayName.charAt(0);
        }
        c.drawColor(pickColor(displayName));
        if (isEnglishLetterOrDigit(firstChar)) {
            this.mFirstChar[0] = Character.toUpperCase(firstChar);
        } else {
            this.mFirstChar[0] = firstChar;
        }
        this.mPaint.setTextSize((float) this.mTileLetterFontSize);
        this.mPaint.getTextBounds(this.mFirstChar, 0, 1, this.mBounds);
        c.drawText(this.mFirstChar, 0, 1, (float) ((width / 2) + 0), (float) ((height / 2) + 0 + ((this.mBounds.bottom - this.mBounds.top) / 2)), this.mPaint);
        Drawable drawable = context.getResources().getDrawable(R.drawable.person);

        // Step 2: Convert the Drawable to a Bitmap using BitmapFactory.
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // Create a canvas to draw the Drawable onto the Bitmap.
        // Note: If your drawable does not have a transparent background, you can use Bitmap.Config.RGB_565 for better memory efficiency.
        // Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        // However, using ARGB_8888 is recommended for images with transparency.

        // Create a canvas using the bitmap.
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

        // Set the bounds of the Drawable.
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the Drawable onto the canvas.
        drawable.draw(canvas);
        return bitmap;
    }

    private static boolean isEnglishLetterOrDigit(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9');
    }

    public int pickColor(String key) {
        if (TextUtils.isEmpty(key)) {
            return 0;
        }
        return this.mColors[Math.abs(key.hashCode()) % this.mColors.length];
    }
}
