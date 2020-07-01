
package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Scaling;

/** A ScalingViewport that uses {@link Scaling#fill} so it keeps the aspect ratio by scaling the world up to take the whole screen
 * (some of the world may be off screen).
 * @author Daniel Holderbaum
 * @author Nathan Sweet */
public class FillViewport extends ScalingViewport {
	/** Creates a new viewport using a new {@link OrthographicCamera}. */
	public FillViewport(Graphics graphics, float worldWidth, float worldHeight) {
		super(graphics, Scaling.fill, worldWidth, worldHeight);
	}

	public FillViewport(Graphics graphics, float worldWidth, float worldHeight, Camera camera) {
		super(graphics, Scaling.fill, worldWidth, worldHeight, camera);
	}
}
