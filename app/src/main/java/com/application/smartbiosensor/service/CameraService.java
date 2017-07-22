package com.application.smartbiosensor.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.application.smartbiosensor.exception.CameraException;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraService {

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private String camera;
    private ImageReader imageReader;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;
    public static int COUNT_PHOTOS = 0;
    public static String DIRECTORY;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * The current state of camera state for taking pictures.
     */
    private int cameraState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private CaptureRequest.Builder previewRequestBuilder;

    private CaptureRequest.Builder captureBuilder;

    private CaptureRequest previewRequest;

    private Activity activity;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public CameraService(Activity activity, TextureView textureView){

        DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmartBioSensorFolder/";
        File newdir = new File(DIRECTORY);
        newdir.mkdirs();

        this.activity = activity;
        this.textureView = textureView;
    }

    public void takePicture() {

        if(textureView != null) {
            lockFocus();
        }else{
            captureStillPicture();
        }
    }

    public void openCamera() throws Exception{

        try {

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new CameraException("Tempo excedido aguardando o lock da abertura da câmera.");
            }

            setupCamera();

            cameraManager.openCamera(camera, cameraStateCallback, backgroundHandler);

        } catch (SecurityException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void closeCamera() throws Exception {
        try {
            cameraOpenCloseLock.acquire();
            if (cameraCaptureSession != null) {
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new CameraException("Interrupção durante tentativa fechar a câmera.");
        } finally {
            cameraOpenCloseLock.release();
        }
    }


    private void setupCamera() throws Exception{


        try {

            cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                camera = cameraId;

                Point size = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(size);
                int pixelWidth = size.x;
                int pixelHeight = size.y;

                if(textureView != null) {
                    textureView.getSurfaceTexture().setDefaultBufferSize(pixelWidth, pixelHeight);
                }

                imageReader = ImageReader.newInstance(pixelWidth, pixelHeight, PixelFormat.RGBA_8888, 1);
                imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

            }

        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private final CameraCaptureSession.CaptureCallback onCameraCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (cameraState) {
                case STATE_PREVIEW: {
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        cameraState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            cameraState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        cameraState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        cameraState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            process(result);
        }

    };

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice device) {
            cameraOpenCloseLock.release();
            cameraDevice = device;

            if(textureView != null) {
                createCameraPreviewSession();
            }else{
                createCaptureSession();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            stopBackgroundThread();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            stopBackgroundThread();
        }
    };

    private final CameraCaptureSession.CaptureCallback onCameraCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            if(textureView != null) {
                unlockFocus();
            }
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            if(textureView != null) {
                unlockFocus();
            }
        }

    };

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }


    private void captureStillPicture() {
        try {

            // This is the CaptureRequest.Builder that we use to take a picture.
            captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            // Use the same AE and AF modes as the preview. Focus
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, previewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));

            captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            cameraCaptureSession.stopRepeating();
            cameraCaptureSession.capture(captureBuilder.build(), onCameraCaptureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            cameraState = STATE_WAITING_PRECAPTURE;
            cameraCaptureSession.capture(previewRequestBuilder.build(), onCameraCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        try {
            //Wait until the cameraState is valid to be locked
            while(cameraState != STATE_PREVIEW){
            }

            // Tell #mCaptureCallback to wait for the lock.
            cameraState = STATE_WAITING_LOCK;

            // This is how to tell the camera to lock focus.
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            cameraCaptureSession.capture(previewRequestBuilder.build(), onCameraCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            //Wait until the cameraState is valid to be unlocked
            while(cameraState != STATE_PICTURE_TAKEN){
            }

            // Reset the auto-focus trigger
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

            cameraCaptureSession.capture(previewRequestBuilder.build(), onCameraCallback, backgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            cameraState = STATE_PREVIEW;
            cameraCaptureSession.setRepeatingRequest(previewRequest, onCameraCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void createCameraPreviewSession() {
        try {

            final Surface surface;

            surfaceTexture = textureView.getSurfaceTexture();
            assert surfaceTexture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());

            // This is the output Surface we need to start preview.
            surface = new Surface(surfaceTexture);


            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession captureSession) {
                            // The camera is already closed
                            if (cameraDevice == null) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            cameraCaptureSession = captureSession;
                            try {
                                // We set up a CaptureRequest.Builder with the output Surface.
                                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                previewRequestBuilder.addTarget(surface);

                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                previewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                cameraCaptureSession.setRepeatingRequest(previewRequest, onCameraCallback, backgroundHandler);


                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession captureSession) {

                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCaptureSession() {

        try {

            SurfaceTexture surfaceTexture = new SurfaceTexture(1);
            Size size = getSmallestSize(cameraDevice.getId());
            surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            final Surface surface = new Surface(surfaceTexture);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    cameraCaptureSession = session;
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {}
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    public void setOnImageAvailableListener(ImageReader.OnImageAvailableListener onImageAvailableListener){
        this.onImageAvailableListener = onImageAvailableListener;

        if(imageReader != null)
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
    }

    public boolean isCameraOpened(){
        return (cameraDevice != null);
    }

}
