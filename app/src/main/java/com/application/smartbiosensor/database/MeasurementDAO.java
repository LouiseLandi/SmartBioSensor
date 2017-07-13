package com.application.smartbiosensor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.application.smartbiosensor.vo.Configuration;
import com.application.smartbiosensor.vo.Correction;
import com.application.smartbiosensor.vo.ItemMeasurement;
import com.application.smartbiosensor.vo.Measurement;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MeasurementDAO {

    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public MeasurementDAO(Context context) {
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

    public long addMeasurement(Measurement measurement){

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.CORRECTION_ID_COLUMN, measurement.getCorrection().getId());
        values.put(DataBaseHelper.CONFIGURATION_ID_COLUMN, measurement.getConfiguration().getId());

        long id = database.insert(DataBaseHelper.MEASUREMENT_TABLE, null, values);

        close();
        return id;

    }


    public ArrayList<Measurement> getMeasurements(Date date) {

        ArrayList<Measurement>  measurements = new ArrayList<Measurement>();

        String whereClause = "date(" + DataBaseHelper.DATETIME_COLUMN + ") = ? ";
        String [] whereArgs = {date.toString()};

        Cursor cursor = database.query(DataBaseHelper.MEASUREMENT_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.CORRECTION_ID_COLUMN,
                        DataBaseHelper.CONFIGURATION_ID_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN},
                whereClause, whereArgs, null, null, DataBaseHelper.DATETIME_COLUMN + " ASC");

        while (cursor.moveToNext()) {
            Measurement measurement = new Measurement();
            measurement.setId(cursor.getLong(0));

            CorrectionDAO correctionDAO = new CorrectionDAO(context);
            Correction correction = correctionDAO.getCorrection(cursor.getLong(1));
            measurement.setCorrection(correction);

            ConfigurationDAO configurationDAO = new ConfigurationDAO(context);
            Configuration configuration = configurationDAO.getConfiguration(cursor.getLong(2));
            measurement.setConfiguration(configuration);

            measurement.setDatetime(Timestamp.valueOf(cursor.getString(3)));

            ItemMeasurementDAO itemMeasurementDAO = new ItemMeasurementDAO(context);
            ArrayList<ItemMeasurement> itemsMeasurement = itemMeasurementDAO.getItemsMeasurement(measurement.getId());
            measurement.setItemsMeasurements(itemsMeasurement);

            measurements.add(measurement);

        }
        close();
        return measurements;
    }

}
