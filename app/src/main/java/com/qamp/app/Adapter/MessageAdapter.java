package com.qamp.app.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboFile;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.Activity.ContactScreenActivity;
import com.qamp.app.Activity.ForwardActivity;
import com.qamp.app.CustomClasses.TimeFormatConverter;
import com.qamp.app.MessagingModule.AllUtils.MyTrace;
import com.qamp.app.MessagingModule.MesiboConfiguration;
import com.qamp.app.MessagingModule.MesiboImages;
import com.qamp.app.MessagingModule.MesiboRecycleViewHolder;
import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.MessagingModule.MessageData;
import com.qamp.app.MessagingModule.MessageViewHolder;
import com.qamp.app.MessagingModule.SelectableAdapter;
import com.qamp.app.MessagingModule.UserListFragment;
import com.qamp.app.MessagingModule.Utils;
import com.qamp.app.R;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MessageAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {

    private MessageViewHolder.ClickListener clickListener = null;
    private List<com.qamp.app.MessagingModule.MessageData> mChatList = null;
    private Context mContext = null;
    private MessageData mData = null;

    private GoogleMap mMap;


    private LayoutInflater mInflater;
    private static final float MIN_SWIPE_DISTANCE = 100; // Adjust this threshold as needed
    private float initialX;
    private WeakReference<MesiboRecycleViewHolder.Listener> mCustomViewListener = null;
    private String mDateCoin = null;
    private int mDisplayMsgCnt = 0;
    private ImageView mImageVu = null;
    private MessagingAdapterListener mListener = null;
    int mOriginalId = 0;
    private ProgressBar mProgress = null;
    private SwipeCallback swipeCallback;

    private int mTotalMessages = 0;
    private MapView mapLayout;

    private LongPressListener longPressListener;

    private int mcellHeight = 0;

    public interface MessagingAdapterListener {
        boolean isMoreMessage();

        void loadMoreMessages();

        void showMessageInvisible();
    }

    public void setSwipeCallback(SwipeCallback swipeCallback) {
        this.swipeCallback = swipeCallback;
    }
    public interface LongPressListener {
        void onItemLongPressed(int Position);

        void onItemClick(int position);

//        void onSwipeListener(int position,MessageData data);
    }

    public interface SwipeCallback {
        void onSwipe(int position, MessageData data);
    }

    public MessageAdapter(Context context, MessagingAdapterListener listener, List<com.qamp.app.MessagingModule.MessageData> ChatList, MessageViewHolder.ClickListener cl1, MesiboRecycleViewHolder.Listener customViewListner) {
        this.mContext = context;
        this.mChatList = ChatList;
        this.mListener = listener;
        this.clickListener = cl1;
        setListener(customViewListner);
        this.mDisplayMsgCnt = 30;
        this.mDateCoin = "";
        this.mTotalMessages = this.mChatList.size();
        this.mDisplayMsgCnt = this.mTotalMessages;
        this.mcellHeight = 0;
    }

    public int getItemViewType(int position) {
        MesiboRecycleViewHolder.Listener l;
        int viewType;
        MesiboMessage m = this.mChatList.get(position).getMesiboMessage();
        if (m.isDate()) {
            return 3;
        }
        if (37 == m.getStatus()) {
            return 4;
        }
        if (m.isCustom()) {
            return 100;
        }
        if (this.mCustomViewListener != null && (l = (MesiboRecycleViewHolder.Listener) this.mCustomViewListener.get()) != null && (viewType = l.Mesibo_onGetItemViewType(m)) >= 100) {
            return viewType;
        }
        int status = this.mChatList.get(position).getStatus();
        if (21 == status) {
            return 5;
        }
        if (35 == status) {
            return 6;
        }
        if (18 == status || 19 == status) {
            return 1;
        }
        return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MesiboRecycleViewHolder view = null;
        if (this.mCustomViewListener != null) {
            MesiboRecycleViewHolder.Listener l = (MesiboRecycleViewHolder.Listener) this.mCustomViewListener.get();
            if (l != null) {
                view = l.Mesibo_onCreateViewHolder(parent, viewType);
            }
            if (view != null) {
                view.setCustom(true);
            }
        }
        if (viewType == 7) {
            view = new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_empty_view, parent, false));
        } else if (viewType == 2) {
            view = new MessagesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_send_msg_item, parent, false));
        } else if (viewType == 1) {
            view = new MessagesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.other_recieve_msg_item, parent, false));
        } else if (viewType == 3) {
            view = new DateViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_date_view, parent, false));
        } else if (viewType == 4) {
            view = new SystemMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_systemmessage_view, parent, false), this.mContext, MesiboConfiguration.headerCellBackgroundColor, false);
        } else if (viewType == 5) {
            view = new SystemMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_systemmessage_view, parent, false), this.mContext, MesiboConfiguration.otherCellsBackgroundColor, true);
        } else if (viewType == 6) {
            view = new SystemMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_systemmessage_view, parent, false), this.mContext, MesiboConfiguration.headerCellBackgroundColor, true);
        } else if (viewType >= 100) {
            view = new SystemMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(com.mesibo.messaging.R.layout.chat_systemmessage_view, parent, false), this.mContext, MesiboConfiguration.otherCellsBackgroundColor, false);
        }

        if (view != null) {
            view.setType(viewType);
        }
        MyTrace.stop();
        if (view != null) {
            view.setAdapter(this);
        }
        return view;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        String msg;
        MesiboUI.Config opts = MesiboUI.getConfig();
        MyTrace.start("Messaging-BVH");
        if (position >= 20) {
            this.mListener.showMessageInvisible();
        } else if (this.mListener.isMoreMessage()) {
            this.mListener.loadMoreMessages();
        }
        MesiboRecycleViewHolder h = (MesiboRecycleViewHolder) holder;
        int type = h.getType();
        h.setItemPosition(position);
        MessageData cm = this.mChatList.get(position);
