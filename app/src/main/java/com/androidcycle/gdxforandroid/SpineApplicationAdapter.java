package com.androidcycle.gdxforandroid;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.attachments.Attachment;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * base Spine {@link ApplicationListener}
 *
 * @author shoudao
 */
public abstract class SpineApplicationAdapter extends ApplicationAdapter {
    private final String mAtlasPath;
    private final String mSkeletonDataPath;
    private TextureAtlas atlas;
    private final AndroidFiles mAndroidFiles;
    private Skeleton skeleton;
    private AnimationState state;
    private PolygonSpriteBatch batch;
    private SkeletonRenderer renderer;
    private SkeletonRendererDebug debugRenderer;
    private boolean showDebug = false;

    /**
     * callback on Skeleton initialized. You can config skeleton in this method.e.g. {@link Attachment} replace and so on.
     *
     * @param skeleton
     */
    public abstract void onSkeletonReady(Skeleton skeleton);

    /**
     * callback on AnimationState initialized.
     *
     * @param animationState
     */
    public abstract void onAnimationReady(AnimationState animationState);

    /**
     * @param atlasPath        .atlas file path
     * @param skeletonDataPath .skel or .json file path
     * @throws FileNotFoundException
     */
    public SpineApplicationAdapter(String atlasPath, String skeletonDataPath) throws FileNotFoundException {
        mAtlasPath = atlasPath;
        mSkeletonDataPath = skeletonDataPath;
        if (!new File(mAtlasPath).exists() || !new File(skeletonDataPath).exists()) {
            throw new FileNotFoundException("atlas or skeleton file not found!");
        }
        mAndroidFiles = new AndroidFiles(null);
    }

    @Override
    public void create(Graphics graphics, Application app) {
        super.create(graphics, app);
        //init render kit
        batch = new PolygonSpriteBatch(graphics);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        if (BuildConfig.DEBUG)
            debugRenderer = new SkeletonRendererDebug(graphics);

        //init spine data
        atlas = new TextureAtlas(mAndroidFiles.absolute(mAtlasPath), app);
        SkeletonData skeletonData;
        if (mSkeletonDataPath.endsWith(".skel")) {//binary data
            SkeletonBinary skeletonBinary = new SkeletonBinary(atlas);
            skeletonData = skeletonBinary.readSkeletonData(mAndroidFiles.absolute(mSkeletonDataPath));
        } else {
            SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
            skeletonData = json.readSkeletonData(mAndroidFiles.absolute(mSkeletonDataPath));
        }

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        onSkeletonReady(skeleton);

        state = new AnimationState(new AnimationStateData(skeletonData));
        onAnimationReady(state);
    }

    @Override
    public void render() {
        super.render();
        state.update(graphics.getDeltaTime()); // Update the animation time.
        graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state.apply(skeleton)) // Poses skeleton using current animations. This sets the bones' local SRT.
            skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        batch.begin();
        renderer.draw(batch, skeleton); // Draw the skeleton images.
        batch.end();

        if (debugRenderer != null && showDebug)
            debugRenderer.draw(skeleton); // Draw debug lines.
    }

    @Override
    public void dispose() {
        super.dispose();
        atlas.dispose();
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }
}
