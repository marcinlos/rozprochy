package rozprochy.lab3.server;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.omg.CORBA.ORB;

public class ServerMain {

    private ORB orb;
    private Server server;

    private ServerMain() { }

    private void start(String[] args) {
        registerShutdownHook();
        try {
            orb = ORB.init(args, null);
            server = new Server(orb);
            server.run();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void cleanup() {
        if (orb != null) {
            if (server != null) {
                server.cleanup();
            }
            System.out.println("Waiting for ORB shutdown...");
            orb.shutdown(true);
            orb.destroy();
            System.out.println("Done.");
        }
    }

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

    /*
     * Registers a shutdown hook, which causes the cleanup to be performed even
     * when the application is shut down in a brutal manner (e.g. after ctrl+c).
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanup();
            }
        });
    }

    public static void main(String[] args) {
        ServerMain sv = new ServerMain();
        sv.start(args);
    }

}

