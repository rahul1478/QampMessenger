package com.qamp.app.Activity;


import android.Manifest;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qamp.app.LoginModule.MesiboApiClasses.SampleAPI;
import com.qamp.app.MesiboImpModules.ContactSyncClass;
import com.qamp.app.R;
import com.qamp.app.Utils.AnimationUtils;
import com.qamp.app.Utils.AppConfig;
import com.qamp.app.Utils.AppUtils;

import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {

    Button agree_btn;
    private TextView textView2, footer_text_primary, textView3;
    private static final int REQUEST_READ_CONTACTS = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setStatusBarColor(SplashScreenActivity.this, R.color.colorAccent);
        setContentView(R.layout.activity_splash_screen);
        initViews();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        } else {
            //Permission has already been granted
            ContactSyncClass.getMyQampContacts(SplashScreenActivity.this);
        }
        NotificationsTest.getFirebaseDeviceToken();
        boolean isLoggedIn = !AppConfig.getConfig().token.isEmpty();
        if (isLoggedIn) {
            textView2.setVisibility(View.GONE);
            footer_text_primary.setVisibility(View.GONE);
            textView3.setVisibility(View.GONE);
            agree_btn.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(AppConfig.getConfig().token)) {
                        if (AppUtils.getStringValue(SplashScreenActivity.this,"isOnBoarded","").equals("true")){
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }else{
                            Intent intent = new Intent(SplashScreenActivity.this, OnBoardingProfile.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        finish();
                    }
                }
            }, 2000);
        } else {
            textView2.setVisibility(View.VISIBLE);
            footer_text_primary.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.VISIBLE);
            agree_btn.setVisibility(View.VISIBLE);
            AnimationUtils.animateViewVisibility(agree_btn, false);
            AnimationUtils.animateViewVisibility(agree_btn, true);
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    // Define the shared element and its transition name
                    View sharedElement = findViewById(R.id.parentLayout);
                    sharedElements.put("backgroundTransition", sharedElement);
                }
            });
            agree_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoginActivity();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch contacts
                ContactSyncClass.getMyQampContacts(SplashScreenActivity.this);
            } else {
                // Permission denied, show a message or handle it gracefully
                Toast.makeText(this, "Read contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(SplashScreenActivity.this).toBundle();
        intent.putExtra("backgroundResource", R.mipmap.splash);
        startActivity(intent, bundle);
    }

    private void initViews() {
        agree_btn = findViewById(R.id.agree_btn);
        textView2 = findViewById(R.id.textView2);
        footer_text_primary = findViewById(R.id.footer_text_primary);
        textView3 = findViewById(R.id.textView3);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
