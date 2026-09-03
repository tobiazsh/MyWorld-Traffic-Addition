package at.tobiazsh.myworld.traffic_addition.payload.custom_network;

import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.payload.CustomPayload;
import at.tobiazsh.myworld.traffic_addition.payload.CustomPayloadDecoder;
import com.google.gson.JsonObject;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public record SetCustomizableSignTexturePayload(int x, int y, int z, CustomizableSignTextureData textureData) implements CustomPayload {

    public static final CustomPayloadDecoder<SetCustomizableSignTexturePayload> DECODER = data -> {
        ByteBuffer buf = ByteBuffer.wrap(data);

        try {
            int x = buf.getInt();
            int y = buf.getInt();
            int z = buf.getInt();
            int jsonLength = buf.getInt();

            byte[] jsonBytes = new byte[jsonLength];
            buf.get(jsonBytes);

            String jsonString = new String(jsonBytes);
            JsonObject textureDataJson = new JsonObject();
            textureDataJson.addProperty("texture", jsonString);

            var textureData = CustomizableSignTextureData.fromJson(textureDataJson);

            return new SetCustomizableSignTexturePayload(x, y, z, textureData);
        } finally {
            MemoryUtil.memFree(buf);
        }
    };

    @Override
    public byte[] encode() {
        byte[] jsonBytes = textureData.toJson().toString().getBytes();

        // X, Y, Z, JSON String Length, JSON String
        int bufferSize = (Integer.BYTES * 4) + jsonBytes.length;

        ByteBuffer buf = MemoryUtil.memAlloc(bufferSize);
        try {
            buf.putInt(x);
            buf.putInt(y);
            buf.putInt(z);
            buf.putInt(jsonBytes.length);
            buf.put(jsonBytes);

            buf.flip();

            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            return bytes;
        } finally {
            MemoryUtil.memFree(buf);
        }
    }
}
