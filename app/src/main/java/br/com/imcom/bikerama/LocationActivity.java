package br.com.imcom.bikerama;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TITLE = "Gravar Percurso";

    // LogCat tag
    private static final String TAG = LocationActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;

    // UI elements
    private TextView tvCoordinate;

    // Location updates intervals in sec
    // Ex: - 1000 * 60 * 1; //1 minute -- 10000; // 10 sec
    private static int UPDATE_INTERVAL = 15000; // 15 sec
    private static int FATEST_INTERVAL = 10000; // 10 sec
    private static int DISPLACEMENT = 25; // 25 meters

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKE    = "bike";
    private static final String TAG_BIKEID  = "bikeid";
    private static final String TAG_PERCURSO_ID  = "percursoid";
    private static final String TAG_PERCURSO_DATA  = "date";

    private TextView txtBike;
    private TextView txtPausado;
    private Button btnEndPercurso;
    private Button btnSavePercurso;

    private SQLiteHandler db;
    private SessionManager session;

    Chronometer m_chronometer;
    boolean isClickPause = false;
    long tempoQuandoParado = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        LocationActivity.this.setTitle(TITLE);

        txtBike = (TextView) findViewById(R.id.textBike);
        txtPausado = (TextView) findViewById(R.id.textViewGravando);
        btnEndPercurso = (Button) findViewById(R.id.btnPararPercurso);
        btnSavePercurso = (Button) findViewById(R.id.btnConcluirPercurso);
        m_chronometer = (Chronometer) findViewById(R.id.chronometer);

        m_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {
                int cTextSize = c.getText().length();
                if (cTextSize == 5) {
                    m_chronometer.setText("00:"+c.getText().toString());
                } else if (cTextSize == 7) {
                    m_chronometer.setText("0"+c.getText().toString());
                }
            }
        });

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        final String bike_name = bike.get("name");
        final String bike_id = bike.get("uid");

        // Displaying the user details on the screen
        txtBike.setText(bike_name);

        // getting id from intent
        Intent i = getIntent();
        final String percurso_uid = i.getStringExtra(TAG_PERCURSO_ID);
        final String date_uid = i.getStringExtra(TAG_PERCURSO_DATA);
        Log.d(TAG, "Percurso UID: " + percurso_uid.toString());

        // Botão Pausar/Continuar Registro
        btnEndPercurso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });

        // Botão Salvar Percurso
        btnSavePercurso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();

                // Abre nova Activity com opções de Salvar ou Cancelar
                Intent intent = new Intent(LocationActivity.this,
                        SavePercursoActivity.class);
                // sending id to next activity
                intent.putExtra(TAG_PERCURSO_ID, percurso_uid);
                intent.putExtra(TAG_PERCURSO_DATA, date_uid);
                intent.putExtra(TAG_BIKEID, bike_id);
                intent.putExtra(TAG_BIKE, bike_name);
                startActivity(intent);
                finish();
            }
        });

        // Mostra a localização atual
        tvCoordinate = (TextView) findViewById(R.id.tv_coordinate);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            callConnection();
            createLocationRequest();

            // Inicia o cronometro
            if(isClickPause){
                m_chronometer.setBase(SystemClock.elapsedRealtime() + tempoQuandoParado);
                m_chronometer.start();
                tempoQuandoParado = 0;
                isClickPause = false;
            }
            else{
                m_chronometer.setBase(SystemClock.elapsedRealtime());
                m_chronometer.start();
                tempoQuandoParado = 0;
            }
        }
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.i("LOG", "Latitude | Longitude: " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());

            tvCoordinate.setText(latitude + ", " + longitude);


        } else {

            tvCoordinate.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to record the location on SQLite
     * */
    public void gravarLocation(String percurso_id) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            // Define Data de Hoje
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
            Date data = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            Date data_atual = cal.getTime();
            String data_completa = dateFormat.format(data_atual);

            Log.i("LOG", "GRAVAR SQLite - Latitude | Longitude: " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());

            final String latlong = mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude();

            //tvCoordinate.setText(latitude + ", " + longitude);
            // Inserting row in Dados Percurso table
            db.addDadosPercurso(percurso_id, latlong, data_completa);

        } else {

            Log.i("LOG", "GRAVAR SQLite - Latitude | Longitude: Não foi possível Salvar os dados de Localização...");
        }
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private synchronized void callConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            btnEndPercurso.setText(getString(R.string.btn_stop_location_updates));
            btnEndPercurso.getBackground().clearColorFilter();

            // Changing the text
            txtPausado.setText(getString(R.string.percurso_gravando));

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            // Inicia o cronometro
            if(isClickPause == true){
                m_chronometer.setBase(SystemClock.elapsedRealtime() + tempoQuandoParado);
                m_chronometer.start();
                tempoQuandoParado = 0;
                isClickPause = false;
            }

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            btnEndPercurso.setText(getString(R.string.btn_start_location_updates));
            btnEndPercurso.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

            // Changing the text
            txtPausado.setText(getString(R.string.percurso_pausado));

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            if(isClickPause == false){ //entra para false;
                tempoQuandoParado = m_chronometer.getBase() - SystemClock.elapsedRealtime();
            }
            m_chronometer.stop();
            isClickPause = true;

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    // LISTENER
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected(" + bundle + ")");

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        //Location local_atual = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        /*if(local_atual != null){
            Log.i("LOG", "Latitude: "+ local_atual.getLatitude());
            Log.i("LOG", "Longitude: "+ local_atual.getLongitude());
            tvCoordinate.setText(local_atual.getLatitude() +" | "+ local_atual.getLongitude());
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed(" + connectionResult + ")");
    }

    // ATUALIZACAO DE LOCAL
    @Override
    public void onLocationChanged(Location location) {
        // getting id from intent
        Intent i = getIntent();
        final String percurso_id = i.getStringExtra(TAG_PERCURSO_ID);
        Log.d(TAG, "Percurso ID: " + percurso_id.toString());

        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();

        // Gravar the new location on SQLite
        gravarLocation(percurso_id);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(LocationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
