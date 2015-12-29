package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterComponentesActivity extends AppCompatActivity {

    private static final String TITLE = "Cadastro Componentes Bike";
    private static final String TAG = RegisterBikeActivity.class.getSimpleName();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PID     = "pid";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bikeid";

    private BackGroundTask bgt;

    private Button btnSubmit;

    private ProgressDialog pDialog;

    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_componentes);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();
        final String user_name = user.get("name");
        final String user_email = user.get("email");

        RegisterComponentesActivity.this.setTitle(TITLE);

        // getting product details from intent
        Intent i = getIntent();
        // getting product id (pid) from intent
        final String bike_id = i.getStringExtra(TAG_BIKEID);
        //Log.d(TAG, "Bike ID: " + bike_id.toString());

        // Controle
        final EditText nomeGuidao = (EditText) findViewById(R.id.inputGuidao);
        nomeGuidao.setHint("Guidão");

        final EditText nomeManetefreio = (EditText) findViewById(R.id.inputManetefreio);
        nomeManetefreio.setHint("Manete de Freio (Marca/Modelo/Tipo)");

        final EditText nomeDirecao = (EditText) findViewById(R.id.inputDirecao);
        nomeDirecao.setHint("Movimento de direção");

        final EditText nomePassador = (EditText) findViewById(R.id.inputPassador);
        nomePassador.setHint("Passador de marcha (Marca/Modelo)");

        final EditText nomeMesa = (EditText) findViewById(R.id.inputMesa);
        nomeMesa.setHint("Suporte de guidão (Mesa)");

        // Transmissao
        final EditText nomePedivela = (EditText) findViewById(R.id.inputPedivela);
        nomePedivela.setHint("Pedivela (Marca/Modelo)");

        final EditText nomeCoroaMaior = (EditText) findViewById(R.id.inputCoroaMaior);
        nomeCoroaMaior.setHint("Coroa Maior (Marca/Qtde Dentes)");

        final EditText nomeCoroaIntermediaria = (EditText) findViewById(R.id.inputCoroaIntermediaria);
        nomeCoroaIntermediaria.setHint("Coroa Intermediária (Marca/Qtde Dentes)");

        final EditText nomeCoroaMenor = (EditText) findViewById(R.id.inputCoroaMenor);
        nomeCoroaMenor.setHint("Coroa Menor (Marca/Qtde Dentes)");

        final EditText nomeCorrente = (EditText) findViewById(R.id.inputCorrente);
        nomeCorrente.setHint("Corrente (Marca/Modelo)");

        final EditText nomeCassete = (EditText) findViewById(R.id.inputCassete);
        nomeCassete.setHint("Cassete (Marca/Modelo)");

        final EditText nomeCentral = (EditText) findViewById(R.id.inputCentral);
        nomeCentral.setHint("Movimento Central (Marca/Modelo)");

        final EditText nomePedal = (EditText) findViewById(R.id.inputPedal);
        nomePedal.setHint("Pedal (Marca/Modelo)");

        final EditText nomeCambioDianteiro = (EditText) findViewById(R.id.inputCambioDianteiro);
        nomeCambioDianteiro.setHint("CâmbioDianteiro (Marca/Modelo)");

        final EditText nomeCambioTraseiro = (EditText) findViewById(R.id.inputCambioTraseiro);
        nomeCambioTraseiro.setHint("Câmbio Traseiro (Marca/Modelo)");

        // Frente
        final EditText nomeGarfo = (EditText) findViewById(R.id.inputGarfo);
        nomeGarfo.setHint("Garfo (Marca/Modelo)");

        final EditText nomeAroFrente = (EditText) findViewById(R.id.inputAroFrente);
        nomeAroFrente.setHint("Aro Dianteiro (Marca/Modelo)");

        final EditText nomeCuboFrente = (EditText) findViewById(R.id.inputCuboFrente);
        nomeCuboFrente.setHint("Cubo Dianteiro (Marca/Modelo)");

        final EditText nomePneuFrente = (EditText) findViewById(R.id.inputPneuFrente);
        nomePneuFrente.setHint("Pneu Dianteiro (Marca/Modelo)");

        final EditText nomeFreioFrente = (EditText) findViewById(R.id.inputFreioFrente);
        nomeFreioFrente.setHint("Freio Dianteiro (Marca/Modelo)");

        // Traseira
        final EditText nomeShock = (EditText) findViewById(R.id.inputShock);
        nomeShock.setHint("Suspensão Traseira (Marca/Modelo)");

        final EditText nomeAroTraseiro = (EditText) findViewById(R.id.inputAroTraseiro);
        nomeAroTraseiro.setHint("Aro Traseiro (Marca/Modelo)");

        final EditText nomeCuboTraseiro = (EditText) findViewById(R.id.inputCuboTraseiro);
        nomeCuboTraseiro.setHint("Cubo Traseiro (Marca/Modelo)");

        final EditText nomePneuTraseiro = (EditText) findViewById(R.id.inputPneuTraseiro);
        nomePneuTraseiro.setHint("Pneu Traseiro (Marca/Modelo)");

        final EditText nomeFreioTraseiro = (EditText) findViewById(R.id.inputFreioTraseiro);
        nomeFreioTraseiro.setHint("Freio Traseiro (Marca/Modelo)");


        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        // Register Button Click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Controle
                String var_Guidao = nomeGuidao.getText().toString().trim();
                String var_Manete = nomeManetefreio.getText().toString().trim();
                String var_Direcao = nomeDirecao.getText().toString().trim();
                String var_Passador = nomePassador.getText().toString().trim();
                String var_Mesa = nomeMesa.getText().toString().trim();
                // Transmissao
                String var_Pedivela = nomePedivela.getText().toString().trim();
                String var_CoroaMaior = nomeCoroaMaior.getText().toString().trim();
                String var_CoroaInter = nomeCoroaIntermediaria.getText().toString().trim();
                String var_CoroaMenor = nomeCoroaMenor.getText().toString().trim();
                String var_Corrente = nomeCorrente.getText().toString().trim();
                String var_Cassete = nomeCassete.getText().toString().trim();
                String var_Central = nomeCentral.getText().toString().trim();
                String var_Pedal = nomePedal.getText().toString().trim();
                String var_CambioD = nomeCambioDianteiro.getText().toString().trim();
                String var_CambioT = nomeCambioTraseiro.getText().toString().trim();
                // Frente
                String var_Garfo = nomeGarfo.getText().toString().trim();
                String var_AroDia = nomeAroFrente.getText().toString().trim();
                String var_CuboDia = nomeCuboFrente.getText().toString().trim();
                String var_PneuDia = nomePneuFrente.getText().toString().trim();
                String var_FreioDia = nomeFreioFrente.getText().toString().trim();
                // Traseira
                String var_Shock = nomeShock.getText().toString().trim();
                String var_AroTra = nomeAroTraseiro.getText().toString().trim();
                String var_CuboTra = nomeCuboTraseiro.getText().toString().trim();
                String var_PneuTra = nomePneuTraseiro.getText().toString().trim();
                String var_FreioTra = nomeFreioTraseiro.getText().toString().trim();

                if (!var_Pedivela.isEmpty()) {
                    // Salvar
                    SaveComponentes(var_Guidao, var_Manete, var_Direcao, var_Passador, var_Mesa, var_Pedivela,
                            var_CoroaMaior, var_CoroaInter, var_CoroaMenor, var_Corrente, var_Cassete, var_Central,
                            var_Pedal, var_CambioD, var_CambioT, var_Garfo, var_AroDia, var_CuboDia, var_PneuDia,
                            var_FreioDia, var_Shock, var_AroTra, var_CuboTra, var_PneuTra, var_FreioTra, user_email, bike_id);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Por favor preencha o campo Pedivela! (Obrigatório)", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    // Salva os dados de componentes
    private void SaveComponentes(final String var_Guidao, final String var_Manete, final String var_Direcao, final String var_Passador, final String var_Mesa,
                                 final String var_Pedivela, final String var_CoroaMaior, final String var_CoroaInter, final String var_CoroaMenor,
                                 final String var_Corrente, final String var_Cassete, final String var_Central, final String var_Pedal, final String var_CambioD,
                                 final String var_CambioT, final String var_Garfo, final String var_AroDia, final String var_CuboDia, final String var_PneuDia,
                                 final String var_FreioDia, final String var_Shock, final String var_AroTra, final String var_CuboTra, final String var_PneuTra,
                                 final String var_FreioTra, final String user_email, final String bike_id) {

        // Tag used to cancel the request
        String tag_string_req = "req_register2";

        pDialog.setMessage("Salvando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SAVE_COMP_BIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Save Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String pid = jObj.getString("pid");
                    String bike = jObj.getString("bike");
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

                        // Verificar se passou dados da Bike cadastrada, se sim, gravar do SQLite
                        if(pid != null){
                            // Inserting row in users table
                            //db.addBike(bike, pid);
                        }

                        // Launch Cadastro Bike Activity
                        Intent intent = new Intent(
                                RegisterComponentesActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Save Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("guidao", String.valueOf(var_Guidao));
                params.put("manete", String.valueOf(var_Manete));
                params.put("direcao", String.valueOf(var_Direcao));
                params.put("passador", String.valueOf(var_Passador));
                params.put("mesa", String.valueOf(var_Mesa));
                params.put("pedivela", String.valueOf(var_Pedivela));
                params.put("coroa_maior", String.valueOf(var_CoroaMaior));
                params.put("coroa_inter", String.valueOf(var_CoroaInter));
                params.put("coroa_menor", String.valueOf(var_CoroaInter));
                params.put("corrente", String.valueOf(var_CoroaMenor));
                params.put("cassete", String.valueOf(var_Cassete));
                params.put("central", String.valueOf(var_Central));
                params.put("pedal", String.valueOf(var_Pedal));
                params.put("cambio_diant", String.valueOf(var_CambioD));
                params.put("cambio_tras", String.valueOf(var_CambioT));
                params.put("garfo", String.valueOf(var_Garfo));
                params.put("aro_diant", String.valueOf(var_AroDia));
                params.put("cubo_diant", String.valueOf(var_CuboDia));
                params.put("pneu_diant", String.valueOf(var_PneuDia));
                params.put("freio_diant", String.valueOf(var_FreioDia));
                params.put("shock", String.valueOf(var_Shock));
                params.put("aro_tras", String.valueOf(var_AroTra));
                params.put("cubo_tras", String.valueOf(var_CuboTra));
                params.put("pneu_tras", String.valueOf(var_PneuTra));
                params.put("freio_tras", String.valueOf(var_FreioTra));
                params.put("email_user", String.valueOf(user_email));
                params.put("bike_id", String.valueOf(bike_id));
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
