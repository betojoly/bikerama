package br.com.imcom.bikerama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class ViewRelatoriosActivity extends AppCompatActivity {

    private static final String TITLE = "Relatórios";
    private static final String LOG_TAG = ViewAcessoriosActivity.class.getSimpleName();

    private SQLiteHandler db;
    private SessionManager session;

    // Detecta Conexao com internet
    private DetectaConexao detectaConexao;

    private String bike_id;
    private String bike_name;
    private TextView txtNenhumResultado;

    // Comps JSONArray
    JSONArray comps = null;

    ArrayList<HashMap<String, String>> compsList;

    ListView listViewRelatorios;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PID     = "pid";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bike_id";
    private static final String KEY_EMAIL   = "email_user";
    private static final String KEY_RELATORIO_ID = "id";
    private static final String KEY_RELATORIO_MANUTENCAO_ID = "manutencao_id";
    private static final String KEY_RELATORIO_MANUAL_ID = "manual_id";
    private static final String KEY_RELATORIO_DATA = "data_criacao";
    private static final String KEY_RELATORIO_STATUS = "status";
    private static final String KEY_RELATORIO_VERIFICACAO = "verificacao";
    private static final String KEY_RELATORIO_CONJUNTO = "conjunto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_relatorios);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        // Hashmap for ListView
        compsList = new ArrayList<HashMap<String, String>>();

        // Get ListView object from xml
        listViewRelatorios = (ListView) findViewById(R.id.listViewRelatorios);

        // Detecta conexao instancia
        detectaConexao = new DetectaConexao(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Relatorio item : db.getAllRelatorios()) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put(KEY_RELATORIO_ID, String.valueOf(item.getId()));
            datum.put(KEY_RELATORIO_DATA, item.getData_criacao());
            datum.put(KEY_RELATORIO_CONJUNTO, item.getConjunto());
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.listview_item_relatorios,
                new String[] {KEY_RELATORIO_ID, KEY_RELATORIO_DATA, KEY_RELATORIO_CONJUNTO},
                new int[] {R.id.id,
                           R.id.data_criacao,
                           R.id.conjunto});

        listViewRelatorios.setAdapter(adapter);


        // on seleting single
        // launching Screen
        listViewRelatorios.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.id)).getText().toString();
                Log.d(LOG_TAG, "Relatorio ID (Selecionado): " + pid.toString());

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        DetalhesRelatorioActivity.class);
                // sending pid to next activity
                in.putExtra(KEY_RELATORIO_ID, pid);
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ViewRelatoriosActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
