package com.example.gpshares.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.gpshares.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class Dialog_map extends AppCompatDialogFragment {
    private DialogListener listener;
    private AutoCompleteTextView autoCompleteTextView, autoCompleteTextView2, visibilidadeAutoComplete;
    private TextInputEditText coment_estabelecimento, nome_estabelecimento;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
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
                        String username = autoCompleteTextView.getText().toString();
                        String password = autoCompleteTextView2.getText().toString();
                        String nome = nome_estabelecimento.getText().toString();
                        String comment  = coment_estabelecimento.getText().toString();
                        String visibilidade = visibilidadeAutoComplete.getText().toString();
                        listener.applyTexts(username, password, nome, comment, visibilidade);
                    }
                });
        coment_estabelecimento = view.findViewById(R.id.coment_do_estabelecimento);
        nome_estabelecimento = view.findViewById(R.id.estabelecimento_nome);

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView_ALGO);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.tipo, R.layout.dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteTextView.setAdapter(adapter1);

        autoCompleteTextView2 = view.findViewById(R.id.autoCompleteTextView_ALGO2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.Avaliacao, R.layout.dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteTextView2.setAdapter(adapter2);

        visibilidadeAutoComplete = view.findViewById(R.id.VisibilidadeAutoComplete);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(), R.array.tipo_visibilidade, R.layout.dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilidadeAutoComplete.setAdapter(adapter3);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }

    public interface DialogListener {
        void applyTexts(String username, String password, String nome, String comment, String visibilidade);
    }
}
