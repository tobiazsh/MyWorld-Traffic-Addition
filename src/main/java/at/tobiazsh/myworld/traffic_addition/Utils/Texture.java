package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 19:55
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Texture {
	private int textureId = 0;
	private int h = -1;
	private int w = -1;
	private int c = -1;

	public boolean isEmpty() {
		return textureId == 0;
	}

	public void loadTexture(String resourcePath) {
		ByteBuffer imageData;

		GL.createCapabilities();

		try {
			imageData = loadResourceToByteBuffer(resourcePath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		textureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureId);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);

		ByteBuffer image = stbi_load_from_memory(imageData, width, height, channels, 0);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

		h = height.get(0);
		w = width.get(0);
		c = channels.get(0);

		if (image != null) {
			if (channels.get(0) == 4) {
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			}
			else if (channels.get(0) == 3) {
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
			}
			else
				System.err.println("Error: (Loading Texture): Unknown number of channels: " + channels.get(0) + "! Channels must either be 3 (RGB) or 4 (RGBA)!");
		} else {
			System.err.println("Error: (Texture Loading) Couldn't load image: " + resourcePath);
		}
	}

	public int getTextureId() {
		return textureId;
	}

	public static ByteBuffer loadResourceToByteBuffer(String resourcePath) throws Exception {
		InputStream is = MyWorldTrafficAddition.class.getResourceAsStream(resourcePath);

		if (is == null) {
			throw new IllegalArgumentException("Error: (Load Resource To ByteBuffer) Resource was not found: " + resourcePath);
		}

		ByteBuffer buffer;
		try (ReadableByteChannel rbc = Channels.newChannel(is)) {
			buffer = memAlloc(16 * 1024);

			while (true) {
				int bytesRead = rbc.read(buffer);
				if (bytesRead == -1) break;
				if (buffer.remaining() == 0) {
					ByteBuffer newBuffer = memAlloc(buffer.capacity() * 2);
					buffer.flip();
					newBuffer.put(buffer);
					memFree(buffer);
					buffer = newBuffer;
				}
			}

			buffer.flip();
		}

		return buffer;
	}

	public static int quickLoad(String resourcePath) {
		ByteBuffer imageData;

		try {
			imageData = loadResourceToByteBuffer(resourcePath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		int texId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texId);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);

		ByteBuffer image = stbi_load_from_memory(imageData, width, height, channels, 0);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

		if (image != null) {
			if (channels.get(0) == 4) {
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			}
			else if (channels.get(0) == 3) {
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
			}
			else
				System.err.println("Error: (Loading Texture): Unknown number of channels: " + channels.get(0) + "! Channels must either be 3 (RGB) or 4 (RGBA)!");
		} else {
			System.err.println("Error: (Texture Loading) Couldn't load image: " + resourcePath);
		}

		return texId;
	}

	public int getChannels() {
		if (c == -1) System.err.println("Error (Finding Channels in Texture class): Couldn't determine texture's channels because no texture ID associated with that resource path has been found! Register texture first!");
		return c;
	}

	public int getHeight() {
		if (h == -1) System.err.println("Error (Returning height in Texture class): Couldn't determine texture's height because no texture ID associated with that resource path has been found! Register texture first!");
		return h;
	}

	public int getWidth() {
		if (w == -1) System.err.println("Error (Returning width in Texture class): Couldn't determine texture's width because no texture ID associated with that resource path has been found! Register texture first!");
		return w;
	}
}