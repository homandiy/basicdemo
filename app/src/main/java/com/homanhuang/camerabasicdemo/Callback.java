package com.homanhuang.camerabasicdemo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Homan on 1/30/2018.
 */

/*
    Handle all the callbacks
 */
public interface Callback {
    void onFocusStarted();

    void onFocusFinished();

    void onPhotoTaken(@NonNull String photoUrl, @Nullable Integer photoSourceType);

    void onCameraAccessException();

    void onCameraOpenException(@Nullable OpenCameraException.Reason reason);

    void onException(Throwable throwable);
}
