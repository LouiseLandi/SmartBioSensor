package com.application.smartbiosensor.vo;

import java.sql.Timestamp;

public class Correction {

    private long id;
    private double intensity;
    private double referenceIntensity;
    private Timestamp Datetime;
    private Configuration configuration;

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

    public double getFactor(){
        return getIntensity()/getReferenceIntensity();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
