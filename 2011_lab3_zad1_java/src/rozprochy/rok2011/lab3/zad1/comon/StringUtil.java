package rozprochy.rok2011.lab3.zad1.comon;

import java.nio.charset.Charset;

/**
 * Auxilary class to handle string encoding/decoding
 */
public class StringUtil {

    /** Non-instantiable */
    private StringUtil() { }

    /** Available in java.nio.charset.StandardCharsets since 1.7 :( */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Encodes a string as UTF-8
     * 
     * @param string string to be encoded
     * @return Array of bytes with UTF-8-representation of the string, or empty
     *         array, if {@code string} is {@code null}
     */
    public static byte[] encode(String string) {
        if (string != null) {
            return string.getBytes(UTF_8);
        } else {
            return new byte[0];
        }
    }

    /**
     * Decodes a string from UTF-8
     * 
     * @param bytes array of bytes with UTF-8-representation of a string
     * @return {@code String} represented by {@code bytes}
     */
    public static String decode(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

}