//        Log.e("messgaes",cm.getMessage());
        cm.setDirty(false);
        if (h.getCustom()) {
            MesiboRecycleViewHolder.Listener l = (MesiboRecycleViewHolder.Listener) this.mCustomViewListener.get();
            cm.setViewHolder(h);
            if (l != null) {
                l.Mesibo_onBindViewHolder(h, type, isSelected(position), cm.getMesiboMessage());
            }
            MyTrace.stop();
        } else {
            if (position > 0 && cm.getGroupId() > 0) {
                cm.checkPreviousData(this.mChatList.get(position - 1));
            }
            if (7 != type) {
                if (3 == type) {
                    ((DateViewHolder) holder).mDate.setText(cm.getDateStamp());
                } else if (1 == type || 2 == type) {
                    ((MessagesViewHolder) holder).setData(cm, position, isSelected(position));
                    Log.e("check", "1");
                } else if (4 == type) {
                    ((SystemMessageViewHolder) holder).setText(opts.headerTitle, MesiboImages.getHeaderImage());
                } else if (6 == type) {
                    SystemMessageViewHolder smvh = (SystemMessageViewHolder) holder;
                    int messageType = cm.getMessageType();
                    if (messageType == 1) {
                        msg = opts.e2eeActive;
                    } else if (messageType == 3) {
                        msg = opts.e2eeIdentityChanged;
                    } else {
                        msg = opts.e2eeInactive;
                    }
                    smvh.setText(msg.replaceAll("AppName", Mesibo.getAppName()), MesiboImages.getE2EEImage());
                } else if (5 == type) {
                    SystemMessageViewHolder smvh2 = (SystemMessageViewHolder) holder;
                    if ((cm.getMessageType() & 1) > 0) {
                        smvh2.setText(opts.missedVideoCallTitle + " " + opts.f0at + " " + cm.getTimestamp(), MesiboImages.getMissedVideoCallImage());
                    } else {
                        smvh2.setText(opts.missedVoiceCallTitle + " " + opts.f0at + " " + cm.getTimestamp(), MesiboImages.getMissedVoiceCallImage());
                    }
                }
            }
            MyTrace.stop();
        }

    }

