package rozprochy.rok2012.lab1.zad4;


import java.nio.charset.Charset;

/**
 * Auxilary class to handle string encoding/decoding
 */
public class Utils {

    /** Available in java.nio.charset.StandardCharsets since 1.7 :( */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    
    /**
     * Encodes string in UTF-8 and returns resulting byte array.
     */
    public static byte[] encode(String string) {
        if (string != null) {
            return string.getBytes(UTF_8);
        } else {
            return new byte[0];
        }
    }

    /**
     * Decodes UTF-8-encoded string, and returns it.
     */
    public static String decode(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

}