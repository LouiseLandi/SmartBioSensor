package com.application.smartbiosensor;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;

public class Util {

    public static File saveBitmapToFile(Bitmap bmp, String fileName, String directory) throws Exception {

        try {

            File file = new File(directory + fileName + ".jpg");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes);
            fo.close();

            return file;

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public static File saveBytesToFile(byte[] bytes, String fileName, String directory) throws Exception {

        try {

            File file = new File(directory + fileName + ".jpg");

            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes);
            fo.close();

            return file;

        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

    }


    public static double roundDoubleDecimalCases(double value, int numberDecimalPlaces) {

        String format = "0.";

        for(int i = 0; i < numberDecimalPlaces; i++)
            format = format + "0";

        DecimalFormat formatador = new DecimalFormat(format);
        String valueFormat = formatador.format(value);
        valueFormat = valueFormat.replace(",", ".");
        return Double.valueOf(valueFormat);

    }

    public static Date addDaysToDate(Date date, int days){

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return new java.sql.Date(cal.getTimeInMillis());
    }

}
