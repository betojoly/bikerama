package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;


public class SplashActivity extends AppCompatActivity {

    private static int TEMPO_SPLASH = 3000; //5000 milissegundos, ou 5 segundos
    private static final String LOG_TAG = SplashActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private String pid;

    private SQLiteHandler db;
    private SessionManager session;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        final String email = user.get("email");

        pid = user.get("email");


        // Temporizador mantém carregando os dados
        new Handler().postDelayed(new Runnable() {
           // Carrega a imagem

            @Override
            public void run() {
                // Este método executa por 5 segundos antes de abrir a Main2Activity
                /*Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                // Fecha a Activity atual
                finish();*/

                // Verificar se já foi preenchido cadastro secundário, se sim, não exibe botão Continuar Cadastro
                // e redireciona para Tela Inicial
                if(email != null){
                    // Verifica se tem Conexao com a internet
                    if (detectaConexao.existeConexao()) {

                        //new VerificaUser().execute();
                        VerificaUsuario();
                    }
                    else {
                        mostraAlerta();
                    }
                }
            }
        }, TEMPO_SPLASH);
    }

    // Verifica se Usuario completou o Cadastro
    public void VerificaUsuario() {

        // Busca Registro no Database SQLite
        HashMap<String, String> Cadastro = db.getCadastroDetails();

        // Retira os dados atraves da Key
        String Status = Cadastro.get("status");
        Log.d("LOG", "CADASTRO (DB): " + Status);

        if (Status != null) {
                //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();
                // Verifica se tem Bike Cadastrada
                VerificaBikeSQLite();

        } else {
                new VerificaUser().execute();
                // Grava status de cadastro completo no SQLite
                /*db.addCadastro("1");
                // Envia para Tela de Continuar o Registro
                Intent intent = new Intent(SplashActivity.this, Register3Activity.class);
                startActivity(intent);
                finish();*/
        }
    }

    // Verifica se Usuario completou o Cadastro
    private class VerificaUser extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SplashActivity.this);
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
                        //Toast.makeText(getApplicationContext(), "Cadastro OK!", Toast.LENGTH_LONG).show();
                        db.addCadastro("1");
                        // Verifica se tem Bike Cadastrada
                        VerificaBikeSQLite();

                    } else if (success == 0) {
                        // Envia para Tela de Continuar o Registro
                        Intent intent = new Intent(SplashActivity.this, Register3Activity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // Verifica Bike no SQLite
    private void VerificaBikeSQLite() {

        // Fetching bike details from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        String bike_name = bike.get("name");
        String bike_id = bike.get("uid");

        if (bike_id != null) {
            // Iniciar Activity Tela Inicial
            Intent intent = new Intent(
                    SplashActivity.this,
                    Main2Activity.class);
            //InicialActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Verifica se tem Bike Cadastrada, se não tiver verifica e cadastra
            new VerificaBike().execute();
        }
    }


    // Verifica se Usuario cadastrou Bike
    private class VerificaBike extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SplashActivity.this);
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
                                SplashActivity.this,
                                Main2Activity.class);
                        //InicialActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (success == 0) {
                        // Envia para Tela Inicial
                        Intent intent = new Intent(
                                SplashActivity.this,
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
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Mostra a informação caso não tenha internet.
     * */
    private void mostraAlerta() {
        android.support.v7.app.AlertDialog.Builder informa = new android.support.v7.app.AlertDialog.Builder(SplashActivity.this);
        informa.setTitle("Sem conexão com a internet.");
        informa.setNeutralButton("Voltar", null).show();
    }
}
