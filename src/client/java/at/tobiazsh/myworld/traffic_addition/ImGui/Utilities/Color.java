package at.tobiazsh.myworld.traffic_addition.ImGui.Utilities;


/*
 * @created 04/10/2024 (DD/MM/YYYY) - 16:03
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


public class Color {
	public short red = 255;
	public short green = 255;
	public short blue = 255;
	public short alpha = 255;

	public void setColors(int r, int g, int b, int a) {
		checkValues(r, g, b, a);

		red = (short) r;
		green = (short) g;
		blue = (short) b;
		alpha = (short) a;
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
