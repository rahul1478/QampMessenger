/*
 * *
 *  * Created by Shivam Tiwari on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 2:35 AM
 *
 */

package com.qamp.app.MessagingModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import com.qamp.app.Activity.MainActivity;
import com.qamp.app.Activity.ShowProfileActivityNew;
import com.qamp.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class MesiboMessagingActivity extends AppCompatActivity implements MesiboMessagingFragment.FragmentListener, Mesibo.ConnectionListener {

    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private static final int PERMISSION_REQUEST_CODE = 123;
    static int FROM_MESSAGING_ACTIVITY = 1;
    /* access modifiers changed from: private */
    public ActionMode mActionMode = null;
    /* access modifiers changed from: private */
    public MesiboUI.Listener mMesiboUIHelperlistener = null;
    /* access modifiers changed from: private */
    public MesiboUI.Config mMesiboUIOptions = null;
    /* access modifiers changed from: private */
    public String mProfileImagePath = null;
    /* access modifiers changed from: private */
    public MesiboProfile mUser = null;
    public MesiboMessage mParameter = null;
    ImageView callButton, videoCallButton;
    MessagingFragment mFragment = null;
    ConstraintLayout NameLayout;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private ImageView mProfileImage = null;
    private Bitmap mProfileThumbnail = null;
    private TextView mTitle = null;
    private Toolbar mToolbar = null;
    private TextView mUserStatus = null;
    private ImageView isOnlineDot;

    private String backStatus = "";
    private Long groupId;
    private String page = "0";

    public static boolean isNetWorkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (connMgr == null) {
                return false;
            } else return connMgr.getActiveNetworkInfo() != null
                    && connMgr.getActiveNetworkInfo().isAvailable()
                    && connMgr.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @SuppressLint("MissingInflatedId")
    public void onCreate(Bundle savedInstanceState) {
        MesiboMessagingActivity.super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();


        // Initializing Mesibo
        Mesibo mesibo = Mesibo.getInstance();
        mesibo.init(getApplicationContext());
        //mesibo.setAccessToken(token);
        MesiboCall mesiboCall = MesiboCall.getInstance();
        mesiboCall.init(getApplicationContext());
        boolean res = mesibo.setDatabase("callapp.db");
        mesibo.addListener(this);
        Mesibo.start();

//     Initializing call

//        MesiboCall.getInstance().init(this);

        this.page = args.getString("page");


            backStatus = getIntent().getStringExtra("groupCreate");
            if (backStatus == null) {
                // Handle the case when "groupCreate" extra is not present
                // For example, you can set a default value or perform some other action
                backStatus = "1"; // Setting a default value
            }
        /* set profile so that it is visible in call screen */
        MesiboProfile u = new MesiboProfile();
        u.setName("Mabel Bay");
        u.address = "destination";
        if (args != null) {
            if (!Mesibo.isReady()) {
                finish();
                return;
            }
            this.mMesiboUIHelperlistener = MesiboUI.getListener();
            this.mMesiboUIOptions = MesiboUI.getConfig();
            String peer = args.getString(MesiboUI.PEER);
            groupId = args.getLong(MesiboUI.GROUP_ID);
            if (groupId > 0) {
                this.mUser = Mesibo.getProfile(groupId);
            } else {
                this.mUser = Mesibo.getProfile(peer);
            }
            if (this.mUser == null) {
                finish();
                return;
            }
            //mParameter = new MesiboMessageProperties(peer, groupId, Mesibo.FLAG_DEFAULT, 0);
            mParameter = new MesiboMessage();
            mParameter.setPeer("peer");
            //=new MesiboMessage("peer",groupId,3L,0);

            // mParameter.set
            //mParameter.setP
            //mParameter = new MesiboMessage();
            //this.mParameter = new MesiboMessageProperties(peer,groupId,3L,0);
            MesiboUIManager.enableSecureScreen(this);
            setContentView(R.layout.activity_qamp_messaging);

            Utils.setActivityStyle(this, this.mToolbar);


            findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    MesiboMessagingActivity.this.onBackPressed();
                }
            });
            this.mUserStatus = (TextView) findViewById(R.id.active_status);
            this.NameLayout = findViewById(R.id.constraintLayout3);
