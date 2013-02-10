package rozprochy.rok2011.lab1.zad2;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    

    public static void main(String[] args) {
        try {
            Server server = Server.fromArguments(args);
            server.run();
        } catch (Exception e) {
            logger.severe("Fatal server error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
