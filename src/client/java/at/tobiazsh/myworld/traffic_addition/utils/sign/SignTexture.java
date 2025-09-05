package at.tobiazsh.myworld.traffic_addition.utils.sign;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.exception.SignTextureParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

/**
 * A record for storing texture information for a sign.
 *
 * @param name the name of the texture
 * @param path The path to the texture file. Must be relative to the mod jar's assets folder OR an absolute path on the filesystem.
 * @param isInModJar Whether the path is relative to the mod jar's assets folder (true) or an absolute path on the filesystem (false).
 * @param category the category of the sign {@link CATEGORY} (for filtering purposes)
 * @param country The country the sign belongs to. Handled as a simple string for maximum flexibility.
 */
public record SignTexture (String name, Path path, boolean isInModJar, CATEGORY category, SignBlock.SIGN_SHAPE shape, String country) {

    public enum CATEGORY {
        REGULATORY,
        WARNING,
        PRIORITY,
        OTHER
    }

    private static final HashSet<String> countries = new HashSet<>();

    /**
     * Parses a SignTexture from a Json root.
     *
     * @param defaults Any defaults defined in the textures.json file. Can be null.
     * @param entry The Json root to parse the texture from. If shape is missing, it'll be taken from defaults. If it's missing from defaults too, a {@link SignTextureParseException} is thrown. CANNOT be null.
     * @param category The category of the sign. If missing, a {@link SignTextureParseException} is thrown. CANNOT be null.
     * @param isInModJar Whether the path is relative to the mod jar's assets
     * @param country The country the sign belongs to. CANNOT be null.
     * @param relativeTo The path of the entry file (textures.json). Must be either an absolute path on the filesystem or relative path to the mod's jar-assets folder!
     */
    public static SignTexture parse(
            @Nullable JsonObject defaults,
            @NotNull JsonObject entry,
            @NotNull CATEGORY category,
            boolean isInModJar,
            @NotNull String country,
            @NotNull Path relativeTo
    ) throws SignTextureParseException {

        String name;
        Path path;
        SignBlock.SIGN_SHAPE shape = null;

        // First check if the provided category has any defaults
        JsonObject categoryDefault = null;
        if (defaults != null && defaults.has(category.name().toLowerCase()))
            categoryDefault = defaults.getAsJsonObject(category.name().toLowerCase());

        // Now get the defaults (e.g. shape)
        if (categoryDefault != null && categoryDefault.has("shape"))
            shape = SignBlock.SIGN_SHAPE.valueOf(categoryDefault.get("shape").getAsString().toUpperCase());

        // Check if all obligatory fields are present
        if (!entry.has("name") || !entry.has("path") || (!entry.has("shape") && shape == null)) // If either name or path is missing and if shape is missing and doesn't have a default to replace it with
            throw new SignTextureParseException("Missing obligatory field in sign texture entry: " + entry);

        // Now parse the fields
        name = tr("SignTextures", entry.get("name").getAsString());

        String raw = entry.get("path").getAsString().trim().replace('\\', '/');


        // Following "/" means root-relative to the resource folder
        if (raw.startsWith("/"))
            raw = raw.substring(1); // Remove first slash

        path = relativeTo.resolve(raw).normalize();

        return new SignTexture(name, path, isInModJar, category, shape, country);
    }

    /**
     * Parses a whole file containing sign texture definitions. The file must contain a "country" attribute anywhere in the root root and a "textures" array containing the actual textures, which are seperated into each type or "other".
     *
     * @param file The file to parse the textures from.
     * @param isInModJar Whether the path is relative to the mod jar's assets folder (true) or an absolute path on the filesystem (false).
     * @throws SignTextureParseException If an error occurs while parsing the file.
     */
    @SuppressWarnings("ConstantConditions")
    public static @NotNull List<SignTexture> parseFile(Path file, boolean isInModJar) throws SignTextureParseException {
        StringBuilder fileContent = new StringBuilder();

        try (BufferedReader reader = isInModJar
                ? new BufferedReader(
                        new InputStreamReader(
                                        MyWorldTrafficAddition.class.getResourceAsStream(
                                            file.toString().replace("\\", "/"))))
                : new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(file.toFile())))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (IOException e) {
            throw new SignTextureParseException("An error occurred while trying to read the sign textures from the file: " + file, e);
        }

        JsonObject root = JsonParser.parseString(fileContent.toString()).getAsJsonObject();

        if (!root.has("textures")) { // No textures = No need to do anything :)
            MyWorldTrafficAddition.LOGGER.warn("No textures found in sign texture file: {}", file);
            return new ArrayList<>();
        }

        if (!root.has("country"))
            return new ArrayList<>(); // No country

        String country = root.get("country").getAsString();
        countries.add(country);

        JsonObject defaults;
        if (root.has("defaults"))
            defaults = root.getAsJsonObject("defaults");
        else
            defaults = null;

        JsonObject textures = root.getAsJsonObject("textures");
        List<SignTexture> parsedTextures = new ArrayList<>();
        for (String categoryName : textures.keySet()) {
            CATEGORY category;

            try {
                category = CATEGORY.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException e) {
                MyWorldTrafficAddition.LOGGER.warn("Unknown category: {}", categoryName);
                continue; // Invalid category, skip
            }

            JsonArray categoryArray = textures.getAsJsonArray(categoryName);

            categoryArray.forEach(entry -> {
                if (!entry.isJsonObject()) {
                    MyWorldTrafficAddition.LOGGER.warn("Invalid entry in sign texture file: {} Entry: {}", file, entry);
                    return; // Invalid entry, skip
                }

                SignTexture texture = parse(
                        defaults,
                        entry.getAsJsonObject(),
                        category,
                        isInModJar,
                        country,
                        file.getParent()
                );

                parsedTextures.add(texture);
            });
        }

        return parsedTextures;
    }


    // GETTERS

    public static HashSet<String> getCountries() {
        return countries;
    }
}
