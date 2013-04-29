package rozprochy.lab4.server;

import java.util.Map;

import rozprochy.lab4.bank.server.SystemManagerImpl;
import Ice.Identity;
import Ice.Properties;

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
        Properties props = communicator().getProperties();
        Map<String, String> config = props.getPropertiesForPrefix("BankApp");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Bank");
        System.out.println("done");
        System.out.println("Activating bank servant");
        SystemManagerImpl system = new SystemManagerImpl(adapter, config);
        Identity id = communicator().stringToIdentity("Bank/Manager");
        adapter.add(system, id);
        adapter.activate();
        System.out.println("Bank servant activated");
        System.out.println("Bank application initialization successfully " + 
                "finished");
    }
    
    private void setupChat() {
        System.out.print("Creating chat adapter...");
        System.out.flush();
        Properties props = communicator().getProperties();
        Map<String, String> config = props.getPropertiesForPrefix("ChatApp");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Chat");
        System.out.println("done");
        System.out.println("Activating chat servant");

        adapter.activate();
        System.out.println("Chat servant activated");
        System.out.println("Chat application initialization successfully " + 
                "finished");
    }

    @Override
    public int run(String[] args) {
        System.out.print("Installing shutdown hook...");
        System.out.flush();
        setInterruptHook(new Thread(new ShutdownHook()));
        System.out.println("done");
        setupBank();
        setupChat();
        System.out.println("Initialization finished, server running.");
        communicator().waitForShutdown();
        return 0;
    }

}
