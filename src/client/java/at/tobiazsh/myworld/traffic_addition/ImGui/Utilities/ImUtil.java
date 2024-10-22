package at.tobiazsh.myworld.traffic_addition.ImGui.Utilities;


/*
 * @created 04/10/2024 (DD/MM/YYYY) - 15:10
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import imgui.ImGui;

public class ImUtil {

	public static class Colors {
		public static final Color red = new Color(217, 62, 62, 255);
		public static final Color green = new Color(100, 255, 100, 255);
	}

	// Sets ImGui cursor centered horizontally for next element
	public static void centerHorizontal(float screenW, float elemW) {
		ImGui.setCursorPosX((screenW - elemW) / 2);
	}

	// Sets ImGui cursor centered vertically for next element
	public static void centerVertical(float screenH, float elemH) {
		ImGui.setCursorPosY((screenH - elemH) / 2);
	}

	// Sets ImGui cursor centered both vertically and horizontally for next element
	public static void center(float screenH, float screenW, float elemH, float elemW) {
		centerVertical(screenH, elemH);
		centerHorizontal(screenW, elemW);
	}
}
