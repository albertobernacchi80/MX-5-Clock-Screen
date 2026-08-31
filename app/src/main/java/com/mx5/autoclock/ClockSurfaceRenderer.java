package com.mx5.autoclock;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.car.app.AppManager;
import androidx.car.app.CarContext;
import androidx.car.app.SurfaceCallback;
import androidx.car.app.SurfaceContainer;

/**
 * Gestisce l'unica Surface di disegno personalizzato che l'host mette a disposizione alle
 * schermate NavigationTemplate, e la ridisegna periodicamente. Stessa tecnica usata nel
 * progetto pilota (MX-5 Driver Metrics Advanced) per i gauge a schermo intero: qui serve per
 * disegnare l'orologio (digitale o analogico) molto più grande di quanto permetterebbero le
 * icone standard dei template (limitate a 44dp indipendentemente dalla risoluzione fornita).
 *
 * Il disegno sulla Surface NON è cliccabile né selezionabile con la rotella. Tutta
 * l'interazione reale (passare da digitale ad analogico e viceversa, avviare/azzerare il
 * cronometro) passa dall'ActionStrip del NavigationTemplate, un elemento standard gestito
 * dall'host e quindi selezionabile con la rotella esattamente come una voce di lista, anche
 * su head unit senza touchscreen.
 *
 * IMPORTANTE: questo ridisegno periodico scrive direttamente sulla Surface (lockCanvas /
 * unlockCanvasAndPost) e non chiama MAI Screen.invalidate(). Chiamare invalidate() su un
 * timer ricostruisce l'intero Template a ogni tick e, su alcuni head unit, tiene la rotella
 * "occupata" al punto da perdere o ritardare i click sull'ActionStrip proprio mentre il
 * quadrante si aggiorna — il problema riscontrato in un progetto precedente. Le uniche
 * chiamate a invalidate() in questo progetto sono quelle dentro ai listener dei pulsanti
 * (es. Avvia/Pausa e Azzera del cronometro): una tantum, subito dopo un click già arrivato,
 * mai su un ciclo periodico.
 *
 * Singleton perché l'host fornisce una sola Surface per l'intera app: quando si passa da una
 * schermata all'altra, la schermata che diventa visibile sostituisce solo il "drawCallback"
 * (cosa disegnare), non la registrazione della Surface stessa.
 */
final class ClockSurfaceRenderer {

    interface DrawCallback {
        void draw(Canvas canvas, Rect visibleArea);
    }

    private static final ClockSurfaceRenderer INSTANCE = new ClockSurfaceRenderer();
    private static final long TICK_MS = 200; // aggiornamento frequente per lancetta secondi fluida
    private static final int BACKGROUND = Color.parseColor("#09090F");

    static ClockSurfaceRenderer getInstance() {
        return INSTANCE;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile Surface surface;
    private volatile Rect visibleArea;
    private volatile DrawCallback drawCallback;
    private boolean registered = false;
    // Vedi il commento gemello nel progetto pilota (GaugeSurfaceRenderer): l'epoca evita che
    // il tick di una schermata appena lasciata congeli il ridisegno di quella nuova, quando
    // stop() della prima viene eseguito dopo start() della seconda.
    private int epoch = 0;

    private final class Tick implements Runnable {
        private final int myEpoch;

        Tick(int myEpoch) {
            this.myEpoch = myEpoch;
        }

        @Override
        public void run() {
            if (myEpoch != epoch) {
                return;
            }
            render();
            handler.postDelayed(this, TICK_MS);
        }
    }

    private final SurfaceCallback surfaceCallback = new SurfaceCallback() {
        @Override
        public void onSurfaceAvailable(@NonNull SurfaceContainer surfaceContainer) {
            surface = surfaceContainer.getSurface();
            render();
        }

        @Override
        public void onVisibleAreaChanged(@NonNull Rect visible) {
            visibleArea = visible;
            render();
        }

        @Override
        public void onStableAreaChanged(@NonNull Rect stableArea) {
            render();
        }

        @Override
        public void onSurfaceDestroyed(@NonNull SurfaceContainer surfaceContainer) {
            surface = null;
        }
    };

    private ClockSurfaceRenderer() {
    }

    /** Da chiamare in onStart() di ogni schermata che disegna sulla Surface. */
    void start(@NonNull CarContext carContext, @NonNull DrawCallback callback) {
        this.drawCallback = callback;
        if (!registered) {
            carContext.getCarService(AppManager.class).setSurfaceCallback(surfaceCallback);
            registered = true;
        }
        epoch++;
        handler.post(new Tick(epoch));
    }

    /** Da chiamare in onStop(). Vedi il commento sul campo epoch. */
    void stop() {
        // Intenzionalmente vuoto.
    }

    private void render() {
        Surface s = surface;
        DrawCallback cb = drawCallback;
        if (s == null || !s.isValid() || cb == null) {
            return;
        }
        Canvas canvas;
        try {
            canvas = s.lockCanvas(null);
        } catch (Exception e) {
            return;
        }
        try {
            canvas.drawColor(BACKGROUND);
            Rect area = visibleArea != null ? visibleArea : new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
            cb.draw(canvas, area);
        } finally {
            s.unlockCanvasAndPost(canvas);
        }
    }
}
