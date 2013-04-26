package rozprochy.lab3b.client;


public class ClientMain {

    public static void main(String[] args) {
        Ice.Communicator ice = null;
        try {
            ice = Ice.Util.initialize(args);
            Client client = new Client(ice);
            client.run();
        } catch (Exception e) { 
            handleException(e);
        } finally {
            destroyIce(ice);
        }
    }
    
    private static void handleException(Exception e) {
        e.printStackTrace(System.err);
    }
    
    private static void destroyIce(Ice.Communicator ice) {
        if (ice != null) {
            try { ice.destroy(); }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

}
