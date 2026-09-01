package com.mx5.autoclock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.car.app.model.CarIcon;
import androidx.core.graphics.drawable.IconCompat;

/**
 * Icone dell'ActionStrip usate per il pulsante di cambio vista (digitale <-> analogico).
 * Stessa tecnica del progetto pilota (GaugeIcon): bitmap pre-renderizzata su Canvas, nessuna
 * dipendenza da risorse vettoriali esterne. Ogni schermata mostra l'icona della vista "verso
 * cui si va" (dalla schermata digitale si vede l'icona dell'orologio analogico, e viceversa).
 */
final class ClockIcons {

    private static final int WHITE = Color.parseColor("#F4F4FA");
    private static final int RED = Color.parseColor("#FF4D4D");

    private ClockIcons() {
    }

    /** Icona "vai all'orologio analogico": quadrante con lancette. */
    static CarIcon analogGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        float cx = size / 2f, cy = size / 2f, r = size * 0.40f;

        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(size * 0.08f);
        ring.setColor(WHITE);
        c.drawCircle(cx, cy, r, ring);

        Paint hourHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourHand.setStyle(Paint.Style.STROKE);
        hourHand.setStrokeWidth(size * 0.09f);
        hourHand.setStrokeCap(Paint.Cap.ROUND);
        hourHand.setColor(WHITE);
        c.drawLine(cx, cy, cx, cy - r * 0.5f, hourHand);

        Paint minuteHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteHand.setStyle(Paint.Style.STROKE);
        minuteHand.setStrokeWidth(size * 0.08f);
        minuteHand.setStrokeCap(Paint.Cap.ROUND);
        minuteHand.setColor(RED);
        c.drawLine(cx, cy, cx + r * 0.62f, cy, minuteHand);

        Paint hub = new Paint(Paint.ANTI_ALIAS_FLAG);
        hub.setColor(WHITE);
        c.drawCircle(cx, cy, size * 0.06f, hub);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }

    /** Icona "vai all'orologio digitale": due cifre stilizzate con i due punti tra loro. */
    static CarIcon digitalGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        float cx = size / 2f, cy = size / 2f;

        Paint frame = new Paint(Paint.ANTI_ALIAS_FLAG);
        frame.setStyle(Paint.Style.STROKE);
        frame.setStrokeWidth(size * 0.075f);
        frame.setStrokeCap(Paint.Cap.ROUND);
        frame.setStrokeJoin(Paint.Join.ROUND);
        frame.setColor(WHITE);
        float w = size * 0.62f, h = size * 0.42f;
        RectF box = new RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        c.drawRoundRect(box, size * 0.08f, size * 0.08f, frame);

        Paint dots = new Paint(Paint.ANTI_ALIAS_FLAG);
        dots.setColor(RED);
        c.drawCircle(cx, cy - size * 0.07f, size * 0.035f, dots);
        c.drawCircle(cx, cy + size * 0.07f, size * 0.035f, dots);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }

    /** Icona "vai al cronometro": un piccolo quadrante con un solo indice, stile cronometro. */
    static CarIcon stopwatchGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        float cx = size / 2f, cy = size * 0.56f, r = size * 0.34f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(size * 0.075f);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setColor(WHITE);
        c.drawCircle(cx, cy, r, stroke);

        // Pulsante in alto.
        Paint button = new Paint(stroke);
        button.setStrokeWidth(size * 0.09f);
        c.drawLine(cx, cy - r - size * 0.02f, cx, cy - r - size * 0.14f, button);

        // Lancetta a ore 2, in rosso.
        Paint needle = new Paint(Paint.ANTI_ALIAS_FLAG);
        needle.setStyle(Paint.Style.STROKE);
        needle.setStrokeWidth(size * 0.06f);
        needle.setStrokeCap(Paint.Cap.ROUND);
        needle.setColor(RED);
        double rad = Math.toRadians(-60);
        c.drawLine(cx, cy, (float) (cx + r * 0.6f * Math.cos(rad)), (float) (cy + r * 0.6f * Math.sin(rad)), needle);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }

    /** Icona "avvia" (triangolo). */
    static CarIcon playGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(RED);
        float cx = size / 2f, cy = size / 2f, s = size * 0.30f;
        Path tri = new Path();
        tri.moveTo(cx - s * 0.55f, cy - s);
        tri.lineTo(cx - s * 0.55f, cy + s);
        tri.lineTo(cx + s * 0.9f, cy);
        tri.close();
        c.drawPath(tri, fill);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }

    /** Icona "pausa" (due barre). */
    static CarIcon pauseGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(RED);
        float cx = size / 2f, cy = size / 2f;
        float barW = size * 0.16f, barH = size * 0.5f, gap = size * 0.12f;
        RectF left = new RectF(cx - gap / 2f - barW, cy - barH / 2f, cx - gap / 2f, cy + barH / 2f);
        RectF right = new RectF(cx + gap / 2f, cy - barH / 2f, cx + gap / 2f + barW, cy + barH / 2f);
        c.drawRoundRect(left, barW * 0.2f, barW * 0.2f, fill);
        c.drawRoundRect(right, barW * 0.2f, barW * 0.2f, fill);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }

    /** Icona "azzera" (freccia circolare), stesso linguaggio grafico del progetto pilota. */
    static CarIcon resetGlyph(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);

        float cx = size / 2f, cy = size / 2f, r = size * 0.30f;

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(size * 0.075f);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setColor(WHITE);

        RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
        c.drawArc(oval, -50, 280, false, stroke);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(WHITE);
        double rad = Math.toRadians(-50);
        float tipX = (float) (cx + r * Math.cos(rad));
        float tipY = (float) (cy + r * Math.sin(rad));
        float aw = size * 0.10f;
        Path arrow = new Path();
        arrow.moveTo(tipX + aw * 0.9f, tipY - aw * 0.5f);
        arrow.lineTo(tipX - aw * 0.2f, tipY - aw * 0.9f);
        arrow.lineTo(tipX - aw * 0.5f, tipY + aw * 0.3f);
        arrow.close();
        c.drawPath(arrow, fill);

        return new CarIcon.Builder(IconCompat.createWithBitmap(bmp)).build();
    }
}
