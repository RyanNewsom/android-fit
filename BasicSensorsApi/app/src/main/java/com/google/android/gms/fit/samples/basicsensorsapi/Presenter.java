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
 * The presenter handles everything that the activity/fragment needs. All communication is done through
 * an interface.
 */
public class Presenter implements ActivityPresenterContract.Presenter, WalkingServiceListener, GoogleApiClient.OnConnectionFailedListener {
    public static final int GOOGLE_FIT_CONNECT_CODE = 2011;
    private WalkingService mWalkingService;
    private final String TAG = Presenter.class.getSimpleName();
    private ActivityPresenterContract.View mView;

    /**
     * The connection for binding to the service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(service != null){
                WalkingService.WalkingServiceBinder binder = (WalkingService.WalkingServiceBinder) service;
                mWalkingService = binder.getService();
                mWalkingService.initCallbacks(Presenter.this, Presenter.this);
                mWalkingService.startTracking();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * Constructor
     * Makes a new Presenter to handle all things for the view
     * @param view - anything that wants to present
     */
    public Presenter(ActivityPresenterContract.View view) {
        mView = view;
    }

    @Override
    /**
     * Begins tracking distance. Start the service & hook up to Google Fit Sensor's API
     */
    public void startTrackingDistance(Activity activity) {
        Intent startService = null;
        //create the service and start tracking distance
        if(mWalkingService == null) {
            startService = new Intent(activity, WalkingService.class);
            activity.getApplication().bindService(startService, mConnection, Context.BIND_AUTO_CREATE);
        } else{
            mWalkingService.startTracking();
        }
    }

    @Override
    /**
     * Stop tracking distance.
     */
    public void stopTrackingDistance() {
        if(mWalkingService != null){
            mWalkingService.stopTracking();
        }
    }

    @Override
    /**
     * Handle the Activity result code for the View
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GOOGLE_FIT_CONNECT_CODE){
            if(resultCode == Activity.RESULT_OK){
                mWalkingService.startTracking();
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
    /**
     * The total distance we that has been traveled has been updated, notify the View
     */
    public void onTotalDistanceUpdated(double newTotal) {
        mView.updateDistance(newTotal);
    }
}
