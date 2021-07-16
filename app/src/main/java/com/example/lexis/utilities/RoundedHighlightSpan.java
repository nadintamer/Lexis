package com.example.lexis.utilities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

// adapted from: https://gist.github.com/aftabsikander/0b188c55a4e7066f9e14
public class RoundedHighlightSpan extends ReplacementSpan {

    private static final float PADDING = 7;

    @Override
    public  void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end) + PADDING, bottom);
        paint.setColor(Color.parseColor("#FFBF69"));
        canvas.drawRoundRect(rect, 15f, 15f, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText(text, start, end, x + PADDING, y, paint);
    }
    @Override
    public  int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
    {
        return Math.round(measureText(paint, text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end)
    {
        return Math.round(paint.measureText(text, start, end) + PADDING);
    }

}