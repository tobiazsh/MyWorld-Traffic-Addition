package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.ImageOperations;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class ImageUtils {

    /**
     * Gets the image format from the given image bytes
     * @param imageBytes Image bytes
     * @return Image format (e.g. "png", "jpeg", etc.) or null if the format could not be determined
     */
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

    /**
     * Encodes raw pixel data to PNG format
     * @param imageData Raw pixel data
     * @param width Width of the image
     * @param height Height of the image
     * @param channels Number of channels
     * @return The PNG encoded image as byte array
     */
    public static byte[] encodePNG(ByteBuffer imageData, int width, int height, int channels) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        STBIWriteCallbackI callback = new STBIWriteCallback() {
            @Override
            public void invoke(long context, long data, int size) {
                byte[] buffer = new byte[size];
                MemoryUtil.memCopy(data, MemoryUtil.memAddress(MemoryUtil.memAlloc(size)), size);
                MemoryUtil.memByteBuffer(data, size).get(buffer);
                outputStream.write(buffer, 0, buffer.length);
            }
        };

        int stride = width * channels;

        boolean success = STBImageWrite.stbi_write_png_to_func(
                callback,
                0,
                width,
                height,
                channels,
                imageData,
                stride
        );

        if (!success) {
            String failureReason = STBImage.stbi_failure_reason();
            MyWorldTrafficAddition.LOGGER.error("Failed to encode image to PNG! Aborting...\nDetails: {}", failureReason);
            throw new RuntimeException(failureReason);
        }

        return outputStream.toByteArray();
    }
}
