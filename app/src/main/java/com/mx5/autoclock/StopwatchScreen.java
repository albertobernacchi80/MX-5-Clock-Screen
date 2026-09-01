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
 * grande possibile, nessun quadrante analogico né disegno dell'auto.
 *
 * Servono tre funzioni (Avvia/Pausa, Azzera, torna alla Home) ma l'ActionStrip resta a sole
 * DUE icone, come nel resto dell'app: nel progetto pilota, su head unit reali senza
 * touchscreen, un ActionStrip con più di due icone ne rendeva alcune non raggiungibili con la
 * rotella. La prima icona è sempre Avvia/Pausa. La seconda cambia icona e funzione a seconda
 * dello stato — non serve Azzera e Home insieme nello stesso istante:
 *  - cronometro in corso, oppure fermo a zero (niente da azzerare): la seconda icona è Home;
 *  - cronometro in pausa con un tempo registrato (c'è qualcosa da azzerare): la seconda icona
 *    è Azzera.
 * Per uscire mentre il cronometro è in pausa con un tempo sul display, senza azzerarlo, resta
 * disponibile il tasto indietro fisico della rotella (il cronometro continua comunque a
 * contare in background se è in corso, anche cambiando schermata).
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
        boolean hasTimeToReset = !running && StopwatchState.elapsedMs() > 0;

        ActionStrip.Builder stripBuilder = new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setIcon(running ? ClockIcons.pauseGlyph(120) : ClockIcons.playGlyph(120))
                        .setOnClickListener(() -> {
                            StopwatchState.toggleStartStop();
                            invalidate();
                        })
                        .build());

        if (hasTimeToReset) {
            // In pausa con un tempo registrato: la seconda icona azzera. Per tornare alla
            // Home in questo stato resta il tasto indietro fisico della rotella.
            stripBuilder.addAction(new Action.Builder()
                    .setIcon(ClockIcons.resetGlyph(120))
                    .setOnClickListener(() -> {
                        StopwatchState.reset();
                        invalidate();
                    })
                    .build());
        } else {
            // In corso, oppure fermo a zero: niente da azzerare, la seconda icona torna alla
            // Home. Se il cronometro è in corso continua a contare in background.
            stripBuilder.addAction(new Action.Builder()
                    .setIcon(ClockIcons.digitalGlyph(120))
                    .setOnClickListener(() -> getScreenManager().pop())
                    .build());
        }

        return new NavigationTemplate.Builder()
                .setActionStrip(stripBuilder.build())
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
