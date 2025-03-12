package com.qamp.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.Profile;
import com.mesibo.mediapicker.MediaPicker;
import com.mesibo.messaging.UserListFragment;
import com.qamp.app.Fragment.OnBoardingFromCreateGroup;
import com.qamp.app.LoginModule.MesiboApiClasses.MesiboUI;
import com.qamp.app.MessagingModule.MesiboMessagingActivity;
import com.qamp.app.R;
import com.qamp.app.Utils.AppUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;


public class CreateGroupActivity extends AppCompatActivity implements MediaPicker.ImageEditorListener,
        Mesibo.GroupListener, MesiboProfile.Listener {
    public static ArrayList<MesiboProfile> mMemberProfiles = new ArrayList<>();
    Bundle mGroupEditBundle = null;
    int mGroupMode;

    Long groupId;

    Uri ImageUri;
    ImageView backBtn, nugroup_picture, imageView6, imageView10;
    EditText nugroup_editor1, nugroup_editor;
    CardView nextButton;

    MesiboProfile mProfile;

    TextView members_list, nu_rv_name;

    ShapeableImageView nu_rv_profile;

    RecyclerView nugroup_members;

    ShapeableImageView showImageView;

    private void openOnboardingBottomSheet(boolean isPhoto) {
        FragmentManager fm = getSupportFragmentManager();
        OnBoardingFromCreateGroup bottomSheetFragment = new OnBoardingFromCreateGroup(isPhoto);
        bottomSheetFragment.show(fm, bottomSheetFragment.getTag());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setStatusBarColor(CreateGroupActivity.this, R.color.colorAccent);
        setContentView(R.layout.activity_create_group);
        initviews();
        this.mGroupMode = getIntent().getIntExtra(MesiboUI.GROUP_MODE, 0);
        this.mGroupEditBundle = getIntent().getBundleExtra(MesiboUI.BUNDLE);
        nugroup_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOnboardingBottomSheet(true);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creategroup();
            }
        });


    }

    private void creategroup() {
        String groupName = nu_rv_name.toString().trim();

        MesiboGroupProfile.GroupSettings settings = new MesiboGroupProfile.GroupSettings();
        settings.name = groupName;
        settings.flags = 0;

        Mesibo.createGroup(settings, this);


    }

    public boolean isExistingMember(Profile p) {
        if (UserListFragment.mExistingMembers == null) {
            return false;
        }
        for (MesiboGroupProfile.Member m : UserListFragment.mExistingMembers) {
            if (p == m.getProfile()) {
                return true;
            }
        }
        return false;

    }

    private void addMembers() {
        ArrayList<String> members = new ArrayList<>();
        for (int i = 0; i < mMemberProfiles.size(); i++) {
            MesiboProfile mp = mMemberProfiles.get(i);
            if (!isExistingMember(mp)) {
                members.add(mp.getAddress());
            }
        }
        if (members.size() > 0) {
            String[] m = new String[members.size()];
            members.toArray(m);
            MesiboGroupProfile.MemberPermissions permissions = new MesiboGroupProfile.MemberPermissions();
            permissions.flags = 31;
            permissions.adminFlags = 0;
            this.mProfile.getGroupProfile().addMembers(m, permissions);
        }


        Intent intent = new Intent(CreateGroupActivity.this, MesiboMessagingActivity.class);
        intent.putExtra(MesiboUI.GROUP_ID,groupId);
        intent.putExtra("groupCreate","5");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void openCamera() {
//        if (!AppUtils.aquireUserPermission(CreateGroupActivity.this, "android.permission.CAMERA", CreateNewGroupFragment.CAMERA_PERMISSION_CODE)) {
//            return true;
//        }
        MediaPicker.launchPicker(CreateGroupActivity.this, MediaPicker.TYPE_CAMERAIMAGE);
    }

     public void openGallery() {
         MediaPicker.launchPicker(CreateGroupActivity.this, MediaPicker.TYPE_FILEIMAGE);

     }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        if (uri != null) {
            this.ImageUri = uri;
           showImageView.setImageURI(uri);
           nugroup_picture.setVisibility(View.GONE);
        } else {
            Toast.makeText(getApplicationContext(),"Something went Wrong",Toast.LENGTH_SHORT).show();
        }

    }


     public void removeProfilePic() {

//             if (CreateGroupActivity.this.mProfile != null) {
//                 CreateGroupActivity.this.mProfile.setImage(null);
//                 CreateGroupActivity.this.mProfile.save();
//             }
//         CreateGroupActivity.this.mGroupImage = null;
         //CreateGroupActivity.this.setGroupImage(null);

     }


    private void initviews() {
        backBtn = findViewById(R.id.backBtn);
        nugroup_picture = findViewById(R.id.nugroup_picture);
        imageView6 = findViewById(R.id.imageView6);
        imageView10 = findViewById(R.id.imageView10);
        nugroup_editor1 = findViewById(R.id.nugroup_editor1);
        nugroup_editor = findViewById(R.id.nugroup_editor);
        nextButton = findViewById(R.id.nextButton);
        members_list = findViewById(R.id.members_list);
        nu_rv_name = findViewById(R.id.nu_rv_name);
        nu_rv_profile = findViewById(R.id.nu_rv_profile);
        nugroup_members = findViewById(R.id.nugroup_members);
        showImageView = findViewById(R.id.circleImageView);

    }

    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            // Open an input stream from the Uri
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // Decode the input stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Close the input stream
            inputStream.close();

            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setGroupInfo() {

        Toast.makeText(getApplicationContext(),"SetImage",Toast.LENGTH_SHORT).show();

        Bitmap image = uriToBitmap(getApplicationContext(),ImageUri);

        String GroupName = nu_rv_name.getText().toString();

        if (this.ImageUri != null) {
            this.mProfile.addListener(this);
            this.mProfile.setImage(image);
            this.mProfile.setName(GroupName);
            this.mProfile.save();
            return;
        }

    }

    @Override
    public void Mesibo_onGroupCreated(MesiboProfile mesiboProfile) {
        Toast.makeText(getApplicationContext(),"GroupCreated",Toast.LENGTH_SHORT).show();
        mProfile = mesiboProfile;
        groupId = mesiboProfile.getGroupId();
        setGroupInfo();
        addMembers();
    }

    @Override
    public void Mesibo_onGroupJoined(MesiboProfile mesiboProfile) {

    }

    @Override
    public void Mesibo_onGroupLeft(MesiboProfile mesiboProfile) {

    }

    @Override
    public void Mesibo_onGroupMembers(MesiboProfile mesiboProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersJoined(MesiboProfile mesiboProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersRemoved(MesiboProfile mesiboProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupSettings(MesiboProfile mesiboProfile, MesiboGroupProfile.GroupSettings groupSettings, MesiboGroupProfile.MemberPermissions memberPermissions, MesiboGroupProfile.GroupPin[] groupPins) {

    }

    @Override
    public void Mesibo_onGroupError(MesiboProfile mesiboProfile, long l) {

    }

    @Override
    public void MesiboProfile_onUpdate(MesiboProfile mesiboProfile) {

    }

    @Override
    public void MesiboProfile_onEndToEndEncryption(MesiboProfile mesiboProfile, int i) {

    }

    @Override
    public void MesiboProfile_onPublish(MesiboProfile mesiboProfile, boolean b) {

    }

    @Override
    public void onImageEdit(int type, String caption, String filePath, Bitmap bmp, int result) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
