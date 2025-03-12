/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 3:25 AM
 *
 */

package com.qamp.app.Utils;

import static com.qamp.app.LoginModule.MesiboApiClasses.UIManager.initUiHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;



import com.qamp.app.Activity.EditProfileActivity;
import com.qamp.app.Activity.ShowProfileActivityNew;
import com.qamp.app.Activity.SplashScreenActivity;

import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.R;
import com.qamp.app.sources.AlbumListData;
import com.qamp.app.sources.MediaPicker;

import java.util.ArrayList;
import java.util.List;

public class UIManager {

//    public static void launchStartupActivity(Context context, boolean skipTour) {
//        Intent intent = new Intent(context, SplashScreenActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(SplashScreenActivity.SKIPTOUR, skipTour);
//        context.startActivity(intent);
//    }

    public static boolean mMesiboLaunched = false;
    public static void launchMesibo(Context context, int flag, boolean startInBackground, boolean keepRunningOnBackPressed) {
        mMesiboLaunched = true;
        MesiboUI.launch(context, flag, startInBackground, keepRunningOnBackPressed);
    }

    public static void launchMesiboContacts(Context context, long forwardid, int selectionMode, int flag, Bundle bundle, String task,Long groupId) {
        MesiboUI.launchContacts(context, forwardid, selectionMode, flag, bundle,task,groupId);
    }

    public static void launchUserProfile(Context context, long groupid, String peer) {
        Intent subActivity = new Intent(context, ShowProfileActivityNew.class);
        subActivity.
                putExtra("peer", peer).
                putExtra("groupid", groupid);
        context.startActivity(subActivity);
    }

//    public static void launchUserSettings(Context context) {
//        Intent intent = new Intent(context, SettingsActivity.class);
//        context.startActivity(intent);
//    }
//
    public static void launchEditProfile(Context context, int flag, long groupid, boolean launchMesibo) {
        Intent subActivity = new Intent(context, EditProfileActivity.class);
        if(flag > 0)
            subActivity.setFlags(flag);
        subActivity.putExtra("groupid", groupid);
        subActivity.putExtra("launchMesibo", launchMesibo);

        context.startActivity(subActivity);
    }

    public static void launchImageViewer(Activity context, String filePath) {
        MediaPicker.launchImageViewer(context, filePath);
    }
    public static void launchImageViewer(Activity context, ArrayList<String> files, int firstIndex) {
        MediaPicker.launchImageViewer(context, files, firstIndex);
    }

    public static void launchImageEditor(Context context, int type, int drawableid, String title, String filePath, boolean showEditControls, boolean showTitle, boolean showCropOverlay, boolean squareCrop, int maxDimension, MediaPicker.ImageEditorListener listener){
        MediaPicker.launchEditor((AppCompatActivity)context, type, drawableid, title, filePath, showEditControls, showTitle, showCropOverlay, squareCrop, maxDimension, listener);
    }

    public static void launchAlbum(Activity context, List<AlbumListData> albumList) {
        MediaPicker.launchAlbum(context, albumList);
    }

    public static boolean mProductTourShown = false;


//    public static void launchWelcomeactivity(Activity context, boolean newtask, ILoginInterface loginInterface, IProductTourListener tourListener){
//
//        initUiHelper();
//
//         // if mesibo was lauched in this session, we came here after logout, so
//        // no need to show tour
//        if(mMesiboLaunched) {
//            launchLogin(context, MesiboListeners.getInstance());
//            return;
//        }
//
//        mProductTourShown = true;
//        MesiboUiHelper.launchTour(context, newtask, tourListener);
//    }

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
