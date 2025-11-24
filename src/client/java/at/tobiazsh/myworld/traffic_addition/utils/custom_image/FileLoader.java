package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileLoader {

    public static ByteImage loadFileToStbImage(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        ByteBuffer encodedBuffer = MemoryUtil.memAlloc(bytes.length);
        encodedBuffer.put(bytes).flip();

        ByteImage image;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer stbImage = STBImage.stbi_load_from_memory(encodedBuffer, width, height, channels, 0);

            if (stbImage == null) {
                throw new IOException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            image = new ByteImage(stbImage, width.get(0), height.get(0), channels.get(0));
        }

        return image;
    }

}
