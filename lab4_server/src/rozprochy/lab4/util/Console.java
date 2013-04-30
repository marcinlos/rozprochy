package rozprochy.lab4.util;

public class Console {

    private Console() {
        // Non-instantiable
    }
    
    // Colors
    public static final String COLORS[] = {
        "\u001B[1;32m",
        "\u001B[1;34m",
        "\u001B[1;36m",
        "\u001B[1;31m",
        "\u001B[1;35m",
        "\u001B[1;33m",
    };
    
    public static final String RESET = "\u001B[0m";
    
    public static final String GREEN   = COLORS[0];
    public static final String BLUE    = COLORS[1];
    public static final String CYAN    = COLORS[2];
    public static final String RED     = COLORS[3];
    public static final String PURPLE  = COLORS[4];
    public static final String BROWN   = COLORS[5];

}
