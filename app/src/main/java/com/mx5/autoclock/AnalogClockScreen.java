package com.mx5.autoclock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.Template;
import androidx.car.app.navigation.model.NavigationTemplate;

import java.util.Calendar;
import java.util.Locale;

/**
 * Orologio analogico a schermo intero: quadrante con tacche delle ore/minuti, lancetta ore e
 * minuti bianche, lancetta dei secondi rossa. Nessuna silhouette della MX-5 a grandezza piena
 * (a differenza della schermata digitale): solo una versione piccola dello stesso disegno, con
 * la scritta "Mazda MX-5" in piccolissimo sotto di essa (stesso schema colori della schermata
 * digitale, in scala ridotta), disegnata al centro del quadrante come il logo di un orologio
 * reale, tra il perno centrale e il numero 6 — le lancette continuano a passarci sopra, come
 * su un orologio vero. Il pulsante nella barra in alto (unica azione dell'ActionStrip, quindi
 * sempre raggiungibile con la sola rotella) torna alla vista digitale (DigitalClockScreen),
 * con un pop dello stack invece di un nuovo push: le due schermate restano così un semplice
 * toggle avanti/indietro, senza accumulare schermate.
 */
public final class AnalogClockScreen extends Screen implements androidx.lifecycle.DefaultLifecycleObserver {

    private static final int RED = Color.parseColor("#FF4D4D");
    private static final int WHITE = Color.parseColor("#F4F4FA");
    private static final int TRACK = Color.parseColor("#2A2A3C");

    private Bitmap silhouette;

    public AnalogClockScreen(@NonNull CarContext context) {
        super(context);
        getLifecycle().addObserver(this);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        ActionStrip actionStrip = new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setIcon(ClockIcons.digitalGlyph(120))
                        .setOnClickListener(() -> getScreenManager().pop())
                        .build())
                .build();

