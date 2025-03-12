package com.qamp.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.qamp.app.R;

import java.util.ArrayList;

class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.SwipeViewHolder> {
    // 1- Making swipeadapter constructor and implement the methods

    private Context context;
    private ArrayList<String> employees;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    public SwipeAdapter (Context context, ArrayList<String> employees) {
        this.context = context;
        this.employees = employees;
    }

    private void setEmployees (ArrayList<String> employees) {
        this.employees = new ArrayList<>();
        this.employees = employees;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_after_swipe,parent,  false);
        return new SwipeViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeViewHolder holder, int position) {
//        viewBinderHelper.setOpenOnlyOne (true);
//        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(employees.get(position).getName()));
//        viewBinderHelper.closeLayout(String.valueOf(employees.get(position).getName()));
//        holder.bindData(employees.get(position));
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    // 2- ViewHolder: SwipeViewHolder
    class SwipeViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView txtEdit;
        private TextView txtDelete;
        private SwipeRevealLayout swipeRevealLayout;
        public SwipeViewHolder (@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            txtEdit = itemView.findViewById(R.id.txtEdit);
            txtDelete = itemView.findViewById(R.id.txtDelete);
            swipeRevealLayout = itemView.findViewById(R.id.swipelayout);

            txtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Edit is Clicked", Toast.LENGTH_SHORT).show();
                }
            });
            txtDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Deleted is Clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

}

