package at.tobiazsh.myworld.traffic_addition;

public class ModVars {

    public enum SIGN_SELECTION_TYPE {
        TRIANGULAR_UPSIDE_UP,
        TRIANGULAR_UPSIDE_DOWN,
        ROUND,
        OCTAGONAL,
        RECT_SMALL,
        RECT_MEDIUM,
        RECT_LARGE,
        RECT_STRETCH_SMALL,
        RECT_STRETCH_MEDIUM,
        RECT_STRETCH_LARGE,
        SQUARE_TURN_45
    }

    public static int getSignSelectionEnumInt (SIGN_SELECTION_TYPE type) {
        switch (type) {
            case TRIANGULAR_UPSIDE_UP -> { return 0; }
            case TRIANGULAR_UPSIDE_DOWN -> { return 1; }
            case OCTAGONAL -> { return 3; }
            case RECT_SMALL -> { return 4; }
            case RECT_MEDIUM -> { return 5; }
            case RECT_LARGE -> { return 6; }
            case RECT_STRETCH_SMALL -> { return 7; }
            case RECT_STRETCH_MEDIUM -> { return 8; }
            case RECT_STRETCH_LARGE -> { return 9; }
            case SQUARE_TURN_45 -> { return 10; }
            default -> { return 2; }
        }
    }

    public static SIGN_SELECTION_TYPE getSignSelectionEnum (int num) {
        switch (num) {
            case 0 -> { return SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_UP; }
            case 1 -> { return SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_DOWN; }
            case 3 -> { return SIGN_SELECTION_TYPE.OCTAGONAL; }
            case 4 -> { return SIGN_SELECTION_TYPE.RECT_SMALL; }
            case 5 -> { return SIGN_SELECTION_TYPE.RECT_MEDIUM; }
            case 6 -> { return SIGN_SELECTION_TYPE.RECT_LARGE; }
            case 7 -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_SMALL; }
            case 8 -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_MEDIUM; }
            case 9 -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_LARGE; }
            case 10 -> { return SIGN_SELECTION_TYPE.SQUARE_TURN_45; }
            default -> { return SIGN_SELECTION_TYPE.ROUND; }
        }
    }

    public static SIGN_SELECTION_TYPE getSignSelectionEnumFromString (String str) {
        switch (str) {
            case "triangular" -> { return SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_UP; }
            case "upside_down_triangular" -> { return SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_DOWN; }
            case "octagonal" -> { return SIGN_SELECTION_TYPE.OCTAGONAL; }
            case "rect_small" -> { return SIGN_SELECTION_TYPE.RECT_SMALL; }
            case "rect_medium" -> { return SIGN_SELECTION_TYPE.RECT_MEDIUM; }
            case "rect_large" -> { return SIGN_SELECTION_TYPE.RECT_LARGE; }
            case "rect_stretch_small" -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_SMALL; }
            case "rect_stretch_medium" -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_MEDIUM; }
            case "rect_stretch_large" -> { return SIGN_SELECTION_TYPE.RECT_STRETCH_LARGE; }
            case "square_turn_45" -> { return SIGN_SELECTION_TYPE.SQUARE_TURN_45; }
            default -> { return SIGN_SELECTION_TYPE.ROUND; }
        }
    }

    public enum ImGuiWindowIds {
        ABOUT,
        DEMO,
        PREF
    }
}
