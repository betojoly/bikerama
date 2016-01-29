package br.com.imcom.bikerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class RegistroPercursoActivity extends AppCompatActivity {

    private static final String TITLE = "Registro de Percurso";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bikeid";
    private static final String TAG_PERCURSO_ID  = "percursoid";
    private static final String TAG_PERCURSO_DATA  = "date";

    private TextView txtBike;
    private Button btnLogout;
    private Button btnNewPercurso;

    private String pid;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_percurso);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        txtBike = (TextView) findViewById(R.id.textBike);
        btnNewPercurso = (Button) findViewById(R.id.btnIniciarPercurso);

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
        txtBike.setText(bike_name);

        // Botão Continuar Registro
        btnNewPercurso.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gravarPercurso(bike_id);
            }
        });

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

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(RegistroPercursoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
        Intent intent = new Intent(RegistroPercursoActivity.this, LocationActivity.class);
        // sending id to next activity
        intent.putExtra(TAG_PERCURSO_ID, percurso_id);
        intent.putExtra(TAG_PERCURSO_DATA, data_completa);
        startActivity(intent);
        finish();
    }
}
