package br.com.imcom.bikerama;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class GravaPercursoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TITLE = "Gravar Percurso";

    // LogCat tag
    private static final String TAG = GravaPercursoActivity.class.getSimpleName();

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
    private static final String TAG_BIKE = "bike";
    private static final String TAG_BIKEID = "bikeid";
    private static final String TAG_PERCURSO_ID = "percursoid";
    private static final String TAG_PERCURSO_DATA = "date";
    private static final String TAG_PERCURSO_DIST = "distancia";

    private TextView txtBike;
    private TextView txtPausado;
    private TextView txtDistancia;
    private TextView txtVelocidade;
    private Button btnEndPercurso;
    private Button btnSavePercurso;

    private SQLiteHandler db;
    private SessionManager session;

    Chronometer m_chronometer;
    boolean isClickPause = false;
    long tempoQuandoParado = 0;

    //*****************************
    private static final String TRACKID = "trackid";
    private static final String IN_TRACKID = "in_trackid";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String SPEED = "speed";
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String DISTANCE = "distance";
    private static final String LASTKNOWNLATITUDE = "last_latitude";
    private static final String LASTKNOWNLONGITUDE = "last_longitude";
    private static final String DURATION = "duracao";
    //*****************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grava_percurso);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TITLE);
        getSupportActionBar().setIcon(R.drawable.actionbar_space_between_icon_and_title); // or setLogo()

        txtBike = (TextView) findViewById(R.id.textBike);
        txtPausado = (TextView) findViewById(R.id.textViewGravando);
        txtDistancia = (TextView) findViewById(R.id.textDistancia);
        txtVelocidade = (TextView) findViewById(R.id.textVelocidade);

        btnEndPercurso = (Button) findViewById(R.id.btnPararPercurso);
        btnSavePercurso = (Button) findViewById(R.id.btnConcluirPercurso);
        m_chronometer = (Chronometer) findViewById(R.id.chronometer);

        m_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {
                int cTextSize = c.getText().length();
                if (cTextSize == 5) {
                    m_chronometer.setText("00:" + c.getText().toString());
                } else if (cTextSize == 7) {
                    m_chronometer.setText("0" + c.getText().toString());
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

        // Fetching Bike details from sqlite
        HashMap<String, String> bike = db.getBikeDetails();

        final String bike_name = bike.get("name");
        final String bike_id = bike.get("uid");
        final String kilometragem_bike = bike.get("kilometragem");

        // Displaying the user details on the screen
        txtBike.setText(bike_name);

        if(kilometragem_bike != null){
            // Arredondar Kilometragem Total
            double roundedKilometragem = Double.parseDouble(kilometragem_bike);
            roundedKilometragem = (double) Math.round(roundedKilometragem * 100) / 100;

            txtVelocidade.setText(String.valueOf(roundedKilometragem) + " Km");
        }
        else{
            txtVelocidade.setText("0.0 Km");
        }

        // getting id from intent
        final String percurso_uid;
        final String date_uid;

        Intent i = getIntent();
        percurso_uid = i.getStringExtra(TAG_PERCURSO_ID);
        date_uid = i.getStringExtra(TAG_PERCURSO_DATA);

        if(percurso_uid != null){
            Log.d(TAG, "Percurso UID: " + percurso_uid.toString());
        }else{

        }


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
                Intent intent = new Intent(GravaPercursoActivity.this,
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
            if (isClickPause) {
                m_chronometer.setBase(SystemClock.elapsedRealtime() + tempoQuandoParado);
                m_chronometer.start();
                tempoQuandoParado = 0;
                isClickPause = false;
            } else {
                m_chronometer.setBase(SystemClock.elapsedRealtime());
                m_chronometer.start();
                tempoQuandoParado = 0;
            }
        }
    }

    /**
     * Method to display the location on UI
     * */
    public void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();



            Log.i("LOG", "Latitude | Longitude: " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());

            tvCoordinate.setText(latitude + ", \n" + longitude);
        } else {
            //tvCoordinate.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            tvCoordinate.setText("(GPS desativado. Inicie seu GPS.)");
        }
    }

    /**
     * Method to display the location on UI
     *
     * @param percurso_id*/
    private void displayDistanceSpeed(String percurso_id) {
        //
    }

    /**
     * Method to calculate Distance
     * */
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }

    /**
     * Method to record the location on SQLite
     * */
    public void gravarLocation(String percurso_id) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

            /*
            *******************************************************************************************
            * Teste Velocidade Real
             */
            String latitude_anterior;
            String longitude_anterior;
            double latitude_atual;
            double longitude_atual;
            double distance = 0;
            double distanceKm = 0;
            String distanceSaved;
            double distanceDB = 0;
            double speedInKm = 0;
            double distanceBetweenTwoPoint = 0;
            //float distanceBetweenTwoPoint = 0;

            latitude_atual = latitude;
            longitude_atual = longitude;

            // Busca Kilometragem Total no DB SQLite
            // Fetching Bike details from sqlite
            HashMap<String, String> bike = db.getBikeDetails();
            final String kilometragem_bike = bike.get("kilometragem");

            /*
            *******************************************************************************************
            */
            // Busca no Banco de Dados: Lat | Lng do último ponto.
            HashMap<String, String> LastLatLng = db.getLastLatLong(percurso_id);

            // Busca o último valor de distância atualizado para o Percurso Atual
            HashMap<String, String> DistanciaTotal = db.getLastDist(percurso_id);

            if (LastLatLng != null) {
                // Retira os dados atraves da Key
                String LatLng = LastLatLng.get("latlong");
                //Log.d("LOG", "LATLNG (DB): " + LatLng);

                if (DistanciaTotal != null) {
                    distanceSaved = DistanciaTotal.get("distancia");

                    if(distanceSaved != null){
                        distanceDB = Double.parseDouble(distanceSaved);
                    }
                }

                if (LatLng != null) {
                    // Separa a String em Lat , Lng
                    String[] items1 = LatLng.split(",");
                    latitude_anterior = items1[0];
                    longitude_anterior = items1[1];

                    Location oldLocation = new Location("oldlocation");
                    oldLocation.setLatitude(Double.parseDouble(latitude_anterior));
                    oldLocation.setLongitude(Double.parseDouble(longitude_anterior));

                    Location newLocation = new Location("newlocation");
                    newLocation.setLatitude(latitude_atual);
                    newLocation.setLongitude(longitude_atual);

                    //distanceBetweenTwoPoint = crntLocation.distanceTo(newLocation);  in meters
                    //distanceBetweenTwoPoint = crntLocation.distanceTo(newLocation) / 1000; // in km

                    double distanceMeters = oldLocation.distanceTo(newLocation);
                    distanceKm = distanceMeters / 1000f;

                    // Obter distancia usando calculo Math da funcao
                    //distanceBetweenTwoPoint = distFrom(Double.parseDouble(latitude_anterior), Double.parseDouble(longitude_anterior), latitude_atual, longitude_atual);
                    // Arredondando valor da distancia percorrida
                    //double roundedDistance = (double) Math.round(distanceBetweenTwoPoint * 100) / 100;

                    // VELOCIDADE
                    if (mLastLocation.hasSpeed()) {
                        speedInKm = mLastLocation.getSpeed() * 3.6;
                        //speedInKm = mLastLocation.getSpeed();
                    } else {
                        speedInKm = 0.0;
                    }

                    // Mostra Distância Total
                    distance = distanceDB + distanceKm;

                    // Inserting row in Dados Percurso table
                    db.updateDistancePercurso(percurso_id, distance);

                    Log.d("LOG", "LAT | LNG (ANTIGA): " + latitude_anterior + " , " + longitude_anterior);
                    Log.d("LOG", "LAT | LNG (CURRENT): " + latitude_atual + " , " + longitude_atual);
                    Log.d("LOG", "DISTANCIA ANTERIOR (DB): " + distanceDB);
                    Log.d("LOG", "DISTANCIA ATUAL: " + distanceKm);
                    Log.d("LOG", "DISTANCIA SOMA: " + distanceDB + " + " + distanceKm + " = " + distance);
                    //Log.d("LOG", "VELOCIDADE: " + speedInKm);

                    // Converte e arredonda o valor de Kilometragem Total do MySQL
                    double KilometragemBike = Double.parseDouble(kilometragem_bike);

                    // Configura a Kilometragem Total para atualizar durante o percurso
                    double KilometragemTotal = KilometragemBike + distance;

                    // Arredondar Kilometragem Total
                    double roundedKilometragem = (double) Math.round(KilometragemTotal * 100) / 100;

                    // Mostrar no Display os resultados
                    txtDistancia.setText(String.format("%.2f Km", distance));
                    //txtDistancia.setText(String.valueOf(roundedDistance));
                    txtVelocidade.setText(String.valueOf(roundedKilometragem) + " Km");
                } else {
                    txtDistancia.setText("Aguarde...");
                }
            }
            /*
            *******************************************************************************************
             */


            Log.i("LOG", "GRAVAR SQLite - Latitude | Longitude: " + latitude + ", " + longitude);

            final String latlong = latitude + ", " + longitude;

            //tvCoordinate.setText(latitude + ", " + longitude);
            // Inserting row in Dados Percurso table
            db.addDadosPercurso(percurso_id, latlong, data_completa, distanceKm);

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
            if (isClickPause == true) {
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

            if (isClickPause == false) { //entra para false;
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

        Toast.makeText(getApplicationContext(), "GPS atualizado!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayDistanceSpeed(percurso_id);

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
        Intent intent = new Intent(GravaPercursoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
