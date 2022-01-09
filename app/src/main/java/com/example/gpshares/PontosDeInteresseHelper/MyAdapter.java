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
    private onAdapterListener monAdapterListener;

    public MyAdapter(Context context, ArrayList<FindNewRestaurante> list, onAdapterListener onAdapterListener) {
        this.context = context;
        this.list = list;
        this.monAdapterListener = onAdapterListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView avaliacao;
        private TextView nome;
        private TextView comentario;
        onAdapterListener onAdapterListener;
        public MyViewHolder(final View view, onAdapterListener onAdapterListener){
            super(view);
            avaliacao = view.findViewById(R.id.allEstabelecimentosAvaliacao);
            nome = view.findViewById(R.id.allEstabelecimentosFullName);
            comentario = view.findViewById(R.id.allEstabelecimentosComentarios);
            view.setOnClickListener(this);
            this.onAdapterListener = onAdapterListener;
        }

        @Override
        public void onClick(View v) {
            onAdapterListener.onAdapterClick(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.all_restaurantes_layout, parent, false);
        return new MyViewHolder(itemView, monAdapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        FindNewRestaurante findNewRestaurante = list.get(position);
        holder.avaliacao.setText(findNewRestaurante.getAvaliacao());
        holder.nome.setText(findNewRestaurante.getNome());
        holder.comentario.setText(findNewRestaurante.getComentario());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface onAdapterListener{
        void onAdapterClick(int position);
    }
}
