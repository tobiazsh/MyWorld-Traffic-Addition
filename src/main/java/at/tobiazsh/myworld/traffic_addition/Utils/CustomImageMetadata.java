package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CustomImageMetadata {
    private final UUID imageUUID;
    private final String imageName;
    private final UUID uploaderUUID;
    private final Instant creationDateTime;
    private final ZonedDateTime creationDateTimeLocal;
    private final boolean hidden;
    private final JsonObject rawData;
    private CompletableFuture<String> uploaderNameFuture = null;

    public CustomImageMetadata(JsonElement metadata) {
        JsonObject obj = metadata.getAsJsonObject();

        this.imageUUID = UUID.fromString(obj.get("ImageUUID").getAsString());
        this.imageName = obj.get("ImageName").getAsString();
        this.uploaderUUID = UUID.fromString(obj.get("UploaderUUID").getAsString());
        this.creationDateTime = Instant.parse(obj.get("CreationDate").getAsString());
        this.creationDateTimeLocal = creationDateTime.atZone(ZoneId.systemDefault());
        this.hidden = obj.get("Hidden").getAsBoolean();
        this.rawData = obj;
    }

    public UUID getImageUUID() {
        return imageUUID;
    }

    public String getImageName() {
        return Crypto.decodeBase64(imageName);
    }

    public boolean isHidden() {
        return hidden;
    }

    public UUID getUploaderUUID() {
        return uploaderUUID;
    }

    public Instant getCreationDateUTC() {
        return creationDateTime;
    }

    public String getImageNameEncoded() {
        return imageName;
    }

    public JsonObject getRawData() {
        return rawData;
    }

    public ZonedDateTime getCreationDateLocal() {
        return creationDateTimeLocal;
    }

    public CompletableFuture<String> getUploaderName() {
        if (uploaderNameFuture != null) {
            uploaderNameFuture.cancel(false); // Cancel previous request if one is already in progress
            MyWorldTrafficAddition.LOGGER.info("Cancelled previous request for thumbnail data!");
        }

        uploaderNameFuture = new CompletableFuture<>();

        Thread thread = new Thread(() -> {
            try {
                URI uri = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/" + this.getUploaderUUID().toString().replaceAll("-", ""));
                HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                MyWorldTrafficAddition.LOGGER.info("(Fetching Username) Response Code from Mojang's API: {}", responseCode);

                if (responseCode == 200) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                        if (response.isEmpty() || !response.has("name")) {
                            uploaderNameFuture.completeExceptionally(new Exception("Response is empty or doesn't contain field 'name'!"));
                        } else {
                            uploaderNameFuture.complete(response.get("name").getAsString());
                        }
                    }
                } else {
                    uploaderNameFuture.completeExceptionally(new Exception("Response code is not 200! Response code: " + responseCode));
                }
            } catch (Exception e) {
                uploaderNameFuture.completeExceptionally(e);
            }
        });

        thread.setName("FetchUploaderName");
        thread.start();

        return uploaderNameFuture;
    }
}
