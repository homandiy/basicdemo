package com.homanhuang.camerabasicdemo;

import android.support.annotation.Nullable;

import io.reactivex.Emitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

/**
 * Created by Homan on 1/30/2018.
 */

/*
    An Observable will emit an event when the camera API
    calls callbacks onOpened, onClosed and onDisconnected.

    ObservableEmitter expands the Emitter interface:
        public interface Emitter<T> {
          void onNext(@NonNull T value);
          void onError(@NonNull Throwable error);
          void onComplete();
        }

    We’ll call onNext each time Camera API calls the callbacks
    CameraDevice.StateCallback onOpened / onClosed / onDisconnected;
    and we’ll call onError when Camera API calls the onError callback.
 */
public interface ObservableEmitter<T> extends Emitter<T> {
    void setDisposable(@Nullable Disposable d);
    void setCancellable(@Nullable Cancellable c);
    boolean isDisposed();
    ObservableEmitter<T> serialize();
}