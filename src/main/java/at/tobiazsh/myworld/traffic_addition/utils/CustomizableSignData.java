package at.tobiazsh.myworld.traffic_addition.utils;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 22:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElementInterface;
import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity.*;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getBlockPosAtDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class CustomizableSignData {
	public String jsonString; // JSON as String
	public JsonObject json = new JsonObject(); // JSON

	public static class ElementsContainer {
		JsonArray Elements;

		public ElementsContainer() {
			Elements = new JsonArray();
		}

		public void addElement(JsonObject element) {
			Elements.add(element);
		}
	}

	public void setJsonString(String string) {
		jsonString = string;
		json = JsonParser.parseString(string).getAsJsonObject();
	}

	public void setJson(JsonObject json) {
		this.json = json;
		updateString();
	}

	/**
	 * Creates the JSON for the background of the sign for this instance of SignStyleJson
	 * @param path The path to the style
	 * @return The SignStyleJson object
	 */
	public CustomizableSignData setStyle(String path) {

		if (json.has("Style")) json.remove("Style");
		if (path == null) return this;
		if (path.isBlank()) return this;

		path = path.replaceAll("\\\\", "/"); // Replace windows backslashes with forward slashes
		if (path.charAt(0) != '/') path = "/" + path; // Add first slash if not present
		if (path.charAt(path.length() - 1) == '/') path = path.substring(0, path.length() - 1); // Remove last slash if present

		json.addProperty("Style", path);
		updateString();

		return this;
	}

	/**
	 * Sets the elements of this SignStyleJson object to the given list of BaseElements. Commonly used to store NBT data in the block.
	 * @param elements The list of BaseElements to set
	 * @return The SignStyleJson object
	 */
	public CustomizableSignData setElements(List<? extends BaseElement> elements) {
		ElementsContainer container = new ElementsContainer();

		elements.forEach(element -> container.addElement(element.toJson()));

		Gson gson = new GsonBuilder().create();

		// Serialize container to JSON
		JsonObject elementsJsonObject = gson.toJsonTree(container).getAsJsonObject();

		// Merge with local JSON Variable
		for (String key : elementsJsonObject.keySet()) {
			json.add(key, elementsJsonObject.get(key));
		}

		updateString();

		return this;
	}

	/**
	 * Sets the JSON of this SignStyleJson object to the given JSON String
	 * @param jsonString The JSON String to convert
	 * @return The SignStyleJson object
	 */
	public CustomizableSignData setJson(String jsonString) {
		json = JsonParser.parseString(jsonString).getAsJsonObject();
		updateString();
		return this;
	}

	/**
	 * Deconstructs the background (style) of the sign from JSON to a list of textures
	 * @param customizableSignData The SignStyleJson object to deconstruct
	 * @return The JSON String
	 */
	public static List<String> getBackgroundTexturePathList(CustomizableSignData customizableSignData, CustomizableSignBlockEntity blockEntity) {
		List<String> textures = new ArrayList<>();

		BlockPos pos = blockEntity.getPos();
		World world = blockEntity.getWorld();

		if (pos == null) return textures;
		if (world == null) return textures;

		if (!customizableSignData.json.has("Style")) return textures;

		BlockPos scanPosX = pos;
		BlockPos scanPosY = pos;

		// The direction that's on the right side of the block
		Direction rightSide = getRightSideDirection(getFacing(blockEntity).getOpposite());

		List<String> textureNames = new ArrayList<>();

		// Scan the blocks around the masterPos and get the necessary textures. Then store them in a list
		while (world.getBlockEntity(scanPosY) instanceof CustomizableSignBlockEntity) {
			while (world.getBlockEntity(scanPosX) instanceof CustomizableSignBlockEntity csbEntity) {
				// ALTERNATIVE: BorderProperty borders = getBorderListBoundingBased(scanPosX, world); // Get the true/false map for the border at that pos
				BorderProperty borders = csbEntity.getBorderType();
				textureNames.add(borders.toNormalString()); // Get the texture name based on the border map

				scanPosX = getBlockPosAtDirection(rightSide, scanPosX, 1);
			}

			scanPosY = scanPosY.up();
			scanPosX = scanPosY;
		}

		if (styleMatchesOldVersion(customizableSignData)) {
            updateToNewVersion(customizableSignData);
			blockEntity.setSignTextureJson(customizableSignData.jsonString);
        }

		String relativeTexturePath = customizableSignData.json.get("Style").getAsString();

		textureNames.forEach(textureName -> {
			String texture = relativeTexturePath + "/" + textureName + ".png";
			textures.add(texture);
		});

        return textures;
	}

	/**
	 * Deconstructs the elements from the JSON to a list of BaseElements
	 * @param customizableSignData The SignStyleJson object to deconstruct
	 * @return A list of BaseElements
	 */
	public static List<BaseElement> deconstructElementsToArray(CustomizableSignData customizableSignData) {
		JsonArray elements = customizableSignData.json.getAsJsonArray("Elements");

		List<BaseElement> elementsList = new ArrayList<>();

		for (JsonElement elementElement : elements) {
			JsonObject elementObject = elementElement.getAsJsonObject();
			BaseElement element = BaseElementInterface.fromJson(elementObject);

			if (element == null) {
				MyWorldTrafficAddition.LOGGER.error("Couldn't deconstruct element {} because it's null! It is likely that no ID for this element has been found and thus cannot target the right element!", elementElement);
				continue;
			}

			elementsList.add(element);
		}

		return elementsList;
	}

	private void updateString() {
		jsonString = json.toString();
	}

	/**
	 * Checks if the style of the sign is in the old format
	 * @param css The SignStyleJson object to check
	 * @return True if the style is in the old format, false if not
	 */
	public static boolean styleMatchesOldVersion(CustomizableSignData css) {
		if (!css.json.has("Style")) return false;

		String style = css.json.get("Style").getAsString();
		return style.contains(";");
	}

	/*
	 * Example of the old style:
	 * "-350;69;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_bottom_left.png*
	 * -349;69;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_bottom.png*
	 * -348;69;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_bottom_right.png*
	 * -350;70;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_top_left.png*
	 * -349;70;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_top.png*
	 * -348;70;764;/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/highway/border_top_right.png"
	 */

	/**
	 * Updates the style of the sign to the new version
	 * @param css The SignStyleJson object to update
	 * @return The updated SignStyleJson object
	 */

	public static CustomizableSignData updateToNewVersion(CustomizableSignData css) {
		if (!css.json.has("Style")) return css;
		if (!styleMatchesOldVersion(css)) return css;

		String style = css.json.get("Style").getAsString();

		String[] styleParts = style.split("\\*"); // Split each one by '*'
		String firstPart = styleParts[0]; // Get the first part because we only need one here
		String[] splitStyle = firstPart.split(";"); // Split the first part by ';'

		String pathStr = splitStyle[3]; // Get the path
		Path path = Path.of(pathStr).getParent(); // Convert the path to a Path object
		String newStyle = path.toString(); // Convert the relative path to a string
		newStyle = newStyle.replace("\\", "/"); // Replace backslashes with forward slashes

		if (newStyle.charAt(0) != '/') newStyle = "/" + newStyle; // Add the first slash (/) if it doesn't exist
		css.setStyle(newStyle); // Set the new style

		return css;
	}

	public static String getPrettyJson(String json) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement jsonElement = JsonParser.parseString(json);
		return gson.toJson(jsonElement);
	}
}
