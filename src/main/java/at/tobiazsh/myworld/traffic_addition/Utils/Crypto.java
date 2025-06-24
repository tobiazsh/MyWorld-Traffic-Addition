package at.tobiazsh.myworld.traffic_addition.Utils;

import java.util.Base64;

public class Crypto {

    /**
     * Encrypts a string using Base64 and also makes it URL-safe by replacing certain characters. Only decode with the decode method from this class.
     * @param plainText The string to be encoded
     * @return The encoded string
     */
    public static String encodeBase64(String plainText) {
        String encoded = Base64.getEncoder().encodeToString(plainText.getBytes()); // Encode
        encoded = encoded.replace("+", "-").replace("/", "_").replace("=", ""); // Make URL-Safe
        return encoded;
    }

    /**
     * Decrypts a string that was encoded using the encryptBase64 method. Only use this method to decode.
     * @param encodedText The string to be decrypted
     * @return The decrypted string
     */
    public static String decodeBase64(String encodedText) {

        // Add padding if necessary
        int mod = encodedText.length() % 4;
        if (mod != 0) {
            encodedText += "====".substring(mod);
        }

        encodedText = encodedText.replace("-", "+").replace("_", "/"); // Make URL-Unsafe

        byte[] decodedBytes = Base64.getDecoder().decode(encodedText); // Decode
        return new String(decodedBytes);
    }
}
