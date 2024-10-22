package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 06/10/2024 (DD/MM/YYYY) - 15:03
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import java.util.HashMap;
import java.util.Map;

public class Textures {
	public static Map<String, Texture> loadedTextures = new HashMap<>();

	public static boolean textureRegistered(String resourcePath){
		return loadedTextures.containsKey(resourcePath);
	}

	public static Texture getTexture(String resourcePath) {
		if (!textureRegistered(resourcePath)) {
			System.err.println("Error (Texture Database; Retrieving Texture) Texture is not registered yet!");
			return new Texture();
		}

		return loadedTextures.get(resourcePath);
	}

	public static int getTextureId(String resourcePath) {
		if (!textureRegistered(resourcePath)) {
			System.err.println("Error (Texture Database; Retrieving Texture) Texture is not registered yet!");
			return 0;
		}

		return loadedTextures.get(resourcePath).getTextureId();
	}

	// Register texture efficiently
	public static Texture registerTexture(String resourcePath) {
		Texture texture = new Texture();

		if (textureRegistered(resourcePath)) {
			System.out.println("Warning (Registering Texture): Texture already registered! Ignoring command!");
			return texture;
		}

		texture.loadTexture(resourcePath);
		loadedTextures.put(resourcePath, texture);
		return texture;
	}

	// Register Texture efficiently²; If already registered, return texture ID; A = Advanced
	public static Texture smartRegisterTexture(String resourcePath) {
		if (textureRegistered(resourcePath)) {
			return getTexture(resourcePath);
		}

		Texture texture = new Texture();
		texture.loadTexture(resourcePath);
		loadedTextures.put(resourcePath, texture);
		return texture;
	}
}
