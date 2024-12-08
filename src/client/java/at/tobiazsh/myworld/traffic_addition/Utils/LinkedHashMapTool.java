package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LinkedHashMapTool {
    public static <K, H> int getIndex(LinkedHashMap<K, H> map, K key) {
        int index = 0;
        for (K k : map.keySet()) {
            if (k.equals(key))
                return index;
            index++;
        }
        MyWorldTrafficAddition.LOGGER.error("Key not found in map");

        return -1;
    }

    public static <K, H> int getIndexValue(LinkedHashMap<K, H> map, H value) {
        int index = 0;
        for (H v : map.values()) {
            if (v.equals(value))
                return index;
            index++;
        }
        MyWorldTrafficAddition.LOGGER.error("Value not found in map");

        return -1;
    }

    public static <K, V> K getKeyAtIndex(LinkedHashMap<K, V> map, int index) {
        // Convert keySet to a List to access by index
        List<K> keyList = new ArrayList<>(map.keySet());
        if (index >= 0 && index < keyList.size()) {
            return keyList.get(index);
        } else {
            return null; // Index out of bounds
        }
    }
}
