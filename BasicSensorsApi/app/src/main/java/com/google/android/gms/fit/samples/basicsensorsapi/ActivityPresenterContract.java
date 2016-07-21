package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;

/**
 * Used to commuicate between the activity and presenter
 */
public interface ActivityPresenterContract {

    interface View {
        void updateDistance(double distanceTotal);
    }

    interface Presenter {
        void startTrackingDistance(Activity activity);
    }

}
