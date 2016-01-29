package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

public class Register3Activity extends AppCompatActivity {

    private static final String TITLE = "Cadastro";
    private static final String TAG = Register3Activity.class.getSimpleName();
    private static final String estadoTextForSpinner = "Selecione um Estado";
    private static final String cidadeTextForSpinner = "Selecione uma Cidade";
    private static final String idadeTextForSpinner  = "Idade";
    private static final String sexoTextForSpinner   = "Sexo";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ESTADOS = "estados";
    private static final String TAG_CIDADES = "cidades";
    private static final String TAG_IDADES  = "idades";
    private static final String TAG_SEXOS   = "sexos";
    private static final String TAG_PID     = "pid";
    private static final String TAG_NAME    = "estado";
    private static final String TAG_CIDADE  = "cidade";
    private static final String TAG_IDADE   = "idade";
    private static final String TAG_SEXO    = "sexo";

    private BackGroundTask bgt;

    // Fields
    Spinner estadoField;
    Spinner cidadeField;
    Spinner idadeField;
    Spinner sexoField;

    ArrayList<Estado> listaEstados = new ArrayList<Estado>();
    ArrayList<Cidade> listaCidades = new ArrayList<Cidade>();
    ArrayList<Idade> listaIdades   = new ArrayList<Idade>();
    ArrayList<Sexo> listaSexos     = new ArrayList<Sexo>();

    private Button btnSubmit;

