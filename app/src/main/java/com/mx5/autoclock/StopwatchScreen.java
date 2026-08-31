package com.mx5.autoclock;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.Template;
import androidx.car.app.navigation.model.NavigationTemplate;

import java.util.Locale;

/**
 * Cronometro solo digitale a schermo intero: minuti:secondi.millisecondi (i millesimi
 * mostrano insieme decimi, centesimi e millesimi in un'unica lettura a 3 cifre), il più
 * grande possibile, nessun quadrante analogico né disegno dell'auto. Due sole azioni
 * nell'ActionStrip (Avvia/Pausa e Azzera): restano entrambe raggiungibili con la sola
 * rotella, come verificato nel progetto pilota fino a un massimo di due icone per schermata
 * su head unit reali senza touchscreen. Si torna alla schermata digitale con il tasto
 * indietro fisico della rotella (gestito automaticamente dallo stack delle Screen).
 */
public final class StopwatchScreen extends Screen implements androidx.lifecycle.DefaultLifecycleObserver {

    private static final int RED = Color.parseColor("#FF4D4D");
    private static final int WHITE = Color.parseColor("#F4F4FA");
    private static final int DIM = Color.parseColor("#8888A0");

    public StopwatchScreen(@NonNull CarContext context) {
        super(context);
        getLifecycle().addObserver(this);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        boolean running = StopwatchState.isRunning();

        ActionStrip actionStrip = new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setIcon(running ? ClockIcons.pauseGlyph(120) : ClockIcons.playGlyph(120))
                        .setOnClickListener(() -> {
                            StopwatchState.toggleStartStop();
                            invalidate();
                        })
                        .build())
                .addAction(new Action.Builder()
                        .setIcon(ClockIcons.resetGlyph(120))
                        .setOnClickListener(() -> {
                            StopwatchState.reset();
                            invalidate();
                        })
                        .build())
                .build();

        return new NavigationTemplate.Builder()
                .setActionStrip(actionStrip)
                .build();
    }

    private void draw(Canvas canvas, Rect visibleArea) {
        float cx = visibleArea.centerX();
        float cy = visibleArea.centerY();
        float areaW = visibleArea.width();
        float areaH = visibleArea.height();
        boolean running = StopwatchState.isRunning();

        String label = "CRONOMETRO";
        float labelSize = Math.min(areaW, areaH) * 0.055f;
        float labelY = cy - areaH * 0.20f;

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(DIM);
        labelPaint.setFakeBoldText(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(labelSize);
        canvas.drawText(label, cx, labelY, labelPaint);

        long elapsedMs = StopwatchState.elapsedMs();
        long totalMs = elapsedMs % 1000;
        long totalSec = elapsedMs / 1000;
        long minutes = totalSec / 60;
        long seconds = totalSec % 60;
        // mm:ss come prima parte, in dimensione piena; .mmm (millesimi) più piccolo ma ben
        // leggibile, per mostrare in un colpo solo decimi, centesimi e millesimi di secondo.
        String mainText = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
        String msText = String.format(Locale.ROOT, ".%03d", totalMs);

        float mainSize = Math.min(areaW, areaH) * 0.26f;
        float msSize = mainSize * 0.42f;

        Paint mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(WHITE);
        mainPaint.setFakeBoldText(true);
        mainPaint.setTextSize(mainSize);

        Paint msPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        msPaint.setColor(running ? RED : DIM);
        msPaint.setFakeBoldText(true);
        msPaint.setTextSize(msSize);

        float mainW = mainPaint.measureText(mainText);
        float msW = msPaint.measureText(msText);
        float totalW = mainW + msW;
        float startX = cx - totalW / 2f;
        float baselineY = cy + areaH * 0.06f;

        canvas.drawText(mainText, startX, baselineY, mainPaint);
        // I millesimi appoggiano sulla stessa base ma con dimensione minore: leggero
        // allineamento verso il basso per restare visivamente "in linea" con i numeri grandi.
        canvas.drawText(msText, startX + mainW, baselineY, msPaint);

        String status = running ? "IN CORSO" : "FERMO";
        float statusSize = Math.min(areaW, areaH) * 0.045f;
        float statusY = baselineY + mainSize * 0.42f;

        Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPaint.setColor(running ? RED : DIM);
        statusPaint.setFakeBoldText(true);
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(statusSize);
        canvas.drawText(status, cx, statusY, statusPaint);
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
