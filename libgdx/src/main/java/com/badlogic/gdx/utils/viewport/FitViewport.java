
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Scaling;

/**
 * A ScalingViewport that uses {@link Scaling#fit} so it keeps the aspect ratio by scaling the world up to fit the screen, adding
 * black bars (letterboxing) for the remaining space.
 *
 * @author Daniel Holderbaum
 * @author Nathan Sweet
 */
public class FitViewport extends ScalingViewport {
    /**
     * Creates a new viewport using a new {@link OrthographicCamera}.
     */
    public FitViewport(Graphics graphics, float worldWidth, float worldHeight) {
        super(graphics, Scaling.fit, worldWidth, worldHeight);
    }

    public FitViewport(Graphics graphics, float worldWidth, float worldHeight, Camera camera) {
        super(graphics, Scaling.fit, worldWidth, worldHeight, camera);
    }
}
