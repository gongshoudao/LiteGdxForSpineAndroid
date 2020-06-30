package com.androidcycle.gdxforandroid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SpineAnimationListener extends ApplicationAdapter {
    OrthographicCamera camera;
    SpriteBatch batch;
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
        batch = new SpriteBatch(graphics);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        debugRenderer = new SkeletonRendererDebug();

        atlas = new TextureAtlas(Gdx.files.internal("spineboy/spineboy-pma.atlas"));
        SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
        json.setScale(1f); // Load the skeleton at 60% the size it was in Spine.
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("spineboy/spineboy-ess.json"));

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(250, 0);
        skeleton.setAttachment("head-bb", "head"); // Attach "head" bounding box to "head-bb" slot.

        Slot slot = skeleton.findSlot("gun");
        Attachment attachment = skeleton.getAttachment("gun", "gun");
        if (attachment instanceof RegionAttachment) {
            TextureRegion region = ((RegionAttachment) attachment).getRegion();
//            TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//            textPaint.setTextAlign(TextPaint.Align.CENTER);
//            textPaint.setColor(Color.WHITE);
            try {
                FileHandle fileHandle = new FileHandle(SpineApp.getInstance().getFilesDir()+"/texture/ic_launcher_round.png");
                Texture texture = new Texture(fileHandle);
                TextureRegion textureRegion = new TextureRegion(texture);
//                region.setRegion(texture);
                ((RegionAttachment) attachment).setRegion(textureRegion);
                ((RegionAttachment) attachment).setScaleX(1.5f);
                ((RegionAttachment) attachment).setScaleY(1.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((RegionAttachment) attachment).updateOffset();
        }

        slot.setAttachment(attachment);

        bounds = new SkeletonBounds(); // Convenience class to do hit detection with bounding boxes.

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
        stateData.setMix("run", "jump", 0.01f);
        stateData.setMix("jump", "run", 0.01f);
        stateData.setMix("jump", "jump", 0);

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
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                System.out.println(entry.getTrackIndex() + " event: " + entry + ", " + event.getData().getName() + ", " + event.getInt());
            }
        });

        // Set animation on track 0.
        AnimationState.TrackEntry trackEntry = state.setAnimation(0, "run", true);
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

        debugRenderer.draw(skeleton); // Draw debug lines.
    }

    @Override
    public void dispose() {
        super.dispose();
        atlas.dispose();
    }
}
