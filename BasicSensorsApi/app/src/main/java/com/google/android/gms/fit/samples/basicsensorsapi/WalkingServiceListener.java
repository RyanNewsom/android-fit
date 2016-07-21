package com.google.android.gms.fit.samples.basicsensorsapi;

/**
 * Used to communicate from the service to the presenter
 */
public interface WalkingServiceListener {
    void onTotalDistanceUpdated(double newTotal);
}
