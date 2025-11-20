package com.example.ac_conexionapisql_19nov;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ac_conexionapisql_19nov.models.Usuario;

import java.util.List;

public class UsuarioAdapter extends ArrayAdapter<Usuario> {
    private MainActivity mainActivity;

    public UsuarioAdapter(Context context, List<Usuario> usuarios) {
        super(context, 0, usuarios);
        if (context instanceof MainActivity) {
            this.mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_usuario, parent, false);
        }

        Usuario usuario = getItem(position);

        TextView txtNombre = convertView.findViewById(R.id.txtNombre);
        TextView txtEmail = convertView.findViewById(R.id.txtEmail);
        TextView txtRol = convertView.findViewById(R.id.txtRol);
        Button btnEditar = convertView.findViewById(R.id.btnEditar);
        Button btnEliminar = convertView.findViewById(R.id.btnEliminar);

        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtEmail.setText(usuario.getEmail());
            
            // Obtener nombre del rol si está disponible
            String nombreRol = "Rol ID: " + usuario.getIdRol();
            if (usuario.getRol() != null && usuario.getRol().getNombreRol() != null) {
                nombreRol = usuario.getRol().getNombreRol();
            }
            txtRol.setText(nombreRol);

            btnEditar.setOnClickListener(v -> {
                if (mainActivity != null) {
                    mainActivity.editarUsuario(usuario);
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (mainActivity != null) {
                    mainActivity.confirmarEliminarUsuario(usuario);
                }
            });
        }

        return convertView;
    }
}

