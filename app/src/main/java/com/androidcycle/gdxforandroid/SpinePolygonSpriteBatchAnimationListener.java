package com.androidcycle.gdxforandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;

import java.io.ByteArrayOutputStream;

public class SpinePolygonSpriteBatchAnimationListener extends ApplicationAdapter {
    OrthographicCamera camera;
    Batch batch;
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;

    TextureAtlas atlas;
    Skeleton skeleton;
    SkeletonBounds bounds;
    AnimationState state;

    @Override
    public void create(Graphics graphics) {
        super.create(graphics);
        camera = new OrthographicCamera();
        batch = new PolygonSpriteBatch(graphics);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        debugRenderer = new SkeletonRendererDebug();

        atlas = new TextureAtlas(Gdx.files.internal("raptor/raptor-pma.atlas"));
        SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
        json.setScale(1f); // Load the skeleton at 60% the size it was in Spine.
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("raptor/raptor-pro.json"));

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(350, 0);

        Slot slot = skeleton.findSlot("gun");
        Attachment attachment = skeleton.getAttachment("gun", "gun-nohand");
        if (attachment instanceof MeshAttachment) {
            TextureRegion region = ((MeshAttachment) attachment).getRegion();
            try {
                FileHandle fileHandle = new FileHandle(SpineApp.getInstance().getFilesDir() + "/texture/ic_launcher_round.png");
                Texture texture = new Texture(fileHandle);
                region.setRegion(texture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (attachment instanceof RegionAttachment) {
            TextureRegion region = ((RegionAttachment) attachment).getRegion();
            try {
                Texture textureText = testTextByAndroid();
                TextureRegion textureRegion = new TextureRegion(textureText);
                ((RegionAttachment) attachment).setRegion(textureRegion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        slot.setAttachment(attachment);

        bounds = new SkeletonBounds(); // Convenience class to do hit detection with bounding boxes.

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.

        state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        state.setTimeScale(0.5f); // Slow all animations down to 30% speed.
        state.addListener(new AnimationState.AnimationStateListener() {
            @Override
            public void start(AnimationState.TrackEntry entry) {
                System.out.println(entry.getTrackIndex() + " start: " + entry);
            }

            @Override
            public void interrupt(AnimationState.TrackEntry entry) {
                System.out.println(entry.getTrackIndex() + " interrupt: " + entry);
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
                System.out.println(entry.getTrackIndex() + " end: " + entry);
            }

            @Override
            public void dispose(AnimationState.TrackEntry entry) {
                System.out.println(entry.getTrackIndex() + " dispose: " + entry);
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                System.out.println(entry.getTrackIndex() + " complete: " + entry);

                if (entry.getAnimation().getName().equals("gun-grab")) {
                    Slot slot = skeleton.findSlot("front-hand");
                    Attachment attachment = skeleton.getAttachment("front-hand", "gun");
                    if (attachment instanceof MeshAttachment) {
                        TextureRegion region = ((MeshAttachment) attachment).getRegion();
                        try {
                            FileHandle fileHandle = new FileHandle(SpineApp.getInstance().getFilesDir() + "/texture/ic_launcher_round.png");
                            Texture texture = new Texture(fileHandle);
                            region.setRegion(texture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (attachment instanceof RegionAttachment) {
                        try {
                            Texture textureText = testTextByAndroid();
                            TextureRegion textureRegion = new TextureRegion(textureText);
                            ((RegionAttachment) attachment).setRegion(textureRegion);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    slot.setAttachment(attachment);
                }
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                System.out.println(entry.getTrackIndex() + " event: " + entry + ", " + event.getData().getName() + ", " + event.getInt());
            }
        });

        // Queue animations on tracks 0 and 1.
        state.setAnimation(0, "walk", true);
        state.addAnimation(1, "gun-grab", false, 2); // Keys in higher tracks override the pose from lower tracks.
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.setToOrtho(false); // Update camera with new size.
    }

    @Override
    public void render() {
        super.render();
        state.update(Gdx.graphics.getDeltaTime()); // Update the animation time.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state.apply(skeleton)) // Poses skeleton using current animations. This sets the bones' local SRT.
            skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        // Configure the camera, SpriteBatch, and SkeletonRendererDebug.
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        debugRenderer.getShapeRenderer().setProjectionMatrix(camera.combined);

        batch.begin();
        renderer.draw(batch, skeleton); // Draw the skeleton images.
        batch.end();

//        debugRenderer.draw(skeleton); // Draw debug lines.
    }

    @Override
    public void dispose() {
        super.dispose();
        atlas.dispose();
    }

    public Texture testTextByAndroid() {
        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.createFromAsset(SpineApp.getInstance().getAssets(), "setofont.ttf"));
        textPaint.setTextSize(48);
        textPaint.setColor(android.graphics.Color.YELLOW);
        float width = textPaint.measureText("文字abcd") + .5f;
        float baseline = -textPaint.ascent();
        float height = textPaint.descent() - textPaint.ascent() + .5f;
        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText("文字abcd", 0, -textPaint.ascent(), textPaint);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        Pixmap pixmap = new Pixmap(byteArray, 0, byteArray.length);
        return new Texture(pixmap);
    }
}