        return new NavigationTemplate.Builder()
                .setActionStrip(actionStrip)
                .build();
    }

    private void draw(Canvas canvas, Rect visibleArea) {
        if (silhouette == null) {
            silhouette = BitmapFactory.decodeResource(getCarContext().getResources(), R.drawable.car_silhouette);
        }

        float areaW = visibleArea.width();
        float areaH = visibleArea.height();
        float top = visibleArea.top;

        // Il quadrante occupa tutta l'area disponibile: nessuna fascia riservata in basso,
        // macchina e scritta ora vivono dentro al cerchio (vedi sotto).
        float cx = visibleArea.centerX();
        float cy = top + areaH / 2f;
        float r = Math.min(areaW, areaH) * 0.46f;

        drawFace(canvas, cx, cy, r);
        drawLogo(canvas, cx, cy, r);
        drawHands(canvas, cx, cy, r);
    }

    /** Silhouette piccola e scritta "Mazda MX-5" piccolissima, disegnate al centro del
     *  quadrante tra il numero 12 e il perno (sopra la congiunzione delle lancette, non
     *  sotto): così restano visibili più a lungo, invece di stare proprio dove le lancette
     *  ore/minuti si sovrappongono più spesso vicino al perno. Va chiamato DOPO drawFace ma
     *  PRIMA di drawHands: le lancette continuano comunque a passarci sopra quando puntano
     *  verso l'alto, come su un orologio reale. */
    private void drawLogo(Canvas c, float cx, float cy, float r) {
        float logoCy = cy - r * 0.34f;

        if (silhouette != null) {
            float maxW = r * 0.62f;
            float maxH = r * 0.22f;
            float scale = Math.min(maxW / silhouette.getWidth(), maxH / silhouette.getHeight());
            float w = silhouette.getWidth() * scale;
            float h = silhouette.getHeight() * scale;
            RectF dst = new RectF(cx - w / 2f, logoCy - h / 2f, cx + w / 2f, logoCy + h / 2f);
            Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            c.drawBitmap(silhouette, null, dst, imgPaint);
        }

        String brand = "Mazda";
        String model = " MX-5";
        float titleSize = r * 0.075f;
        float titleY = logoCy + r * 0.17f;

        Paint brandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        brandPaint.setColor(WHITE);
        brandPaint.setFakeBoldText(true);
        brandPaint.setTextSize(titleSize);

        Paint modelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        modelPaint.setColor(RED);
        modelPaint.setFakeBoldText(true);
        modelPaint.setTextSize(titleSize);

        float brandW = brandPaint.measureText(brand);
        float modelW = modelPaint.measureText(model);
        float titleStartX = cx - (brandW + modelW) / 2f;
        c.drawText(brand, titleStartX, titleY, brandPaint);
        c.drawText(model, titleStartX + brandW, titleY, modelPaint);
    }

    /** Quadrante: cerchio esterno, tacche delle ore (spesse) e dei minuti (sottili), numeri
     *  1-12. */
    private void drawFace(Canvas c, float cx, float cy, float r) {
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(r * 0.02f);
        ring.setColor(TRACK);
        c.drawCircle(cx, cy, r, ring);

        Paint minuteTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteTick.setStrokeWidth(r * 0.010f);
        minuteTick.setColor(TRACK);

        Paint hourTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourTick.setStrokeWidth(r * 0.028f);
        hourTick.setStrokeCap(Paint.Cap.ROUND);
        hourTick.setColor(WHITE);

        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            boolean isHour = i % 5 == 0;
            float outer = r * 0.96f;
            float inner = isHour ? r * 0.84f : r * 0.91f;
            float x1 = cx + (float) Math.cos(angle) * outer;
            float y1 = cy + (float) Math.sin(angle) * outer;
            float x2 = cx + (float) Math.cos(angle) * inner;
            float y2 = cy + (float) Math.sin(angle) * inner;
            c.drawLine(x1, y1, x2, y2, isHour ? hourTick : minuteTick);
        }

        Paint numPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numPaint.setColor(WHITE);
        numPaint.setFakeBoldText(true);
        numPaint.setTextAlign(Paint.Align.CENTER);
        numPaint.setTextSize(r * 0.16f);
        Paint.FontMetrics fm = numPaint.getFontMetrics();
        float numR = r * 0.70f;
        for (int h = 1; h <= 12; h++) {
            double angle = Math.toRadians(h * 30 - 90);
            float x = cx + (float) Math.cos(angle) * numR;
            float y = cy + (float) Math.sin(angle) * numR - (fm.ascent + fm.descent) / 2f;
            c.drawText(String.valueOf(h), x, y, numPaint);
        }

        Paint hub = new Paint(Paint.ANTI_ALIAS_FLAG);
        hub.setColor(RED);
        c.drawCircle(cx, cy, r * 0.03f, hub);
    }

    /** Lancette ore/minuti bianche, secondi rossa. La lancetta dei secondi si muove con
     *  precisione al millisecondo per un movimento fluido invece che a scatti. */
    private void drawHands(Canvas c, float cx, float cy, float r) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        double secFrac = second + millis / 1000.0;
        double minFrac = minute + secFrac / 60.0;
        double hourFrac = (hour % 12) + minFrac / 60.0;

        double hourAngle = Math.toRadians(hourFrac * 30 - 90);
        double minAngle = Math.toRadians(minFrac * 6 - 90);
        double secAngle = Math.toRadians(secFrac * 6 - 90);

        Paint hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourPaint.setStrokeWidth(r * 0.045f);
        hourPaint.setStrokeCap(Paint.Cap.ROUND);
        hourPaint.setColor(WHITE);
        drawHand(c, cx, cy, hourAngle, r * 0.52f, hourPaint);

        Paint minPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minPaint.setStrokeWidth(r * 0.032f);
        minPaint.setStrokeCap(Paint.Cap.ROUND);
        minPaint.setColor(WHITE);
        drawHand(c, cx, cy, minAngle, r * 0.74f, minPaint);

        Paint secPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secPaint.setStrokeWidth(r * 0.014f);
        secPaint.setStrokeCap(Paint.Cap.ROUND);
        secPaint.setColor(RED);
        drawHand(c, cx, cy, secAngle, r * 0.86f, secPaint);
        // Piccola coda opposta alla lancetta dei secondi, come sugli orologi analogici reali.
        float tailX = cx - (float) Math.cos(secAngle) * r * 0.18f;
        float tailY = cy - (float) Math.sin(secAngle) * r * 0.18f;
        c.drawLine(cx, cy, tailX, tailY, secPaint);

        Paint hub = new Paint(Paint.ANTI_ALIAS_FLAG);
        hub.setColor(RED);
        c.drawCircle(cx, cy, r * 0.03f, hub);
    }

    private void drawHand(Canvas c, float cx, float cy, double angle, float length, Paint paint) {
        float x = cx + (float) Math.cos(angle) * length;
        float y = cy + (float) Math.sin(angle) * length;
        c.drawLine(cx, cy, x, y, paint);
    }

    @Override
    public void onStart(@NonNull androidx.lifecycle.LifecycleOwner owner) {
        ClockSurfaceRenderer.getInstance().start(getCarContext(), this::draw);
    }

    @Override
    public void onStop(@NonNull androidx.lifecycle.LifecycleOwner owner) {
        ClockSurfaceRenderer.getInstance().stop();
    }
}
