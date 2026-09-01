package com.mx5.autoclock;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.Session;

public final class ClockSession extends Session {
    @NonNull
    @Override
    public Screen onCreateScreen(@NonNull Intent intent) {
        return new DigitalClockScreen(getCarContext());
    }
}
