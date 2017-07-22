package com.application.smartbiosensor.exception;

public class CameraException extends Exception{

    private String description;

    public CameraException(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

}
