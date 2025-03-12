package com.qamp.app.Adapter;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.qamp.app.MessagingModule.UserListFragment;

public class SwipeToDeleteChat extends ItemTouchHelper.SimpleCallback {

    private MessageAdapter mAdapter;

    public SwipeToDeleteChat(MessageAdapter adapter) {
        super(0, ItemTouchHelper.RIGHT);
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
//        mAdapter.deleteItem(position); // Implement this method in your adapter to remove the item from the ArrayList
    }
}

