package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;
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
    public static final int GOOGLE_FIT_CONNECT_CODE = 2011;
    private WalkingService mWalkingService;
    private final String TAG = Presenter.class.getSimpleName();
    private ActivityPresenterContract.View mView;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(service != null){
                WalkingService.WalkingServiceBinder binder = (WalkingService.WalkingServiceBinder) service;
                mWalkingService = binder.getService();
                mWalkingService.initCallbacks(Presenter.this, Presenter.this);
                mWalkingService.startWalk();
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
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GOOGLE_FIT_CONNECT_CODE){
            if(resultCode == Activity.RESULT_OK){
                mWalkingService.startWalk();
            }
        }
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
