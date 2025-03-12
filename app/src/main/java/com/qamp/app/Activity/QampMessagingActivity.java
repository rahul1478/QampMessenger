package com.qamp.app.Activity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.CustomClasses.OnlineStatusImageView;
import com.qamp.app.Fragment.ChatFragment;
import com.qamp.app.Fragment.QampMessagingFragment;
import com.qamp.app.LoginModule.MesiboApiClasses.MesiboUI;
import com.qamp.app.R;
import com.qamp.app.Utils.AppUtils;

public class QampMessagingActivity extends AppCompatActivity {

    FrameLayout fragment_container;
    OnlineStatusImageView imageViewProfile;

    String address;
    Long groupId;
    TextView receiver;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qamp_messaging);

        address = getIntent().getStringExtra(MesiboUI.PEER);
        groupId = 0L;

        AppUtils.setStatusBarColor(QampMessagingActivity.this, R.color.colorAccent);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        fragment_container = findViewById(R.id.fragment_container);
        imageViewProfile.setOnlineStatus(true);
        final QampMessagingFragment qampMessagingFragment = new QampMessagingFragment(address,groupId);
        switchFragment(qampMessagingFragment);

        receiver = findViewById(R.id.chat_user);



        MesiboProfile profile = Mesibo.getProfile(address);

        receiver.setText(profile.getName());

        imageViewProfile.setImageBitmap(profile.getImage().getImage());

    }
    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
}
