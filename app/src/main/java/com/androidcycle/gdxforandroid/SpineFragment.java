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

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class SpineFragment extends AndroidFragmentApplication {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.a = 8;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        View view = initializeForView(new SpineTwoColorPolygonBatchAnimationListener(), config);
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
