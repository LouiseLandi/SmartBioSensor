package com.application.smartbiosensor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.application.smartbiosensor.LinearRegressionResult;
import com.application.smartbiosensor.vo.Calibration;

import java.sql.Timestamp;

public class CalibrationDAO {

    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public CalibrationDAO(Context context) {
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



    public boolean addCalibration(Calibration calibration){

        boolean addCalibrationResult = false;
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.A_COLUMN, calibration.getLinearRegressionResult().getA());
        values.put(DataBaseHelper.B_COLUMN, calibration.getLinearRegressionResult().getB());
        values.put(DataBaseHelper.R2_COLUMN, calibration.getLinearRegressionResult().getR2());

        if(database.insert(DataBaseHelper.CALIBRATION_TABLE, null, values) != -1) {
            addCalibrationResult = true;
        }
        close();
        return addCalibrationResult;

    }

    public Calibration getActualCalibration() {

        Calibration calibration = null;

        Cursor cursor = database.query(DataBaseHelper.CALIBRATION_TABLE,
                                    new String[] { DataBaseHelper.ID_COLUMN,
                                                   DataBaseHelper.A_COLUMN,
                                                   DataBaseHelper.B_COLUMN,
                                                   DataBaseHelper.R2_COLUMN,
                                                   DataBaseHelper.DATETIME_COLUMN},
                                    null, null, null, null, DataBaseHelper.DATETIME_COLUMN + " DESC");

        if (cursor.moveToNext()) {
            calibration = new Calibration();
            calibration.setId(cursor.getLong(0));

            LinearRegressionResult linearRegressionResult = new LinearRegressionResult(cursor.getDouble(1), cursor.getDouble(2), cursor.getDouble(3));
            calibration.setLinearRegressionResult(linearRegressionResult);

            calibration.setDatetime(Timestamp.valueOf(cursor.getString(4)));

        }
        return calibration;
    }


}



