package at.tobiazsh.myworld.traffic_addition.image;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
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
import java.nio.IntBuffer;
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
     * @param scale Scale to scale the image to
     * @param width Current width
     * @param height Current height
     * @param channels The channels of the image. This will later correlate to STBImageResize formats "stbir_pixel_formats". For more information, please take a look at {@link STBImageResize}
     * @return Triplet(A, B, C)
     * <ul>
     * <li>
     *     A = Width of the scaled image (Integer)
     * </li>
     * <li>
     *     B = Height of the scaled image (Integer)
     * </li>
     * <li>
     *     C = ByteBuffer containing the raw pixel data
     * </li>
     * </ul>
     */
    public static Triplet<Integer, Integer, ByteBuffer> scaleImage(ByteBuffer imageData, float scale, int width, int height, int channels, Runnable onAbort) {
        int newWidth = (int) Math.ceil(width * scale);
        int newHeight = (int) Math.ceil(height * scale);

        if (imageData == null || imageData.remaining() == 0) {
            MyWorldTrafficAddition.LOGGER.error("No valid image data provided for scaling! Aborting...");
            onAbort.run();
            return new Triplet<>(0, 0, null);
        }

        ByteBuffer output = MemoryUtil.memAlloc(newWidth * newHeight * channels);
        boolean success = ImageOperations.bilinearResize(imageData, width, height, output, newWidth, newHeight, channels);

        if (!success) { // Abort if unsuccessful (empty)
            MyWorldTrafficAddition.LOGGER.error("Failed to resize image! Aborting...");
            onAbort.run();
            return new Triplet<>(0, 0, null);
        }

        // Already flipped in the resize operation
        return new Triplet<>(newWidth, newHeight, output);
    }

    /**
     * Encodes raw pixel data to PNG format
     * @param pixelData Raw pixel data
     * @param width Width of the image
     * @param height Height of the image
     * @param channels Number of channels
     * @return The PNG encoded image as byte array
     */
    public static byte[] encodePNG(ByteBuffer pixelData, int width, int height, int channels, Runnable onAbort) {
        if (pixelData == null) {
            MyWorldTrafficAddition.LOGGER.error("No image data provided for PNG encoding! Aborting...");
            throw new IllegalArgumentException("pixelData is null");
        }

        if (channels < 1 || channels > 4) {
            if (onAbort != null) onAbort.run();
            MyWorldTrafficAddition.LOGGER.error("Invalid channels on encoding to PNG! Aborting...");
            return null;
        }

        int required = width * height * channels;
        // Create a read-only/duplicated view and rewind to ensure readable bytes from position 0
        boolean tmpAllocated = false;
        ByteBuffer pixels = pixelData;
        pixels = pixels.asReadOnlyBuffer();
        pixels.rewind();

        if (!pixels.isDirect()) {
            ByteBuffer tmp = MemoryUtil.memAlloc(pixels.remaining());
            tmp.put(pixels);
            tmp.flip();
            pixels = tmp;
            tmpAllocated = true;
        }

        if (pixels.remaining() < required) {
            MyWorldTrafficAddition.LOGGER.error("Image data has insufficient remaining bytes: {} < {}. Aborting...", pixels.remaining(), required);
            throw new IllegalArgumentException("Not enough image data bytes for given dimensions");
        }

        final int stride = width * channels;
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        STBIWriteCallback callback = STBIWriteCallback.create((context, data, size) ->  {
            if (size <= 0) return;
            ByteBuffer bb = MemoryUtil.memByteBuffer(data, size);
            byte[] buffer = new byte[size];
            bb.get(buffer);
            try {
                outputStream.write(buffer);
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to encode image! Aborting...");
                onAbort.run();
                throw new RuntimeException(e);
            }
        });

        try {

            boolean success = STBImageWrite.stbi_write_png_to_func(
                    callback,
                    0,
                    width,
                    height,
                    channels,
                    pixels,
                    stride
            );

            if (!success) {
                String failureReason = STBImage.stbi_failure_reason();
                MyWorldTrafficAddition.LOGGER.error("Failed to encode image to PNG! Aborting...\nDetails: {}", failureReason);
                onAbort.run();
                throw new RuntimeException(failureReason);
            }

            return outputStream.toByteArray();
        } finally {
            callback.free();

            if (tmpAllocated)
                MemoryUtil.memFree(pixels);

            try { outputStream.close(); } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to close output stream after PNG encoding!", e);
                onAbort.run();
            }
        }
    }

    public static boolean isValidImage(ByteBuffer imageData) {
        IntBuffer w = MemoryUtil.memAllocInt(1);
        IntBuffer h = MemoryUtil.memAllocInt(1);
        IntBuffer c = MemoryUtil.memAllocInt(1);

        boolean valid = STBImage.stbi_info_from_memory(imageData, w, h, c);

        MemoryUtil.memFree(w);
        MemoryUtil.memFree(h);
        MemoryUtil.memFree(c);

        return valid;
    }
}
