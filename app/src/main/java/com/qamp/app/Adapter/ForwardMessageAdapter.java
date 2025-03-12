package com.qamp.app.Adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboProfile;
import com.qamp.app.CustomClasses.CheckboxImageView;
import com.qamp.app.Modal.ForwardMessageModel;
import com.qamp.app.R;

import java.util.ArrayList;
import java.util.List;

public class ForwardMessageAdapter extends RecyclerView.Adapter<ForwardMessageAdapter.MessageViewHolder> {

    private ArrayList<ForwardMessageModel> messages;
    private ArrayList<MesiboProfile> selectList = new ArrayList<>();
    private LayoutInflater inflater;
    private ForwardMessageClickListener clickListener;

    public interface ForwardMessageClickListener {
        void onItemClicked(ArrayList<MesiboProfile> profiles,Long message);
    }

    private Long messsage;
    public ForwardMessageAdapter(Context context, ArrayList<ForwardMessageModel> messages, Long messsage, ForwardMessageClickListener clickListener) {
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
        this.messsage = messsage;
        this.clickListener = clickListener;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_forward_screen, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ForwardMessageModel profile = messages.get(position);

        holder.nameText.setText(profile.getProfile().getName());
        holder.numberText.setText(profile.getProfile().address);

        if (profile.getProfile().getImage().getImageOrThumbnail() != null) {
            holder.profile.setImageBitmap(profile.getProfile().getImage().getImageOrThumbnail());
        } else {
            holder.profile.setImageResource(R.drawable.person);
        }

        holder.checkBox.setOnCheckedChangeListener(null); // Remove any existing listeners

        // Set the checkbox state based on the model
        holder.checkBox.setChecked(profile.isCheck());

        holder.checkBox.setOnCheckedChangeListener(new CheckboxImageView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckboxImageView checkboxImageView, boolean isChecked) {
                profile.setCheck(isChecked); // Update the model

                // Update the selection list based on the model's state
                if (isChecked) {
                    selectList.add(profile.getProfile());
                } else {
                    selectList.remove(profile.getProfile());
                }
                Log.e("selectList", String.valueOf(selectList.size()));
                clickListener.onItemClicked(selectList, messsage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView numberText;
        TextView nameText;
        ShapeableImageView profile;
        Button forwardButton;
        CheckboxImageView checkBox;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.mes_rv_phone);
            nameText = itemView.findViewById(R.id.mes_rv_name);
            profile = itemView.findViewById(R.id.mes_rv_profile);
//            forwardButton = itemView.findViewById(R.id.forward_button);
            checkBox = itemView.findViewById(R.id.isChecked);

        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
