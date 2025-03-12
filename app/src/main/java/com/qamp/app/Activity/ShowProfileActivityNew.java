/*
 * *
 *  *  on 20/05/23, 3:30 AM
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 20/05/23, 2:39 AM
 *
 */

package com.qamp.app.Activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.qamp.app.Activity.OnBoardingProfile.CAMERA_PERMISSION_CODE;
import static com.qamp.app.MessagingModule.MesiboUserListFragment.MODE_EDITGROUP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.L;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboDateTime;
import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.MesiboReadSession;
import com.mesibo.emojiview.EmojiconTextView;


import com.mesibo.mediapicker.MediaPicker;
import com.qamp.app.CustomClasses.OnlineStatusImageView;
import com.qamp.app.Fragment.CreateNewGroupFragment;
import com.qamp.app.LoginModule.MesiboApiClasses.SampleAPI;
import com.qamp.app.MessagingModule.MesiboConfiguration;
import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.MessagingModule.RoundImageDrawable;
import com.qamp.app.MessagingModule.Utils;
import com.qamp.app.R;
import com.qamp.app.Utils.AppUtils;
import com.qamp.app.Utils.UIManager;
import com.qamp.app.sources.AlbumListData;
import com.qamp.app.sources.AlbumPhotosData;



import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowProfileActivityNew extends AppCompatActivity implements MesiboProfile.Listener,
        Mesibo.MessageListener, Mesibo.GroupListener, Mesibo.ConnectionListener,
        com.qamp.app.sources.MediaPicker.ImageEditorListener {

    private static final int MAX_THUMBNAIL_GALERY_SIZE = 35;
    private static MesiboProfile mUser;
    private static int VIDEO_FILE = 2;
    ImageView mGroupPicture;
    private static int IMAGE_FILE = 1;
    private static int OTHER_FILE = 2;
    private static Bitmap mDefaultProfileBmp;
    MesiboProfile mProfile = null;
    OnlineStatusImageView mUsermageView;
    Bitmap mGroupImage = null;
    MesiboProfile mUserProfile;
    long mGroupId = 0;
    String mPeer = null;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    LinearLayout mAddMemebers, mEditGroup;
    ArrayList<MesiboGroupProfile.Member> mGroupMemberList = new ArrayList<>();
    MesiboGroupProfile.Member mSelfMember;
    LinearLayout mll;
    TextView mStatus;
    TextView mStatusTime;
    TextView mMobileNumber;
    TextView mPhoneType;
    ImageView audioCall, videoCall, isOnlineDt, backButton;

    private ArrayList<String> mThumbnailMediaFiles;
    private LinearLayout mGallery;
    private int mMediaFilesCounter = 0;
    private TextView mMediaCounterView;
    private ArrayList<AlbumListData> mGalleryData;
    private ImageView mMessageBtn;
    private CardView mMediaCardView;

    private  View view7;
    private CardView mStatusPhoneCard;
    private CardView mGroupMembersCard;
    private CardView mExitGroupCard;
    private TextView mExitGroupText;
    private MesiboReadSession mReadSession = null;
    private TextView number_text, up_status_text;
    private ImageView back_btn;
//    public static ShowProfileActivityNew newInstance(MesiboProfile userdata) {
//        ShowProfileActivityNew fragment = new ShowProfileActivityNew();
//        mUser = userdata;
//        mUser.addListener(fragment);
//        return fragment;
//    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_profile_new);
        AppUtils.setStatusBarColor(ShowProfileActivityNew.this, R.color.colorAccent);
        Bundle args = getIntent().getExtras();
        if (null == args) {
            return;
        }

        audioCall = findViewById(R.id.imageView8);
        videoCall = findViewById(R.id.imageView9);
        backButton = findViewById(R.id.back_btn);



        mPeer = args.getString("peer");
        mGroupId = args.getLong("groupid");

        mUserProfile = null;

        if (mGroupId > 0) {
            mUserProfile = Mesibo.getProfile(mGroupId);
        } else {
            mUserProfile = Mesibo.getProfile(mPeer);
        }

        mUserProfile.addListener(ShowProfileActivityNew.this);

        mUsermageView =  findViewById(R.id.up_image_profile);
        isOnlineDt = (ImageView) findViewById(R.id.isOnlineDot);
        number_text = (TextView) findViewById(R.id.number_text);
        up_status_text = (TextView) findViewById(R.id.up_status_text);
        back_btn = (ImageView) findViewById(R.id.back_btn);

        Mesibo.addListener(this);

        mUsermageView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
//                launchImageViewer(ShowProfileActivityNew.this, mUserProfile.getImageOrThumbnailPath());

                @SuppressLint("RestrictedApi") MenuBuilder menuBuilder = new MenuBuilder(ShowProfileActivityNew.this);
                new MenuInflater(ShowProfileActivityNew.this).inflate(R.menu.selected_menu, menuBuilder);
                @SuppressLint("RestrictedApi") MenuPopupHelper optionsMenu = new MenuPopupHelper(ShowProfileActivityNew.this, menuBuilder, v);
                optionsMenu.setForceShowIcon(true);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        if (item.getItemId() == com.mesibo.messaging.R.id.popup_camera) {
                            if (!Utils.aquireUserPermission(ShowProfileActivityNew.this, "android.permission.CAMERA", CAMERA_PERMISSION_CODE)) {
                                return true;
                            }
                            MediaPicker.launchPicker(ShowProfileActivityNew.this, MediaPicker.TYPE_CAMERAIMAGE);
                            return true;
                        } else if (item.getItemId() == com.mesibo.messaging.R.id.popup_gallery) {
                            MediaPicker.launchPicker(ShowProfileActivityNew.this, MediaPicker.TYPE_FILEIMAGE);
                            return true;
                        } else {
                            if (item.getItemId() == com.mesibo.messaging.R.id.popup_remove) {
                                if (ShowProfileActivityNew.this.mProfile != null) {
                                    ShowProfileActivityNew.this.mProfile.setImage(null);
                                    ShowProfileActivityNew.this.mProfile.save();
                                }
                                ShowProfileActivityNew.this.mGroupImage = null;
                                ShowProfileActivityNew.this.setGroupImage(null);
                            }
                            return false;
                        }
                    }

                    public void onMenuModeChange(MenuBuilder menu) {
                    }
                });
                optionsMenu.show();



            }

        });

        TextView userName = (TextView) findViewById(R.id.up_user_name);

        TextView userstatus = (TextView) findViewById(R.id.up_current_status);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userName.setText(mUserProfile.getName());
        if (mUserProfile.isOnline()) {
            isOnlineDt.setVisibility(VISIBLE);
        } else {
            isOnlineDt.setVisibility(View.INVISIBLE);
        }
        number_text.setText(AppUtils.formatPhoneNumber(mUserProfile.getAddress()));

        // Check if lastSeen is available before accessing `ts`
        if (!mUserProfile.isGroup()){
            long lastSeen = mUserProfile.getLastSeen().ts;
            userstatus.setVisibility(View.VISIBLE);
            if (0 == lastSeen) {
                userstatus.setText(getResources().getString(R.string.online_text));
            } else if (lastSeen < 0) {
                // never seen or group
                userstatus.setVisibility(View.GONE);
            } else {
                String seenStatus = "";
                if (lastSeen >= 2 * 3600 * 24) {
                    seenStatus = (int) mUserProfile.getLastSeen().day + " days ago";
                } else if (lastSeen >= 24 * 3600) {
                    seenStatus = "yesterday";
                } else if (lastSeen >= 2 * 3600) {
                    seenStatus = (int) mUserProfile.getLastSeen().hour + " hours ago";
                } else if (lastSeen >= 3600) {
                    seenStatus = "an hour ago";
                } else if (lastSeen >= 120) {
                    seenStatus = mUserProfile.getLastSeen().min + " minutes ago";
                } else {
                    seenStatus = "a few moments ago";
                }

                userstatus.setText("Last seen " + seenStatus);
            }
        }else {
            userstatus.setVisibility(8);
        }



        fragmentFunctions();

        String destination = "destination";

