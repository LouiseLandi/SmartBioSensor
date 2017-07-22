package com.application.smartbiosensor.exception;

public class ImageProcessingException extends Exception{

    private String description;

    public ImageProcessingException(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

}
