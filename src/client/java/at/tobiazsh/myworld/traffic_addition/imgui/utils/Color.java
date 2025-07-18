package at.tobiazsh.myworld.traffic_addition.imgui.utils;


/*
 * @created 04/10/2024 (DD/MM/YYYY) - 16:03
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import java.util.HashMap;
import java.util.Map;

public class Color {
	public short red = 255;
	public short green = 255;
	public short blue = 255;
	public short alpha = 255;

	private static final Map<float[], Integer> hexCache = new HashMap<>();

	public void setColors(int r, int g, int b, int a) {
		checkValues(r, g, b, a);

		red = (short) r;
		green = (short) g;
		blue = (short) b;
		alpha = (short) a;
	}

	/**
	 * Returns the color as hex in the format of AARRGGBB
	 * @param rgba The RGBA values as an array of floats
	 * @return The color as an integer in hex format (AARRGGBB)
	 */
	public static int toHexARGB(float[] rgba) {
		if (rgba.length != 4) {
			System.err.println("Error (Colors): RGBA array must have a length of 4!");
			return 0;
		}

		if (colorIsInCache(rgba))
			return getCacheColHex(rgba);

		String a = normalizeHexLength(Integer.toHexString((int) (rgba[3] * 255)));
		String r = normalizeHexLength(Integer.toHexString((int) (rgba[0] * 255)));
		String g = normalizeHexLength(Integer.toHexString((int) (rgba[1] * 255)));
		String b = normalizeHexLength(Integer.toHexString((int) (rgba[2] * 255)));

		String argbHex = a + r + g + b;

		int hex = (int) Long.parseLong(argbHex, 16);
		putHexCache(rgba, hex); // Cache the color since I assume it takes a lot of resources to convert

		return hex;
	}

	private static String normalizeHexLength(String s) {
		return (s.length() == 1) ? "0" + s : s;
	}

	private static int getCacheColHex(float[] rgba) {
		if (!colorIsInCache(rgba)) {
			System.err.println("Error (Colors): Color is not in cache!");
			return 0;
		}

		return hexCache.get(rgba);
	}

	private static void putHexCache(float[] rgba, int hex) {
		if (colorIsInCache(rgba)) {
			System.err.println("Error (Colors): Color is already in cache!");
			return;
		}

		hexCache.put(rgba, hex);
	}

	private static boolean colorIsInCache(float[] rgba) {
		if (hexCache == null) return false;
		return hexCache.containsKey(rgba);
	}

	public Color (int r, int g, int b, int a) {
		setColors(r, g, b, a);
	}

	// Checks if each value is in range of 0 to 255
	private static void checkValues(int r, int g, int b, int a) {
		int[] values = {r, g, b, a};

		for (int value : values) {
			if (value < 0 || value > 255) System.err.println("Error (Colors): At least one value is not in range of 0 to 255!");
		}
	}
}
