package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.ImageOperations;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class ImageUtils {

    public static String getImageFormat(byte[] imageBytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return null; // No image reader found -> invalid or unsupported format
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                return reader.getFormatName();
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scales image down to specified scale
     *
     * @param imageData Raw pixel data to scale
     * @param originalImageData ensures that the original image buffer is not accidentally freed when deallocating the imageData buffer during image scaling.
     * @param scale Scale to scale the image to
     * @param width Current width
     * @param height Current height
     * @param channels The channels of the image. This will later correlate to STBImageResize formats "stbir_pixel_formats". For more information, please take a look at {@link STBImageResize}
     * @return Triplet(A, B, C)
     * <li>
     *     A = Width of the scaled image (Integer)
     * </li>
     * <li>
     *     B = Height of the scaled image (Integer)
     * </li>
     * <li>
     *     C = ByteBuffer containing the raw pixel data
     * </li>
     */
    public static Triplet<Integer, Integer, ByteBuffer> scaleImage(ByteBuffer imageData, ByteBuffer originalImageData, float scale, int width, int height, int channels, Runnable onAbort) {
        int newWidth = (int) Math.ceil(width * scale);
        int newHeight = (int) Math.ceil(height * scale);

        if (imageData != null && imageData != originalImageData) {
            MemoryUtil.memFree(imageData);
        }

        ByteBuffer scaledImage = MemoryUtil.memAlloc(newWidth * newHeight * channels);
        boolean state = ImageOperations.bilinearResize(imageData, width, height, scaledImage, newWidth, newHeight, channels);

        if (!state) { // Abort if unsuccessful (empty)
            MyWorldTrafficAddition.LOGGER.error("Failed to resize image! Aborting...");
            onAbort.run();
            return new Triplet<>(0, 0, null);
        }

        return new Triplet<>(newWidth, newHeight, scaledImage);
    }
}
