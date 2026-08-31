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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Schermata iniziale ("splash") a schermo intero: una piccola silhouette della MX-5 in alto
 * con sotto la scritta "Mazda MX-5" ("Mazda" in bianco, "MX-5" in rosso), poi l'orario
 * corrente in grande (ore:minuti:secondi) e sotto ancora giorno della settimana e data
 * (giorno e mese) in italiano. Resta la schermata di partenza dell'app (non passa
 * automaticamente ad altro, a differenza dello splash del progetto pilota): da qui, con
 * i pulsanti nella barra in alto (selezionabili con la rotella, nessuna dipendenza dal
 * touchscreen), si passa alla vista analogica o al cronometro a schermo intero.
 */
public final class DigitalClockScreen extends Screen implements androidx.lifecycle.DefaultLifecycleObserver {

    private static final int RED = Color.parseColor("#FF4D4D");
    private static final int WHITE = Color.parseColor("#F4F4FA");
    private static final int DIM = Color.parseColor("#8888A0");

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ITALY);
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ITALIAN);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.ITALIAN);
    private Bitmap silhouette;

    public DigitalClockScreen(@NonNull CarContext context) {
        super(context);
        getLifecycle().addObserver(this);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // Due pulsanti nell'ActionStrip (analogico, cronometro): nel progetto pilota, su head
        // unit reali senza touchscreen, un NavigationTemplate senza contenuti di navigazione
        // reali rendeva selezionabili con la rotella solo le prime due icone su quattro. Qui
        // ci fermiamo a due per restare nella soglia verificata affidabile.
        ActionStrip actionStrip = new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setIcon(ClockIcons.analogGlyph(120))
                        .setOnClickListener(() -> getScreenManager().push(new AnalogClockScreen(getCarContext())))
                        .build())
                .addAction(new Action.Builder()
                        .setIcon(ClockIcons.stopwatchGlyph(120))
                        .setOnClickListener(() -> getScreenManager().push(new StopwatchScreen(getCarContext())))
                        .build())
                .build();

        return new NavigationTemplate.Builder()
                .setActionStrip(actionStrip)
                .build();
    }

    /** Disegna una piccola silhouette in alto, poi orario e data centrati, il più grandi
     *  possibile nell'area disponibile senza che nulla si sovrapponga: la silhouette resta
     *  in una fascia propria sopra l'orario, non più dietro al testo come nello splash del
     *  progetto pilota (lì funzionava perché il testo era molto più piccolo). */
    private void draw(Canvas canvas, Rect visibleArea) {
        if (silhouette == null) {
            silhouette = BitmapFactory.decodeResource(getCarContext().getResources(), R.drawable.car_silhouette);
        }

        float cx = visibleArea.centerX();
        float areaW = visibleArea.width();
        float areaH = visibleArea.height();
        float top = visibleArea.top;

        // Fascia alta per la silhouette (piccola) e, subito sotto, la scritta "Mazda MX-5":
        // "Mazda" in bianco, "MX-5" in rosso, come richiesto. Un margine sotto il bordo
        // superiore evita che il disegno resti incollato in cima allo schermo.
        float bandTopMargin = areaH * 0.08f;
        float carBandH = areaH * 0.22f;
        float carCy = top + bandTopMargin + carBandH * 0.40f;
        if (silhouette != null) {
            float maxW = areaW * 0.28f;
            float maxH = carBandH * 0.52f;
            float scale = Math.min(maxW / silhouette.getWidth(), maxH / silhouette.getHeight());
            float w = silhouette.getWidth() * scale;
            float h = silhouette.getHeight() * scale;
            RectF dst = new RectF(cx - w / 2f, carCy - h / 2f, cx + w / 2f, carCy + h / 2f);
            Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(silhouette, null, dst, imgPaint);
        }

        String brand = "Mazda";
        String model = " MX-5";
        float titleSize = Math.min(areaW, areaH) * 0.05f;
        float titleY = top + bandTopMargin + carBandH * 0.86f;

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
        canvas.drawText(brand, titleStartX, titleY, brandPaint);
        canvas.drawText(model, titleStartX + brandW, titleY, modelPaint);

        // Ora e giorno/data centrati sull'altezza TOTALE dello schermo (non sullo spazio
        // restante sotto la silhouette): la fascia di silhouette+titolo sopra resta
        // indipendente, il quadrante dell'ora non si sposta se quella fascia cambia altezza.
        float screenCy = top + areaH / 2f;

        String time = timeFormat.format(Calendar.getInstance().getTime());
        float timeSize = Math.min(areaW, areaH) * 0.26f;
        float timeY = screenCy + timeSize * 0.10f;

        Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(WHITE);
        timePaint.setFakeBoldText(true);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setTextSize(timeSize);
        canvas.drawText(time, cx, timeY, timePaint);

        Calendar now = Calendar.getInstance();
        String day = capitalize(dayFormat.format(now.getTime()));
        String date = dateFormat.format(now.getTime());
        String dayDate = day + " " + date;
        float daySize = Math.min(areaW, areaH) * 0.062f;
        float dayY = timeY + timeSize * 0.42f;

        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setColor(RED);
        dayPaint.setFakeBoldText(true);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setTextSize(daySize);
        canvas.drawText(dayDate, cx, dayY, dayPaint);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
