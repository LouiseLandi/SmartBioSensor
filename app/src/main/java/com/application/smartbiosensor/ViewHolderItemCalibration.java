package com.application.smartbiosensor;

import android.app.Activity;
import android.content.Context;
import android.media.ImageReader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.smartbiosensor.service.CameraService;
import com.application.smartbiosensor.service.ImageProcessingService;

import java.util.ArrayList;

public class ViewHolderItemCalibration extends RecyclerView.ViewHolder {

    private CameraService cameraService;
    private ImageProcessingService imageProcessingService;
    private Context context;
    protected TextView medium;
    protected TextView refractiveIndex;
    protected TextView mediumTitle;
    protected TextView refractiveIndexTitle;
    protected TextView factorReference;
    protected TextView factorReferenceTitle;
    protected TextView factorMeasure;
    protected TextView factorMeasureTitle;
    protected TextView factor;
    protected TextView factorTitle;
    protected Button measureButton;
    protected ImageButton checkButton;
    protected ProgressBar progressBar;
    public static ArrayList<ProcessResult> processMeasureResults;
    protected ProcessResultFactor processMeasureAverageResult;
    private int numberAverage = 10;

    public ViewHolderItemCalibration(LayoutInflater inflater, ViewGroup parent, Context context, CameraService cameraService, ImageProcessingService imageProcessingService) {
        super(inflater.inflate(R.layout.item_calibracao, parent, false));

        this.context = context;
        this.cameraService = cameraService;
        this.imageProcessingService = imageProcessingService;

        medium = (TextView) itemView.findViewById(R.id.mediumItemCalibration);
        refractiveIndex = (TextView) itemView.findViewById(R.id.refractiveIndexItemCalibration);
        factorReference = (TextView) itemView.findViewById(R.id.factorReferenceItemCalibration);
        factorMeasure = (TextView) itemView.findViewById(R.id.factorMeasureItemCalibration);
        factor = (TextView) itemView.findViewById(R.id.factorItemCalibration);

        mediumTitle = (TextView) itemView.findViewById(R.id.mediumItemCalibrationTitle);
        refractiveIndexTitle = (TextView) itemView.findViewById(R.id.refractiveIndexItemCalibrationTitle);
        factorReferenceTitle = (TextView) itemView.findViewById(R.id.factorReferenceItemCalibrationTitle);
        factorMeasureTitle = (TextView) itemView.findViewById(R.id.factorMeasureItemCalibrationTitle);
        factorTitle = (TextView) itemView.findViewById(R.id.factorItemCalibrationTitle);

        measureButton = (Button) itemView.findViewById(R.id.buttonMeasureItemCalibration);
        measureButton.setOnClickListener(measureListener);

        checkButton = (ImageButton) itemView.findViewById(R.id.buttonCheckItemCalibration);

        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBarItemCalibration);


    }


    protected View.OnClickListener measureListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            try {
                cameraService.setOnImageAvailableListener(onImageAvailableListener);

                progressBar.setVisibility(View.VISIBLE);
                checkButton.setVisibility(View.GONE);

                processMeasureResults = new ArrayList<ProcessResult>();

                for(int number = 0; number < numberAverage; number++){
                    cameraService.takePicture();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    public final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            try {

                ProcessResult processResult = imageProcessingService.processImage(reader);

                processMeasureResults.add(processResult);

                if(processMeasureResults.size() == numberAverage){

                    processMeasureAverageResult = new ProcessResultFactor();

                    showMeasureResults();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void showMeasureResults(){

        double averageMeasureFactor = 0;
        double actualReferenceFactor = 0;
        double averageFactor = 0;

        actualReferenceFactor = ViewHolderHeaderCalibration.processReferenceResult.getIntensityFactor();

        for(int number = 0; number < processMeasureResults.size(); number++){
            averageMeasureFactor = averageMeasureFactor + processMeasureResults.get(number).getIntensityFactor();
        }

        averageMeasureFactor = averageMeasureFactor/processMeasureResults.size();
        averageFactor = averageMeasureFactor/actualReferenceFactor;

        processMeasureAverageResult.setFactorMeasure(averageMeasureFactor);
        processMeasureAverageResult.setFactorReference(actualReferenceFactor);
        processMeasureAverageResult.setFactor(averageFactor);

        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {

                factor.setText(String.valueOf(processMeasureAverageResult.getFactorRounded()));
                factorReference.setText(String.valueOf(processMeasureAverageResult.getFactorReferenceRounded()));
                factorMeasure.setText(String.valueOf(processMeasureAverageResult.getFactorMeasureRounded()));

                checkButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                factor.setVisibility(View.VISIBLE);
                factorTitle.setVisibility(View.VISIBLE);
                factorReference.setVisibility(View.VISIBLE);
                factorReferenceTitle.setVisibility(View.VISIBLE);
                factorMeasure.setVisibility(View.VISIBLE);
                factorMeasureTitle.setVisibility(View.VISIBLE);

            }
        });
    }

}
