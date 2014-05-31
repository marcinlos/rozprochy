package pl.edu.agh.iosr.bank.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Identity;
import Ice.Properties;

public class Server extends Ice.Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    
    public static void main(String[] args) {
        Server server = new Server();
        int status = server.main("server", args);
        System.exit(status);
    }
    
    private class ShutdownHook implements Runnable {
        
        @Override
        public void run() {
            logger.info("Shutting down...");
            communicator().destroy();
            logger.info("Done");
        }
        
    }
    
    private void setupBank() {
        logger.info("Creating BankManager...");
        
        Properties props = communicator().getProperties();
        Map<String, String> config = props.getPropertiesForPrefix("BankApp");
        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Bank");
        
        logger.info("Activating servant");
        Manager system = new Manager(/*adapter, config*/);
        Identity id = communicator().stringToIdentity("Bank/Manager");
        adapter.add(system, id);
        adapter.activate();

        logger.info("BankManager servant activated");
    }
    
    @Override
    public int run(String[] args) {
        logger.debug("Adding shutdown hook");
        setInterruptHook(new Thread(new ShutdownHook()));
        setupBank();
        System.out.println("Initialization completed.");
        communicator().waitForShutdown();
        return 0;
    }

}
