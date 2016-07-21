package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fit.samples.common.logger.Log;

/**
 *
 */
public class Presenter implements ActivityPresenterContract.Presenter, WalkingServiceListener, GoogleApiClient.OnConnectionFailedListener {
    private WalkingService mWalkingService;
    private final String TAG = Presenter.class.getSimpleName();
    private ActivityPresenterContract.View mView;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(service != null){
                WalkingService.WalkingServiceBinder binder = (WalkingService.WalkingServiceBinder) service;
                mWalkingService = binder.getService();
                mWalkingService.initAndStartWalk(Presenter.this, Presenter.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public Presenter(ActivityPresenterContract.View view) {
        mView = view;
    }

    @Override
    public void startTrackingDistance(Activity activity) {
        Intent intent = new Intent(activity, WalkingService.class);
        activity.getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed()");
        Log.e(TAG, "connectionResultError: " + connectionResult.getErrorMessage());
        if(connectionResult.hasResolution()){
            Log.i(TAG, "connectionResultHasResulotion, attempting to resolve");
            mView.connectionToFitFailed(connectionResult);
        }
    }

    @Override
    public void onTotalDistanceUpdated(double newTotal) {
        mView.updateDistance(newTotal);
    }
}
