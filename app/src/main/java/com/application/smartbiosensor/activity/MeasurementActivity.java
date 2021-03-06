package com.application.smartbiosensor.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.smartbiosensor.vo.ProcessResult;
import com.application.smartbiosensor.R;
import com.application.smartbiosensor.database.ConfigurationDAO;
import com.application.smartbiosensor.database.CorrectionDAO;
import com.application.smartbiosensor.database.ItemMeasurementDAO;
import com.application.smartbiosensor.database.MeasurementDAO;
import com.application.smartbiosensor.exception.CameraException;
import com.application.smartbiosensor.exception.ImageProcessingException;
import com.application.smartbiosensor.service.CameraService;
import com.application.smartbiosensor.service.ImageProcessingService;
import com.application.smartbiosensor.util.Util;
import com.application.smartbiosensor.vo.Configuration;
import com.application.smartbiosensor.vo.Correction;
import com.application.smartbiosensor.vo.ItemMeasurement;
import com.application.smartbiosensor.vo.Measurement;

import java.util.ArrayList;

public class MeasurementActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_CODE = 50;
    private static final int REQUEST_EXTERNAL_STORAGE_CODE = 1;
    private Toolbar toolbar;
    private CameraService cameraService;
    private ImageProcessingService imageProcessingService;
    private TextureView textureView;
    private Button measureButton;
    private Button correctionButton;
    private TextView measureIntensity;
    private TextView measureIntensityReference;
    private TextView measureFactor;
    private TextView measureCorrectedFactor;
    private TextView correctionFactor;
    private TextView correctionIntensity;
    private TextView correctionIntensityReference;
    private TextView measureRefractiveIndex;
    private ProgressBar progressBarMeasure;
    private ProgressBar progressBarCorrection;
    private CardView resultMeasure;
    private CardView resultCalibration;
    private int processRequest;
    private Configuration configuration;
    private Measurement measurement;
    private ArrayList<ItemMeasurement> itemsMeasurements;

    private static final int STATE_CORRECTION = 0;
    private static final int STATE_MEASURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        toolbar = (Toolbar) findViewById(R.id.barra_ferramentas);
        setSupportActionBar(toolbar);

        if (!hasFlash() || !hasCamera()) {

            AlertDialog alert = new AlertDialog.Builder(MeasurementActivity.this).create();
            alert.setTitle(R.string.message_error);
            alert.setMessage("Seu dispositivo não possui Flash/Camera.");
                alert.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            alert.show();
            return;
        }

        measureButton = (Button) findViewById(R.id.measureButton);
        measureButton.setOnClickListener(measureListener);

        correctionButton = (Button) findViewById(R.id.correctionButton);
        correctionButton.setOnClickListener(correctionListener);

        measureIntensity = (TextView) findViewById(R.id.measureIntensityTextView);
        measureIntensityReference = (TextView) findViewById(R.id.measureIntensityReferenceTextView);
        measureFactor = (TextView) findViewById(R.id.measureFactorTextView);
        measureCorrectedFactor = (TextView) findViewById(R.id.correctedFactorTextView);
        measureRefractiveIndex = (TextView) findViewById(R.id.measureRefractiveIndexTextView);

        correctionIntensity = (TextView) findViewById(R.id.correctionIntensityTextView);
        correctionIntensityReference = (TextView) findViewById(R.id.correctionIntensityReferenceTextView);
        correctionFactor = (TextView) findViewById(R.id.correctionFactorTextView);

        textureView = (TextureView) findViewById(R.id.textureView);

        progressBarMeasure = (ProgressBar) findViewById(R.id.progressBarMeasure);
        progressBarCorrection = (ProgressBar) findViewById(R.id.progressBarCorrection);

        resultMeasure = (CardView) findViewById(R.id.resultado_medicao);
        resultCalibration = (CardView) findViewById(R.id.resultado_correcao);

        cameraService = new CameraService(this, textureView);
        cameraService.setOnImageAvailableListener(onImageAvailableListener);

        imageProcessingService = new ImageProcessingService();


    }

    @Override
    public void onResume(){
        super.onResume();

        try {

            checkStoragePermissions();

            if (checkCameraPermissions()) {
                cameraService.startBackgroundThread();

                if (textureView.isAvailable()) {
                    cameraService.openCamera();
                } else {
                    textureView.setSurfaceTextureListener(surfaceTextureListener);
                }
            }

            ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());
            configuration = configurationDAO.getActualConfiguration();

        } catch(CameraException e){
            Toast.makeText(getApplicationContext(), e.getDescription(), Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {

            if (cameraService.isCameraOpened()) {
                cameraService.closeCamera();
                cameraService.stopBackgroundThread();
            }

        } catch(CameraException e){
            Toast.makeText(getApplicationContext(), e.getDescription(), Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected View.OnClickListener measureListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            CorrectionDAO correctionDAO = new CorrectionDAO(getApplicationContext());
            Correction correction = correctionDAO.getActualCorrection();

            if (correction == null){
                Toast.makeText(getApplicationContext(), R.string.message_measure_factor_corretion , Toast.LENGTH_SHORT).show();

            }else {

                progressBarMeasure.setVisibility(View.VISIBLE);
                measureButton.setVisibility(View.GONE);
                processRequest = STATE_MEASURE;

                measurement = new Measurement();
                measurement.setConfiguration(configuration);
                measurement.setCorrection(correction);

                MeasurementDAO measurementDAO = new MeasurementDAO(getApplicationContext());
                long idMeasurement = measurementDAO.addMeasurement(measurement);
                measurement.setId(idMeasurement);
                itemsMeasurements = new ArrayList<ItemMeasurement>();

                takePictures();
            }

        }
    };

    protected View.OnClickListener correctionListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            progressBarCorrection.setVisibility(View.VISIBLE);
            correctionButton.setVisibility(View.GONE);
            processRequest = STATE_CORRECTION;
            cameraService.takePicture();

        }
    };

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {

            try{
                cameraService.openCamera();

            } catch(CameraException e){
                Toast.makeText(getApplicationContext(), e.getDescription(), Toast.LENGTH_SHORT).show();
            } catch(Exception e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            try {

                imageProcessingService.setConfiguration(configuration);
                ProcessResult processResult = imageProcessingService.processImage(reader);

                switch(processRequest){
                    case STATE_CORRECTION:{

                        Correction correction = new Correction();
                        correction.setIntensity(processResult.getIntensity());
                        correction.setReferenceIntensity(processResult.getIntensityReference());
                        correction.setConfiguration(configuration);

                        CorrectionDAO correctionDAO = new CorrectionDAO(getApplicationContext());
                        correctionDAO.addCorrection(correction);

                        showCorrectionResults(processResult);
                        break;
                    }
                    case STATE_MEASURE: {

                        ItemMeasurement itemMeasurement = new ItemMeasurement();
                        itemMeasurement.setIntensity(processResult.getIntensity());
                        itemMeasurement.setReferenceIntensity(processResult.getIntensityReference());
                        itemMeasurement.setMeasurement(measurement);

                        ItemMeasurementDAO itemMeasurementDAO = new ItemMeasurementDAO(getApplicationContext());
                        itemMeasurementDAO.addItemMeasurement(itemMeasurement);

                        itemsMeasurements.add(itemMeasurement);

                        if(itemsMeasurements.size() == configuration.getNumberAverageMeasure()) {
                            measurement.setItemsMeasurements(itemsMeasurements);
                            showMeasurementResults();
                        }


                        break;
                    }
                }

            } catch (ImageProcessingException e){
                updateControlsVisibility(false);
                Toast.makeText(getApplicationContext(), e.getDescription(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                updateControlsVisibility(false);
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    };


    private void showMeasurementResults(){

        final ProcessResult processResult = new ProcessResult();

        double averageMeasureFactor = 0;
        double actualCorrectionFactor = 0;

        actualCorrectionFactor = measurement.getCorrection().getFactor();
        averageMeasureFactor = measurement.getAverageIntensity()/measurement.getAverageReferenceIntensity();

        final double averageCorrectedFactor = averageMeasureFactor/actualCorrectionFactor;

        processResult.setIntensity(measurement.getAverageIntensity());
        processResult.setIntensityReference(measurement.getAverageReferenceIntensity());

        this.runOnUiThread(new Runnable() {
            public void run() {

                measureIntensity.setText(String.valueOf(processResult.getIntensityRounded()));
                measureIntensityReference.setText(String.valueOf(processResult.getIntensityReferenceRounded()));
                measureFactor.setText(String.valueOf(processResult.getIntensityFactorRounded()));
                measureCorrectedFactor.setText(String.valueOf(Util.roundDoubleDecimalCases(averageCorrectedFactor, 4)));

                updateControlsVisibility(true);

                final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                ConfigurationDAO configurationDAO = new ConfigurationDAO(getApplicationContext());
                Configuration configuracao = configurationDAO.getActualConfiguration();

                if(configuracao.getCalibration() != null){
                    double refractiveIndex = configuracao.getCalibration().getXGivenYRounded(averageCorrectedFactor);
                    measureRefractiveIndex.setText(String.valueOf(refractiveIndex));
                }

            }
        });
    }


    private void showCorrectionResults(final ProcessResult processResult){


        this.runOnUiThread(new Runnable() {
            public void run() {

                correctionIntensity.setText(String.valueOf(processResult.getIntensityRounded()));
                correctionIntensityReference.setText(String.valueOf(processResult.getIntensityReferenceRounded()));
                correctionFactor.setText(String.valueOf(processResult.getIntensityFactorRounded()));

                updateControlsVisibility(true);

                final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

            }
        });
    }


    public void takePictures() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(int measurementItemCount = 0; measurementItemCount < configuration.getNumberAverageMeasure(); measurementItemCount++) {
                    cameraService.takePicture();
                }
            }

        };
        new Thread(runnable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int
                id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);

            return true;
        }else if(id == R.id.action_export){

            Intent intent = new Intent(this, ExportActivity.class);
            startActivity(intent);

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkStoragePermissions() {
        // Check if we have write permission
        int permissionWrite = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionWrite != PackageManager.PERMISSION_GRANTED || permissionRead != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE_CODE
            );
            return false;
        }else{
            return true;
        }
    }

    private boolean checkCameraPermissions() {

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CODE
            );
            return false;
        }else{
            return true;
        }

    }

    private boolean hasFlash(){
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_CODE: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    finish();
                }

                return;
            }
            case REQUEST_EXTERNAL_STORAGE_CODE: {

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    finish();
                }

                return;
            }

        }
    }

    private void updateControlsVisibility(final boolean success){

        this.runOnUiThread(new Runnable() {
            public void run() {


                switch (processRequest) {
                    case STATE_CORRECTION: {

                        if(success) {
                            resultCalibration.setVisibility(View.VISIBLE);
                        }

                        measureButton.setVisibility(View.VISIBLE);
                        correctionButton.setVisibility(View.VISIBLE);
                        progressBarCorrection.setVisibility(View.GONE);

                        break;
                    }
                    case STATE_MEASURE: {

                        if(success) {
                            resultMeasure.setVisibility(View.VISIBLE);
                        }

                        measureButton.setVisibility(View.VISIBLE);
                        progressBarMeasure.setVisibility(View.GONE);

                        break;
                    }
                }

            }
        });


    }

}