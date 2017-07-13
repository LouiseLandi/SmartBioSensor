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



    public long addCalibration(Calibration calibration){

        boolean addCalibrationResult = false;
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.A_COLUMN, calibration.getA());
        values.put(DataBaseHelper.B_COLUMN, calibration.getB());
        values.put(DataBaseHelper.R2_COLUMN, calibration.getR2());

        long id = database.insert(DataBaseHelper.CALIBRATION_TABLE, null, values);
        close();
        return id;

    }

    public Calibration getCalibration(long id) {

        Calibration calibration = null;

        String whereClause = DataBaseHelper.ID_COLUMN + " = ? ";
        String [] whereArgs = {String.valueOf(id)};

        Cursor cursor = database.query(DataBaseHelper.CALIBRATION_TABLE,
                                    new String[] { DataBaseHelper.ID_COLUMN,
                                                   DataBaseHelper.A_COLUMN,
                                                   DataBaseHelper.B_COLUMN,
                                                   DataBaseHelper.R2_COLUMN,
                                                   DataBaseHelper.DATETIME_COLUMN},
                                    whereClause, whereArgs, null, null, null);

        if (cursor.moveToNext()) {
            calibration = new Calibration(cursor.getDouble(1), cursor.getDouble(2), cursor.getDouble(3));
            calibration.setId(cursor.getLong(0));
            calibration.setDatetime(Timestamp.valueOf(cursor.getString(4)));

        }
        return calibration;
    }


}



