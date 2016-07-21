package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;
import android.content.Intent;

/**
 *
 */
public class Presenter implements ActivityPresenterContract.Presenter {

    @Override
    public void startTrackingDistance(Activity activity) {
        Intent intent = new Intent(activity, WalkingService.class);
        activity.startService(intent);

    }
}
