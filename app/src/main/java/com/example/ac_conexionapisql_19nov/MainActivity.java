package com.example.ac_conexionapisql_19nov;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ac_conexionapisql_19nov.database.DBHelper;
import com.example.ac_conexionapisql_19nov.models.Rol;
import com.example.ac_conexionapisql_19nov.models.Usuario;
import com.example.ac_conexionapisql_19nov.utils.NetworkUtils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvOrigenDatos;
    private View viewIndicator;
    private Button btnAgregarUsuario;
    private Button btnAgregarRol;
    private Button btnSincronizar;
    private ListView lvUsuarios;
    private ListView lvRoles;

    private DBHelper dbHelper;
    private UsuarioAdapter adapter;
    private RolAdapter adapterRoles;
    private ArrayList<Usuario> listaUsuarios;
    private ArrayList<Rol> listaRoles;
    private boolean isOnline = false;
    private Usuario usuarioEditando = null;  // Usuario que se está editando (null si es nuevo)
    private Rol rolEditando = null;  // Rol que se está editando (null si es nuevo)

    private String apiURLUsuario = "http://demomovileva.somee.com/api/Usuario";
    private String apiURLRol = "http://demomovileva.somee.com/api/Rol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias
        tvOrigenDatos = findViewById(R.id.tvOrigenDatos);
        viewIndicator = findViewById(R.id.viewIndicator);
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario);
        btnAgregarRol = findViewById(R.id.btnAgregarRol);
        btnSincronizar = findViewById(R.id.btnSincronizar);
        lvUsuarios = findViewById(R.id.lvUsuarios);
        lvRoles = findViewById(R.id.lvRoles);

        // Inicializar componentes
        dbHelper = new DBHelper(this);
        listaUsuarios = new ArrayList<>();
        listaRoles = new ArrayList<>();

        // Cargar datos iniciales
        verificarConexionYCargar();

        // Botón agregar usuario
        btnAgregarUsuario.setOnClickListener(v -> mostrarDialogAgregarUsuario());

        // Botón agregar rol
        btnAgregarRol.setOnClickListener(v -> mostrarDialogAgregarRol());

        // Botón sincronizar
        btnSincronizar.setOnClickListener(v -> sincronizarConAPI());
    }

    private void verificarConexionYCargar() {
        isOnline = NetworkUtils.isNetworkAvailable(this);
        
        if (isOnline) {
            tvOrigenDatos.setText("🌐 Conectado - Datos desde API");
            tvOrigenDatos.setTextColor(ContextCompat.getColor(this, R.color.success));
            viewIndicator.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.success));
            cargarUsuariosDesdeAPI();
            cargarRolesDesdeAPI();
        } else {
            tvOrigenDatos.setText("📱 Sin conexión - Datos desde SQLite");
            tvOrigenDatos.setTextColor(ContextCompat.getColor(this, R.color.warning));
            viewIndicator.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.warning));
            cargarUsuariosDesdeSQLite();
            cargarRolesDesdeSQLite();
        }
    }

    private void cargarUsuariosDesdeAPI() {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLUsuario);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }
                
                JSONArray array = new JSONArray(respuesta.toString());
                listaUsuarios.clear();
                
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(obj.optInt("idUsuario", 0));
                    usuario.setNombre(obj.optString("nombre", ""));
                    usuario.setEmail(obj.optString("email", ""));
                    usuario.setPassword(obj.optString("password", ""));
                    usuario.setIdRol(obj.optInt("idRol", 0));
                    
                    // Si viene el objeto rol
                    if (obj.has("rol") && !obj.isNull("rol")) {
                        JSONObject rolObj = obj.getJSONObject("rol");
                        Rol rol = new Rol();
                        rol.setIdRol(rolObj.optInt("idRol", 0));
                        rol.setNombreRol(rolObj.optString("nombreRol", ""));
                        usuario.setRol(rol);
                    }
                    
                    listaUsuarios.add(usuario);
                }
                
                // Guardar en SQLite
                dbHelper.sincronizarUsuarios(listaUsuarios);
                
                runOnUiThread(() -> {
                    actualizarLista();
                    Toast.makeText(this, "Datos cargados desde API", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al cargar desde API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cargarUsuariosDesdeSQLite();
                });
            }
        }).start();
    }

    private void cargarRolesDesdeAPI() {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLRol);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }
                
                JSONArray array = new JSONArray(respuesta.toString());
                listaRoles.clear();
                
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Rol rol = new Rol();
                    rol.setIdRol(obj.optInt("idRol", 0));
                    rol.setNombreRol(obj.optString("nombreRol", ""));
                    listaRoles.add(rol);
                }
                
                // Guardar en SQLite
                dbHelper.sincronizarRoles(listaRoles);
                
                runOnUiThread(() -> {
                    actualizarListaRoles();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> cargarRolesDesdeSQLite());
            }
        }).start();
    }

    private void cargarUsuariosDesdeSQLite() {
        listaUsuarios.clear();
        listaUsuarios.addAll(dbHelper.obtenerUsuarios());
        actualizarLista();
        
        if (listaUsuarios.isEmpty()) {
            Toast.makeText(this, "No hay usuarios guardados localmente", 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarRolesDesdeSQLite() {
        listaRoles.clear();
        listaRoles.addAll(dbHelper.obtenerRoles());
        actualizarListaRoles();
    }

    private void sincronizarConAPI() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            verificarConexionYCargar();
            return;
        }
        
        Toast.makeText(this, "Sincronizando...", Toast.LENGTH_SHORT).show();
        isOnline = true;
        runOnUiThread(() -> {
            tvOrigenDatos.setText("🌐 Conectado - Datos desde API");
            tvOrigenDatos.setTextColor(ContextCompat.getColor(this, R.color.success));
            viewIndicator.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.success));
        });
        cargarUsuariosDesdeAPI();
        cargarRolesDesdeAPI();
    }

    private void actualizarLista() {
        adapter = new UsuarioAdapter(this, listaUsuarios);
        lvUsuarios.setAdapter(adapter);
    }

    private void actualizarListaRoles() {
        adapterRoles = new RolAdapter(this, listaRoles);
        lvRoles.setAdapter(adapterRoles);
    }

    private void mostrarDialogAgregarUsuario(Usuario usuarioEditar) {
        // Cargar roles primero (desde SQLite o actualizar)
        cargarRolesDesdeSQLite();
        
        if (listaRoles.isEmpty()) {
            Toast.makeText(this, "No hay roles disponibles. Sincronice primero.", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_agregar_usuario, null);
        builder.setView(dialogView);

        TextView tituloDialog = dialogView.findViewById(R.id.tvTituloDialog);
        if (tituloDialog != null) {
            tituloDialog.setText(usuarioEditar == null ? "Agregar Usuario" : "Editar Usuario");
        } else {
            builder.setTitle(usuarioEditar == null ? "Agregar Usuario" : "Editar Usuario");
        }

        TextInputEditText edtNombre = dialogView.findViewById(R.id.edtNombre);
        TextInputEditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        TextInputEditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Spinner spinnerRol = dialogView.findViewById(R.id.spinnerRol);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Si es edición, prellenar campos
        if (usuarioEditar != null) {
            edtNombre.setText(usuarioEditar.getNombre());
            edtEmail.setText(usuarioEditar.getEmail());
            edtPassword.setText(usuarioEditar.getPassword());
            usuarioEditando = usuarioEditar;
        } else {
            usuarioEditando = null;
        }

        ArrayAdapter<Rol> adapterRolesSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaRoles);
        adapterRolesSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapterRolesSpinner);

        if (usuarioEditar != null) {
            for (int i = 0; i < listaRoles.size(); i++) {
                if (listaRoles.get(i).getIdRol() == usuarioEditar.getIdRol()) {
                    spinnerRol.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, 
                    "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Rol rolSeleccionado = (Rol) spinnerRol.getSelectedItem();
            if (rolSeleccionado == null) {
                Toast.makeText(MainActivity.this, 
                    "Seleccione un rol", Toast.LENGTH_SHORT).show();
                return;
            }

            if (usuarioEditar == null) {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setPassword(password);
                nuevoUsuario.setIdRol(rolSeleccionado.getIdRol());

                if (isOnline && NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    guardarUsuarioEnAPI(nuevoUsuario);
                } else {
                    guardarUsuarioEnSQLite(nuevoUsuario);
                }
            } else {
                // Actualizar usuario existente
                usuarioEditar.setNombre(nombre);
                usuarioEditar.setEmail(email);
                usuarioEditar.setPassword(password);
                usuarioEditar.setIdRol(rolSeleccionado.getIdRol());

                if (isOnline && NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    actualizarUsuarioEnAPI(usuarioEditar);
                } else {
                    actualizarUsuarioEnSQLite(usuarioEditar);
                }
            }
            
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> {
            usuarioEditando = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogAgregarUsuario() {
        mostrarDialogAgregarUsuario(null);
    }

    public void editarUsuario(Usuario usuario) {
        mostrarDialogAgregarUsuario(usuario);
    }

    public void confirmarEliminarUsuario(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de que desea eliminar el usuario: " + usuario.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarUsuario(usuario))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarUsuarioEnAPI(Usuario usuario) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLUsuario);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setDoOutput(true);

                // Crear JSON
                JSONObject jsonUsuario = new JSONObject();
                jsonUsuario.put("nombre", usuario.getNombre());
                jsonUsuario.put("email", usuario.getEmail());
                jsonUsuario.put("password", usuario.getPassword());
                jsonUsuario.put("idRol", usuario.getIdRol());

                JSONObject jsonRol = new JSONObject();
                jsonRol.put("idRol", usuario.getIdRol());
                jsonUsuario.put("Rol", jsonRol);

                // Escribir JSON
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                writer.write(jsonUsuario.toString());
                writer.flush();
                writer.close();

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_CREATED) {
                    // Leer respuesta
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder respuestaStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        respuestaStr.append(linea);
                    }
                    
                    // Parsear usuario creado
                    JSONObject usuarioCreado = new JSONObject(respuestaStr.toString());
                    Usuario usuarioRetornado = new Usuario();
                    usuarioRetornado.setIdUsuario(usuarioCreado.optInt("idUsuario", 0));
                    usuarioRetornado.setNombre(usuarioCreado.optString("nombre", ""));
                    usuarioRetornado.setEmail(usuarioCreado.optString("email", ""));
                    usuarioRetornado.setPassword(usuarioCreado.optString("password", ""));
                    usuarioRetornado.setIdRol(usuarioCreado.optInt("idRol", 0));
                    
                    // Guardar también en SQLite
                    dbHelper.insertarUsuario(usuarioRetornado);
                    
                    runOnUiThread(() -> {
                        listaUsuarios.add(usuarioRetornado);
                        actualizarLista();
                        Toast.makeText(this, "Usuario creado en API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Leer error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al crear usuario: " + mensajeError, Toast.LENGTH_LONG).show();
                        guardarUsuarioEnSQLite(usuario);
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    guardarUsuarioEnSQLite(usuario);
                });
            }
        }).start();
    }

    private void guardarUsuarioEnSQLite(Usuario usuario) {
        // Generar un ID temporal negativo para usuarios offline
        // Verificar si idUsuario es null o 0
        if (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0) {
            usuario.setIdUsuario(-(int) System.currentTimeMillis());
        }
        
        boolean exito = dbHelper.insertarUsuario(usuario);
        
        runOnUiThread(() -> {
            if (exito) {
                listaUsuarios.add(usuario);
                actualizarLista();
                Toast.makeText(this, "Usuario guardado en SQLite", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void actualizarUsuarioEnAPI(Usuario usuario) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLUsuario + "/" + usuario.getIdUsuario());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("PUT");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setDoOutput(true);

                JSONObject jsonUsuario = new JSONObject();
                jsonUsuario.put("idUsuario", usuario.getIdUsuario());
                jsonUsuario.put("nombre", usuario.getNombre());
                jsonUsuario.put("email", usuario.getEmail());
                jsonUsuario.put("password", usuario.getPassword());
                jsonUsuario.put("idRol", usuario.getIdRol());
                
                // Agregar objeto Rol con solo idRol
                JSONObject jsonRol = new JSONObject();
                jsonRol.put("idRol", usuario.getIdRol());
                jsonUsuario.put("Rol", jsonRol);

                // Escribir JSON
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                writer.write(jsonUsuario.toString());
                writer.flush();
                writer.close();

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_NO_CONTENT) {
                    // Actualizar también en SQLite (esto actualizará la lista dentro de runOnUiThread)
                    actualizarUsuarioEnSQLite(usuario);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Usuario actualizado en API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al actualizar usuario: " + mensajeError, Toast.LENGTH_LONG).show();
                    });
                    actualizarUsuarioEnSQLite(usuario);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                // Actualizar en SQLite fuera del hilo UI
                actualizarUsuarioEnSQLite(usuario);
            }
        }).start();
    }

    private void actualizarUsuarioEnSQLite(Usuario usuario) {
        boolean exito = dbHelper.actualizarUsuario(usuario);
        
        runOnUiThread(() -> {
            if (exito) {
                for (int i = 0; i < listaUsuarios.size(); i++) {
                    if (listaUsuarios.get(i).getIdUsuario() != null && 
                        listaUsuarios.get(i).getIdUsuario().equals(usuario.getIdUsuario())) {
                        listaUsuarios.set(i, usuario);
                        break;
                    }
                }
                actualizarLista();
                Toast.makeText(this, "Usuario actualizado en SQLite", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void eliminarUsuario(Usuario usuario) {
        if (isOnline && NetworkUtils.isNetworkAvailable(this)) {
            eliminarUsuarioEnAPI(usuario);
        } else {
            eliminarUsuarioEnSQLite(usuario);
        }
    }


    private void eliminarUsuarioEnAPI(Usuario usuario) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLUsuario + "/" + usuario.getIdUsuario());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_NO_CONTENT) {
                    // Eliminar también de SQLite
                    eliminarUsuarioEnSQLite(usuario);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Usuario eliminado de la API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Leer error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al eliminar usuario: " + mensajeError, Toast.LENGTH_LONG).show();
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void eliminarUsuarioEnSQLite(Usuario usuario) {
        if (usuario.getIdUsuario() != null) {
            boolean exito = dbHelper.eliminarUsuario(usuario.getIdUsuario());
            
            runOnUiThread(() -> {
                if (exito) {
                    // Eliminar de la lista
                    listaUsuarios.remove(usuario);
                    actualizarLista();
                    Toast.makeText(this, "Usuario eliminado de SQLite", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarDialogAgregarRol(Rol rolEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_agregar_rol, null);
        builder.setView(dialogView);

        TextView tituloDialog = dialogView.findViewById(R.id.tvTituloDialog);
        if (tituloDialog != null) {
            tituloDialog.setText(rolEditar == null ? "Agregar Rol" : "Editar Rol");
        } else {
            builder.setTitle(rolEditar == null ? "Agregar Rol" : "Editar Rol");
        }

        TextInputEditText edtNombreRol = dialogView.findViewById(R.id.edtNombreRol);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        if (rolEditar != null) {
            edtNombreRol.setText(rolEditar.getNombreRol());
            rolEditando = rolEditar;
        } else {
            rolEditando = null;
        }

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            String nombreRol = edtNombreRol.getText().toString().trim();

            if (nombreRol.isEmpty()) {
                Toast.makeText(MainActivity.this, 
                    "Ingrese un nombre de rol", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rolEditar == null) {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombreRol(nombreRol);

                if (isOnline && NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    guardarRolEnAPI(nuevoRol);
                } else {
                    guardarRolEnSQLite(nuevoRol);
                }
            } else {
                rolEditar.setNombreRol(nombreRol);

                if (isOnline && NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    actualizarRolEnAPI(rolEditar);
                } else {
                    actualizarRolEnSQLite(rolEditar);
                }
            }
            
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> {
            rolEditando = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogAgregarRol() {
        mostrarDialogAgregarRol(null);
    }

    public void editarRol(Rol rol) {
        mostrarDialogAgregarRol(rol);
    }

    public void confirmarEliminarRol(Rol rol) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de que desea eliminar el rol: " + rol.getNombreRol() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarRol(rol))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarRolEnAPI(Rol rol) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLRol);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setDoOutput(true);

                // Crear JSON
                JSONObject jsonRol = new JSONObject();
                jsonRol.put("nombreRol", rol.getNombreRol());

                // Escribir JSON
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                writer.write(jsonRol.toString());
                writer.flush();
                writer.close();

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_CREATED) {
                    // Leer respuesta
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder respuestaStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        respuestaStr.append(linea);
                    }
                    
                    // Parsear rol creado
                    JSONObject rolCreado = new JSONObject(respuestaStr.toString());
                    Rol rolRetornado = new Rol();
                    rolRetornado.setIdRol(rolCreado.optInt("idRol", 0));
                    rolRetornado.setNombreRol(rolCreado.optString("nombreRol", ""));
                    
                    // Guardar también en SQLite
                    dbHelper.insertarRol(rolRetornado);
                    
                    runOnUiThread(() -> {
                        listaRoles.add(rolRetornado);
                        actualizarListaRoles();
                        Toast.makeText(this, "Rol creado en API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Leer error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al crear rol: " + mensajeError, Toast.LENGTH_LONG).show();
                        guardarRolEnSQLite(rol);
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    guardarRolEnSQLite(rol);
                });
            }
        }).start();
    }

    private void guardarRolEnSQLite(Rol rol) {
        // Generar un ID temporal negativo para roles offline
        if (rol.getIdRol() == 0) {
            rol.setIdRol(-(int) System.currentTimeMillis());
        }
        
        boolean exito = dbHelper.insertarRol(rol);
        
        runOnUiThread(() -> {
            if (exito) {
                listaRoles.add(rol);
                actualizarListaRoles();
                Toast.makeText(this, "Rol guardado en SQLite", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar rol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarRolEnAPI(Rol rol) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLRol + "/" + rol.getIdRol());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("PUT");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setDoOutput(true);

                // Crear JSON
                JSONObject jsonRol = new JSONObject();
                jsonRol.put("idRol", rol.getIdRol());
                jsonRol.put("nombreRol", rol.getNombreRol());

                // Escribir JSON
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                writer.write(jsonRol.toString());
                writer.flush();
                writer.close();

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_NO_CONTENT) {
                    actualizarRolEnSQLite(rol);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Rol actualizado en API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Leer error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al actualizar rol: " + mensajeError, Toast.LENGTH_LONG).show();
                    });
                    actualizarRolEnSQLite(rol);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                actualizarRolEnSQLite(rol);
            }
        }).start();
    }

    private void actualizarRolEnSQLite(Rol rol) {
        boolean exito = dbHelper.actualizarRol(rol);
        
        runOnUiThread(() -> {
            if (exito) {
                // Buscar y actualizar en la lista
                for (int i = 0; i < listaRoles.size(); i++) {
                    if (listaRoles.get(i).getIdRol() == rol.getIdRol()) {
                        listaRoles.set(i, rol);
                        break;
                    }
                }
                actualizarListaRoles();
                Toast.makeText(this, "Rol actualizado en SQLite", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar rol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarRol(Rol rol) {
        if (isOnline && NetworkUtils.isNetworkAvailable(this)) {
            eliminarRolEnAPI(rol);
        } else {
            eliminarRolEnSQLite(rol);
        }
    }

    private void eliminarRolEnAPI(Rol rol) {
        new Thread(() -> {
            try {
                URL url = new URL(apiURLRol + "/" + rol.getIdRol());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");

                int respuesta = con.getResponseCode();
                
                if (respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_NO_CONTENT) {
                    eliminarRolEnSQLite(rol);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Rol eliminado de la API", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    StringBuilder errorStr = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        errorStr.append(linea);
                    }
                    
                    final String mensajeError = errorStr.toString();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al eliminar rol: " + mensajeError, Toast.LENGTH_LONG).show();
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void eliminarRolEnSQLite(Rol rol) {
        boolean exito = dbHelper.eliminarRol(rol.getIdRol());
        
        runOnUiThread(() -> {
            if (exito) {
                listaRoles.remove(rol);
                actualizarListaRoles();
                Toast.makeText(this, "Rol eliminado de SQLite", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al eliminar rol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarConexionYCargar();
        cargarRolesDesdeSQLite();
    }
}
