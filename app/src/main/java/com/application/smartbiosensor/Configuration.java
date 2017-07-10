package com.application.smartbiosensor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.application.smartbiosensor.database.ConfigurationDAO;

public class Configuration extends AppCompatActivity {

    private EditText numberAverageMeasure;
    private EditText numberAverageCalibration;
    private EditText numberThreshold;
    private Button saveConfigurationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbarConfiguration));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_configuration);


        numberAverageMeasure = (EditText) findViewById(R.id.numberAverageMeasure);
        numberAverageCalibration = (EditText) findViewById(R.id.numberAverageCalibration);
        numberThreshold = (EditText) findViewById(R.id.numberThreshold);

        saveConfigurationButton = (Button) findViewById(R.id.buttonSaveConfiguration);
        saveConfigurationButton.setOnClickListener(saveConfigurationListener);

        ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());
        com.application.smartbiosensor.vo.Configuration configuration = configurationDAO.getActualConfiguration();

        numberAverageCalibration.setText(String.valueOf(configuration.getNumberAverageCalibration()));
        numberAverageMeasure.setText(String.valueOf(configuration.getNumberAverageMeasure()));
        numberThreshold.setText(String.valueOf(configuration.getNumberThreshold()));
    }

    protected View.OnClickListener saveConfigurationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {



            ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());

            com.application.smartbiosensor.vo.Configuration configuration = new com.application.smartbiosensor.vo.Configuration();
            configuration.setNumberAverageMeasure(Integer.parseInt(numberAverageMeasure.getText().toString()));
            configuration.setNumberAverageCalibration(Integer.parseInt(numberAverageCalibration.getText().toString()));
            configuration.setNumberThreshold(Integer.parseInt(numberThreshold.getText().toString()));

            configurationDAO.addConfiguration(configuration);

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
