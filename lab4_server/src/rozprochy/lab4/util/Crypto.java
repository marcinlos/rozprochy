package rozprochy.lab4.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class Crypto {

    private Crypto() {
        // Non-instantiable
    }
    
    private static SecureRandom rng = new SecureRandom();
    
    public static final int SESSION_ID_SIZE = 16;

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
    
    public static byte[] computeHash(String password, byte[] salt) {
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(salt);
            hasher.update(StringUtil.encode(password));
            return hasher.digest();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No SHA-256 provider available");
            throw new RuntimeException(e);
        }
    }
    
    public static boolean compareDigests(byte[] a, byte[] b) {
        if (a.length == b.length) {
            for (int i = 0; i < a.length; ++ i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    
    public static boolean authenticate(String password, byte[] salt, 
            byte[] hashedPassword) {
        byte[] value = Crypto.computeHash(password, salt);
        return Crypto.compareDigests(hashedPassword, value);
    }
}
