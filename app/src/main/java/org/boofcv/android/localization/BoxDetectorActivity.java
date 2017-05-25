package org.boofcv.android.localization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import org.boofcv.android.DemoMain;
import org.boofcv.android.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

/**
 * Created by Amy on 20/5/17.
 */

public class BoxDetectorActivity extends Activity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private File lastPictureTaken;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            lastPictureTaken = pictureFile;
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d(TAG, "Successfully save picture");
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkCameraHardware(this);
        setContentView(R.layout.camera_localization);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else {
            Log.e("AUTO FOCUS", "doesn't work");
        }
        mCamera.setParameters(params);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        final Button captureButton = (Button) findViewById(R.id.button_capture);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    takePicture(mPicture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                captureButton.performClick();
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }, 7000);



        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        Button in = (Button) findViewById(R.id.in);
        in.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.e("Max Zoom", mCamera.getParameters().getMaxZoom() + "");
                        Log.e("Focal Length", mCamera.getParameters().getFocalLength() + "");
                        Log.e("Current Zoom", mCamera.getParameters().getZoom() + "");
                        int current = mCamera.getParameters().getZoom();
                        if (current < 10) {
                            Camera.Parameters param = mCamera.getParameters();
                            param.setZoom(current + 1);
                            mCamera.setParameters(param);
                        }
                    }
                }
        );

        Button out = (Button) findViewById(R.id.out);
        out.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.e("Max Zoom", mCamera.getParameters().getMaxZoom() + "");
                        Log.e("Focal Length", mCamera.getParameters().getFocalLength() + "");
                        Log.e("Current Zoom", mCamera.getParameters().getZoom() + "");
                        Log.e("Zoom Supported", mCamera.getParameters().isZoomSupported() + "");
                        int current = mCamera.getParameters().getZoom();
                        if (current > 0) {
                            Camera.Parameters param = mCamera.getParameters();
                            param.setZoom(current - 1);
                            mCamera.setParameters(param);
                        }
                    }
                }
        );
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
//            Log.e(Camera.;)
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "shelves");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void close() {
        mPreview.stopPreviewAndFreeCamera();

        //Code
        Bitmap bitmap = BitmapFactory.decodeFile(lastPictureTaken.toString());

        Mat ImageMat = new Mat();
        Bitmap myBitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(myBitmap32, ImageMat);

        //      Blob Detection
        Mat mHsv = new Mat();
        Mat mMaskMat = new Mat();
        Mat mDilatedMat = new Mat();
        Imgproc.cvtColor(ImageMat , mHsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerThreshold = new Scalar (115, 100, 25); // Dull Red color – lower hsv values
        Scalar upperThreshold = new Scalar (120, 255, 255); // Dull Red color – higher hsv values

        Core.inRange ( mHsv, lowerThreshold , upperThreshold, mMaskMat );

        Imgproc.erode ( mMaskMat, mDilatedMat, new Mat() );
        Imgproc.erode ( mDilatedMat, mDilatedMat, new Mat() );
        Imgproc.erode ( mDilatedMat, mDilatedMat, new Mat() );
        Imgproc.dilate ( mDilatedMat, mDilatedMat, new Mat() );
        Imgproc.dilate ( mDilatedMat, mDilatedMat, new Mat() );
        Imgproc.dilate ( mDilatedMat, mDilatedMat, new Mat() );
        Imgproc.dilate ( mDilatedMat, mDilatedMat, new Mat() );


        // contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours ( mDilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE );

        final List<Point> detected = new ArrayList<Point>();
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            if (Imgproc.contourArea(contours.get(contourIdx)) > 50) {
                Moments moments = Imgproc.moments(contours.get(contourIdx));
                final Point centroid = new Point();
                centroid.x = moments.get_m10() / moments.get_m00();
                centroid.y = moments.get_m01() / moments.get_m00();
                detected.add(centroid);
            }
        }

        Collections.sort(detected, new Comparator<Point>() {
            public int compare(Point s1, Point s2) {
                return Double.compare(s1.y, s2.y);
            }
        });

        List<Point> temp = new ArrayList<>();
        final List<List<Point>> grouped = new ArrayList<>();

        for(int i = 0; i < detected.size()-1; i++){
            double jump = detected.get(i+1).y - detected.get(i).y;
            if (jump > 100){
                temp.add(detected.get(i));
                grouped.add(temp);
                temp = new ArrayList<Point>();
            }
            else if(i == detected.size()-2){
                temp.add(detected.get(i));
                temp.add(detected.get(detected.size()-1));
                grouped.add(temp);
            }
            else{
                temp.add(detected.get(i));
            }
        }

        //      Label Detection
        Mat aaInputFrame1 = new Mat();
        Mat aaInputFrame2 = new Mat();
        Mat aaInputFrame5 = new Mat();
        List<MatOfPoint> coor1 = new ArrayList<MatOfPoint>();
        Mat result = new Mat();
        Mat result1 = new Mat();
        Mat result2 = new Mat();
        Imgproc.cvtColor(ImageMat, aaInputFrame5, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Sobel(aaInputFrame5, aaInputFrame1, CvType.CV_32F, 1, 0);
        Imgproc.Sobel(aaInputFrame5, aaInputFrame2, CvType.CV_32F, 0, 1);
        Core.subtract(Mat.ones(aaInputFrame2.size(),CvType.CV_32F),aaInputFrame1,result);
        Core.convertScaleAbs(result, result);
        Size size = new Size(9,9);
        Imgproc.blur(result, result, size);
        Imgproc.threshold(result,result, 30, 255, Imgproc.THRESH_BINARY);
        Size size1 = new Size(21,7);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,size1);
        Imgproc.morphologyEx(result,result1, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.erode(result1, result1, kernel);
        Imgproc.dilate(result1,result1,kernel);

        Imgproc.findContours(result1, coor1,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        final List<Point> detectedlabel = new ArrayList<Point>();
        final List<Float> ratio = new ArrayList<>();
        for(int i=0; i< coor1.size();i++) {
            if (Imgproc.contourArea(coor1.get(i)) > 4500) {
                Rect rect = Imgproc.boundingRect(coor1.get(i));
                float ratiod = ((float)rect.height/(float)rect.width);
                if(0<ratiod && ratiod<0.4) {
                    Imgproc.rectangle(ImageMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 20);
                    Moments moments = Imgproc.moments(coor1.get(i));
                    final Point centroid = new Point();
                    centroid.x = moments.get_m10() / moments.get_m00();
                    centroid.y = moments.get_m01() / moments.get_m00();
                    detectedlabel.add(centroid);
                    ratio.add(ratiod);
                }
            }
        }

        Scalar colour;
        for(int i = 0; i < grouped.size(); i++){
            for (int contourIdx=0; contourIdx < grouped.get(i).size(); contourIdx++ ){
                if(i == grouped.size()-1){
                    colour = new Scalar(0, 0, 255);
                    Imgproc.line(ImageMat, new Point(grouped.get(i).get(contourIdx).x, grouped.get(i).get(contourIdx).y), new Point(grouped.get(i).get(contourIdx).x, 0), new Scalar(0, 255, 0), 20);
                }
                else{
                    colour = new Scalar(255, 0, 0);
                }
                Imgproc.circle (ImageMat, grouped.get(i).get(contourIdx), 2, colour, 150);
            }
            if(i == 0){
                Imgproc.line(ImageMat, new Point(0, grouped.get(0).get(0).y), new Point(5000, grouped.get(0).get(0).y), new Scalar(0, 255, 0), 20);
            }
            if(i == 1){
                Imgproc.line(ImageMat, new Point(0, grouped.get(1).get(0).y), new Point(5000, grouped.get(1).get(0).y), new Scalar(0, 255, 0), 20);
            }
        }
        String coor = "";
        for(Point i : detected){
            coor = coor + "x : " + Math.round(i.x) + "|| y : " + Math.round(i.y) +"\n";
        }
        coor = coor.trim();

        Log.d("Debug",coor);

        List<Double> x = new ArrayList<>();
        for (List<Point> j : grouped){
            x.add(j.get(0).y);
        }
        for(Point i : detectedlabel){
            for(double z : x){
                if(z - 100 <i.y && i.y < z + 100){
                    Imgproc.circle (ImageMat,i , 2, new Scalar (0,255,0), 100);
                }
            }
        }

        coor = "";
        for(Point i : detectedlabel){
            coor = coor + "x : " + Math.round(i.x) + "|| y : " + Math.round(i.y) +"\n";
        }
        coor = coor.trim();

        Log.d("Debug",coor);

        //Convert the processed Mat to Bitmap
        Imgproc.cvtColor(ImageMat, ImageMat, Imgproc.COLOR_BGR2RGB);
        Bitmap resultBitmap = Bitmap.createBitmap(ImageMat.cols(),  ImageMat.rows(),Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(ImageMat, resultBitmap);

        setContentView(R.layout.templatematching);
        ImageView mImageView;
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(resultBitmap);

    }

    public void takePicture(final Camera.PictureCallback callback) throws Exception
    {
        if(mCamera == null)
            throw new IllegalStateException("Camera unavailable!");

        // TODO lock camera here?

        // Use auto focus if the camera supports it
        String focusMode = mCamera.getParameters().getFocusMode();
        if(focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focusMode.equals(Camera.Parameters.FOCUS_MODE_FIXED))
        {
            mCamera.autoFocus(new Camera.AutoFocusCallback()
            {
                @Override
                public void onAutoFocus(boolean success, Camera camera)
                {
                    camera.takePicture(null, null, callback);
                }
            });
        }
        else
            Log.d("FOCUS","NOT IN AUTOFOCUS");
            mCamera.takePicture(null, null, callback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}
