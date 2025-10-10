package at.tobiazsh.myworld.traffic_addition.utils;

import org.apache.commons.codec.binary.Base32;

import java.util.Base64;

public class Crypto {

    /**
     * Encodes a string using Base64 and also makes it URL-safe by replacing certain characters. Only decode with the decode method from this class.
     * @param plainText The string to be encoded
     * @return The encoded string
     */
    public static String encodeBase64(String plainText) {
        String encoded = Base64.getEncoder().encodeToString(plainText.getBytes()); // Encode
        encoded = encoded.replace("+", "-").replace("/", "_").replace("=", ""); // Make URL-Safe
        return encoded;
    }

    /**
     * Decodes a string that was encoded using the encryptBase64 method. Only use this method to decode.
     * @param encodedText The string to be decoded
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

    /**
     * Encodes a string using Base32. Note that Base32 encoding typically includes padding characters ('='). This method removes them for a cleaner output.
     * @param plainText The string to be encoded
     * @return The encoded string
     */
    public static String encodeBase32(String plainText) {
        Base32 base32 = new Base32();
        String encoded = base32.encodeAsString(plainText.getBytes());
        encoded = encoded.replace("=", ""); // Remove padding
        return encoded;
    }

    /**
     * Decodes a string that was encoded using the encodeBase32 method. Only use this method to decode.
     * @param encodedText The string to be decoded
     * @return The decrypted string
     */
    public static String decodeBase32(String encodedText) {
        // Add padding if necessary
        int mod = encodedText.length() % 8;
        if (mod != 0) {
            encodedText += "========".substring(mod);
        }

        Base32 base32 = new Base32();
        byte[] decodedBytes = base32.decode(encodedText);
        return new String(decodedBytes);
    }
}
