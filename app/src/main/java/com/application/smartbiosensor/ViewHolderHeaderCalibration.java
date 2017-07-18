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

class ViewHolderHeaderCalibration extends RecyclerView.ViewHolder {

    private CameraService cameraService;
    private ImageProcessingService imageProcessingService;
    private Context context;
    public static ProcessResult processReferenceResult;
    protected TextView factor;
    protected TextView factorTitle;
    protected Button measureButton;
    protected ImageButton checkButton;
    protected ProgressBar progressBar;

    public ViewHolderHeaderCalibration(LayoutInflater inflater, ViewGroup parent, Context context, CameraService cameraService, ImageProcessingService imageProcessingService) {
        super(inflater.inflate(R.layout.calibracao_header, parent, false));

        this.context = context;
        this.cameraService = cameraService;
        this.imageProcessingService = imageProcessingService;

        factor = (TextView) itemView.findViewById(R.id.factorReferenceCalibration);
        factorTitle = (TextView) itemView.findViewById(R.id.factorReferenceCalibrationTitle);

        measureButton = (Button) itemView.findViewById(R.id.buttonMeasureReferenceCalibration);
        measureButton.setOnClickListener(measureListener);

        checkButton = (ImageButton) itemView.findViewById(R.id.buttonCheckReferenceCalibration);

        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBarReferenceCalibration);

    }

    protected View.OnClickListener measureListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            try {

                cameraService.setOnImageAvailableListener(onImageAvailableListener);

                progressBar.setVisibility(View.VISIBLE);
                checkButton.setVisibility(View.GONE);

                cameraService.takePicture();


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

                processReferenceResult = processResult;
                showReferenceResults();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void showReferenceResults(){

        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {

                factor.setText(String.valueOf(processReferenceResult.getIntensityFactorRounded()));
                factorTitle.setVisibility(View.VISIBLE);
                factor.setVisibility(View.VISIBLE);

                checkButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }
        });
    }


}