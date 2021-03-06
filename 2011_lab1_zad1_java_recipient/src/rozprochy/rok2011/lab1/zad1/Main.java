package rozprochy.rok2011.lab1.zad1;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        Incrementor service = new Incrementor();
        try {
            Server server = Server.fromArguments(service, args);
            server.run();
        } catch (Exception e) {
            logger.severe("Fatal server error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