//        audioCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!MesiboCall.getInstance().callUi(getApplicationContext(), mUserProfile, false))
//                    //MesiboCall.getInstance().callUiForExistingCall(getApplicationContext());
//                    MesiboCall.getInstance().callUiForExistingCall(getApplicationContext());
//                    NotificationSendClass.pushNotifications(getApplicationContext(), mUserProfile.getAddress()
//                        , ""+mUserProfile.getName(), "Incoming Audio Call","AUDIO_CALL","","","");
//            }
//        });
        //android:theme="@style/AppTheme.NoActionBar"   android:configChanges="keyboardHidden|orientation|screenSize

//        videoCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!MesiboCall.getInstance().callUi(getApplicationContext(), mUserProfile, true))
//                    //MesiboCall.getInstance().callUiForExistingCall(getApplicationContext());
//                    //launchCustomCallActivity(destination, true, false);//
//                    launchCustomCallActivity(destination, true, false);
//                    NotificationSendClass.pushNotifications(getApplicationContext(), mUserProfile.getAddress()
//                        , ""+mUserProfile.getName(), "Incoming Video Call","VIDEO_CALL","","","");
//            }
//        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setGroupImage(Bitmap bmp) {
        if (bmp == null) {
            bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.person);
//            bmp = MesiboImages.getDefaultGroupBitmap();
        }
        this.mGroupPicture.setImageDrawable(new RoundImageDrawable(bmp));
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != CAMERA_PERMISSION_CODE) {
            return;
        }
        if (grantResults.length <= 0 || grantResults[0] != 0) {
            Utils.showAlert(ShowProfileActivityNew.this, "Permission Denied", MesiboConfiguration.MSG_PERMISON_CAMERA_FAIL);
        } else {
            MediaPicker.launchPicker(ShowProfileActivityNew.this, MediaPicker.TYPE_CAMERAIMAGE);
        }
    }

    public void onImageEdit(int i, String s, String s1, Bitmap bitmap, int i1) {
        this.mGroupImage = bitmap;
        Log.e("bitmap",bitmap.toString());
        setGroupImage(bitmap);
        if (this.mProfile != null) {
            this.mProfile.setImage(this.mGroupImage);
            this.mProfile.save();
        }
    }

