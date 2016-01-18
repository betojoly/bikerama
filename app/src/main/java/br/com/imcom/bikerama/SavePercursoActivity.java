package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class SavePercursoActivity extends AppCompatActivity {

    private static final String TITLE = "Salvar Percurso";
    private static final String TAG = SavePercursoActivity.class.getSimpleName();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bikeid";
    private static final String TAG_PERCURSO_ID  = "percursoid";
    private static final String TAG_PERCURSO_DATA  = "date";
    private static final String TAG_TIPOS   = "tipos";
    // Dados Percurso Table Columns names
    private static final String KEY_DADOS_PERCURSO_ID = "id";
    private static final String KEY_DADOS_PERCURSO_UID = "uid";
    private static final String KEY_DADOS_PERCURSO_LATLONG = "latlong";
    private static final String KEY_DADOS_PERCURSO_DATA = "date";
    private static final String KEY_DADOS_PERCURSO_STATUS = "status";
    private static final String TAG_DADOS_PERCURSOS = "dadospercursos";

    private TextView txtBike;
    private Button btnCancelPercurso;
    private Button btnSavePercurso;
    private RadioGroup radioGroupNivel;
    private RadioButton radioButtonNivel;

    // Fields
    Spinner tipoAtividadeField;

    private SQLiteHandler db;
    private SessionManager session;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParserNova jsonParser = new JSONParserNova();

    // locais JSONArray
    JSONArray dadospercursos = null;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_percurso);

        SavePercursoActivity.this.setTitle(TITLE);

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //........................................................................................//
        //executa acao de exportar Dados de Percurso
        // Verifica se tem Conexao com a internet
        if (detectaConexao.existeConexao()) {

            //Display Sync status of SQLite DB Dados Percurso
            Toast.makeText(SavePercursoActivity.this,
                    db.getSyncStatusDadosPercurso(),
                    Toast.LENGTH_LONG).show();

            //Sync SQLite DB data to remote MySQL DB - // Get Percursos
            ArrayList<HashMap<String, String>> userListDados =  db.getallPercursosDBDadosPercurso();
            if(userListDados.size()!=0){
                if(db.dbSyncCountDadosPercurso() != 0){
                    String paramsDados = db.composeJSONfromSQLiteDadosPercurso();
                    //Log.d("Create Response GSON", paramsDados);
                    new syncSQLiteMySQLDBDadosPercurso().execute(paramsDados);
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

        //........................................................................................//

        // Adiciona os botoes radio
        radioGroupNivel = (RadioGroup) findViewById(R.id.radioNivel);

        // Define Data de Hoje
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
        Date data = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();
        final String data_completa = dateFormat.format(data_atual);

        final EditText nomePercurso = (EditText) findViewById(R.id.inputNome);
        nomePercurso.setHint(data_completa);

        final EditText descricaoPercurso = (EditText) findViewById(R.id.inputDescricao);
        descricaoPercurso.setHint("Descrição. Ex. Passeio de domingo...");

        // getting id from intent
        Intent i = getIntent();
        final String percurso_id = i.getStringExtra(TAG_PERCURSO_ID);
        final String percurso_date = i.getStringExtra(TAG_PERCURSO_DATA);
        final String bike_id = i.getStringExtra(TAG_BIKEID);
        final String bike_name = i.getStringExtra(TAG_BIKE);
        Log.d(TAG, "Percurso ID (Save): " + percurso_id.toString());

        // Spinner
        tipoAtividadeField = (Spinner) findViewById(R.id.spinnerTipoAtividade);
        List<String> list = new ArrayList<String>();
        list.add("Passeio Cidade");
        list.add("Pedalada Estrada");
        list.add("Treino");
        list.add("Trilha");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        tipoAtividadeField.setAdapter(dataAdapter);
        // Spinner item selection Listener
        addListenerOnSpinnerItemSelection();

        btnSavePercurso = (Button) findViewById(R.id.btnSubmit);
        btnCancelPercurso = (Button) findViewById(R.id.btnCancelar);

        btnSavePercurso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Mostra o Tipo de Atividade Selecionado ao clicar Salvar
                /*Toast.makeText(SavePercursoActivity.this,
                        "On Button Click : " +
                                "\n" + String.valueOf(tipoAtividadeField.getSelectedItem()),
                        Toast.LENGTH_LONG).show();*/

                // Mostra o radio button selecionado from radioGroup
                int selectedRadio = radioGroupNivel.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                radioButtonNivel = (RadioButton) findViewById(selectedRadio);
                /*Toast.makeText(SavePercursoActivity.this,
                        radioButtonNivel.getText(), Toast.LENGTH_SHORT).show();*/

                // Salva no Banco de Dados as informações de Percurso do Usuário
                if(percurso_id != null){
                    // Coleta as infos inseridas nos campos de EditText
                    String var_Nome = nomePercurso.getText().toString().trim();
                    String var_Descricao = descricaoPercurso.getText().toString().trim();
                    String var_Tipo = String.valueOf(tipoAtividadeField.getSelectedItem()).trim();
                    String var_Nivel = String.valueOf(radioButtonNivel.getText()).trim();
                    String var_Id = percurso_id.toString().trim();
                    String var_Bike = bike_id.toString().trim();
                    String var_Date = percurso_date.toString().trim();
                    String var_Status = "no";

                    // valida se foram preenchidos os campos
                    if (var_Nome == null || var_Nome.trim().isEmpty()) {
                        var_Nome = data_completa;
                    }
                    if (var_Descricao == null || var_Descricao.trim().isEmpty()) {
                        var_Descricao = "Não informado";
                    }

                    if(var_Nome != null && var_Descricao != null && var_Tipo != null && var_Nivel != null) {

                        Log.d(TAG, "Dados db.updatePercurso(: " + var_Nome + ", " + var_Tipo + ", " + var_Nivel + ", " + var_Descricao + ", " + var_Status + ", " + var_Id + ")");
                        //Log.d(TAG, "Dados db.addPercursoFinal(: " + var_Id + ", " + var_Bike + ", " + var_Date + ", " + var_Nome + ", " + var_Tipo + ", " + var_Nivel + ", " + var_Descricao + ")");

                        // Inserting row in users table
                        db.updatePercurso(var_Nome, var_Tipo, var_Nivel, var_Descricao, var_Status, var_Id); //String nome, String tipo, String nivel, String descricao, String id
                        //db.addPercursoFinal(var_Id, var_Bike, var_Date, var_Nome, var_Tipo, var_Nivel, var_Descricao);

                        // Se retornar Update OK abre Nova Activity, senão informa Erro
                        Intent intent = new Intent(SavePercursoActivity.this,
                                PercursosActivity.class);
                        // sending id to next activity
                        intent.putExtra(TAG_PERCURSO_ID, percurso_id);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        btnCancelPercurso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(percurso_id != null) {
                    // Cancela as alterações e apenas mantém o Histórico de navegação com a data e hora atuais como nome
                    // Coleta as infos inseridas nos campos de EditText
                    String var_Nome = nomePercurso.getText().toString().trim();
                    String var_Descricao = "Não informado";
                    String var_Tipo = "Não informado";
                    String var_Nivel = "Não informado";
                    String var_Id = percurso_id.toString().trim();
                    String var_Status = "no";

                    // Inserting row in users table
                    db.updatePercurso(var_Nome, var_Tipo, var_Nivel, var_Descricao, var_Status, var_Id); //String nome, String tipo, String nivel, String descricao, String id

                    // Se retornar Update OK abre Nova Activity, senão informa Erro
                    Intent intent = new Intent(SavePercursoActivity.this,
                            PercursosActivity.class);
                    // sending id to next activity
                    intent.putExtra(TAG_PERCURSO_ID, percurso_id);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }

    /**
     * Background Async Task to Transfer Percursos
     * */
    public class syncSQLiteMySQLDBDadosPercurso extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SavePercursoActivity.this);
            pDialog.setMessage("Transferindo dados para servidor remoto. Por favor aguarde...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating Local
         * */
        protected String doInBackground(String... args){
            String dadosGsonD = args[0];

            // Building Parameters
            Map<String, String> paramsDados = new HashMap<>();
            paramsDados.put("dados_JSON", dadosGsonD);

            // Check for success tag
            int success = 0; //initialize to zero

            // getting JSON Object
            // Note that create local url accepts POST method
            JSONObject jsonDados = jsonParser.makeHttpRequest(AppConfig.URL_EXPORTA_DADOS_PERCURSO, "POST", (HashMap<String, String>) paramsDados);

            // check log cat for response
            //added null check:
            if (jsonDados != null) {
                //Log.d("Create Response", json.toString());

                // check for success tag
                try {
                    // Getting Array of Products
                    dadospercursos = jsonDados.getJSONArray(TAG_DADOS_PERCURSOS);

                    // looping through All Products
                    for (int i = 0; i < dadospercursos.length(); i++) {
                        JSONObject c = dadospercursos.getJSONObject(i);

                        // Checking for SUCCESS TAG
                        success = c.getInt(TAG_SUCCESS);

                        if(success == 1){
                            // successfully
                            Log.d("Sucesso ", "OK");
                            db.updateSyncStatusDadosPercurso(c.getString(KEY_DADOS_PERCURSO_ID), c.getString(KEY_DADOS_PERCURSO_STATUS));

                        } else {
                            // failed
                            Log.d("Falha ", "ERRO");
                            db.updateSyncStatusDadosPercurso(c.getString(KEY_DADOS_PERCURSO_ID), c.getString(KEY_DADOS_PERCURSO_STATUS));
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


    // Add spinner data
    public void addListenerOnSpinnerItemSelection(){

        tipoAtividadeField.setOnItemSelectedListener(new TipoAtividade());
    }

    // Mostra a informação caso não tenha internet.
    private void mostraAlerta() {
        AlertDialog.Builder informa = new AlertDialog.Builder(SavePercursoActivity.this);
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
        Intent intent = new Intent(SavePercursoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
