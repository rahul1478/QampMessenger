package com.qamp.app.Activity;



import static com.qamp.app.Activity.CreateGroupActivity.mMemberProfiles;
import static com.qamp.app.Adapter.QampContactScreenAdapter.slectedgtoup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.Profile;
import com.qamp.app.Adapter.QampContactScreenAdapter;
import com.qamp.app.Fragment.CreateNewGroupFragment;
import com.qamp.app.MesiboImpModules.ContactSyncClass;
import com.qamp.app.MessagingModule.MesiboMessagingActivity;
import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.MessagingModule.UserListFragment;
import com.qamp.app.Modal.QampContactScreenModel;
import com.qamp.app.R;
import com.qamp.app.Utils.AppUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ContactScreenActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 101;
    ImageView backBtn, moreOptionBtn, imageView3, create_group_text, next_group;
    TextView selectedContacts, create_group, clearSelectionText;
    EditText phoneNumberText;
    RecyclerView contacts;
    String forwardMessages;
    ConstraintLayout constraintLayout;
    private boolean isAddMember = false;
    private QampContactScreenAdapter adapter;
    private boolean isGroupMakingProcedureActive = false;
    private Long groupId = 0L;
    private MesiboProfile mProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setStatusBarColor(ContactScreenActivity.this, R.color.colorAccent);
        setContentView(R.layout.activity_contact_screen);
        initViews();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        } else {
            // Permission has already been granted
            //ContactSyncClass.getContactData();
        }
        Mesibo.addListener(this);
        if (!isGroupMakingProcedureActive) {
            showList(false, next_group, selectedContacts);
            isGroupMakingProcedureActive = false;
        }

        forwardMessages = getIntent().getStringExtra("message");

        String task = getIntent().getStringExtra("isTask");
        groupId = getIntent().getLongExtra("groupId",0L);

        if (groupId != 0){
            mProfile = Mesibo.getProfile(groupId);
        }

        if (task != null){
            if (task.equals("true")){
                create_group.setText("Add Members");
                create_group_text.setVisibility(View.VISIBLE);
                clearSelectionText.setVisibility(View.VISIBLE);
                showList(true, create_group_text, selectedContacts);
                isGroupMakingProcedureActive = true;
                isAddMember = true;
            }
        }

        ArrayList<QampContactScreenModel> Search = ContactSyncClass.contacts;

        phoneNumberText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<QampContactScreenModel> filter = new ArrayList<>();
                for (QampContactScreenModel item : Search) {
                    if (item.getMes_rv_name().toLowerCase().contains(s.toString().toLowerCase())) {
                        filter.add(item);
                    }
                }

                adapter.updateData(filter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGroupMakingProcedureActive) {
                    create_group_text.setVisibility(View.VISIBLE);
                    clearSelectionText.setVisibility(View.VISIBLE);
                    showList(true, create_group_text, selectedContacts);
                    isGroupMakingProcedureActive = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        create_group.setBackgroundColor(ContactScreenActivity.this.getColor(R.color.light_orange));
                        next_group.setBackgroundColor(ContactScreenActivity.this.getColor(R.color.light_orange));
                        constraintLayout.setBackgroundColor(ContactScreenActivity.this.getColor(R.color.light_orange));
                    }
                }
            }
        });


        create_group_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAddMember){
                    CreateNewGroupFragment.mMemberProfiles.clear();
                    ContactSyncClass.getMyQampContacts(ContactScreenActivity.this);
                    if (slectedgtoup.size() >= 1) {
                        Iterator<MesiboProfile> it = slectedgtoup.iterator();
                        while (it.hasNext()) {
                            MesiboProfile d = it.next();
                            CreateNewGroupFragment.mMemberProfiles.add(d);
                        }

                        addGroupMembers();

                    } else if (slectedgtoup.size() == 0 || slectedgtoup.size() == 1) {
                        Toast.makeText(ContactScreenActivity.this, "Please Select atleast one members to create group", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    CreateNewGroupFragment.mMemberProfiles.clear();
                    ContactSyncClass.getMyQampContacts(ContactScreenActivity.this);
                    if (slectedgtoup.size() >= 2) {
                        Iterator<MesiboProfile> it = slectedgtoup.iterator();
                        while (it.hasNext()) {
                            MesiboProfile d = it.next();
                            CreateNewGroupFragment.mMemberProfiles.add(d);
                        }
                        Intent intent = new Intent(ContactScreenActivity.this, CreateNewGroupActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        for (int i = 0; i < ContactSyncClass.groupContacts.size(); i++)
                            ContactSyncClass.groupContacts.get(i).setChecked(false);
                    } else if (slectedgtoup.size() == 0 || slectedgtoup.size() == 1) {
                        Toast.makeText(ContactScreenActivity.this, "Please Select atleast two members to create group", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ContactScreenActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void showList(boolean isGroupMaking, ImageView next_group, TextView selectedContacts) {
        if (isGroupMaking) {
            slectedgtoup.clear();
            adapter = new QampContactScreenAdapter(ContactScreenActivity.this,
                    ContactSyncClass.groupContacts, isGroupMaking, create_group_text, selectedContacts);
            selectedContacts.setText(slectedgtoup.size() + " Contacts Selected");
        } else {
            slectedgtoup.clear();
            adapter = new QampContactScreenAdapter(ContactScreenActivity.this,
                    ContactSyncClass.contacts, isGroupMaking, create_group_text, selectedContacts);
            selectedContacts.setText(ContactSyncClass.deviceContactList.size() + " Contacts");
        }
        contacts.setAdapter(adapter);
        contacts.setLayoutManager(new LinearLayoutManager(ContactScreenActivity.this));
        adapter.notifyDataSetChanged();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        moreOptionBtn = findViewById(R.id.moreOptionBtn);
        imageView3 = findViewById(R.id.imageView3);
        create_group_text = findViewById(R.id.create_group_text);
        selectedContacts = findViewById(R.id.selectedContacts);
        create_group = findViewById(R.id.create_group);
        clearSelectionText = findViewById(R.id.clearSelectionText);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        contacts = findViewById(R.id.contacts);
        contacts.getRecycledViewPool().setMaxRecycledViews(0, 0);

        constraintLayout = findViewById(R.id.constraintLayout);
        next_group = findViewById(R.id.next_group);
    }


    public void addGroupMembers() {
        Log.e("member", String.valueOf(slectedgtoup.size()));
        ArrayList<String> members = new ArrayList<>();
        for (int i = 0; i < slectedgtoup.size(); i++) {
            MesiboProfile mp = slectedgtoup.get(i);
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



            onBackPressed();
            finish();
        }
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

}
