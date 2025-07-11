package at.tobiazsh.myworld.traffic_addition.utils.texturing;

import java.nio.ByteBuffer;

public class ImageOperations {

    /**
     * Resizes an image stored in a ByteBuffer using bi-linear scaling.
     *
     * @param pixelData The input ByteBuffer containing the original image (row-major).
     * @param srcWidth Width of the source image.
     * @param srcHeight Height of the source image.
     * @param destWidth Desired output width.
     * @param destHeight Desired output height.
     * @param channels Number of color channels (e.g., 3 = RGB, 4 = RGBA).
     * @return true => Success, false => Failure
     */
    public static boolean bilinearResize(ByteBuffer pixelData, int srcWidth, int srcHeight, ByteBuffer outputData, int destWidth, int destHeight, int channels) {

        if (pixelData == null || srcWidth <= 0 || srcHeight <= 0 ||
                destWidth <= 0 || destHeight <= 0 || channels <= 0) {
            return false; // invalid input
        }

        float xRatio = (float)(srcWidth - 1) / destWidth;
        float yRatio = (float)(srcHeight - 1) / destHeight;

        pixelData.rewind();
        outputData.rewind();

        byte[] pixel = new byte[channels];

        for (int y = 0; y < destHeight; y++) {
            float yFloat = y * yRatio;
            int yInt = (int) yFloat;
            float yDiff = yFloat - yInt;

            for (int x = 0; x < destWidth; x++) {
                float xFloat = x * xRatio;
                int xInt = (int) xFloat;
                float xDiff = xFloat - xInt;

                // Get four surrounding pixels
                byte[] p00 = getPixel(pixelData, srcWidth, xInt, yInt, channels);
                byte[] p01 = getPixel(pixelData, srcWidth, xInt + 1, yInt, channels);
                byte[] p10 = getPixel(pixelData, srcWidth, xInt, yInt + 1, channels);
                byte[] p11 = getPixel(pixelData, srcWidth, xInt + 1, yInt + 1, channels);

                // Perform bilinear interpolation for each channel
                for (int c = 0; c < channels; c++) {
                    float v00 = p00[c] & 0xFF;
                    float v01 = p01[c] & 0xFF;
                    float v10 = p10[c] & 0xFF;
                    float v11 = p11[c] & 0xFF;

                    // Interpolate vertically first
                    float top = v00 + (v01 - v00) * yDiff;
                    float bottom = v10 + (v11 - v10) * yDiff;

                    // Then interpolate horizontally
                    float value = top + (bottom - top) * xDiff;

                    pixel[c] = (byte)(value + 0.5f); // Round to nearest integer
                }

                // Put the interpolated pixel in the output buffer
                outputData.put(pixel);
            }
        }

        outputData.flip();

        return true;
    }

    private static byte[] getPixel(ByteBuffer buffer, int width, int x, int y, int channels) {
        // Clamp coordinates to image bounds
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, (buffer.capacity() / (width * channels)) - 1));

        // Calculate position in buffer
        int pos = (y * width + x) * channels;

        // Read pixel
        byte[] pixel = new byte[channels];
        buffer.position(pos);
        buffer.get(pixel, 0, channels);
        return pixel;
    }
}
