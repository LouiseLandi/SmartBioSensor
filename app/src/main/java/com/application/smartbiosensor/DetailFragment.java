package com.application.smartbiosensor;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DetailFragment extends Fragment {

    private ImageView imageMeasure;
    private ImageView imageGreyscale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.tab_detalhe, container, false);

        imageMeasure = (ImageView) view.findViewById(R.id.image_measure);
        imageMeasure.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmartBioSensorFolder/pic1.jpg"));

        imageGreyscale = (ImageView) view.findViewById(R.id.image_greyscale);
        imageGreyscale.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmartBioSensorFolder/picGrayScale1.jpg"));

        return view;
    }

}

