package rozprochy.lab4.server;

import rozprochy.lab4.bank.server.SystemManagerImpl;
import Ice.Identity;

public class Server extends Ice.Application {
    
    public static void main(String[] args) {
        Server server = new Server();
        int status = server.main("server", args);
        System.exit(status);
    }
    
    private class ShutdownHook implements Runnable {
        
        @Override
        public void run() {
            System.out.println("\rShutting down...");
            System.out.print("Destroying communicator...");
            communicator().destroy();
            System.out.println("done");
            System.out.println("Shutdown procedure completed.");
        }
        
    }
    
    private void setupBank() {
        System.out.print("Creating bank adapter...");
        System.out.flush();
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Bank");
        System.out.println("done");
        System.out.print("Activating system manager servant");
        System.out.flush();
        SystemManagerImpl system = new SystemManagerImpl(adapter);
        Identity id = communicator().stringToIdentity("Bank/Manager");
        adapter.add(system, id);
        adapter.activate();
        System.out.println("System manager servant activated");
        System.out.println("Bank application initialization successfully " + 
                "finished");
    }
    
    private void setupCommunicator() {
        
    }

    @Override
    public int run(String[] args) {
        System.out.print("Installing shutdown hook...");
        System.out.flush();
        setInterruptHook(new Thread(new ShutdownHook()));
        System.out.println("done");
        setupBank();
        setupCommunicator();
        System.out.println("Initialization finished, server running.");
        communicator().waitForShutdown();
        return 0;
    }

}