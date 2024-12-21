package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 22:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity.*;
import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity.getFacing;

public class CustomizableSignStyle {
	public String jsonString; // JSON as String
	public JsonObject json = new JsonObject(); // JSON

	private enum ELEMENT_TYPE{
		NONE,
		IMAGE_ELEMENT,
		TEXT_ELEMENT
	}

	public static class ElementsContainer {
		JsonArray Elements;

		public ElementsContainer() {
			Elements = new JsonArray();
		}

		public void addElement(JsonObject element) {
			Elements.add(element);
		}
	}

	/**
	 * Creates the JSON for the background of the sign for this instance of SignStyleJson
	 * @param path The path to the style
	 * @param customizableSignBlockEntity The CustomizableSignBlockEntity to gather the correct borders
	 * @return The SignStyleJson object
	 */
	public CustomizableSignStyle setStyle(String path, CustomizableSignBlockEntity customizableSignBlockEntity) {

		if (json.has("Style")) json.remove("Style");

		BlockPos pos = customizableSignBlockEntity.getMasterPos();
		BlockPos posX = pos;
		BlockPos posY = pos;
		StringBuilder textureList = new StringBuilder();
		World world = customizableSignBlockEntity.getWorld();

        assert world != null;
        Direction rightSide = getRightSideDirection(getFacing(pos, world).getOpposite());

		while(world.getBlockEntity(posY) instanceof CustomizableSignBlockEntity) {
			while(world.getBlockEntity(posX) instanceof CustomizableSignBlockEntity) {
				List<Boolean> borders = getBorderListBoundingBased(posX, world);
				String textureName = getBorderName(borders.get(0), borders.get(1), borders.get(2), borders.get(3), "border");

				textureList
						.append(posX.getX()).append(";")
						.append(posX.getY()).append(";")
						.append(posX.getZ()).append(";")
						.append(path).append(textureName).append(".png");

				posX = getBlockPosAtDirection(rightSide, posX, 1);
				if (world.getBlockEntity(posX) instanceof CustomizableSignBlockEntity) textureList.append("*");
			}

			posY = posY.up();
			posX = posY;
			if (world.getBlockEntity(posY) instanceof CustomizableSignBlockEntity) textureList.append("*");
		}

		json.addProperty("Style", textureList.toString());
		updateString();

		return this;
	}

