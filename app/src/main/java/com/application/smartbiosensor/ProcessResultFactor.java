package com.application.smartbiosensor;

public class ProcessResultFactor {

    private double factor;
    private double factorMeasure;
    private double factorReference;
    private int numberDecimalPlaces = 4;

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getFactorMeasure() {
        return factorMeasure;
    }

    public void setFactorMeasure(double factorMeasure) {
        this.factorMeasure = factorMeasure;
    }

    public double getFactorReference() {
        return factorReference;
    }

    public void setFactorReference(double factorReference) {
        this.factorReference = factorReference;
    }

    public double getFactorRounded() {
        return Util.roundDoubleDecimalCases(getFactor(), numberDecimalPlaces);
    }

    public double getFactorMeasureRounded() {
        return Util.roundDoubleDecimalCases(getFactorMeasure(), numberDecimalPlaces);
    }

    public double getFactorReferenceRounded() {
        return Util.roundDoubleDecimalCases(getFactorReference(), numberDecimalPlaces);
    }

    public void setNumberDecimalPlaces(int numberDecimalPlaces){
        this.numberDecimalPlaces = numberDecimalPlaces;
    }

}
