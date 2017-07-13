package com.application.smartbiosensor.vo;

import java.sql.Timestamp;

public class Configuration {

    private long id;
    private int numberAverageMeasure;
    private int numberThreshold;
    private Calibration calibration;
    private Timestamp datetime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumberAverageMeasure() {
        return numberAverageMeasure;
    }

    public void setNumberAverageMeasure(int numberAverageMeasure) {
        this.numberAverageMeasure = numberAverageMeasure;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public int getNumberThreshold() {
        return numberThreshold;
    }

    public void setNumberThreshold(int threshold) {
        this.numberThreshold = threshold;
    }

    public Calibration getCalibration() {
        return calibration;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }
}
