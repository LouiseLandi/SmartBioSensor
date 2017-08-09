package com.application.smartbiosensor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseHelper extends SQLiteOpenHelper{

    private static DataBaseHelper instance;
    private static final String DATABASE_NAME = "smartsensorbd";
    private static final int DATABASE_VERSION = 8;

    public static final String CALIBRATION_TABLE = "calibracao";
    public static final String CONFIGURATION_TABLE = "configuracao";
    public static final String MEASUREMENT_TABLE = "medicao";
    public static final String ITEM_MEASUREMENT_TABLE = "itemMedicao";
    public static final String CORRECTION_TABLE = "correcao";

    public static final String ID_COLUMN = "id";
    public static final String DATETIME_COLUMN = "datahora";
    public static final String A_COLUMN = "a";
    public static final String B_COLUMN = "b";
    public static final String R2_COLUMN = "r2";
    public static final String NUMBER_AVERAGE_MEASURE_COLUMN = "numeroMediasMedicao";
    public static final String NUMBER_THRESHOLD_COLUMN = "numeroThreshold";
    public static final String INTENSITY_COLUMN = "intensidade";
    public static final String REFERENCE_INTENSITY_COLUMN = "intensidadeReferencia";
    public static final String CONFIGURATION_ID_COLUMN = "configuracaoId";
    public static final String CORRECTION_ID_COLUMN = "correcaoId";
    public static final String MEASUREMENT_ID_COLUMN = "medicaoId";
    public static final String CALIBRATION_ID_COLUMN = "calibracaoId";

    public static synchronized DataBaseHelper getHelper(Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CALIBRATION_TABLE = "CREATE TABLE "
                + CALIBRATION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
                + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + A_COLUMN + " REAL," + B_COLUMN + " REAL," + R2_COLUMN + " REAL)";

        String CREATE_CONFIGURATION_TABLE = "CREATE TABLE "
                + CONFIGURATION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
                + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + NUMBER_AVERAGE_MEASURE_COLUMN
                + " INTEGER," + NUMBER_THRESHOLD_COLUMN + " INTEGER,"
                + CALIBRATION_ID_COLUMN + " INTEGER, FOREIGN KEY(" + CALIBRATION_ID_COLUMN
                + ") REFERENCES " + CALIBRATION_TABLE + "(" + ID_COLUMN + "))";

        String CREATE_MEASUREMENT_TABLE = "CREATE TABLE "
                + MEASUREMENT_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
                + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
                + CORRECTION_ID_COLUMN + " INTEGER," + CONFIGURATION_ID_COLUMN + " INTEGER, FOREIGN KEY(" + CORRECTION_ID_COLUMN
                + ") REFERENCES " + CORRECTION_TABLE + "(" + ID_COLUMN + "), FOREIGN KEY(" + CONFIGURATION_ID_COLUMN
                + ") REFERENCES " + CONFIGURATION_TABLE + "(" + ID_COLUMN + "))";

        String CREATE_CORRECTION_TABLE = "CREATE TABLE "
                + CORRECTION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
                + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + INTENSITY_COLUMN
                + " REAL," + REFERENCE_INTENSITY_COLUMN + " REAL," + CONFIGURATION_ID_COLUMN + " INTEGER, FOREIGN KEY(" + CONFIGURATION_ID_COLUMN
                + ") REFERENCES " + CONFIGURATION_TABLE + "(" + ID_COLUMN + "))";

        String CREATE_ITEM_MEASUREMENT_TABLE = "CREATE TABLE "
                + ITEM_MEASUREMENT_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
                + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + INTENSITY_COLUMN
                + " REAL," + REFERENCE_INTENSITY_COLUMN + " REAL," + MEASUREMENT_ID_COLUMN + " INTEGER, FOREIGN KEY("
                + MEASUREMENT_ID_COLUMN + ") REFERENCES " + MEASUREMENT_TABLE + "(" + ID_COLUMN + "))";


        String INSERT_DEFAULT_CONFIGURATION = "INSERT INTO " + CONFIGURATION_TABLE + "(" +
                NUMBER_AVERAGE_MEASURE_COLUMN + ", " + NUMBER_THRESHOLD_COLUMN + ", "
                + CALIBRATION_ID_COLUMN + ")" + " VALUES (1, 235, 1)";

        String INSERT_DEFAULT_CALIBRATION = "INSERT INTO " + CALIBRATION_TABLE + "(" +
                A_COLUMN + ", " + B_COLUMN + ", " + R2_COLUMN + ")" + " VALUES (-4.628, 7.1702, 0.9932)";


        db.execSQL(CREATE_CALIBRATION_TABLE);
        db.execSQL(CREATE_CONFIGURATION_TABLE);
        db.execSQL(CREATE_CORRECTION_TABLE);
        db.execSQL(CREATE_ITEM_MEASUREMENT_TABLE);
        db.execSQL(CREATE_MEASUREMENT_TABLE);
        db.execSQL(INSERT_DEFAULT_CALIBRATION);
        db.execSQL(INSERT_DEFAULT_CONFIGURATION);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        String DROP_CALIBRATION_TABLE = "DROP TABLE IF EXISTS " + CALIBRATION_TABLE;
        String DROP_CONFIGURATION_TABLE = "DROP TABLE IF EXISTS " + CONFIGURATION_TABLE;
        String DROP_MEASUREMENT_TABLE = "DROP TABLE IF EXISTS " + MEASUREMENT_TABLE;
        String DROP_CORRECTION_TABLE = "DROP TABLE IF EXISTS " + CORRECTION_TABLE;
        String DROP_ITEM_MEASUREMENT_TABLE = "DROP TABLE IF EXISTS " + ITEM_MEASUREMENT_TABLE;

        db.execSQL(DROP_CALIBRATION_TABLE);
        db.execSQL(DROP_CONFIGURATION_TABLE);
        db.execSQL(DROP_CORRECTION_TABLE);
        db.execSQL(DROP_ITEM_MEASUREMENT_TABLE);
        db.execSQL(DROP_MEASUREMENT_TABLE);
        onCreate(db);
    }
}





