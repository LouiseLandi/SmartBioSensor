package com.application.smartbiosensor.service;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Pair;

import com.application.smartbiosensor.vo.ProcessResult;
import com.application.smartbiosensor.exception.ImageProcessingException;
import com.application.smartbiosensor.util.Util;
import com.application.smartbiosensor.vo.Configuration;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessingService {

    static {
        System.loadLibrary("opencv_java3");
        if (!OpenCVLoader.initDebug()) {
            android.util.Log.e("TAG", "Error");
        }
    }

    private ProcessResult processResult;
    private boolean SAVE_IMAGES_TO_FILES = false;
    private boolean OPTION_AVERAGE_INTENSITY_PER_PIXEL = true;
    private Configuration configuration;

    public ImageProcessingService(){
    }

    private Mat convertBitmapToMat(Bitmap bmp){
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        return mat;
    }

    private Bitmap convertMatToBitmap(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }

    private Mat convertMatToGrayScale(Mat imgToProcess){
        Mat imgToProcessGray = new Mat();
        Imgproc.cvtColor(imgToProcess, imgToProcessGray, Imgproc.COLOR_BGR2GRAY);
        return imgToProcessGray;
    }

    private Bitmap convertImageToBitmap(Image image){

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * image.getWidth();

            Bitmap fileBitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
            fileBitmap.copyPixelsFromBuffer(buffer);

            return fileBitmap;

    }

    public double calculateIntensity(Mat imgToProcess) {

        return Core.sumElems(imgToProcess).val[0];

    }


    private int saturated(Mat mat) {
        int countSaturated = 0;
        for(int i = 0;i < mat.rows();i++) {
           for(int j = 0; j < mat.cols(); j++) {
               if(mat.get(i,j)[0] == 255)
                   countSaturated++;
           }
        }
        return countSaturated;
    }

    private ArrayList<MatOfPoint> detectContoursLightPoints(Mat imgToProcessGray) throws Exception {

        try {
            int threshold = configuration.getNumberThreshold();
            Imgproc.GaussianBlur(imgToProcessGray, imgToProcessGray, new org.opencv.core.Size(5, 5), 0);

            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();


            contours = new ArrayList<MatOfPoint>();
            hierarchy = new Mat();

            //mostrar essa imagem binary
            Imgproc.threshold(imgToProcessGray, imgToProcessGray, threshold, 255, Imgproc.THRESH_BINARY);

            Imgproc.findContours(imgToProcessGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //aparentemente o findContours modifica o source


            if(SAVE_IMAGES_TO_FILES) {
                Bitmap imgContoursDetectedBinaryBitmap = convertMatToBitmap(imgToProcessGray);
                Util.saveBitmapToFile(imgContoursDetectedBinaryBitmap, "picPointsDetectedBinary" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                imgContoursDetectedBinaryBitmap.recycle();
            }

            return contours;

        }catch  (Exception e){
            e.printStackTrace();
            throw e;
        }

    }

    private List<Pair<Integer,Double>> calculateIntensityLightPoints(Mat imgToProcessGray, ArrayList<MatOfPoint> contours, boolean OPTION_AVERAGE_INTENSITY_PER_PIXEL){

        List<Pair<Integer,Double>> lightIntensityPoints = new ArrayList<Pair<Integer,Double>>();;

        try {

            int numberRectangles = 0;
            Double intensity;

            for(MatOfPoint contour: contours) {

                numberRectangles++;

                Mat mask = Mat.zeros(imgToProcessGray.rows(), imgToProcessGray.cols(), CvType.CV_8UC1);
                Imgproc.drawContours(mask, contours, numberRectangles - 1, new Scalar(255), Core.FILLED);

                Mat imgContour = new Mat(imgToProcessGray.rows(), imgToProcessGray.cols(), CvType.CV_8UC1);
                imgContour.setTo(new Scalar(0,0,0));
                imgToProcessGray.copyTo(imgContour, mask);

                Rect rectangle = Imgproc.boundingRect(contour);
                Mat imgRectangle = new Mat(imgContour, rectangle);

                if(SAVE_IMAGES_TO_FILES) {
                    Bitmap fileLightPointsSeparatedBitmap = convertMatToBitmap(imgRectangle);
                    Util.saveBitmapToFile(fileLightPointsSeparatedBitmap, "picCentralPointsSeparated" + CameraService.COUNT_PHOTOS + numberRectangles, CameraService.DIRECTORY);
                    fileLightPointsSeparatedBitmap.recycle();
               }

                intensity = calculateIntensity(imgRectangle);
                //saturated(imgRectangle);

                if(OPTION_AVERAGE_INTENSITY_PER_PIXEL) {
                    int tamanhoOriginal = imgToProcessGray.width() * imgToProcessGray.height();
                    intensity = intensity/tamanhoOriginal;
                }

                lightIntensityPoints.add(new Pair(rectangle.x, intensity));

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return lightIntensityPoints;

    }

    public ProcessResult processImage(ImageReader reader) throws Exception {

        processResult = new ProcessResult();
        Image image = null;
        SAVE_IMAGES_TO_FILES = true;
        try {

            CameraService.COUNT_PHOTOS++;
            image = reader.acquireLatestImage();

            Bitmap imgBitmap = convertImageToBitmap(image);

            Mat imgToProcess;
            imgToProcess = convertBitmapToMat(imgBitmap);

            if(SAVE_IMAGES_TO_FILES) {
                Util.saveBitmapToFile(imgBitmap, "pic" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                imgBitmap.recycle();
            }

            Mat imgToProcessGray = convertMatToGrayScale(imgToProcess);
            Mat imgToProcessGrayOriginal = imgToProcessGray.clone(); //versão que não será alterada de forma alguma

            if(SAVE_IMAGES_TO_FILES) {
                Bitmap imgGrayScaleBitmap = convertMatToBitmap(imgToProcessGray);
                Util.saveBitmapToFile(imgGrayScaleBitmap, "picGrayScale" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                imgGrayScaleBitmap.recycle();
            }

            ArrayList<MatOfPoint> contoursDetected = detectContoursLightPoints(imgToProcessGray);

            if(SAVE_IMAGES_TO_FILES) {
                imgToProcess = imgToProcessGrayOriginal;
                Imgproc.drawContours(imgToProcess, contoursDetected, -1, new Scalar(0, 255, 0), 3);
                Bitmap imgContoursDetectedBitmap = convertMatToBitmap(imgToProcess);
                Util.saveBitmapToFile(imgContoursDetectedBitmap, "picPointsDetected" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                imgContoursDetectedBitmap.recycle();
            }

            if(contoursDetected.size() != 2) { //É necessário que a luz de referência e de medição sejam detectadas. Nada a mais nem a menos.
                throw new ImageProcessingException("Não é possível continuar o processamento. Número inválido de fontes de luz detectadas:" + String.valueOf(contoursDetected.size()));
            }

            List<Pair<Integer,Double>> lightIntensityPoints = calculateIntensityLightPoints(imgToProcessGrayOriginal, contoursDetected, OPTION_AVERAGE_INTENSITY_PER_PIXEL);

            if (lightIntensityPoints.get(0).first < lightIntensityPoints.get(1).first) {
                processResult.setIntensityReference(lightIntensityPoints.get(0).second);
                processResult.setIntensity(lightIntensityPoints.get(1).second);
            } else {
                processResult.setIntensityReference(lightIntensityPoints.get(1).second);
                processResult.setIntensity(lightIntensityPoints.get(0).second);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(image != null) {
                image.close();
            }
        }

        return processResult;

    }

    public void set_SAVE_IMAGES_TO_FILES(Boolean SAVE_IMAGES_TO_FILES){
        this.SAVE_IMAGES_TO_FILES = SAVE_IMAGES_TO_FILES;
    }

    public void set_AVERAGE_INTENSITY_PER_PIXEL(Boolean OPTION_AVERAGE_INTENSITY_PER_PIXEL){
        this.OPTION_AVERAGE_INTENSITY_PER_PIXEL = OPTION_AVERAGE_INTENSITY_PER_PIXEL;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

}