	/**
	 * Sets the elements of this SignStyleJson object to the given list of BaseElements. Commonly used to store NBT data in the block.
	 * @param elements The list of BaseElements to set
	 * @param customizableSignBlockEntity The CustomizableSignBlockEntity to gather the correct position
	 * @return The SignStyleJson object
	 */
	public CustomizableSignStyle setElements(List<? extends BaseElement> elements, CustomizableSignBlockEntity customizableSignBlockEntity) {
		ElementsContainer container = new ElementsContainer();

		elements.forEach(element -> container.addElement(toJson(element)));

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
	 * Converts the JSON String to a JSON Object
	 * @param jsonString The JSON String to convert
	 * @return The SignStyleJson object
	 */
	public CustomizableSignStyle convertStringToJson(String jsonString) {
		json = JsonParser.parseString(jsonString).getAsJsonObject();
		updateString();
		return this;
	}

	/**
	 * Deconstructs the background (style) of the sign from JSON to a list of textures
	 * @param customizableSignStyle The SignStyleJson object to deconstruct
	 * @return The JSON String
	 */
	public static List<String> deconstructStyleToArray(CustomizableSignStyle customizableSignStyle) {
		List<String> textures = new ArrayList<>();

		if (!customizableSignStyle.json.has("Style")) return textures;
		String constructedJson = customizableSignStyle.json.get("Style").toString();

		// Step 1: Split by '*'
		String[] segments = constructedJson.split("\\*");

		// Step 2: Create a 2D array to hold the results
		String[][] result = new String[segments.length][];

		// Step 3: Split each segment by ';'
		for (int i = 0; i < segments.length; i++) {
			result[i] = segments[i].split(";");
		}

        for (String[] strings : result) {
            textures.add(strings[3]);
        }

		// Just removing quote since they seem to be there and I don't really get why and this causes a crash since the last quote is left behind and included in the texture path?
		textures.replaceAll(s -> s.replaceAll("\"", ""));

		return textures;
	}

	/**
	 * Deconstructs the elements from the JSON to a list of BaseElements
	 * @param customizableSignStyle The SignStyleJson object to deconstruct
	 * @return A list of BaseElements
	 */
	public static List<BaseElement> deconstructElementsToArray(CustomizableSignStyle customizableSignStyle) {
		JsonArray elements = customizableSignStyle.json.getAsJsonArray("Elements");

		List<BaseElement> elementsList = new ArrayList<>();

		for (JsonElement elementElement : elements) {
			JsonObject elementObject = elementElement.getAsJsonObject();
			elementsList.add(fromJson(elementObject));
		}

		return elementsList;
	}

	/**
	 * Converts an element of the type BaseElement to a JSON Object
	 * @param element The BaseElement to convert
	 * @return The JSON Object
	 */
	public static JsonObject toJson(BaseElement element) {
		JsonObject object = new JsonObject();

		JsonArray elementPosition = new JsonArray();
		for (float v : new float[]{element.getX(), element.getY()}) elementPosition.add(v);
		object.add("ElementPosition", elementPosition); // X, Y

		JsonArray color = new JsonArray();
		for (float v : element.getColor()) color.add(v);
		object.add("Color", color); // R, G, B, A

		JsonArray size = new JsonArray();
		for (float v : new float[]{element.getWidth(), element.getHeight()}) size.add(v);
		object.add("Size", size); // X, Y

		object.addProperty("Rotation", element.getRotation());
		object.addProperty("Factor", element.getFactor());
		object.addProperty("Name", element.getName());

		if (element instanceof ImageElement) {
			object.addProperty("ElementType", ELEMENT_TYPE.IMAGE_ELEMENT.ordinal());
			object.addProperty("Texture", ((ImageElement) element).getResourcePath());
		} else if (element instanceof TextElement) {
			object.addProperty("ElementType", ELEMENT_TYPE.TEXT_ELEMENT.ordinal());
			object.addProperty("Text", ((TextElement) element).getText());
			object.addProperty("FontPath", ((TextElement) element).getFont().getFontPath());
			object.addProperty("FontSize", ((TextElement) element).getFont().getFontSize());
		}

		return object;
	}

	/**
	 * Converts a JSON Object to an element of the type BaseElement
	 * @param object The JSON Object to convert
	 * @return The converted BaseElement
	 */
	public static BaseElement fromJson(JsonObject object) {
		BaseElement element;

		JsonArray elementPosJson = object.getAsJsonArray("ElementPosition");
		float[] elementPos = new float[]{elementPosJson.get(0).getAsFloat(), elementPosJson.get(1).getAsFloat()}; // X, Y

		JsonArray colorJson = object.getAsJsonArray("Color");
		float[] color = new float[]{colorJson.get(0).getAsFloat(), colorJson.get(1).getAsFloat(), colorJson.get(2).getAsFloat(), colorJson.get(3).getAsFloat()}; // R, G, B, A

		JsonArray sizeJson = object.getAsJsonArray("Size");
		float[] size = new float[]{sizeJson.get(0).getAsFloat(), sizeJson.get(1).getAsFloat()}; // Width, Height

		float rotation = object.get("Rotation").getAsFloat();
		float factor = object.get("Factor").getAsFloat();
		ELEMENT_TYPE type = ELEMENT_TYPE.values()[object.get("ElementType").getAsInt()];
		String name = object.get("Name").getAsString();

		if (type == ELEMENT_TYPE.IMAGE_ELEMENT) {

			element = new ImageElement(
					elementPos[0],
					elementPos[1],
					size[0], size[1],
					factor,
					rotation,
					object.get("Texture").getAsString()
			);

		} else if (type == ELEMENT_TYPE.TEXT_ELEMENT) {

			element = new TextElement(
					elementPos[0],
					elementPos[1],
					size[0],
					size[1],
					rotation,
					factor,
					new BasicFont(object.get("FontPath").getAsString(), object.get("FontSize").getAsFloat()),
					object.get("Text").getAsString(),
					false
			);

		} else {
			MyWorldTrafficAddition.LOGGER.error("Error: Couldn't deconstruct elements to JSON! Element type is invalid.");
			return null;
		}

		element.setName(name);
		element.setColor(color);

		return element;
	}

	private void updateString() {
		jsonString = json.toString();
	}
}
