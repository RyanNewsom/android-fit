package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;

/**
 * Used to commuicate between the activity and presenter
 */
public interface ActivityPresenterContract {

    interface View {
        void updateDistance(double distanceTotal);
        void connectionToFitFailed(ConnectionResult connectionResult);
    }

    interface Presenter {
        void startTrackingDistance(Activity activity);
    }

}
