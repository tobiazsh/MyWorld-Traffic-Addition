package at.tobiazsh.myworld.traffic_addition.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class JsonUtil {

    private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Converts ugly JSON to pretty JSON
     */
    public static String toPrettyJson(String uglyJson) {
        return prettyGson.toJson(JsonParser.parseString(uglyJson));
    }
}
