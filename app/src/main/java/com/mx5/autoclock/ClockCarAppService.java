package com.mx5.autoclock;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public final class ClockCarAppService extends CarAppService {
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // Valido per test locali di Android Auto. Prima di un'eventuale pubblicazione,
        // sostituire con una allow-list degli host ufficiali (stessa nota del progetto pilota).
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new ClockSession();
    }
}
