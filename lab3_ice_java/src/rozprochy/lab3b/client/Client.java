package rozprochy.lab3b.client;

import java.io.IOException;

import rozprochy.lab3b.common.CommandInterpreter;
import Ice.Properties;
import MiddlewareTestbed.AFactoryPrx;
import MiddlewareTestbed.AFactoryPrxHelper;


public class Client {

    private static final String DEFAULT_FACTORY = "Factory:default -p 6666";
    private final Properties config;
    private final String factoryAddress;
    private final Ice.Communicator ice;
    private AFactoryPrx factory;

    public Client(Ice.Communicator ice) {
        this.ice = ice;
        this.config = ice.getProperties();
        factoryAddress = config.getPropertyWithDefault("Factory.Address", 
                DEFAULT_FACTORY);
        System.out.println("Obtaining factory proxy...");
        getFactory();
        System.out.println("Done.");
    }
    
    private void getFactory() {
        Ice.ObjectPrx objPrx = ice.stringToProxy(factoryAddress);
        factory = AFactoryPrxHelper.checkedCast(objPrx);
    }
    
    public void run() throws IOException {
        CommandInterpreter cli = new CommandInterpreter();
        cli.run();
    }

}
