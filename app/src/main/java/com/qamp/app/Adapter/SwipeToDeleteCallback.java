package com.qamp.app.Adapter;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mesibo.api.MesiboProfile;
import com.qamp.app.MessagingModule.UserListFragment;
import com.qamp.app.R;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private UserListFragment.ChatFragmentAdapter mAdapter;
    private MesiboProfile pin;


    public SwipeToDeleteCallback(UserListFragment.ChatFragmentAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // Do nothing, we only support swiping to delete
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        MesiboProfile profile = mAdapter.mDataList.get(position);

        if (direction == ItemTouchHelper.LEFT) {
            // Handle left swipe action (e.g., delete action)
            // You can pass the gesture of the swipe to the adapter here
            mAdapter.unRead(position,profile);
        } else if (direction == ItemTouchHelper.RIGHT) {
            // Handle right swipe action (e.g., archive action)
            // You can pass the gesture of the swipe to the adapter here
            mAdapter.pinChats(position,profile);
        }

    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        Context context = recyclerView.getContext();
        new RecyclerViewSwipeDecorator.Builder(context, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
       .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                .addSwipeLeftActionIcon(R.drawable.ico_unread)
                .addSwipeRightBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .addSwipeRightActionIcon(R.drawable.ico_pin)
                .setActionIconTint(ContextCompat.getColor(recyclerView.getContext(), android.R.color.black))
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}

