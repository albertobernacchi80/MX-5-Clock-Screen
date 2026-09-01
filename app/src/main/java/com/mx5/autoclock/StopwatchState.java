package com.mx5.autoclock;

import android.os.SystemClock;

/**
 * Stato del cronometro, condiviso e sopravvive al cambio di schermata (es. se in futuro si
 * aggiungessero altre viste). Usa SystemClock.elapsedRealtime(), monotono e non influenzato
 * da eventuali cambi dell'orologio di sistema, invece di System.currentTimeMillis().
 */
final class StopwatchState {

    private static boolean running = false;
    private static long accumulatedMs = 0L;
    private static long startedAtRealtimeMs = 0L;

    private StopwatchState() {
    }

    /** Avvia se fermo, mette in pausa se in corso. */
    static synchronized void toggleStartStop() {
        if (running) {
            accumulatedMs += SystemClock.elapsedRealtime() - startedAtRealtimeMs;
            running = false;
        } else {
            startedAtRealtimeMs = SystemClock.elapsedRealtime();
            running = true;
        }
    }

    /** Azzera il tempo. Se il cronometro è in corso, riparte da zero senza fermarsi. */
    static synchronized void reset() {
        accumulatedMs = 0L;
        if (running) {
            startedAtRealtimeMs = SystemClock.elapsedRealtime();
        }
    }

    static synchronized long elapsedMs() {
        return running ? accumulatedMs + (SystemClock.elapsedRealtime() - startedAtRealtimeMs) : accumulatedMs;
    }

    static synchronized boolean isRunning() {
        return running;
    }
}
