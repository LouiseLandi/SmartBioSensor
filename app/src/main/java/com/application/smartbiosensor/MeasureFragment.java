package com.application.smartbiosensor;

import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.application.smartbiosensor.database.ConfigurationDAO;
import com.application.smartbiosensor.database.CorrectionDAO;
import com.application.smartbiosensor.database.ItemMeasurementDAO;
import com.application.smartbiosensor.database.MeasurementDAO;
import com.application.smartbiosensor.vo.*;
import com.application.smartbiosensor.vo.Configuration;

import java.util.ArrayList;

public class MeasureFragment extends Fragment {

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
    private com.application.smartbiosensor.vo.Configuration configuration;
    private Measurement measurement;
    private ArrayList<ItemMeasurement> itemsMeasurements;

    private static final int STATE_CORRECTION = 0;
    private static final int STATE_MEASURE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_medicao, container, false);

        measureButton = (Button) view.findViewById(R.id.measureButton);
        measureButton.setOnClickListener(measureListener);

        correctionButton = (Button) view.findViewById(R.id.correctionButton);
        correctionButton.setOnClickListener(referenceListener);

        measureIntensity = (TextView) view.findViewById(R.id.measureIntensityTextView);
        measureIntensityReference = (TextView) view.findViewById(R.id.measureIntensityReferenceTextView);
        measureFactor = (TextView) view.findViewById(R.id.measureFactorTextView);

        correctionIntensity = (TextView) view.findViewById(R.id.correctionIntensityTextView);
        correctionIntensityReference = (TextView) view.findViewById(R.id.correctionIntensityReferenceTextView);
        correctionFactor = (TextView) view.findViewById(R.id.correctionFactorTextView);

        measureCorrectedFactor = (TextView) view.findViewById(R.id.correctedFactorTextView);

        measureRefractiveIndex = (TextView) view.findViewById(R.id.measureRefractiveIndexTextView);

        textureView = (TextureView) view.findViewById(R.id.textureView);

        progressBarMeasure = (ProgressBar) view.findViewById(R.id.progressBarMeasure);
        progressBarCorrection = (ProgressBar) view.findViewById(R.id.progressBarCorrection);

        resultMeasure = (CardView) view.findViewById(R.id.resultado_medicao);
        resultCalibration = (CardView) view.findViewById(R.id.resultado_correcao);

        cameraService = new CameraService(getActivity(), textureView);
        cameraService.setOnImageAvailableListener(onImageAvailableListener);

        imageProcessingService = new ImageProcessingService();

        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        cameraService.startBackgroundThread();

        if (textureView.isAvailable()) {
            cameraService.openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }

        ConfigurationDAO configurationDAO = new ConfigurationDAO(getContext());
        configuration =  configurationDAO.getActualConfiguration();
    }

    @Override
    public void onPause() {
        cameraService.closeCamera();
        cameraService.stopBackgroundThread();
        super.onPause();
    }


    protected View.OnClickListener measureListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            progressBarMeasure.setVisibility(View.VISIBLE);
            measureButton.setVisibility(View.GONE);
            processRequest = STATE_MEASURE;

            CorrectionDAO correctionDAO = new CorrectionDAO(getContext());
            Correction correction = correctionDAO.getActualCorrection();

            measurement = new Measurement();
            measurement.setConfiguration(configuration);
            measurement.setCorrection(correction);

            MeasurementDAO measurementDAO = new MeasurementDAO(getContext());
            long idMeasurement = measurementDAO.addMeasurement(measurement);
            measurement.setId(idMeasurement);
            itemsMeasurements =  new ArrayList<ItemMeasurement>();

            takePictures();

        }
    };

    protected View.OnClickListener referenceListener = new View.OnClickListener() {

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

            cameraService.openCamera();

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

                        CorrectionDAO correctionDAO = new CorrectionDAO(getContext());
                        correctionDAO.addCorrection(correction);

                        showCorrectionResults(processResult);
                        break;
                    }
                    case STATE_MEASURE: {

                        ItemMeasurement itemMeasurement = new ItemMeasurement();
                        itemMeasurement.setIntensity(processResult.getIntensity());
                        itemMeasurement.setReferenceIntensity(processResult.getIntensityReference());
                        itemMeasurement.setMeasurement(measurement);

                        ItemMeasurementDAO itemMeasurementDAO = new ItemMeasurementDAO(getContext());
                        itemMeasurementDAO.addItemMeasurement(itemMeasurement);

                        itemsMeasurements.add(itemMeasurement);

                        if(itemsMeasurements.size() == configuration.getNumberAverageMeasure()) {
                            measurement.setItemsMeasurements(itemsMeasurements);
                            showMeasurementResults(processResult);
                        }


                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private void showMeasurementResults(final ProcessResult processResult){


        double averageMeasureFactor = 0;
        double actualCorrectionFactor = 0;

        actualCorrectionFactor = measurement.getCorrection().getFactor();
        averageMeasureFactor = measurement.getAverageIntensity()/measurement.getAverageReferenceIntensity();

        final double averageCorrectedFactor = averageMeasureFactor/actualCorrectionFactor;

        processResult.setIntensity(measurement.getAverageIntensity());
        processResult.setIntensityReference(measurement.getAverageReferenceIntensity());

        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                measureIntensity.setText(String.valueOf(processResult.getIntensityRounded()));
                measureIntensityReference.setText(String.valueOf(processResult.getIntensityReferenceRounded()));
                measureFactor.setText(String.valueOf(processResult.getIntensityFactorRounded()));
                measureCorrectedFactor.setText(String.valueOf(averageCorrectedFactor));

                resultMeasure.setVisibility(View.VISIBLE);
                measureButton.setVisibility(View.VISIBLE);
                progressBarMeasure.setVisibility(View.GONE);

                final ScrollView scrollview = ((ScrollView) getView().findViewById(R.id.scrollView));
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                //TRATAR CASO NÃO TENHA NENHUMA CALIBRAÇÃO AINDA
                ConfigurationDAO configurationDAO = new ConfigurationDAO(getContext());
                Configuration configuracao = configurationDAO.getActualConfiguration();

                if(configuracao.getCalibration() != null){
                    double refractiveIndex = configuracao.getCalibration().getXGivenYRounded(averageCorrectedFactor);
                    measureRefractiveIndex.setText(String.valueOf(refractiveIndex));
                }

            }
        });
    }


    private void showCorrectionResults(final ProcessResult processResult){


        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                correctionIntensity.setText(String.valueOf(processResult.getIntensityRounded()));
                correctionIntensityReference.setText(String.valueOf(processResult.getIntensityReferenceRounded()));
                correctionFactor.setText(String.valueOf(processResult.getIntensityFactorRounded()));

                resultCalibration.setVisibility(View.VISIBLE);
                measureButton.setVisibility(View.VISIBLE);
                correctionButton.setVisibility(View.VISIBLE);
                progressBarCorrection.setVisibility(View.GONE);

                final ScrollView scrollview = ((ScrollView) getView().findViewById(R.id.scrollView));
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



}

