package com.google.android.gms.fit.samples.basicsensorsapi;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fit.samples.common.logger.Log;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class WalkingService extends Service {
    private static final String TAG = WalkingService.class.getSimpleName();
    private IBinder mBinder = new WalkingServiceBinder();
    private WalkingServiceListener mWalkingServiceListener;

    private GoogleApiClient mClient;
    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;
    private OnDataPointListener mOnDataPointListener;

    private double mTotalDistanceTraveled = 0;


    //Used to connect to service
    public class WalkingServiceBinder extends Binder {
        public WalkingService getService() {
            return WalkingService.this;
        }
    }

    public void initCallbacks(WalkingServiceListener listener, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener){
        mWalkingServiceListener = listener;
        mOnConnectionFailedListener = onConnectionFailedListener;
    }

    public void startTracking() {
        Log.i(TAG, "startTracking()");
        buildFitnessClient();
    }

    public void stopTracking() {
        unregisterFitnessDataListener();
    }
        // [START auth_build_googleapiclient_beginning]
        /**
         *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
         *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
         *  (see documentation for details). Authentication will occasionally fail intentionally,
         *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
         *  can address. Examples of this include the user never having signed in before, or having
         *  multiple accounts on the device and needing to specify which account to use, etc.
         */
        private void buildFitnessClient() {

            if (mClient == null) {
                mClient = new GoogleApiClient.Builder(this)
                        .addApi(Fitness.SENSORS_API)
                        .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                        .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                        .addConnectionCallbacks(
                                new GoogleApiClient.ConnectionCallbacks() {
                                    @Override
                                    public void onConnected(Bundle bundle) {
                                        Log.i(TAG, "Connected!!!");
                                        // Now you can make calls to the Fitness APIs.
                                        findFitnessDataSources();
                                    }

                                    @Override
                                    public void onConnectionSuspended(int i) {
                                        // If your connection to the sensor gets lost at some point,
                                        // you'll be able to determine the reason and react to it here.
                                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                        } else if (i
                                                == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                            Log.i(TAG,
                                                    "Connection lost.  Reason: Service Disconnected");
                                        }
                                    }
                                }
                        )
                        .addOnConnectionFailedListener(mOnConnectionFailedListener)
                        .build();
            }

            if(!mClient.isConnected()) {
                mClient.connect();
            }
        }
        // [END auth_build_googleapiclient_beginning]

    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     *     {@link com.google.android.gms.fitness.SensorsApi
     *     #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_DISTANCE_DELTA)
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        processDataSourcesResult(dataSourcesResult);
                    }
                });
        // [END find_data_sources]
    }

    /**
     * Process data sources result. We only use live distance from steps.
     *
     * @param dataSourcesResult the data sources result
     */
    protected void processDataSourcesResult(DataSourcesResult dataSourcesResult){
        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
        final String DATA_SOURCE_TYPE = "live_distance_from_steps";
        
        for (int i = 0; i < dataSourcesResult.getDataSources().size(); i++){
            DataSource dataSource = dataSourcesResult.getDataSources().get(i);
            String dataType = dataSource.toString();
            Log.i(TAG, "Data source found: " + dataSource.toString());
            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

            // we use live distance from steps because we know the pitfalls of using anything derived from GPS
            if(dataSource.getDataType().equals(DataType.TYPE_DISTANCE_DELTA) && dataType != null && dataType.contains(DATA_SOURCE_TYPE)) {
                //Let's register a listener to receive Activity data!
                Log.i(TAG, "Distance From Steps Data Source Found");
                Log.i(TAG, "Data source for STEPS DISTANCE found!  Registering.");
                registerFitnessDataListener(dataSource, DataType.TYPE_DISTANCE_DELTA);
            }
        }
    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        // [START register_data_listener]
        mOnDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                    mTotalDistanceTraveled += val.asFloat();

                    //notify the presenter we have a new total distance
                    mWalkingServiceListener.onTotalDistanceUpdated(mTotalDistanceTraveled);
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .setTimeout(24, TimeUnit.HOURS) //effects when the listener will be killed, regardless
                                                        //if we unregister it or not
                        .setAccuracyMode(SensorRequest.ACCURACY_MODE_DEFAULT)
                        .build(),
                mOnDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener registered!");
                        } else {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });
        // [END register_data_listener]
    }

    /**
     * Unregister the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener() {
        if (mOnDataPointListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                mClient,
                mOnDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }



    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskTemoved()");
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}
