package br.com.imcom.bikerama;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class DetalhesComponentesActivity extends AppCompatActivity {

    private static final String TITLE = "Detalhes Componentes";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";

    private static final String KEY_COMPONENTES = "componentes";
    private static final String KEY_NOME_COMPONENTES = "componente_nome";
    private static final String KEY_VALOR_COMPONENTES = "componente_valor";
    private static final String KEY_DATA_CRIACAO = "data_criacao";
    private static final String KEY_ID_COMPONENTES = "ID";
    private static final String KEY_BIKE_ID = "bike_id";

    TextView componente_ID;
    TextView componente_nome;
    TextView componente_nome_DB;
    EditText componente_valor;
    TextView data_criacao;
    TextView nova_data;

    public String bike_id;
    public String bike_name;

    private Button btnSubmit;
    private Button btnCalendar;

    private ProgressDialog pDialog;

    JSONArray localObj = null;

    private SQLiteHandler db;
    private SessionManager session;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    String pid;
    String nomeComp;
    String var_Data_Formatada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_componentes);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        final EditText valorComponente = (EditText) findViewById(R.id.valorComponente);
        final TextView dataComponente = (TextView) findViewById(R.id.dateComponente);
        dataComponente.setEnabled(false);
        final TextView novadataComponente = (TextView) findViewById(R.id.novadateComponente);
        final TextView componenteNomeDB = (TextView) findViewById(R.id.componenteNomeDB);

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // getting product details from intent
        Intent i = getIntent();
        // getting product id (pid) from intent
        pid = i.getStringExtra(KEY_ID_COMPONENTES);
        nomeComp = i.getStringExtra(KEY_NOME_COMPONENTES).toUpperCase();

        // Verifica código da Bike Cadastrada
        VerificaBikeSQLite();

        if (pid != null) {
            // Verifica se tem Conexao com a internet
            if (detectaConexao.existeConexao()) {

                // Busca os Componentes da Bike no servidor
                new getDetalhes().execute();
            } else {
                mostraAlerta();
            }

        }

        btnCalendar = (Button) findViewById(R.id.btnCalendar);

        // Mostra calendario para selecionar data
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showPopup(DetalhesComponentesActivity.this);
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        // Register Button Click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Controle
                String var_ID = componente_ID.getText().toString().trim();
                String var_Componente_DB = componenteNomeDB.getText().toString().trim().toLowerCase();
                String var_Componente = componente_nome.getText().toString().trim().toLowerCase();
                String var_Valor = componente_valor.getText().toString().trim();
                String var_Data = dataComponente.getText().toString().trim();
                String var_NovaData = novadataComponente.getText().toString().trim();

                if (!var_NovaData.isEmpty() && var_NovaData != null) {
                    // Formatar Data para "dd/mm/yyyy";
                    String[] items1 = var_NovaData.split("/");
                    String date1 = items1[0];
                    String month = items1[1];
                    String year = items1[2];
                    int d1 = date1.length();
                    int m2 = month.length();
                    if (d1 == 1) {
                        date1 = "0" + date1;
                    }
                    if (m2 == 1) {
                        month = "0" + month;
                    }
                    var_Data_Formatada = year + "-" + month + "-" + date1;
                    Log.d(LOG_TAG, "Data Formatada: " + var_Data_Formatada.toString());

                    // Salvar com Nova data
                    String var_DataFinal = var_Data_Formatada;
                    SaveComponentes(var_ID, var_Componente_DB, var_Valor, var_DataFinal, bike_id);
                    Log.d(LOG_TAG, "SaveComponentes: " + var_ID + ", " + var_Componente_DB + ", " + var_Valor + ", " + var_DataFinal + ", " + bike_id);
                } else {
                    // Formatar Data para "dd/mm/yyyy";
                    String[] items1 = var_Data.split("/");
                    String date1 = items1[0];
                    String month = items1[1];
                    String year = items1[2];
                    int d1 = date1.length();
                    int m2 = month.length();
                    if (d1 == 1) {
                        date1 = "0" + date1;
                    }
                    if (m2 == 1) {
                        month = "0" + month;
                    }
                    var_Data_Formatada = year + "-" + month + "-" + date1;
                    Log.d(LOG_TAG, "Data Formatada: " + var_Data_Formatada.toString());

                    // Apenas Atualiza com data atual
                    String var_DataFinal = var_Data_Formatada;
                    SaveComponentes(var_ID, var_Componente_DB, var_Valor, var_DataFinal, bike_id);
                    Log.d(LOG_TAG, "SaveComponentes: " + var_ID + ", " + var_Componente_DB + ", " + var_Valor + ", " + var_DataFinal + ", " + bike_id);
                }
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    // Salva os dados de componentes
    private void SaveComponentes(final String var_ID, final String var_Componente, final String var_Valor, final String var_Data, final String bike_id) {

        // Tag used to cancel the request
        String tag_string_req = "req_update_comp";

        pDialog.setMessage("Salvando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_COMP_BIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "Save Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String pid = jObj.getString(KEY_ID_COMPONENTES);
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

                        // Launch Cadastro Bike Activity
                        Intent intent = new Intent(
                                DetalhesComponentesActivity.this,
                                ViewComponentesActivity.class);
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
                Log.e(LOG_TAG, "Save Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_ID_COMPONENTES, String.valueOf(var_ID));
                params.put(KEY_NOME_COMPONENTES, String.valueOf(var_Componente));
                params.put(KEY_VALOR_COMPONENTES, String.valueOf(var_Valor));
                params.put(KEY_DATA_CRIACAO, String.valueOf(var_Data));
                params.put(KEY_BIKE_ID, String.valueOf(bike_id));
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

    // Verifica Bike no SQLite
    public void VerificaBikeSQLite() {

        // Fetching bike details from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        bike_name = bike.get("name");
        bike_id = bike.get("uid");
    }

    // The method that displays the popup.
    private void showPopup(Activity context) {

        // Inflate the calendar.xml
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.calendar, null,false);
        // Creating the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(
                layout,400,400);

        popupWindow.setContentView(layout);
        popupWindow.setHeight(500);
        popupWindow.setOutsideTouchable(false);
        // Clear the default translucent background
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        CalendarView cv = (CalendarView) layout.findViewById(R.id.calendarView);
        cv.setBackgroundColor(Color.WHITE);

        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
                month = month + 1;
                Log.d("date selected", "date selected " + year + " - " + month + " - " + dayOfMonth);
                String data_selected = dayOfMonth + "/" + month + "/" + year;
                nova_data.setText(data_selected);

            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP,5,170);
    }

    /*
    * carrega os detalhes
     */
    private class getDetalhes extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            componente_ID = (TextView) findViewById(R.id.id);
            componente_nome = (TextView) findViewById(R.id.nomeComponente);
            componente_valor = (EditText) findViewById(R.id.valorComponente);
            data_criacao = (TextView) findViewById(R.id.dateComponente);
            nova_data = (TextView) findViewById(R.id.novadateComponente);
            componente_nome_DB = (TextView) findViewById(R.id.componenteNomeDB);

            pDialog = new ProgressDialog(DetalhesComponentesActivity.this);
            pDialog.setMessage("Obtendo dados...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONParserNova jParser = new JSONParserNova();

            // Building Parameters, Use a HashMap instead with the varargs:
            HashMap<String, String> params = new HashMap<>();
            params.put("bike_id", bike_id);
            params.put("comp_id", pid);

            // Escrever um LOG dos Params
            Log.d(LOG_TAG, "params: " + params);

            // Getting JSON from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_DETALHE_COMPONENTES, "GET", params);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();

            // Check for success tag
            int success = 0; //initialize to zero

            try {
                //added null check:
                if (json != null) {
                    // check your log for json response
                    Log.d(LOG_TAG, "JSON String: " + json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    String ID = json.getString(KEY_ID_COMPONENTES);
                    String nome = json.getString(KEY_NOME_COMPONENTES).toUpperCase();
                    String valor = json.getString(KEY_VALOR_COMPONENTES);
                    String data = json.getString(KEY_DATA_CRIACAO);

                    if (success == 1) {
                        // successfully received details
                        if (valor != null) {
                            componente_valor.setText(valor);
                            Log.d(LOG_TAG, "Valor: " + valor.toString());
                        } else {
                            componente_valor.setText("Não informado");
                        }

                        // Formatar Data para "dd/mm/yyyy";
                        String[] items1 = data.split("-");
                        String year=items1[0];
                        String month=items1[1];
                        String date1=items1[2];
                        String date = date1 + "/" + month + "/" + year;

                        componente_ID.setText(ID);
                        componente_nome_DB.setText(nome);
                        componente_nome.setText(nomeComp);
                        data_criacao.setText(date);

                    } else {
                        // Informa Erro
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(DetalhesComponentesActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Mostra a informação caso não tenha internet.
     */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(DetalhesComponentesActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();

    }
}
