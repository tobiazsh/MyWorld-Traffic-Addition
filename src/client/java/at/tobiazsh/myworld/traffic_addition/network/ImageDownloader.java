package at.tobiazsh.myworld.traffic_addition.network;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.error.Error;
import at.tobiazsh.myworld.traffic_addition.image.ByteImage;
import at.tobiazsh.myworld.traffic_addition.image.ImageUtils;
import net.minecraft.util.Pair;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog.maximumUploadSize;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class ImageDownloader {

    private static final Pair<String, String> requestProperty = new Pair<>("User-Agent", "Mozilla/5.0 (Compatible) MyWorldTrafficAddon/" + MyWorldTrafficAddition.MOD_VERSION);
    private static final int connectTimeout = 15000; // 15 seconds
    private static final int readTimeout = 30000; // 15 seconds

    private Error error;
    private final Function<String, String> operationMessageSetter;
    private final Function<Float, Float> operationProgressSetter;

    volatile private boolean cancelDownload = false;

    public ImageDownloader(
            Function<String, String> operationMessageSetter,
            Function<Float, Float> operationProgressSetter
    ) {
        this.operationMessageSetter = operationMessageSetter;
        this.operationProgressSetter = operationProgressSetter;
    }

    ByteBuffer downloadedData = null;
    IntBuffer imgWidth = null;
    IntBuffer imgHeight = null;
    IntBuffer imgChannels = null;

    /**
     * Downloads the image from the given URL into the returned Path
     * @param url Image URL (make sure it's only the image and not a website)
     * @return DownloadedImage object containing the raw image data and its properties (including error state!)
     */
    public ByteImage downloadImage(String url) {
        URL imageUrl;

        // Set status
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Constructing URL"));

        // Handle Empty URL
        if (url == null || url.isEmpty()) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "No URL Provided"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Please provide a valid URL to download the image from"));

            return null;
        }

        // Try to create a URL object from the given string
        try {
            imageUrl = URI.create(url).toURL();
        } catch (MalformedURLException e) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Malformed URL"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The URL you provided is malformed. Download action has been aborted! Please check the URL and try again. Otherwise, please check the logs"));

            return null;
        }

        operationProgressSetter.apply(0.25f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Opening Connection"));

        URLConnection connection; // URLConnection to connect to the image URL

        try {
            connection = imageUrl.openConnection();
        } catch (IOException e) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Connection Failed"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open a connection to the URL. Download Action has been aborted! Please check your Internet and try again"));

            return null;
        }

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestProperty(requestProperty.getLeft(), requestProperty.getRight());

        long totalBytes = connection.getContentLength();
        if (totalBytes <= 0) {
            totalBytes = 1;
        }

        if (totalBytes > maximumUploadSize) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "File Too Large"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The file is too large to be downloaded. Maximum size is ") + (maximumUploadSize / 1024) + " KiB.");

            return null;
        }

        operationProgressSetter.apply(0.0f); // Reset for download Progress
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading Image"));

        // Open input stream to read data
        InputStream inputStream;

        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Input Stream Failed"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open an input stream to the URL. Download Action has been aborted! Please check your Internet and the URL and try again"));

            return null;
        }

        try {
            downloadedData = MemoryUtil.memAlloc((int) totalBytes > 0 ? (int) totalBytes : 1024 * 1024);

            byte[] buffer = new byte[8192];
            int read;
            long bytesRead = 0;

            while ((read = inputStream.read(buffer)) != -1) {
                downloadedData.put(buffer, 0, read);
                bytesRead += read;

                // Update download progress
                float progress = totalBytes > 0 ? (float) bytesRead / totalBytes : 0.5f; // If totalBytes is unknown, just set it to 50%
                operationProgressSetter.apply(progress);
                operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading") + "... "
                        + (bytesRead / 1024) + " KiB " + tr("Global", "Of") + " " + (totalBytes / 1024) + " KiB");

                if (cancelDownload) {
                    freeMemory();
                    applyError("Cancelled", tr("ImGui.Child.PopUps.OnlineImageDialog", "Download Cancelled By User"));
                    return null;
                }
            }

            downloadedData.flip();

            inputStream.close();
        } catch (IOException e) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Download Failed"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while downloading the file. Please check your Internet connection and try again"));

            freeMemory(); // Free memory because allocated for the first time above.. EVERY Error will now ALWAYS free memory!
            return null;
        }

        operationProgressSetter.apply(0.75f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Validating Image"));

        // Validate if the file is an image
        boolean isValidImage = ImageUtils.isValidImage(downloadedData);

        if (!isValidImage) {
            applyError(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Invalid Image"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The downloaded file is not a valid or supported image. Please check the URL and format and try again"));

            freeMemory();
            return null;
        }

        operationProgressSetter.apply(1.0f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloaded Image Successfully"));

        imgWidth = MemoryUtil.memAllocInt(1);
        imgHeight = MemoryUtil.memAllocInt(1);
        imgChannels = MemoryUtil.memAllocInt(1);

        ByteBuffer stbiImage = stbi_load_from_memory(downloadedData, imgWidth, imgHeight, imgChannels, 0);

        int width = imgWidth.get(0);
        int height = imgHeight.get(0);
        int channels = imgChannels.get(0);

        freeMemory();

        return new ByteImage(
                stbiImage,
                width,
                height,
                channels
        );
    }

    private void applyError(String title, String message) {
        error = new Error(title, message);
    }

    public Error getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

    public void cancelDownload() {
        this.cancelDownload = true;
    }

    private void freeMemory() {
        if (downloadedData != null) {
            MemoryUtil.memFree(downloadedData);
            downloadedData = null;
        }

        if (imgWidth != null) {
            MemoryUtil.memFree(imgWidth);
            imgWidth = null;
        }

        if (imgHeight != null) {
            MemoryUtil.memFree(imgHeight);
            imgHeight = null;
        }

        if (imgChannels != null) {
            MemoryUtil.memFree(imgChannels);
            imgChannels = null;
        }
    }
}