//    protected void launchCustomCallActivity(String destination, boolean video, boolean incoming) {
//        Intent intent = new Intent(getApplicationContext(), MesiboDefaultCallActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("video", video);
//        intent.putExtra("address", destination);
//        intent.putExtra("incoming", incoming);
//        startActivity(intent);
//    }

    public static void launchImageViewer(Activity context, String filePath) {
        MediaPicker.launchImageViewer(context, filePath);
    }

    private void fragmentFunctions() {
        mDefaultProfileBmp = BitmapFactory.decodeResource(this.getResources(), com.mesibo.messaging.R.drawable.default_user_image);
        mThumbnailMediaFiles = new ArrayList<>();
        mGalleryData = new ArrayList<>();

        AlbumListData Images = new AlbumListData();
        Images.setmAlbumName("Images");
        AlbumListData Video = new AlbumListData();
        Video.setmAlbumName("Videos");
        AlbumListData Documents = new AlbumListData();
        Documents.setmAlbumName("Documents");
        mGalleryData.add(Images);
        mGalleryData.add(Video);
        mGalleryData.add(Documents);

        mMediaCardView = (CardView) findViewById(R.id.up_media_layout);
        view7 = findViewById(R.id.view7);
        mMediaCardView.setVisibility(GONE);
        view7.setVisibility(GONE);
        Mesibo.addListener(this);

        mReadSession = mUserProfile.createReadSession(this);
        mReadSession.enableFiles(true);
        mReadSession.enableReadReceipt(true);
        mReadSession.read(100);

//        mMessageBtn = (ImageView) findViewById(R.id.up_message_btn);
//        mMessageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        mRecyclerView = (RecyclerView) findViewById(R.id.showprofile_memebers_rview);

        // change in file
        mAddMemebers = (LinearLayout) findViewById(R.id.showprofile_add_member);
        mAddMemebers.setVisibility(GONE);

        mEditGroup = (LinearLayout) findViewById(R.id.showprofile_editgroup);
        mEditGroup.setVisibility(GONE);


        mll = (LinearLayout) findViewById(R.id.up_status_card);
        mStatus = (TextView) findViewById(R.id.up_status_text);
        mStatusTime = (TextView) findViewById(R.id.up_status_update_time);
        mMobileNumber = (TextView) findViewById(R.id.up_number);
        mPhoneType = (TextView) findViewById(R.id.up_phone_type);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mAdapter = new GroupMemeberAdapter(ShowProfileActivityNew.this, mGroupMemberList);
        mRecyclerView.setAdapter(mAdapter);
        ///
        mGallery = (LinearLayout) findViewById(R.id.up_gallery);
        mMediaCounterView = (TextView) findViewById(R.id.up_media_counter);
        mMediaCounterView.setText(String.valueOf(mMediaFilesCounter) + "\u3009 ");

        mStatusPhoneCard = (CardView) findViewById(R.id.status_phone_card);
        mGroupMembersCard = (CardView) findViewById(R.id.showprofile_members_card);
        mExitGroupCard = (CardView) findViewById(R.id.group_exit_card);
        mExitGroupText = (TextView) findViewById(R.id.group_exit_text);
        mExitGroupCard.setVisibility(GONE);
        mExitGroupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelfMember.isOwner()) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ShowProfileActivityNew.this);
                    builder1.setMessage(getResources().getString(R.string.delete_group_confirmation));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getResources().getString(R.string.ok_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //put your code that needed to be executed when okay is clicked
                                    mUserProfile.getGroupProfile().deleteGroup();
                                    finish();
                                    dialog.cancel();
                                }
                            });
                    builder1.setNegativeButton(getResources().getString(R.string.cancel_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                } else {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ShowProfileActivityNew.this);
                    builder1.setMessage(getResources().getString(R.string.leave_group_confirmation));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getResources().getString(R.string.ok_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //put your code that needed to be executed when okay is clicked
                                    mUserProfile.getGroupProfile().leave();
                                    finish();
                                    dialog.cancel();
                                }
                            });
                    builder1.setNegativeButton(getResources().getString(R.string.cancel_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }

            }
        });

        String status = mUserProfile.getString("status", "");
        mStatus.setText(status);

        CardView e2ecard = (CardView) findViewById(R.id.e2ee_card);
        e2ecard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesiboUI.showEndToEndEncryptionInfo(ShowProfileActivityNew.this, mUserProfile.getAddress(), mUserProfile.getGroupId());
            }
        });


