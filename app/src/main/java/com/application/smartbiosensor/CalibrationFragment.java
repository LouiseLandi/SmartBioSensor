package com.application.smartbiosensor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.androidplot.xy.XYPlot;
import java.util.ArrayList;

public class CalibrationFragment extends Fragment {

    private CameraService cameraService;
    private ImageProcessingService imageProcessingService;
    private RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private static CalibrationAdapter calibrationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cameraService = new CameraService(getActivity(), null);
        imageProcessingService = new ImageProcessingService();

        recyclerView = (RecyclerView) inflater.inflate(R.layout.tab_calibracao, container, false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<CalibrationItem> calibrationItems = new ArrayList<CalibrationItem>();
        calibrationItems.add(new CalibrationItem("Água Pura", 1.3321));
        calibrationItems.add(new CalibrationItem("15% Sacarose", 1.3519));
        calibrationItems.add(new CalibrationItem("25% Sacarose", 1.3618));
        calibrationItems.add(new CalibrationItem("30% Sacarose", 1.3688));
        calibrationItems.add(new CalibrationItem("45% Sacarose", 1.3840));
        calibrationItems.add(new CalibrationItem("52% Sacarose", 1.3880));

        calibrationAdapter = new CalibrationAdapter(this.getActivity(), calibrationItems, cameraService, imageProcessingService);
        recyclerView.setAdapter(calibrationAdapter);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraService.openCamera();
        cameraService.startBackgroundThread();
    }

    @Override
    public void onPause() {
        cameraService.closeCamera();
        cameraService.stopBackgroundThread();
        super.onPause();
    }


    public static ArrayList<Pair<Double, Double>> getRefractiveIndexAndIntensityFactorResults(){

        ArrayList<Pair<Double, Double>> refractiveIndexAndFactor = new ArrayList<Pair<Double, Double>>();
        ArrayList<RecyclerView.ViewHolder> allBoundViewHolders = calibrationAdapter.getAllBoundViewHolders();

        for (int i = 0; i < allBoundViewHolders.size(); i++) {
            RecyclerView.ViewHolder holder = allBoundViewHolders.get(i);

            if (holder instanceof ViewHolderItemCalibration) {
                ViewHolderItemCalibration viewHolderItem;
                viewHolderItem = (ViewHolderItemCalibration) holder;

                if (viewHolderItem.processMeasureAverageResult != null){
                    double refractiveIndex = Double.valueOf(viewHolderItem.refractiveIndex.getText().toString());
                    double factor = viewHolderItem.processMeasureAverageResult.getFactor();
                    refractiveIndexAndFactor.add(new Pair<Double, Double>(refractiveIndex, factor));
                }

            }

        }

        return refractiveIndexAndFactor;

    }


}

