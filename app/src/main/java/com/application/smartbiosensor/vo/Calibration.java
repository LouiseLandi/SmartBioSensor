package com.application.smartbiosensor.vo;

import com.application.smartbiosensor.Util;
import java.sql.Timestamp;

public class Calibration {

    private long id;
    private Timestamp datetime;
    private double a;
    private double b;
    private double r2;
    private int numberDecimalPlaces = 4;

    public Calibration(double a, double b, double r2){
        this.a = a;
        this.b = b;
        this.r2 = r2;
    }

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

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getR2() {
        return r2;
    }

    public void setR2(double r2) {
        this.r2 = r2;
    }


    public double getARounded() {
        return Util.roundDoubleDecimalCases(getA(),numberDecimalPlaces);
    }

    public double getBRounded() {
        return Util.roundDoubleDecimalCases(getB(), numberDecimalPlaces);
    }

    public double getR2Rounded() {
        return Util.roundDoubleDecimalCases(getR2(),numberDecimalPlaces);
    }

    public void setNumberDecimalPlaces(int numberDecimalPlaces){
        this.numberDecimalPlaces = numberDecimalPlaces;
    }

    public double getXGivenY(double y){
        return (y - getB())/getA();
    }

    public double getXGivenYRounded(double y){
        return Util.roundDoubleDecimalCases(getXGivenY(y), numberDecimalPlaces);
    }

    public double getYGivenX(double x){
        return ((getA()*x) + getB());
    }


}
