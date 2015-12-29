package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import helper.SQLiteHandler;

import static br.com.imcom.bikerama.Perfil.*;
import static br.com.imcom.bikerama.Tipo.*;

public class RegisterBikeActivity extends AppCompatActivity {

    private static final String TITLE = "Cadastro Bike";
    private static final String TAG = RegisterBikeActivity.class.getSimpleName();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TIPOS   = "tipos";
    private static final String TAG_PERFIS  = "perfis";
    private static final String TAG_PID     = "pid";
    private static final String TAG_BIKEID  = "bikeid";
    private static final String TAG_TIPO    = "tipo";
    private static final String TAG_PERFIL  = "perfil";

    private BackGroundTask bgt;

    // Fields
    Spinner tipoField;
    Spinner perfilField;

    ArrayList<Tipo> listaTipos = new ArrayList<Tipo>();
    ArrayList<Perfil> listaPerfis = new ArrayList<Perfil>();

    private Button btnSubmit;

    private ProgressDialog pDialog;

    private SQLiteHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bike);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String user_name = user.get("name");
        final String user_email = user.get("email");

        /////////////////////////////////////////////////////////////////////////
        //db.deleteBikes(); // ***USAR SOMENTE DURANTE HOMOLOGAÇAO - DEPOIS REMOVER
        /////////////////////////////////////////////////////////////////////////

        RegisterBikeActivity.this.setTitle(TITLE);

        final EditText nomeBike = (EditText) findViewById(R.id.inputNome);
        nomeBike.setHint("Nome da Bike");

        final EditText nomeFabricante = (EditText) findViewById(R.id.inputFabricante);
        nomeFabricante.setHint("Fabricante (Marca)");

        final EditText nomeModelo = (EditText) findViewById(R.id.inputModelo);
        nomeModelo.setHint("Modelo");

        final EditText nomeNserie = (EditText) findViewById(R.id.inputNserie);
        nomeNserie.setHint("Nº de Série");

        final EditText nomeCor = (EditText) findViewById(R.id.inputCor);
        nomeCor.setHint("Cor");

        final EditText dtCompra = (EditText) findViewById(R.id.inputDtCompra);
        dtCompra.setHint("Data de Compra (DD/MM/AAAA)");
        dtCompra.addTextChangedListener(Mask.insert("##/##/####", dtCompra));

        tipoField = (Spinner) findViewById(R.id.spinnerTipo);
        perfilField = (Spinner) findViewById(R.id.spinnerPerfil);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        // Register Button Click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String var_Bike = nomeBike.getText().toString().trim();
                String var_Fabricante = nomeFabricante.getText().toString().trim();
                String var_Modelo = nomeModelo.getText().toString().trim();
                String var_Nserie = nomeNserie.getText().toString().trim();
                String var_Cor = nomeCor.getText().toString().trim();
                String var_Compra = dtCompra.getText().toString().trim();
                String var_Tipo = tipoField.getSelectedItem().toString();
                //String var_Tipo = Tipo.getId();
                //String var_Perfil = perfilField.getSelectedItem().toString();
                String var_Perfil = Perfil.getId();

                if (!var_Bike.isEmpty()) {
                    // Salvar
                    SaveBike(var_Bike, var_Fabricante, var_Modelo, var_Nserie, var_Cor, var_Compra, var_Tipo, var_Perfil, user_email);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Por favor preencha o campo Nome da Bike!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        buildTipoDropDown();
        buildPerfilDropDown();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }


    // Salva os dados de usuario
    private void SaveBike(final String var_Bike, final String var_Fabricante, final String var_Modelo, final String var_Nserie, final String var_Cor, final String var_Compra, final String var_Tipo, final String var_Perfil, final String user_email) {

        // Tag used to cancel the request
        String tag_string_req = "req_register2";

        pDialog.setMessage("Salvando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SAVE_BIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Save Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String bike_id = jObj.getString("pid");
                    String bike = jObj.getString("bike");
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

                        Log.d(TAG, "Bike ID (Bike Activity): " + bike_id.toString());
                        Log.d(TAG, "Bike (Bike Activity): " + bike.toString());

                        // Verificar se passou dados da Bike cadastrada, se sim, gravar do SQLite
                        if(bike_id != null){
                            // Inserting row in users table
                            db.addBike(bike, bike_id);
                        }

                        // Launch Cadastro Bike Activity
                        Intent intent = new Intent(
                                RegisterBikeActivity.this,
                                RegisterComponentesActivity.class);
                        // sending pid to next activity
                        intent.putExtra(TAG_BIKEID, bike_id);
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
                params.put("bike", String.valueOf(var_Bike));
                params.put("fabricante", String.valueOf(var_Fabricante));
                params.put("modelo", String.valueOf(var_Modelo));
                params.put("nserie", String.valueOf(var_Nserie));
                params.put("cor", String.valueOf(var_Cor));
                params.put("dtcompra", String.valueOf(var_Compra));
                params.put("tipo", String.valueOf(var_Tipo));
                params.put("perfil", String.valueOf(var_Perfil));
                params.put("email_user", user_email);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    // Carrega os Tipos
    public void buildTipoDropDown() {

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, "lista"));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_TIPO, "GET", apiParams);

        try {
            JSONObject tipoJSON = bgt.execute().get();
            // Getting Array
            JSONArray tipos = tipoJSON.getJSONArray(TAG_TIPOS);

            // looping through All
            for (int i = 0; i < tipos.length(); i++) {

                JSONObject c = tipos.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String name = c.getString(TAG_TIPO);

                // add Tipo
                listaTipos.add(new Tipo(id, name.toUpperCase()));
            }

            // bind adapter to spinner
            tipoField = (Spinner) findViewById(R.id.spinnerTipo);
            final TipoAdapter cTipoAdapter = new TipoAdapter(RegisterBikeActivity.this, android.R.layout.simple_spinner_item, listaTipos);
            tipoField.setPrompt("Selecione um Tipo");
            tipoField.setAdapter(cTipoAdapter);

            tipoField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Tipo selectedTipo = listaTipos.get(position);
                    //showToast(selectedTipo.getName() + " foi selecionado!");

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerTipo) {
                        //do this
                        String tipoID = selectedTipo.getId();
                        //showToast(selectedTipo.getName() + " foi selecionado!" + " | Cod: " + tipoID);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //buildCidadeDropDown("1");
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Carrega os Perfis
    public void buildPerfilDropDown() {

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, "lista"));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_PERFIL, "GET", apiParams);

        try {
            JSONObject perfilJSON = bgt.execute().get();
            // Getting Array
            JSONArray perfis = perfilJSON.getJSONArray(TAG_PERFIS);

            // looping through All
            for (int i = 0; i < perfis.length(); i++) {

                JSONObject c = perfis.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String name = c.getString(TAG_PERFIL);

                // add Estado
                listaPerfis.add(new Perfil(id, name.toUpperCase()));
            }

            // bind adapter to spinner
            perfilField = (Spinner) findViewById(R.id.spinnerPerfil);
            final PerfilAdapter cPerfilAdapter = new PerfilAdapter(RegisterBikeActivity.this, android.R.layout.simple_spinner_item, listaPerfis);
            perfilField.setPrompt("Selecione um Perfil");
            perfilField.setAdapter(cPerfilAdapter);

            perfilField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Perfil selectedPerfil = listaPerfis.get(position);
                    //showToast(selectedEstado.getName() + " foi selecionado!");

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerPerfil) {
                        //do this
                        String perfilID = selectedPerfil.getId();
                        //showToast2(selectedPerfil.getName() + " foi selecionado!" + " | Cod: " + perfilID);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //buildCidadeDropDown("1");
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    // Exibe Mensagem de Estado Selecionado
    public void showToast(String msg) {
        Toast.makeText(this, "Tipo: " + msg, Toast.LENGTH_LONG).show();
    }

    // Exibe Mensagem de Estado Selecionado
    public void showToast2(String msg) {
        Toast.makeText(this, "Perfil: " + msg, Toast.LENGTH_LONG).show();
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
