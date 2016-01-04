package br.com.imcom.bikerama;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnRegisterContinue;

    private String pid;

    private SQLiteHandler db;
    private SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnRegisterContinue = (Button) findViewById(R.id.btnContinueRegister);

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
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Verificar se já foi preenchido cadastro secundário, se sim, não exibe botão Continuar Cadastro
        // e redireciona para Tela Inicial
        pid = user.get("email");

        if(pid != null){
            new VerificaUser().execute();
        }
    }

    // Verifica se Usuario completou o Cadastro
    private class VerificaUser extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
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
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CONSULTA, "POST", params);
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

                    if (success == 1) {
                        // successfully received Data Cadastro
                        // Iniciar Activity Inicial
                        //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();

                        // Verifica se tem Bike Cadastrada
                        new VerificaBike().execute();

                    } else if (success == 0) {
                        // Permanecer nesta Tela
                        Toast.makeText(getApplicationContext(), "Complete seu Cadastro!", Toast.LENGTH_LONG).show();

                        // Logout button click event
                        btnLogout.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                logoutUser();
                            }
                        });

                        // Botão Continuar Registro
                        btnRegisterContinue.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                continueRegister();
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // Verifica se Usuario cadastrou Bike
    private class VerificaBike extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
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
                    String bike_id = json.getString("bike_id");
                    String bike = json.getString("bike");

                    if (success == 1) {
                        // successfully received Data Cadastro
                        //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();

                        // Verificar se passou dados da Bike cadastrada, se sim, gravar do SQLite
                        if(bike_id != null){
                            // Inserting row in users table
                            db.addBike(bike, bike_id);
                        }

                        // Iniciar Activity Tela Inicial
                        Intent intent = new Intent(
                                MainActivity.this,
                                InicialActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (success == 0) {
                        // Permanecer nesta Tela
                        Toast.makeText(getApplicationContext(), "Cadastre sua Bike!", Toast.LENGTH_LONG).show();

                        // Envia para Tela Inicial
                        // Launch Cadastro Bike Activity
                        Intent intent = new Intent(
                                MainActivity.this,
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


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Continua o Registro e Sai da Tela de Boas Vindas
     * */
    private void continueRegister() {

        // Launching the Register2 activity
        Intent intent = new Intent(MainActivity.this, Register3Activity.class);
        startActivity(intent);
        finish();
    }
}
