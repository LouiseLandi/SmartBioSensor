package com.application.smartbiosensor.vo;


import com.application.smartbiosensor.LinearRegression;
import com.application.smartbiosensor.LinearRegressionResult;
import com.application.smartbiosensor.Util;

import java.sql.Timestamp;

public class Calibration {

    private long id;
    private Timestamp datetime;
    private LinearRegressionResult linearRegressionResult;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public LinearRegressionResult getLinearRegressionResult() {
        return linearRegressionResult;
    }

    public void setLinearRegressionResult(LinearRegressionResult linearRegressionResult) {
        this.linearRegressionResult = linearRegressionResult;
    }

}
