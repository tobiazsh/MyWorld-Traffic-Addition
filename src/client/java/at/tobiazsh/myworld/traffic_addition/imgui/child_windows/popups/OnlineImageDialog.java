package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.networking.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.custom_image.ClientCustomImageDirectory;
import at.tobiazsh.myworld.traffic_addition.utils.Crypto;
import at.tobiazsh.myworld.traffic_addition.utils.ImageUtils;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.ImageOperations;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Textures;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class OnlineImageDialog {
    private static boolean shouldOpen = false;
    private static boolean shouldRender = false;

    // Regarding Download
    private static boolean isOperating = false;
    private static boolean shouldOpenProgressPopup = false;
    private static boolean isOperationComplete = false;
    private static float operationProgress = 0.0f;
    private static String operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Operation Message Default");
    private static boolean cancelDownload = false;

    private static ImString imageUrl = new ImString(512);

    // Regarding error handling after the download in the main thread
    private static String errorTitle = "";
    private static String errorMessage = "";
    private static boolean hasError = false;

    // Image things
    private static ByteBuffer imageData;
    private static ByteBuffer originalImageData;
    private static Texture currentTexture = null;
    private static IntBuffer imgW = BufferUtils.createIntBuffer(1); // Width
    private static IntBuffer imgH = BufferUtils.createIntBuffer(1); // Height
    private static IntBuffer imgC = BufferUtils.createIntBuffer(1); // Channels
    private static IntBuffer orgImgW = BufferUtils.createIntBuffer(1); // Original Width
    private static IntBuffer orgImgH = BufferUtils.createIntBuffer(1);
    private static final ImString imageName = new ImString(128);
    private static ImBoolean hideForOthers = new ImBoolean(false);

    // Window
    private static float windowWidth = 500;
    private static float windowHeight = 160;

    // Server things
    public static long maximumUploadSize = 1024 * 1024 * 5; // 5 MiB
    public static float[] imageScale = { 1.0f };

    public enum OnlineImageDialogPage {
        NEW,
        EDIT,
        CONFIRM
    }

    public static OnlineImageDialogPage currentPage = null;

    public static void render() {
        if (!shouldRender) return; // Prevent rendering if not necessary

        ImGui.setNextWindowSize(windowWidth, windowHeight);
        if (ImGui.beginPopupModal(tr("ImGui.Child.PopUps.OnlineImageDialog", "Online Image Uploader"))) {

            switch (currentPage) {
                case NEW -> renderNewPage();
                case EDIT -> renderEditPage();
                case CONFIRM -> renderConfirmPage();
            }

            ConfirmationPopup.render(); // Render confirmation popup

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(tr("ImGui.Child.PopUps.OnlineImageDialog", "Online Image Uploader"));
            shouldOpen = false;
        }
    }

    public static void startDialog() {
        shouldRender = true;
        shouldOpen = true;
        currentPage = OnlineImageDialogPage.NEW;

        CustomClientNetworking.getInstance().sendStringToServer(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_maximum_image_upload_size"), "dummy");

        imageUrl = new ImString(1024);
    }

    private static void handleDownload() {
        resetValues();
        isOperating = true;

        // Execute download in separate thread to prevent blocking the UI and giving feedback to the user
        new Thread(() -> {
            Pair<Integer, String> result = downloadImage(imageUrl.get());

            if (result.getLeft() == 1)
                MyWorldTrafficAddition.LOGGER.error(result.getRight());

        }).start();

        shouldOpenProgressPopup = true;
    }

    /**
     * Downloads the image from the given URL into the returned Path
     * @param url Image URL (make sure it's only the image and not a website)
     * @return Pair<A, B> where A is the status code (0 = success, 1 = error) and B is result. If the status code is 0, B is the Path to the downloaded image, otherwise it's the error message.
     */
    private static Pair<Integer, String> downloadImage(String url) {
        URL imageUrl;

        // Set status
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Constructing URL");

        // Handle Empty URL
        if (url == null || url.isEmpty()) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "No URL Provided");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Please provide a valid URL to download the image from");
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "No URL Provided"));
        }

        // Try to create a URL object from the given string
        try {
            imageUrl = URI.create(url).toURL();
        } catch (MalformedURLException e) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Malformed URL");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The URL you provided is malformed. Download action has been aborted! Please check the URL and try again. Otherwise, please check the logs");
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, "Malformed image URL!\nURL: " + url + "\nJava's Nonsense: " + e.getMessage());
        }

        operationProgress = 0.25f;
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Opening Connection");

        URLConnection connection; // URLConnection to connect to the image URL

        try {
            connection = imageUrl.openConnection();
        } catch (IOException e) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Connection Failed");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open a connection to the URL. Download Action has been aborted! Please check your Internet and try again");
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, "Couldn't open connection to URL! Java's Nonsense: " + e.getMessage());
        }

        long totalBytes = connection.getContentLength();
        if (totalBytes <= 0) {
            totalBytes = 1;
        }

        if (totalBytes > maximumUploadSize) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "File Too Large");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The file is too large to be downloaded. Maximum size is ") + (maximumUploadSize / 1024) + " KiB.";
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, "File too large! File size: " + (totalBytes / 1024) + " KiB");
        }

        operationProgress = 0.0f; // Reset for download Progress
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading Image");

        // Validate if the file is an image
        InputStream inputStream;

        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Input Stream Failed");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while trying to open an input stream to the URL. Download Action has been aborted! Please check your Internet and the URL and try again");
            hasError = true;
            isOperationComplete = true;

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
                operationProgress = (float) bytesRead / totalBytes;
                operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloading") + "... " + (bytesRead / 1024) + " KiB " + tr("Global", "of") + " " + (totalBytes / 1024) + " KiB";

                if (cancelDownload) {
                    deleteImageData(false);
                    return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog", "Download Cancelled By User"));
                }
            }

            byteBuffer.flip();
            inputStream.close();
            imageData = byteBuffer;
        } catch (IOException e) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Download Failed");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while downloading the file. Please check your Internet connection and try again");
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, "Error downloading file from URL " + url + "\nJava's Nonsense: " + e.getMessage());
        }

        operationProgress = 0.75f;
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Validating Image");

        // Validate if the file is an image
        byte[] imageBytes = new byte[imageData.remaining()];
        imageData.mark();
        imageData.get(imageBytes);
        imageData.reset();

        try (InputStream validationStream = new ByteArrayInputStream(imageBytes)) {
            if (ImageIO.read(validationStream) == null || (Objects.equals(ImageUtils.getImageFormat(imageBytes), "webp"))) {
                errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Invalid Image");
                errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "The downloaded file is not a valid or supported image. Please check the URL and format and try again");
                hasError = true;
                isOperationComplete = true;

                deleteImageData(true);
                return new Pair<>(1, tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Downloaded file is not a valid image"));
            }
        } catch (IOException e) {
            errorTitle = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Validation Failed");
            errorMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "An error occurred while validating the image. Please check the link and try again");
            hasError = true;
            isOperationComplete = true;

            return new Pair<>(1, "Error validating image file!\nJava's Nonsense: " + e.getMessage());
        }

        operationProgress = 1.0f;
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Downloaded Image Successfully");
        isOperationComplete = true;

        imageData = stbi_load_from_memory(imageData, imgW, imgH, imgC, 0);
        orgImgW.put(0, imgW.get(0));
        orgImgH.put(0, imgH.get(0));

        return new Pair<>(0, "");
    }

    private static void handleCancel() {
        cancelDownload = true;
        resetValues();
    }

    private static void resetValues() {
        resetProgressPopup();
        windowWidth = 500;
        windowHeight = 160;
        deleteImageData(true);
        imgW = BufferUtils.createIntBuffer(1);
        imgH = BufferUtils.createIntBuffer(1);
        imgC = BufferUtils.createIntBuffer(1);
        orgImgW = BufferUtils.createIntBuffer(1);
        orgImgH = BufferUtils.createIntBuffer(1);
        imageScale[0] = 1.0f;
        imageName.set("");
        hideForOthers = new ImBoolean(false);
    }

    private static void resetProgressPopup() {
        operationProgress = 0.0f;
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Waiting");
        isOperating = false;
        cancelDownload = false;
        shouldOpenProgressPopup = false;
        isOperationComplete = false;
        progressPopupWidth = 500;
        progressPopupHeight = 160;
        hasError = false;
    }

    private static String progressPopupTitle = tr("Global", "Download");
    private static float progressPopupWidth = 500;
    private static float progressPopupHeight = 160;

    private static void renderProgressPopup(boolean showCancel, Runnable onClose, Runnable onCancel) {
        ImGui.setNextWindowSize(progressPopupWidth, progressPopupHeight);
        if (ImGui.beginPopupModal(tr("ImGui.Child.PopUps.OnlineImageDialog", "Operation Progress"))) {

            ImGui.pushFont(ImGuiImpl.RobotoBoldMedium); // Set font to bold
            ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(progressPopupTitle)) / 2); // Center title
            ImGui.text(progressPopupTitle); // Display title
            ImGui.popFont(); // Pop Font

            ImGui.progressBar(operationProgress);
            ImGui.textWrapped(operationMessage); // Status text

            if (!isOperationComplete) {
                if (showCancel && ImGui.button(tr("Global", "Cancel"))) onCancel.run();
            } else {
                if (ImGui.button(tr("Global", "Close"))) {
                    onClose.run();
                }

                if (hasError) {
                    operationMessage = tr("Global", "Error");
                    progressPopupHeight = 300;
                    ImGui.separator();
                    ImGui.pushFont(ImGuiImpl.RobotoBold);
                    ImGui.textWrapped(errorTitle);
                    ImGui.popFont();
                    ImGui.textWrapped(errorMessage);
                }
            }

            ImGui.endPopup();
        }

        if (shouldOpenProgressPopup) {
            ImGui.openPopup(tr("ImGui.Child.PopUps.OnlineImageDialog", "Operation Progress"));
            shouldOpenProgressPopup = false;
        }
    }

    private static void renderNewPage() {
        windowWidth = 800;
        windowHeight = 140;

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Enter Image URL"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Enter Image URL")); // Display title
        ImGui.popFont(); // Pop Font

        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Center input field
        ImGui.inputText("##imageUrlDummylabel", imageUrl); // Input field for URL
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Center button

        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageDialog", "Load from URL"))) handleDownload();

        ImGui.sameLine();

        if (ImGui.button(tr("Global", "Cancel"))) {
            resetValues();
            shouldRender = false;
            ImGui.closeCurrentPopup();
        }

        if (isOperating) {
            // Show progress in separate window
            renderProgressPopup(true, () -> {
                ImGui.closeCurrentPopup();

                if (!hasError) {
                    currentPage = OnlineImageDialogPage.EDIT; // Go to next page
                    uploadImage(); // Upload image to GPU
                    createImageBackup();
                }

                progressPopupTitle = tr("Global", "Download"); // Reset title
            }, OnlineImageDialog::handleCancel);
        }
    }

    private static void renderEditPage() {
        windowWidth = 1300;
        windowHeight = 600;

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Image Size Options"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Image Size Options")); // Display title
        ImGui.popFont(); // Pop Font

        ImGui.separator();
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Current Image Size") + ": " + (imageData.remaining() >> 10) + " KiB");
        ImGui.separator();

        if (currentTexture != null) {
            ImGui.setCursorPosX((ImGui.getWindowSizeX() - (float) (currentTexture.getWidth() * 400) / currentTexture.getHeight()) / 2); // Center image
            ImGui.image(currentTexture.getTextureId(), (float) (currentTexture.getWidth() * 400) / currentTexture.getHeight(), 400);
        } else {
            MyWorldTrafficAddition.LOGGER.error("Failed to load image! Texture is null! Aborting...");
            resetValues();
            ImGui.closeCurrentPopup();
        }

        float[] scale = new float[]{imageScale[0]*100.0f};
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Center slider
        if (ImGui.sliderFloat(tr("ImGui.Child.PopUps.OnlineImageDialog", "Resolution"), scale, 1.0f, 100.0f, "%.0f%% " + tr("Global", "of") + " " + tr("Global", "Current"))) {
            imageScale[0] = scale[0] / 100.0f;
        }

        ImGui.setCursorPosY(ImGui.getWindowSizeY() - ImGui.getFontSize()*2); // Make things appear all the way at the bottom
        ImGui.beginChild("##imageSizerActionButtonContainer");
        if (ImGui.button(tr("Global", "Apply"))) applySettings();
        ImGui.sameLine();
        if (ImGui.button(tr("Global", "Next"))) currentPage = OnlineImageDialogPage.CONFIRM;
        ImGui.sameLine();
        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageDialog", "Restore Original"))) restoreOriginal();

        ImGui.endChild();
    }

    private static void renderConfirmPage() {
        windowWidth = 800;
        windowHeight = 400;

        renderProgressPopup(false, () -> {
            ImGui.closeCurrentPopup();
            shouldRender = false;
            resetValues();
        }, () -> {});

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm Image Upload"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm Image Upload")); // Display title
        ImGui.popFont();

        ImGui.inputText(tr("Global", "Name"), imageName); // Input field for image name
        ImGui.textWrapped(tr("ImGui.Child.PopUps.OnlineImageDialog", "Give your image a unique name"));
        ImGui.spacing();
        ImGui.checkbox(tr("ImGui.Child.PopUps.OnlineImageDialog", "Hide for others"), hideForOthers); // Checkbox to hide image for others
        ImGui.textWrapped(tr("ImGui.Child.PopUps.OnlineImageDialog", "This will hide your image for others. Admins can still see it, so don't be naughty"));

        ImGui.setCursorPosY(ImGui.getWindowSizeY() - ImGui.getFontSize()*2); // Make things appear all the way at the bottom
        ImGui.beginChild("##confirmPageActionButtonContainer");

        if (ImGui.button(tr("Global", "Cancel"))) {
            resetValues();
            shouldRender = false;
            ImGui.closeCurrentPopup();
        }

        ImGui.sameLine();
        if (imageName.isEmpty()) ImGui.beginDisabled();
        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm and Upload"))) uploadToServer();
        if (imageName.isEmpty()) ImGui.endDisabled();
        ImGui.endChild();
    }

    private static void applySettings() {
        if (imageScale[0] != 1.0f) {
            Triplet<Integer, Integer, ByteBuffer> result = scaleImage(imageData, imageScale[0], imgW.get(0), imgH.get(0), imgC.get(0));

            if (result.getC() == null) {
                MyWorldTrafficAddition.LOGGER.error("Failed to scale image! Aborting...");
                ConfirmationPopup.show(
                        tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to scale image"),
                        tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "MyWorld Traffic Addition has failed at scaling the image. Please check the logs for more details. Continue anyway (all actions are permanent)"),
                        (callback) -> {
                            if (callback) {
                                // Proceed with editing
                                MyWorldTrafficAddition.LOGGER.info("User decided to ditch scaling and proceed with editing. All actions were permanent.");
                            } else abort();
                        });

                return;
            }

            imgW.put(0, result.getA());
            imgH.put(0, result.getB());
            imageData = result.getC();
            imageScale[0] = 1.0f; // Reset scale to 1.0f
        }

        reuploadImage();
    }

    /**
     * Uploads downloaded image to GPU
     */
    private static void uploadImage() {
        // Test if imagePath is valid
        if (imageData == null || imageData.remaining() == 0) {
            MyWorldTrafficAddition.LOGGER.error("Image data is not present!");
            return;
        }

        // Upload image to GPU
        currentTexture = Textures.registerRawData(imageData, imgW.get(0), imgH.get(0), imgC.get(0));
    }

    /**
     * Reuploads image to GPU; replaces the old image with the new one while keeping the same texture ID
     */
    private static void reuploadImage() {
        // Test if imagePath is valid
        if (imageData == null) {
            MyWorldTrafficAddition.LOGGER.error("Failed reuploading image to GPU! Image data is null!");
            abort();
        }

        // Reupload image to GPU
        currentTexture.replaceRawPixelData(imageData, imgW.get(0), imgH.get(0), imgC.get(0));
    }

    private static void uploadToServer() {
        isOperating = true;
        progressPopupTitle = tr("Global", "Upload");
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Uploading image to server");

        Thread thread = new Thread(() -> {
            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Encoding image to PNG");
            byte[] imagePngData = encodePNG(imageData, imgW.get(0), imgH.get(0), imgC.get(0));
            operationProgress = 0.2f;

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Creating Thumbnail");
            Triplet<Integer, Integer, ByteBuffer> thumbnail = getThumbnail(imageData);
            operationProgress = 0.4f;

            if (thumbnail.getC() == null) {
                MyWorldTrafficAddition.LOGGER.error("Failed to upload image to server! Aborting...");
                operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to create thumbnail");
                isOperationComplete = true;
                isOperating = false;
                return;
            }

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Encoding thumbnail to PNG");
            byte[] thumbnailPngData = encodePNG(thumbnail.getC(), thumbnail.getA(), thumbnail.getB(), imgC.get(0));
            operationProgress = 0.6f;

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Creating Metadata");
            JsonObject metadata = createMetadata(
                    imageName.get(),
                    MinecraftClient.getInstance().getGameProfile().getId(),
                    UUID.randomUUID(),
                    hideForOthers.get(),
                    Instant.now()
            );
            operationProgress = 0.8f;

            // Also save metadata locally for faster loading and less server load
            saveLocally(metadata, imagePngData, thumbnailPngData);

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Packing data");

            // Combine data
            byte[] metadataBytes = metadata.toString().getBytes(StandardCharsets.UTF_8);
            int headerSize = Integer.BYTES * 3 + 1; // 3 Ints + 1 byte for hidden
            int totalSize = headerSize + imagePngData.length + thumbnailPngData.length + metadataBytes.length;

            ByteBuffer buffer = ByteBuffer.allocate(totalSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN); // Consistent byte order

            buffer.putInt(imagePngData.length);
            buffer.putInt(thumbnailPngData.length);
            buffer.putInt(metadataBytes.length);
            buffer.put((byte)(hideForOthers.get() ? 0 : 1)); // Hidden? Used to save in a different folder on server

            buffer.put(imagePngData);
            buffer.put(thumbnailPngData);
            buffer.put(metadataBytes);

            CustomClientNetworking.getInstance().sendBytesToServer(
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "send_custom_image_to_server"),
                    buffer.array(), 20, 16000
            );

            operationProgress = 1f;
            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Upload complete! If everything went right, you are now able to see your image in the gallery");
            isOperationComplete = true;
            isOperating = false;
        });

        thread.setName("ImageUploadThread");
        thread.start();

        shouldOpenProgressPopup = true;
    }

    private static void saveLocally(JsonObject metadata, byte[] imagePngData, byte[] thumbnailPngData) {
        ClientCustomImageDirectory.createCustomImageDir(); // Create if dir doesn't exist

        try {
            File imageFile = new File(ClientCustomImageDirectory.getCacheImageDir().resolve(metadata.get("ImageUUID").getAsString() + ".png").toAbsolutePath().toString());
            File thumbnailFile = new File(ClientCustomImageDirectory.getCacheImageDir().resolve(metadata.get("ImageUUID").getAsString() + "_thumbnail.png").toAbsolutePath().toString());
            java.nio.file.Files.write(imageFile.toPath(), imagePngData);
            java.nio.file.Files.write(thumbnailFile.toPath(), thumbnailPngData);
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to write image to file! Aborting...\nDetails: {}", e.getMessage());
            abort();
        }
    }

    /**
     * Metadata contains User ID, Image Link encrypted in Base64, Image UUID and Creation Date
     * @param imageName Link to the image
     * @param playerUuid UUID of the player
     * @param imageUuid UUID of the image
     * @param hide True if the image should be hidden for others, false otherwise
     * @param uploadDateTime Upload date & time
     * @return JsonObject containing metadata
     */
    private static JsonObject createMetadata(String imageName, UUID playerUuid, UUID imageUuid, boolean hide, Instant uploadDateTime) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("UploaderUUID", playerUuid.toString());
        metadata.addProperty("ImageName", Crypto.encodeBase64(imageName));
        metadata.addProperty("ImageUUID", imageUuid.toString());
        metadata.addProperty("CreationDate", uploadDateTime.toString());
        metadata.addProperty("Hidden", hide);

        return metadata;
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
     * <li>
     *     A = Width of the thumbnail (Integer)
     * </li>
     * <li>
     *     B = Height of the thumbnail (Integer)
     * </li>
     * <li>
     *     C = ByteBuffer containing the raw pixel data
     * </li>
     */
    private static Triplet<Integer, Integer, ByteBuffer> scaleImage(ByteBuffer imageData, float scale, int width, int height, int channels) {
        int newWidth = (int) Math.ceil(width * scale);
        int newHeight = (int) Math.ceil(height * scale);

        if (imageData != null && imageData != originalImageData) {
            MemoryUtil.memFree(imageData);
        }

        ByteBuffer scaledImage = MemoryUtil.memAlloc(newWidth * newHeight * channels);
        boolean state = ImageOperations.bilinearResize(imageData, width, height, scaledImage, newWidth, newHeight, channels);

        if (!state) { // Abort if unsuccessful (empty)
            MyWorldTrafficAddition.LOGGER.error("Failed to resize image! Aborting...");
            abort();
            return new Triplet<>(0, 0, null);
        }

        return new Triplet<>(newWidth, newHeight, scaledImage);
    }

    /**
     * Returns a thumbnail of the provided imageData that is 128 pixels on the longest side
     * @param imageData The raw pixel data of the image
     * @return Triplet(A, B, C)
     * <li>
     *     A = Width of the thumbnail (Integer)
     * </li>
     * <li>
     *     B = Height of the thumbnail (Integer)
     * </li>
     * <li>
     *     C = ByteBuffer containing the raw pixel data
     * </li>
     */
    private static Triplet<Integer, Integer, ByteBuffer> getThumbnail(ByteBuffer imageData) {
        float scale = 128f / (Math.max(imgH.get(0), imgW.get(0)));
        return scaleImage(imageData, scale, imgW.get(0), imgH.get(0), imgC.get(0));
    }

    private static byte[] encodePNG(ByteBuffer imageData, int width, int height, int channels) {
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

    private static void restoreOriginal() {
        if (originalImageData == null) {
            MyWorldTrafficAddition.LOGGER.error("Backup image data does not exist!");
            ConfirmationPopup.show(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to load backup"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "MyWorld Traffic Addition has failed at loading the image backup. Continue anyways"),
                    (callback) -> {
                        if (callback) {
                            // Proceed with editing
                            MyWorldTrafficAddition.LOGGER.info("User decided to ditch backup loading and proceed with editing. All actions were permanent.");
                        } else abort();
                    });
        } else {
            imageData = originalImageData;
            imgW.put(0, orgImgW.get(0));
            imgH.put(0, orgImgH.get(0));
        }

        reuploadImage();
    }

    /**
     * Deletes the image files of the downloaded image
     * @param deleteBackup true if the backup should be deleted too, false otherwise
     */
    private static void deleteImageData(boolean deleteBackup) {
        imageData = null;
        if (deleteBackup) originalImageData = null;
    }

    private static void createImageBackup() {
        if (imageData == null) {
            ConfirmationPopup.show(
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to create backup"),
                    tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to create a backup of the image file. Please check the logs for more details. Continue anyway (all actions are permanent)"),
                    (callback)-> {
                        if (callback) {
                            // Proceed with editing
                            MyWorldTrafficAddition.LOGGER.info("User decided to ditch backup creation and proceed with editing. All actions were permanent.");
                        } else abort();
                    });
        } else {
            originalImageData = imageData;
        }
    }

    private static void abort() {
        resetValues();
        shouldRender = false;
        ImGui.closeCurrentPopup();
        deleteImageData(true); // Delete everything
        MyWorldTrafficAddition.LOGGER.info("Aborting operations and deleting temp files...");
    }
}