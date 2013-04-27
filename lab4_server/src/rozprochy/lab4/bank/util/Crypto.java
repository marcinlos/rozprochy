package rozprochy.lab4.bank.util;

import java.security.SecureRandom;

public class Crypto {

    private Crypto() {
        // Non-instantiable
    }
    
    private static SecureRandom rng = new SecureRandom();
    
    public static final int SESSION_ID_SIZE = 8;

    public static byte[] randomBytes(int count) {
        byte[] bytes = new byte[count];
        rng.nextBytes(bytes);
        return bytes;
    }
    
    public static String randomBytesHex(int count) {
        return toHex(randomBytes(count));
    }
    
    public static String createSessionId() {
        return randomBytesHex(SESSION_ID_SIZE);
    }
    
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b: data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
