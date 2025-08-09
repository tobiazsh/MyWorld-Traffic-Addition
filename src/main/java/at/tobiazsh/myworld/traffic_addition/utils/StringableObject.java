package at.tobiazsh.myworld.traffic_addition.utils;

public interface StringableObject<C> {
    C fromString(String str);
    String toObjectString();
}
