package at.tobiazsh.myworld.traffic_addition.utils;

import java.io.*;
import java.util.List;

public class ListUtils {

    public static <C> byte[] toByteArray(List<C> list) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(list);
        byteArrayOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <C> List<C> fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (List<C>) objectInputStream.readObject();
    }
}
