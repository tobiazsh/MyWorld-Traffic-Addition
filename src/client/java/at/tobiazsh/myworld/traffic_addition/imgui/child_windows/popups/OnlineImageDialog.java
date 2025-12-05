package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.error.Error;
import at.tobiazsh.myworld.traffic_addition.image.ImageUtils;
import at.tobiazsh.myworld.traffic_addition.image.ByteImage;
import at.tobiazsh.myworld.traffic_addition.filesystem.ClientCustomImageDirectory;
import at.tobiazsh.myworld.traffic_addition.utils.crypto.Crypto;
import at.tobiazsh.myworld.traffic_addition.image.ImageLoader;
import at.tobiazsh.myworld.traffic_addition.network.ImageDownloader;
import at.tobiazsh.myworld.traffic_addition.gui.NativeFileDialogs;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import oshi.util.tuples.Triplet;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class OnlineImageDialog {

    public static final OnlineImageDialog INSTANCE = new OnlineImageDialog("@GlobalOnlineImageDialog");

    private final String key; // For distinguishing multiple instances

    private boolean shouldOpen = false;
    private boolean shouldRender = false;

    // Server things
    public static long maximumUploadSize = 1024 * 1024 * 5; // 5 MiB
    public static float[] imageScale = { 1.0f };

    volatile private Thread uploadThread = null;

    // Regarding Download
    volatile private boolean isOperating = false;
    volatile private boolean shouldOpenProgressPopup = false;
    volatile private boolean isOperationComplete = false;
    volatile private float operationProgress = 0.0f;
    volatile private String operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Operation Message Default");

    private final ImageDownloader downloader = new ImageDownloader(
            message -> operationMessage = message,
            progress -> operationProgress = progress
    );

    private ImString imageUrl = new ImString(512);

    private ByteBuffer originalImageData;
    private ByteBuffer imageData;

    // Image things
    private final Texture currentTexture = new Texture();
    private IntBuffer imgW = MemoryUtil.memAllocInt(1);
    private IntBuffer imgH = MemoryUtil.memAllocInt(1);
    private IntBuffer imgC = MemoryUtil.memAllocInt(1);
    private IntBuffer orgImgW = MemoryUtil.memAllocInt(1);
    private IntBuffer orgImgH = MemoryUtil.memAllocInt(1);
    private final ImString imageName = new ImString(128);
    private ImBoolean hideForOthers = new ImBoolean(false);

    // Window
    private static float windowWidth = 500;
    private static float windowHeight = 160;

    // Progress Popup
    private static String progressPopupTitle = tr("Global", "Download");
    private static float progressPopupWidth = 500;
    private static float progressPopupHeight = 160;

    public enum OnlineImageDialogPage {
        NEW,
        EDIT,
        CONFIRM
    }

    public OnlineImageDialogPage currentPage = null;


    public OnlineImageDialog(String key) {
        this.key = key;
    }


    public void render() {
        if (!shouldRender) return; // Prevent rendering if not necessary

        ImGui.setNextWindowSize(windowWidth, windowHeight);
        if (ImGui.beginPopupModal(tr("ImGui.Child.PopUps.OnlineImageDialog", "Online Image Uploader") + "###" + key)) {

            switch (currentPage) {
                case NEW -> renderNewPage();
                case EDIT -> renderEditPage();
                case CONFIRM -> renderConfirmPage();
            }

            ConfirmationPopup.render(); // Render confirmation popup

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(tr("ImGui.Child.PopUps.OnlineImageDialog", "Online Image Uploader")  + "###" + key);
            shouldOpen = false;
        }
    }

    public void startDialog() {
        shouldRender = true;
        shouldOpen = true;
        currentPage = OnlineImageDialogPage.NEW;

        CustomClientNetworking.getInstance().sendStringToServer(Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_maximum_image_upload_size"), "dummy");

        imageUrl = new ImString(1024);
    }

    private void handleDownload() {
        isOperating = true;
        shouldOpenProgressPopup = true;

        // Execute download in separate thread to prevent blocking the UI and giving feedback to the user
        new Thread(() -> {
            ByteImage result = downloader.downloadImage(imageUrl.get());

            if (downloader.hasError()) { // Error occurred
                Error e = downloader.getError();
                MyWorldTrafficAddition.LOGGER.error(e.getMessage());
                ErrorPopup.open(e, null);
            } else {
                setImageData(result);
            }

            isOperationComplete = true;
        }).start();
    }

    private void handleLocalFile() {

        Thread fileLoadThread;

        fileLoadThread = new Thread(() -> {
            String path = NativeFileDialogs.open(
                    "Select an image file to upload",
                    new NativeFileDialogs.FilterItem("Image Files", new String[] { "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif" }),
                    System.getProperty("user.home"),
                    (msg) -> {
                        isOperationComplete = true;
                        isOperating = false;
                        MyWorldTrafficAddition.LOGGER.debug("File dialog aborted: {}", msg);
                    });

            if (path == null || path.isEmpty()) {
                MyWorldTrafficAddition.LOGGER.debug("No file selected or dialog was cancelled.");
                return;
            }

            isOperating = true;
            shouldOpenProgressPopup = true;
            operationProgress = 0.0f;
            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Loading image from file");

            ByteImage result;

            try {
                result = ImageLoader.loadFileToStbImage(Path.of(path));
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load image from file: {}", e.getMessage());
                ErrorPopup.open(
                        new Error(
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Error loading image from file"),
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Could not load image from the selected file. Please check the file and try again.")),
                        null
                );
                isOperationComplete = true;
                isOperating = false;
                return;
            }

            setImageData(result);

            isOperationComplete = true;
            operationProgress = 1.0f;
            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Loaded image from file successfully");
        });

        fileLoadThread.setName("File Load Thread");
        fileLoadThread.start();
    }

    private void handleCancel() {
        downloader.cancelDownload();
        abort();
    }

    private void renderProgressPopup(boolean showCancel, Runnable onClose, Runnable onCancel) {
        if (shouldOpenProgressPopup) {
            ImGui.openPopup(tr("ImGui.Child.PopUps.OnlineImageDialog", "Operation Progress"));
            shouldOpenProgressPopup = false;
        }

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
            }

            ImGui.endPopup();
        }
    }

    private void renderNewPage() {
        windowWidth = 800;
        windowHeight = 300;

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Enter Image URL"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Enter Image URL")); // Display title
        ImGui.popFont(); // Pop Font

        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Center input field
        ImGui.inputText("##imageUrlDummylabel", imageUrl); // Input field for URL
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Position button

        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageDialog", "Load from URL"))) handleDownload();

        ImGui.sameLine();

        if (ImGui.button(tr("Global", "Cancel"))) {
            shouldRender = false;
            ImGui.closeCurrentPopup();
            abort();
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        final float buttonWidth = 500;
        final float buttonHeight = 40;
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - buttonWidth) / 2); // Center button
        ImGui.setCursorPosY((float) (ImGui.getWindowSizeY() - 0.5 * ImGui.getContentRegionAvailY() - buttonHeight)); // Add some vertical spacing

        if (ImGui.button(tr("Global", "Open File"), new ImVec2(buttonWidth, buttonHeight))) handleLocalFile();

        if (isOperating) {
            // Show progress in separate window
            renderProgressPopup(true, () -> {
                ImGui.closeCurrentPopup();

                if (!downloader.hasError()) {
                    currentPage = OnlineImageDialogPage.EDIT; // Go to next page
                    MinecraftClient.getInstance().execute(this::uploadImageToGPU); // Upload image to GPU
                    createImageBackup();
                }

                progressPopupTitle = tr("Global", "Download"); // Reset title
            }, this::handleCancel);
        }
    }

    private void renderEditPage() {
        windowWidth = 1300;
        windowHeight = 600;

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Image Size Options"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Image Size Options")); // Display title
        ImGui.popFont(); // Pop Font

        ImGui.separator();

        int sizeKiB = (imageData != null) ? (imageData.remaining() >> 10) : 0;
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Current Image Size") + ": " + sizeKiB + " KiB");
        ImGui.separator();

        if (imageData == null) {
            MyWorldTrafficAddition.LOGGER.error("Image data is null! Aborting...");
            abort();
            ImGui.closeCurrentPopup();

            ErrorPopup.open(
                    new Error(
                            "An error occured!",
                            "MyWorld Traffic Addition has failed at loading the image data. Please check the logs for more details."
                    ), null);
            return;
        }

        if (currentTexture != null) {
            ImGui.setCursorPosX((ImGui.getWindowSizeX() - (float) (currentTexture.getWidth() * 400) / currentTexture.getHeight()) / 2); // Center image
            ImGui.image(currentTexture.getTextureId(), (float) (currentTexture.getWidth() * 400) / currentTexture.getHeight(), 400);
        } else {
            MyWorldTrafficAddition.LOGGER.error("Failed to load image! Texture is null! Aborting...");
            ImGui.closeCurrentPopup();
            abort();
        }

        float[] scale = new float[]{imageScale[0]*100.0f};
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - ImGui.calcItemWidth()) / 2); // Center slider
        if (ImGui.sliderFloat(tr("ImGui.Child.PopUps.OnlineImageDialog", "Resolution"), scale, 1.0f, 100.0f, "%.0f%% " + tr("Global", "Of") + " " + tr("Global", "Current"))) {
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

    private void renderConfirmPage() {
        windowWidth = 800;
        windowHeight = 400;

        renderProgressPopup(false, () -> {
            ImGui.closeCurrentPopup();

            Thread t = uploadThread;
            if (t != null && t.isAlive()) {
                try {
                    t.join(2000); // Wait for upload thread to finish
                } catch (InterruptedException e) {
                    MyWorldTrafficAddition.LOGGER.error("Interrupted while waiting for upload thread to finish: {}", e.getMessage());
                    abort();
                }
            }
            shouldRender = false;
            close(true);
        }, this::abort);

        ImGui.pushFont(ImGuiImpl.RobotoBoldMedium);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm Image Upload"))) / 2); // Center title
        ImGui.text(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm Image Upload")); // Display title
        ImGui.popFont();

        ImGui.inputText(tr("Global", "Name"), imageName); // Input field for image name
        ImGui.textWrapped(tr("ImGui.Child.PopUps.OnlineImageDialog", "Give your image a unique name"));
        ImGui.spacing();
        ImGui.checkbox(tr("ImGui.Child.PopUps.OnlineImageDialog", "Hide for others"), hideForOthers); // Checkbox to hide image for others
        ImGui.textWrapped(tr("ImGui.Child.PopUps.OnlineImageDialog", "This will hide your image for others. Admins can still see it, so don\u0027t be naughty") + "! ;)");

        ImGui.setCursorPosY(ImGui.getWindowSizeY() - ImGui.getFontSize()*2); // Make things appear all the way at the bottom
        ImGui.beginChild("##confirmPageActionButtonContainer");

        if (ImGui.button(tr("Global", "Cancel"))) {
            shouldRender = false;
            ImGui.closeCurrentPopup();
            abort();
        }

        ImGui.sameLine();
        if (imageName.isEmpty()) ImGui.beginDisabled();
        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageDialog", "Confirm and Upload"))) uploadToServer();
        if (imageName.isEmpty()) ImGui.endDisabled();
        ImGui.endChild();
    }

    private void applySettings() {
        if (imageScale[0] != 1.0f) {
            Triplet<Integer, Integer, ByteBuffer> result = ImageUtils.scaleImage(imageData, imageScale[0], imgW.get(0), imgH.get(0), imgC.get(0), this::abort);

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

            if (imageData != null)
                freeImageData();

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
    private void uploadImageToGPU() {
        // Test if image data is present
        if (imageData == null) {
            MyWorldTrafficAddition.LOGGER.error("Image data is not present!");
            return;
        }

        // Ensure buffer is readable: use a read-only / duplicated view and rewind it
        ByteBuffer readView = imageData.asReadOnlyBuffer();
        readView.rewind();

        if (readView.remaining() == 0) {
            MyWorldTrafficAddition.LOGGER.error("Image data has no remaining bytes!");
            return;
        }

        // Upload image to GPU
        if (!currentTexture.isEmpty()) {
            currentTexture.delete();
        }

        currentTexture.loadRawPixelData(readView, imgW.get(0), imgH.get(0), imgC.get(0));
    }

    /**
     * Reuploads image to GPU; replaces the old image with the new one while keeping the same texture ID
     */
    private void reuploadImage() {
        // Test if imagePath is valid
        if (imageData == null) {
            MyWorldTrafficAddition.LOGGER.error("Failed reuploading image to GPU! Image data is null!");
            abort();
        }

        // Reupload image to GPU
        currentTexture.replaceRawPixelData(imageData, imgW.get(0), imgH.get(0), imgC.get(0));
    }

    private void uploadToServer() {
        isOperating = true;
        progressPopupTitle = tr("Global", "Upload");
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Uploading image to server");

        uploadThread = new Thread(() -> { // Upload in a new thread
            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Encoding image to PNG");
            byte[] imagePngData = ImageUtils.encodePNG(imageData, imgW.get(0), imgH.get(0), imgC.get(0), () -> {
                ErrorPopup.open(
                        new Error(
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Error encoding image to PNG!"),
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Could not encode image to PNG! Check logs!")),
                        this::abort
                );
            });
            operationProgress = 0.2f;

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Creating Thumbnail");
            Triplet<Integer, Integer, ByteBuffer> thumbnail = getThumbnail(originalImageData, imgW, imgH, imgC, this::abort);
            operationProgress = 0.4f;

            if (thumbnail.getC() == null) {
                MyWorldTrafficAddition.LOGGER.error("Failed to upload image to server! Aborting...");
                operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog.Error", "Failed to create thumbnail");
                isOperationComplete = true;
                isOperating = false;
                return;
            }

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Encoding thumbnail to PNG");
            byte[] thumbnailPngData = ImageUtils.encodePNG(thumbnail.getC(), thumbnail.getA(), thumbnail.getB(), imgC.get(0), () -> {
                ErrorPopup.open(
                        new Error(
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Error encoding thumbnail to PNG!"),
                                tr("ImGui.Child.PopUps.OnlineImageDialog", "Could not encode thumbnail to PNG! Check logs!")),
                        this::abort
                );
            });
            operationProgress = 0.6f;

            operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Creating Metadata");
            JsonObject metadata = createMetadata(
                    imageName.get(),
                    MinecraftClient.getInstance().getGameProfile().id(),
                    UUID.randomUUID(),
                    hideForOthers.get(),
                    Instant.now()
            );
            operationProgress = 0.8f;

            // Also save metadata locally for faster loading and less server load
            saveLocal(metadata, imagePngData, thumbnailPngData);

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

            // Free thumbnail memory
            MemoryUtil.memFree(thumbnail.getC());
        });

        uploadThread.setName("ImageUploadThread");
        uploadThread.start();

        shouldOpenProgressPopup = true;
    }

    private void saveLocal(JsonObject metadata, byte[] imagePngData, byte[] thumbnailPngData) {
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
    private static Triplet<Integer, Integer, ByteBuffer> getThumbnail(ByteBuffer imageData, IntBuffer imgW, IntBuffer imgH, IntBuffer imgC, Runnable onAbort) {
        float scale = 128f / (Math.max(imgH.get(0), imgW.get(0)));
        return ImageUtils.scaleImage(imageData, scale, imgW.get(0), imgH.get(0), imgC.get(0), onAbort);
    }

    private void restoreOriginal() {
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
        } else {    // Free current STB image
            if (imageData != null)
                freeImageData();

            // Allocate new STB buffer and copy backup into it
            ByteBuffer backup = originalImageData.asReadOnlyBuffer();
            backup.rewind();

            imageData = MemoryUtil.memAlloc(backup.remaining());
            imageData.put(backup);
            imageData.flip();

            // Restore dimensions
            imgW.put(0, orgImgW.get(0));
            imgH.put(0, orgImgH.get(0));

            reuploadImage();
        }

        reuploadImage();
    }

    private void createImageBackup() {
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
            if (originalImageData != null) {
                MemoryUtil.memFree(originalImageData);
                originalImageData = null;
            }

            ByteBuffer src = imageData.asReadOnlyBuffer();
            src.rewind();
            originalImageData = MemoryUtil.memAlloc(src.remaining());
            originalImageData.put(src);
            originalImageData.flip();
        }
    }

    private void abort() {
        resetValuesImGui();
        shouldRender = false;
        ImGui.closeCurrentPopup();
        freeMemory();
        MyWorldTrafficAddition.LOGGER.info("Aborting operations and deleting temp files...");
    }

    /**
     * Resets all values handled by ImGui in any way
     */
    private void resetValuesImGui() {
        resetProgressPopup();
        windowWidth = 500;
        windowHeight = 160;
        imageScale[0] = 1.0f;
        imageName.set("");
        hideForOthers = new ImBoolean(false);
    }

    private void freeMemory() {
        freeImageData();

        if (imgW != null) {
            MemoryUtil.memFree(imgW);
            imgW = null;
        }

        if (imgH != null) {
            MemoryUtil.memFree(imgH);
            imgH = null;
        }

        if (imgC != null) {
            MemoryUtil.memFree(imgC);
            imgC = null;
        }

        if (orgImgW != null) {
            MemoryUtil.memFree(orgImgW);
            orgImgW = null;
        }

        if (orgImgH != null) {
            MemoryUtil.memFree(orgImgH);
            orgImgH = null;
        }

        if (originalImageData != null) {
            MemoryUtil.memFree(originalImageData);
            originalImageData = null;
        }
    }

    /**
     * Sets the image data from a ByteImage and fees the ByteImage afterward
     */
    private void setImageData(ByteImage image) {
        ByteBuffer src = image.stbImage().asReadOnlyBuffer();
        src.rewind();

        if (imageData != null) {
            MemoryUtil.memFree(imageData);
            imageData = null;
        }

        if (originalImageData != null) {
            MemoryUtil.memFree(originalImageData);
            originalImageData = null;
        }

        imageData = MemoryUtil.memAlloc(src.remaining());
        imageData.put(src);
        imageData.flip();

        originalImageData = MemoryUtil.memAlloc(src.remaining());
        originalImageData.put(src);
        originalImageData.flip();

        imageData.rewind();
        originalImageData.rewind();

        imgW = MemoryUtil.memAllocInt(1);
        imgH = MemoryUtil.memAllocInt(1);
        imgC = MemoryUtil.memAllocInt(1);

        imgW.put(0, image.width());
        imgH.put(0, image.height());
        imgC.put(0, image.channels());

        orgImgW = MemoryUtil.memAllocInt(1);
        orgImgH = MemoryUtil.memAllocInt(1);

        orgImgW.put(0, image.width());
        orgImgH.put(0, image.height());

        image.free();
    }

    private void freeImageData() {
        if (imageData != null) {
            MemoryUtil.memFree(imageData);
            imageData = null;
        }
    }

    private void resetProgressPopup() {
        operationProgress = 0.0f;
        operationMessage = tr("ImGui.Child.PopUps.OnlineImageDialog", "Waiting");
        isOperating = false;
        shouldOpenProgressPopup = false;
        isOperationComplete = false;
        progressPopupWidth = 500;
        progressPopupHeight = 160;
    }

    /**
     * Closes the dialog
     * @param free true if memory should be freed, false otherwise
     */
    private void close(boolean free) {
        if (free) freeMemory();
        resetValuesImGui();
        shouldRender = false;
        ImGui.closeCurrentPopup();
    }

    public static void setMaximumUploadSize(long maximumUploadSize) {
        // Only set if the value is greater than 0 because you cannot obviously upload negative or zero byte files
        if (maximumUploadSize > 0) {
            OnlineImageDialog.maximumUploadSize = maximumUploadSize;
        }
    }

}