package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.imcom.bikerama.Percurso;
import br.com.imcom.bikerama.Relatorio;

/**
 * Created by BETO on 13/12/2015.
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";
    // CadastroCompleto table name
    private static final String TABLE_CADASTRO = "cadastro_completo";
    // Bike table name
    private static final String TABLE_BIKE = "bike";
    // Bike table percurso
    private static final String TABLE_PERCURSO = "percurso";
    // Bike table percurso
    private static final String TABLE_PERCURSO_FINAL = "percurso_final";
    // Bike table dados percurso
    private static final String TABLE_DADOS_PERCURSO = "dadospercurso";
    // Bike table relatorio
    private static final String TABLE_RELATORIO = "relatorio";

    private static final String KEY_BIKE_ATUAL = "bike_id";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";
    // CadastroCompleto Table Columns names
    private static final String KEY_CADASTRO_ID = "id";
    private static final String KEY_CADASTRO_NAME = "status";
    // Bike Table Columns names
    private static final String KEY_BIKE_ID = "id";
    private static final String KEY_BIKE_NAME = "name";
    private static final String KEY_BIKE_UID = "uid";
    private static final String KEY_BIKE_KILO = "kilometragem";
    // Percurso Table Columns names
    private static final String KEY_PERCURSO_ID = "id";
    private static final String KEY_PERCURSO_UID = "uid";
    private static final String KEY_PERCURSO_BIKEID = "bikeid";
    private static final String KEY_PERCURSO_DATA = "date";
    private static final String KEY_PERCURSO_NOME = "nome";
    private static final String KEY_PERCURSO_TIPO = "tipo";
    private static final String KEY_PERCURSO_NIVEL = "nivel";
    private static final String KEY_PERCURSO_DESCRICAO = "descricao";
    private static final String KEY_PERCURSO_STATUS = "updatestatus";
    private static final String KEY_PERCURSO_DIST = "distancia";
    // Dados Percurso Table Columns names
    private static final String KEY_DADOS_PERCURSO_ID = "id";
    private static final String KEY_DADOS_PERCURSO_UID = "uid";
    private static final String KEY_DADOS_PERCURSO_LATLONG = "latlong";
    private static final String KEY_DADOS_PERCURSO_DATA = "date";
    private static final String KEY_DADOS_PERCURSO_STATUS = "updatestatus";
    private static final String KEY_DADOS_PERCURSO_DIST = "distancia";
    // Dados Relatorio Manutencao Table Columns names
    private static final String KEY_RELATORIO_ID = "id";
    private static final String KEY_RELATORIO_MANUTENCAO_ID = "manutencao_id";
    private static final String KEY_RELATORIO_MANUAL_ID = "manual_id";
    private static final String KEY_RELATORIO_DATA = "data_criacao";
    private static final String KEY_RELATORIO_STATUS = "status";
    private static final String KEY_RELATORIO_VERIFICACAO = "verificacao";
    private static final String KEY_RELATORIO_CONJUNTO = "conjunto";

    // Camps adicionais dasd Tabelas
    private static final String DATABASE_ALTER_PERCURSO_1 = "ALTER TABLE "
            + TABLE_PERCURSO + " ADD COLUMN " + KEY_PERCURSO_NOME + " TEXT;";

    private static final String DATABASE_ALTER_PERCURSO_2 = "ALTER TABLE "
            + TABLE_PERCURSO + " ADD COLUMN " + KEY_PERCURSO_TIPO + " TEXT;";

    private static final String DATABASE_ALTER_PERCURSO_3 = "ALTER TABLE "
            + TABLE_PERCURSO + " ADD COLUMN " + KEY_PERCURSO_NIVEL + " TEXT;";

    private static final String DATABASE_ALTER_PERCURSO_4 = "ALTER TABLE "
            + TABLE_PERCURSO + " ADD COLUMN " + KEY_PERCURSO_DESCRICAO + " TEXT;";

    // ......................................................
    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User
        final String CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database " + TABLE_BIKE + " tables created");


        // Create Bike
        final String CREATE_BIKE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BIKE + "("
                + KEY_BIKE_ID + " INTEGER PRIMARY KEY,"
                + KEY_BIKE_NAME + " TEXT,"
                + KEY_BIKE_UID + " TEXT,"
                + KEY_BIKE_KILO + " TEXT" + ")";
        db.execSQL(CREATE_BIKE_TABLE);

        Log.d(TAG, "Database " + TABLE_BIKE + " tables created");


        // Create Dados_Percurso
        final String CREATE_DADOS_PERCURSO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DADOS_PERCURSO + "("
                + KEY_DADOS_PERCURSO_ID + " INTEGER PRIMARY KEY,"
                + KEY_DADOS_PERCURSO_UID + " INTEGER,"
                + KEY_DADOS_PERCURSO_LATLONG + " TEXT,"
                + KEY_DADOS_PERCURSO_DATA + " TEXT,"
                + KEY_DADOS_PERCURSO_STATUS + " TEXT,"
                + KEY_DADOS_PERCURSO_DIST + " TEXT" + ")";
        db.execSQL(CREATE_DADOS_PERCURSO_TABLE);

        Log.d(TAG, "Database " + TABLE_DADOS_PERCURSO + " tables created");


        // Create Percurso
        final String CREATE_PERCURSO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PERCURSO + "("
                + KEY_PERCURSO_ID + " INTEGER PRIMARY KEY,"
                + KEY_PERCURSO_BIKEID + " TEXT,"
                + KEY_PERCURSO_DATA + " TEXT,"
                + KEY_PERCURSO_NOME + " TEXT,"
                + KEY_PERCURSO_TIPO + " TEXT,"
                + KEY_PERCURSO_NIVEL + " TEXT,"
                + KEY_PERCURSO_DESCRICAO + " TEXT,"
                + KEY_PERCURSO_STATUS + " TEXT,"
                + KEY_PERCURSO_DIST + " TEXT" + ")";
        db.execSQL(CREATE_PERCURSO_TABLE);

        Log.d(TAG, "Database " + TABLE_PERCURSO + " tables created");


        // Create Cadastro
        final String CREATE_CADASTRO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CADASTRO + "("
                + KEY_CADASTRO_ID + " INTEGER PRIMARY KEY,"
                + KEY_CADASTRO_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CADASTRO_TABLE);

        Log.d(TAG, "Database " + TABLE_CADASTRO + " tables created");


        // Create Relatorio
        final String CREATE_RELATORIO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RELATORIO + "("
                + KEY_RELATORIO_ID + " INTEGER PRIMARY KEY,"
                + KEY_RELATORIO_MANUTENCAO_ID + " INTEGER,"
                + KEY_RELATORIO_MANUAL_ID + " INTEGER,"
                + KEY_RELATORIO_DATA + " TEXT,"
                + KEY_RELATORIO_STATUS + " TEXT,"
                + KEY_RELATORIO_VERIFICACAO + " TEXT,"
                + KEY_RELATORIO_CONJUNTO + " TEXT" + ")";
        db.execSQL(CREATE_RELATORIO_TABLE);

        Log.d(TAG, "Database " + TABLE_DADOS_PERCURSO + " tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        //onCreate(db);

        /*if (oldVersion < 3) {
            // Create Cadastro
            final String CREATE_CADASTRO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CADASTRO + "("
                    + KEY_CADASTRO_ID + " INTEGER PRIMARY KEY,"
                    + KEY_CADASTRO_NAME + " TEXT" + ")";
            db.execSQL(CREATE_CADASTRO_TABLE);

            Log.d(TAG, "Database " + TABLE_CADASTRO + " tables created");
        }*/

        /*if (oldVersion < 3) {
            // Alter Table Percurso
            db.execSQL(DATABASE_ALTER_PERCURSO_1);
            db.execSQL(DATABASE_ALTER_PERCURSO_2);
            db.execSQL(DATABASE_ALTER_PERCURSO_3);
            db.execSQL(DATABASE_ALTER_PERCURSO_4);

            //Log.d(TAG, "Database: " + TABLE_PERCURSO + " tables ALTERADA");
        }*/

        /*if (oldVersion < 4) {
            // Create Percurso
            final String CREATE_PERCURSO_FINAL_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PERCURSO_FINAL + "("
                    + KEY_PERCURSO_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PERCURSO_UID + " TEXT,"
                    + KEY_PERCURSO_DATA + " TEXT,"
                    + KEY_PERCURSO_NOME + " TEXT,"
                    + KEY_PERCURSO_TIPO + " TEXT,"
                    + KEY_PERCURSO_NIVEL + " TEXT,"
                    + KEY_PERCURSO_DESCRICAO + " TEXT,"
                    + KEY_PERCURSO_BIKEID + " TEXT" + ")";
            db.execSQL(CREATE_PERCURSO_FINAL_TABLE);

            Log.d(TAG, "Database " + TABLE_PERCURSO_FINAL + " tables created");

            Log.d(TAG, "Database: " + CREATE_PERCURSO_FINAL_TABLE + " tables CRIADA");
        }*/
    }

    /**
     * ********************************************************************************************
     * ********************************************************************************************
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New USER inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    /**
     * ********************************************************************************************
     * ********************************************************************************************
     * Storing Bike details in database
     * */
    public void addBike(String name, String uid) {
        //onUpgrade(this.getWritableDatabase(), 1, 2);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BIKE_NAME, name); // Name
        values.put(KEY_BIKE_UID, uid); // Uid

        // Inserting Row
        long id = db.insert(TABLE_BIKE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New BIKE inserted into sqlite: " + id);
    }

    public void updateBike(String id, String kilo){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_BIKE + " SET " + KEY_BIKE_KILO + " = '"+ kilo +"' WHERE " + KEY_BIKE_UID + " ="+"'"+ id +"'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    /**
     * Getting Bike data from database
     * */
    public HashMap<String, String> getBikeDetails() {
        HashMap<String, String> bike = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_BIKE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            bike.put("name", cursor.getString(1));
            bike.put("uid", cursor.getString(2));
            bike.put("kilometragem", cursor.getString(3));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Bikes from Sqlite: " + bike.toString());

        return bike;
    }

    /**
     * Getting Bike data from database
     * */
    public HashMap<String, String> getBikeAtual() {
        HashMap<String, String> bike = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_BIKE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            //bike.put("name", cursor.getString(1));
            bike.put("uid", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Bike Atual from Sqlite: " + bike.toString());

        return bike;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteBikes() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_BIKE, null, null);
        db.close();

        Log.d(TAG, "Deleted all Bikes info from sqlite");
    }

    /**
     * ********************************************************************************************
     * ********************************************************************************************
     * Storing Cadastro Completo details in database
     * */
    public void addCadastro(String name) {
        //onUpgrade(this.getWritableDatabase(), 1, 2);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CADASTRO_NAME, name); // Name

        // Inserting Row
        long id = db.insert(TABLE_CADASTRO, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New CADASTRO inserted into sqlite: " + id);
    }

    /**
     * Getting Cadastro Completo data from database
     * */
    public HashMap<String, String> getCadastroDetails() {
        HashMap<String, String> cadastro = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_CADASTRO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cadastro.put("status", cursor.getString(1));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Cadastro from Sqlite: " + cadastro.toString());

        return cadastro;
    }

    /**
     * ********************************************************************************************
     * ********************************************************************************************
     * Storing Percurso details in database
     * */
    public void addPercurso(String bikeid, String date) {
        //onUpgrade(this.getWritableDatabase(), 1, 2);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PERCURSO_BIKEID, bikeid); // Bike Id
        values.put(KEY_PERCURSO_DATA, date); // Data

        // Inserting Row
        long id = db.insert(TABLE_PERCURSO, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New Percurso inserted into sqlite: " + id);
    }

    /**
     * Update dados de Percurso
     * */
    public boolean updatePercurso(String nome, String tipo, String nivel, String descricao, String status, String id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_PERCURSO_NOME, nome);
        updateValues.put(KEY_PERCURSO_TIPO, tipo);
        updateValues.put(KEY_PERCURSO_NIVEL, nivel);
        updateValues.put(KEY_PERCURSO_DESCRICAO, descricao);
        updateValues.put(KEY_PERCURSO_STATUS, status);
        String where = KEY_PERCURSO_ID + " = " + id;

        int i = db.update(TABLE_PERCURSO, updateValues, where, null);
        if(i > 0){
            Log.i(TAG, "Rows updated table " + TABLE_PERCURSO + " returned: " + i);
        }
        return i > 0;
    }

    public void updateDistancePercurso(String id, double dist){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_PERCURSO + " SET " + KEY_PERCURSO_DIST + " = '"+ dist +"' WHERE " + KEY_PERCURSO_ID + " ="+"'"+ id +"'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
        // return
        Log.i(TAG, "DISTANCE updated table " + TABLE_PERCURSO + " returned: " + updateQuery);
    }

    /**
     * Getting Percurso data from database
     * */
    public HashMap<String, String> getPercursoLast() {
        HashMap<String, String> percurso = new HashMap<String, String>();
        String selectQuery = "SELECT id FROM " + TABLE_PERCURSO + " ORDER BY id DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            percurso.put("id", cursor.getString(0));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching ID Percurso from Sqlite: " + percurso.toString());

        return percurso;
    }

    /**
     * Getting Percurso data from database
     * */
    public HashMap<String, String> getPercursoDetalhes(String id) {
        HashMap<String, String> percurso = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_PERCURSO + " WHERE " + KEY_PERCURSO_ID + " = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            percurso.put(KEY_PERCURSO_ID, cursor.getString(0));
            percurso.put(KEY_PERCURSO_BIKEID, cursor.getString(1));
            percurso.put(KEY_PERCURSO_DATA, cursor.getString(2));
            percurso.put(KEY_PERCURSO_NOME, cursor.getString(3));
            percurso.put(KEY_PERCURSO_TIPO, cursor.getString(4));
            percurso.put(KEY_PERCURSO_NIVEL, cursor.getString(5));
            percurso.put(KEY_PERCURSO_DESCRICAO, cursor.getString(6));
            percurso.put(KEY_PERCURSO_STATUS, cursor.getString(7));
            percurso.put(KEY_PERCURSO_DIST, cursor.getString(8));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching * Percurso from Sqlite: " + percurso.toString());

        return percurso;
    }

    /**
     * Getting Percurso data from database
     * */
    public HashMap<String, String> getPercurso() {
        HashMap<String, String> percurso = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_PERCURSO + " ORDER BY id DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
                percurso.put(KEY_PERCURSO_ID, cursor.getString(0));
                percurso.put(KEY_PERCURSO_DATA, cursor.getString(1));
                percurso.put(KEY_PERCURSO_NOME, cursor.getString(2));
                percurso.put(KEY_PERCURSO_TIPO, cursor.getString(3));
                percurso.put(KEY_PERCURSO_NIVEL, cursor.getString(4));
                percurso.put(KEY_PERCURSO_DESCRICAO, cursor.getString(5));
                percurso.put(KEY_PERCURSO_BIKEID, cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching DADOS Percurso from Sqlite: " + percurso.toString());

        return percurso;
    }

    // Get Data Percursos (*** FUNCIONA para um Resultado)
    public ArrayList<String> getDataPercursos() {
        ArrayList<String> values = new ArrayList<String>();
        String columns[] = new String[] { KEY_PERCURSO_NOME };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(TABLE_PERCURSO, columns, null, null, null, null,
                null);
        String result;
        int nome_percurso = c.getColumnIndex(KEY_PERCURSO_NOME);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = c.getString(nome_percurso);
            values.add(result);
        }
        return values;
    }

    // Get All Percursos (*** FUNCIONA)
    public List<Percurso> getAllPercursos() {
        List<Percurso> percursos = new ArrayList<Percurso>();
        // 0. get bike_id
        String bike_id = String.valueOf(this.getBikeAtual());
        // 1. build the query
        String query = "SELECT * FROM " + TABLE_PERCURSO + " ORDER BY " + KEY_PERCURSO_ID + " DESC";
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // 3. go over each row, build book and add it to list
        Percurso percurso = null;
        if (cursor.moveToFirst()) {
            do {
                percurso = new Percurso();
                percurso.setId(Integer.valueOf(cursor.getString(0)));
                percurso.setBikeid(cursor.getString(1));
                percurso.setDate(cursor.getString(2));
                percurso.setNome(cursor.getString(3));
                percurso.setTipo(cursor.getString(4));
                percurso.setNivel(cursor.getString(5));
                percurso.setDescricao(cursor.getString(6));

                // Add percurso to percursos
                percursos.add(percurso);
            } while (cursor.moveToNext());
        }
        Log.d("getAllPercursos(TODOS)", percursos.toString());

        // return percursos
        return percursos;
    }

    // Get All Relatorios (*** FUNCIONA)
    public List<Relatorio> getAllRelatorios() {
        List<Relatorio> relatorios = new ArrayList<Relatorio>();
        // 0. get bike_id
        //String relatorio_id = String.valueOf(this.getBikeAtual());
        // 1. build the query
        String query = "SELECT * FROM " + TABLE_RELATORIO + " ORDER BY " + KEY_RELATORIO_ID + " DESC";
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // 3. go over each row, build book and add it to list
        Relatorio relatorio = null;
        if (cursor.moveToFirst()) {
            do {
                relatorio = new Relatorio();
                relatorio.setId(Integer.valueOf(cursor.getString(0)));
                relatorio.setManutencao_id(cursor.getString(1));
                relatorio.setManual_id(cursor.getString(2));
                relatorio.setData_criacao(cursor.getString(3));
                relatorio.setStatus(cursor.getString(4));
                relatorio.setVerificacao(cursor.getString(5));
                relatorio.setConjunto(cursor.getString(6));

                // Add percurso to percursos
                relatorios.add(relatorio);
            } while (cursor.moveToNext());
        }
        Log.d("getAllRelatorios(TODOS)", relatorios.toString());

        // return percursos
        return relatorios;
    }

    /**
     * Getting Dados Percurso data from database
     * */
    public HashMap<String, String> getLastDist(String percurso_id) {
        HashMap<String, String> dados_percurso = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_PERCURSO + " WHERE " + KEY_PERCURSO_ID + " = " + percurso_id + " ORDER BY " + KEY_PERCURSO_ID + " DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            dados_percurso.put("id", cursor.getString(0));
            dados_percurso.put("distancia", cursor.getString(8));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Percurso [DISTANCIA] from Sqlite: " + dados_percurso.toString());

        return dados_percurso;
    }

    /**
     * *********************************************************************************************
     * SINCRONIZACAO SQLITE COM MYSQL - PERCURSOS - BEGIN
     * *********************************************************************************************
     */
    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getallPercursosDB() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM  " + TABLE_PERCURSO;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(KEY_PERCURSO_ID, cursor.getString(0));
                map.put(KEY_PERCURSO_BIKEID, cursor.getString(1));
                map.put(KEY_PERCURSO_DATA, cursor.getString(2));
                map.put(KEY_PERCURSO_NOME, cursor.getString(3));
                map.put(KEY_PERCURSO_TIPO, cursor.getString(4));
                map.put(KEY_PERCURSO_NIVEL, cursor.getString(5));
                map.put(KEY_PERCURSO_DESCRICAO, cursor.getString(6));
                map.put(KEY_PERCURSO_DIST, cursor.getString(8));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + TABLE_PERCURSO + " WHERE " + KEY_PERCURSO_STATUS + " = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(KEY_PERCURSO_ID, cursor.getString(0));
                map.put(KEY_PERCURSO_BIKEID, cursor.getString(1));
                map.put(KEY_PERCURSO_DATA, cursor.getString(2));
                map.put(KEY_PERCURSO_NOME, cursor.getString(3));
                map.put(KEY_PERCURSO_TIPO, cursor.getString(4));
                map.put(KEY_PERCURSO_NIVEL, cursor.getString(5));
                map.put(KEY_PERCURSO_DESCRICAO, cursor.getString(6));
                map.put(KEY_PERCURSO_DIST, cursor.getString(8));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            //msg = "SQLite and Remote MySQL DBs are in Sync!";
            msg = "Dados sendo sincronizados!";
        }else{
            //msg = "DB Sync needed";
            msg = "Necessário sincronizar dados";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_PERCURSO + " WHERE " + KEY_PERCURSO_STATUS + " = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_PERCURSO + " SET " + KEY_PERCURSO_STATUS + " = '"+ status +"' WHERE " + KEY_PERCURSO_ID + " ="+"'"+ id +"'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }
    /**
     * *********************************************************************************************
     * // SINCRONIZACAO SQLITE COM MYSQL - PERCURSOS - END
     * *********************************************************************************************
     */


    /**
     * *********************************************************************************************
     * SINCRONIZACAO SQLITE COM MYSQL - DADOS PERCURSOS - BEGIN
     * *********************************************************************************************
     *
     */
    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getallPercursosDBDadosPercurso() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM  " + TABLE_DADOS_PERCURSO;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(KEY_DADOS_PERCURSO_ID, cursor.getString(0));
                map.put(KEY_DADOS_PERCURSO_UID, cursor.getString(1));
                map.put(KEY_DADOS_PERCURSO_LATLONG, cursor.getString(2));
                map.put(KEY_DADOS_PERCURSO_DATA, cursor.getString(3));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLiteDadosPercurso(){

        HashMap<String, String> bike = this.getBikeAtual();
        String bike_id = bike.get("uid");

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + TABLE_DADOS_PERCURSO + " WHERE " + KEY_DADOS_PERCURSO_STATUS + " = '"+"no"+"'";

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(KEY_DADOS_PERCURSO_ID, cursor.getString(0));
                map.put(KEY_DADOS_PERCURSO_UID, cursor.getString(1));
                map.put(KEY_DADOS_PERCURSO_LATLONG, cursor.getString(2));
                map.put(KEY_DADOS_PERCURSO_DATA, cursor.getString(3));
                map.put(KEY_DADOS_PERCURSO_DIST, cursor.getString(5));
                map.put(KEY_BIKE_ATUAL, bike_id);
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatusDadosPercurso(){
        String msg = null;
        if(this.dbSyncCountDadosPercurso() == 0){
            //msg = "SQLite and Remote MySQL DBs are in Sync!";
            msg = "Dados sendo sincronizados!";
        }else{
            //msg = "DB Sync needed";
            msg = "Necessário sincronizar dados";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCountDadosPercurso(){
        int count = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_DADOS_PERCURSO + " WHERE " + KEY_DADOS_PERCURSO_STATUS + " = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatusDadosPercurso(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_DADOS_PERCURSO + " SET " + KEY_DADOS_PERCURSO_STATUS + " = '"+ status +"' WHERE " + KEY_DADOS_PERCURSO_ID + " ="+"'"+ id +"'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }
    /**
     * *********************************************************************************************
     * // SINCRONIZACAO SQLITE COM MYSQL - DADOS PERCURSOS - END
     * *********************************************************************************************
     */


    /**
     * ********************************************************************************************
     * DADOS DE PERCURSO
     * ********************************************************************************************
     * Storing Dados Percurso details in database
     * */
    public void addDadosPercurso(String uid, String latlong, String date, double dist) {
        //onUpgrade(this.getWritableDatabase(), 1, 2);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DADOS_PERCURSO_UID, uid); // Percurso Id
        values.put(KEY_DADOS_PERCURSO_LATLONG, latlong); // LatLong
        values.put(KEY_DADOS_PERCURSO_DATA, date); // Data
        values.put(KEY_DADOS_PERCURSO_STATUS, "no"); // Status
        values.put(KEY_DADOS_PERCURSO_DIST, dist); // Status

        // Inserting Row
        long id = db.insert(TABLE_DADOS_PERCURSO, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New Dado de Percurso inserted into sqlite: " + id);
    }

    /**
     * Getting Dados Percurso data from database
     * */
    public HashMap<String, String> getDadosPercursoDetails() {
        HashMap<String, String> dados_percurso = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_DADOS_PERCURSO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            dados_percurso.put("latlong", cursor.getString(1));
            dados_percurso.put("uid", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Dados de Percurso from Sqlite: " + dados_percurso.toString());

        return dados_percurso;
    }

    /**
     * Getting Dados Percurso data from database
     * */
    public HashMap<String, String> getLastLatLong(String percurso_id) {
        HashMap<String, String> dados_percurso = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_DADOS_PERCURSO + " WHERE " + KEY_DADOS_PERCURSO_UID + " = " + percurso_id + " ORDER BY " + KEY_DADOS_PERCURSO_ID + " DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            dados_percurso.put("uid", cursor.getString(1));
            dados_percurso.put("latlong", cursor.getString(2));
            dados_percurso.put("distancia", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching Dados de Percurso [LAT/LNG] from Sqlite: " + dados_percurso.toString());

        return dados_percurso;
    }

    /**
     * ********************************************************************************************
     * DADOS RELATORIO
     * ********************************************************************************************
     * Storing Relatorio details in database
     * */
    public void addRelatorio(String manual_id, String manutencao_id, String data_criacao, String verificacao, String conjunto) {
        //onUpgrade(this.getWritableDatabase(), 1, 2);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RELATORIO_MANUAL_ID, manual_id); // manual_id
        values.put(KEY_RELATORIO_MANUTENCAO_ID, manutencao_id); // manutencao_id
        values.put(KEY_RELATORIO_DATA, data_criacao); // data_criacao
        values.put(KEY_RELATORIO_VERIFICACAO, "no"); // Status
        values.put(KEY_RELATORIO_STATUS, verificacao); // verificacao
        values.put(KEY_RELATORIO_CONJUNTO, conjunto); // conjunto

        // Inserting Row
        long id = db.insert(TABLE_RELATORIO, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New RELATORIO inserted into sqlite: " + id);
    }

    /**
     * Getting Detalhes Relatorio data from database
     * */
    public HashMap<String, String> getRelatorioDetalhes(String id) {
        HashMap<String, String> relatorio = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_RELATORIO + " WHERE " + KEY_RELATORIO_ID + " = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            relatorio.put(KEY_RELATORIO_ID, cursor.getString(0));
            relatorio.put(KEY_RELATORIO_MANUAL_ID, cursor.getString(1));
            relatorio.put(KEY_RELATORIO_MANUTENCAO_ID, cursor.getString(2));
            relatorio.put(KEY_RELATORIO_DATA, cursor.getString(3));
            relatorio.put(KEY_RELATORIO_VERIFICACAO, cursor.getString(4));
            relatorio.put(KEY_RELATORIO_STATUS, cursor.getString(5));
            relatorio.put(KEY_RELATORIO_CONJUNTO, cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching DETALHES RELATORIO from Sqlite: " + relatorio.toString());

        return relatorio;
    }

    /***********************************************************************************************
     * DELETE
     *
     * Re crate database Delete all tables and create them again
     * */
    public void deletePercurso() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_PERCURSO, null, null);
        db.close();

        Log.d(TAG, "Deleted all Percursos info from sqlite");
    }

    public void deletePercursoFinal() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_PERCURSO_FINAL, null, null);
        db.close();

        Log.d(TAG, "Deleted all Percursos info from sqlite");
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteDadosPercurso() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_DADOS_PERCURSO, null, null);
        db.close();

        Log.d(TAG, "Deleted all Dados de Percursos info from sqlite");
    }

    /***********************************************************************************************
     * DROP TABLES
     *
     * */
    public void dropPercurso() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERCURSO);
    }

    public void dropDadosPercurso() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DADOS_PERCURSO);
    }
}
