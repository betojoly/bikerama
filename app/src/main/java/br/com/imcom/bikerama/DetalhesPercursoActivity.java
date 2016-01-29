package br.com.imcom.bikerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class DetalhesPercursoActivity extends AppCompatActivity {

    private static final String TITLE = "Detalhes Percurso";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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
    private static final String KEY_PERCURSO_STATUS = "updatestatus";
    private static final String KEY_PERCURSO_DIST = "distancia";

    private SQLiteHandler db;
    private SessionManager session;

    String pid;

    TextView txtNome;
    TextView txtData;
    TextView txtTipo;
    TextView txtNivel;
    TextView txtDescricao;
    TextView txtSincronizado;
    TextView txtKilometros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_percurso);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        txtNome = (TextView) findViewById(R.id.txtNome);
        txtData = (TextView) findViewById(R.id.txtData);
        txtTipo = (TextView) findViewById(R.id.txtTipo);
        txtNivel = (TextView) findViewById(R.id.txtNivel);
        txtDescricao = (TextView) findViewById(R.id.txtDescricao);
        txtKilometros = (TextView) findViewById(R.id.txtDistancia);
        txtSincronizado = (TextView) findViewById(R.id.txtSincro);

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
        pid = i.getStringExtra(KEY_PERCURSO_ID);

        if(pid != null){
            getDetalhes();
        }
    }

    private void getDetalhes() {

        HashMap<String, String> percurso = db.getPercursoDetalhes(pid);

        final String percurso_id = percurso.get(KEY_PERCURSO_ID);
        final String percurso_bike = percurso.get(KEY_PERCURSO_BIKEID);
        final String percurso_data = percurso.get(KEY_PERCURSO_DATA);
        final String percurso_nome = percurso.get(KEY_PERCURSO_NOME);
        final String percurso_tipo = percurso.get(KEY_PERCURSO_TIPO);
        final String percurso_nivel = percurso.get(KEY_PERCURSO_NIVEL);
        final String percurso_descricao = percurso.get(KEY_PERCURSO_DESCRICAO);
        final String percurso_sincronizado = percurso.get(KEY_PERCURSO_STATUS);
        final String percurso_kilometragem = percurso.get(KEY_PERCURSO_DIST);

        if(percurso_nome != null) {
            txtNome.setText(percurso_nome);
            Log.d(LOG_TAG, "Percurso Nome: " + percurso_nome.toString());
        }else {
            txtNome.setText("Não informado");
        }

        if(percurso_data != null) {
            // Separar Data de Hora
            int tamanho = percurso_data.length();
            double aux = tamanho / 10;

            int qtde = (int) aux + 1;

            String[] res = new String[qtde];
            int inicio = 0;
            int fim = 10;

            for(int i = 0; i < qtde; i++){
                if(i == (qtde - 1)){
                    fim = tamanho;
                }
                res[i] = String.valueOf(percurso_data.subSequence(inicio, fim));
                inicio = fim;
                fim =  fim + 10;
                //System.out.println(i + ": " + res[i]);
                //Log.d(LOG_TAG, "Percurso DATA: " + i + ": " + res[i].toString());
            }

            // Formatar Data para "dd/mm/yyyy";
            String[] items1 = res[0].split("-");
            String year  = items1[0];
            String month = items1[1];
            String date1 = items1[2];
            String date = date1 + "/" + month + "/" + year;

            txtData.setText(date);
            Log.d(LOG_TAG, "Percurso DATA: " + date.toString());
        }

        if(percurso_tipo != null) {
            txtTipo.setText(percurso_tipo);
            Log.d(LOG_TAG, "Percurso TIPO: " + percurso_tipo.toString());
        }

        if(percurso_nivel != null) {
            txtNivel.setText(percurso_nivel);
            Log.d(LOG_TAG, "Percurso NIVEL: " + percurso_nivel.toString());
        }

        if(percurso_sincronizado != null) {
            if(percurso_sincronizado.equals("yes")){
                txtSincronizado.setText("Sim");
            }else{
                txtSincronizado.setText("Não");
            }
            Log.d(LOG_TAG, "Percurso SINCRONIZADO: " + percurso_sincronizado.toString());
        }

        if(percurso_descricao != null) {
            txtDescricao.setText(percurso_descricao);
            Log.d(LOG_TAG, "Percurso DESCRICAO: " + percurso_descricao.toString());
        }

        if(percurso_kilometragem != null) {
            // Arredondar Kilometragem Total
            double roundedKilometragem = Double.parseDouble(percurso_kilometragem);
            roundedKilometragem = (double) Math.round(roundedKilometragem * 100) / 100;

            txtKilometros.setText(String.valueOf(roundedKilometragem) + " Km");
            Log.d(LOG_TAG, "Percurso KILOMETROS: " + percurso_kilometragem.toString());
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
        Intent intent = new Intent(DetalhesPercursoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
