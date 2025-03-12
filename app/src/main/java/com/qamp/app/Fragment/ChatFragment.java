package com.qamp.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.MesiboReadSession;
import com.mesibo.messaging.UserData;

import com.qamp.app.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatFragment extends Fragment implements Mesibo.MessageListener {

    private RecyclerView userListRecycler;
//    private ChatFragmentAdapter adapter;
    private Set<String> uniqueProfiles = new HashSet<>(); // Initialize this somewhere in your code
    public int mSelectionMode = 0;
    private ArrayList<MesiboProfile> mUser = new ArrayList<>();
    private ArrayList<MesiboMessage> messageList = new ArrayList<>();

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        userListRecycler = view.findViewById(R.id.userList_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        userListRecycler.setLayoutManager(layoutManager);

//        this.adapter = new ChatFragmentAdapter(getContext(),mUser,this,messageList);

//        userListRecycler.setAdapter(this.adapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Mesibo.addListener(this);
        MesiboReadSession mReadSession = new MesiboProfile().createReadSession(this);
        mReadSession.enableReadReceipt(false);
        mReadSession.enableMissedCalls(false);
        mReadSession.read(100);
        Log.e("work", String.valueOf(mReadSession.getTotalMessageCount()));
    }

    @Override
    public void Mesibo_onMessage(@NotNull MesiboMessage mesiboMessage) {

        mUser.add(mesiboMessage.profile);
//        adapter.notifyDataSetChanged();

        addMessage(mesiboMessage);

        Log.e("profile",mesiboMessage.profile.address.toString());
    }

    private void addMessage(MesiboMessage mesiboMessage) {
        UserData userData = UserData.getUserData(mesiboMessage);

        userData.setMessage(mesiboMessage);

    }

    @Override
    public void Mesibo_onMessageStatus(@NotNull MesiboMessage mesiboMessage) {



    }

    @Override
    public void Mesibo_onMessageUpdate(@NotNull MesiboMessage mesiboMessage) {

    }
}
