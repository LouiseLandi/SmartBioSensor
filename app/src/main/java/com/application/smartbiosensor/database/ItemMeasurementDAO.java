package com.application.smartbiosensor.database;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.application.smartbiosensor.vo.ItemMeasurement;
import com.application.smartbiosensor.vo.Measurement;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ItemMeasurementDAO {

    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public ItemMeasurementDAO(Context context) {
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

    public boolean addItemMeasurement(ItemMeasurement itemMeasurement){

        boolean addItemMeasurementResult = false;
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.INTENSITY_COLUMN, itemMeasurement.getIntensity());
        values.put(DataBaseHelper.REFERENCE_INTENSITY_COLUMN, itemMeasurement.getReferenceIntensity());
        values.put(DataBaseHelper.MEASUREMENT_ID_COLUMN, itemMeasurement.getMeasurement().getId());

        if(database.insert(DataBaseHelper.ITEM_MEASUREMENT_TABLE, null, values) != -1) {
            addItemMeasurementResult = true;
        }
        close();
        return addItemMeasurementResult;

    }


    public ArrayList<ItemMeasurement> getItemsMeasurement(long idMeasurement) {

        ArrayList<ItemMeasurement>  itemsMeasurements = new ArrayList<ItemMeasurement>();

        String whereClause = DataBaseHelper.MEASUREMENT_ID_COLUMN + " = ? ";
        String [] whereArgs = {String.valueOf(idMeasurement)};

        Cursor cursor = database.query(DataBaseHelper.ITEM_MEASUREMENT_TABLE,
                new String[] { DataBaseHelper.ID_COLUMN,
                        DataBaseHelper.INTENSITY_COLUMN,
                        DataBaseHelper.REFERENCE_INTENSITY_COLUMN,
                        DataBaseHelper.DATETIME_COLUMN},
                whereClause, whereArgs, null, null, null);

        while (cursor.moveToNext()) {
            ItemMeasurement itemMeasurement = new ItemMeasurement();
            itemMeasurement.setId(cursor.getLong(0));
            itemMeasurement.setIntensity(cursor.getDouble(1));
            itemMeasurement.setReferenceIntensity(cursor.getDouble(2));
            itemMeasurement.setDatetime(Timestamp.valueOf(cursor.getString(3)));

            Measurement measurement = new Measurement();
            measurement.setId(idMeasurement);
            itemMeasurement.setMeasurement(measurement);

            itemsMeasurements.add(itemMeasurement);

        }
        close();
        return itemsMeasurements;
    }


}
