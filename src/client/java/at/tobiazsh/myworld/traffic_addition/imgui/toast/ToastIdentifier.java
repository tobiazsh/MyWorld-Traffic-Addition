package at.tobiazsh.myworld.traffic_addition.imgui.toast;

import java.security.SecureRandom;

public class ToastIdentifier {
    private final String nanoId;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcedefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ToastIdentifier(String nanoId) {
        this.nanoId = nanoId;
    }

    public static ToastIdentifier ofRandom() {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            int index = RANDOM.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }

        return new ToastIdentifier(sb.toString());
    }

    @Override
    public String toString() {
        return nanoId;
    }
}