    private ProgressDialog pDialog;

    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register3);

        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        // Permanecer nesta Tela
        Toast.makeText(getApplicationContext(), "Complete seu Cadastro!", Toast.LENGTH_LONG).show();

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String user_name = user.get("name");
        final String user_email = user.get("email");

        Register3Activity.this.setTitle(TITLE);

        final EditText cep = (EditText) findViewById(R.id.inputCep);
        cep.setHint("CEP");
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String var_cep = cep.getText().toString().trim();
                int var_estado = estadoField.getSelectedItemPosition();
                String var_cidade = cidadeField.getSelectedItem().toString();
                String var_idade  = idadeField.getSelectedItem().toString();
                //String var_idade  = Idade.getId().toString();
                String var_sexo   = sexoField.getSelectedItem().toString();
                //String var_sexo   = Sexo.getId().toString();

                if (!var_cep.isEmpty()) {
                    // Salvar
                    SaveUser(var_cep, var_estado, var_cidade, var_idade, var_sexo, user_email);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Por favor preencha o campo CEP!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        buildEstadoDropDown();
        buildIdadeDropDown();
        buildSexoDropDown();
    }


    // Salva os dados de usuario
    private void SaveUser(final String var_cep, final int var_estado, final String var_cidade, final String var_idade, final String var_sexo, final String user_email) {


        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registrando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SAVE_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

                        // Grava status de cadastro completo no SQLite
                        db.addCadastro("1");

                        // Launch Cadastro Bike Activity
                        Intent intent = new Intent(
                                Register3Activity.this,
                                RegisterBikeActivity.class);
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
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("cep", var_cep);
                params.put("estado", String.valueOf(var_estado));
                params.put("cidade", String.valueOf(var_cidade));
                params.put("idade", String.valueOf(var_idade));
                params.put("sexo", String.valueOf(var_sexo));
                params.put("email_user", user_email);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    // Carrega os Estados
    public void buildEstadoDropDown() {

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, "lista"));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_ESTADO, "GET", apiParams);

        try {
            JSONObject estadoJSON = bgt.execute().get();
            // Getting Array
            JSONArray estados = estadoJSON.getJSONArray(TAG_ESTADOS);

            // looping through All
            for (int i = 0; i < estados.length(); i++) {

                JSONObject c = estados.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String name = c.getString(TAG_NAME);

                // add Estado
                listaEstados.add(new Estado(id, name.toUpperCase()));
            }

            // bind adapter to spinner
            estadoField = (Spinner) findViewById(R.id.spinnerEstado);
            final EstadoAdapter cEstadoAdapter = new EstadoAdapter(Register3Activity.this, android.R.layout.simple_spinner_item, listaEstados);
            estadoField.setPrompt("Selecione um Estado");
            estadoField.setAdapter(cEstadoAdapter);
            /*estadoField.setAdapter(
                    new NothingSelectedSpinnerAdapter(
                            cEstadoAdapter,
                            R.layout.content_spinner_estado_nothing_selected,
                            // R.layout.content_spinner_nothing_selected_dropdown, // Optional
                            this));*/

            estadoField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Estado selectedEstado = listaEstados.get(position);
                    //showToast(selectedEstado.getName() + " foi selecionado!");

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerEstado) {
                        //do this
                        String estadoID = selectedEstado.getId();
                        //showToast(selectedEstado.getName() + " foi selecionado!" + " | Cod: " + estadoID);

                        cEstadoAdapter.notifyDataSetChanged();

                        // Chama a outra requisição via API com as cidades
                        buildCidadeDropDown(estadoID);
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


    // Carrega as Cidades
    public ArrayList<Cidade> buildCidadeDropDown(String estadoID) {

        String CodEstado;
        CodEstado = estadoID;

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, CodEstado));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_CIDADE, "GET", apiParams);

        try {
            JSONObject cidadeJSON = bgt.execute().get();
            // Getting Array
            JSONArray cidades = cidadeJSON.getJSONArray(TAG_CIDADES);

            Log.d("Response: ", "> " + cidades);

            // looping through All
            for (int i = 0; i < cidades.length(); i++) {

                JSONObject c = cidades.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String name = c.getString(TAG_CIDADE);

                // add Country
                listaCidades.add(new Cidade(id, name.toUpperCase()));
            }

            // bind adapter to spinner
            cidadeField = (Spinner) findViewById(R.id.spinnerCidade);
            CidadeAdapter cCidadeAdapter = new CidadeAdapter(Register3Activity.this, android.R.layout.simple_spinner_item, listaCidades);
            cidadeField.setPrompt("Selecione uma Cidade");
            cidadeField.setAdapter(cCidadeAdapter);
            //cAdapter.notifyDataSetChanged();
            /*cidadeField.setAdapter(
                    new NothingSelectedSpinnerAdapter(
                            cCidadeAdapter,
                            R.layout.content_spinner_cidade_nothing_selected,
                            this));*/

            cidadeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Cidade selectedCidade = listaCidades.get(position);

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerCidade) {
                        //do this
                        String cidadeID = selectedCidade.getId();
                        //showToast2(selectedCidade.getName() + " foi selecionada!" + " | ID: " + cidadeID);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Carrega as Idades
    public void buildIdadeDropDown() {

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, "lista"));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_IDADE, "GET", apiParams);

        try {
            JSONObject idadeJSON = bgt.execute().get();
            // Getting Array
            JSONArray idades = idadeJSON.getJSONArray(TAG_IDADES);

            // looping through All
            for (int i = 0; i < idades.length(); i++) {

                JSONObject c = idades.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String idade = c.getString(TAG_IDADE);

                // add Idade
                listaIdades.add(new Idade(id, idade));
            }

            // bind adapter to spinner
            idadeField = (Spinner) findViewById(R.id.spinnerIdade);
            IdadeAdapter cIdadeAdapter = new IdadeAdapter(Register3Activity.this, android.R.layout.simple_spinner_item, listaIdades);
            idadeField.setPrompt("Idade");
            idadeField.setAdapter(cIdadeAdapter);

            idadeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Idade selectedIdade = listaIdades.get(position);
                    //showToast(selectedEstado.getName() + " foi selecionado!");

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerIdade) {
                        //do this
                        String idadeID = selectedIdade.getId();
                        //showToast3(selectedIdade.getName() + " foi selecionada!");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
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


    // Carrega o Sexo
    public void buildSexoDropDown() {

        // Building post parameters, key and value pair
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>(1);
        apiParams.add(new BasicNameValuePair(TAG_PID, "lista"));

        bgt = new BackGroundTask(AppConfig.MAP_API_URL_SEXO, "GET", apiParams);

        try {
            JSONObject sexoJSON = bgt.execute().get();
            // Getting Array
            JSONArray sexos = sexoJSON.getJSONArray(TAG_SEXOS);

            // looping through All
            for (int i = 0; i < sexos.length(); i++) {

                JSONObject c = sexos.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String sexo = c.getString(TAG_SEXO);

                // add Idade
                listaSexos.add(new Sexo(id, sexo));
            }

            // bind adapter to spinner
            sexoField = (Spinner) findViewById(R.id.spinnerSexo);
            SexoAdapter cSexoAdapter = new SexoAdapter(Register3Activity.this, android.R.layout.simple_spinner_item, listaSexos);
            sexoField.setPrompt("Sexo");
            sexoField.setAdapter(cSexoAdapter);

            sexoField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Sexo selectedSexo = listaSexos.get(position);
                    //showToast(selectedEstado.getName() + " foi selecionado!");

                    Spinner spinner = (Spinner) parent;
                    if (spinner.getId() == R.id.spinnerSexo) {
                        //do this
                        String sexoID = selectedSexo.getId();
                        //showToast4(selectedSexo.getName() + " foi selecionado!");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
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
        Toast.makeText(this, "Estado: " + msg, Toast.LENGTH_LONG).show();
    }

    // Exibe Mensagem de Cidade Selecionado
    public void showToast2(String msg) {
        Toast.makeText(this, "Cidade: " + msg, Toast.LENGTH_LONG).show();
    }

    // Exibe Mensagem de Idade Selecionado
    public void showToast3(String msg) {
        Toast.makeText(this, "Idade: " + msg, Toast.LENGTH_LONG).show();
    }

    // Exibe Mensagem de Idade Selecionado
    public void showToast4(String msg) {
        Toast.makeText(this, "Sexo: " + msg, Toast.LENGTH_LONG).show();
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
