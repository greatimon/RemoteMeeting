package com.example.jyn.remotemeeting.Otto;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by JYN on 2017-11-17.
 */

public final class BusProvider {

    private static Bus sBus;

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new Bus(ThreadEnforcer.ANY);
        }
        return sBus;
    }
}