//            this.isOnlineDot = (ImageView) findViewById(R.id.isOnlineDot);
            Utils.setTextViewColor(this.mUserStatus, MesiboUI.getConfig().mToolbarTextColor);
            this.mProfileImage = (ImageView) findViewById(R.id.imageViewProfile);
            String name = this.mUser.getName();
            if (TextUtils.isEmpty(name)) {
                name = this.mUser.address;
            }
            this.mTitle = (TextView) findViewById(R.id.chat_user);
            this.mTitle.setText(name);
            Utils.setTextViewColor(this.mTitle, MesiboUI.getConfig().mToolbarTextColor);
            if (this.mProfileImage != null) {
                final String name_final = name;
                this.mProfileImage.setOnClickListener(new View.OnClickListener() {
                    /* JADX WARNING: type inference failed for: r0v3, types: [android.content.Context, com.qamp.app.MessagingModule.MesiboMessagingActivity] */
                    public void onClick(View v) {
                        if (MesiboMessagingActivity.this.mUser != null) {
                            String unused = MesiboMessagingActivity.this.mProfileImagePath = MesiboMessagingActivity.this.mUser.getImage().getImageOrThumbnailPath();
                            MesiboUIManager.launchPictureActivity(MesiboMessagingActivity.this, name_final, MesiboMessagingActivity.this.mProfileImagePath);
                        }
                    }
                });
            }
            findViewById(R.id.audio_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mesiboCall.getInstance().callUi(getApplicationContext(), mUser, false);
                }
            });
            findViewById(R.id.video_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mesiboCall.getInstance().callUi(getApplicationContext(), mUser, true);
                }
            });
//            RelativeLayout nameLayout = (RelativeLayout) findViewById(R.id.name_tite_layout);
//            this.mTitle = (TextView) findViewById(R.id.chat_user);
//            this.mTitle.setText(name);
//            Utils.setTextViewColor(this.mTitle, MesiboUI.getConfig().mToolbarTextColor);
//            if (this.mTitle != null) {

            TextView nameLayout = findViewById(R.id.chat_user);
                nameLayout.setOnClickListener(new View.OnClickListener() {
                    /* JADX WARNING: type inference failed for: r1v0, types: [android.content.Context, com.qamp.app.MessagingModule.MesiboMessagingActivity] */
                    public void onClick(View v) {
                        if (MesiboMessagingActivity.this.mMesiboUIHelperlistener != null) {
                            MesiboMessagingActivity.this.mMesiboUIHelperlistener.MesiboUI_onShowProfile(MesiboMessagingActivity.this, MesiboMessagingActivity.this.mUser);
                        }

                            Intent intent = new Intent(MesiboMessagingActivity.this, ShowProfileActivityNew.class);
                            intent.putExtra("groupid", groupId);
                            intent.putExtra("peer",peer);
                            startActivity(intent);

                       }
                });
            }
            startFragment(savedInstanceState);



    }

    private void setProfilePicture() {
    }

