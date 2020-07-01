package com.androidcycle.gdxforandroid;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class SpineFragment<T extends ApplicationListener> extends AndroidFragmentApplication {

    private Class<T> mClazz;

    public void setApplicationListenerClass(Class<T> clazz) {
        mClazz = clazz;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.a = 8;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        ApplicationListener listener = null;
        if (mClazz != null) {
            try {
                listener = (ApplicationListener) mClazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        View view = initializeForView(listener, config);
        if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            SurfaceHolder holder = surfaceView.getHolder();
            surfaceView.setZOrderOnTop(true);
            if (holder != null) {
                holder.setFormat(PixelFormat.TRANSPARENT);
            }
        }
        return view;
    }
}
