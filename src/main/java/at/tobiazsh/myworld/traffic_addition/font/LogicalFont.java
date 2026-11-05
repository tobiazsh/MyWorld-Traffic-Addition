package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * A record representing a logical font, which only contains the hash of the font file used. This is to send the font
 * that's been used to the server, so it can be used for identifying the font used on everybody else's client.
 *
 * @param fontHash The computed hash of the font file's content used.
 */
public record LogicalFont(String fontHash) {
    public static LogicalFont of(String fontHash) {
        return new LogicalFont(fontHash);
    }

    /**
     * Hashes the given TTF file and returns a LogicalFont instance representing it.
     *
     * @param ttfFile The TTF file to hash and create a LogicalFont from.
     * @throws IOException If an I/O error occurs while reading the TTF file.
     * @return A LogicalFont instance representing the hashed TTF file.
     */
    public static LogicalFont ofFile(FileSystem.File ttfFile) throws IOException {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try (var is = ttfFile.getInputStream()) {
            byte[] buffer = new byte[8192];

            int read;

            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }

        return LogicalFont.of(HexFormat.of().formatHex(md.digest()));
    }
}
