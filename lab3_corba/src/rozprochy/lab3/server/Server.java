package rozprochy.lab3.server;

import java.io.IOException;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import rozprochy.lab3.common.CORBAException;
import rozprochy.lab3.common.CORBAUtil;
import rozprochy.lab3.common.Command;
import rozprochy.lab3.common.CommandInterpreter;

public class Server {
    
    private static final String POA_NAME = "ItemPOA";
    private NameComponent[] name;
    
    private ORB orb;
    private NamingContextExt nameService;
    
    private POA rootPOA;
    private POA factoryPOA;
    
    private AFactoryImpl factory;
    private final String factoryName;
    
    private CommandInterpreter cli;
    
    /**
     * Creates a new server exporting laboratory and device objects.
     * All the initialization takes place in this constructor, when 
     * it returns {@link #run()} can be called.
     * 
     * @param orb ORB object to use
     * @throws CORBAException
     */
    public Server(ORB orb) throws CORBAException {
        this.orb = orb;
        this.factoryName = System.getProperty("factory.name", "factory");
        System.out.println("Obtaining name service...");
        this.nameService = getNameService();
        System.out.println("Obtaining root POA...");
        this.rootPOA = getRootPOA();
        activate();
        System.out.println("Server initialized.");
    }

    public void run() throws IOException {
        cli = new CommandInterpreter();
        cli.registerHandler("list", new Command() {
            @Override public boolean execute(String cmd, Scanner input) {
                factory.printItems();
                return true;
            }
        });
        cli.run();
    }

    public void cleanup() {
        System.out.println("\rServer cleanup...");
        try {
            nameService.unbind(name);
        } catch (NotFound e) {
            e.printStackTrace();
        } catch (CannotProceed e) {
            e.printStackTrace();
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            e.printStackTrace();
        }
    }


    /*
     * Obtains reference to name service.
     */
    private NamingContextExt getNameService() throws CORBAException {
        try {
            Object obj = orb.resolve_initial_references("NameService");
            NamingContextExt nameService = NamingContextExtHelper.narrow(obj);
            return nameService;
        } catch (InvalidName e) {
            System.err.println("Cannot obtain a reference to name service");
            System.err.println("Make sure it's running and its address is "
                    + "correctly passed");
            throw new CORBAException(e);
        }
    }

    /*
     * Obtains reference to the root POA. Does not activate it.
     */
    private POA getRootPOA() throws CORBAException {
        try {
            Object obj = orb.resolve_initial_references("RootPOA");
            POA poa = POAHelper.narrow(obj);
            return poa;
        } catch (InvalidName e) {
            System.err.println("Cannot obtain a reference to name root POA");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Creates & activates object adapters.
     */
    private void activate() throws CORBAException {
        try {
            System.out.println("Creating POA...");
            factoryPOA = makePOA();
            System.out.println("Activating POA manager...");
            rootPOA.the_POAManager().activate();
            System.out.println("Activating factory...");
            activateFactoryServant();
            System.out.println("Registering factory in Name Service...");
            bindFactoryName();
        } catch (AdapterInactive e) {
            System.err.println("Attempt to activate the POA manager while" + 
                    "shutting down. Programmer error, maybe?");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Creates POA
     */
    private POA makePOA() throws CORBAException {
        try {
            // TODO: Use appropriate policies
            Policy[] policies = new Policy[0];
            POAManager manager = rootPOA.the_POAManager();
            return rootPOA.create_POA(POA_NAME, manager, policies);
        } catch (AdapterAlreadyExists e) {
            System.err.println("Adapter " + POA_NAME + " already exists");
            throw new CORBAException(e);
        } catch (InvalidPolicy e) {
            System.err.println("Invalid policy specified for lab POA");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Registers factory servant in the POA.
     */
    private void activateFactoryServant() throws CORBAException {
        try {
            this.factory = new AFactoryImpl(factoryPOA);
            factoryPOA.activate_object(factory);
        } catch (ServantAlreadyActive e) {
            System.err.println("Laboratory servant activated more than once");
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            System.err.println("Laboratory POA policy does not allow " + 
                    "simple activation");
            throw new CORBAException(e);
        }
    }
    
    /*
     * Binds the factory object in the name service.
     */
    private void bindFactoryName() throws CORBAException {
        try {
            Object labRef = factoryPOA.servant_to_reference(factory);
            name = nameService.to_name(factoryName);
            nameService.bind(name, labRef);
        } catch (ServantNotActive e) {
            System.err.println("Cannot obtain laboratory reference, " +
                    "it has not been activated");
            throw new CORBAException(e);
        } catch (WrongPolicy e) {
            System.err.println("Cannot register laboratory at name " + 
                    "service, wrong policy");
            throw new CORBAException(e);
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            System.err.println("Cannot register laboratory, name `" + 
                    factoryName + "' is invalid");
            throw new CORBAException(e);
        } catch (NotFound e) {
            String reason = CORBAUtil.formatNotFoundReason(e);
            System.err.println(reason);
            System.err.println("(full name: `" + factoryName + "'");
            throw new CORBAException(e);
        } catch (CannotProceed e) {
            System.err.println("Some problem encountered while going down " + 
                    "the naming contexts tree by using `" + factoryName + 
                    "'");
            String rest = CORBAUtil.formatName(e.rest_of_name);
            System.err.println("Remaining part of path: " + rest);
            throw new CORBAException(e);
        } catch (AlreadyBound e) {
            System.err.println("Laboratory (" + factoryName + ") " + 
                    "already bound");
            throw new CORBAException(e);
        }
    }
    
}