//    protected void launchCustomCallActivity(String destination, boolean video, boolean incoming) {
//        Intent intent = new Intent(this, MesiboDefaultCallActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("video", video);
//        intent.putExtra("address", destination);
//        intent.putExtra("incoming", incoming);
//        startActivity(intent);
//    }


    private void startFragment(Bundle savedInstanceState) {
        if (findViewById(R.id.fragment_container) != null && savedInstanceState == null) {
            this.mFragment = new MessagingFragment();
            this.mFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, this.mFragment).commit();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        MesiboMessagingActivity.super.onStart();
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [android.content.Context, com.qamp.app.MessagingModule.MesiboMessagingActivity] */
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mMesiboUIHelperlistener != null) {
            this.mMesiboUIHelperlistener.MesiboUI_onGetMenuResourceId(this, FROM_MESSAGING_ACTIVITY, this.mUser, menu);
        }
        return true;
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [android.content.Context, com.qamp.app.MessagingModule.MesiboMessagingActivity, androidx.appcompat.app.AppCompatActivity] */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            finish();
            return true;
        }
        this.mMesiboUIHelperlistener.MesiboUI_onMenuItemSelected(this, FROM_MESSAGING_ACTIVITY, this.mUser, id);
        return MesiboMessagingActivity.super.onOptionsItemSelected(item);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        MesiboMessagingActivity.super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if ("5".equals(backStatus)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }


    public void Mesibo_onUpdateUserPicture(MesiboProfile profile, Bitmap thumbnail, String picturePath) {
        this.mProfileThumbnail = thumbnail;
        this.mProfileImagePath = picturePath;
//cfgvhbjnkml,;
        this.mProfileImage.setImageDrawable(new RoundImageDrawable(this.mProfileThumbnail));
        String name = this.mUser.getName();
        if (TextUtils.isEmpty(name)) {
            name = this.mUser.address;
        }
        if (name.length() > 16) {
            name = name.substring(0, 14) + "...";
        }
        this.mTitle.setText(name);
    }

    public void Mesibo_onUpdateUserOnlineStatus(MesiboProfile profile, String status) {
        if (status == null) {
            this.mUserStatus.setVisibility(View.GONE);
//            this.isOnlineDot.setVisibility(View.GONE);
            return;
        }
        if (!profile.isGroup() && (status.equals(getResources().getString(R.string.online_text)) ||
                status.equals(getResources().getString(R.string.online_text))) &&
                (isNetWorkAvailable(MesiboMessagingActivity.this))) {
//            this.isOnlineDot.setVisibility(View.VISIBLE);
        }
        this.mUserStatus.setVisibility(View.VISIBLE);
        this.mUserStatus.setText(status);
    }

    public void Mesibo_onShowInContextUserInterface() {
        this.mActionMode = startSupportActionMode(this.mActionModeCallback);
    }

    public void Mesibo_onHideInContextUserInterface() {
        this.mActionMode.finish();
    }

    public void Mesibo_onContextUserInterfaceCount(int count) {
        if (this.mActionMode != null) {
            this.mActionMode.setTitle(String.valueOf(count));
            this.mActionMode.invalidate();
        }
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [android.content.Context, com.qamp.app.MessagingModule.MesiboMessagingActivity] */
    public void Mesibo_onError(int type, String title, String message) {
        Utils.showAlert(this, title, message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.mFragment.Mesibo_onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("code", String.valueOf(requestCode));
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        MesiboMessagingActivity.super.onActivityResult(requestCode, resultCode, data);
        if (this.mFragment != null) {
            this.mFragment.Mesibo_onActivityResult(requestCode, resultCode, data);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        MesiboMessagingActivity.super.onResume();
        MesiboUIManager.setMessagingActivity(this);
        setProfilePicture();
    }

    @Override
    public void Mesibo_onConnectionStatus(int i) {
        Log.d("Mesibo", "Connection status: " + i);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }



    private class ActionModeCallback implements ActionMode.Callback {
        private final String TAG;

        private ActionModeCallback() {
            this.TAG = ActionModeCallback.class.getSimpleName();
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            NameLayout.setVisibility(View.GONE);
            menu.clear();
            mode.getMenuInflater().inflate(R.menu.selected_menu, menu);
            menu.findItem(R.id.menu_reply).setShowAsAction(2);
//            menu.findItem(R.id.menu_star).setShowAsAction(2);
//            menu.findItem(R.id.menu_resend).setShowAsAction(2);
            menu.findItem(R.id.menu_copy).setShowAsAction(2);
//            menu.findItem(R.id.menu_forward).setShowAsAction(2);
//            menu.findItem(R.id.menu_forward).setVisible(MesiboMessagingActivity.this.mMesiboUIOptions.enableForward);
//            menu.findItem(R.id.menu_forward).setEnabled(MesiboMessagingActivity.this.mMesiboUIOptions.enableForward);
            menu.findItem(R.id.menu_remove).setShowAsAction(2);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean z;
            boolean z2;
            boolean z3 = false;
            if (MesiboMessagingActivity.this.mFragment == null) {
                return false;
            }
            int enabled = MesiboMessagingActivity.this.mFragment.Mesibo_onGetEnabledActionItems();
//            MenuItem findItem = menu.findItem(R.id.menu_resend);
            if ((enabled & 4) > 0) {
                z = true;
            } else {
                z = false;
            }
//            findItem.setVisible(z);
            MenuItem findItem2 = menu.findItem(R.id.menu_copy);
            if ((enabled & 16) > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            findItem2.setVisible(z2);
            MenuItem findItem3 = menu.findItem(R.id.menu_reply);
            if ((enabled & 2) > 0) {
                z3 = true;
            }

            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int mesiboItemId = 0;
            if (item.getItemId() == R.id.menu_remove) {
                mesiboItemId = 8;
            } else if (item.getItemId() == R.id.menu_copy) {
                mesiboItemId = 16;
            }
//            else if (item.getItemId() == R.id.menu_resend) {
//                mesiboItemId = 4;
//            } else if (item.getItemId() == R.id.menu_forward) {
//                mesiboItemId = 1;
//            } else if (item.getItemId() == R.id.menu_star) {
//                mesiboItemId = 32;
//            }
            else if (item.getItemId() == R.id.menu_reply) {
                mesiboItemId = 2;
            }
            if (mesiboItemId <= 0) {
                return false;
            }
            MesiboMessagingActivity.this.mFragment.Mesibo_onActionItemClicked(mesiboItemId);
            mode.finish();
            MesiboMessagingActivity.this.mFragment.Mesibo_onInContextUserInterfaceClosed();
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            MesiboMessagingActivity.this.mFragment.Mesibo_onInContextUserInterfaceClosed();
            ActionMode unused = MesiboMessagingActivity.this.mActionMode = null;

            Handler handler = new Handler(Looper.getMainLooper());

            long delayMillis = 350;

// Create a Runnable to be executed after the delay
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    NameLayout.setVisibility(View.VISIBLE);
                }
            };

// Post the runnable with the specified delay
            handler.postDelayed(runnable, delayMillis);
        }
    }


}
