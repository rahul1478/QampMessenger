package com.qamp.app.Adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.qamp.app.Adapter.MessageAdapter;

public class CustomSwipeCallback extends ItemTouchHelper.SimpleCallback {
    private MessageAdapter adapter;

    public CustomSwipeCallback(MessageAdapter adapter) {
        super(0, ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int viewType = viewHolder.getItemViewType();

        // Check if the viewType is 1 or 2 and return swipe directions for those view types
        if (viewType == 1 || viewType == 2) {
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        // For all other view types, return 0 (no swipe)
        return 0;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // Don't allow drag-and-drop
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        int viewType = adapter.getItemViewType(position);

        if (viewType == 1 || viewType == 2) {
            // Handle swipe for viewType 1 and 2
            if (direction == ItemTouchHelper.LEFT) {
                // Handle left swipe (e.g., hide the message)
                adapter.hideMessage(position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                // Handle right swipe (e.g., show options)
                adapter.showOptions(position);
            }
        } else {
            // Consume the swipe event for other view types (prevents default swipe behavior)
            adapter.notifyItemChanged(position);
            Log.e("swipe", "not allowed for this viewType: " + viewType);
        }
    }
}