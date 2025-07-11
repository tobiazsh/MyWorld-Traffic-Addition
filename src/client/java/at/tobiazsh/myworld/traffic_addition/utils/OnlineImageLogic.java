package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.networking.CustomClientNetworking;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlineImageLogic {

    private static int imageCount = 0;
    private static int privateImageCount = 0;
    private static List<CustomImageMetadata> currentMetadataList = new ArrayList<>();
    private static List<byte[]> thumbnailData = null;
    private static CompletableFuture<Integer> privateImageCountFuture = null;
    private static CompletableFuture<Integer> imageCountFuture = null;
    private static CompletableFuture<List<CustomImageMetadata>> metadataFuture = null;
    private static CompletableFuture<List<byte[]>> thumbnailFuture = null;

    public static int getImageCount() {
        return imageCount;
    }

    /**
     * Fetches the total number of uploaded images from the server.
     * @return A CompletableFuture that will be completed with the total number of uploaded images.
     */
    public static CompletableFuture<Integer> fetchEntryCount() {
        if (imageCountFuture != null) {
            imageCountFuture.cancel(false); // Cancel previous request if one is already in progress
            MyWorldTrafficAddition.LOGGER.info("Cancelled previous request for total uploaded images!");
        }

        imageCountFuture = new CompletableFuture<>();

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_total_uploaded_images"),
                new byte[1],
                -1, -1
        ); // Dummy byte & -1 for no limits in transmission

        return imageCountFuture;
    }

    public static void setImageCount(byte[] bytes) {
        int num = Integer.parseInt(new String(bytes));
        imageCount = num;
        imageCountFuture.complete(num);
    }



    /**
     * Get the number of private images uploaded by the player.
     * @return A CompletableFuture that will be completed with the number of private images uploaded by the player.
     */
    public static CompletableFuture<Integer> fetchPrivateEntryCount() {
        if (privateImageCountFuture != null) {
            privateImageCountFuture.cancel(false); // Cancel previous request if one is already in progress
            MyWorldTrafficAddition.LOGGER.info("Cancelled previous request for private uploaded images!");
        }

        privateImageCountFuture = new CompletableFuture<>();

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_private_uploaded_images"),
                new byte[1],
                -1, -1
        ); // Dummy byte & -1 for no limits in transmission

        return privateImageCountFuture;
    }

    public static void setPrivateImageCount(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();
        int num = buffer.getInt();
        privateImageCount = num;
        privateImageCountFuture.complete(num);
    }



    /**
     * Fetches metadata for the specified images from the server.
     * @param from Starting index (inclusive)
     * @param to Ending index (exclusive)
     * @return A CompletableFuture that will be completed with the metadata as a JsonObject.
     */
    public static CompletableFuture<List<CustomImageMetadata>> fetchImageMetadata(int from, int to, boolean privateImages) {
        if (metadataFuture != null) {
            metadataFuture.cancel(false); // Cancel previous request if one is already in progress
            MyWorldTrafficAddition.LOGGER.info("Cancelled previous request for image metadata!");
        }

        metadataFuture = new CompletableFuture<>();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2 + 1); // Start and End Index + 1 byte for privateImages flag
        buffer.putInt(from);
        buffer.putInt(to);
        buffer.put(BooleanUtils.toByte(privateImages)); // 1 byte to indicate if private images are requested

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_entries_metadata"),
                buffer.array(),
                -1, -1
        ); // Dummy byte & -1 for no limits in transmission

        return metadataFuture;
    }

    public static void setMetadataList(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();

        int numberOfEntries = buffer.getInt();

        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < numberOfEntries; i++) {
            int length = buffer.getInt();
            byte[] jsonBytes = new byte[length];
            buffer.get(jsonBytes);
            String jsonString = new String(jsonBytes);
            JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
            list.add(jsonObject);
        }

        currentMetadataList = list.stream().map(CustomImageMetadata::new).toList();
        metadataFuture.complete(currentMetadataList);
    }


    /**
     * Fetches the thumbnail of an image from the server.
     * @param imageUUID The UUID of the image
     * @return A CompletableFuture that will be completed with the thumbnail data as a byte array (encoded in PNG).
     */
    public static CompletableFuture<List<byte[]>> fetchThumbnails(List<UUID> imageUUID) {
        if (thumbnailFuture != null) {
            thumbnailFuture.cancel(false); // Cancel previous request if one is already in progress
            MyWorldTrafficAddition.LOGGER.info("Cancelled previous request for thumbnail data!");
        }

        thumbnailFuture = new CompletableFuture<>();

        // Convert UUIDs to byte array
        List<String> uuidString = imageUUID.stream().map(UUID::toString).toList();
        List<byte[]> uuidBytes = uuidString.stream().map(String::getBytes).toList();

        int totalSize = 0;
        for (byte[] uuid : uuidBytes)
            totalSize += Integer.BYTES + uuid.length; // 4 bytes for length + length of UUID

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        for (byte[] uuid : uuidBytes) {
            buffer.putInt(uuid.length);
            buffer.put(uuid);
        }

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_thumbnail_data"),
                buffer.array(),
                -1, -1
        ); // Dummy byte & -1 for no limits in transmission

        return thumbnailFuture;
    }

    public static void setThumbnailData(byte[] bytes) {
        List<byte[]> unpackedThumbnails = unpackImageData(bytes);
        thumbnailData = unpackedThumbnails;
        thumbnailFuture.complete(unpackedThumbnails);
    }

    /**
     * Returns unpacks thumbnails from ByteBuffer
     * @return List of byte arrays containing the unpacked thumbnail data in whatever stupid format it is in
     */
    private static List<byte[]> unpackImageData(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();

        List<byte[]> ls = new ArrayList<>();
        while (buffer.hasRemaining()) {
            int len = buffer.getInt();
            byte[] by = new byte[len];
            buffer.get(by);
            ls.add(by);
        }

        return ls;
    }

    private final static Map<UUID, CompletableFuture<byte[]>> imageRequests = Collections.synchronizedMap(new HashMap<>());

    public static CompletableFuture<byte[]> fetchImage(final CompletableFuture<byte[]> future, UUID imageUUID) {
        executor.submit(() -> {
            UUID requestId = UUID.randomUUID();
            imageRequests.put(requestId, future);

            byte[] uuidBytes = imageUUID.toString().getBytes();
            byte[] requestIdBytes = requestId.toString().getBytes();

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2 + uuidBytes.length + requestIdBytes.length);
            buffer.putInt(uuidBytes.length);
            buffer.putInt(requestIdBytes.length);
            buffer.put(uuidBytes);
            buffer.put(requestIdBytes);

            CustomClientNetworking.getInstance().sendBytesToServer(
                    Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_data"),
                    buffer.array(),
                    -1, -1
            ); // Dummy byte & -1 for no limits in transmission

        });

        return future;
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void setImageData(byte[] bytes) {
        executor.submit(() -> {
            ByteBuffer data = ByteBuffer.wrap(bytes);
            data.rewind();

            boolean success = BooleanUtils.fromByte(data.get());

            int requestIdLength = data.getInt();
            int imageDataLength = data.getInt();

            byte[] requestIdBytes = new byte[requestIdLength];
            data.get(requestIdBytes);

            UUID requestId = UUID.fromString(new String(requestIdBytes));

            if (!success || imageDataLength <= 0 || !imageRequests.containsKey(requestId)) {
                String message = "Failed to fetch image data for request ID: " + requestId + "! Cause is unknown, but likely the request doesn't exist anymore or the image doesn't exist on the server anymore! Make sure the image exists and isn't empty!";

                MyWorldTrafficAddition.LOGGER.error(message);
                imageRequests.get(requestId).completeExceptionally(new Exception(message));

                return; // Handle failure case
            }

            byte[] imageData = new byte[imageDataLength];
            data.get(imageData);

            imageRequests.get(requestId).complete(imageData);
        });
    }
}
