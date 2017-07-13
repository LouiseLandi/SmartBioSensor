package com.application.smartbiosensor.vo;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Measurement {

    private long id;
    private Timestamp Datetime;
    private Correction correction;
    private Configuration configuration;
    ArrayList<ItemMeasurement> itemsMeasurements;
    private double averageIntensity;
    private double averageReferenceIntensity;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getDatetime() {
        return Datetime;
    }

    public void setDatetime(Timestamp datetime) {
        Datetime = datetime;
    }

    public Correction getCorrection() {
        return correction;
    }

    public void setCorrection(Correction correction) {
        this.correction = correction;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ArrayList<ItemMeasurement> getItemsMeasurements() {
        return itemsMeasurements;
    }

    public void setItemsMeasurements(ArrayList<ItemMeasurement> itemsMeasurements) {
        this.itemsMeasurements = itemsMeasurements;

        if (itemsMeasurements.size() > 0) {

            double averageIntensity = 0, averageReferenceIntensity = 0;

            for (int item = 0; item < itemsMeasurements.size(); item++) {
                averageIntensity = averageIntensity + itemsMeasurements.get(item).getIntensity();
                averageReferenceIntensity = averageReferenceIntensity + itemsMeasurements.get(item).getReferenceIntensity();
            }

            averageIntensity = averageIntensity / itemsMeasurements.size();
            averageReferenceIntensity = averageReferenceIntensity / itemsMeasurements.size();

            setAverageIntensity(averageIntensity);
            setAverageReferenceIntensity(averageReferenceIntensity);
        }
    }

    public void setAverageIntensity(double averageIntensity){
        this.averageIntensity = averageIntensity;
    }

    public double getAverageIntensity(){
        return this.averageIntensity;
    }

    public void setAverageReferenceIntensity(double averageReferenceIntensity){
        this.averageReferenceIntensity = averageReferenceIntensity;
    }

    public double getAverageReferenceIntensity(){
        return this.averageReferenceIntensity;
    }

    public double getAverageFactor(){
        return getAverageIntensity()/getAverageReferenceIntensity();
    }
}
