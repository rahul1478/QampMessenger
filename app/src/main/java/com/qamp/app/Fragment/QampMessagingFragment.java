package com.qamp.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboMessageProperties;
import com.mesibo.api.MesiboProfile;
import com.mesibo.api.MesiboReadSession;
import com.mesibo.emojiview.EmojiconEditText;
import com.qamp.app.Adapter.MessageAdapter;
import com.qamp.app.MessagingModule.MesiboMessagingFragment;
import com.qamp.app.MessagingModule.MessageData;
import com.qamp.app.MessagingModule.UserData;
import com.qamp.app.R;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class QampMessagingFragment extends Fragment {

    ImageButton sendButton,cameraButton;
    EmojiconEditText chatEditText;

    private boolean showLoadMore = true;
    private boolean mLoading = false;
    private long mLoadTs = 0L;

    private int mLastReadCount = 0;
    private int mLastMessageCount = 0;
    private boolean read_flag = false;
     private MesiboReadSession mReadSession = null;
    public Activity mActivity = null;

    ArrayList<MesiboMessage> messageList =new ArrayList<>();
    private boolean mShowMissedCalls = true;

    private boolean forMe = false;

    private UserData mUserData = null;
    private MesiboProfile mUser = null;
    RecyclerView mainRecycler;
     private int mNonDeliveredCount = 0;

    String receiverAddress;
    Long groupId;

    String myAddress;
    ArrayList<MessageData> mList =new ArrayList<>();
    private MessageAdapter messageAdapter;

    public QampMessagingFragment(String address ,long group_id) {
        // Required empty public constructor
        receiverAddress = address;
        groupId = group_id;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qamp_messaging, container, false);





        sendButton = view.findViewById(R.id.sendmessage);
        chatEditText = view.findViewById(R.id.chat_edit_text);
        cameraButton = view.findViewById(R.id.cameraButton);

        mainRecycler = view.findViewById(R.id.chat_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mainRecycler.setLayoutManager(layoutManager);

        MesiboProfile selfProfile = Mesibo.getSelfProfile();
        this.myAddress = selfProfile.address;


//        this.messageAdapter = new  MessageAdapter(getContext(),mList,myAddress,messageList);
//
//        mainRecycler.setAdapter(this.messageAdapter);


        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    sendButton.setVisibility(View.GONE);
                    cameraButton.setVisibility(View.VISIBLE);
                }
                else{
                    sendButton.setVisibility(View.VISIBLE);
                    cameraButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = chatEditText.getText().toString().trim();
                addMessage(message);
                chatEditText.setText("");
            }
        });


        return view;

    }

    private void addMessage(String text) {

        MesiboProfile receiver = Mesibo.getProfile(receiverAddress);
        MesiboMessage message = receiver.newMessage();
        message.message = text;
        message.send();



    }




}
