package com.example.ac_conexionapisql_19nov;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ac_conexionapisql_19nov.models.Rol;

import java.util.List;

public class RolAdapter extends ArrayAdapter<Rol> {
    private MainActivity mainActivity;

    public RolAdapter(Context context, List<Rol> roles) {
        super(context, 0, roles);
        if (context instanceof MainActivity) {
            this.mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_rol, parent, false);
        }

        Rol rol = getItem(position);

        TextView txtNombreRol = convertView.findViewById(R.id.txtNombreRol);
        TextView txtIdRol = convertView.findViewById(R.id.txtIdRol);
        Button btnEditar = convertView.findViewById(R.id.btnEditar);
        Button btnEliminar = convertView.findViewById(R.id.btnEliminar);

        if (rol != null) {
            txtNombreRol.setText(rol.getNombreRol());
            txtIdRol.setText("ID: " + rol.getIdRol());

            btnEditar.setOnClickListener(v -> {
                if (mainActivity != null) {
                    mainActivity.editarRol(rol);
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (mainActivity != null) {
                    mainActivity.confirmarEliminarRol(rol);
                }
            });
        }

        return convertView;
    }
}

