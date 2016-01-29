package br.com.imcom.bikerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class DetalhesRelatorioActivity extends AppCompatActivity {

    private static final String TITLE = "Detalhes Relatório";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TAG_PERCURSO_ID = "percursoid";
    // Relatorio Table Columns names
    private static final String KEY_RELATORIO_ID = "id";
    private static final String KEY_RELATORIO_MANUTENCAO_ID = "manutencao_id";
    private static final String KEY_RELATORIO_MANUAL_ID = "manual_id";
    private static final String KEY_RELATORIO_DATA = "data_criacao";
    private static final String KEY_RELATORIO_STATUS = "status";
    private static final String KEY_RELATORIO_VERIFICACAO = "verificacao";
    private static final String KEY_RELATORIO_CONJUNTO = "conjunto";

    private SQLiteHandler db;
    private SessionManager session;

    String pid;

    TextView txtData;
    TextView txtConjunto;
    TextView txtStatus;
    TextView txtVerificacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_relatorio);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        txtData = (TextView) findViewById(R.id.txtData);
        txtConjunto = (TextView) findViewById(R.id.txtConjunto);
        txtStatus = (TextView) findViewById(R.id.txtSincro);
        txtVerificacao = (TextView) findViewById(R.id.txtVerificacao);

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
        pid = i.getStringExtra(KEY_RELATORIO_ID);

        if(pid != null){
            getDetalhes();
        }
    }

    private void getDetalhes() {

        HashMap<String, String> percurso = db.getRelatorioDetalhes(pid);

        final String relatorio_id = percurso.get(KEY_RELATORIO_ID);
        final String relatorio_data = percurso.get(KEY_RELATORIO_DATA);
        final String relatorio_status = percurso.get(KEY_RELATORIO_STATUS);
        final String relatorio_conjunto = percurso.get(KEY_RELATORIO_CONJUNTO);
        final String relatorio_verificacao = percurso.get(KEY_RELATORIO_VERIFICACAO);

        if(relatorio_data != null) {
            // Formatar Data para "dd/mm/yyyy";
            String[] items1 = relatorio_data.split("-");
            String year  = items1[0];
            String month = items1[1];
            String date1 = items1[2];
            String date = date1 + "/" + month + "/" + year;

            txtData.setText(date);
            Log.d(LOG_TAG, "DATA: " + date.toString());
        }

        if(relatorio_verificacao != null) {
            txtVerificacao.setText(relatorio_verificacao);
            Log.d(LOG_TAG, "VERIFICACAO: " + relatorio_verificacao.toString());
        }

        if(relatorio_conjunto != null) {
            txtConjunto.setText(relatorio_conjunto);
            Log.d(LOG_TAG, "CONJUNTO: " + relatorio_conjunto.toString());
        }

        if(relatorio_status != null) {
            if(relatorio_status.equals("yes")){
                txtStatus.setText("Sim");
            }else{
                txtStatus.setText("Não");
            }
            Log.d(LOG_TAG, "SINCRONIZADO: " + relatorio_status.toString());
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
        Intent intent = new Intent(DetalhesRelatorioActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
