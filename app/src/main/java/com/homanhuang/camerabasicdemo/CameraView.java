package com.homanhuang.camerabasicdemo;

import android.content.Context;

import android.hardware.Camera;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Homan on 1/29/2018.
 */

@SuppressWarnings("deprecation") //I don't want to see the crossline all over the code
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    /* Log tag and shortcut */
    final static String TAG = "MYLOG CameraV";
    public static void ltag(String message) {
        Log.i(TAG, message);
    }

    private SurfaceHolder mHolder;

    //use functions from android.hardware.Camera, not android.graphic.Camera;
    private Camera mCamera;

    private Context context;

    public CameraView(Context context, Camera camera) {
        super(context);

        this.context = context;
        this.mCamera = camera;

        //get the holder and set this class as the callback wih camera data
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            //Set the camera to draw images -> surfaceholder
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            ltag("Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //check if the surface is ready to receive camera data
        if(mHolder.getSurface() == null)
            return;

        //stop the preview, rotate and then start it again
        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
            ltag("Camera Mulfunction: "+e.getMessage());
        }

        //Recreate the camera preview
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }
}
