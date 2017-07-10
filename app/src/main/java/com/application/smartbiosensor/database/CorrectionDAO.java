package com.application.smartbiosensor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.application.smartbiosensor.vo.Correction;

import java.sql.Timestamp;

public class CorrectionDAO {

    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public CorrectionDAO(Context context) {
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

    public boolean addCorrection(Correction correction){

        boolean addCorrectionResult = false;
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.INTENSITY_COLUMN, correction.getIntensity());
        values.put(DataBaseHelper.REFERENCE_INTENSITY_COLUMN, correction.getReferenceIntensity());

        if(database.insert(DataBaseHelper.CORRECTION_TABLE, null, values) != -1) {
            addCorrectionResult = true;
        }
        close();
        return addCorrectionResult;

    }

    public Correction getActualCorrection() {

        Correction correction = null;

        Cursor cursor = database.query(DataBaseHelper.CORRECTION_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.INTENSITY_COLUMN,
                        DataBaseHelper.REFERENCE_INTENSITY_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN},
                null, null, null, null, DataBaseHelper.DATETIME_COLUMN + " DESC");

        if (cursor.moveToNext()) {
            correction = new Correction();
            correction.setId(cursor.getLong(0));
            correction.setIntensity(cursor.getDouble(1));
            correction.setReferenceIntensity(cursor.getDouble(2));
            correction.setDatetime(Timestamp.valueOf(cursor.getString(3)));

        }
        close();
        return correction;
    }

    public Correction getCorrection(long id) {

        Correction correction = null;

        String whereClause = DataBaseHelper.ID_COLUMN + " = ? ";
        String [] whereArgs = {String.valueOf(id)};

        Cursor cursor = database.query(DataBaseHelper.CORRECTION_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.INTENSITY_COLUMN,
                        DataBaseHelper.REFERENCE_INTENSITY_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN},
                whereClause, whereArgs, null, null, null);

        if (cursor.moveToNext()) {
            correction = new Correction();
            correction.setId(cursor.getLong(0));
            correction.setIntensity(cursor.getDouble(1));
            correction.setReferenceIntensity(cursor.getDouble(2));
            correction.setDatetime(Timestamp.valueOf(cursor.getString(3)));

        }
        close();
        return correction;
    }

}
