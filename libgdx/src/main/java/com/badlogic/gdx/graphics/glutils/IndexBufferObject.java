/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * <p>
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs. This class can be
 * seamlessly used with OpenGL ES 1.x and 2.0.
 * </p>
 *
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 *
 * <p>
 * You can also use this to store indices for vertex arrays. Do not call {@link #bind()} or {@link #unbind()} in this case but
 * rather use {@link #getBuffer()} to use the buffer directly with glDrawElements. You must also create the IndexBufferObject with
 * the second constructor and specify isDirect as true as glDrawElements in conjunction with vertex arrays needs direct buffers.
 * </p>
 *
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 *
 * @author mzechner, Thorsten Schleinzer
 */
public class IndexBufferObject implements IndexData {
    final ShortBuffer buffer;
    final ByteBuffer byteBuffer;
    final boolean ownsBuffer;
    int bufferHandle;
    final boolean isDirect;
    boolean isDirty = true;
    boolean isBound = false;
    final int usage;

    // used to work around bug: https://android-review.googlesource.com/#/c/73175/
    private final boolean empty;
	private Graphics graphics;

	/**
     * Creates a new static IndexBufferObject to be used with vertex arrays.
     *
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IndexBufferObject(Graphics graphics, int maxIndices) {
        this(graphics, true, maxIndices);
    }

    /**
     * Creates a new IndexBufferObject.
     *
     * @param isStatic   whether the index buffer is static
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IndexBufferObject(Graphics graphics, boolean isStatic, int maxIndices) {
		this.graphics = graphics;
        empty = maxIndices == 0;
        if (empty) {
            maxIndices = 1; // avoid allocating a zero-sized buffer because of bug in Android's ART < Android 5.0
        }

        byteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * 2);
        isDirect = true;

        buffer = byteBuffer.asShortBuffer();
        ownsBuffer = true;
        buffer.flip();
        byteBuffer.flip();
        bufferHandle = graphics.getGL20().glGenBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    public IndexBufferObject(Graphics graphics, boolean isStatic, ByteBuffer data) {
		this.graphics = graphics;
		empty = data.limit() == 0;
        byteBuffer = data;
        isDirect = true;

        buffer = byteBuffer.asShortBuffer();
        ownsBuffer = false;
        bufferHandle = graphics.getGL20().glGenBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    /**
     * @return the number of indices currently stored in this buffer
     */
    public int getNumIndices() {
        return empty ? 0 : buffer.limit();
    }

    /**
     * @return the maximum number of indices this IndexBufferObject can store.
     */
    public int getNumMaxIndices() {
        return empty ? 0 : buffer.capacity();
    }

    /**
     * <p>
     * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
     * copied to this IndexBufferObject.
     * </p>
     *
     * <p>
     * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
     * </p>
     *
     * @param indices the vertex data
     * @param offset  the offset to start copying the data from
     * @param count   the number of shorts to copy
     */
    public void setIndices(short[] indices, int offset, int count) {
        isDirty = true;
        buffer.clear();
        buffer.put(indices, offset, count);
        buffer.flip();
        byteBuffer.position(0);
        byteBuffer.limit(count << 1);

        if (isBound) {
			graphics.getGL20().glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    public void setIndices(ShortBuffer indices) {
        isDirty = true;
        int pos = indices.position();
        buffer.clear();
        buffer.put(indices);
        buffer.flip();
        indices.position(pos);
        byteBuffer.position(0);
        byteBuffer.limit(buffer.limit() << 1);

        if (isBound) {
			graphics.getGL20().glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void updateIndices(int targetOffset, short[] indices, int offset, int count) {
        isDirty = true;
        final int pos = byteBuffer.position();
        byteBuffer.position(targetOffset * 2);
        BufferUtils.copy(indices, offset, byteBuffer, count);
        byteBuffer.position(pos);
        buffer.position(0);

        if (isBound) {
			graphics.getGL20().glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    /**
     * <p>
     * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
     * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
     * </p>
     *
     * @return the underlying short buffer.
     */
    public ShortBuffer getBuffer() {
        isDirty = true;
        return buffer;
    }

    /**
     * Binds this IndexBufferObject for rendering with glDrawElements.
     */
    public void bind() {
        if (bufferHandle == 0) throw new GdxRuntimeException("No buffer allocated!");

		graphics.getGL20().glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
        if (isDirty) {
            byteBuffer.limit(buffer.limit() * 2);
			graphics.getGL20().glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
        isBound = true;
    }

    /**
     * Unbinds this IndexBufferObject.
     */
    public void unbind() {
		graphics.getGL20().glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /**
     * Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
     */
    public void invalidate() {
        bufferHandle = graphics.getGL20().glGenBuffer();
        isDirty = true;
    }

    /**
     * Disposes this IndexBufferObject and all its associated OpenGL resources.
     */
    public void dispose() {
        graphics.getGL20().glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        graphics.getGL20().glDeleteBuffer(bufferHandle);
        bufferHandle = 0;

        if (ownsBuffer) {
            BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
        }
    }
}
