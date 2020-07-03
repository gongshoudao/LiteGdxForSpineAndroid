package com.androidcycle.gdxforandroid;

import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;

import java.io.FileNotFoundException;

/**
 * spine animation listener.
 */
public class PolygonSpine extends SpineApplicationAdapter {
    /**
     * @param atlasPath        .atlas file path
     * @param skeletonDataPath .skel or .json file path
     * @throws FileNotFoundException
     */
    public PolygonSpine(String atlasPath, String skeletonDataPath) throws FileNotFoundException {
        super(atlasPath, skeletonDataPath);
    }

    @Override
    public void onSkeletonReady(Skeleton skeleton) {
        //find slot and attachment
    }

    @Override
    public void onAnimationReady(AnimationState animationState) {
        //find animation and start.
    }
}
