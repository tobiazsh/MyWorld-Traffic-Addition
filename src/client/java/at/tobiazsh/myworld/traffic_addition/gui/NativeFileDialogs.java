package at.tobiazsh.myworld.traffic_addition.gui;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Utility class providing native file dialogs using LWJGL bindings for TinyFileDialogs.
 * <p>
 * This class handles low-level memory allocation and conversion between Java types
 * and native pointers required by TinyFD. All allocated native memory is freed
 * after use to prevent memory leaks.
 * <p>
 * Note: All methods in this class are blocking and should not be called on the render thread
 * if UI responsiveness is important.
 */
public class NativeFileDialogs {

    /**
     * High-Level API for opening a "Save File" dialog, creating parent directories and writing data.
     * @param title             The title displayed on the dialog window.
     * @param filter            The filter describing allowed files extensions and their label.
     * @param defaultPath       The initial directory path shown when the dialog opens.
     * @param defaultFileName   The default file name pre-filled in the dialog.
     * @param data              The data to write after the user selected a directory
     * @param onAbort           Callback, in case the user aborts
     * @return Aborted or not
     *
     * @throws IOException If an I/O error occurs writing to or creating the file,
     * or if the parent directory does not exist and cannot be created.
     */
    public static boolean writeFileWithDialog(
            String title,
            FilterItem filter,
            Path defaultPath,
            String defaultFileName,
            byte[] data,
            Consumer<String> onAbort
    ) throws IOException {
        String selectedPath = save(title, filter, defaultPath.toString(), defaultFileName, onAbort);

        if (selectedPath == null) // onAbort already invoked in save()-method
            return false;

        Path filePath = Path.of(selectedPath);

        Files.createDirectories(filePath.getParent()); // Create all parent directories
        Files.write(filePath, data);

        return true;
    }

    /**
     * High-Level API for opening an "Open File" dialog, creating parent directories and writing data.
     * @param title         The title displayed on the dialog window.
     * @param filter        The filter describing allowed files extensions and their label.
     * @param defaultPath   The initial directory path shown when the dialog opens.
     * @param onAbort       Callback, in case the user aborts.
     * @return Read data or empty array if aborted
     *
     * @throws IOException If an I/O error occurs reading the file, or if the selected path is invalid.
     */
    public static byte[] readFileWithDialog(
            String title,
            FilterItem filter,
            Path defaultPath,
            Consumer<String> onAbort
    ) throws IOException {
        String selectedPath = open(title, filter, defaultPath.toString(), onAbort);

        if (selectedPath == null) // onAbort already invoked in save()-method
            return new byte[]{};

        Path filePath = Path.of(selectedPath);
        return Files.readAllBytes(filePath);
    }

    /**
     * Opens a native file selection dialog.
     *
     * @param title       The title displayed on the dialog window.
     * @param filterItem  The filter describing allowed file extensions and their label.
     * @param defaultPath The initial directory path shown when the dialog opens.
     * @param onAbort     Callback invoked if the user cancels the dialog or an error occurs.
     *                    Receives a descriptive message.
     * @return The selected file path as a {@link String}, or {@code null} if the dialog was canceled.
     *
     * @implNote This method allocates native memory for strings and extension arrays.
     * All allocated memory is freed before returning.
     */
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

    /**
     * Opens a native file save dialog.
     *
     * @param title           The title displayed on the dialog window.
     * @param filterItem      The filter describing allowed file extensions and their label.
     * @param defaultPath     The initial directory path shown when the dialog opens.
     * @param defaultFileName The default file name pre-filled in the dialog.
     * @param onAbort         Callback invoked if the user cancels the dialog or an error occurs.
     *                        Receives a descriptive message.
     * @return The selected file path as a {@link String}, or {@code null} if the dialog was canceled.
     *
     * @implNote Automatically combines {@code defaultPath} and {@code defaultFileName}.
     * Handles both Unix (/) and Windows (\) path separators.
     * <p>
     * This method allocates native memory which is freed before returning.
     */
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


    /**
     * Represents a file dialog filter item consisting of a display name
     * and a list of allowed file extensions.
     *
     * @param name Human-readable description of the filter (e.g., "JSON Files").
     * @param ext  Array of file extensions (e.g., {"*.json", "*.txt"}).
     */
    public record FilterItem(String name, String[] ext) {

        /**
         * Converts the extension strings into UTF-8 encoded {@link ByteBuffer}s
         * suitable for use with TinyFileDialogs.
         *
         * @return An array of {@link ByteBuffer}s representing the extensions.
         *
         * @implNote The returned buffers are allocated using {@link MemoryUtil#memUTF8(CharSequence)}
         * and must be freed manually using {@link MemoryUtil#memFree(Buffer)}.
         */
        public ByteBuffer[] getExtAsByteBuffers() {
            ByteBuffer[] exts = new ByteBuffer[ext.length];

            for (int i = 0; i < ext.length; i++)
                exts[i] = MemoryUtil.memUTF8(ext[i]);

            return exts;
        }

        /**
         * Converts the filter name into a UTF-8 encoded {@link ByteBuffer}
         * suitable for use with TinyFileDialogs.
         *
         * @return A {@link ByteBuffer} representing the filter name.
         *
         * @implNote The returned buffer must be freed manually using
         * {@link MemoryUtil#memFree(Buffer)} after use.
         */
        public ByteBuffer getNameAsByteBuffer() {
            return MemoryUtil.memUTF8(name);
        }
    }
}
