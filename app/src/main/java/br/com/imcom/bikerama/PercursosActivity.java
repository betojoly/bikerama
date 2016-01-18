package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class PercursosActivity extends AppCompatActivity {

    private static final String TITLE = "Percursos Salvos";
    private static final String LOG_TAG = PercursosActivity.class.getSimpleName();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PERCURSOS = "percursos";
    private static final String TAG_DADOS_PERCURSOS = "dadospercursos";
    private static final String TAG_BIKE = "bike";
    private static final String TAG_BIKEID = "bikeid";
    private static final String TAG_PERCURSO_ID = "percursoid";
    // Percurso Table Columns names
    private static final String KEY_PERCURSO_ID = "id";
    private static final String KEY_PERCURSO_UID = "uid";
    private static final String KEY_PERCURSO_BIKEID = "bikeid";
    private static final String KEY_PERCURSO_DATA = "date";
    private static final String KEY_PERCURSO_NOME = "nome";
    private static final String KEY_PERCURSO_TIPO = "tipo";
    private static final String KEY_PERCURSO_NIVEL = "nivel";
    private static final String KEY_PERCURSO_DESCRICAO = "descricao";
    private static final String KEY_PERCURSO_STATUS = "status";
    // Dados Percurso Table Columns names
    private static final String KEY_DADOS_PERCURSO_ID = "id";
    private static final String KEY_DADOS_PERCURSO_UID = "uid";
    private static final String KEY_DADOS_PERCURSO_LATLONG = "latlong";
    private static final String KEY_DADOS_PERCURSO_DATA = "date";
    private static final String KEY_DADOS_PERCURSO_STATUS = "updatestatus";

    private TextView txtBike;
    private Button btnLogout;
    private Button btnNewPercurso;

    ListView listViewPercursos;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParserNova jsonParser = new JSONParserNova();

    // locais JSONArray
    JSONArray percursos = null;
    JSONArray dadospercursos = null;

    private SQLiteHandler db;
    private SessionManager session;
    private DetectaConexao detectaConexao;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percursos);

        PercursosActivity.this.setTitle(TITLE);

        // Get ListView object from xml
        listViewPercursos = (ListView) findViewById(R.id.listViewPercursos);

        //txtBike = (TextView) findViewById(R.id.textBike);
        //btnNewPercurso = (Button) findViewById(R.id.btnIniciarPercurso);

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
        HashMap<String, String> bike = db.getBikeDetails();

        String bike_name = bike.get("name");
        final String bike_id = bike.get("uid");

        // Displaying the user details on the screen
        /////txtBike.setText(bike_name);

        // Carrega os Percursos
        displayPercursos();

        // Se abrir ao terminar um Registro, (get Intent)
        // getting id from intent
        Intent i = getIntent();
        final String percurso_id = i.getStringExtra(TAG_PERCURSO_ID);
        Log.d(LOG_TAG, "Percurso ID (Salvo): " + percurso_id.toString());

        if (percurso_id != null) {
            // informa dados salvos corretamente
            Toast.makeText(PercursosActivity.this,
                    "Percurso Salvo com Sucesso! ",
                    Toast.LENGTH_LONG).show();

            // Verifica se tem Conexao com a internet
            if (detectaConexao.existeConexao()) {

                //Display Sync status of SQLite DB
                Toast.makeText(PercursosActivity.this,
                        db.getSyncStatus(),
                        Toast.LENGTH_LONG).show();

                //Sync SQLite DB data to remote MySQL DB - // Get Percursos
                ArrayList<HashMap<String, String>> userList =  db.getallPercursosDB();
                if(userList.size()!=0){
                    if(db.dbSyncCount() != 0){
                        String params = db.composeJSONfromSQLite();
                        new syncSQLiteMySQLDB().execute(params);
                    }else{
                        //Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Dados sendo sincronizados!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    //Toast.makeText(getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Nenhum dado sincronizado", Toast.LENGTH_LONG).show();
                }

            } else {
                mostraAlerta();
            }
        }

        // Botão Continuar Registro
        /*btnNewPercurso.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gravarPercurso(bike_id);
            }
        });*/
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Background Async Task to Transfer Percursos
     * */
    public class syncSQLiteMySQLDB extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PercursosActivity.this);
            pDialog.setMessage("Transferindo dados para servidor remoto. Por favor aguarde...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating Local
         * */
        protected String doInBackground(String... args){
            String dadosGson = args[0];

            // Building Parameters
            Map<String, String> params = new HashMap<>();
            params.put("dadosJSON", dadosGson);

            // Check for success tag
            int success = 0; //initialize to zero

            // getting JSON Object
            // Note that create local url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.URL_EXPORTA_PERCURSO, "POST", (HashMap<String, String>) params);

            // check log cat for response
            //added null check:
            if (json != null) {
                //Log.d("Create Response", json.toString());

                // check for success tag
                try {
                    // Getting Array of Products
                    percursos = json.getJSONArray(TAG_PERCURSOS);

                    // looping through All Products
                    for (int i = 0; i < percursos.length(); i++) {
                        JSONObject c = percursos.getJSONObject(i);

                        // Checking for SUCCESS TAG
                        success = c.getInt(TAG_SUCCESS);

                        if(success == 1){
                            // successfully
                            Log.d("Sucesso ", "OK");
                            db.updateSyncStatus(c.getString(KEY_PERCURSO_ID), c.getString(KEY_PERCURSO_STATUS));

                        } else {
                            // failed
                            Log.d("Falha ", "ERRO");
                            db.updateSyncStatus(c.getString(KEY_PERCURSO_ID), c.getString(KEY_PERCURSO_STATUS));
                        }
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return null;

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            super.onPostExecute(file_url);
            // dismiss the dialog once done
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Sincronização completa!", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * *********************************************************************************************
     * */


    /**
     * Show All Percursos from SQLite
     */
    private void displayPercursos() {

        // Reading all values
        Log.d("Reading: ", "Carregando percursos...");

        // Get ListView object from xml
        listViewPercursos = (ListView) findViewById(R.id.listViewPercursos);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Percurso item : db.getAllPercursos()) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put(KEY_PERCURSO_NOME, item.getNome());
            datum.put(KEY_PERCURSO_DATA, item.getDate().toString());
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.listview_item_percursos,
                new String[] {KEY_PERCURSO_NOME, KEY_PERCURSO_DATA},
                new int[] {R.id.nome,
                        R.id.date});

        listViewPercursos.setAdapter(adapter);
    }


    /**
     * Continua o Registro e Sai da Tela de Boas Vindas
     *
     * @param bike_id
     */
    public void gravarPercurso(String bike_id) {

        // Define Data de Hoje
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
        Date data = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();
        String data_completa = dateFormat.format(data_atual);
        String hora_atual = dateFormat_hora.format(data_atual);
        String data_SQL = dateFormat.toString();

        Log.i("data_SQL", data_SQL);
        Log.i("data_completa", data_completa);
        Log.i("data_atual", data_atual.toString());
        Log.i("hora_atual", hora_atual);

        // Inserting row in users table
        db.addPercurso(bike_id, data_completa);

        // Recupera o ultimo ID se gravou o novo Percurso from sqlite
        HashMap<String, String> percurso = db.getPercursoLast();
        final String percurso_id = percurso.get("id");

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(PercursosActivity.this,
                LocationActivity.class);
        // sending id to next activity
        intent.putExtra(TAG_PERCURSO_ID, percurso_id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Percursos Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://br.com.imcom.bikerama/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Percursos Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://br.com.imcom.bikerama/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // Mostra a informação caso não tenha internet.
    private void mostraAlerta() {
        AlertDialog.Builder informa = new AlertDialog.Builder(PercursosActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PercursosActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
