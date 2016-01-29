package br.com.imcom.bikerama;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class InicialActivity extends AppCompatActivity {

    private static final String TITLE = "Bikerama";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE = "bike";
    private static final String TAG_BIKEID = "bikeid";

    private TextView txtBike;
    private Button btnLogout;
    private Button btnNewPercurso;
    private Button btnHistorico;
    private Button btnComponentes;
    private Button btnAcessorios;

    private String pid;

    private SQLiteHandler db;
    private SessionManager session;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        txtBike = (TextView) findViewById(R.id.textBike);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnNewPercurso = (Button) findViewById(R.id.btnNovoPercurso);
        btnHistorico = (Button) findViewById(R.id.btnHistorico);
        btnComponentes = (Button) findViewById(R.id.btnComponentes);
        btnAcessorios = (Button) findViewById(R.id.btnAcessorios);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching bikedetails from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        String bike_name = bike.get("name");
        String bike_id = bike.get("uid");

        // Displaying the user details on the screen
        txtBike.setText(bike_name);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Botão Continuar Registro
        btnNewPercurso.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gravarPercurso();
            }
        });

        // Botão Carregar Histórico
        btnHistorico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                carregaHistorico();
            }
        });

        // Botão Carregar Componentes
        btnComponentes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                carregaComponentes();
            }
        });

        // Botão Carregar Acessorios
        btnAcessorios.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                carregaAcessorios();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Menu create
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    /**
     * Menu Selecionado
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                // check for updates action
                CallHelp();
                return true;
            case R.id.action_check_updates:
                // check for updates action
                CheckUpdates();
                return true;
            case R.id.action_settings:
                // check for updates action
                VerifySettings();
                return true;
            case R.id.action_help2:
                // check for updates action
                CallHelp2();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

    /**
     * Launching new activity
     */
    private void CheckUpdates() {
        Toast.makeText(InicialActivity.this, "Verificando Atualizações...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InicialActivity.this, Teste_Remover_Depois_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Launching new activity
     */
    private void CallHelp() {
        Intent intent = new Intent(InicialActivity.this, FullscreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Launching new activity
     */
    private void CallHelp2() {
        Intent intent = new Intent(InicialActivity.this, LocationServiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Launching new activity
     */
    private void VerifySettings() {
        Intent intent = new Intent(InicialActivity.this, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(InicialActivity.this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    /**
     * Continua o Registro e Sai da Tela de Boas Vindas
     */
    private void gravarPercurso() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, RegistroPercursoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega o Historico de Atividades
     */
    private void carregaHistorico() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, HistoricoPercursosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega Componentes
     */
    private void carregaComponentes() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, ViewComponentesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        //finish();
    }

    /**
     * Carrega Acessorios
     */
    private void carregaAcessorios() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, ViewAcessoriosActivity.class);
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
}
