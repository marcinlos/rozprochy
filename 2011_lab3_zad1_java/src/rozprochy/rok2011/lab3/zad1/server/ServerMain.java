package rozprochy.rok2011.lab3.zad1.server;

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

    private void handleException(Throwable e) {
        System.err.println("Fatal error: " + e.getMessage());
        e.printStackTrace(System.err);
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
