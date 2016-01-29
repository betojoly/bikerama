package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TITLE = "";
    //private static final String TITLE = "Bikerama";

    private static final String LOG_TAG = Main2Activity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE = "bike";
    private static final String TAG_BIKEID = "bikeid";
    private static final String TAG_PERCURSO_ID  = "percursoid";
    private static final String TAG_PERCURSO_DATA  = "date";

    private String pid;

    private SQLiteHandler db;
    private SessionManager session;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.bikerama_action); // or setLogo()

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        final String email = user.get("email");
        pid = user.get("email");

        // Fetching bikedetails from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        String bike_name = bike.get("name");
        String bike_id = bike.get("uid");

        // Verifica se tem Conexao com a internet
        if (detectaConexao.existeConexao()) {

            // Verifica Kilometragem Total
            new VerificaKilometragem().execute();
        }
        else {
            mostraAlerta();
        }

        if(bike_id != null){
            // Botão Gravar Percurso
            ImageView imgPercurso = (ImageView) findViewById(R.id.btnPercurso);
            final String finalBike_id = bike_id;
            imgPercurso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Main2Activity.this,
                            "Iniciar Percurso!", Toast.LENGTH_LONG).show();
                    gravarPercurso(finalBike_id);
                }
            });

        }else{
            // Verifica se tem Bike Cadastrada, se não tiver verifica e cadastra
            new VerificaBike().execute();

            // Fetching bikedetails from sqlite
            HashMap<String, String> newbike = db.getBikeDetails();
            bike_name = newbike.get("name");
            bike_id = newbike.get("uid");

            // Botão Gravar Percurso
            ImageView imgPercurso = (ImageView) findViewById(R.id.btnPercurso);
            final String finalBike_id = bike_id;
            imgPercurso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Main2Activity.this,
                            "Iniciar Percurso!", Toast.LENGTH_LONG).show();
                    gravarPercurso(finalBike_id);
                }
            });
        }

        /*
         * Recupera acao dos Botoes
         */
        ImageView imgBikes = (ImageView) findViewById(R.id.btnBikes);
        imgBikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Bikes!", Toast.LENGTH_LONG).show();
            }
        });

        // Botão Carregar Componentes
        ImageView imgComponentes = (ImageView) findViewById(R.id.btnComponentes);
        imgComponentes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Componentes!", Toast.LENGTH_LONG).show();
                carregaComponentes();
            }
        });

        // Botão Carregar Acessorios
        ImageView imgAcessorios = (ImageView) findViewById(R.id.btnAcessorios);
        imgAcessorios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Acessorios!", Toast.LENGTH_LONG).show();
                carregaAcessorios();
            }
        });

        // Botão Carregar Histórico
        ImageView imgHistorico = (ImageView) findViewById(R.id.btnHistorico);
        imgHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Ver Histórico!", Toast.LENGTH_LONG).show();
                carregaHistorico();
            }
        });

        ImageView imgMecanica = (ImageView) findViewById(R.id.btnMecanica);
        imgMecanica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Mecanica!", Toast.LENGTH_LONG).show();
            }
        });

        ImageView imgBikeFriendly = (ImageView) findViewById(R.id.btnBikeFriendly);
        imgBikeFriendly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Bike Friendly!", Toast.LENGTH_LONG).show();
            }
        });

        ImageView imgRelatorios = (ImageView) findViewById(R.id.btnRelatorios);
        imgRelatorios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Relatórios de manutenção", Toast.LENGTH_LONG).show();
                carregaRelatorios();
            }
        });

        /*ImageView imgSair = (ImageView) findViewById(R.id.btnSair);
        imgSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Main2Activity.this,
                        "Sair!", Toast.LENGTH_LONG).show();
                logoutUser();
            }
        });*/

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        // Clean tables Percurso e Dados_Percurso
        /////////////////////////////////////////////////////////////////////////////
        ////db.deletePercurso(); // ***USAR SOMENTE DURANTE HOMOLOGAÇAO - DEPOIS REMOVER
        ////db.deleteDadosPercurso(); // ***USAR SOMENTE DURANTE HOMOLOGAÇAO - DEPOIS REMOVER
        ////db.deletePercursoFinal(); // ***USAR SOMENTE DURANTE HOMOLOGAÇAO - DEPOIS REMOVER
        /////////////////////////////////////////////////////////////////////////////
        // DROP tables
        /////////////////////////////////////////////////////////////////////////////
        ////db.dropPercursoFinal(); // ***USAR SOMENTE DURANTE HOMOLOGAÇAO - DEPOIS REMOVER
        /////////////////////////////////////////////////////////////////////////////
    }

    // Verifica se Usuario cadastrou Bike
    private class VerificaBike extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Main2Activity.this);
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
            params.put("email_user", pid);

            // Escrever um LOG dos Params
            Log.d(LOG_TAG, "params: " + params);

            // Getting JSON from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CONSULTA_BIKE, "POST", params);
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
                    String new_bike_id = json.getString("bike_id");
                    String new_bike = json.getString("bike");

                    if (success == 1) {
                        // successfully received Data Cadastro
                        //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();

                        // Verificar se passou dados da Bike cadastrada, se sim, gravar do SQLite
                        if(new_bike_id != null){
                            // Inserting row in users table
                            db.addBike(new_bike, new_bike_id);
                        }

                    } else if (success == 0) {
                        // Envia para Tela Inicial
                        Intent intent = new Intent(
                                Main2Activity.this,
                                RegisterBikeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // Verifica se Usuario cadastrou Bike
    private class VerificaKilometragem extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Main2Activity.this);
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
            params.put("email_user", pid);

            // Escrever um LOG dos Params
            Log.d(LOG_TAG, "params: " + params);

            // Getting JSON from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CONSULTA_BIKE_ALERTA, "POST", params);
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
                    String new_bike_id = json.getString("bike_id");
                    String new_bike = json.getString("bike");
                    String kilometragem = json.getString("kilometros");
                    String manual_id = json.getString("manual_id");
                    String manutencao_id = json.getString("manutencao_id");
                    String data_criacao = json.getString("data_criacao");
                    String verificacao = json.getString("verificacao");
                    String conjunto = json.getString("conjunto");

                    if (success == 1) {
                        // successfully received Data Cadastro
                        //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();

                        // Verificar se passou dados da Bike cadastrada, se sim, gravar do SQLite
                        if(kilometragem != null){
                            // Inserting row in users table
                            db.updateBike(new_bike_id, kilometragem);

                            // Arredondar Kilometragem Total
                            double roundedKilometragem = (double) Math.round(Double.parseDouble(kilometragem) * 100) / 100;

                            // Verificar se passou dados de Manutenção cadastrada, se sim, gravar do SQLite
                            if(verificacao != null){
                                // Inserting row in users table
                                db.addRelatorio(manual_id, manutencao_id, data_criacao, verificacao, conjunto);

                                // Mostra Alerta da Kilometragem Total
                                mostraAlertaKiloRelatorio(roundedKilometragem);
                            }else{
                                // Mostra Alerta da Kilometragem Total
                                mostraAlertaKilo(roundedKilometragem);
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // check for updates action
            CheckUpdates();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Menu lateral
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Launching new activity
     */
    private void CheckUpdates() {
        Toast.makeText(Main2Activity.this, "Verificando Atualizações...", Toast.LENGTH_SHORT).show();
        /*Intent intent = new Intent(Main2Activity.this, FullscreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);*/
    }

    /**
     * Continua o Registro e Sai da Tela de Boas Vindas
     *
     * @param bike_id*/
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
        //Intent intent = new Intent(Main2Activity.this, LocationActivity.class);
        Intent intent = new Intent(Main2Activity.this, GravaPercursoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // sending id to next activity
        intent.putExtra(TAG_PERCURSO_ID, percurso_id);
        intent.putExtra(TAG_PERCURSO_DATA, data_completa);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega o Historico de Atividades
     */
    private void carregaHistorico() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(Main2Activity.this, HistoricoPercursosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega os Relatórios de Manutenção
     */
    private void carregaRelatorios() {

        // Launching the ViewRelatorios activity
        Intent intent = new Intent(Main2Activity.this, ViewRelatoriosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega Componentes
     */
    private void carregaComponentes() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(Main2Activity.this, ViewComponentesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega Acessorios
     */
    private void carregaAcessorios() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(Main2Activity.this, ViewAcessoriosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Inicial Page", // TODO: Define a title for the content shown.
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
                "Inicial Page", // TODO: Define a title for the content shown.
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

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Main2Activity.this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    /**
     * Mostra a informação caso não tenha internet.
     * */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(Main2Activity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Fechar", null).show();
    }

    /**
     * Mostra a informação caso Kilometragem Total seja informada.
     *
     * @param kilometragem*/
    private void mostraAlertaKilo(double kilometragem) {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(Main2Activity.this);
        informa.setTitle("Kilometragem Acumulada: \n " + kilometragem + " Km");
        informa.setNeutralButton("Fechar", null).show();
    }

    /**
     * Mostra a informação caso Kilometragem Total seja informada.
     *
     * @param kilometragem*/
    private void mostraAlertaKiloRelatorio(double kilometragem) {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(Main2Activity.this);
        informa.setTitle("Kilometragem Acumulada:\n" + kilometragem + " Km" + "\n\n" + "Alerta de Manutenção:\n" + "Verifique a Guia Relatórios.");
        informa.setNeutralButton("Fechar", null).show();
    }
}
