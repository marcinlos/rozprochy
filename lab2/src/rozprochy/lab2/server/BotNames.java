package rozprochy.lab2.server;

import java.util.Random;

public class BotNames {

    private BotNames() { }
    
    private static final String[] NAMES = {
        "Bach", "Beethoven", "Berlioz", "Bruckner", "Barber",
        "Brahms", "Balakirev", "Bartok", "Bizet", "Bernstein"
    };
    
    public static String randomName() {
        int n = NAMES.length;
        Random rand = new Random();
        return NAMES[rand.nextInt(n)];
    }

}
