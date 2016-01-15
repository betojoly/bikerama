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

    private SQLiteHandler db;
    private SessionManager session;

    String pid;

    TextView txtNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_percurso);

        txtNome = (TextView) findViewById(R.id.inputName);

        DetalhesPercursoActivity.this.setTitle(TITLE);

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

        if(percurso_nome != null) {
            txtNome.setText(percurso_nome);
            Log.d(LOG_TAG, "Percurso Nome: " + percurso_nome.toString());
        }else {
            txtNome.setText("Não informado");
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
