package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Networking.CustomServerNetworking;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlineImageServerLogic {

    public static int entries = 0;
    public static int publicEntries = 0;
    public static int hiddenEntries = 0;
    private static final List<Pair<CustomImageMetadata, Boolean>> metadataList = new CopyOnWriteArrayList<>(); // List of metadata so it is being saved in RAM and avoids unnecessary file I/O

    private static final ExecutorService executorService = Executors.newFixedThreadPool(32);

    /**
     * Processes an uploaded image.
     * @param image The byte array containing the image data, thumbnail, metadata, and hidden status.
     */
    public static void processUploadedImage(byte[] image) {
        // Extract image
        executorService.submit(() -> {
            ByteBuffer buffer = ByteBuffer.wrap(image);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.rewind();

            int imageSize = buffer.getInt();
            int thumbnailSize = buffer.getInt();
            int metadataSize = buffer.getInt();

            byte hiddenByte = buffer.get();
            boolean hidden = hiddenByte == 0;

            byte[] imageData = new byte[imageSize];
            buffer.get(imageData);

            byte[] thumbnailData = new byte[thumbnailSize];
            buffer.get(thumbnailData);

            byte[] metadataData = new byte[metadataSize];
            buffer.get(metadataData);
            String metadata = new String(metadataData, StandardCharsets.UTF_8);
            JsonObject metadataJson = JsonParser.parseString(metadata).getAsJsonObject();
            String imageUUID = metadataJson.get("ImageUUID").getAsString();
            String uploaderUUID = metadataJson.get("UploaderUUID").getAsString();

            CustomImageDir.createCustomImageDir(); // Create custom image directory if it doesn't exist

            Path destinationPath = hidden ?
                    CustomImageDir.getHiddenCustomImageDir() :
                    CustomImageDir.getCustomImageDir();

            File imageFile = new File(destinationPath.resolve(imageUUID + ".png").toAbsolutePath().toString());
            File thumbnailFile = new File(destinationPath.resolve(imageUUID + "_thumbnail.png").toAbsolutePath().toString());
            File metadataFile = new File(destinationPath.resolve(imageUUID + "_metadata.json").toAbsolutePath().toString());

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

            entries++;

            if (hidden) hiddenEntries++;
            else publicEntries++;

            MyWorldTrafficAddition.LOGGER.info("User with UUID {} uploaded custom image with UUID {}!", uploaderUUID, imageUUID);
            metadataList.add(new Pair<>(new CustomImageMetadata(metadataJson), metadataJson.get("Hidden").getAsBoolean())); // Add to list for later use
        });
    }



    /**
     * Counts the number of uploaded images by a specific player based on their UUID and sends it to client.
     * @param player The player to count the entries for and send the count to.
     */
    public static void getEntryNumberByPlayer(ServerPlayerEntity player) {
        executorService.submit(() -> {
            int count = 0;
            UUID playerUUID = player.getUuid();

            for (Pair<CustomImageMetadata, Boolean> entry : metadataList) {
                if (entry.getLeft().getUploaderUUID().equals(playerUUID)) {
                    count++;
                }
            }

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
        Path hiddenImageDir = CustomImageDir.getHiddenCustomImageDir();
        Path customImageDir = CustomImageDir.getCustomImageDir();

        if (!hiddenImageDir.toFile().exists()) // If dir doesn't exist, no uploads have been made; return
            return;

        // Count JSON Files in the directory as they represent image entries. For each uploaded image, there's exactly one JSON file.
        hiddenEntries = processImageDirectory(hiddenImageDir);
        publicEntries = processImageDirectory(customImageDir);
        entries = hiddenEntries + publicEntries;
    }



    /**
     * Processes the image directory to count entries and read metadata.
     * @param hiddenImageDir The directory containing the hidden images.
     * @return The number of entries processed in the directory.
     */
    private static int processImageDirectory(Path hiddenImageDir) {
        int count = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(hiddenImageDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) continue;
                if (entry.getFileName().toString().endsWith(".json")) {
                    String content = new String(Files.readAllBytes(entry));
                    JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                    metadataList.add(new Pair<>(new CustomImageMetadata(jsonObject), jsonObject.get("Hidden").getAsBoolean()));
                    count++;
                }
            }
        } catch (IOException e) {
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
                sendableData = metadataList.stream()
                        .filter(entry -> entry.getLeft().getUploaderUUID().equals(playerUUID))
                        .map(Pair::getLeft)
                        .toList(); // Get only the Json of the entries that are uploaded by the player
            } else {
                sendableData = metadataList.stream()
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
                if (i >= metadataList.size()) break;
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
     * Reads the image image from the custom image directories and returns it as a byte array (encoded in whatever format the image is in). Also supports hidden images
     * @param uuid The UUID of the image to read
     * @param suffix MUST match exactly type (like ".png" or ".jpeg"), otherwise the image will NOT be found. Can help get specific types of the image if the suffix is included (for example for thumbnails: "_thumbnail.png").
     * @return Image image as byte array
     */
    private static byte[] readCustomImageData(String uuid, @NotNull @NotBlank String suffix) {
        String imageName = uuid + suffix;

        // Check if and where image exists
        try {
            if (Files.exists(CustomImageDir.getCustomImageDir().resolve(imageName)))
                return Files.readAllBytes(CustomImageDir.getCustomImageDir().resolve(imageName));

            else if (Files.exists(CustomImageDir.getHiddenCustomImageDir().resolve(imageName)))
                return Files.readAllBytes(CustomImageDir.getHiddenCustomImageDir().resolve(imageName));
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

            if (!Files.exists(CustomImageDir.getCustomImageDir().resolve(imageUUID + ".png"))) {
                sendFailedImageResponse(player, imageUUID, requestIdBytes);
                return; // Exit if image does not exist
            }

            Path imagePath = CustomImageDir.getCustomImageDir().resolve(imageUUID + ".png");

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

            boolean existsInPublicDir = Files.exists(CustomImageDir.getCustomImageDir().resolve(imageUUID + "_metadata.json"));

            Path parentDir = existsInPublicDir ? CustomImageDir.getCustomImageDir() : CustomImageDir.getHiddenCustomImageDir();

            boolean successful = true; // We assume that the deletion is going to be successful

            Path imagePath = parentDir.resolve(imageUUID + ".png");
            Path thumbnailPath = parentDir.resolve(imageUUID + "_thumbnail.png");
            Path metadataPath = parentDir.resolve(imageUUID + "_metadata.json");

            // Compare Uploaders UUID with the player's UUID to verify if the player is allowed to delete the image
            UUID playerUUID = player.getUuid();
            CustomImageMetadata metadata = metadataList.stream()
                    .filter(data -> data.getLeft().getImageUUID().equals(imageUUID))
                    .map(Pair::getLeft)
                    .toList().getFirst(); // Get first match as there should only be one match per UUID (two matches are EXTREMELY (like really extremely) unlikely, but technically possible)

            if (!metadata.getUploaderUUID().equals(playerUUID)) { // Is not original uploader
                MyWorldTrafficAddition.LOGGER.warn("Player with UUID {} and NAME {} tried to delete image with UUID {} but is not the original uploader! Please investigate this issue! It is recommended to take actions against the player because this should only be possible if the player runs a modified mod!", player.getUuid(), player.getName(), imageUUID);
                successful = false; // Deletion was not successful because the player is not the original uploader
            } else {
                try {
                    Files.deleteIfExists(imagePath);
                    Files.deleteIfExists(thumbnailPath);
                    Files.deleteIfExists(metadataPath);

                    // Remove from metadata list
                    metadataList.removeIf(entry -> entry.getLeft().getImageUUID().equals(imageUUID));
                } catch (IOException e) {
                    MyWorldTrafficAddition.LOGGER.error("Failed to delete image image for UUID {}: {}", imageUUID, e.getMessage());
                    successful = true; // Still true so the images get deleted on the client side
                }
            }

            CustomServerNetworking.getInstance().sendBytesToClient(
                    player,
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "delete_image_response"),
                    new byte[BooleanUtils.toByte(successful)],
                    -1, -1
            ); // Send response to client indicating whether the deletion was successful or not
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
}
