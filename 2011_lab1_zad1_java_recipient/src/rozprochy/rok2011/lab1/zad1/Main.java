package rozprochy.rok2011.lab1.zad1;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        Server server = new Server(args, new Incrementor());
        try {
            server.run();
        } catch (IOException e) {
            logger.severe("Fatal server error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            try { server.close(); }
            catch (IOException e) { 
                logger.severe("Error while closing: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

}
