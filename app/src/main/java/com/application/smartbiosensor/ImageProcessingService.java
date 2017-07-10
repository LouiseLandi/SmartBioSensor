package com.application.smartbiosensor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.util.Pair;
import com.application.smartbiosensor.vo.Configuration;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;
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
    private com.application.smartbiosensor.vo.Configuration configuration;

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

            //TRATAR erro caso ache mais q 2
            while(contours.size() < 2){

                contours = new ArrayList<MatOfPoint>();
                hierarchy = new Mat();

                //mostrar essa imagem binary
                Imgproc.threshold(imgToProcessGray, imgToProcessGray, threshold, 255, Imgproc.THRESH_BINARY);

                Imgproc.findContours(imgToProcessGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //aparentemente o findContours modifica o source

                ArrayList<MatOfPoint> contoursFiltered = new ArrayList<MatOfPoint>();

                for(MatOfPoint contour: contours) {
                    if(Imgproc.contourArea(contour) > 40000)//400000
                        contoursFiltered.add(contour);
                }

                contours = contoursFiltered;

                threshold++;

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

                /*Rect rectangle = Imgproc.boundingRect(contour);

                Mat imgRectangle = new Mat(imgToProcessGray, rectangle);

                Bitmap fileLightPointsSeparatedBitmap = convertMatToBitmap(imgRectangle);
                Util.saveBitmapToFile(fileLightPointsSeparatedBitmap, "picPointsSeparated" + CameraService.COUNT_PHOTOS + numberRectangles, CameraService.DIRECTORY);*/

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

                lightIntensityPoints.add(new Pair(rectangle.y, intensity));

            }

            //MELHORAR ISSO, tá considerando q só tem 2
            if(lightIntensityPoints.size() > 1) {
                if (lightIntensityPoints.get(0).first < lightIntensityPoints.get(1).first) {
                    processResult.setIntensityReference(lightIntensityPoints.get(0).second);
                    processResult.setIntensity(lightIntensityPoints.get(1).second);
                } else {

                    processResult.setIntensityReference(lightIntensityPoints.get(1).second);
                    processResult.setIntensity(lightIntensityPoints.get(0).second);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return lightIntensityPoints;

    }

    protected ProcessResult processImage(ImageReader reader){

        processResult = new ProcessResult();

        try {

            CameraService.COUNT_PHOTOS++;
            Image image = reader.acquireLatestImage();

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            Mat imgToProcess;
            
            if(SAVE_IMAGES_TO_FILES) {

                File file = Util.saveBytesToFile(bytes, "pic" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                Bitmap fileBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imgToProcess = convertBitmapToMat(fileBitmap);
                fileBitmap.recycle();

            }else{
                imgToProcess = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            }

            Mat imgToProcessGray = new Mat();
            Imgproc.cvtColor(imgToProcess, imgToProcessGray, Imgproc.COLOR_BGR2GRAY);
            Mat imgToProcessGrayOriginal = imgToProcessGray.clone(); //versão que não será alterada de forma alguma

            if(SAVE_IMAGES_TO_FILES) {
                Bitmap fileGrayScaleBitmap = convertMatToBitmap(imgToProcessGray);
                Util.saveBitmapToFile(fileGrayScaleBitmap, "picGrayScale" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                fileGrayScaleBitmap.recycle();
            }

            ArrayList<MatOfPoint> contoursDetected = detectContoursLightPoints(imgToProcessGray);

            Imgproc.drawContours(imgToProcess, contoursDetected, -1, new Scalar(0, 255, 0), 3);

            if(SAVE_IMAGES_TO_FILES) {
                Bitmap bmpContoursDetected = convertMatToBitmap(imgToProcess);
                Util.saveBitmapToFile(bmpContoursDetected, "picPointsDetected" + CameraService.COUNT_PHOTOS, CameraService.DIRECTORY);
                bmpContoursDetected.recycle();
            }

            calculateIntensityLightPoints(imgToProcessGrayOriginal, contoursDetected, OPTION_AVERAGE_INTENSITY_PER_PIXEL);

            image.close();
        } catch (Exception e) {
            e.printStackTrace();
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
