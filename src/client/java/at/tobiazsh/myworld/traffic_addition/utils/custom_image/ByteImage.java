package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

/**
 * @param stbImage the raw downloaded bytes
 * @param width    optional decoded width
 * @param height   optional decoded height
 * @param channels optional decoded channels
 */
public record ByteImage(ByteBuffer stbImage, int width, int height, int channels) {

    @Override
    public ByteBuffer stbImage() {
        return stbImage.asReadOnlyBuffer(); // safe, does not copy underlying memory
    }

    public void free() {
        if (stbImage != null) {
            STBImage.stbi_image_free(stbImage);
        }
    }
}