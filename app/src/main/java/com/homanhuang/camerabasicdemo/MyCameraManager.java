package com.homanhuang.camerabasicdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.content.Context.CAMERA_SERVICE;

/**
 * Created by Homan on 1/29/2018.
 */

public class MyCameraManager {
    CameraManager manager;

    /* Toast shortcut */
    public static void msg(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /* Log tag and shortcut */
    final static String TAG = "MYLOG CHAT ACT";

    public static void ltag(String message) {
        Log.i(TAG, message);
    }

    private static Context context;

    List<String> cameraIdList = new ArrayList<String>();

    //camera ids
    private Integer frontCameraId = null;
    private Integer rearCameraId = null;
    private Integer externalCameraId = null;

    /*
        Construtor
     */
    public MyCameraManager(Context context) {
        this.context = context;
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);

        try {
            for (String cameraId : manager.getCameraIdList()) {
                cameraIdList.add(cameraId);
                CameraCharacteristics cameraChars = manager.getCameraCharacteristics(cameraId);

                /*
                Cameras may be front (CameraCharacteristics.LENS_FACING_FRONT),
                rear (CameraCharacteristics.LENS_FACING_BACK) or
                external (CameraCharacteristics.LENS_FACING_EXTERNAL).
                 */
                Integer facing = cameraChars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontCameraId = facing;
                    }
                    if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        rearCameraId = facing;
                    }
                    if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) {
                        externalCameraId = facing;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfLens() {
        return cameraIdList.size();
    }

    public Integer getFrontCameraId() {
        return frontCameraId;
    }

    public Integer getRearCameraId() {
        return rearCameraId;
    }

    public Integer getExternalCameraId() {
        return externalCameraId;
    }


    /*
        Events serve for camera device
     */
    public enum DeviceStateEvents {
        ON_OPENED,
        ON_CLOSED,
        ON_DISCONNECTED
    }

    /*
        RxJava: Observable create method
        Pair Type: Events and CameraDevice

        The permission check will be done in the activity
     */
    @SuppressLint("MissingPermission")
    public static Observable< Pair<DeviceStateEvents, CameraDevice> >
        openCamera(@NonNull String cameraId, @NonNull CameraManager cameraManager) {

        return Observable.create(observableEmitter -> {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    observableEmitter.onNext(new Pair<>(DeviceStateEvents.ON_OPENED, cameraDevice));
                }

                @Override
                public void onClosed(@NonNull CameraDevice cameraDevice) {
                    observableEmitter.onNext(new Pair<>(DeviceStateEvents.ON_CLOSED, cameraDevice));
                    observableEmitter.onComplete();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    observableEmitter.onNext(new Pair<>(DeviceStateEvents.ON_DISCONNECTED, cameraDevice));
                    observableEmitter.onComplete();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    observableEmitter.onError(new OpenCameraException(OpenCameraException.Reason.getReason(error)));
                }
            }, null);
        });
    }
}
