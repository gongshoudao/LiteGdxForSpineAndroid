
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Scaling;

/** A ScalingViewport that uses {@link Scaling#stretch} so it does not keep the aspect ratio, the world is scaled to take the whole
 * screen.
 * @author Daniel Holderbaum
 * @author Nathan Sweet */
public class StretchViewport extends ScalingViewport {
	/** Creates a new viewport using a new {@link OrthographicCamera}. */
	public StretchViewport(Graphics graphics, float worldWidth, float worldHeight) {
		super(graphics, Scaling.stretch, worldWidth, worldHeight);
	}

	public StretchViewport(Graphics graphics, float worldWidth, float worldHeight, Camera camera) {
		super(graphics, Scaling.stretch, worldWidth, worldHeight, camera);
	}
}
