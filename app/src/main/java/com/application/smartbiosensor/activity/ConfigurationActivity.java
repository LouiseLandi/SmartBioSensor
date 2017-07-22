package com.application.smartbiosensor.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.application.smartbiosensor.R;
import com.application.smartbiosensor.database.CalibrationDAO;
import com.application.smartbiosensor.database.ConfigurationDAO;
import com.application.smartbiosensor.vo.Calibration;

public class ConfigurationActivity extends AppCompatActivity {

    private EditText numberAverageMeasure;
    private EditText numberThreshold;
    private EditText calibrationA;
    private EditText calibrationB;
    private EditText calibrationR2;
    private com.application.smartbiosensor.vo.Configuration actualConfiguration;
    private Button saveConfigurationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbarConfiguration));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_configuration);


        numberAverageMeasure = (EditText) findViewById(R.id.numberAverageMeasure);
        numberThreshold = (EditText) findViewById(R.id.numberThreshold);

        calibrationA = (EditText) findViewById(R.id.calibrationA);
        calibrationB = (EditText) findViewById(R.id.calibrationB);
        calibrationR2 = (EditText) findViewById(R.id.calibrationR2);

        saveConfigurationButton = (Button) findViewById(R.id.buttonSaveConfiguration);
        saveConfigurationButton.setOnClickListener(saveConfigurationListener);

        ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());
        actualConfiguration = configurationDAO.getActualConfiguration();

        numberAverageMeasure.setText(String.valueOf(actualConfiguration.getNumberAverageMeasure()));
        numberThreshold.setText(String.valueOf(actualConfiguration.getNumberThreshold()));

        calibrationA.setText(String.valueOf(actualConfiguration.getCalibration().getARounded()));
        calibrationB.setText(String.valueOf(actualConfiguration.getCalibration().getBRounded()));
        calibrationR2.setText(String.valueOf(actualConfiguration.getCalibration().getR2Rounded()));
    }

    protected View.OnClickListener saveConfigurationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            com.application.smartbiosensor.vo.Configuration configuration = new com.application.smartbiosensor.vo.Configuration();
            configuration.setNumberAverageMeasure(Integer.parseInt(numberAverageMeasure.getText().toString()));
            configuration.setNumberThreshold(Integer.parseInt(numberThreshold.getText().toString()));

            double a = Double.parseDouble(calibrationA.getText().toString());
            double b = Double.parseDouble(calibrationB.getText().toString());
            double r2 = Double.parseDouble(calibrationR2.getText().toString());
            Calibration calibration = actualConfiguration.getCalibration();

            if(a != actualConfiguration.getCalibration().getARounded() || b != actualConfiguration.getCalibration().getBRounded() || r2 != actualConfiguration.getCalibration().getR2Rounded())
            {
                CalibrationDAO calibrationDAO = new CalibrationDAO(getApplicationContext());
                calibration = new Calibration(a, b, r2);
                long calibrationId = calibrationDAO.addCalibration(calibration);
                calibration.setId(calibrationId);
            }

            configuration.setCalibration(calibration);

            ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());
            boolean configurationAdded = configurationDAO.addConfiguration(configuration);

            if(configurationAdded){
                Toast.makeText(getApplicationContext(), R.string.message_ok_saving_configuration, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), R.string.message_error_saving_configuration, Toast.LENGTH_SHORT).show();
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


}
