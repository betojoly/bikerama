package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class RegisterAcessoriosActivity extends AppCompatActivity {

    private static final String TITLE = "Cadastro Acessórios";
    private static final String TAG = RegisterBikeActivity.class.getSimpleName();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PID     = "pid";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bike_id";
    private static final String KEY_ACESSSORIO_NOME = "acessorio";
    private static final String KEY_ACESSSORIO_PRECO = "preco";
    private static final String KEY_EMAIL   = "email_user";

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    private Button btnSubmit;

    private ProgressDialog pDialog;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_acessorios);

        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String user_name = user.get("name");
        final String user_email = user.get("email");

        // getting product details from intent
        Intent i = getIntent();
        // getting product id (pid) from intent
        final String bike_id = i.getStringExtra(TAG_BIKEID);
        //Log.d(TAG, "Bike ID: " + bike_id.toString());

        // Dados Acessorio
        final EditText nomeAcessorio = (EditText) findViewById(R.id.inputAcessorio);
        nomeAcessorio.setHint("Nome do Acessório");

        final EditText nomePreco = (EditText) findViewById(R.id.inputPreco);
        nomePreco.setHint("Valor de Compra (R$)");


        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        // Register Button Click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Controle
                String var_Acessorio = nomeAcessorio.getText().toString().trim();
                String var_Preco = nomePreco.getText().toString().trim();

                // Verifica se tem Conexao com a internet
                if (detectaConexao.existeConexao()) {

                    // Salva o Acessorio da Bike no servidor
                    if (!var_Acessorio.isEmpty()) {
                        // Salvar
                        SaveAcessorio(var_Acessorio, var_Preco, bike_id);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Por favor preencha o campo Acessório! (Obrigatório)", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                else {
                    mostraAlerta();
                }
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    // Salva os dados de componentes
    private void SaveAcessorio(final String var_Acessorio, final String var_Preco, final String bike_id) {

        // Tag used to cancel the request
        String tag_string_req = "req_register_acessorio";

        pDialog.setMessage("Salvando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SAVE_ACESSORIO_BIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Save Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

                        // Launch View Acessorios Activity
                        Intent intent = new Intent(
                                RegisterAcessoriosActivity.this,
                                ViewAcessoriosActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Save Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_ACESSSORIO_NOME, String.valueOf(var_Acessorio));
                params.put(KEY_ACESSSORIO_PRECO, String.valueOf(var_Preco));
                params.put(TAG_BIKEID, String.valueOf(bike_id));
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(RegisterAcessoriosActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Mostra a informação caso não tenha internet.
     * */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(RegisterAcessoriosActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();
    }

}