//    private void openItemOnLongClick(MessageData data) {
//        // Implement your logic to open the item here
//        // For example, you can start an activity or show a dialog
//        // You can also call your long-press listener if needed
//        if (longPressListener != null) {
//            longPressListener.onItemLongPressed(data);
//        }
//    }

    @Override
    public int getItemCount() {
        if (this.mChatList != null) {
            return this.mChatList.size();
        }
        return 0;
    }

    private boolean isSwipeAction(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Store the initial touch coordinates
                initialX = event.getX();
                return false;
            case MotionEvent.ACTION_UP:
                // Calculate the distance between initial and final touch coordinates
                float finalX = event.getX();
                float distanceX = finalX - initialX;

                // Check if the horizontal swipe distance exceeds the threshold
                if (distanceX > MIN_SWIPE_DISTANCE) {
                    Toast.makeText(mContext,"right",Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false; // Not a right swipe
            default:
                return false; // Not a swipe action
        }
    }

    public class MessagesViewHolder extends MesiboRecycleViewHolder {
        private TextView messageTextView;
        private TextView timeTextView;

        private ImageView readStatus;
        private TextView text_time_image;
        private ImageView read_image;

        private ImageView forward;
        private LinearLayout imageLayout;
        private LinearLayout textLayout;
        private CardView imageCard;
        private ImageView image_display;
        private TextView document_name;
        private ConstraintLayout document_card;
        private ConstraintLayout audio_layout;
        private TextView audioLength;
        protected View mSelectedOverlay;

        private RelativeLayout relativeLayout;
        private LinearLayout linearLayout;
        private LinearLayout otherLayout;
        private ShapeableImageView otherProfile;
        private TextView otherName;

        private LinearLayout mReplyLayout;
        private ImageView mReplyImage;
        private TextView mReplyUserName;

        private TextView mReplyMessage;




//        private ImageView seenIcon;

        public MessagesViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_content);
            timeTextView = itemView.findViewById(R.id.text_time);
            readStatus = itemView.findViewById(R.id.mes_cont_status);
            forward = itemView.findViewById(R.id.forward);
//            textLayout = itemView.findViewById(R.id.textMsgLayout);
            imageCard = itemView.findViewById(R.id.image_card);
            image_display = itemView.findViewById(R.id.display_image);
            document_card = itemView.findViewById(R.id.document_card);
            document_name = itemView.findViewById(R.id.document_name);
            audio_layout = itemView.findViewById(R.id.audio_layout);
            audioLength = itemView.findViewById(R.id.audio_length);
            mapLayout = itemView.findViewById(R.id.mapView);
            this.mSelectedOverlay = itemView.findViewById(R.id.selected_overlay);
            relativeLayout = itemView.findViewById(R.id.rel_parent);
            linearLayout = itemView.findViewById(R.id.lyt_parent);
            otherLayout = itemView.findViewById(R.id.other_layout);
            otherProfile = itemView.findViewById(R.id.other_profile);
            otherName = itemView.findViewById(R.id.other_name);
            this.mReplyLayout = itemView.findViewById(R.id.reply_layout);
            this.mReplyImage = (ImageView) itemView.findViewById(R.id.reply_image);
            this.mReplyUserName = (TextView) itemView.findViewById(R.id.reply_name);
            this.mReplyMessage = (TextView) itemView.findViewById(R.id.reply_text);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void setData(com.qamp.app.MessagingModule.MessageData cm, int position, boolean selected) {
            int i = 8;
            int i2 = 0;
            reset();
            setItemPosition(position);
            mData = cm;
            mData.setViewHolder(this);
            this.document_card.setVisibility(8);
            this.audio_layout.setVisibility(8);
//            Log.e("messages",mData.getMessage());

            if (1 == getType()){
                if (cm.getGroupId() == 0){
                    otherLayout.setVisibility(8);
                }else {
                    otherLayout.setVisibility(0);
                    otherName.setText(mData.getUsername());
                    if (mData.getMesiboMessage().profile.getImage().getThumbnail() != null){
                        otherProfile.setImageBitmap(mData.getMesiboMessage().profile.getImage().getThumbnail());
                    }else {
                        otherProfile.setImageResource(R.drawable.person);
                    }

                }
            }

            String message = mData.getMessage();
            Bitmap image = mData.getImage();


            MesiboFile file = mData.getMesiboMessage().getFile();

            if (mData.isReply()) {
                loadReplyView();
                mReplyLayout.setVisibility(0);
                Log.e("reply", "this is reply");
                if (mData.getReplyString() != null) {
                    mReplyMessage.setText(mData.getReplyString());
                } else {
                    mReplyMessage.setText("");
                }
                mReplyUserName.setText(mData.getReplyName());
                if (mData.getReplyBitmap() != null) {
                    mReplyImage.setVisibility(0);
                    mReplyImage.setImageBitmap(mData.getReplyBitmap());
                } else {
                    mReplyImage.setVisibility(8);
                }
            }else {
                mReplyLayout.setVisibility(8);
            }

            if (mData.isDeleted()){
                mReplyLayout.setVisibility(View.GONE);
            }

//            if (mData.getMesiboMessage().hasLocation()){
//                imageCard.setVisibility(8);
////                mapLayout.setVisibility(0);
//                MapView mapView = mapLayout;
//                mapView.onCreate(null);
//                Double Lat = mData.getMesiboMessage().latitude;
//                Double Long = mData.getMesiboMessage().longitude;
//                bindLocation(Lat,Long);
//            }else {

//            }

            if (image != null){
                imageCard.setVisibility(View.VISIBLE);
                image_display.setImageBitmap(image);
                String time  = TimeFormatConverter.convertToAmPm(mData.getTimestamp());
                timeTextView.setText(time);
            }else {
                imageCard.setVisibility(View.GONE);
            }

            if (mData.getMesiboMessage().isDocument()){
                this.image_display.setVisibility(View.VISIBLE);
                this.image_display.setImageBitmap(mData.getMesiboMessage().getThumbnail());
                this.document_card.setVisibility(0);
                document_name.setText(mData.getMesiboMessage().getFileName());
                String time  = TimeFormatConverter.convertToAmPm(mData.getTimestamp());
                timeTextView.setText(time);
            }


            if (!(file == null || this.document_card == null || (3 != file.type && 2 != file.type))){
//                this.document_card.setVisibility(0);
            }

            if (mData.getMesiboMessage().isAudio()){
                String audioFilePath = mData.getMesiboMessage().getFilePath(); // Replace with your audio file path or URI
                MediaPlayer mediaPlayer = new MediaPlayer();

                int minutes = 0;
                try {
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare(); // You must call prepare() before accessing the duration

                    int duration = mediaPlayer.getDuration(); // Duration in milliseconds

                    int seconds = (duration / 1000) % 60;
                    minutes = (duration / 1000) / 60;

                    // Use 'minutes' and 'seconds' as needed

                    mediaPlayer.release(); // Release the MediaPlayer when done
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.audio_layout.setVisibility(0);
                this.audioLength.setText("2.00");
                timeTextView.setText(mData.getTimestamp());
            }

                if (!TextUtils.isEmpty(message)){
                    messageTextView.setText(mData.getMessage());
                    String time  = TimeFormatConverter.convertToAmPm(mData.getTimestamp());
                    timeTextView.setText(time);
                    Log.e("time",mData.getTimestamp());
                }else {
                    messageTextView.setText("");
                }

            try {
                setupMessageStatus(this.readStatus, mData.getStatus());
            }catch (NullPointerException e){
                Log.e("error",e.toString());
            }

            forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ForwardActivity.class);
                    intent.putExtra("message",cm.getMid());
                    mContext.startActivity(intent);
                }
            });

            View view = this.mSelectedOverlay;
            if (!selected) {
                i2 = 4;
            }
            view.setVisibility(i2);




            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Trigger the long-click callback
                    if (longPressListener != null) {
                        longPressListener.onItemLongPressed(position);
                    }
                    return true; // Consume the long-click event
                }
            });

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Trigger the long-click callback
                    if (longPressListener != null) {
                        longPressListener.onItemClick(position);
                    }
                }
            });

        }
    }



    private void loadImageView() {

    }

    public static class DateViewHolder extends MesiboRecycleViewHolder {
        protected TextView mDate;

        public DateViewHolder(View v) {
            super(v);
            this.mDate = (TextView) v.findViewById(com.mesibo.messaging.R.id.chat_date);
        }
    }

    public class EmptyViewHolder extends MesiboRecycleViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }

    public void loadReplyView() {
//            this.mInflater = LayoutInflater.from(mContext);
//        if (this.mReplyLayout == null) {
//            View v = this.mInflater.inflate(R.layout.qamp_reply_layout, this.reply_container, true);
//            this.mReplyLayout = (RelativeLayout) v.findViewById(R.id.reply_layout);
//            this.mReplyImage = (ImageView) v.findViewById(R.id.reply_image);
//            this.mReplyUserName = (TextView) v.findViewById(R.id.reply_name);
//            this.mReplyMessage = (TextView) v.findViewById(R.id.reply_text);
//        }
    }

    public class SystemMessageViewHolder extends MesiboRecycleViewHolder {
        private Context mContext = null;
        private Bitmap mImage = null;
        protected ImageView mImageView = null;
        private String mText = null;
        protected TextView mTextView = null;

        public SystemMessageViewHolder(View v, Context context, int color, boolean showImage) {
            super(v);
            this.mTextView = (TextView) v.findViewById(com.mesibo.messaging.R.id.system_msg_text);
            this.mImageView = (ImageView) v.findViewById(com.mesibo.messaging.R.id.system_msg_icon);
            Utils.createRoundDrawable(context, v.findViewById(com.mesibo.messaging.R.id.system_msg_layout), color, 9.0f);
        }

        /* access modifiers changed from: package-private */
        public void setSpannableText() {
            this.mTextView.setVisibility(0);
            if (this.mImage == null) {
                this.mTextView.setText(this.mText);
                return;
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(this.mText);
            ssb.setSpan(new MessageAdapter.VerticalImageSpan(this.mContext, this.mImage), 0, 1, 17);
            this.mTextView.setText(ssb, TextView.BufferType.SPANNABLE);
        }

        public void setText(String text, Bitmap image) {
            this.mText = "  " + text;
            this.mImage = image;
            setSpannableText();
        }

        public void setText(String text, int drawable) {
            setText(text, BitmapFactory.decodeResource(this.mContext.getResources(), drawable));
        }
    }

    public void setListener(MesiboRecycleViewHolder.Listener listener) {
        this.mCustomViewListener = new WeakReference<>(listener);
    }

    public class VerticalImageSpan extends ImageSpan {
        public VerticalImageSpan(Drawable drawable) {
            super(drawable);
        }

        public VerticalImageSpan(Context context, Bitmap image) {
            super(context, image);
        }
    }

    public void addRow() {
        this.mDisplayMsgCnt++;
        this.mTotalMessages = this.mChatList.size();
    }

    public float getItemHeight() {
        return (float) this.mcellHeight;
    }

    public void hideMessage(int position) {
        // Modify the data source or set a flag to hide the message at the specified position
        // You can also trigger a UI update to hide the message
    }

    public void showOptions(int position) {
        MessageData item = mChatList.get(position);
//        item.setShowOptions(true); // You can set a flag in your data model
        notifyItemChanged(position);


        if (swipeCallback != null) {
            swipeCallback.onSwipe(position,item);
        }
    }

    public void clearSelections() {
        List<Integer> selection = getSelectedItems();
        clearSelectedItems();
        for (Integer i : selection) {
            notifyItemChanged(i.intValue());
        }
    }
    public void setLongPressListener(LongPressListener listener) {
        this.longPressListener = listener;
    }
    public String copyData() {
        String copiedData = "";
        for (Integer i : getSelectedItems()) {
            copiedData = (copiedData + this.mChatList.get(i.intValue()).getDisplayMessage()) + "\n";
        }
        return copiedData;
    }

    public void setupMessageStatus(ImageView im, int status) {
        im.setVisibility(this.mData.isDeleted() ? 8 : 0);
        if (!this.mData.isDeleted()) {
            im.setImageBitmap(MesiboImages.getStatusImage(status));
        }
    }

    public int globalPosition(int position) {
        return position;
    }

    public void updateStatus(int index) {
        notifyItemChanged(index);
    }

    public void bindLocation(double latitude, double longitude) {
        if (mapLayout != null) {
            mapLayout.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.clear(); // Clear existing markers (if any)

                    LatLng location = new LatLng(latitude, longitude);

                    // Add a marker at the specified location
                    googleMap.addMarker(new MarkerOptions().position(location).title("Current Location"));

                    // Move the camera to the specified location and set an initial zoom level
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }
            });
        }
    }

//    public void deleteItem(int position) {
//        longPressListener.onSwipeListener(position);
//    }

}
