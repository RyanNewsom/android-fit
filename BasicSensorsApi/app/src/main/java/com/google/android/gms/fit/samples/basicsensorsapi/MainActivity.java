/*
 * Copyright (C) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.fit.samples.basicsensorsapi;

import android.content.IntentSender;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fit.samples.common.logger.Log;
import com.google.android.gms.fit.samples.common.logger.LogView;
import com.google.android.gms.fit.samples.common.logger.LogWrapper;
import com.google.android.gms.fit.samples.common.logger.MessageOnlyLogFilter;


/**
 * This sample demonstrates how to use the Sensors API of the Google Fit platform to find
 * available data sources and to register/unregister listeners to those sources. It also
 * demonstrates how to authenticate a user with Google Play Services.
 */
public class MainActivity extends AppCompatActivity implements ActivityPresenterContract.View {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GOOGLE_FIT_CONNECT_CODE = 2011;
    // [START auth_variable_references]
    private GoogleApiClient mClient = null;
    // [END auth_variable_references]
    private Presenter mPresenter;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    // [END mListener_variable_reference]

    // [START view_references]
    private Button mStartTrackingButton;
    private TextView mTotalDistanceTextView;


    // [START auth_oncreate_setup]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Put application specific code here.

        setContentView(R.layout.activity_main);
        initViews();
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        mPresenter = new Presenter(this);
    }

    private void initViews() {
        mStartTrackingButton = (Button) findViewById(R.id.start_tracking_button);
        mStartTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startTrackingDistance(MainActivity.this);
            }
        });
        mTotalDistanceTextView = (TextView) findViewById(R.id.total_distance_text_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    // [END auth_oncreate_setup]

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_unregister_listener) {
            Snackbar.make(findViewById(R.id.main_activity_view), R.string.disabled, Snackbar.LENGTH_SHORT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  Initialize a custom log class that outputs both to in-app targets and logcat.
     */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
        LogView logView = (LogView) findViewById(R.id.sample_logview);

        // Fixing this lint errors adds logic without benefit.
        //noinspection AndroidLintDeprecation
        logView.setTextAppearance(this, R.style.Log);

        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
    }

    @Override
    public void updateDistance(double distanceTotal) {
        Log.i(TAG, "New total distance: " + distanceTotal);
        if(mTotalDistanceTextView != null){
            mTotalDistanceTextView.setText(Double.toString(distanceTotal));
        }
    }

    @Override
    public void connectionToFitFailed(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(this, GOOGLE_FIT_CONNECT_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while trying to resolve google fit connection");
            e.printStackTrace();
        }
    }
}
