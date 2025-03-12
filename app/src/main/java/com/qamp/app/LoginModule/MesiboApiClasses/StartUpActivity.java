/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 3:25 AM
 *
 */

package com.qamp.app.LoginModule.MesiboApiClasses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

//import com.mesibo.contactutils.ContactUtils;

public class StartUpActivity extends AppCompatActivity {

    private static final String TAG = "MesiboStartupActivity";
    public final static String INTENTEXIT="exit";
    public final static String SKIPTOUR="skipTour";
    public final static String STARTINBACKGROUND ="startinbackground";
    private boolean mRunInBackground = false;

    public static void newInstance(Context context, boolean startInBackground) {
        Intent i = new Intent(context, StartUpActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(StartUpActivity.STARTINBACKGROUND, startInBackground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getBooleanExtra(INTENTEXIT, false)) {
            Log.d(TAG, "onCreate closing");
            finish();
            return;
        }
        mRunInBackground = getIntent().getBooleanExtra(STARTINBACKGROUND, false);
        if(mRunInBackground) {
            Log.e(TAG, "Moving app to background");
            moveTaskToBack(true);
        } else {
            Log.e(TAG, "Not Moving app to background");
        }
        startNextActivity();
    }

    void startNextActivity() {

//        if(TextUtils.isEmpty(SampleAPI.getToken())) {
//            MesiboUiHelperConfig.mDefaultCountry = ContactUtils.getCountryCode();
//            MesiboUiHelperConfig.mPhoneVerificationBottomText = "Note, Mesibo may call instead of sending an SMS if SMS delivery to your phone fails.";
//            if(null == MesiboUiHelperConfig.mDefaultCountry) {
//                MesiboUiHelperConfig.mDefaultCountry = "91";
//            }
//
//            if (getIntent().getBooleanExtra(SKIPTOUR, false)) {
//                //UIManager.launchLogin(this, MesiboListeners.getInstance());
//            } else {
//
//            }
//
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//
//        } else {
//            UIManager.launchMesibo(this, 0, mRunInBackground, true);
//        }
//
//        finish();
    }

    // since this activity is singleTask, intent will be delivered here if it's running
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        if(intent.getBooleanExtra(INTENTEXIT, false)) {
            finish();
        }

        super.onNewIntent(intent);
    }
}
