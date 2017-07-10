package com.application.smartbiosensor.vo;

import com.application.smartbiosensor.MeasureFragment;

import java.sql.Timestamp;

public class ItemMeasurement {

    private long id;
    private double intensity;
    private double referenceIntensity;
    private Timestamp Datetime;
    private Measurement measurement;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getReferenceIntensity() {
        return referenceIntensity;
    }

    public void setReferenceIntensity(double referenceIntensity) {
        this.referenceIntensity = referenceIntensity;
    }

    public Timestamp getDatetime() {
        return Datetime;
    }

    public void setDatetime(Timestamp datetime) {
        Datetime = datetime;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    public double getFactor(){
        return getIntensity()/getReferenceIntensity();
    }

}
