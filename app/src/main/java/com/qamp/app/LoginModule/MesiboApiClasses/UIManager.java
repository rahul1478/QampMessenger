/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 3:25 AM
 *
 */

package com.qamp.app.LoginModule.MesiboApiClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mesibo.mediapicker.AlbumListData;
import com.mesibo.mediapicker.MediaPicker;
import com.qamp.app.Activity.SplashScreenActivity;

import com.qamp.app.R;

import java.util.ArrayList;
import java.util.List;

public class UIManager {

    public static void launchStartupActivity(Context context, boolean skipTour) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(StartUpActivity.SKIPTOUR, skipTour);
        context.startActivity(intent);
    }

    public static boolean mMesiboLaunched = false;
    public static void launchMesibo(Context context, int flag, boolean startInBackground, boolean keepRunningOnBackPressed) {
        mMesiboLaunched = true;
        MesiboUI.launch(context, flag, startInBackground, keepRunningOnBackPressed);
    }



    public static void initUiHelper() {
        MesiboUiHelperConfig config = new MesiboUiHelperConfig();

        config.mWelcomeBottomText = "Mesibo will never share your information";

        config.mWelcomeBackgroundColor = 0xff00868b; //TBD, not required, take from welcomescren[0]

        config.mBackgroundColor = 0xffffffff;
        config.mPrimaryTextColor = 0xff172727;
        config.mButttonColor = 0xff00868b;
        config.mButttonTextColor = 0xffffffff;
        config.mSecondaryTextColor = 0xff666666;

        config.mScreenAnimation = true;
        config.mSmartLockUrl = null; //"https://mesibo.com/sampleapp/";

        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_CONTACTS);
        config.mPermissions = permissions;
        config.mPermissionsRequestMessage = "mesibo requires Storage and Contacts permissions so that you can send messages and make calls to your contacts. Please grant to continue!";
        config.mPermissionsDeniedMessage = "mesibo will close now since the required permissions were not granted";

        //config.mPhoneVerificationText = "Get OTP from your mesibo console (In App settings), login https://mesibo.com/console";
        config.mPhoneVerificationBottomText = "IMPORTANT: We will NOT send OTP.  Instead, you can generate OTP for any number from the mesibo console. Sign up at https://mesibo.com/console";
        config.mLoginBottomDescColor = 0xAAFF0000;

        //MesiboUiHelper.setConfig(config);
    }

//    public static void launchLogin(Activity context, ILoginInterface loginInterface){
//        initUiHelper();
//        MesiboUiHelper.launchLogin(context, true, 2, loginInterface);
//    }

    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener pl, DialogInterface.OnClickListener nl) {
        if(null == context) {
            return; //
        }
        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);

        dialog.setPositiveButton(android.R.string.ok, pl);
        dialog.setNegativeButton(android.R.string.cancel, nl);

        try {
            dialog.show();
        } catch (Exception e) {
        }
    }

    public static void showAlert(Context context, String title, String message) {
        if(null == context) return;
        showAlert(context, title, message, null, null);
    }

}
