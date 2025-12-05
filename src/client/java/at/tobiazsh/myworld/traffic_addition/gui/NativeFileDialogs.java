package at.tobiazsh.myworld.traffic_addition.gui;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

public class NativeFileDialogs {

    public static String open(String title, FilterItem filterItem, String defaultPath, Consumer<String> onAbort) {

        ByteBuffer titleBuffer = MemoryUtil.memUTF8(title);
        ByteBuffer pathBuffer = MemoryUtil.memUTF8(defaultPath);

        ByteBuffer[] extsBuffers = filterItem.getExtAsByteBuffers();

        // Build char** array (array of pointers)
        LongBuffer ptrArray = MemoryUtil.memAllocLong(extsBuffers.length);
        for (int i = 0; i < extsBuffers.length; i++) {
            ptrArray.put(i, MemoryUtil.memAddress(extsBuffers[i]));
        }

        // Name/description buffer
        ByteBuffer nameBuffer = filterItem.getNameAsByteBuffer(); // adjust as needed

        long resultPtr = TinyFileDialogs.ntinyfd_openFileDialog(
                MemoryUtil.memAddress(titleBuffer),
                MemoryUtil.memAddress(pathBuffer),
                extsBuffers.length,
                MemoryUtil.memAddress(ptrArray),
                MemoryUtil.memAddress(nameBuffer),
                0
        );

        String result = resultPtr != 0 ? MemoryUtil.memUTF8(resultPtr) : null;

        // Free memory
        for (ByteBuffer extBuffer : extsBuffers)
            MemoryUtil.memFree(extBuffer);

        MemoryUtil.memFree(titleBuffer);
        MemoryUtil.memFree(pathBuffer);
        MemoryUtil.memFree(ptrArray);
        MemoryUtil.memFree(nameBuffer);

        if (result == null) {
            onAbort.accept("User canceled the open dialog or an error occurred.");
            return null;
        }

        return result;
    }

    public static String save(String title, FilterItem filterItem, String defaultPath, String defaultFileName, Consumer<String> onAbort) {

        String defaultPathAndFile = defaultPath.endsWith("/") || defaultPath.endsWith("\\")
                ? defaultPath + defaultFileName
                : defaultPath + "/" + defaultFileName;

        ByteBuffer titleBuffer = MemoryUtil.memUTF8(title);
        ByteBuffer pathAndFileBuffer = MemoryUtil.memUTF8(defaultPathAndFile);

        ByteBuffer[] extsBuffers = filterItem.getExtAsByteBuffers();

        // Build char** array (array of pointers)
        LongBuffer ptrArray = MemoryUtil.memAllocLong(extsBuffers.length);
        for (int i = 0; i < extsBuffers.length; i++) {
            ptrArray.put(i, MemoryUtil.memAddress(extsBuffers[i]));
        }

        ByteBuffer nameBuffer = filterItem.getNameAsByteBuffer(); // adjust as needed

        long resultPtr = TinyFileDialogs.ntinyfd_saveFileDialog(
                MemoryUtil.memAddress(titleBuffer),
                MemoryUtil.memAddress(pathAndFileBuffer),
                extsBuffers.length,
                MemoryUtil.memAddress(ptrArray),
                MemoryUtil.memAddress(nameBuffer)
        );

        String result = resultPtr != 0 ? MemoryUtil.memUTF8(resultPtr) : null;

        // Free memory
        for (ByteBuffer extBuffer : extsBuffers)
            MemoryUtil.memFree(extBuffer);

        MemoryUtil.memFree(titleBuffer);
        MemoryUtil.memFree(pathAndFileBuffer);
        MemoryUtil.memFree(ptrArray);
        MemoryUtil.memFree(nameBuffer);

        if (result == null) {
            onAbort.accept("User canceled the save dialog or an error occurred.");
            return null;
        }

        return result;
    }

    public record FilterItem(String name, String[] ext) {
        /**
         * Convert extensions to ByteBuffers for TinyFD. Must be freed after use.
         * @return Array of ByteBuffers representing the extensions.
         */
        public ByteBuffer[] getExtAsByteBuffers() {
            ByteBuffer[] exts = new ByteBuffer[ext.length];

            for (int i = 0; i < ext.length; i++)
                exts[i] = MemoryUtil.memUTF8(ext[i]);

            return exts;
        }

        /**
         * Convert name to ByteBuffer for TinyFD. Must be freed after use.
         * @return ByteBuffer representing the name.
         */
        public ByteBuffer getNameAsByteBuffer() {
            return MemoryUtil.memUTF8(name);
        }
    }
}
