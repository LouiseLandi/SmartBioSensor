package com.application.smartbiosensor;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.application.smartbiosensor.database.MeasurementDAO;
import com.application.smartbiosensor.vo.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Export extends AppCompatActivity {

    private Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export);

        exportButton = (Button) findViewById(R.id.exportButton);
        exportButton.setOnClickListener(exportListener);
    }

    protected View.OnClickListener exportListener = new View.OnClickListener() {

        @Override

        public void onClick(View v) {

            MeasurementDAO measurementDAO = new MeasurementDAO(getApplicationContext());
            ArrayList<Measurement> measurements = measurementDAO.getMeasurements();
            String separator = ";";
            String empty = "";

            try {
                File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/teste.csv");
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

                for(int m = 0; m < measurements.size(); m++){
                    Measurement measurement = measurements.get(m);
                    com.application.smartbiosensor.vo.Configuration configuration = measurement.getConfiguration();
                    Correction correction = measurement.getCorrection();

                    String correctionLine = String.valueOf(correction.getId()) + separator +
                                            "Correção" + separator +
                                            correction.getDatetime().toString() + separator +
                                            String.valueOf(correction.getIntensity()) + separator +
                                            String.valueOf(correction.getReferenceIntensity()) + separator +
                                            String.valueOf(correction.getIntensity()/correction.getReferenceIntensity());

                    myOutWriter.append(System.lineSeparator());
                    myOutWriter.append(correctionLine);

                    String measurementLine = String.valueOf(measurement.getId()) + separator +
                                             "Medição" + separator +
                                             measurement.getDatetime().toString() + separator +
                                             String.valueOf(measurement.getAverageIntensity()) + separator +
                                             String.valueOf(measurement.getAverageReferenceIntensity()) + separator +
                                             String.valueOf(measurement.getAverageIntensity()/measurement.getAverageReferenceIntensity()) + separator +
                                             String.valueOf((measurement.getAverageIntensity()/measurement.getAverageReferenceIntensity())/correction.getFactor()) + separator +
                                             String.valueOf(measurement.getCorrection().getId()) + separator +
                                             empty + separator +
                                             String.valueOf(measurement.getConfiguration().getId()) + separator +
                                             String.valueOf(measurement.getConfiguration().getNumberAverageMeasure()) + separator +
                                             String.valueOf(measurement.getConfiguration().getNumberThreshold());

                    myOutWriter.append(System.lineSeparator());
                    myOutWriter.append(measurementLine);

                    for(int i = 0; i < measurement.getItemsMeasurements().size(); i++){
                        ItemMeasurement itemMeasurement = measurement.getItemsMeasurements().get(i);

                        String itemMeasurementLine = String.valueOf(itemMeasurement.getId()) + separator +
                                                     "Item Medição" + separator +
                                                     itemMeasurement.getDatetime().toString() + separator +
                                                     String.valueOf(itemMeasurement .getIntensity()) + separator +
                                                     String.valueOf(itemMeasurement.getReferenceIntensity()) + separator +
                                                     String.valueOf(itemMeasurement.getIntensity()/itemMeasurement.getReferenceIntensity()) + separator +
                                                     String.valueOf((itemMeasurement.getIntensity()/itemMeasurement.getReferenceIntensity())/correction.getFactor()) + separator +
                                                     empty + separator +
                                                     String.valueOf(itemMeasurement.getMeasurement().getId());

                        myOutWriter.append(System.lineSeparator());
                        myOutWriter.append(itemMeasurementLine);
                    }

                }

                myOutWriter.close();
                fOut.close();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
            }

        }
    };

}
