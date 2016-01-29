package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class ViewAcessoriosActivity extends AppCompatActivity {

    private static final String TITLE = "Acessórios";
    private static final String LOG_TAG = ViewAcessoriosActivity.class.getSimpleName();

    private SQLiteHandler db;
    private SessionManager session;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    private String bike_id;
    private String bike_name;
    private TextView txtNenhumResultado;

    // Comps JSONArray
    JSONArray comps = null;

    ArrayList<HashMap<String, String>> compsList;

    ListView listViewAcessorios;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PID     = "pid";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bike_id";
    private static final String KEY_EMAIL   = "email_user";
    private static final String TAG_ACESSORIOS  = "acessorios";
    private static final String KEY_ACESSSORIO_ID = "id";
    private static final String KEY_ACESSSORIO_NOME = "acessorio";
    private static final String KEY_ACESSSORIO_PRECO = "preco";
    private static final String KEY_ACESSSORIO_DATA = "data_cad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_acessorios);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                NovoAcessorio();
            }
        });

        // Hashmap for ListView
        compsList = new ArrayList<HashMap<String, String>>();

        // Get ListView object from xml
        listViewAcessorios = (ListView) findViewById(R.id.listViewAcessorios);

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Verifica código da Bike Cadastrada
        VerificaBikeSQLite();

        // on seleting single
        // launching Screen
        /*listViewAcessorios.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.id)).getText().toString();
                String nome = ((TextView) view.findViewById(R.id.nomeComponente)).getText().toString();
                Log.d(LOG_TAG, "Acessorio ID (Selecionado): " + pid.toString());

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), DetalhesComponentesActivity.class);
                // sending pid to next activity
                in.putExtra(KEY_ACESSSORIO_ID, pid);
                in.putExtra(KEY_ACESSSORIO_NOME, nome);
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });*/
    }

    /**
     * Launching new activity
     */
    private void NovoAcessorio() {
        Intent intent = new Intent(ViewAcessoriosActivity.this, RegisterAcessoriosActivity.class);
        intent.putExtra(TAG_BIKEID, bike_id);
        startActivity(intent);
    }

    // Verifica Bike no SQLite
    public void VerificaBikeSQLite() {

        // Fetching bike details from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        bike_name = bike.get("name");
        bike_id = bike.get("uid");

        if (bike_id != null) {
            // Verifica se tem Conexao com a internet
            if (detectaConexao.existeConexao()) {

                // Busca os Acessorios da Bike no servidor
                new VerificaAcessorios().execute();
            }
            else {
                mostraAlerta();
            }
        }
    }

    // Verifica Acessorios cadastrados da Bike
    private class VerificaAcessorios extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ViewAcessoriosActivity.this);
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

            // Escrever um LOG dos Params
            Log.d(LOG_TAG, "params: " + params);

            // Getting JSON from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CONSULTA_ACESSORIOS, "POST", params);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject resultJSON) {
            pDialog.dismiss();

            // Check for success tag
            int success = 0; //initialize to zero

            try {
                //added null check:
                if (resultJSON != null) {
                    // check your log for json response
                    //Log.d(LOG_TAG, "JSON String: " + resultJSON.toString());

                    // json success tag
                    success = resultJSON.getInt(TAG_SUCCESS);
                    //String bike_id = json.getString("bike_id");

                    if (success == 1) {
                        // successfully received
                        // Getting Array of Products
                        comps = resultJSON.getJSONArray(TAG_ACESSORIOS);

                        // looping through All Products
                        for (int i = 0; i < comps.length(); i++) {
                            JSONObject c = comps.getJSONObject(i);

                            // Storing each json item in variable
                            String acessorio_ID = c.getString(KEY_ACESSSORIO_ID);
                            String acessorio_nome = c.getString(KEY_ACESSSORIO_NOME);
                            String acessorio_valor = c.getString(KEY_ACESSSORIO_PRECO);
                            String data_criacao = c.getString(KEY_ACESSSORIO_DATA);

                            // Formatar Data para "dd/mm/yyyy";
                            String[] items0 = data_criacao.split(" ");
                            String[] items1 = items0[0].split("-");
                            String year=items1[0];
                            String month=items1[1];
                            String date1=items1[2];
                            String date = date1 + "/" + month + "/" + year;

                            // FAZER REPLACE DOS NOMES DOS ACESSSORIOS
                            if(acessorio_nome.equals(KEY_ACESSSORIO_NOME)){
                                acessorio_nome = acessorio_valor.replace(KEY_ACESSSORIO_PRECO, "Acessório");
                                //Log.d(LOG_TAG, "Acessorio: " + acessorio_nome.toString());
                            }
                            if(acessorio_valor.equals(KEY_ACESSSORIO_PRECO)){
                                acessorio_valor = acessorio_valor.replace(KEY_ACESSSORIO_PRECO, "Preço");
                            }
                            if(data_criacao.equals(KEY_ACESSSORIO_DATA)){
                                data_criacao = data_criacao.replace(KEY_ACESSSORIO_DATA, "Data Cadastro");
                            }

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(KEY_ACESSSORIO_ID, acessorio_ID);
                            map.put(KEY_ACESSSORIO_NOME, acessorio_nome.toUpperCase());
                            map.put(KEY_ACESSSORIO_PRECO, acessorio_valor);
                            map.put(KEY_ACESSSORIO_DATA, date);
                            // adding HashList to ArrayList
                            compsList.add(map);
                        }

                        //Log.d(LOG_TAG, "compsList: " + compsList);

                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                ViewAcessoriosActivity.this, compsList,
                                R.layout.listview_item_components,
                                new String[] { KEY_ACESSSORIO_ID, KEY_ACESSSORIO_NOME, KEY_ACESSSORIO_PRECO, KEY_ACESSSORIO_DATA},
                                new int[] { R.id.id, R.id.nomeComponente, R.id.valorComponente, R.id.dateComponente });
                        // updating listview
                        listViewAcessorios.setAdapter(adapter);


                    } else if (success == 0) {
                        // Permanecer nesta Tela
                        Toast.makeText(getApplicationContext(), "Dados não obtidos!", Toast.LENGTH_LONG).show();

                        txtNenhumResultado.setText("\n Nenhum Acessório Cadastrado. \n\n Utilize o botão no canto inferior e cadastre.".toString());
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
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ViewAcessoriosActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Mostra a informação caso não tenha internet.
     * */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(ViewAcessoriosActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();
    }

}
