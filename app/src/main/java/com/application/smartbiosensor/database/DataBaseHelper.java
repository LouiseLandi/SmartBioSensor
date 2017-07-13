package com.application.smartbiosensor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseHelper extends SQLiteOpenHelper{


    private static DataBaseHelper instance;
    private static final String DATABASE_NAME = "smartsensordb";
    private static final int DATABASE_VERSION = 6;

    public static final String CALIBRATION_TABLE = "calibration";
    public static final String CONFIGURATION_TABLE = "configuration";
    public static final String MEASUREMENT_TABLE = "measurement";
    public static final String ITEM_MEASUREMENT_TABLE = "itemMeasurement";
    public static final String CORRECTION_TABLE = "correction";

    public static final String ID_COLUMN = "id";
    public static final String DATETIME_COLUMN = "datetime";
    public static final String A_COLUMN = "a";
    public static final String B_COLUMN = "b";
    public static final String R2_COLUMN = "r2";
    public static final String NUMBER_AVERAGE_MEASURE_COLUMN = "numberAverageMeasure";
    public static final String NUMBER_THRESHOLD_COLUMN = "numberThreshold";
    public static final String INTENSITY_COLUMN = "intensity";
    public static final String REFERENCE_INTENSITY_COLUMN = "referenceIntensity";
    public static final String CONFIGURATION_ID_COLUMN = "configurationId";
    public static final String CORRECTION_ID_COLUMN = "correctionId";
    public static final String MEASUREMENT_ID_COLUMN = "measurementId";
    public static final String CALIBRATION_ID_COLUMN = "calibrationId";

    public static final String CREATE_CALIBRATION_TABLE = "CREATE TABLE "
            + CALIBRATION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + A_COLUMN + " REAL," + B_COLUMN + " REAL," + R2_COLUMN + " REAL)";

    public static final String CREATE_CONFIGURATION_TABLE = "CREATE TABLE "
            + CONFIGURATION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + NUMBER_AVERAGE_MEASURE_COLUMN
            + " INTEGER," + NUMBER_THRESHOLD_COLUMN + " INTEGER,"
            + CALIBRATION_ID_COLUMN + " INTEGER, FOREIGN KEY(" + CALIBRATION_ID_COLUMN
            + ") REFERENCES " + CALIBRATION_TABLE + "(" + ID_COLUMN + "))";

    public static final String CREATE_MEASUREMENT_TABLE = "CREATE TABLE "
            + MEASUREMENT_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
            + CORRECTION_ID_COLUMN + " INTEGER," + CONFIGURATION_ID_COLUMN + " INTEGER, FOREIGN KEY(" + CORRECTION_ID_COLUMN
            + ") REFERENCES " + CORRECTION_TABLE + "(" + ID_COLUMN + "), FOREIGN KEY(" + CONFIGURATION_ID_COLUMN
            + ") REFERENCES " + CONFIGURATION_TABLE + "(" + ID_COLUMN + "))";

    public static final String CREATE_CORRECTION_TABLE = "CREATE TABLE "
            + CORRECTION_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + INTENSITY_COLUMN
            + " REAL," + REFERENCE_INTENSITY_COLUMN + " REAL)";

    public static final String CREATE_ITEM_MEASUREMENT_TABLE = "CREATE TABLE "
            + ITEM_MEASUREMENT_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + DATETIME_COLUMN + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))," + INTENSITY_COLUMN
            + " REAL," + REFERENCE_INTENSITY_COLUMN + " REAL," + MEASUREMENT_ID_COLUMN + " INTEGER, FOREIGN KEY("
            + MEASUREMENT_ID_COLUMN + ") REFERENCES " + MEASUREMENT_TABLE + "(" + ID_COLUMN + "))";


    public static final String INSERT_DEFAULT_CONFIGURATION = "INSERT INTO " + CONFIGURATION_TABLE + "(" +
            NUMBER_AVERAGE_MEASURE_COLUMN + ", " + NUMBER_THRESHOLD_COLUMN + ", "
            + CALIBRATION_ID_COLUMN + ")" + " VALUES (1, 235, 1)";

    public static final String INSERT_DEFAULT_CALIBRATION = "INSERT INTO " + CALIBRATION_TABLE + "(" +
            A_COLUMN + ", " + B_COLUMN + ", " + R2_COLUMN + ")" + " VALUES (-4.628, 7.1702, 0.9932)";


    public static final String DROP_CALIBRATION_TABLE = "DROP TABLE IF EXISTS " + CALIBRATION_TABLE;
    public static final String DROP_CONFIGURATION_TABLE = "DROP TABLE IF EXISTS " + CONFIGURATION_TABLE;
    public static final String DROP_MEASUREMENT_TABLE = "DROP TABLE IF EXISTS " + MEASUREMENT_TABLE;
    public static final String DROP_CORRECTION_TABLE = "DROP TABLE IF EXISTS " + CORRECTION_TABLE;
    public static final String DROP_ITEM_MEASUREMENT_TABLE = "DROP TABLE IF EXISTS " + ITEM_MEASUREMENT_TABLE;


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
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

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
        db.execSQL(DROP_CALIBRATION_TABLE);
        db.execSQL(DROP_CONFIGURATION_TABLE);
        db.execSQL(DROP_CORRECTION_TABLE);
        db.execSQL(DROP_ITEM_MEASUREMENT_TABLE);
        db.execSQL(DROP_MEASUREMENT_TABLE);
        onCreate(db);
    }
}





