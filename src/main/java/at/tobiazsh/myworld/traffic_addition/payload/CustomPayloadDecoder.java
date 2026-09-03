package at.tobiazsh.myworld.traffic_addition.payload;

public interface CustomPayloadDecoder<T extends CustomPayload> {
    T decode(byte[] data);
}
