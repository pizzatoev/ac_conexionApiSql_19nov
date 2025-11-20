package com.example.ac_conexionapisql_19nov.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.ac_conexionapisql_19nov.models.Rol;
import com.example.ac_conexionapisql_19nov.models.Usuario;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context) {
        super(context, "UsuarioRol.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla roles
        db.execSQL("CREATE TABLE roles(" +
                "idRol INTEGER PRIMARY KEY," +
                "nombreRol TEXT)");

        // Tabla Usuarios
        db.execSQL("CREATE TABLE usuarios(" +
                "idUsuario INTEGER PRIMARY KEY," +
                "nombre TEXT," +
                "email TEXT," +
                "password TEXT," +
                "idRol INTEGER," +
                "FOREIGN KEY (idRol) REFERENCES roles(idRol))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS roles");
        onCreate(db);
    }

    // ============ MÉTODOS PARA ROLES ============

    // Limpiar y sincronizar roles desde la API
    public void sincronizarRoles(ArrayList<Rol> roles) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            // Limpiar tabla
            db.execSQL("DELETE FROM roles");
            // Insertar nuevos roles
            for (Rol rol : roles) {
                ContentValues values = new ContentValues();
                values.put("idRol", rol.getIdRol());
                values.put("nombreRol", rol.getNombreRol());
                db.insert("roles", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Insertar un rol
    public boolean insertarRol(Rol rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idRol", rol.getIdRol());
        values.put("nombreRol", rol.getNombreRol());
        long resultado = db.insert("roles", null, values);
        return resultado != -1;
    }

    // Actualizar un rol
    public boolean actualizarRol(Rol rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombreRol", rol.getNombreRol());
        int resultado = db.update("roles", values, "idRol = ?", 
                new String[]{String.valueOf(rol.getIdRol())});
        return resultado > 0;
    }

    // Eliminar un rol
    public boolean eliminarRol(int idRol) {
        SQLiteDatabase db = this.getWritableDatabase();
        int resultado = db.delete("roles", "idRol = ?", 
                new String[]{String.valueOf(idRol)});
        return resultado > 0;
    }

    // Obtener todos los roles
    public ArrayList<Rol> obtenerRoles() {
        ArrayList<Rol> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM roles ORDER BY idRol", null);
        if (cursor.moveToFirst()) {
            do {
                Rol rol = new Rol();
                rol.setIdRol(cursor.getInt(0));
                rol.setNombreRol(cursor.getString(1));
                lista.add(rol);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // ============ MÉTODOS PARA USUARIOS ============

    // Limpiar y sincronizar usuarios desde la API
    public void sincronizarUsuarios(ArrayList<Usuario> usuarios) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            // Limpiar tabla
            db.execSQL("DELETE FROM usuarios");
            // Insertar nuevos usuarios
            for (Usuario usuario : usuarios) {
                ContentValues values = new ContentValues();
                values.put("idUsuario", usuario.getIdUsuario());
                values.put("nombre", usuario.getNombre());
                values.put("email", usuario.getEmail());
                values.put("password", usuario.getPassword());
                values.put("idRol", usuario.getIdRol());
                db.insert("usuarios", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Insertar un usuario
    public boolean insertarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idUsuario", usuario.getIdUsuario());
        values.put("nombre", usuario.getNombre());
        values.put("email", usuario.getEmail());
        values.put("password", usuario.getPassword());
        values.put("idRol", usuario.getIdRol());
        long resultado = db.insert("usuarios", null, values);
        return resultado != -1;
    }

    // Actualizar un usuario
    public boolean actualizarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", usuario.getNombre());
        values.put("email", usuario.getEmail());
        values.put("password", usuario.getPassword());
        values.put("idRol", usuario.getIdRol());
        int resultado = db.update("usuarios", values, "idUsuario = ?", 
                new String[]{String.valueOf(usuario.getIdUsuario())});
        return resultado > 0;
    }

    // Eliminar un usuario
    public boolean eliminarUsuario(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        int resultado = db.delete("usuarios", "idUsuario = ?", 
                new String[]{String.valueOf(idUsuario)});
        return resultado > 0;
    }

    // Obtener todos los usuarios
    public ArrayList<Usuario> obtenerUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios ORDER BY idUsuario", null);
        if (cursor.moveToFirst()) {
            do {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(cursor.getInt(0));
                usuario.setNombre(cursor.getString(1));
                usuario.setEmail(cursor.getString(2));
                usuario.setPassword(cursor.getString(3));
                usuario.setIdRol(cursor.getInt(4));
                lista.add(usuario);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
}

