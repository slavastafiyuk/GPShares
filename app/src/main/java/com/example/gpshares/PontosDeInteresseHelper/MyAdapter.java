package com.example.gpshares.PontosDeInteresseHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpshares.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<FindNewRestaurante> list;


    public MyAdapter(Context context, ArrayList<FindNewRestaurante> list) {
        this.context = context;
        this.list = list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView avaliacao;
        public MyViewHolder(final View view){
            super(view);
            avaliacao = view.findViewById(R.id.allRestaurantesAvaliacao);
        }
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.all_restaurantes_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        FindNewRestaurante findNewRestaurante = list.get(position);
        holder.avaliacao.setText(findNewRestaurante.getAvaliacao());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
