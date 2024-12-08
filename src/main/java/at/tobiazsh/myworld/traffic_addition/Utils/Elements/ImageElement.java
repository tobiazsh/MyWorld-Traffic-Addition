package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:55
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

/*
 * MINIDOCS
 * 1. Create
 * 2. loadTexture()
 * 2. render()
 */

import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;

public class ImageElement extends BaseElement{
	public Texture elementTexture = new Texture();
	public String resourcePath;
	public boolean texIsLoaded = false;

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, Texture texture) {
		super(x, y, width, height, factor);
		this.elementTexture = texture;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, String resourcePath) {
		super(x, y, width, height, factor);
		this.resourcePath = resourcePath;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, Texture texture) {
		this(x, y, width, height, factor, 0, texture);
	}

	public ImageElement(float x, float y, float width, float height, float factor, String resourcePath) {
		this(x, y, width, height, factor, 0, resourcePath);
	}

	public ImageElement(float x, float y, float factor, String resourcePath) {
		this(x, y, 0, 0, factor, resourcePath);
	}

	public ImageElement(float x, float y, String resourcePath) {
		this(x, y, 0, 0, 0, resourcePath);
	}

	public ImageElement(float factor, String resourcePath) {
		this(0, 0, 0, 0, factor, resourcePath);
	}

	public void loadTexture() {
		if (resourcePath.isEmpty()) {
			System.err.println("Error (Loading texture on ImageElement): Couldn't load texture because resource path is empty!");
			return;
		}

		elementTexture = Textures.smartRegisterTexture(resourcePath);
		texIsLoaded = true;
	}

	public Texture getTexture() {
		return elementTexture;
	}

	public void setExplicitTexture(Texture texture) {
		this.elementTexture = texture;
	}

	// Always call after loadTexture() was called!
	public void sizeAuto() {
		if (elementTexture.isEmpty()) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine size because texture hasn't been initialized! Initialize with ImageElement.loadTexture()!");
			return;
		}

		float w = elementTexture.getWidth();
		float h = elementTexture.getHeight();

		if (w == -1) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine width because width in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
			return;
		}

		if (h == -1) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine height because height in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
			return;
		}

		width = w;

		height = h;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public ImageElement setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}
}
