package br.com.imcom.bikerama;

import android.app.ActionBar;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

public class HistoricoPercursosActivity extends AppCompatActivity {

    private static final String TITLE = "Histórico Percursos";
    private static final String LOG_TAG = HistoricoPercursosActivity.class.getSimpleName();

    private static final String TAG_PERCURSO_ID  = "percursoid";
    private static final String TAG_PERCURSO_DATA  = "date";
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
        setContentView(R.layout.historico_percursos);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

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
        final String bike_id = bike.get("uid");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                final String finalBike_id = bike_id;

                Toast.makeText(HistoricoPercursosActivity.this,
                        "Iniciar Percurso!", Toast.LENGTH_LONG).show();
                gravarPercurso(finalBike_id);
            }
        });

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
                Intent in = new Intent(getApplicationContext(),
                        DetalhesPercursoActivity.class);
                // sending pid to next activity
                in.putExtra(KEY_PERCURSO_ID, pid);
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
    }


    /**
     * Launching new activity
     */
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
        //Intent intent = new Intent(Main2Activity.this, LocationActivity.class);
        Intent intent = new Intent(HistoricoPercursosActivity.this, GravaPercursoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // sending id to next activity
        intent.putExtra(TAG_PERCURSO_ID, percurso_id);
        intent.putExtra(TAG_PERCURSO_DATA, data_completa);
        startActivity(intent);
        //finish();
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
