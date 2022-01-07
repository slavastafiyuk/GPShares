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


    public FriendsAdapter (Context context, ArrayList<FindNewFriends> list) {
        this.context = context;
        this.list = list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView nome;
        public MyViewHolder(final View view){
            super(view);
            nome = view.findViewById(R.id.allUsersFullNames);

        }
    }

    @NonNull
    @Override
    public FriendsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.all_users_layout, parent, false);
        return new MyViewHolder(itemView);
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
}
