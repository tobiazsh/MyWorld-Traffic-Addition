package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import at.tobiazsh.myworld.traffic_addition.utils.ImageUtils;
import net.minecraft.util.Pair;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog.maximumUploadSize;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class ImageDownloader {

    private final Function<String, String> errorTitleSetter;
    private final Function<String, String> errorMessageSetter;
    private final Function<String, String> operationMessageSetter;
    private final Function<Float, Float> operationProgressSetter;

    volatile private boolean cancelDownload = false;

    volatile private ByteBuffer imageData = null;

    public ImageDownloader(
            Function<String, String> errorTitleSetter,
            Function<String, String> errorMessageSetter,
            Function<String, String> operationMessageSetter,
            Function<Float, Float> operationProgressSetter
    ) {
        this.errorTitleSetter = errorTitleSetter;
        this.errorMessageSetter = errorMessageSetter;
        this.operationMessageSetter = operationMessageSetter;
        this.operationProgressSetter = operationProgressSetter;
    }

    /**
     * Downloads the image from the given URL into the returned Path
     * @param url Image URL (make sure it's only the image and not a website)
     * @return Pair<A, B> where A is the status code (0 = success, 1 = error) and B is result. If the status code is 0, B is the Path to the downloaded image, otherwise it's the error message.
     */
    public Pair<Integer, String> downloadImage(String url, IntBuffer orgImgW, IntBuffer orgImgH, IntBuffer imgW, IntBuffer imgH, IntBuffer imgC) {
        URL imageUrl;

        // Set status
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Constructing URL"));

        // Handle Empty URL
        if (url == null || url.isEmpty()) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "No URL Provided"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Please provide a valid URL to download the image from"));

            return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "No URL Provided"));
        }

        // Try to create a URL object from the given string
        try {
            imageUrl = URI.create(url).toURL();
        } catch (MalformedURLException e) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Malformed URL"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The URL you provided is malformed. Download action has been aborted! Please check the URL and try again. Otherwise, please check the logs"));

            return new Pair<>(1, "Malformed image URL!\nURL: " + url + "\nJava's Nonsense: " + e.getMessage());
        }

        operationProgressSetter.apply(0.25f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Opening Connection"));

        URLConnection connection; // URLConnection to connect to the image URL

        try {
            connection = imageUrl.openConnection();
        } catch (IOException e) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Connection Failed"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open a connection to the URL. Download Action has been aborted! Please check your Internet and try again"));

            return new Pair<>(1, "Couldn't open connection to URL! Java's Nonsense: " + e.getMessage());
        }

        long totalBytes = connection.getContentLength();
        if (totalBytes <= 0) {
            totalBytes = 1;
        }

        if (totalBytes > maximumUploadSize) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "File Too Large"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The file is too large to be downloaded. Maximum size is ") + (maximumUploadSize / 1024) + " KiB.");

            return new Pair<>(1, "File too large! File size: " + (totalBytes / 1024) + " KiB");
        }

        operationProgressSetter.apply(0.0f); // Reset for download Progress
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading Image"));

        // Validate if the file is an image
        InputStream inputStream;

        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Input Stream Failed"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open an input stream to the URL. Download Action has been aborted! Please check your Internet and the URL and try again"));

            return new Pair<>(1, "Couldn't open input stream to URL! URL: " + url + "\nJava's Nonsense: " + e.getMessage());
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) totalBytes);

        try {
            byte[] buffer = new byte[8192];
            long bytesRead = 0;
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, read);
                bytesRead += read;

                // Update download progress
                operationProgressSetter.apply((float) bytesRead / totalBytes);
                operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading") + "... " + (bytesRead / 1024) + " KiB " + tr("Global", "of") + " " + (totalBytes / 1024) + " KiB");

                if (cancelDownload) {
                    deleteImageData();
                    return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog", "Download Cancelled By User"));
                }
            }

            byteBuffer.flip();
            inputStream.close();
            imageData = byteBuffer;
        } catch (IOException e) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Download Failed"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while downloading the file. Please check your Internet connection and try again"));

            return new Pair<>(1, "Error downloading file from URL " + url + "\nJava's Nonsense: " + e.getMessage());
        }

        operationProgressSetter.apply(0.75f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Validating Image"));

        // Validate if the file is an image
        byte[] imageBytes = new byte[imageData.remaining()];
        imageData.mark();
        imageData.get(imageBytes);
        imageData.reset();

        try (InputStream validationStream = new ByteArrayInputStream(imageBytes)) {
            if (ImageIO.read(validationStream) == null || (Objects.equals(ImageUtils.getImageFormat(imageBytes), "webp"))) {
                errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Invalid Image"));
                errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The downloaded file is not a valid or supported image. Please check the URL and format and try again"));

                deleteImageData();
                return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Downloaded file is not a valid image"));
            }
        } catch (IOException e) {
            errorTitleSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Validation Failed"));
            errorMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while validating the image. Please check the link and try again"));

            return new Pair<>(1, "Error validating image file!\nJava's Nonsense: " + e.getMessage());
        }

        operationProgressSetter.apply(1.0f);
        operationMessageSetter.apply(tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloaded Image Successfully"));

        imageData = stbi_load_from_memory(imageData, imgW, imgH, imgC, 0);
        orgImgW.put(0, imgW.get(0));
        orgImgH.put(0, imgH.get(0));

        return new Pair<>(0, "");
    }

    public void cancelDownload() {
        this.cancelDownload = true;
    }

    public ByteBuffer getDownloadedImageData() {
        return imageData;
    }

    private void deleteImageData() {
        imageData = null;
    }
}
