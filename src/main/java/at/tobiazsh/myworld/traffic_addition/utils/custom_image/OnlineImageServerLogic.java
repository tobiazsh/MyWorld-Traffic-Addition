package at.tobiazsh.myworld.traffic_addition.utils.custom_image;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.networking.CustomServerNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.BooleanUtils;
import at.tobiazsh.myworld.traffic_addition.utils.Error;
import at.tobiazsh.myworld.traffic_addition.utils.ImageUtils;
import at.tobiazsh.myworld.traffic_addition.utils.preferences.ServerBlacklist;
import at.tobiazsh.myworld.traffic_addition.utils.preferences.ServerPreferences;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OnlineImageServerLogic {

    private static final ConcurrentHashMap<UUID, AtomicInteger> perPlayerCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Pair<CustomImageMetadata, Boolean>> metadataMap = new ConcurrentHashMap<>(); // List of metadata so it is being saved in RAM and avoids unnecessary file I/O
    public static AtomicInteger totalEntries = new AtomicInteger(0);
    public static AtomicInteger publicEntries = new AtomicInteger(0);
    public static AtomicInteger hiddenEntries = new AtomicInteger(0);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(32);

    /**
     * Processes an uploaded image.
     * @param image The byte array containing the image data, thumbnail, metadata, and hidden status.
     */
    public static void processUploadedImage(ServerPlayerEntity source, byte[] image) {
        // Extract image
        executorService.submit(() -> {

            // Check if player is blacklisted
            if (ServerBlacklist.bannedImageUploadPlayers.contains(source.getUuid())) {
                errorToClient(
                        source,
                        new Error("Image Upload Error", "You are banned from uploading images to this server!")
                );

                MyWorldTrafficAddition.LOGGER.info("Blocked image upload attempt from blacklisted player with UUID {}!", source.getUuid());

                return;
            }

            // Check if uploads are enabled
            if (!ServerPreferences.isPlayerUploadEnabled) {
                errorToClient(
                        source,
                        new Error("Image Upload Error", "Image uploads are disabled on this server!")
                );
                MyWorldTrafficAddition.LOGGER.info("Blocked image upload attempt from player with UUID {} because uploads are disabled!", source.getUuid());
                return;
            }

            // Check if upload limit is set
            if (ServerPreferences.isUploadLimitSet) {
                // Check if player maxed out their upload limit
                AtomicInteger userUploads = perPlayerCounts.getOrDefault(source.getUuid(), new AtomicInteger(0));
                if (userUploads.get() == ServerPreferences.maximumUploadsPerPlayer) {
                    errorToClient(
                            source,
                            new Error("Image Upload Error", "You have maxed out your upload limit! Delete some of your uploaded images to upload new ones.\nUpload limit per player on server: " + ServerPreferences.maximumUploadsPerPlayer)
                    );
                    MyWorldTrafficAddition.LOGGER.info("Blocked image upload attempt from player with UUID {} because they maxed out their upload limit!", source.getUuid());
                    return;
                }
            }

            ByteBuffer buffer = ByteBuffer.wrap(image);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.rewind();

            if (image.length < 16) { // 3 ints (12) + hidden byte (1) + padding
                errorToClient(
                        source,
                        new Error("Image Upload Error", "Malformed package!")
                );

                return;
            }

            int imageSize = buffer.getInt();
            int thumbnailSize = buffer.getInt();
            int metadataSize = buffer.getInt();

            // Hard rejection if any of the sizes exceed maximum allowed size
            if (
                    imageSize < 0 || thumbnailSize < 0 || metadataSize < 0 ||
                    imageSize > ServerPreferences.maximumImageUploadSize ||
                    thumbnailSize > ServerPreferences.maximumThumbnailUploadSize ||
                    metadataSize > ServerPreferences.maximumMetadataUploadSize) {
                errorToClient(
                        source,
                        new Error("Image Upload Error", "Uploaded image, thumbnail or metadata exceeds maximum allowed size of " + ServerPreferences.maximumImageUploadSize + " bytes.")
                );

                return;
            }

            // Validate total size
            long expectedTotalSize = 12L + 1L + imageSize + thumbnailSize + metadataSize; // 3 ints (12 bytes) + 1 byte for hidden + sizes
            if (image.length != expectedTotalSize) {
                errorToClient(
                        source,
                        new Error("Image Upload Error", "Malformed package! Expected size: " + expectedTotalSize + ", actual size: " + image.length)
                );

                return;
            }

            byte hiddenByte = buffer.get();
            boolean hidden = hiddenByte == 0;

            byte[] imageData = new byte[imageSize];
            buffer.get(imageData);

            byte[] thumbnailData = new byte[thumbnailSize];
            buffer.get(thumbnailData);

            // Validate image formats
            String imageFormat = ImageUtils.getImageFormat(imageData);
            String thumbnailFormat = ImageUtils.getImageFormat(thumbnailData);
            if (thumbnailFormat == null || imageFormat == null ||
                !isOfValidFormat(thumbnailFormat) || !isOfValidFormat(imageFormat)) {

                errorToClient(
                        source,
                        new Error("Image Upload Error", "Uploaded image or thumbnail is not of a valid format! Supported formats are PNG, JPEG, JPG and BMP.")
                );
            }

            byte[] metadataData = new byte[metadataSize];
            buffer.get(metadataData);

            String metadata = new String(metadataData, StandardCharsets.UTF_8);
            JsonObject metadataJson = JsonParser.parseString(metadata).getAsJsonObject();
            CustomImageMetadata metadataObj = new CustomImageMetadata(metadataJson);

            String imageUUIDStr = metadataJson.get("ImageUUID").getAsString();
            String uploaderUUIDStr = metadataJson.get("UploaderUUID").getAsString();

            UUID imageUUID = UUID.fromString(imageUUIDStr);
            UUID uploaderUUID = UUID.fromString(uploaderUUIDStr);

            CustomImageDirectory.createCustomImageDir(); // Create custom image directory if it doesn't exist

            Path destinationPath = hidden ?
                    CustomImageDirectory.getHiddenCustomImageDir() :
                    CustomImageDirectory.getCustomImageDir();

            File imageFile = new File(destinationPath.resolve(imageUUIDStr + ".png").toAbsolutePath().toString());
            File thumbnailFile = new File(destinationPath.resolve(imageUUIDStr + "_thumbnail.png").toAbsolutePath().toString());
            File metadataFile = new File(destinationPath.resolve(imageUUIDStr + "_metadata.json").toAbsolutePath().toString());

            // Write files
            try (FileOutputStream imageOutputStream = new FileOutputStream(imageFile);
                 FileOutputStream thumbnailOutputStream = new FileOutputStream(thumbnailFile)) {

                imageOutputStream.write(imageData);
                thumbnailOutputStream.write(thumbnailData);

            } catch (IOException e) {
                throw new RuntimeException("Failed to write image or thumbnail image", e);
            }

            try {
                java.nio.file.Files.writeString(metadataFile.toPath(), metadataJson.toString());
            } catch (IOException e) {
                throw new RuntimeException("Failed to write metadata", e);
            }


            metadataMap.put(imageUUID, new Pair<>(metadataObj, metadataJson.get("Hidden").getAsBoolean())); // Add to list for later use
            MyWorldTrafficAddition.LOGGER.info("User with UUID {} uploaded custom image with UUID {}!", uploaderUUIDStr, imageUUIDStr);

            perPlayerCounts.computeIfAbsent(uploaderUUID, k -> new AtomicInteger(0)).incrementAndGet();
            totalEntries.incrementAndGet(); // Update total entries
            if (hidden) hiddenEntries.incrementAndGet(); // Update hidden entries
            else publicEntries.incrementAndGet(); // Update public entries
        });
    }



    /**
     * Counts the number of uploaded images by a specific player based on their UUID and sends it to client.
     * @param player The player to count the entries for and send the count to.
     */
    public static void getEntryNumberByPlayer(ServerPlayerEntity player) {
        executorService.submit(() -> {
            UUID playerUUID = player.getUuid();
            int count = perPlayerCounts.getOrDefault(playerUUID, new AtomicInteger(0)).get();

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(count);
            buffer.flip(); // Prepare the buffer for reading

            CustomServerNetworking.getInstance().sendBytesToClient(
                    player,
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_private_uploaded_images"),
                    buffer.array(),
                    -1,
                    -1
            ); // Dummy byte & -1 for no limits in transmission because it's just a single integer and won't starve the network
        });
    }



    /**
     * Counts the number of entries and reads all metadata into memory.
     */
    public static void countEntriesAndReadIntoMemory() {
        Path hiddenImageDir = CustomImageDirectory.getHiddenCustomImageDir();
        Path customImageDir = CustomImageDirectory.getCustomImageDir();

        if (!hiddenImageDir.toFile().exists()) // If dir doesn't exist, no uploads have been made; return
            return;

        // Count JSON Files in the directory as they represent image entries. For each uploaded image, there's exactly one JSON file.
        hiddenEntries = new AtomicInteger(processImageDirectory(hiddenImageDir));
        publicEntries = new AtomicInteger(processImageDirectory(customImageDir));
        totalEntries = new AtomicInteger(hiddenEntries.get() + publicEntries.get());
    }



    /**
     * Processes the image directory to count entries and read metadata.
     * @param dir The directory containing the hidden images.
     * @return The number of entries processed in the directory.
     */
    private static int processImageDirectory(Path dir) {
        int count = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) continue;
                if (entry.getFileName().toString().endsWith(".json")) {
                    String content = new String(Files.readAllBytes(entry));
                    JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                    CustomImageMetadata metadata = new CustomImageMetadata(jsonObject);

                    metadataMap.put(metadata.getImageUUID(), new Pair<>(metadata, metadata.isHidden()));
                    perPlayerCounts.computeIfAbsent(metadata.getUploaderUUID(), k -> new AtomicInteger(0)).incrementAndGet();

                    count++;
                }
            }
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to process image directory", e);
            throw new RuntimeException(e);
        }

        return count;
    }



    /**
     * Sends the metadata of requested entry to the client.
     * @param player The client to send the metadata to.
     * @param bytes The metadata
     */
    public static void sendEntryMetadataToClient(ServerPlayerEntity player, byte[] bytes) {
        executorService.submit(() -> {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.rewind();

            int startIndex = buffer.getInt();
            int endIndex = buffer.getInt();
            boolean privateImagesOnly = BooleanUtils.fromByte(buffer.get());

            List<CustomImageMetadata> sendableData;

            if (privateImagesOnly) {
                UUID playerUUID = player.getUuid();
                sendableData = metadataMap.values().stream()
                        .filter(entry -> entry.getLeft().getUploaderUUID().equals(playerUUID))
                        .map(Pair::getLeft)
                        .toList(); // Get only the Json of the entries that are uploaded by the player
            } else {
                sendableData = metadataMap.values().stream()
                        .filter(entry -> !entry.getRight())
                        .map(Pair::getLeft).toList(); // Get only the Json of the entries that are not hidden
            }
            endIndex = Math.min(endIndex, sendableData.size()); // Ensure we don't go out of bounds

            // Calculate allocation size
            int allocatedSize = 0;
            int sentEntries = 0;
            for (int i = startIndex; i < endIndex; i++) {
                JsonElement jsonElement = sendableData.get(i).getRawData();
                allocatedSize += jsonElement.toString().getBytes(StandardCharsets.UTF_8).length + 4; // 4 bytes for the length of the string
                sentEntries++;
            }

            // Allocate the buffer and store the image
            ByteBuffer responseBuffer = ByteBuffer.allocate(allocatedSize + Integer.BYTES); // Extra Integer for specifying the number of entries
            responseBuffer.putInt(sentEntries);
            for (int i = startIndex; i < endIndex; i++) {
                if (i >= metadataMap.size()) break; // Safety check
                JsonElement jsonElement = sendableData.get(i).getRawData();
                byte[] jsonBytes = jsonElement.toString().getBytes(StandardCharsets.UTF_8);
                responseBuffer.putInt(jsonBytes.length);
                responseBuffer.put(jsonBytes);
            }

            // Send the response buffer to the client
            CustomServerNetworking.getInstance().sendBytesToClient(
                    player,
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_image_entries_metadata"),
                    responseBuffer.array(),
                    -1,
                    -1
            ); // Dummy byte & -1 for no limits in transmission
        });
    }



    /**
     * Takes a byte array containing the UUIDs of the images to send and sends them off to the client
     * @param player The client to send to
     * @param imageUuidBytes The byte array containing the requested thumbnails encoded in PNG
     */
    public static void sendThumbnailsOf(ServerPlayerEntity player, byte[] imageUuidBytes) {
        executorService.submit(() -> {
            ByteBuffer buffer = ByteBuffer.wrap(imageUuidBytes);
            buffer.rewind();

            // Unpacks requested thumbnails
            List<String> imageUUIDs = unpackImageUUIDs(buffer);

            // Gathers thumbnail image
            List<byte[]> thumbnails = new ArrayList<>();
            for (String imageUUID : imageUUIDs)
                thumbnails.add(readCustomImageData(imageUUID, "_thumbnail.png"));

            // Packs thumbnails into ByteBuffer
            byte[] thumbnailData = wrapByteListIntoByteBuffer(thumbnails);

            CustomServerNetworking.getInstance().sendBytesToClient(
                    player,
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_thumbnail_data"),
                    thumbnailData,
                    10,
                    16000
            ); // 2 packets per second, 16kB max size as file is larger than just text
        });
    }



    /**
     * Unpacks image UUIDS from a ByteBuffer
     * @param bytes The byte array
     * @return A list of the image UUID as Strings
     */
    private static List<String> unpackImageUUIDs(ByteBuffer bytes) {
        List<String> uuids = new ArrayList<>();
        while (bytes.hasRemaining()) {
            int len = bytes.getInt();
            byte[] uuid = new byte[len];
            bytes.get(uuid);
            uuids.add(new String(uuid, StandardCharsets.UTF_8));
        }

        return uuids;
    }



    /**
     * Reads the image from the custom image directories and returns it as a byte array (encoded in whatever format the image is in). Also supports hidden images
     * @param uuid The UUID of the image to read
     * @param suffix MUST match exactly type (like ".png" or ".jpeg"), otherwise the image will NOT be found. Can help get specific types of the image if the suffix is included (for example for thumbnails: "_thumbnail.png").
     * @return Image as byte array
     */
    private static byte[] readCustomImageData(String uuid, @NotNull @NotBlank String suffix) {
        String imageName = uuid + suffix;

        // Check if and where image exists
        try {
            if (Files.exists(CustomImageDirectory.getCustomImageDir().resolve(imageName)))
                return Files.readAllBytes(CustomImageDirectory.getCustomImageDir().resolve(imageName));

            else if (Files.exists(CustomImageDirectory.getHiddenCustomImageDir().resolve(imageName)))
                return Files.readAllBytes(CustomImageDirectory.getHiddenCustomImageDir().resolve(imageName));
        } catch (IOException exc) {
            MyWorldTrafficAddition.LOGGER.error("Unable to read image image for UUID {}: {}", uuid, exc.getMessage());
        }
        MyWorldTrafficAddition.LOGGER.error("Image with UUID {} not found!", uuid);
        return new byte[0]; // Return empty byte array if image not found
    }



    /**
     * Wraps a byte list into a byte buffer
     * @param byteList The list of the byte arrays to wrap
     * @return The byte buffer containing the wrapped byte arrays and their sizes
     */
    private static byte[] wrapByteListIntoByteBuffer(List<byte[]> byteList) {
        int totalSize = 0;
        for (byte[] by : byteList) {
            totalSize += Integer.BYTES; // 4 bytes for storing the length of the byte array
            totalSize += by.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        for (byte[] by : byteList) {
            buffer.putInt(by.length);
            buffer.put(by);
        }

        buffer.flip(); // Prepare the buffer for reading
        return buffer.array();
    }



    public static void sendImageDataOf(ServerPlayerEntity player, byte[] requestBytes) {
        executorService.submit(() -> {
            ByteBuffer buffer = ByteBuffer.wrap(requestBytes);
            buffer.rewind();

            int uuidLength = buffer.getInt();
            int requestIdLength = buffer.getInt();

            byte[] imageUuidBytes = new byte[uuidLength];
            byte[] requestIdBytes = new byte[requestIdLength];

            buffer.get(imageUuidBytes);
            buffer.get(requestIdBytes);

            UUID imageUUID = byteToUUID(imageUuidBytes);

            if (!Files.exists(CustomImageDirectory.getCustomImageDir().resolve(imageUUID + ".png"))) {
                sendFailedImageResponse(player, imageUUID, requestIdBytes);
                return; // Exit if image does not exist
            }

            Path imagePath = CustomImageDirectory.getCustomImageDir().resolve(imageUUID + ".png");

            byte[] imageData;

            try {
                imageData = Files.readAllBytes(imagePath);
            } catch (IOException e) {
                sendFailedImageResponse(player, imageUUID, requestIdBytes);
                return;
            }

            ByteBuffer successfulResponse = ByteBuffer.allocate(1 + requestIdBytes.length + imageData.length + 2 * Integer.BYTES); // 1 byte for success flag, requestId length, image data and the sizes for the id and image data

            successfulResponse.put(BooleanUtils.toByte(true)); // successful?

            successfulResponse.putInt(requestIdBytes.length);
            successfulResponse.putInt(imageData.length);

            successfulResponse.put(requestIdBytes);
            successfulResponse.put(imageData);

            CustomServerNetworking.getInstance().sendBytesToClient(
                    player,
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_image_data"),
                    successfulResponse.array(),
                    20,
                    16000
            ); // 1 packet per second, 16kB max size as file is larger than just text
        });
    }

    private static void sendFailedImageResponse(ServerPlayerEntity player, UUID imageUUID, byte[] requestIdBytes) {
        ByteBuffer response = ByteBuffer.allocate(1 + requestIdBytes.length + 1); // 1 byte for success flag, requestId length, and 1 byte for empty image data

        response.put(BooleanUtils.toByte(false)); // successful?
        response.put(requestIdBytes);
        response.put(imageUUID.toString().getBytes(StandardCharsets.UTF_8));

        response.flip(); // Prepare the buffer for reading

        CustomServerNetworking.getInstance().sendBytesToClient(
                player,
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_image_data"),
                response.array(), // Send empty byte array if image not found
                -1,
                -1
        );
    }



    /**
     * Deletes an image entry from the server.
     * @param player The player who requested the deletion.
     * @param imageUUIDBytes The UUID of the image to delete, as a byte array.
     */
    public static void deleteImage(ServerPlayerEntity player, byte[] imageUUIDBytes) {
        executorService.submit(() -> {
            UUID imageUUID = byteToUUID(imageUUIDBytes);

            Pair<CustomImageMetadata, Boolean> stored = metadataMap.get(imageUUID);
            if (stored == null) {
                // not found, error message
                errorToClient(
                        player,
                        new Error("Image Deletion Error", "Image with UUID " + imageUUID + " not found on server!")
                );
                return;
            }

            boolean hidden = stored.getRight();
            CustomImageMetadata metadata = stored.getLeft();

            // Compare uploader's UUID with the player's UUID to verify if the player is allowed to delete the image
            UUID playerUUID = player.getUuid();

            if (!metadata.getUploaderUUID().equals(playerUUID)) {
                MyWorldTrafficAddition.LOGGER.warn("Player with UUID {} and NAME {} tried to delete image with UUID {} but is not the original uploader!", player.getUuid(), player.getName(), imageUUID);
                errorToClient(
                        player,
                        new Error("Image Deletion Error", "You are not the original uploader of this image and therefore not allowed to delete it!")
                );
                return;
            }

            Path parentDir = hidden ? CustomImageDirectory.getHiddenCustomImageDir() : CustomImageDirectory.getCustomImageDir();

            Path imagePath = parentDir.resolve(imageUUID + ".png");
            Path thumbnailPath = parentDir.resolve(imageUUID + "_thumbnail.png");
            Path metadataPath = parentDir.resolve(imageUUID + "_metadata.json");

            try {
                Files.deleteIfExists(imagePath);
                Files.deleteIfExists(thumbnailPath);
                Files.deleteIfExists(metadataPath);

                // Remove from metadata list
                metadataMap.remove(imageUUID);
                perPlayerCounts.computeIfPresent(metadata.getUploaderUUID(), (k, v) -> {
                    v.decrementAndGet();
                    return v.get() <= 0 ? null : v;
                });

                totalEntries.decrementAndGet();
                if (hidden) hiddenEntries.decrementAndGet();
                else publicEntries.decrementAndGet();
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to delete image image for UUID {}: {}", imageUUID, e.getMessage());
            }
        });
    }



    /**
     * Converts a byte array to a UUID.
     * @param uuidBytes The byte array containing the UUID, encoded as a UTF-8 string.
     * @return The UUID represented by the byte array.
     */
    private static UUID byteToUUID(byte[] uuidBytes) {
        return UUID.fromString(new String(uuidBytes, StandardCharsets.UTF_8));
    }

    private static boolean isOfValidFormat(String format) {
        return format.equals("png") || format.equals("jpeg") || format.equals("jpg") || format.equals("bmp");
    }


    /**
     * Sends an error to the client, which is displayed in a GUI.
     * @param player The player to send the error to.
     * @param error The error to send.
     */
    private static void errorToClient(ServerPlayerEntity player, Error error) {
        CustomServerNetworking.getInstance().sendBytesToClient(
                player,
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "get_server_error"),
                error.toBytes(),
                -1,
                -1);
    }
}
