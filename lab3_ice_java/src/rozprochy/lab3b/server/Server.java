package rozprochy.lab3b.server;

import java.io.IOException;

import rozprochy.lab3b.common.CommandInterpreter;
import Ice.Identity;
import Ice.Properties;

public class Server {
    
    private static final String DEFAULT_ADAPTER_ADDRESS = "default -p 6666";
    private static final String DEFAULT_ADAPTER_NAME = "FactoryAdapter";
    private static final String DEFAULT_FACTORY_NAME = "Factory";
    
    private final Properties config;
    private final String adapterName;
    private final String adapterAddress;
    private Ice.ObjectAdapter adapter;
    private final String factoryName;
    
    private AFactoryI factory;
    
    private final Ice.Communicator ice;

    public Server(Ice.Communicator ice) {
        this.ice = ice;
        this.config = ice.getProperties();
        this.adapterAddress = config.getPropertyWithDefault("Adapter.Address", 
                DEFAULT_ADAPTER_ADDRESS);
        this.adapterName = config.getPropertyWithDefault("Adapter.Name", 
                DEFAULT_ADAPTER_NAME);
        this.factoryName = config.getPropertyWithDefault("Factory.Name",
                DEFAULT_FACTORY_NAME);
        System.out.printf("Creating an adapter (name = %s, address = %s) ...\n",
                adapterName, adapterAddress);
        createAdapter();
        System.out.printf("Creating factory (name = %s) ...\n", factoryName);
        createFactory();
        System.out.println("Activating adapter...");
        activate();
        System.out.println("Done.");
    }
    
    public void createAdapter() {
        adapter = ice.createObjectAdapterWithEndpoints(adapterName, 
                adapterAddress);
    }
    
    public void createFactory() {
        factory = new AFactoryI(adapter);
        Identity id = ice.stringToIdentity(factoryName);
        adapter.add(factory, id);
    }
    
    public void activate() {
        adapter.activate();
    }
    
    public void run() throws IOException {
        CommandInterpreter cli = new CommandInterpreter();
        cli.run();
    }

}
