package com.example.gpshares.FriendsHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpshares.R;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {
    Context context;
    ArrayList<FindNewFriends> list;
    private onFriendsListener monFriendsListener;

    public FriendsAdapter (Context context, ArrayList<FindNewFriends> list, onFriendsListener onFriendsListener) {
        this.context = context;
        this.list = list;
        this.monFriendsListener = onFriendsListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nome;
        onFriendsListener onFriendsListener;
        public MyViewHolder(final View view, onFriendsListener onFriendsListener){
            super(view);
            nome = view.findViewById(R.id.allUsersFullNames);
            view.setOnClickListener(this);
            this.onFriendsListener = onFriendsListener;
        }

        @Override
        public void onClick(View v) {
            onFriendsListener.onFriendsClick(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public FriendsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.all_users_layout, parent, false);
        return new MyViewHolder(itemView, monFriendsListener);

    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.MyViewHolder holder, int position) {
        FindNewFriends findNewfriends = list.get(position);
        holder.nome.setText(findNewfriends.getNomeInteiro());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface onFriendsListener{
        void onFriendsClick(int position);
    }
}
