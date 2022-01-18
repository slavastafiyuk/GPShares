package com.example.gpshares.Dialogs;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;

import com.example.gpshares.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Dialog_map extends AppCompatDialogFragment {
    private Bitmap imagem;
    private DialogListener listener;
    private AutoCompleteTextView autoCompleteTextView, autoCompleteTextView2, visibilidadeAutoComplete;
    private TextInputEditText coment_estabelecimento, nome_estabelecimento;
    private ImageView imageView;
    private ByteArrayOutputStream bytes;
    private FloatingActionButton floatingActionButton;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view)
                .setTitle("Adicionar Local")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Ok", null);

        imageView = view.findViewById(R.id.imageView_PlaceADD);
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

        floatingActionButton = view.findViewById(R.id.floatingActionButtonADDPIC);
        imageView = view.findViewById(R.id.imageView_PlaceADD);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
        AlertDialog dialog1 =  builder.create();
        dialog1.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = dialog1.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" + autoCompleteTextView.getText().toString().equals("FALTA_DECIDIR"));
                        if (nome_estabelecimento.getText().toString().equals("")){
                            nome_estabelecimento.setError("Necessita de adicionar o nome");
                            nome_estabelecimento.requestFocus();
                        }else if (autoCompleteTextView.getText().toString().equals("FALTA_DECIDIR")){
                            autoCompleteTextView.setError("Necessita de escolher o tipo de estabelecimento");
                            autoCompleteTextView.requestFocus();
                        }else if (autoCompleteTextView2.getText().toString().equals("FALTA_DECIDIR")){
                            autoCompleteTextView2.setError("Necessita de adicionar a avaliação");
                            autoCompleteTextView2.requestFocus();
                        }else if (coment_estabelecimento.getText().toString().equals("")){
                            coment_estabelecimento.setError("Necessita de adicionar comentario");
                            coment_estabelecimento.requestFocus();
                        }else if (visibilidadeAutoComplete.getText().toString().equals("FALTA_DECIDIR")){
                            visibilidadeAutoComplete.setError("Necessida de decidir a visibilidade");
                            visibilidadeAutoComplete.requestFocus();
                        }else if (imageView.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.logo).getConstantState())) {
                            floatingActionButton.setFocusableInTouchMode(true);
                            floatingActionButton.requestFocus();
                            Toast.makeText(getActivity(), "Tem de adicionar imagem para prosseguir", Toast.LENGTH_SHORT).show();
                        } else {
                            String tipo = autoCompleteTextView.getText().toString();
                            String avaliacao = autoCompleteTextView2.getText().toString();
                            String nome = nome_estabelecimento.getText().toString();
                            String comment = coment_estabelecimento.getText().toString();
                            String visibilidade = visibilidadeAutoComplete.getText().toString();
                            listener.applyTexts(tipo, avaliacao, nome, comment, visibilidade, bytes);
                            dialog1.dismiss();
                        }
                    }
                });
            }
        });
        return dialog1;
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

    private void choosePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_profile_picture, null);
        builder.setCancelable(true);
        builder.setView(dialogView);

        ImageView imageViewCamera = dialogView.findViewById(R.id.imageView_dialogCamera);
        ImageView imageViewGallery = dialogView.findViewById(R.id.imageView_dialogGallery);

        final AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.show();

        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    takePictureFromCamera();
                    alertDialogProfilePicture.cancel();
                }

            }
        });

        imageViewGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureFromGallery();
                alertDialogProfilePicture.cancel();
            }
        });
    }

    private void takePictureFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePicture, 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                Uri selectedImageUri = data.getData();
                try {
                    imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                    bytes = new ByteArrayOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imagem.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                imageView.setImageBitmap(imagem);
                break;
            case 2:
                Bundle bundle = data.getExtras();
                imagem = (Bitmap) bundle.get("data");
                bytes = new ByteArrayOutputStream();
                imagem.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                imageView.setImageBitmap(imagem);
                break;
        }
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(getActivity(), "Permissões não garantidas", Toast.LENGTH_SHORT).show();
        }
    }

    public interface DialogListener {
        void applyTexts(String username, String password, String nome, String comment, String visibilidade, ByteArrayOutputStream imagem);
    }


}
