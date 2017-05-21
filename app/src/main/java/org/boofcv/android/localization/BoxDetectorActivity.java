package org.boofcv.android.localization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Handler;

import static android.content.ContentValues.TAG;

/**
 * Created by Amy on 20/5/17.
 */

public class BoxDetectorActivity extends Activity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
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
        setContentView(R.layout.templatematching);
//        Intent intent = new Intent(this, DemoMain.class);
//        startActivity(intent);
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
}
