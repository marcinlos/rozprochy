package rozprochy.lab4.server;

import java.util.Map;

import rozprochy.lab4.bank.server.SystemManagerImpl;
import rozprochy.lab4.chat.server.ChatSystemManager;
import Ice.Identity;
import Ice.Properties;

public class Server extends Ice.Application {
    
    private static final String PREFIX = "[Server] ";
    
    public static void main(String[] args) {
        Server server = new Server();
        int status = server.main("server", args);
        System.exit(status);
    }
    
    private class ShutdownHook implements Runnable {
        
        @Override
        public void run() {
            System.out.printf("\r%sShutting down...\n", PREFIX);
            System.out.print(PREFIX + "Destroying communicator...");
            communicator().destroy();
            System.out.println("done");
            System.out.println(PREFIX + "Shutdown procedure completed.");
        }
        
    }
    
    private void setupBank() {
        System.out.print(PREFIX + "Creating bank adapter...");
        System.out.flush();
        Properties props = communicator().getProperties();
        Map<String, String> config = props.getPropertiesForPrefix("BankApp");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Bank");
        System.out.println("done");
        System.out.println(PREFIX + "Activating bank servant");
        SystemManagerImpl system = new SystemManagerImpl(adapter, config);
        Identity id = communicator().stringToIdentity("Bank/Manager");
        adapter.add(system, id);
        adapter.activate();
        System.out.println(PREFIX + "Bank servant activated");
        System.out.println(PREFIX + "Bank application initialization " + 
                "successfully finished");
    }
    
    private void setupChat() {
        System.out.print(PREFIX + "Creating chat adapter...");
        System.out.flush();
        Properties props = communicator().getProperties();
        Map<String, String> config = props.getPropertiesForPrefix("ChatApp");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Chat");
        System.out.println("done");
        System.out.println(PREFIX + "Activating chat servant");
        ChatSystemManager system = new ChatSystemManager(adapter, config);
        Identity id = communicator().stringToIdentity("Chat/Manager");
        adapter.add(system, id);
        adapter.activate();
        System.out.println(PREFIX + "Chat servant activated");
        System.out.println(PREFIX  +"Chat application initialization " + 
                "successfully finished");
    }

    @Override
    public int run(String[] args) {
        System.out.print(PREFIX + "Installing shutdown hook...");
        System.out.flush();
        setInterruptHook(new Thread(new ShutdownHook()));
        System.out.println("done");
        setupBank();
        setupChat();
        System.out.println(PREFIX + "Initialization finished, server running.");
        communicator().waitForShutdown();
        return 0;
    }

}
