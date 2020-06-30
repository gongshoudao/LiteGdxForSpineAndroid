package com.androidcycle.gdxforandroid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TwoColorPolygonBatch;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SpineTwoColorPolygonBatchAnimationListener extends ApplicationAdapter {
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
        batch = new TwoColorPolygonBatch(graphics);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
        debugRenderer = new SkeletonRendererDebug();

        atlas = new TextureAtlas(Gdx.files.internal("goblins/goblins-pma.atlas"));
        SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
        json.setScale(3f); // Load the skeleton at 60% the size it was in Spine.
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("goblins/goblins-pro.json"));

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(300, 0);


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
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                System.out.println(entry.getTrackIndex() + " event: " + entry + ", " + event.getData().getName() + ", " + event.getInt());
            }
        });

        // Queue animations on tracks 0 and 1.
        state.setAnimation(0, "walk", true);
        Skin skin = new Skin("test");
        skin.copySkin(skeletonData.findSkin("goblingirl"));
        skeleton.setSkin(skin);
        skeleton.setSlotsToSetupPose();

        Slot slot = skeleton.findSlot("head");
        Attachment attachment = skeleton.getAttachment("head", "head");
        if (attachment instanceof MeshAttachment) {
            TextureRegion region = ((MeshAttachment) attachment).getRegion();
//            TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//            textPaint.setTextAlign(TextPaint.Align.CENTER);
//            textPaint.setColor(Color.WHITE);
            try {
                FileHandle fileHandle = new FileHandle(SpineApp.getInstance().getFilesDir() + "/texture/ic_launcher_round.png");
                Texture texture = new Texture(fileHandle);
                TextureRegion textureRegion = new TextureRegion(texture);
                region.setRegion(texture);
//                ((MeshAttachment) attachment).setRegion(textureRegion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (attachment instanceof RegionAttachment) {
            try {
                FileHandle fileHandle = new FileHandle(SpineApp.getInstance().getFilesDir() + "/texture/ic_launcher_round.png");
                Texture texture = new Texture(fileHandle);
                TextureRegion textureRegion = new TextureRegion(texture);
//                region.setRegion(texture);
                ((RegionAttachment) attachment).setRegion(textureRegion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (slot != null) {
            slot.setAttachment(attachment);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.setToOrtho(false); // Update camera with new size.
    }

    @Override
    public void render() {
        super.render();
        state.update(graphics.getDeltaTime()); // Update the animation time.
        graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

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
}
