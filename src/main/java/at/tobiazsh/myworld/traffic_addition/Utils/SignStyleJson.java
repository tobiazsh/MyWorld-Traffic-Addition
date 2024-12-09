package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 22:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


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

public class SignStyleJson {
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
	public SignStyleJson setStyle(String path, CustomizableSignBlockEntity customizableSignBlockEntity) {

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
	public SignStyleJson setElements(List<? extends BaseElement> elements, CustomizableSignBlockEntity customizableSignBlockEntity) {
		BlockPos pos = customizableSignBlockEntity.getMasterPos();
		String position = pos.getX() + ";" + pos.getY() + ";" + pos.getZ();

		ElementsContainer container = new ElementsContainer();

		elements.forEach(element -> {
			JsonObject object = new JsonObject();

			ELEMENT_TYPE elementType = ELEMENT_TYPE.NONE;

			String elementName = element.name;
			String elementSize = element.getWidth() + "x" + element.getHeight();
			String elementPosition = element.getX() + ";" + element.getY();
			float elementFactor = element.getFactor();
			float elementRotation = element.getRotation();
			float[] color = element.getColor();

			elementName = elementName.isEmpty() || elementName.isBlank() ? "UNKNOWN" : elementName;

			object.addProperty("Pos", position);
			object.addProperty("Name", elementName);
			object.addProperty("Size", elementSize);
			object.addProperty("ElementPos", elementPosition);
			object.addProperty("Rotation", elementRotation);
			object.addProperty("Factor", elementFactor);
			object.addProperty("ColorR", color[0]);
			object.addProperty("ColorG", color[1]);
			object.addProperty("ColorB", color[2]);
			object.addProperty("ColorA", color[3]);

			if (element instanceof ImageElement) {
				elementType = ELEMENT_TYPE.IMAGE_ELEMENT;

				if (((ImageElement) element).getResourcePath().isBlank() || ((ImageElement) element).getResourcePath().isEmpty()) {
					return;
				}

				String elementTexture = ((ImageElement) element).getResourcePath();
				object.addProperty("Texture", elementTexture);
			} else if (element instanceof TextElement)  {
				elementType = ELEMENT_TYPE.TEXT_ELEMENT;

				if (((TextElement) element).getText().isBlank() || ((TextElement) element).getText().isEmpty() || ((TextElement) element).getFont() == null) {
					return;
				}

				float fontSize = ((TextElement) element).getFont().getFontSize();
				String fontPath = ((TextElement) element).getFont().getFontPath();
				String text = ((TextElement) element).getText();

				object.addProperty("FontSize", fontSize);
				object.addProperty("FontPath", fontPath);
				object.addProperty("Text", text);
			}

			object.addProperty("ElementType", elementType.ordinal());

			container.addElement(object);
		});

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
	public SignStyleJson convertStringToJson(String jsonString) {
		json = JsonParser.parseString(jsonString).getAsJsonObject();
		updateString();
		return this;
	}

	/**
	 * Deconstructs the background (style) of the sign from JSON to a list of textures
	 * @param signStyleJson The SignStyleJson object to deconstruct
	 * @return The JSON String
	 */
	public static List<String> deconstructStyleToArray(SignStyleJson signStyleJson) {
		List<String> textures = new ArrayList<>();

		if (!signStyleJson.json.has("Style")) return textures;
		String constructedJson = signStyleJson.json.get("Style").toString();

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
	 * @param signStyleJson The SignStyleJson object to deconstruct
	 * @return A list of BaseElements
	 */
	public static List<BaseElement> deconstructElementsToArray(SignStyleJson signStyleJson) {
		JsonArray elements = signStyleJson.json.getAsJsonArray("Elements");

		List<BaseElement> elementsList = new ArrayList<>();

		for (JsonElement elementElement : elements) {
			JsonObject elementObject = elementElement.getAsJsonObject();

			String posStr = elementObject.get("Pos").getAsString();
			String name = elementObject.get("Name").getAsString();
			String sizeStr = elementObject.get("Size").getAsString();
			String elemPosStr = elementObject.get("ElementPos").getAsString();
			ELEMENT_TYPE elementType = ELEMENT_TYPE.values()[elementObject.get("ElementType").getAsInt()]; // Retrieve enumerator; Element Type
			float rotation = elementObject.get("Rotation").getAsFloat();
			float factor = elementObject.get("Factor").getAsFloat();

			float[] color = new float[]{
					elementObject.get("ColorR").getAsFloat(),
					elementObject.get("ColorG").getAsFloat(),
					elementObject.get("ColorB").getAsFloat(),
					elementObject.get("ColorA").getAsFloat()
			};

			String[] pos = posStr.split(";");
			BlockPos masterPos = new BlockPos(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));

			String[] size = sizeStr.split("x");
			float width = Float.parseFloat(size[0]);
			float height = Float.parseFloat(size[1]);

			String[] elemPos = elemPosStr.split(";");
			float x = Float.parseFloat(elemPos[0]);
			float y = Float.parseFloat(elemPos[1]);

			BaseElement element = null;

			if (elementType.equals(ELEMENT_TYPE.IMAGE_ELEMENT)) {
				String texture = elementObject.get("Texture").getAsString();
				element = new ImageElement(x, y, width, height, 1, texture);
			} else if (elementType.equals(ELEMENT_TYPE.TEXT_ELEMENT)) {
				String fontPath = elementObject.get("FontPath").getAsString();
				String text = elementObject.get("Text").getAsString();
				float fontSize = elementObject.get("FontSize").getAsFloat();

				element = new TextElement(x, y, width, height, rotation, factor, new BasicFont(fontPath, fontSize), text, false);
			}

            assert element != null;
            element.name = name;
			element.setRotation(rotation);
			element.setFactor(factor);
			element.setColor(color);
			
			elementsList.add(element);
		}

		return elementsList;
	}

	private void updateString() {
		jsonString = json.toString();
	}
}
