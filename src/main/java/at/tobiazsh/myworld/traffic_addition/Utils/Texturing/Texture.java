package at.tobiazsh.myworld.traffic_addition.Utils.Texturing;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 19:55
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Texture {
	private int textureId;
	private int width = -1, height = -1, channels = -1;

	public boolean isEmpty() {
		return textureId == 0;
	}

	public Texture loadTexturePath(String imagePath) {
		if (textureId == 0) {
			textureId = glGenTextures();
		}

		return loadTextureData(loadImageData(imagePath));
	}

	public Texture replaceTexture(String imagePath) {
		return replaceTextureData(loadImageData(imagePath));
	}

	public Texture replaceRawPixelData(ByteBuffer pixelData, int width, int height, int channels) {

		if (pixelData == null) {
			MyWorldTrafficAddition.LOGGER.error("Failed to load image! Pixel data is empty, invalid or corrupted!");
			return null;
		}

		glBindTexture(GL_TEXTURE_2D, textureId);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

		int format = (channels == 4) ? GL_RGBA : GL_RGB;
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, pixelData);

		this.width = width;
		this.height = height;
		this.channels = channels;

		return this;
	}

	public Texture loadTextureData(ByteBuffer encodedImage) {
		if (textureId == 0) {
			textureId = glGenTextures();
		}

		return replaceTextureData(encodedImage);
	}

	public Texture replaceTextureData(ByteBuffer encodedImage) {
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer c = BufferUtils.createIntBuffer(1);

		ByteBuffer decImage = stbi_load_from_memory(encodedImage, w ,h ,c, 0); // decoded image
		Texture tex = replaceRawPixelData(decImage, w.get(0), h.get(0), c.get(0));

		if (tex == null) {
			MyWorldTrafficAddition.LOGGER.error("Failed to load image! Decoded image is empty, invalid or corrupted!");
			return null;
		}

		stbi_image_free(decImage);
		return tex;
	}

	public Texture loadRawPixelData(ByteBuffer pixelData, int width, int height, int channels) {
		if (textureId == 0) {
			textureId = glGenTextures();
		}

		return replaceRawPixelData(pixelData, width, height, channels);
	}

	private ByteBuffer loadImageData(String resourcePath) {
		try {
			InputStream is;
			// First try loading as a resource from classpath
			if (resourcePath.startsWith("/")) {
				if (resourcePath.startsWith("/assets/myworld_traffic_addition/")) {
					is = MyWorldTrafficAddition.class.getResourceAsStream(resourcePath);
				} else {
					is = MyWorldTrafficAddition.class.getResourceAsStream("assets/myworld_traffic_addition/" + resourcePath);
				}
				// If it starts with a slash, load directly
			} else {
				// Try as resource with added slash
				if (resourcePath.startsWith("assets/myworld_traffic_addition/")) {
					is = MyWorldTrafficAddition.class.getResourceAsStream("/" + resourcePath);
				} else {
					is = MyWorldTrafficAddition.class.getResourceAsStream("/assets/myworld_traffic_addition/" + resourcePath);
				}
			}

			// If not found in classpath, try loading as a file
			if (is == null) {
				try {
					is = Files.newInputStream(Path.of(resourcePath));
				} catch (IOException e) {
					throw new IllegalArgumentException("Resource not found: " + resourcePath);
				}
			}

			try (ReadableByteChannel rbc = Channels.newChannel(is)) {
				ByteBuffer buffer = memAlloc(16 * 1024);
				while (rbc.read(buffer) != -1) {
					if (buffer.remaining() == 0) {
						ByteBuffer newBuffer = memAlloc(buffer.capacity() * 2);
						buffer.flip();
						newBuffer.put(buffer);
						memFree(buffer);
						buffer = newBuffer;
					}
				}
				buffer.flip();
				return buffer;
			} finally {
				is.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error loading image: " + resourcePath, e);
		}
	}

	public int getTextureId() {
		return textureId;
	}

	public int getWidth() {
		if (width == -1) MyWorldTrafficAddition.LOGGER.error("Texture not loaded for texture id {}: width undefined.", this.getTextureId());
		return width;
	}

	public int getHeight() {
		if (height == -1) MyWorldTrafficAddition.LOGGER.error("Texture not loaded for texture id {}: height undefined.", this.getTextureId());
		return height;
	}

	public int getChannels() {
		if (channels == -1) MyWorldTrafficAddition.LOGGER.error("Texture not loaded for texture id {}: channels undefined.", this.getTextureId());
		return channels;
	}
}
