package org.boofcv.android.localization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.boofcv.android.R;

public class LocalizationActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;
    int sensitivity = 25;

    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    TextView txt1;
    TextView txt2;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
//                    setContentView(R.layout.localization_layout);
//                    mOpenCvCameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
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

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public LocalizationActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "Everything should be fine with using the camera.");
        } else {
            Log.d(TAG, "Requesting permission to use the camera.");
            String[] CAMERA_PERMISSONS = {
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSONS, 0);
        }
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
//        }
//
//        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//
//        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
//        }
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.localization_layout);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        txt1 = (TextView) findViewById(R.id.textview);
        txt2 = (TextView) findViewById(R.id.textview1);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat recognize(Mat aInputFrame) {

        Mat mHsv = new Mat();
        Mat mMaskMat = new Mat();
        Mat mMaskMat1 = new Mat();
        Mat mDilatedMat = new Mat();
        Mat output = new Mat();
        Imgproc.cvtColor(aInputFrame, mHsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerThreshold = new Scalar(120, 100, 50); // Dull Red color – lower hsv values
        Scalar upperThreshold = new Scalar(125, 255, 255); // Dull Red color – higher hsv values

//        Scalar lowerThreshold = new Scalar (60 - sensitivity, 100, 100); // Green color – lower hsv values
//        Scalar upperThreshold = new Scalar (60 + sensitivity, 255, 255); // Green color – higher hsv values

        Core.inRange(mHsv, lowerThreshold, upperThreshold, mMaskMat);

        Imgproc.erode(mMaskMat, mDilatedMat, new Mat());
        Imgproc.dilate(mDilatedMat, mDilatedMat, new Mat());

        // contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        final List<Point> detected = new ArrayList<Point>();
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            if (Imgproc.contourArea(contours.get(contourIdx)) > 50) {
                Moments moments = Imgproc.moments(contours.get(contourIdx));
                final Point centroid = new Point();
                centroid.x = moments.get_m10() / moments.get_m00();
                centroid.y = moments.get_m01() / moments.get_m00();
                detected.add(centroid);
//                Imgproc.drawContours ( aInputFrame, contours, contourIdx, new Scalar(255, 0, 0), 5);
//                Imgproc.circle ( aInputFrame, centroid, 2, new Scalar(255, 0, 0), 5);
            }
        }

        Collections.sort(detected, new Comparator<Point>() {
            public int compare(Point s1, Point s2) {
                return Double.compare(s1.x, s2.x);
            }
        });

        List<Point> temp = new ArrayList<>();
        final List<List<Point>> grouped = new ArrayList<>();

        for (int i = 0; i < detected.size() - 1; i++) {
            double jump = detected.get(i + 1).x - detected.get(i).x;
            if (jump > 20) {
                temp.add(detected.get(i));
                grouped.add(temp);
                temp = new ArrayList<Point>();
            } else if (i == detected.size() - 2) {
                temp.add(detected.get(i));
                temp.add(detected.get(detected.size() - 1));
                grouped.add(temp);
            } else {
                temp.add(detected.get(i));
            }
        }

        Scalar colour;
        Log.d("Debug", Integer.toString(grouped.size()));
        for (int i = 0; i < grouped.size(); i++) {
            for (int contourIdx = 0; contourIdx < grouped.get(i).size(); contourIdx++) {
                if (i == 0) {
                    colour = new Scalar(0, 0, 255);
                    Imgproc.line(aInputFrame, new Point(grouped.get(i).get(contourIdx).x, grouped.get(i).get(contourIdx).y), new Point(1200, grouped.get(i).get(contourIdx).y), new Scalar(0, 255, 0), 5);
                } else {
                    colour = new Scalar(255, 0, 0);
                }
                Imgproc.circle(aInputFrame, grouped.get(i).get(contourIdx), 2, colour, 20);
            }
            if (i == 0) {
                Imgproc.line(aInputFrame, new Point(grouped.get(0).get(0).x, 0), new Point(grouped.get(0).get(0).x, 700), new Scalar(0, 255, 0), 5);
            }
            if (i == 1) {
                Imgproc.line(aInputFrame, new Point(grouped.get(1).get(0).x, 0), new Point(grouped.get(1).get(0).x, 700), new Scalar(0, 255, 0), 5);
            }
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String coor = "";
                for (Point i : detected) {
                    coor = coor + "x : " + Math.round(i.x) + "|| y : " + Math.round(i.y) + "\n";
                }
                coor = coor.trim();
                txt1.setText("No of blobs : " + detected.size());
                txt2.setText(coor);
            }
        });

        return aInputFrame;
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 1);
        return recognize(mRgba);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

}

