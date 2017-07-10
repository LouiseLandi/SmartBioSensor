package com.application.smartbiosensor;

public class CalibrationItem {

    private String medium;
    private double refractiveIndex;

    public CalibrationItem(String medium, double refractiveIndex){
        this.medium = medium;
        this.refractiveIndex = refractiveIndex;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public void setRefractiveIndex(double refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

}
