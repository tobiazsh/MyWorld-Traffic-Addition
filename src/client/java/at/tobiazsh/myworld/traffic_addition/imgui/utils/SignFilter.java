package at.tobiazsh.myworld.traffic_addition.imgui.utils;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.sign.SignTexture;

/**
 * A Filter for the Sign Selector Search
 */
public record SignFilter(SignTexture.CATEGORY category, String country, SignBlock.SIGN_SHAPE shape) {

    /**
     * Checks if the given sign texture matches this filter.
     *
     * @param signTexture the sign texture to check
     * @return true if the sign texture matches the filter, false otherwise
     */
    public boolean matches(SignTexture signTexture) {

        if (category != null && category != SignTexture.CATEGORY.OTHER)
            return false; // No match

        if (country != null && !signTexture.country().equalsIgnoreCase(country))
            return false; // No match

        if (shape != null && signTexture.shape() != shape)
            return false; // No match

        return true; // Match or ignored
    }
}