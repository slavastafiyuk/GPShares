package com.example.gpshares.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.gpshares.R;

import java.util.ArrayList;

public class Dialog_filter_PontosDeInteresse extends AppCompatDialogFragment {
    private Dialog_filter_PontosDeInteresse.DialogListenerFilter listener2;
    private CheckBox restaurantes, cinemas;
    private ArrayList<String> Fresults;
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_filter, null);
        Fresults = new ArrayList<>();
        Fresults.add(0, "0"); //0 para restaurantes
        Fresults.add(1, "0"); //0 para cinemas
        builder.setView(view)
                .setTitle("ALGO")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //try {
                        //    Fresults.get(0);
                        //}catch (IndexOutOfBoundsException e){
                        //    Fresults.add(0,"0");
                        //}
                        //try {
                        //    Fresults.get(1);
                        //}catch (IndexOutOfBoundsException e){
                        //    Fresults.add(1,"0");
                        //}
                        String Frestaurantes = Fresults.get(0);
                        String Fcinemas = Fresults.get(1);
                        int size = Fresults.size();
                        listener2.applyTextsFilter(Frestaurantes, Fcinemas, size);
                    }
                });
        restaurantes = view.findViewById(R.id.Filter_restaurantes);
        cinemas = view.findViewById(R.id.Filter_cinemas);
        restaurantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurantes.isChecked()) {
                    Fresults.set(0, "Restaurantes");
                }
            }
        });
        cinemas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cinemas.isChecked()){
                    Fresults.set(1,"Cinemas");
                }
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener2 = (Dialog_filter_PontosDeInteresse.DialogListenerFilter) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }

    public interface DialogListenerFilter {
        void applyTextsFilter(String restaurantes, String cinemas, int size);
    }
}