//        if (mUser.isGroup()) {
//         //   findViewById(R.id.block_layout).setVisibility(GONE);
//        }

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.up_open_media);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mGalleryData.size() > 0) {
                    for (int i = mGalleryData.size() - 1; i >= 0; i--) {
                        AlbumListData tempdata = mGalleryData.get(i);
                        if (tempdata.getmPhotoCount() == 0)
                            mGalleryData.remove(tempdata);
                    }

                    UIManager.launchAlbum(ShowProfileActivityNew.this, mGalleryData);
                }
            }
        });
    }


    public void addThumbnailToGallery(MesiboMessage msg) {
        View thumbnailView = null;
        String path = msg.getFilePath();
        mThumbnailMediaFiles.add(path);
        if (mThumbnailMediaFiles.size() < MAX_THUMBNAIL_GALERY_SIZE) {
            if (null != path) {
                thumbnailView = getThumbnailView(msg.getThumbnail(), msg.hasVideo());
                if (null != thumbnailView) {
                    thumbnailView.setClickable(true);
                    thumbnailView.setTag(mMediaFilesCounter - 1);
                    thumbnailView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int index = (int) v.getTag();
                            //String path = (String) mThumbnailMediaFiles.get(index);
                            UIManager.launchImageViewer(ShowProfileActivityNew.this, mThumbnailMediaFiles, index);
                        }
                    });
                    mGallery.addView(thumbnailView);
                }
            }
        }
    }


    View getThumbnailView(Bitmap bm, Boolean isVideo) {
        Context activity = ShowProfileActivityNew.this;
        if (null == activity) return null;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.video_layer_layout_horizontal_gallery, null, false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView thumbpic = (ImageView) view.findViewById(R.id.mp_thumbnail);
        thumbpic.setImageBitmap(bm);
        //thumbpic.setScaleType(ImageView.ScaleType.CENTER_CROP);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView layer = (ImageView) view.findViewById(R.id.video_layer);
        layer.setVisibility(isVideo ? VISIBLE : GONE);
        DisplayMetrics metrics = new DisplayMetrics();
        ShowProfileActivityNew.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) ((metrics.widthPixels - 50) / (5)); //number of pics in media view
        view.setLayoutParams(new ViewGroup.LayoutParams(width, width));
        return view;
    }

    private void setUserPicture() {
        Bitmap b = mUserProfile.getImage().getImageOrThumbnail();
        if (null != b) {
            mUsermageView.setImageDrawable(new com.mesibo.messaging.RoundImageDrawable(b));
            //mUsermageView.setImageBitmap(b);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUsermageView.setImageDrawable(getDrawable(com.mesibo.calls.api.R.drawable.default_user_image));
            }
        }
    }

    @Override
    public void Mesibo_onMessage(MesiboMessage msg) {
        if (!msg.hasFile()) return;
        mMediaCardView.setVisibility(VISIBLE);
        view7.setVisibility(VISIBLE);
        mMediaFilesCounter++;
        mMediaCounterView.setText(String.valueOf(mMediaFilesCounter) + "\u3009 ");
        AlbumPhotosData newPhoto = new AlbumPhotosData();
        newPhoto.setmPictueUrl(msg.getFilePath());
        newPhoto.setmSourceUrl(msg.getFilePath());
        AlbumListData tempAlbum;
        int index = 0;
        if (msg.hasVideo())
            index = 1;
        else if (!msg.hasImage())
            index = 2;
        tempAlbum = mGalleryData.get(index);

        if (tempAlbum.getmPhotosList() == null) {
            ArrayList<AlbumPhotosData> newPhotoList = new ArrayList<>();
            tempAlbum.setmPhotosList(newPhotoList);
        }
        if (tempAlbum.getmPhotosList().size() == 0) {
            tempAlbum.setmAlbumPictureUrl(msg.getFilePath());
        }
        tempAlbum.getmPhotosList().add(newPhoto);
        tempAlbum.setmPhotoCount(tempAlbum.getmPhotosList().size());
        addThumbnailToGallery(msg);
        return;
    }

    @Override
    public void Mesibo_onMessageStatus(MesiboMessage msg) {

    }

    @Override
    public void Mesibo_onMessageUpdate(MesiboMessage mesiboMessage) {

    }

    public boolean parseGroupMembers(MesiboGroupProfile.Member[] users) {
        if (null == users) return false;

        String phone = SampleAPI.getPhone();
        if (TextUtils.isEmpty(phone))
            return false;

        mGroupMemberList.clear();

        for (int i = 0; i < users.length; i++) {
            String peer = users[i].getAddress();
            if (phone.equalsIgnoreCase(peer)) {
                mSelfMember = users[i];
            }

            mGroupMemberList.add(users[i]);
        }

        if (null == mSelfMember) {
            mExitGroupText.setVisibility(GONE);
            mAddMemebers.setVisibility(GONE);
            mEditGroup.setVisibility(GONE);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        //only owner can delete group
        mExitGroupText.setText(mSelfMember.isOwner() ? getResources().getString(R.string.delete_group) :
                getResources().getString(R.string.exit_group));

        if (mUserProfile.groupid > 0) {
            mAddMemebers.setVisibility(mSelfMember.isAdmin() && mUserProfile.isActive() ? VISIBLE : GONE);
            mEditGroup.setVisibility(mUserProfile.getGroupProfile().canModify() ? VISIBLE : GONE);
        }

        mAdapter.notifyDataSetChanged();
        return true;
    }

    public void updateMember(MesiboGroupProfile.Member m) {
        for (int i = 0; i < mGroupMemberList.size(); i++) {
            MesiboGroupProfile.Member em = mGroupMemberList.get(i);
            if (em.getAddress().equalsIgnoreCase(m.getAddress())) {
                mGroupMemberList.remove(em);
                mGroupMemberList.add(i, m);
                break;
            }
        }
    }

    @Override
    public void Mesibo_onGroupCreated(MesiboProfile mesiboProfile) {

    }

    @Override
    public void Mesibo_onGroupJoined(MesiboProfile mesiboProfile) {

    }

    @Override
    public void Mesibo_onGroupLeft(MesiboProfile mesiboProfile) {

    }

    @Override
    public void Mesibo_onGroupMembers(MesiboProfile mesiboProfile, MesiboGroupProfile.Member[] members) {
        parseGroupMembers(members);
    }

    @Override
    public void Mesibo_onGroupMembersJoined(MesiboProfile mesiboProfile, MesiboGroupProfile.Member[] members) {
        if (null == members) return;

        for (MesiboGroupProfile.Member m : members) {
            updateMember(m);
        }
        mAdapter.notifyDataSetChanged();
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
    public void MesiboProfile_onUpdate(MesiboProfile userProfile) {
        if (null != mAdapter)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void MesiboProfile_onEndToEndEncryption(MesiboProfile mesiboProfile, int i) {

    }

    @Override
    public void MesiboProfile_onPublish(MesiboProfile mesiboProfile, boolean b) {

    }


    @Override
    public void onResume() {
        super.onResume();


        Mesibo.setForegroundContext(this, 0x102, true);

        if (mUserProfile.groupid > 0) {
            TextView userName = (TextView) findViewById(R.id.up_user_name);
            if (null != mUserProfile.getName())
                userName.setText(mUserProfile.getName());
        }

        setUserPicture();

        if (mUserProfile.groupid > 0) {
            boolean isActive = mUserProfile.isActive();
            mExitGroupCard.setVisibility(isActive ? VISIBLE : GONE);
            mAddMemebers.setVisibility(isActive ? VISIBLE : GONE);
            mGroupMembersCard.setVisibility(VISIBLE);
//            mStatusPhoneCard.setVisibility(GONE);
            mAddMemebers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupid", mUserProfile.groupid);
                    Intent intent = new Intent(getApplicationContext(), ContactScreenActivity.class);
                    intent.putExtra("isTask","true");
                    intent.putExtra("groupId",mUserProfile.groupid);
                    startActivity(intent);
                    ShowProfileActivityNew.this.finish();
                }
            });

            mEditGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIManager.launchEditProfile(ShowProfileActivityNew.this, 0, mUserProfile.groupid, false);
                    //UIManager.launchMesiboContacts(getActivity(), 0, MODE_EDITGROUP, 0,bundle);
                    ShowProfileActivityNew.this.finish();
                }
            });


            mUserProfile.getGroupProfile().getMembers(100, true, this);

        } else {
            mExitGroupCard.setVisibility(GONE);
            mGroupMembersCard.setVisibility(GONE);
            //mStatusPhoneCard.setVisibility(VISIBLE);


//            mStatusTime.setText((""));
//            mMobileNumber.setText((mUserProfile.address));
//            mPhoneType.setText("Mobile");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Mesibo.setForegroundContext(this, 0x102, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mesibo.setForegroundContext(this, 0x102, false);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void Mesibo_onConnectionStatus(int i) {
        Log.d("Qamp", "Connection status: " + i);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

//    @Override
//    public void MesiboCall_OnError(MesiboCall.CallProperties callProperties, int i) {
//
//    }
//
//    @Override
//    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile mesiboProfile, boolean z) {
//        MesiboCall.CallProperties cc = MesiboCall.getInstance().createCallProperties(z);
//        cc.parent = getApplicationContext();
//        cc.user = mesiboProfile;
//        cc.className = MesiboDefaultCallActivity.class;
//        return cc;
//    }

//    @Override
//    public boolean MesiboCall_OnShowUserInterface(MesiboCall.Call call, MesiboCall.CallProperties callProperties) {
//        launchCustomCallActivity(callProperties.user.address, callProperties.video.enabled, true);
//        return true;
//    }

//    @Override
//    public boolean MesiboCall_onNotify(int i, MesiboProfile mesiboProfile, boolean z) {
//        return false;
//    }

    public class GroupMemeberAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context mContext = null;
        private ArrayList<MesiboGroupProfile.Member> mDataList = null;

        public GroupMemeberAdapter(Context context, ArrayList<MesiboGroupProfile.Member> list) {
            this.mContext = context;
            mDataList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.showprofile_group_member_rv_item, parent, false);
            return new GroupMembersCellsViewHolder(view);

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holderr, @SuppressLint("RecyclerView") final int position) {
            final int pos = position;
            final MesiboGroupProfile.Member member = mDataList.get(position);
            final MesiboProfile user = member.getProfile();
            final GroupMembersCellsViewHolder holder = (GroupMembersCellsViewHolder) holderr;

            if(member.getProfile().getName().equals(Mesibo.getSelfProfile().getName())){
                holder.mContactsName.setText(getString(R.string.You_text));
            }else
            holder.mContactsName.setText(user.getNameOrAddress());

            Bitmap memberImage = user.getImage().getImage();
            if (null != memberImage)
                holder.mContactsProfile.setImageDrawable(new RoundImageDrawable(memberImage));
            else
                holder.mContactsProfile.setImageDrawable(new RoundImageDrawable(mDefaultProfileBmp));

            if (member.isAdmin()) {
                holder.mAdminTextView.setVisibility(VISIBLE);
            } else {
                holder.mAdminTextView.setVisibility(GONE);
            }



            holder.mContactsStatus.setText("");

            // only admin can have menu, also owner can't be deleted

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final MesiboGroupProfile.Member member = mDataList.get(position);
                    final MesiboProfile profile = member.getProfile();

                    if (!mSelfMember.isAdmin()) {
                        if (profile.isSelfProfile()) {
                            return;
                        }

                        MesiboUI.launchMessageView(ShowProfileActivityNew.this, profile);
                        ShowProfileActivityNew.this.finish();
                        return;
                    }

                    ArrayList<String> items = new ArrayList<String>();

                    if (!member.isAdmin()) {
                        items.add("Make Admin");

                    } else {
                        items.add("Remove Admin");
                    }

                    // don't allow self MessagingModule or self delete member
                    if (!profile.isSelfProfile()) {
                        items.add("Delete member");
                        items.add("Message");
                    }

                    CharSequence[] cs = items.toArray(new CharSequence[items.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //builder.setTitle("Select The Action");
                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            //Delete member
                            if (item == 1) {
                                String[] members = new String[1];
                                members[0] = mDataList.get(position).getAddress();
                                mUserProfile.getGroupProfile().removeMembers(members);
                                mDataList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();

                            } else if (item == 0) {
                                String[] members = new String[1];
                                members[0] = mDataList.get(position).getAddress();
                                mUserProfile.getGroupProfile().addMembers(members, MesiboGroupProfile.MEMBERFLAG_ALL, member.isAdmin() ? 0 : MesiboGroupProfile.ADMINFLAG_ALL);
                            } else if (2 == item) {
                                MesiboUI.launchMessageView(ShowProfileActivityNew.this, profile);
                                ShowProfileActivityNew.this.finish();
                                return;
                            }
                        }
                    });
                    builder.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public class GroupMembersCellsViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString = null;
            public View mView = null;
            public ImageView mContactsProfile = null;
            public TextView mContactsName = null;
            public TextView mAdminTextView = null;
            public EmojiconTextView mContactsStatus = null;

            public GroupMembersCellsViewHolder(View view) {
                super(view);
                mView = view;
                mContactsProfile = (ImageView) view.findViewById(R.id.sp_rv_profile);
                mContactsName = (TextView) view.findViewById(R.id.sp_rv_name);
                mContactsStatus = (EmojiconTextView) view.findViewById(R.id.sp_memeber_status);
                mAdminTextView = (TextView) view.findViewById(R.id.admin_info);
            }
        }

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Uri uri = data.getData();
//        if (uri != null) {
//
//            mUsermageView.setImageURI(uri);
//        } else {
//            Toast.makeText(getApplicationContext(),"Something went Wrong",Toast.LENGTH_SHORT).show();
//        }
//    }
}
