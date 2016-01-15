package br.com.imcom.bikerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class InicialActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bikeid";

    private TextView txtBike;
    private Button btnLogout;
    private Button btnNewPercurso;
    private Button btnHistorico;

    private String pid;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        txtBike = (TextView) findViewById(R.id.textBike);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnNewPercurso = (Button) findViewById(R.id.btnNovoPercurso);
        btnHistorico = (Button) findViewById(R.id.btnHistorico);

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


    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(InicialActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Continua o Registro e Sai da Tela de Boas Vindas
     * */
    private void gravarPercurso() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, RegistroPercursoActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Carrega o Historico de Atividades
     * */
    private void carregaHistorico() {

        // Launching the RegistroPercurso activity
        Intent intent = new Intent(InicialActivity.this, HistoricoPercursosActivity.class);
        startActivity(intent);
        finish();
    }
}
