package com.application.smartbiosensor.activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.application.smartbiosensor.R;
import com.application.smartbiosensor.database.MeasurementDAO;
import com.application.smartbiosensor.util.Util;
import com.application.smartbiosensor.vo.Configuration;
import com.application.smartbiosensor.vo.Correction;
import com.application.smartbiosensor.vo.ItemMeasurement;
import com.application.smartbiosensor.vo.Measurement;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

public class ExportActivity extends AppCompatActivity {

    final static private String APP_KEY = "z4zmyphuikx78fr";
    final static private String APP_SECRET = "jiybgvj6djjugqe";
    private DropboxAPI<AndroidAuthSession> DBApi;

    private final static String PREFERENCES = "Preferences";
    private Button exportButton;
    private EditText initialDate;
    private EditText finalDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_activity);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbarExport));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_export);

        exportButton = (Button) findViewById(R.id.exportButton);
        exportButton.setOnClickListener(exportListener);

        initialDate = (EditText) findViewById(R.id.initialDate);
        initialDate.setOnClickListener(initialDateListener);

        finalDate = (EditText) findViewById(R.id.finalDate);
        finalDate.setOnClickListener(finalDateListener);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        DBApi = new DropboxAPI<AndroidAuthSession>(session);

    }

    protected View.OnClickListener exportListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
            String accessToken = settings.getString("dropboxToken", "");

            if (accessToken == null || accessToken == "") {
                DBApi.getSession().startOAuth2Authentication(ExportActivity.this);
            } else {
                DBApi.getSession().setOAuth2AccessToken(accessToken);
            }

            String[] initialDateDayMonthYear = initialDate.getText().toString().split("/");

            Date initialDate = Date.valueOf(initialDateDayMonthYear[2] + "-" + initialDateDayMonthYear[1] + "-" + initialDateDayMonthYear[0]);

            String[] finalDateDayMonthYear = finalDate.getText().toString().split("/");

            Date finalDate = Date.valueOf(finalDateDayMonthYear[2] + "-" + finalDateDayMonthYear[1] + "-" + finalDateDayMonthYear[0]);

            String separator = ";";
            String empty = "";
            String doubleDecimalPlaceSeparator = ".";
            String excelDecimalPlaceSeparator = ",";

            try {
                for(Date date = initialDate; !date.after(finalDate); date = Util.addDaysToDate(date, 1) ) {
                    MeasurementDAO measurementDAO = new MeasurementDAO(getApplicationContext());
                    ArrayList<Measurement> measurements = measurementDAO.getMeasurements(date);

                    String dateYearMonthDay[] = date.toString().split("-");
                    String filename = dateYearMonthDay[2] + "-" + dateYearMonthDay[1] + "-" + dateYearMonthDay[0] + ".csv";

                    File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + filename);
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut, "UTF-8");

                    String headerLine = "ID" + separator +
                            "Descrição" + separator +
                            "DataHora" + separator +
                            "Intensidade" + separator +
                            "Intensidade de Referência" + separator +
                            "Fator Intensidade" + separator +
                            "Fator Intensidade Corrigido" + separator +
                            "ID Correção" + separator +
                            "ID Medição" + separator +
                            "ID Configuração" + separator +
                            "Nº de Médias" + separator +
                            "Threshold";

                    myOutWriter.append(headerLine);

                    for (int m = 0; m < measurements.size(); m++) {
                        Measurement measurement = measurements.get(m);
                        Configuration configuration = measurement.getConfiguration();
                        Correction correction = measurement.getCorrection();

                        String correctionLine = String.valueOf(correction.getId()) + separator +
                                "Correção" + separator +
                                correction.getDatetime().toString() + separator +
                                String.valueOf(correction.getIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(correction.getReferenceIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(correction.getFactor()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator);

                        myOutWriter.append(System.lineSeparator());
                        myOutWriter.append(correctionLine);

                        String measurementLine = String.valueOf(measurement.getId()) + separator +
                                "Medição" + separator +
                                measurement.getDatetime().toString() + separator +
                                String.valueOf(measurement.getAverageIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(measurement.getAverageReferenceIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(measurement.getAverageFactor()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(measurement.getAverageFactor() / correction.getFactor()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                String.valueOf(measurement.getCorrection().getId()) + separator +
                                empty + separator +
                                String.valueOf(measurement.getConfiguration().getId()) + separator +
                                String.valueOf(measurement.getConfiguration().getNumberAverageMeasure()) + separator +
                                String.valueOf(measurement.getConfiguration().getNumberThreshold());

                        myOutWriter.append(System.lineSeparator());
                        myOutWriter.append(measurementLine);

                        for (int i = 0; i < measurement.getItemsMeasurements().size(); i++) {
                            ItemMeasurement itemMeasurement = measurement.getItemsMeasurements().get(i);

                            String itemMeasurementLine = String.valueOf(itemMeasurement.getId()) + separator +
                                    "Item Medição" + separator +
                                    itemMeasurement.getDatetime().toString() + separator +
                                    String.valueOf(itemMeasurement.getIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                    String.valueOf(itemMeasurement.getReferenceIntensity()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                    String.valueOf(itemMeasurement.getFactor()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                    String.valueOf(itemMeasurement.getFactor() / correction.getFactor()).replace(doubleDecimalPlaceSeparator, excelDecimalPlaceSeparator) + separator +
                                    empty + separator +
                                    String.valueOf(itemMeasurement.getMeasurement().getId());

                            myOutWriter.append(System.lineSeparator());
                            myOutWriter.append(itemMeasurementLine);
                        }

                    }

                    myOutWriter.close();
                    fOut.close();

                    if (DBApi.getSession().isLinked()) {
                        (new UploadFile()).execute(filename);
                    }

                }

                Toast.makeText(getApplicationContext(), R.string.message_ok_exporting_data, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (DBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                DBApi.getSession().finishAuthentication();

                String accessToken = DBApi.getSession().getOAuth2AccessToken();

                SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("dropboxToken", accessToken);
                editor.commit();

                exportButton.performClick();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }


    }

    class UploadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {

                String folderName = "/SmartBioSensor";

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + params[0]);
                FileInputStream inputStream = new FileInputStream(file);

                DropboxAPI.Entry directory = DBApi.metadata(folderName, 1000, null, false, null);

                if(!directory.isDir || directory == null){
                    DBApi.createFolder(folderName);
                }

                DropboxAPI.Entry response = DBApi.putFileOverwrite(folderName + "/" + params[0], inputStream, file.length(), null);
                file.delete();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
            }
            return null;
        }

    }

    protected View.OnClickListener initialDateListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    onInitialDateSetListener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.show(getFragmentManager(), "Datepickerdialog");


        }
    };

    protected DatePickerDialog.OnDateSetListener onInitialDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String date = dayOfMonth + "/" + (monthOfYear+1) + "/" + year;
            initialDate.setText(date);
        }
    };

    protected View.OnClickListener finalDateListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    onFinalDateSetListener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.show(getFragmentManager(), "Datepickerdialog");


        }
    };

    protected DatePickerDialog.OnDateSetListener onFinalDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String date = dayOfMonth + "/" + (monthOfYear+1) + "/" + year;
            finalDate.setText(date);
        }
    };

}

