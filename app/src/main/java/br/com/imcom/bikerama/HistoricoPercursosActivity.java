package br.com.imcom.bikerama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class HistoricoPercursosActivity extends AppCompatActivity {

    private static final String TITLE = "Histórico Percursos";
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

    ListView listViewPercursos;
    //ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_percursos);

        HistoricoPercursosActivity.this.setTitle(TITLE);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Get ListView object from xml
        listViewPercursos = (ListView) findViewById(R.id.listViewPercursos);

        /*
        // *** Para um único Registro, funciona *** //
        ArrayList<String> data = db.getDataPercursos();

        ListAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, data);
        listViewPercursos.setAdapter(adapter);
        // *** Para um único Registro, funciona *** //
        */

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (Percurso item : db.getAllPercursos()) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put(KEY_PERCURSO_ID, String.valueOf(item.getId()));
            datum.put(KEY_PERCURSO_NOME, item.getNome());
            datum.put(KEY_PERCURSO_DATA, item.getDate().toString());
            data.add(datum);
        }
        /*SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {KEY_PERCURSO_NOME, KEY_PERCURSO_DATA},
                new int[] {android.R.id.text1,
                        android.R.id.text2});*/

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.listview_item_percursos,
                new String[] {KEY_PERCURSO_ID, KEY_PERCURSO_NOME, KEY_PERCURSO_DATA},
                new int[] {R.id.id,
                           R.id.nome,
                           R.id.date});

        listViewPercursos.setAdapter(adapter);


        // on seleting single
        // launching Screen
        listViewPercursos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.id)).getText().toString();
                Log.d(LOG_TAG, "Percurso ID (Selecionado): " + pid.toString());

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), DetalhesPercursoActivity.class);
                // sending pid to next activity
                in.putExtra(KEY_PERCURSO_ID, pid);
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
        Intent intent = new Intent(HistoricoPercursosActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
