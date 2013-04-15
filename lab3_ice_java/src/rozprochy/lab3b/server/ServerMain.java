package rozprochy.lab3b.server;




public class ServerMain {

    public static void main(String[] args) {
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Server server = new Server(ic);
            server.run();
            ic.shutdown();
        } catch (Ice.LocalException e) {
            e.printStackTrace(System.err);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (ic != null) {
                try { ic.destroy(); }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

}
