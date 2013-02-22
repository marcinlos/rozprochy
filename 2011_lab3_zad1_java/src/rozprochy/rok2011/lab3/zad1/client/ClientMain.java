package rozprochy.rok2011.lab3.zad1.client;

import org.omg.CORBA.ORB;

public class ClientMain {
    
    private static void handleException(Throwable e) {
        System.err.println("Fatal error: " + e.getMessage());
        e.printStackTrace(System.err);
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
