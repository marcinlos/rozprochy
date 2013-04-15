package rozprochy.lab3.client;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.omg.CORBA.ORB;

public class ClientMain {
    
    private static void handleException(Throwable e) {
        System.err.println("Shutting down, see crash.log for details");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("crash.log");
            writer.println("Fatal error: " + e.getMessage());
            e.printStackTrace(writer);
        } catch (FileNotFoundException e1) {
            System.err.println("Cannot create log file");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void main(String[] args) {
        ORB orb = ORB.init(args, null);
        try {
            Client client = new Client(orb);
            client.run();
        } catch (Exception e) {
            handleException(e);
        } finally {
            orb.destroy();
        }
    }

}

