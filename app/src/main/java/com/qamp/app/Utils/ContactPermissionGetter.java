package com.qamp.app.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qamp.app.Activity.SplashScreenActivity;
import com.qamp.app.MesiboImpModules.ContactSyncClass;

public class ContactPermissionGetter {

    private static final int CONTACT_PERMISSION_REQUEST_CODE = 123;

    private final Activity activity;

    public ContactPermissionGetter(Activity activity) {
        this.activity = activity;
    }

    public boolean checkAndRequestContactPermission() {
        if (isContactPermissionGranted()) {
            // Permission already granted
            return true;
        } else {
            // Request permission
            requestContactPermission();
            return false;
        }
    }

    private boolean isContactPermissionGranted() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                CONTACT_PERMISSION_REQUEST_CODE);
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == CONTACT_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                ContactSyncClass.getMyQampContacts(activity);
            } else {
                // Permission denied
                showToast("Contact permission denied. Opening app settings.");

                // Open the app settings to allow the user to grant the permission manually
                requestContactPermission();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
