package com.application.smartbiosensor;


public class ProcessResult {

    private double intensity;
    private double intensityReference;
    private int numberDecimalPlaces = 4;

    public ProcessResult(){

    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getIntensityReference() {
        return intensityReference;
    }

    public void setIntensityReference(double intensityReference) {
        this.intensityReference = intensityReference;
    }

    public double getIntensityFactor(){
        return getIntensity()/getIntensityReference();
    }

    public double getIntensityReferenceRounded() {
        return Util.roundDoubleDecimalCases(getIntensityReference(), numberDecimalPlaces);
    }

    public double getIntensityFactorRounded(){
        return Util.roundDoubleDecimalCases(getIntensityFactor(), numberDecimalPlaces);
    }

    public double getIntensityRounded() {
        return Util.roundDoubleDecimalCases(getIntensity(), numberDecimalPlaces);
    }

    public void setNumberDecimalPlaces(int numberDecimalPlaces){
        this.numberDecimalPlaces = numberDecimalPlaces;
    }

}
