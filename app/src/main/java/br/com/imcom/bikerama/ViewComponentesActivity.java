package br.com.imcom.bikerama;

import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class ViewComponentesActivity extends AppCompatActivity {

    private static final String TITLE = "Componentes";
    private static final String LOG_TAG = HistoricoPercursosActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";

    private static final String KEY_COMPONENTES = "componentes";
    private static final String KEY_NOME_COMPONENTES = "componente_nome";
    private static final String KEY_VALOR_COMPONENTES = "componente_valor";
    private static final String KEY_DATA_CRIACAO = "data_criacao";
    private static final String KEY_ID_COMPONENTES = "ID";
    // Controle
    private static final String KEY_GUIDAO = "guidao";
    private static final String KEY_MANETE = "manete";
    private static final String KEY_DIRECAO = "direcao";
    private static final String KEY_PASSADOR = "passador";
    private static final String KEY_MESA = "mesa";
    // Transmissao
    private static final String KEY_PEDIVELA = "pedivela";
    private static final String KEY_COROA_MAIOR = "coroa_maior";
    private static final String KEY_COROA_INTER = "coroa_inter";
    private static final String KEY_COROA_MENOR = "coroa_menor";
    private static final String KEY_CORRENTE = "corrente";
    private static final String KEY_CASSETE = "cassete";
    private static final String KEY_CENTRAL = "central";
    private static final String KEY_PEDAL = "pedal";
    private static final String KEY_CAMBIO_D = "cambio_diant";
    private static final String KEY_CAMBIO_T = "cambio_tras";
    // Frente
    private static final String KEY_GARFO = "garfo";
    private static final String KEY_ARO_F = "aro_diant";
    private static final String KEY_CUBO_F = "cubo_diant";
    private static final String KEY_PNEU_F = "pneu_diant";
    private static final String KEY_FREIO_F = "freio_diant";
    // Traseira
    private static final String KEY_SHOCK = "shock";
    private static final String KEY_ARO_T = "aro_tras";
    private static final String KEY_CUBO_T = "cubo_tras";
    private static final String KEY_PNEU_T = "pneu_tras";
    private static final String KEY_FREIO_T = "freio_tras";

    private SQLiteHandler db;
    private SessionManager session;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    private String bike_id;
    private String bike_name;

    // Comps JSONArray
    JSONArray comps = null;

    ArrayList<HashMap<String, String>> compsList;

    ListView listViewComponentes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_componentes);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        // Hashmap for ListView
        compsList = new ArrayList<HashMap<String, String>>();

        // Get ListView object from xml
        listViewComponentes = (ListView) findViewById(R.id.listViewComponentes);

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
        listViewComponentes.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.id)).getText().toString();
                String nome = ((TextView) view.findViewById(R.id.nomeComponente)).getText().toString();
                Log.d(LOG_TAG, "Componente ID (Selecionado): " + pid.toString());

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), DetalhesComponentesActivity.class);
                // sending pid to next activity
                in.putExtra(KEY_ID_COMPONENTES, pid);
                in.putExtra(KEY_NOME_COMPONENTES, nome);
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
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

                // Busca os Componentes da Bike no servidor
                new VerificaComponentes().execute();
            }
            else {
                mostraAlerta();
            }
        }
    }

    // Verifica Componentes cadastrados da Bike
    private class VerificaComponentes extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ViewComponentesActivity.this);
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
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CONSULTA_COMPONENTES, "POST", params);
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
                        comps = resultJSON.getJSONArray(KEY_COMPONENTES);

                        // looping through All Products
                        for (int i = 0; i < comps.length(); i++) {
                            JSONObject c = comps.getJSONObject(i);

                            // Storing each json item in variable
                            String componente_ID = c.getString(KEY_ID_COMPONENTES);
                            String componente_nome = c.getString(KEY_NOME_COMPONENTES);
                            String componente_valor = c.getString(KEY_VALOR_COMPONENTES);
                            String data_criacao = c.getString(KEY_DATA_CRIACAO);

                            // Formatar Data para "dd/mm/yyyy";
                            String[] items1 = data_criacao.split("-");
                            String year=items1[0];
                            String month=items1[1];
                            String date1=items1[2];
                            String date = date1 + "/" + month + "/" + year;

                            // FAZER REPLACE DOS NOMES DOS COMPONENTES
                            if(componente_nome.equals(KEY_GUIDAO)){
                                componente_nome = componente_nome.replace(KEY_GUIDAO, "Guidão");
                                //Log.d(LOG_TAG, "Guidao: " + componente_nome.toString());
                            }
                            if(componente_nome.equals(KEY_DIRECAO)){
                                componente_nome = componente_nome.replace(KEY_DIRECAO, "Direção");
                            }
                            // Transmissao
                            if(componente_nome.equals(KEY_COROA_MAIOR)){
                                componente_nome = componente_nome.replace(KEY_COROA_MAIOR, "Coroa Maior");
                            }
                            if(componente_nome.equals(KEY_COROA_INTER)){
                                componente_nome = componente_nome.replace(KEY_COROA_INTER, "Coroa Intermediária");
                            }
                            if(componente_nome.equals(KEY_COROA_MENOR)){
                                componente_nome = componente_nome.replace(KEY_COROA_MENOR, "Coroa Menor");
                            }
                            if(componente_nome.equals(KEY_CAMBIO_D)){
                                componente_nome = componente_nome.replace(KEY_CAMBIO_D, "Câmbio Dianteiro");
                            }
                            if(componente_nome.equals(KEY_CAMBIO_T)){
                                componente_nome = componente_nome.replace(KEY_CAMBIO_T, "Câmbio Traseiro");
                            }
                            // Frente
                            if(componente_nome.equals(KEY_GARFO)){
                                componente_nome = componente_nome.replace(KEY_GARFO, "Garfo/Suspensão Dianteira");
                            }
                            if(componente_nome.equals(KEY_ARO_F)){
                                componente_nome = componente_nome.replace(KEY_ARO_F, "Aro Dianteiro");
                            }
                            if(componente_nome.equals(KEY_CUBO_F)){
                                componente_nome = componente_nome.replace(KEY_CUBO_F, "Cubo Dianteiro");
                            }
                            if(componente_nome.equals(KEY_PNEU_F)){
                                componente_nome = componente_nome.replace(KEY_PNEU_F, "Pneu Dianteiro");
                            }
                            if(componente_nome.equals(KEY_FREIO_F)){
                                componente_nome = componente_nome.replace(KEY_FREIO_F, "Freio Dianteiro");
                            }
                            // Traseira
                            if(componente_nome.equals(KEY_SHOCK)){
                                componente_nome = componente_nome.replace(KEY_SHOCK, "Suspensão Traseira");
                            }
                            if(componente_nome.equals(KEY_ARO_T)){
                                componente_nome = componente_nome.replace(KEY_ARO_T, "Aro Traseiro");
                            }
                            if(componente_nome.equals(KEY_CUBO_T)){
                                componente_nome = componente_nome.replace(KEY_CUBO_T, "Cubo Traseiro");
                            }
                            if(componente_nome.equals(KEY_PNEU_T)){
                                componente_nome = componente_nome.replace(KEY_PNEU_T, "Pneu Traseiro");
                            }
                            if(componente_nome.equals(KEY_FREIO_T)){
                                componente_nome = componente_nome.replace(KEY_FREIO_T, "Freio Traseiro");
                            }

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(KEY_ID_COMPONENTES, componente_ID);
                            map.put(KEY_NOME_COMPONENTES, componente_nome.toUpperCase());
                            map.put(KEY_VALOR_COMPONENTES, componente_valor);
                            map.put(KEY_DATA_CRIACAO, date);
                            // adding HashList to ArrayList
                            compsList.add(map);
                        }

                        //Log.d(LOG_TAG, "compsList: " + compsList);

                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                ViewComponentesActivity.this, compsList,
                                R.layout.listview_item_components,
                                new String[] { KEY_ID_COMPONENTES, KEY_NOME_COMPONENTES, KEY_VALOR_COMPONENTES, KEY_DATA_CRIACAO},
                                new int[] { R.id.id, R.id.nomeComponente, R.id.valorComponente, R.id.dateComponente });
                        // updating listview
                        listViewComponentes.setAdapter(adapter);


                    } else if (success == 0) {
                        // Permanecer nesta Tela
                        Toast.makeText(getApplicationContext(), "Dados não obtidos!", Toast.LENGTH_LONG).show();

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
        Intent intent = new Intent(ViewComponentesActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Mostra a informação caso não tenha internet.
     * */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(ViewComponentesActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();
    }
}
