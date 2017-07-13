package com.application.smartbiosensor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.application.smartbiosensor.vo.Calibration;
import com.application.smartbiosensor.vo.Configuration;

import java.sql.Timestamp;

public class ConfigurationDAO {

    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public ConfigurationDAO(Context context) {
        this.context = context;
        dbHelper = DataBaseHelper.getHelper(context);
        open();

    }

    public void open() throws SQLException {

        if(dbHelper == null)
            dbHelper = DataBaseHelper.getHelper(context);

        database = dbHelper.getWritableDatabase();

    }

    public void close() {
        dbHelper.close();
        database = null;
    }

    public boolean addConfiguration(Configuration configuration){

        boolean addConfigurationResult = false;
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.NUMBER_AVERAGE_MEASURE_COLUMN, configuration.getNumberAverageMeasure());
        values.put(DataBaseHelper.NUMBER_THRESHOLD_COLUMN, configuration.getNumberThreshold());
        values.put(DataBaseHelper.CALIBRATION_ID_COLUMN, configuration.getCalibration().getId());

        if(database.insert(DataBaseHelper.CONFIGURATION_TABLE, null, values) != -1) {
            addConfigurationResult = true;
        }
        close();
        return addConfigurationResult;

    }

    public Configuration getActualConfiguration() {

        Configuration configuration = null;

        Cursor cursor = database.query(DataBaseHelper.CONFIGURATION_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.NUMBER_AVERAGE_MEASURE_COLUMN,
                        DataBaseHelper.NUMBER_THRESHOLD_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN,
                        DataBaseHelper.CALIBRATION_ID_COLUMN},
                null, null, null, null, DataBaseHelper.DATETIME_COLUMN + " DESC");

        if (cursor.moveToNext()) {
            configuration = new Configuration();
            configuration.setId(cursor.getLong(0));
            configuration.setNumberAverageMeasure(cursor.getInt(1));
            configuration.setNumberThreshold(cursor.getInt(2));
            configuration.setDatetime(Timestamp.valueOf(cursor.getString(3)));

            CalibrationDAO calibrationDAO = new CalibrationDAO(context);
            Calibration calibration = calibrationDAO.getCalibration(cursor.getLong(4));
            configuration.setCalibration(calibration);

        }
        close();
        return configuration;
    }

    public Configuration getConfiguration(long id) {

        Configuration configuration = null;

        String whereClause = DataBaseHelper.ID_COLUMN + " = ? ";
        String [] whereArgs = {String.valueOf(id)};

        Cursor cursor = database.query(DataBaseHelper.CONFIGURATION_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.NUMBER_AVERAGE_MEASURE_COLUMN,
                        DataBaseHelper.NUMBER_THRESHOLD_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN},
                whereClause, whereArgs, null, null, null);

        if (cursor.moveToNext()) {
            configuration = new Configuration();
            configuration.setId(cursor.getLong(0));
            configuration.setNumberAverageMeasure(cursor.getInt(1));
            configuration.setNumberThreshold(cursor.getInt(2));
            configuration.setDatetime(Timestamp.valueOf(cursor.getString(3)));

        }
        close();
        return configuration;
    }



}
